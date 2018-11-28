/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.apihandler.common;


import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.io.IOUtils;
import org.onap.so.logger.MessageEnum;

import org.onap.so.logger.MsoLogger;
import org.xml.sax.SAXException;

public class XMLValidator {

    private static String XSDS_PATH;

    static {
        String prefixMsoPropertiesPath = System.getProperty ("mso.config.path");
        if (prefixMsoPropertiesPath == null) {
            prefixMsoPropertiesPath = "";
        }
        XSDS_PATH = prefixMsoPropertiesPath + "xsds/";
    }

    private String stringXsd;
    private String errorMsg = null;
    private SchemaFactory factory;
    private Schema schema;

    private static MsoLogger msoLogger = MsoLogger.getMsoLogger (MsoLogger.Catalog.APIH, XMLValidator.class);


    public XMLValidator (String xsdFile){

        try (FileInputStream xsdStream = new FileInputStream (XSDS_PATH + xsdFile)) {

            stringXsd = IOUtils.toString (xsdStream);

            factory = SchemaFactory.newInstance (XMLConstants.W3C_XML_SCHEMA_NS_URI);
            factory.setResourceResolver (new PathResourceResolver (XSDS_PATH));
            factory.setFeature (XMLConstants.FEATURE_SECURE_PROCESSING, true);

            String quotedXsd = stringXsd.replaceAll ("&#34;", "\"");
            Source src = new StreamSource (new java.io.StringReader (quotedXsd));
            schema = factory.newSchema (src);

        } catch (IOException | SAXException e) {

            msoLogger.debug ("Cannot open file " + XSDS_PATH + xsdFile, e);
            errorMsg = "ErrorDetails: xsd file " + xsdFile + "could not be opened - " + e.getMessage ();
        }
    }

    // Returns null when XML valid, otherwise returns error details.
    public String isXmlValid (String stringXml) {
        try {
            if (errorMsg != null && !errorMsg.isEmpty ()) {
                return errorMsg;
            }
            Source src2 = new StreamSource (new java.io.StringReader (stringXml));
            Validator validator = schema.newValidator ();
            validator.validate (src2);

        } catch (IOException | SAXException e) {
            msoLogger.debug ("Exception: ", e);
            return "ErrorDetails: " + e.getMessage ();

        } catch (Exception e) {
            msoLogger.error (MessageEnum.APIH_CANNOT_READ_SCHEMA, "", "", MsoLogger.ErrorCode.SchemaError, "APIH cannot read schema file", e);

            return "ErrorDetails: " + "Unable to read the schema file";
        }

        return null;
    }
}
