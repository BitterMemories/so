/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.asdc.installer.heat;


import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.hibernate.StaleObjectStateException;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.LockAcquisitionException;
import org.onap.sdc.api.notification.IArtifactInfo;
import org.onap.sdc.api.notification.IResourceInstance;
import org.onap.sdc.api.notification.IStatusData;
import org.onap.sdc.tosca.parser.api.IEntityDetails;
import org.onap.sdc.tosca.parser.api.ISdcCsarHelper;
import org.onap.sdc.tosca.parser.elements.queries.EntityQuery;
import org.onap.sdc.tosca.parser.elements.queries.TopologyTemplateQuery;
import org.onap.sdc.tosca.parser.enums.SdcTypes;
import org.onap.sdc.tosca.parser.impl.SdcPropertyNames;
import org.onap.sdc.toscaparser.api.CapabilityAssignment;
import org.onap.sdc.toscaparser.api.CapabilityAssignments;
import org.onap.sdc.toscaparser.api.Group;
import org.onap.sdc.toscaparser.api.NodeTemplate;
import org.onap.sdc.toscaparser.api.Policy;
import org.onap.sdc.toscaparser.api.Property;
import org.onap.sdc.toscaparser.api.RequirementAssignment;
import org.onap.sdc.toscaparser.api.RequirementAssignments;
import org.onap.sdc.toscaparser.api.elements.Metadata;
import org.onap.sdc.toscaparser.api.functions.GetInput;
import org.onap.sdc.toscaparser.api.parameters.Input;
import org.onap.sdc.utils.DistributionStatusEnum;
import org.onap.so.asdc.client.ASDCConfiguration;
import org.onap.so.asdc.client.exceptions.ArtifactInstallerException;
import org.onap.so.asdc.installer.ASDCElementInfo;
import org.onap.so.asdc.installer.BigDecimalVersion;
import org.onap.so.asdc.installer.IVfModuleData;
import org.onap.so.asdc.installer.PnfResourceStructure;
import org.onap.so.asdc.installer.ResourceStructure;
import org.onap.so.asdc.installer.ToscaResourceStructure;
import org.onap.so.asdc.installer.VfModuleArtifact;
import org.onap.so.asdc.installer.VfModuleStructure;
import org.onap.so.asdc.installer.VfResourceStructure;
import org.onap.so.asdc.installer.bpmn.WorkflowResource;
import org.onap.so.asdc.util.YamlEditor;
import org.onap.so.db.catalog.beans.AllottedResource;
import org.onap.so.db.catalog.beans.AllottedResourceCustomization;
import org.onap.so.db.catalog.beans.CollectionNetworkResourceCustomization;
import org.onap.so.db.catalog.beans.CollectionResource;
import org.onap.so.db.catalog.beans.CollectionResourceInstanceGroupCustomization;
import org.onap.so.db.catalog.beans.ConfigurationResource;
import org.onap.so.db.catalog.beans.ConfigurationResourceCustomization;
import org.onap.so.db.catalog.beans.CvnfcConfigurationCustomization;
import org.onap.so.db.catalog.beans.CvnfcCustomization;
import org.onap.so.db.catalog.beans.HeatEnvironment;
import org.onap.so.db.catalog.beans.HeatFiles;
import org.onap.so.db.catalog.beans.HeatTemplate;
import org.onap.so.db.catalog.beans.HeatTemplateParam;
import org.onap.so.db.catalog.beans.InstanceGroupType;
import org.onap.so.db.catalog.beans.NetworkCollectionResourceCustomization;
import org.onap.so.db.catalog.beans.NetworkInstanceGroup;
import org.onap.so.db.catalog.beans.NetworkResource;
import org.onap.so.db.catalog.beans.NetworkResourceCustomization;
import org.onap.so.db.catalog.beans.PnfResource;
import org.onap.so.db.catalog.beans.PnfResourceCustomization;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.ServiceProxyResourceCustomization;
import org.onap.so.db.catalog.beans.SubType;
import org.onap.so.db.catalog.beans.TempNetworkHeatTemplateLookup;
import org.onap.so.db.catalog.beans.ToscaCsar;
import org.onap.so.db.catalog.beans.VFCInstanceGroup;
import org.onap.so.db.catalog.beans.VfModule;
import org.onap.so.db.catalog.beans.VfModuleCustomization;
import org.onap.so.db.catalog.beans.VnfResource;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.onap.so.db.catalog.beans.VnfcCustomization;
import org.onap.so.db.catalog.beans.VnfcInstanceGroupCustomization;
import org.onap.so.db.catalog.data.repository.AllottedResourceCustomizationRepository;
import org.onap.so.db.catalog.data.repository.AllottedResourceRepository;
import org.onap.so.db.catalog.data.repository.CollectionResourceCustomizationRepository;
import org.onap.so.db.catalog.data.repository.CollectionResourceRepository;
import org.onap.so.db.catalog.data.repository.ConfigurationResourceCustomizationRepository;
import org.onap.so.db.catalog.data.repository.ConfigurationResourceRepository;
import org.onap.so.db.catalog.data.repository.CvnfcCustomizationRepository;
import org.onap.so.db.catalog.data.repository.ExternalServiceToInternalServiceRepository;
import org.onap.so.db.catalog.data.repository.HeatTemplateRepository;
import org.onap.so.db.catalog.data.repository.InstanceGroupRepository;
import org.onap.so.db.catalog.data.repository.NetworkResourceCustomizationRepository;
import org.onap.so.db.catalog.data.repository.NetworkResourceRepository;
import org.onap.so.db.catalog.data.repository.PnfCustomizationRepository;
import org.onap.so.db.catalog.data.repository.PnfResourceRepository;
import org.onap.so.db.catalog.data.repository.ServiceProxyResourceCustomizationRepository;
import org.onap.so.db.catalog.data.repository.ServiceRepository;
import org.onap.so.db.catalog.data.repository.TempNetworkHeatTemplateRepository;
import org.onap.so.db.catalog.data.repository.VFModuleCustomizationRepository;
import org.onap.so.db.catalog.data.repository.VFModuleRepository;
import org.onap.so.db.catalog.data.repository.VnfResourceRepository;
import org.onap.so.db.catalog.data.repository.VnfcCustomizationRepository;
import org.onap.so.db.catalog.data.repository.VnfcInstanceGroupCustomizationRepository;
import org.onap.so.db.request.beans.WatchdogComponentDistributionStatus;
import org.onap.so.db.request.beans.WatchdogDistributionStatus;
import org.onap.so.db.request.beans.WatchdogServiceModVerIdLookup;
import org.onap.so.db.request.data.repository.WatchdogComponentDistributionStatusRepository;
import org.onap.so.db.request.data.repository.WatchdogDistributionStatusRepository;
import org.onap.so.db.request.data.repository.WatchdogServiceModVerIdLookupRepository;
import org.onap.so.logger.ErrorCode;
import org.onap.so.logger.MessageEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ToscaResourceInstaller {

    protected static final String NODES_VRF_ENTRY = "org.openecomp.nodes.VRFEntry";

    protected static final String VLAN_NETWORK_RECEPTOR = "org.openecomp.nodes.VLANNetworkReceptor";

    protected static final String ALLOTTED_RESOURCE = "Allotted Resource";

    protected static final String MULTI_STAGE_DESIGN = "multi_stage_design";

    protected static final String SCALABLE = "scalable";

    protected static final String BASIC = "BASIC";

    protected static final String PROVIDER = "PROVIDER";

    protected static final String HEAT = "HEAT";

    protected static final String MANUAL_RECORD = "MANUAL_RECORD";

    protected static final String MSO = "SO";

    protected static final String SDNC_MODEL_NAME = "sdnc_model_name";

    protected static final String SDNC_MODEL_VERSION = "sdnc_model_version";

    private static String CUSTOMIZATION_UUID = "customizationUUID";

    protected static final String SKIP_POST_INST_CONF = "skip_post_instantiation_configuration";

    @Autowired
    protected ServiceRepository serviceRepo;

    @Autowired
    protected InstanceGroupRepository instanceGroupRepo;

    @Autowired
    protected ServiceProxyResourceCustomizationRepository serviceProxyCustomizationRepo;

    @Autowired
    protected CollectionResourceRepository collectionRepo;

    @Autowired
    protected CollectionResourceCustomizationRepository collectionCustomizationRepo;

    @Autowired
    protected ConfigurationResourceCustomizationRepository configCustomizationRepo;

    @Autowired
    protected ConfigurationResourceRepository configRepo;

    @Autowired
    protected VnfResourceRepository vnfRepo;

    @Autowired
    protected VFModuleRepository vfModuleRepo;

    @Autowired
    protected VFModuleCustomizationRepository vfModuleCustomizationRepo;

    @Autowired
    protected VnfcInstanceGroupCustomizationRepository vnfcInstanceGroupCustomizationRepo;

    @Autowired
    protected VnfcCustomizationRepository vnfcCustomizationRepo;

    @Autowired
    protected CvnfcCustomizationRepository cvnfcCustomizationRepo;

    @Autowired
    protected AllottedResourceRepository allottedRepo;

    @Autowired
    protected AllottedResourceCustomizationRepository allottedCustomizationRepo;

    @Autowired
    protected NetworkResourceRepository networkRepo;

    @Autowired
    protected HeatTemplateRepository heatRepo;

    @Autowired
    protected NetworkResourceCustomizationRepository networkCustomizationRepo;

    @Autowired
    protected WatchdogComponentDistributionStatusRepository watchdogCDStatusRepository;
    @Autowired
    protected WatchdogDistributionStatusRepository watchdogDistributionStatusRepository;
    @Autowired
    protected WatchdogServiceModVerIdLookupRepository watchdogModVerIdLookupRepository;

    @Autowired
    protected TempNetworkHeatTemplateRepository tempNetworkLookupRepo;

    @Autowired
    protected ExternalServiceToInternalServiceRepository externalServiceToInternalServiceRepository;

    @Autowired
    protected PnfResourceRepository pnfResourceRepository;

    @Autowired
    protected PnfCustomizationRepository pnfCustomizationRepository;

    @Autowired
    protected WorkflowResource workflowResource;

    protected static final Logger logger = LoggerFactory.getLogger(ToscaResourceInstaller.class);

    public boolean isResourceAlreadyDeployed(ResourceStructure vfResourceStruct, boolean serviceDeployed)
            throws ArtifactInstallerException {
        boolean status = false;
        ResourceStructure vfResourceStructure = vfResourceStruct;
        try {
            status = vfResourceStructure.isDeployedSuccessfully();
        } catch (RuntimeException e) {
            status = false;
        }
        try {
            Service existingService =
                    serviceRepo.findOneByModelUUID(vfResourceStructure.getNotification().getServiceUUID());
            if (existingService != null && !serviceDeployed)
                status = true;
            if (status) {
                logger.info(vfResourceStructure.getResourceInstance().getResourceInstanceName(),
                        vfResourceStructure.getResourceInstance().getResourceCustomizationUUID(),
                        vfResourceStructure.getNotification().getServiceName(),
                        BigDecimalVersion.castAndCheckNotificationVersionToString(
                                vfResourceStructure.getNotification().getServiceVersion()),
                        vfResourceStructure.getNotification().getServiceUUID(),
                        vfResourceStructure.getResourceInstance().getResourceName(), "", "");
                WatchdogComponentDistributionStatus wdStatus = new WatchdogComponentDistributionStatus(
                        vfResourceStruct.getNotification().getDistributionID(), MSO);
                wdStatus.setComponentDistributionStatus(DistributionStatusEnum.COMPONENT_DONE_OK.name());
                watchdogCDStatusRepository.saveAndFlush(wdStatus);
            } else {
                logger.info(vfResourceStructure.getResourceInstance().getResourceInstanceName(),
                        vfResourceStructure.getResourceInstance().getResourceCustomizationUUID(),
                        vfResourceStructure.getNotification().getServiceName(),
                        BigDecimalVersion.castAndCheckNotificationVersionToString(
                                vfResourceStructure.getNotification().getServiceVersion()),
                        vfResourceStructure.getNotification().getServiceUUID(),
                        vfResourceStructure.getResourceInstance().getResourceName(), "", "");
            }
            return status;
        } catch (Exception e) {
            logger.error("{} {} {}", MessageEnum.ASDC_ARTIFACT_CHECK_EXC.toString(), ErrorCode.SchemaError.getValue(),
                    "Exception - isResourceAlreadyDeployed");
            throw new ArtifactInstallerException("Exception caught during checking existence of the VNF Resource.", e);
        }
    }

    public void installTheComponentStatus(IStatusData iStatus) throws ArtifactInstallerException {
        logger.debug("Entering installTheComponentStatus for distributionId {} and ComponentName {}",
                iStatus.getDistributionID(), iStatus.getComponentName());

        try {
            WatchdogComponentDistributionStatus cdStatus =
                    new WatchdogComponentDistributionStatus(iStatus.getDistributionID(), iStatus.getComponentName());
            cdStatus.setComponentDistributionStatus(iStatus.getStatus().toString());
            watchdogCDStatusRepository.save(cdStatus);

        } catch (Exception e) {
            logger.debug("Exception caught in installTheComponentStatus {}", e.getMessage());
            throw new ArtifactInstallerException("Exception caught in installTheComponentStatus " + e.getMessage());
        }
    }


    @Transactional(rollbackFor = {ArtifactInstallerException.class})
    public void installTheResource(ToscaResourceStructure toscaResourceStruct, ResourceStructure resourceStruct)
            throws ArtifactInstallerException {
        if (resourceStruct instanceof VfResourceStructure) {
            installTheVfResource(toscaResourceStruct, (VfResourceStructure) resourceStruct);
        } else if (resourceStruct instanceof PnfResourceStructure) {
            installPnfResource(toscaResourceStruct, (PnfResourceStructure) resourceStruct);
        } else {
            logger.warn("Unrecognized resource type");
        }
    }

    private void installPnfResource(ToscaResourceStructure toscaResourceStruct, PnfResourceStructure resourceStruct)
            throws ArtifactInstallerException {

        // PCLO: in case of deployment failure, use a string that will represent
        // the type of artifact that failed...
        List<ASDCElementInfo> artifactListForLogging = new ArrayList<>();
        try {
            createToscaCsar(toscaResourceStruct);
            Service service = createService(toscaResourceStruct, resourceStruct);

            processResourceSequence(toscaResourceStruct, service);
            processPnfResources(toscaResourceStruct, service, resourceStruct);
            serviceRepo.save(service);

            WatchdogComponentDistributionStatus status =
                    new WatchdogComponentDistributionStatus(resourceStruct.getNotification().getDistributionID(), MSO);
            status.setComponentDistributionStatus(DistributionStatusEnum.COMPONENT_DONE_OK.name());
            watchdogCDStatusRepository.save(status);

            toscaResourceStruct.setSuccessfulDeployment();

        } catch (Exception e) {
            logger.debug("Exception :", e);
            WatchdogComponentDistributionStatus status =
                    new WatchdogComponentDistributionStatus(resourceStruct.getNotification().getDistributionID(), MSO);
            status.setComponentDistributionStatus(DistributionStatusEnum.COMPONENT_DONE_ERROR.name());
            watchdogCDStatusRepository.save(status);
            Throwable dbExceptionToCapture = e;
            while (!(dbExceptionToCapture instanceof ConstraintViolationException
                    || dbExceptionToCapture instanceof LockAcquisitionException)
                    && (dbExceptionToCapture.getCause() != null)) {
                dbExceptionToCapture = dbExceptionToCapture.getCause();
            }

            if (dbExceptionToCapture instanceof ConstraintViolationException
                    || dbExceptionToCapture instanceof LockAcquisitionException) {
                logger.warn("{} {} {} {} {}", MessageEnum.ASDC_ARTIFACT_ALREADY_DEPLOYED.toString(),
                        resourceStruct.getResourceInstance().getResourceName(),
                        resourceStruct.getNotification().getServiceVersion(), ErrorCode.DataError.getValue(),
                        "Exception - ASCDC Artifact already deployed", e);
            } else {
                String elementToLog = (!artifactListForLogging.isEmpty()
                        ? artifactListForLogging.get(artifactListForLogging.size() - 1).toString()
                        : "No element listed");
                logger.error("{} {} {} {}", MessageEnum.ASDC_ARTIFACT_INSTALL_EXC.toString(), elementToLog,
                        ErrorCode.DataError.getValue(), "Exception caught during installation of "
                                + resourceStruct.getResourceInstance().getResourceName() + ". Transaction rollback",
                        e);
                throw new ArtifactInstallerException(
                        "Exception caught during installation of "
                                + resourceStruct.getResourceInstance().getResourceName() + ". Transaction rollback.",
                        e);
            }
        }
    }

    @Transactional(rollbackFor = {ArtifactInstallerException.class})
    public void installTheVfResource(ToscaResourceStructure toscaResourceStruct, VfResourceStructure vfResourceStruct)
            throws ArtifactInstallerException {
        VfResourceStructure vfResourceStructure = vfResourceStruct;
        extractHeatInformation(toscaResourceStruct, vfResourceStructure);

        // PCLO: in case of deployment failure, use a string that will represent
        // the type of artifact that failed...
        List<ASDCElementInfo> artifactListForLogging = new ArrayList<>();
        try {
            createToscaCsar(toscaResourceStruct);
            createService(toscaResourceStruct, vfResourceStruct);
            Service service = toscaResourceStruct.getCatalogService();
            List<NodeTemplate> vfNodeTemplatesList = toscaResourceStruct.getSdcCsarHelper().getServiceVfList();

            for (NodeTemplate nodeTemplate : vfNodeTemplatesList) {
                Metadata metadata = nodeTemplate.getMetaData();
                String serviceType = toscaResourceStruct.getCatalogService().getServiceType();
                String vfCustomizationCategory = toscaResourceStruct.getSdcCsarHelper()
                        .getMetadataPropertyValue(metadata, SdcPropertyNames.PROPERTY_NAME_CATEGORY);
                processVfModules(toscaResourceStruct, vfResourceStructure, service, nodeTemplate, metadata,
                        vfCustomizationCategory);
            }

            workflowResource.processWorkflows(vfResourceStructure);
            processResourceSequence(toscaResourceStruct, service);
            List<NodeTemplate> allottedResourceList = toscaResourceStruct.getSdcCsarHelper().getAllottedResources();
            processAllottedResources(toscaResourceStruct, service, allottedResourceList);
            processNetworks(toscaResourceStruct, service);
            // process Network Collections
            processNetworkCollections(toscaResourceStruct, service);
            // Process Service Proxy & Configuration
            processServiceProxyAndConfiguration(toscaResourceStruct, service);

            logger.info("Saving Service: {} ", service.getModelName());
            service = serviceRepo.save(service);
            correlateConfigCustomResources(service);

            WatchdogComponentDistributionStatus status = new WatchdogComponentDistributionStatus(
                    vfResourceStruct.getNotification().getDistributionID(), MSO);
            status.setComponentDistributionStatus(DistributionStatusEnum.COMPONENT_DONE_OK.name());
            watchdogCDStatusRepository.save(status);

            toscaResourceStruct.setSuccessfulDeployment();

        } catch (Exception e) {
            logger.debug("Exception :", e);
            WatchdogComponentDistributionStatus status = new WatchdogComponentDistributionStatus(
                    vfResourceStruct.getNotification().getDistributionID(), MSO);
            status.setComponentDistributionStatus(DistributionStatusEnum.COMPONENT_DONE_ERROR.name());
            watchdogCDStatusRepository.save(status);
            Throwable dbExceptionToCapture = e;
            while (!(dbExceptionToCapture instanceof ConstraintViolationException
                    || dbExceptionToCapture instanceof LockAcquisitionException)
                    && (dbExceptionToCapture.getCause() != null)) {
                dbExceptionToCapture = dbExceptionToCapture.getCause();
            }

            if (dbExceptionToCapture instanceof ConstraintViolationException
                    || dbExceptionToCapture instanceof LockAcquisitionException) {
                logger.warn("{} {} {} {} {}", MessageEnum.ASDC_ARTIFACT_ALREADY_DEPLOYED.toString(),
                        vfResourceStructure.getResourceInstance().getResourceName(),
                        vfResourceStructure.getNotification().getServiceVersion(), ErrorCode.DataError.getValue(),
                        "Exception - ASCDC Artifact already deployed", e);
            } else {
                String elementToLog = (!artifactListForLogging.isEmpty()
                        ? artifactListForLogging.get(artifactListForLogging.size() - 1).toString()
                        : "No element listed");
                logger.error("{} {} {} {}", MessageEnum.ASDC_ARTIFACT_INSTALL_EXC.toString(), elementToLog,
                        ErrorCode.DataError.getValue(),
                        "Exception caught during installation of "
                                + vfResourceStructure.getResourceInstance().getResourceName()
                                + ". Transaction rollback",
                        e);
                throw new ArtifactInstallerException("Exception caught during installation of "
                        + vfResourceStructure.getResourceInstance().getResourceName() + ". Transaction rollback.", e);
            }
        }
    }


    List<NodeTemplate> getRequirementList(List<NodeTemplate> resultList, List<NodeTemplate> nodeTemplates,
            ISdcCsarHelper iSdcCsarHelper) {

        List<NodeTemplate> nodes = new ArrayList<NodeTemplate>();
        nodes.addAll(nodeTemplates);

        for (NodeTemplate nodeTemplate : nodeTemplates) {
            RequirementAssignments requirement = iSdcCsarHelper.getRequirementsOf(nodeTemplate);
            List<RequirementAssignment> reqAs = requirement.getAll();
            for (RequirementAssignment ra : reqAs) {
                String reqNode = ra.getNodeTemplateName();
                for (NodeTemplate rNode : resultList) {
                    if (rNode.getName().equals(reqNode)) {
                        if (!resultList.contains(nodeTemplate)) {
                            resultList.add(nodeTemplate);
                        }
                        if (nodes.contains(nodeTemplate)) {
                            nodes.remove(nodeTemplate);
                        }
                        break;
                    }
                }
            }
        }

        if (!nodes.isEmpty()) {
            getRequirementList(resultList, nodes, iSdcCsarHelper);
        }

        return resultList;
    }

    // This method retrieve resource sequence from csar file
    void processResourceSequence(ToscaResourceStructure toscaResourceStructure, Service service) {
        List<String> resouceSequence = new ArrayList<String>();
        List<NodeTemplate> resultList = new ArrayList<NodeTemplate>();

        ISdcCsarHelper iSdcCsarHelper = toscaResourceStructure.getSdcCsarHelper();
        List<NodeTemplate> nodeTemplates = iSdcCsarHelper.getServiceNodeTemplates();
        List<NodeTemplate> nodes = new ArrayList<NodeTemplate>();
        nodes.addAll(nodeTemplates);

        for (NodeTemplate nodeTemplate : nodeTemplates) {
            RequirementAssignments requirement = iSdcCsarHelper.getRequirementsOf(nodeTemplate);

            if (requirement == null || requirement.getAll() == null || requirement.getAll().isEmpty()) {
                resultList.add(nodeTemplate);
                nodes.remove(nodeTemplate);
            }
        }

        resultList = getRequirementList(resultList, nodes, iSdcCsarHelper);

        for (NodeTemplate node : resultList) {
            String templateName = node.getMetaData().getValue("name");
            if (!resouceSequence.contains(templateName)) {
                resouceSequence.add(templateName);
            }
        }

        String resourceSeqStr = resouceSequence.stream().collect(Collectors.joining(","));
        service.setResourceOrder(resourceSeqStr);
        logger.debug(" resourceSeq for service uuid(" + service.getModelUUID() + ") : " + resourceSeqStr);
    }

    private static String getValue(Object value, List<Input> servInputs) {
        String output = null;
        if (value instanceof Map) {
            // currently this logic handles only one level of nesting.
            return ((LinkedHashMap) value).values().toArray()[0].toString();
        } else if (value instanceof GetInput) {
            String inputName = ((GetInput) value).getInputName();

            for (Input input : servInputs) {
                if (input.getName().equals(inputName)) {
                    // keep both input name and default value
                    // if service input does not supplies value the use default value
                    String defaultValue = input.getDefault() != null ? (String) input.getDefault().toString() : "";
                    output = inputName + "|" + defaultValue;// return default value
                }
            }

        } else {
            output = value != null ? value.toString() : "";
        }
        return output; // return property value
    }

    String getResourceInput(ToscaResourceStructure toscaResourceStructure, String resourceCustomizationUuid)
            throws ArtifactInstallerException {
        Map<String, String> resouceRequest = new HashMap<>();
        ISdcCsarHelper iSdcCsarHelper = toscaResourceStructure.getSdcCsarHelper();

        List<Input> serInput = iSdcCsarHelper.getServiceInputs();
        Optional<NodeTemplate> nodeTemplateOpt = iSdcCsarHelper.getServiceNodeTemplates().stream()
                .filter(e -> e.getMetaData().getValue(CUSTOMIZATION_UUID).equals(resourceCustomizationUuid))
                .findFirst();
        if (nodeTemplateOpt.isPresent()) {
            NodeTemplate nodeTemplate = nodeTemplateOpt.get();
            LinkedHashMap<String, Property> resourceProperties = nodeTemplate.getProperties();

            for (String key : resourceProperties.keySet()) {
                Property property = resourceProperties.get(key);

                String value = getValue(property.getValue(), serInput);
                resouceRequest.put(key, value);
            }
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonStr = objectMapper.writeValueAsString(resouceRequest);

            jsonStr = jsonStr.replace("\"", "\\\"");
            logger.debug(
                    "resource request for resource customization id (" + resourceCustomizationUuid + ") : " + jsonStr);
            return jsonStr;
        } catch (JsonProcessingException e) {
            logger.error("resource input could not be deserialized for resource customization id ("
                    + resourceCustomizationUuid + ")");
            throw new ArtifactInstallerException("resource input could not be parsed", e);
        }
    }

    protected void processNetworks(ToscaResourceStructure toscaResourceStruct, Service service)
            throws ArtifactInstallerException {
        List<NodeTemplate> nodeTemplatesVLList = toscaResourceStruct.getSdcCsarHelper().getServiceVlList();

        if (nodeTemplatesVLList != null) {
            for (NodeTemplate vlNode : nodeTemplatesVLList) {
                String networkResourceModelName = vlNode.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_NAME);

                TempNetworkHeatTemplateLookup tempNetworkLookUp =
                        tempNetworkLookupRepo.findFirstBynetworkResourceModelName(networkResourceModelName);

                if (tempNetworkLookUp != null) {
                    HeatTemplate heatTemplate =
                            heatRepo.findByArtifactUuid(tempNetworkLookUp.getHeatTemplateArtifactUuid());
                    if (heatTemplate != null) {
                        NetworkResourceCustomization networkCustomization = createNetwork(vlNode, toscaResourceStruct,
                                heatTemplate, tempNetworkLookUp.getAicVersionMax(),
                                tempNetworkLookUp.getAicVersionMin(), service);
                        service.getNetworkCustomizations().add(networkCustomization);
                    } else {
                        throw new ArtifactInstallerException("No HeatTemplate found for artifactUUID: "
                                + tempNetworkLookUp.getHeatTemplateArtifactUuid());
                    }
                } else {
                    NetworkResourceCustomization networkCustomization =
                            createNetwork(vlNode, toscaResourceStruct, null, null, null, service);
                    service.getNetworkCustomizations().add(networkCustomization);
                    logger.debug("No NetworkResourceName found in TempNetworkHeatTemplateLookup for "
                            + networkResourceModelName);
                }

            }
        }
    }

    protected void processAllottedResources(ToscaResourceStructure toscaResourceStruct, Service service,
            List<NodeTemplate> allottedResourceList) {
        if (allottedResourceList != null) {
            for (NodeTemplate allottedNode : allottedResourceList) {
                service.getAllottedCustomizations()
                        .add(createAllottedResource(allottedNode, toscaResourceStruct, service));
            }
        }
    }


    protected ConfigurationResource getConfigurationResource(NodeTemplate nodeTemplate) {
        Metadata metadata = nodeTemplate.getMetaData();
        ConfigurationResource configResource = new ConfigurationResource();
        configResource.setModelName(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_NAME));
        configResource.setModelInvariantUUID(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID));
        configResource.setModelUUID(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
        configResource.setModelVersion(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_VERSION));
        configResource.setDescription(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION));
        configResource.setToscaNodeType(nodeTemplate.getType());
        return configResource;
    }

    protected ConfigurationResourceCustomization getConfigurationResourceCustomization(NodeTemplate nodeTemplate,
            ToscaResourceStructure toscaResourceStructure, ServiceProxyResourceCustomization spResourceCustomization,
            Service service) {
        Metadata metadata = nodeTemplate.getMetaData();

        ConfigurationResource configResource = getConfigurationResource(nodeTemplate);

        ConfigurationResourceCustomization configCustomizationResource = new ConfigurationResourceCustomization();

        Set<ConfigurationResourceCustomization> configResourceCustomizationSet = new HashSet<>();

        configCustomizationResource
                .setModelCustomizationUUID(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID));
        configCustomizationResource.setModelInstanceName(nodeTemplate.getName());

        configCustomizationResource.setNfFunction(toscaResourceStructure.getSdcCsarHelper()
                .getNodeTemplatePropertyLeafValue(nodeTemplate, SdcPropertyNames.PROPERTY_NAME_NFFUNCTION));
        configCustomizationResource.setNfRole(toscaResourceStructure.getSdcCsarHelper()
                .getNodeTemplatePropertyLeafValue(nodeTemplate, SdcPropertyNames.PROPERTY_NAME_NFROLE));
        configCustomizationResource.setNfType(toscaResourceStructure.getSdcCsarHelper()
                .getNodeTemplatePropertyLeafValue(nodeTemplate, SdcPropertyNames.PROPERTY_NAME_NFTYPE));
        configCustomizationResource
                .setServiceProxyResourceCustomizationUUID(spResourceCustomization.getModelCustomizationUUID());

        configCustomizationResource.setConfigurationResource(configResource);
        configCustomizationResource.setService(service);
        configResourceCustomizationSet.add(configCustomizationResource);

        configResource.setConfigurationResourceCustomization(configResourceCustomizationSet);

        return configCustomizationResource;
    }


    protected void processServiceProxyAndConfiguration(ToscaResourceStructure toscaResourceStruct, Service service) {

        List<NodeTemplate> serviceProxyResourceList =
                toscaResourceStruct.getSdcCsarHelper().getServiceNodeTemplateBySdcType(SdcTypes.SERVICE_PROXY);

        List<NodeTemplate> configurationNodeTemplatesList =
                toscaResourceStruct.getSdcCsarHelper().getServiceNodeTemplateBySdcType(SdcTypes.CONFIGURATION);

        List<ServiceProxyResourceCustomization> serviceProxyList = new ArrayList<ServiceProxyResourceCustomization>();
        List<ConfigurationResourceCustomization> configurationResourceList =
                new ArrayList<ConfigurationResourceCustomization>();

        ServiceProxyResourceCustomization serviceProxy = null;

        if (serviceProxyResourceList != null) {
            for (NodeTemplate spNode : serviceProxyResourceList) {
                serviceProxy = createServiceProxy(spNode, service, toscaResourceStruct);
                serviceProxyList.add(serviceProxy);

                for (NodeTemplate configNode : configurationNodeTemplatesList) {

                    List<RequirementAssignment> requirementsList =
                            toscaResourceStruct.getSdcCsarHelper().getRequirementsOf(configNode).getAll();
                    for (RequirementAssignment requirement : requirementsList) {
                        if (requirement.getNodeTemplateName().equals(spNode.getName())) {
                            ConfigurationResourceCustomization configurationResource =
                                    createConfiguration(configNode, toscaResourceStruct, serviceProxy, service);

                            Optional<ConfigurationResourceCustomization> matchingObject =
                                    configurationResourceList.stream()
                                            .filter(configurationResourceCustomization -> configNode.getMetaData()
                                                    .getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID)
                                                    .equals(configurationResource.getModelCustomizationUUID()))
                                            .filter(configurationResourceCustomization -> configurationResourceCustomization
                                                    .getModelInstanceName()
                                                    .equals(configurationResource.getModelInstanceName()))
                                            .findFirst();
                            if (!matchingObject.isPresent()) {
                                configurationResourceList.add(configurationResource);
                            }
                            break;
                        }
                    }
                }

            }
        }

        service.setConfigurationCustomizations(configurationResourceList);
        service.setServiceProxyCustomizations(serviceProxyList);
    }

    /*
     * ConfigurationResourceCustomization objects have their IDs auto incremented in the database. Unless we know their
     * IDs we cannot possibly associate their related records. So these ConfigResourceCustomizations are persisted first
     * and subsequently correlated.
     */

    protected void correlateConfigCustomResources(Service service) {
        /* Assuming that we have only one pair of VRF-VNR */
        ConfigurationResourceCustomization vrfConfigCustomResource = null;
        ConfigurationResourceCustomization vnrConfigCustomResource = null;
        List<ConfigurationResourceCustomization> configCustomList = service.getConfigurationCustomizations();
        for (ConfigurationResourceCustomization configResource : configCustomList) {
            String nodeType = configResource.getConfigurationResource().getToscaNodeType();
            if (NODES_VRF_ENTRY.equalsIgnoreCase(nodeType)) {
                vrfConfigCustomResource = configResource;
            } else if (VLAN_NETWORK_RECEPTOR.equalsIgnoreCase(nodeType)) {
                vnrConfigCustomResource = configResource;
            }
        }

        if (vrfConfigCustomResource != null) {
            vrfConfigCustomResource.setConfigResourceCustomization(vnrConfigCustomResource);
            configCustomizationRepo.save(vrfConfigCustomResource);

        }
        if (vnrConfigCustomResource != null) {
            vnrConfigCustomResource.setConfigResourceCustomization(vrfConfigCustomResource);
            configCustomizationRepo.save(vnrConfigCustomResource);
        }
    }

    protected void processNetworkCollections(ToscaResourceStructure toscaResourceStruct, Service service) {

        List<NodeTemplate> networkCollectionList =
                toscaResourceStruct.getSdcCsarHelper().getServiceNodeTemplateBySdcType(SdcTypes.CR);

        if (networkCollectionList != null) {
            for (NodeTemplate crNode : networkCollectionList) {

                createNetworkCollection(crNode, toscaResourceStruct, service);
                collectionRepo.saveAndFlush(toscaResourceStruct.getCatalogCollectionResource());

                List<NetworkInstanceGroup> networkInstanceGroupList =
                        toscaResourceStruct.getCatalogNetworkInstanceGroup();
                for (NetworkInstanceGroup networkInstanceGroup : networkInstanceGroupList) {
                    instanceGroupRepo.saveAndFlush(networkInstanceGroup);
                }

            }
        }
        service.getCollectionResourceCustomizations()
                .add(toscaResourceStruct.getCatalogCollectionResourceCustomization());
    }



    /**
     * This is used to process the PNF specific resource, including resource and resource_customization.
     * {@link IEntityDetails} based API is used to retrieve information. Please check {@link ISdcCsarHelper} for
     * details.
     */
    protected void processPnfResources(ToscaResourceStructure toscaResourceStruct, Service service,
            PnfResourceStructure resourceStructure) throws Exception {
        logger.info("Processing PNF resource: {}", resourceStructure.getResourceInstance().getResourceUUID());

        ISdcCsarHelper sdcCsarHelper = toscaResourceStruct.getSdcCsarHelper();
        EntityQuery entityQuery = EntityQuery.newBuilder(SdcTypes.PNF).build();
        TopologyTemplateQuery topologyTemplateQuery = TopologyTemplateQuery.newBuilder(SdcTypes.SERVICE).build();

        List<IEntityDetails> entityDetailsList = sdcCsarHelper.getEntity(entityQuery, topologyTemplateQuery, false);
        for (IEntityDetails entityDetails : entityDetailsList) {
            Metadata metadata = entityDetails.getMetadata();
            String customizationUUID = metadata.getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID);
            String modelUuid = metadata.getValue(SdcPropertyNames.PROPERTY_NAME_UUID);
            String notifCustomizationUUID = resourceStructure.getResourceInstance().getResourceCustomizationUUID();
            if (customizationUUID != null && customizationUUID.equals(notifCustomizationUUID)) {
                logger.info("Resource customization UUID: {} is the same as notified resource customizationUUID: {}",
                        customizationUUID, notifCustomizationUUID);

                if (checkExistingPnfResourceCutomization(customizationUUID)) {
                    logger.info("Resource customization UUID: {} already deployed", customizationUUID);
                } else {
                    PnfResource pnfResource = findExistingPnfResource(service, modelUuid);
                    if (pnfResource == null) {
                        pnfResource = createPnfResource(entityDetails);
                    }
                    PnfResourceCustomization pnfResourceCustomization =
                            createPnfResourceCustomization(entityDetails, pnfResource);
                    pnfResource.getPnfResourceCustomizations().add(pnfResourceCustomization);
                    toscaResourceStruct.setPnfResourceCustomization(pnfResourceCustomization);
                    service.getPnfCustomizations().add(pnfResourceCustomization);
                }
            } else {
                logger.warn(
                        "Resource customization UUID: {} is NOT the same as notified resource customizationUUID: {}",
                        customizationUUID, notifCustomizationUUID);
            }
        }
    }

    private PnfResource findExistingPnfResource(Service service, String modelUuid) {
        PnfResource pnfResource = null;
        for (PnfResourceCustomization pnfResourceCustomization : service.getPnfCustomizations()) {
            if (pnfResourceCustomization.getPnfResources() != null
                    && pnfResourceCustomization.getPnfResources().getModelUUID().equals(modelUuid)) {
                pnfResource = pnfResourceCustomization.getPnfResources();
            }
        }
        if (pnfResource == null) {
            pnfResource = pnfResourceRepository.findById(modelUuid).orElse(pnfResource);
        }
        return pnfResource;
    }

    private boolean checkExistingPnfResourceCutomization(String customizationUUID) {
        return pnfCustomizationRepository.findById(customizationUUID).isPresent();
    }

    /**
     * Construct the {@link PnfResource} from {@link IEntityDetails} object.
     */
    private PnfResource createPnfResource(IEntityDetails entity) {
        PnfResource pnfResource = new PnfResource();
        Metadata metadata = entity.getMetadata();
        pnfResource.setModelInvariantUUID(testNull(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID)));
        pnfResource.setModelName(testNull(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_NAME)));
        pnfResource.setModelUUID(testNull(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_UUID)));
        pnfResource.setModelVersion(testNull(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_VERSION)));
        pnfResource.setDescription(testNull(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION)));
        pnfResource.setCategory(testNull(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_CATEGORY)));
        pnfResource.setSubCategory(testNull(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_SUBCATEGORY)));
        pnfResource.setToscaNodeType(entity.getToscaType());
        return pnfResource;
    }

    /**
     * Construct the {@link PnfResourceCustomization} from {@link IEntityDetails} object.
     */
    private PnfResourceCustomization createPnfResourceCustomization(IEntityDetails entityDetails,
            PnfResource pnfResource) {

        PnfResourceCustomization pnfResourceCustomization = new PnfResourceCustomization();
        Metadata metadata = entityDetails.getMetadata();
        Map<String, Property> properties = entityDetails.getProperties();
        pnfResourceCustomization.setModelCustomizationUUID(
                testNull(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID)));
        pnfResourceCustomization.setModelInstanceName(entityDetails.getName());
        pnfResourceCustomization
                .setNfFunction(getStringValue(properties.get(SdcPropertyNames.PROPERTY_NAME_NFFUNCTION)));
        pnfResourceCustomization.setNfNamingCode(getStringValue(properties.get(SdcPropertyNames.PROPERTY_NAME_NFCODE)));
        pnfResourceCustomization.setNfRole(getStringValue(properties.get(SdcPropertyNames.PROPERTY_NAME_NFROLE)));
        pnfResourceCustomization.setNfType(getStringValue(properties.get(SdcPropertyNames.PROPERTY_NAME_NFTYPE)));
        pnfResourceCustomization.setMultiStageDesign(getStringValue(properties.get(MULTI_STAGE_DESIGN)));
        pnfResourceCustomization.setBlueprintName(getStringValue(properties.get(SDNC_MODEL_NAME)));
        pnfResourceCustomization.setBlueprintVersion(getStringValue(properties.get(SDNC_MODEL_VERSION)));
        pnfResourceCustomization.setSkipPostInstConf(getBooleanValue(properties.get(SKIP_POST_INST_CONF)));
        pnfResourceCustomization.setPnfResources(pnfResource);

        return pnfResourceCustomization;
    }

    /**
     * Get value from {@link Property} and cast to boolean value. Return true if property is null.
     */
    private boolean getBooleanValue(Property property) {
        if (null == property) {
            return true;
        }
        Object value = property.getValue();
        return new Boolean(String.valueOf(value));
    }

    /**
     * Get value from {@link Property} and cast to String value. Return empty String if property is null value.
     */
    private String getStringValue(Property property) {
        if (null == property) {
            return "";
        }
        Object value = property.getValue();
        return String.valueOf(value);
    }

    protected void processVfModules(ToscaResourceStructure toscaResourceStruct, VfResourceStructure vfResourceStructure,
            Service service, NodeTemplate nodeTemplate, Metadata metadata, String vfCustomizationCategory)
            throws Exception {

        logger.debug("VF Category is : " + vfCustomizationCategory);

        if (vfResourceStructure.getVfModuleStructure() != null
                && !vfResourceStructure.getVfModuleStructure().isEmpty()) {

            String vfCustomizationUUID = toscaResourceStruct.getSdcCsarHelper().getMetadataPropertyValue(metadata,
                    SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID);
            logger.debug("VFCustomizationUUID=" + vfCustomizationUUID);

            IResourceInstance vfNotificationResource = vfResourceStructure.getResourceInstance();

            // Make sure the VF ResourceCustomizationUUID from the notification and tosca customizations match before
            // comparing their VF Modules UUID's
            logger.debug("Checking if Notification VF ResourceCustomizationUUID: "
                    + vfNotificationResource.getResourceCustomizationUUID() + " matches Tosca VF Customization UUID: "
                    + vfCustomizationUUID);

            if (vfCustomizationUUID.equals(vfNotificationResource.getResourceCustomizationUUID())) {

                logger.debug("vfCustomizationUUID: " + vfCustomizationUUID
                        + " matches vfNotificationResource CustomizationUUID");

                VnfResourceCustomization vnfResource = createVnfResource(nodeTemplate, toscaResourceStruct, service);

                Set<CvnfcCustomization> existingCvnfcSet = new HashSet<CvnfcCustomization>();
                Set<VnfcCustomization> existingVnfcSet = new HashSet<VnfcCustomization>();

                for (VfModuleStructure vfModuleStructure : vfResourceStructure.getVfModuleStructure()) {

                    logger.debug("vfModuleStructure:" + vfModuleStructure.toString());
                    List<org.onap.sdc.toscaparser.api.Group> vfGroups =
                            toscaResourceStruct.getSdcCsarHelper().getVfModulesByVf(vfCustomizationUUID);
                    IVfModuleData vfMetadata = vfModuleStructure.getVfModuleMetadata();

                    logger.debug("Comparing Vf_Modules_Metadata CustomizationUUID : "
                            + vfMetadata.getVfModuleModelCustomizationUUID());

                    Optional<org.onap.sdc.toscaparser.api.Group> matchingObject = vfGroups.stream()
                            .peek(group -> logger.debug("To Csar Group VFModuleModelCustomizationUUID "
                                    + group.getMetadata().getValue("vfModuleModelCustomizationUUID")))
                            .filter(group -> group.getMetadata().getValue("vfModuleModelCustomizationUUID")
                                    .equals(vfMetadata.getVfModuleModelCustomizationUUID()))
                            .findFirst();
                    if (matchingObject.isPresent()) {
                        VfModuleCustomization vfModuleCustomization = createVFModuleResource(matchingObject.get(),
                                nodeTemplate, toscaResourceStruct, vfResourceStructure, vfMetadata, vnfResource,
                                service, existingCvnfcSet, existingVnfcSet);
                        vfModuleCustomization.getVfModule().setVnfResources(vnfResource.getVnfResources());
                    } else
                        throw new Exception(
                                "Cannot find matching VFModule Customization in Csar for Vf_Modules_Metadata: "
                                        + vfMetadata.getVfModuleModelCustomizationUUID());

                }


                // Check for VNFC Instance Group info and add it if there is
                List<Group> groupList =
                        toscaResourceStruct.getSdcCsarHelper().getGroupsOfOriginOfNodeTemplateByToscaGroupType(
                                nodeTemplate, "org.openecomp.groups.VfcInstanceGroup");

                for (Group group : groupList) {
                    VnfcInstanceGroupCustomization vnfcInstanceGroupCustomization =
                            createVNFCInstanceGroup(nodeTemplate, group, vnfResource, toscaResourceStruct);
                    vnfcInstanceGroupCustomizationRepo.saveAndFlush(vnfcInstanceGroupCustomization);
                }


                // add this vnfResource with existing vnfResource for this service
                addVnfCustomization(service, vnfResource);
            } else {
                logger.debug("Notification VF ResourceCustomizationUUID: "
                        + vfNotificationResource.getResourceCustomizationUUID() + " doesn't match "
                        + "Tosca VF Customization UUID: " + vfCustomizationUUID);
            }
        }
    }

    public void processWatchdog(String distributionId, String servideUUID, Optional<String> distributionNotification,
            String consumerId) {
        WatchdogServiceModVerIdLookup modVerIdLookup =
                new WatchdogServiceModVerIdLookup(distributionId, servideUUID, distributionNotification, consumerId);
        watchdogModVerIdLookupRepository.saveAndFlush(modVerIdLookup);

        try {

            WatchdogDistributionStatus distributionStatus = new WatchdogDistributionStatus(distributionId);
            watchdogDistributionStatusRepository.saveAndFlush(distributionStatus);

        } catch (ObjectOptimisticLockingFailureException e) {
            logger.debug("ObjectOptimisticLockingFailureException in processWatchdog : " + e.toString());
            throw e;
        }
    }

    protected void extractHeatInformation(ToscaResourceStructure toscaResourceStruct,
            VfResourceStructure vfResourceStructure) {
        for (VfModuleArtifact vfModuleArtifact : vfResourceStructure.getArtifactsMapByUUID().values()) {

            switch (vfModuleArtifact.getArtifactInfo().getArtifactType()) {
                case ASDCConfiguration.HEAT:
                case ASDCConfiguration.HEAT_NESTED:
                    createHeatTemplateFromArtifact(vfResourceStructure, toscaResourceStruct, vfModuleArtifact);
                    break;
                case ASDCConfiguration.HEAT_VOL:
                    createHeatTemplateFromArtifact(vfResourceStructure, toscaResourceStruct, vfModuleArtifact);
                    VfModuleArtifact envModuleArtifact =
                            getHeatEnvArtifactFromGeneratedArtifact(vfResourceStructure, vfModuleArtifact);
                    createHeatEnvFromArtifact(vfResourceStructure, envModuleArtifact);
                    break;
                case ASDCConfiguration.HEAT_ENV:
                    createHeatEnvFromArtifact(vfResourceStructure, vfModuleArtifact);
                    break;
                case ASDCConfiguration.HEAT_ARTIFACT:
                    createHeatFileFromArtifact(vfResourceStructure, vfModuleArtifact, toscaResourceStruct);
                    break;
                case ASDCConfiguration.HEAT_NET:
                case ASDCConfiguration.OTHER:
                    logger.warn("{} {} {} {}", MessageEnum.ASDC_ARTIFACT_TYPE_NOT_SUPPORT.toString(),
                            vfModuleArtifact.getArtifactInfo().getArtifactType() + "(Artifact Name:"
                                    + vfModuleArtifact.getArtifactInfo().getArtifactName() + ")",
                            ErrorCode.DataError.getValue(), "Artifact type not supported");
                    break;
                default:
                    break;

            }
        }
    }

    protected VfModuleArtifact getHeatEnvArtifactFromGeneratedArtifact(VfResourceStructure vfResourceStructure,
            VfModuleArtifact vfModuleArtifact) {
        String artifactName = vfModuleArtifact.getArtifactInfo().getArtifactName();
        artifactName = artifactName.substring(0, artifactName.indexOf('.'));
        for (VfModuleArtifact moduleArtifact : vfResourceStructure.getArtifactsMapByUUID().values()) {
            if (moduleArtifact.getArtifactInfo().getArtifactName().contains(artifactName)
                    && moduleArtifact.getArtifactInfo().getArtifactType().equals(ASDCConfiguration.HEAT_ENV)) {
                return moduleArtifact;
            }
        }
        return null;
    }

    public String verifyTheFilePrefixInArtifacts(String filebody, VfResourceStructure vfResourceStructure,
            List<String> listTypes) {
        String newFileBody = filebody;
        for (VfModuleArtifact moduleArtifact : vfResourceStructure.getArtifactsMapByUUID().values()) {

            if (listTypes.contains(moduleArtifact.getArtifactInfo().getArtifactType())) {

                newFileBody =
                        verifyTheFilePrefixInString(newFileBody, moduleArtifact.getArtifactInfo().getArtifactName());
            }
        }
        return newFileBody;
    }

    public String verifyTheFilePrefixInString(final String body, final String filenameToVerify) {

        String needlePrefix = "file:///";
        String prefixedFilenameToVerify = needlePrefix + filenameToVerify;

        if ((body == null) || (body.length() == 0) || (filenameToVerify == null) || (filenameToVerify.length() == 0)) {
            return body;
        }

        StringBuilder sb = new StringBuilder(body.length());

        int currentIndex = 0;
        int startIndex = 0;

        while (currentIndex != -1) {
            startIndex = currentIndex;
            currentIndex = body.indexOf(prefixedFilenameToVerify, startIndex);

            if (currentIndex == -1) {
                break;
            }
            // We append from the startIndex up to currentIndex (start of File
            // Name)
            sb.append(body.substring(startIndex, currentIndex));
            sb.append(filenameToVerify);

            currentIndex += prefixedFilenameToVerify.length();
        }

        sb.append(body.substring(startIndex));

        return sb.toString();
    }

    protected void createHeatTemplateFromArtifact(VfResourceStructure vfResourceStructure,
            ToscaResourceStructure toscaResourceStruct, VfModuleArtifact vfModuleArtifact) {
        HeatTemplate heatTemplate = new HeatTemplate();
        List<String> typeList = new ArrayList<>();
        typeList.add(ASDCConfiguration.HEAT_NESTED);
        typeList.add(ASDCConfiguration.HEAT_ARTIFACT);

        heatTemplate.setTemplateBody(
                verifyTheFilePrefixInArtifacts(vfModuleArtifact.getResult(), vfResourceStructure, typeList));
        heatTemplate.setTemplateName(vfModuleArtifact.getArtifactInfo().getArtifactName());

        if (vfModuleArtifact.getArtifactInfo().getArtifactTimeout() != null) {
            heatTemplate.setTimeoutMinutes(vfModuleArtifact.getArtifactInfo().getArtifactTimeout());
        } else {
            heatTemplate.setTimeoutMinutes(240);
        }

        heatTemplate.setDescription(vfModuleArtifact.getArtifactInfo().getArtifactDescription());
        heatTemplate.setVersion(BigDecimalVersion
                .castAndCheckNotificationVersionToString(vfModuleArtifact.getArtifactInfo().getArtifactVersion()));
        heatTemplate.setArtifactUuid(vfModuleArtifact.getArtifactInfo().getArtifactUUID());

        if (vfModuleArtifact.getArtifactInfo().getArtifactChecksum() != null) {
            heatTemplate.setArtifactChecksum(vfModuleArtifact.getArtifactInfo().getArtifactChecksum());
        } else {
            heatTemplate.setArtifactChecksum(MANUAL_RECORD);
        }

        Set<HeatTemplateParam> heatParam = extractHeatTemplateParameters(vfModuleArtifact.getResult(),
                vfModuleArtifact.getArtifactInfo().getArtifactUUID());
        heatTemplate.setParameters(heatParam);
        vfModuleArtifact.setHeatTemplate(heatTemplate);
    }

    protected void createHeatEnvFromArtifact(VfResourceStructure vfResourceStructure,
            VfModuleArtifact vfModuleArtifact) {
        HeatEnvironment heatEnvironment = new HeatEnvironment();
        heatEnvironment.setName(vfModuleArtifact.getArtifactInfo().getArtifactName());
        List<String> typeList = new ArrayList<>();
        typeList.add(ASDCConfiguration.HEAT);
        typeList.add(ASDCConfiguration.HEAT_VOL);
        heatEnvironment.setEnvironment(
                verifyTheFilePrefixInArtifacts(vfModuleArtifact.getResult(), vfResourceStructure, typeList));
        heatEnvironment.setDescription(vfModuleArtifact.getArtifactInfo().getArtifactDescription());
        heatEnvironment.setVersion(BigDecimalVersion
                .castAndCheckNotificationVersionToString(vfModuleArtifact.getArtifactInfo().getArtifactVersion()));
        heatEnvironment.setArtifactUuid(vfModuleArtifact.getArtifactInfo().getArtifactUUID());

        if (vfModuleArtifact.getArtifactInfo().getArtifactChecksum() != null) {
            heatEnvironment.setArtifactChecksum(vfModuleArtifact.getArtifactInfo().getArtifactChecksum());
        } else {
            heatEnvironment.setArtifactChecksum(MANUAL_RECORD);
        }
        vfModuleArtifact.setHeatEnvironment(heatEnvironment);
    }

    protected void createHeatFileFromArtifact(VfResourceStructure vfResourceStructure,
            VfModuleArtifact vfModuleArtifact, ToscaResourceStructure toscaResourceStruct) {

        HeatFiles heatFile = new HeatFiles();
        heatFile.setAsdcUuid(vfModuleArtifact.getArtifactInfo().getArtifactUUID());
        heatFile.setDescription(vfModuleArtifact.getArtifactInfo().getArtifactDescription());
        heatFile.setFileBody(vfModuleArtifact.getResult());
        heatFile.setFileName(vfModuleArtifact.getArtifactInfo().getArtifactName());
        heatFile.setVersion(BigDecimalVersion
                .castAndCheckNotificationVersionToString(vfModuleArtifact.getArtifactInfo().getArtifactVersion()));
        toscaResourceStruct.setHeatFilesUUID(vfModuleArtifact.getArtifactInfo().getArtifactUUID());
        if (vfModuleArtifact.getArtifactInfo().getArtifactChecksum() != null) {
            heatFile.setArtifactChecksum(vfModuleArtifact.getArtifactInfo().getArtifactChecksum());
        } else {
            heatFile.setArtifactChecksum(MANUAL_RECORD);
        }
        vfModuleArtifact.setHeatFiles(heatFile);
    }

    protected Service createService(ToscaResourceStructure toscaResourceStructure,
            ResourceStructure resourceStructure) {

        Metadata serviceMetadata = toscaResourceStructure.getServiceMetadata();

        Service service = new Service();

        if (serviceMetadata != null) {

            if (toscaResourceStructure.getServiceVersion() != null) {
                service.setModelVersion(toscaResourceStructure.getServiceVersion());
            }

            service.setServiceType(serviceMetadata.getValue("serviceType"));
            service.setServiceRole(serviceMetadata.getValue("serviceRole"));
            service.setCategory(serviceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_CATEGORY));

            service.setDescription(serviceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION));
            service.setModelName(serviceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_NAME));
            service.setModelUUID(serviceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
            service.setEnvironmentContext(serviceMetadata.getValue("environmentContext"));

            if (resourceStructure != null)
                service.setWorkloadContext(resourceStructure.getNotification().getWorkloadContext());

            service.setModelInvariantUUID(serviceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID));
            service.setCsar(toscaResourceStructure.getCatalogToscaCsar());
        }


        toscaResourceStructure.setCatalogService(service);
        return service;
    }

    protected ServiceProxyResourceCustomization createServiceProxy(NodeTemplate nodeTemplate, Service service,
            ToscaResourceStructure toscaResourceStructure) {

        Metadata spMetadata = nodeTemplate.getMetaData();

        ServiceProxyResourceCustomization spCustomizationResource = new ServiceProxyResourceCustomization();

        Set<ServiceProxyResourceCustomization> serviceProxyCustomizationSet = new HashSet<>();

        spCustomizationResource.setModelName(spMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_NAME));
        spCustomizationResource
                .setModelInvariantUUID(spMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID));
        spCustomizationResource.setModelUUID(spMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
        spCustomizationResource.setModelVersion(spMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_VERSION));
        spCustomizationResource.setDescription(spMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION));

        spCustomizationResource
                .setModelCustomizationUUID(spMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID));
        spCustomizationResource.setModelInstanceName(nodeTemplate.getName());
        spCustomizationResource.setToscaNodeType(nodeTemplate.getType());

        String sourceServiceUUID = spMetadata.getValue("sourceModelUuid");

        Service sourceService = serviceRepo.findOneByModelUUID(sourceServiceUUID);

        spCustomizationResource.setSourceService(sourceService);
        spCustomizationResource.setToscaNodeType(nodeTemplate.getType());
        serviceProxyCustomizationSet.add(spCustomizationResource);


        toscaResourceStructure.setCatalogServiceProxyResourceCustomization(spCustomizationResource);

        return spCustomizationResource;
    }

    protected ConfigurationResourceCustomization createConfiguration(NodeTemplate nodeTemplate,
            ToscaResourceStructure toscaResourceStructure, ServiceProxyResourceCustomization spResourceCustomization,
            Service service) {

        ConfigurationResourceCustomization configCustomizationResource = getConfigurationResourceCustomization(
                nodeTemplate, toscaResourceStructure, spResourceCustomization, service);

        ConfigurationResource configResource = getConfigurationResource(nodeTemplate);

        Set<ConfigurationResourceCustomization> configResourceCustomizationSet = new HashSet<>();

        configCustomizationResource.setConfigurationResource(configResource);

        configResourceCustomizationSet.add(configCustomizationResource);

        configResource.setConfigurationResourceCustomization(configResourceCustomizationSet);

        toscaResourceStructure.setCatalogConfigurationResource(configResource);

        toscaResourceStructure.setCatalogConfigurationResourceCustomization(configCustomizationResource);

        return configCustomizationResource;
    }

    protected ConfigurationResource createFabricConfiguration(NodeTemplate nodeTemplate,
            ToscaResourceStructure toscaResourceStructure) {

        Metadata fabricMetadata = nodeTemplate.getMetaData();

        ConfigurationResource configResource = new ConfigurationResource();

        configResource.setModelName(fabricMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_NAME));
        configResource.setModelInvariantUUID(fabricMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID));
        configResource.setModelUUID(fabricMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
        configResource.setModelVersion(fabricMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_VERSION));
        configResource.setDescription(fabricMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION));
        configResource.setToscaNodeType(nodeTemplate.getType());

        return configResource;
    }

    protected void createToscaCsar(ToscaResourceStructure toscaResourceStructure) {
        ToscaCsar toscaCsar = new ToscaCsar();
        if (toscaResourceStructure.getToscaArtifact().getArtifactChecksum() != null) {
            toscaCsar.setArtifactChecksum(toscaResourceStructure.getToscaArtifact().getArtifactChecksum());
        } else {
            toscaCsar.setArtifactChecksum(MANUAL_RECORD);
        }
        toscaCsar.setArtifactUUID(toscaResourceStructure.getToscaArtifact().getArtifactUUID());
        toscaCsar.setName(toscaResourceStructure.getToscaArtifact().getArtifactName());
        toscaCsar.setVersion(toscaResourceStructure.getToscaArtifact().getArtifactVersion());
        toscaCsar.setDescription(toscaResourceStructure.getToscaArtifact().getArtifactDescription());
        toscaCsar.setUrl(toscaResourceStructure.getToscaArtifact().getArtifactURL());

        toscaResourceStructure.setCatalogToscaCsar(toscaCsar);
    }

    protected VnfcCustomization findExistingVfc(Set<VnfcCustomization> vnfcCustomizations, String customizationUUID) {
        VnfcCustomization vnfcCustomization = null;
        for (VnfcCustomization vnfcCustom : vnfcCustomizations) {
            if (vnfcCustom != null && vnfcCustom.getModelCustomizationUUID().equals(customizationUUID)) {
                vnfcCustomization = vnfcCustom;
            }
        }

        if (vnfcCustomization == null)
            vnfcCustomization = vnfcCustomizationRepo.findOneByModelCustomizationUUID(customizationUUID);
        // vnfcCustomization = new VnfcCustomization();

        return vnfcCustomization;
    }

    protected CvnfcCustomization findExistingCvfc(Set<CvnfcCustomization> cvnfcCustomizations,
            String customizationUUID) {
        CvnfcCustomization cvnfcCustomization = null;
        for (CvnfcCustomization cvnfcCustom : cvnfcCustomizations) {
            if (cvnfcCustom != null && cvnfcCustom.getModelCustomizationUUID().equals(customizationUUID)) {
                cvnfcCustomization = cvnfcCustom;
            }
        }

        if (cvnfcCustomization == null)
            cvnfcCustomization = cvnfcCustomizationRepo.findOneByModelCustomizationUUID(customizationUUID);

        return cvnfcCustomization;
    }

    protected NetworkResourceCustomization createNetwork(NodeTemplate networkNodeTemplate,
            ToscaResourceStructure toscaResourceStructure, HeatTemplate heatTemplate, String aicMax, String aicMin,
            Service service) {

        NetworkResourceCustomization networkResourceCustomization =
                networkCustomizationRepo.findOneByModelCustomizationUUID(
                        networkNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID));

        boolean networkUUIDsMatch = true;
        // Check to make sure the NetworkResourceUUID on the Customization record matches the NetworkResourceUUID from
        // the distribution.
        // If not we'll update the Customization record with latest from the distribution
        if (networkResourceCustomization != null) {
            String existingNetworkModelUUID = networkResourceCustomization.getNetworkResource().getModelUUID();
            String latestNetworkModelUUID =
                    networkNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_UUID);

            if (!existingNetworkModelUUID.equals(latestNetworkModelUUID)) {
                networkUUIDsMatch = false;
            }

        }

        if (networkResourceCustomization != null && !networkUUIDsMatch) {

            NetworkResource networkResource =
                    createNetworkResource(networkNodeTemplate, toscaResourceStructure, heatTemplate, aicMax, aicMin);

            networkResourceCustomization.setNetworkResource(networkResource);

            networkCustomizationRepo.saveAndFlush(networkResourceCustomization);


        } else if (networkResourceCustomization == null) {
            networkResourceCustomization =
                    createNetworkResourceCustomization(networkNodeTemplate, toscaResourceStructure);

            NetworkResource networkResource = findExistingNetworkResource(service,
                    networkNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
            if (networkResource == null)
                networkResource = createNetworkResource(networkNodeTemplate, toscaResourceStructure, heatTemplate,
                        aicMax, aicMin);

            networkResource.addNetworkResourceCustomization(networkResourceCustomization);
            networkResourceCustomization.setNetworkResource(networkResource);
        }

        return networkResourceCustomization;
    }

    protected NetworkResource findExistingNetworkResource(Service service, String modelUUID) {
        NetworkResource networkResource = null;
        for (NetworkResourceCustomization networkCustom : service.getNetworkCustomizations()) {
            if (networkCustom.getNetworkResource() != null
                    && networkCustom.getNetworkResource().getModelUUID().equals(modelUUID)) {
                networkResource = networkCustom.getNetworkResource();
            }
        }
        if (networkResource == null)
            networkResource = networkRepo.findResourceByModelUUID(modelUUID);

        return networkResource;
    }

    protected NetworkResourceCustomization createNetworkResourceCustomization(NodeTemplate networkNodeTemplate,
            ToscaResourceStructure toscaResourceStructure) {
        NetworkResourceCustomization networkResourceCustomization = new NetworkResourceCustomization();
        networkResourceCustomization.setModelInstanceName(
                testNull(networkNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_NAME)));
        networkResourceCustomization.setModelCustomizationUUID(
                testNull(networkNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID)));

        networkResourceCustomization.setNetworkTechnology(
                testNull(toscaResourceStructure.getSdcCsarHelper().getNodeTemplatePropertyLeafValue(networkNodeTemplate,
                        SdcPropertyNames.PROPERTY_NAME_NETWORKTECHNOLOGY)));
        networkResourceCustomization.setNetworkType(testNull(toscaResourceStructure.getSdcCsarHelper()
                .getNodeTemplatePropertyLeafValue(networkNodeTemplate, SdcPropertyNames.PROPERTY_NAME_NETWORKTYPE)));
        networkResourceCustomization.setNetworkRole(testNull(toscaResourceStructure.getSdcCsarHelper()
                .getNodeTemplatePropertyLeafValue(networkNodeTemplate, SdcPropertyNames.PROPERTY_NAME_NETWORKROLE)));
        networkResourceCustomization.setNetworkScope(testNull(toscaResourceStructure.getSdcCsarHelper()
                .getNodeTemplatePropertyLeafValue(networkNodeTemplate, SdcPropertyNames.PROPERTY_NAME_NETWORKSCOPE)));
        return networkResourceCustomization;
    }

    protected NetworkResource createNetworkResource(NodeTemplate networkNodeTemplate,
            ToscaResourceStructure toscaResourceStructure, HeatTemplate heatTemplate, String aicMax, String aicMin) {
        NetworkResource networkResource = new NetworkResource();
        String providerNetwork = toscaResourceStructure.getSdcCsarHelper().getNodeTemplatePropertyLeafValue(
                networkNodeTemplate, SdcPropertyNames.PROPERTY_NAME_PROVIDERNETWORK_ISPROVIDERNETWORK);

        if ("true".equalsIgnoreCase(providerNetwork)) {
            networkResource.setNeutronNetworkType(PROVIDER);
        } else {
            networkResource.setNeutronNetworkType(BASIC);
        }

        networkResource.setModelName(
                testNull(networkNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_NAME)));

        networkResource.setModelInvariantUUID(
                testNull(networkNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID)));
        networkResource.setModelUUID(
                testNull(networkNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_UUID)));
        networkResource.setModelVersion(
                testNull(networkNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_VERSION)));

        networkResource.setAicVersionMax(aicMax);
        networkResource.setAicVersionMin(aicMin);
        networkResource.setToscaNodeType(networkNodeTemplate.getType());
        networkResource.setDescription(
                testNull(networkNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION)));
        networkResource.setOrchestrationMode(HEAT);
        networkResource.setHeatTemplate(heatTemplate);
        return networkResource;
    }

    protected CollectionNetworkResourceCustomization createNetworkCollection(NodeTemplate networkNodeTemplate,
            ToscaResourceStructure toscaResourceStructure, Service service) {

        CollectionNetworkResourceCustomization collectionNetworkResourceCustomization =
                new CollectionNetworkResourceCustomization();

        // **** Build Object to populate Collection_Resource table
        CollectionResource collectionResource = new CollectionResource();

        collectionResource
                .setModelName(networkNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_NAME));
        collectionResource.setModelInvariantUUID(
                networkNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID));
        collectionResource
                .setModelUUID(networkNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
        collectionResource
                .setModelVersion(networkNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_VERSION));
        collectionResource
                .setDescription(networkNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION));
        collectionResource.setToscaNodeType(networkNodeTemplate.getType());

        toscaResourceStructure.setCatalogCollectionResource(collectionResource);

        // **** Build object to populate Collection_Resource_Customization table
        NetworkCollectionResourceCustomization ncfc = new NetworkCollectionResourceCustomization();

        ncfc.setFunction(toscaResourceStructure.getSdcCsarHelper().getNodeTemplatePropertyLeafValue(networkNodeTemplate,
                "cr_function"));
        ncfc.setRole(toscaResourceStructure.getSdcCsarHelper().getNodeTemplatePropertyLeafValue(networkNodeTemplate,
                "cr_role"));
        ncfc.setType(toscaResourceStructure.getSdcCsarHelper().getNodeTemplatePropertyLeafValue(networkNodeTemplate,
                "cr_type"));

        ncfc.setModelInstanceName(networkNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_NAME));
        ncfc.setModelCustomizationUUID(
                networkNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID));

        Set<CollectionNetworkResourceCustomization> networkResourceCustomizationSet = new HashSet<>();
        networkResourceCustomizationSet.add(collectionNetworkResourceCustomization);

        ncfc.setNetworkResourceCustomization(networkResourceCustomizationSet);

        ncfc.setCollectionResource(collectionResource);
        toscaResourceStructure.setCatalogCollectionResourceCustomization(ncfc);

        // *** Build object to populate the Instance_Group table
        List<Group> groupList =
                toscaResourceStructure.getSdcCsarHelper().getGroupsOfOriginOfNodeTemplateByToscaGroupType(
                        networkNodeTemplate, "org.openecomp.groups.NetworkCollection");

        List<NetworkInstanceGroup> networkInstanceGroupList = new ArrayList<>();

        List<CollectionResourceInstanceGroupCustomization> collectionResourceInstanceGroupCustomizationList =
                new ArrayList<CollectionResourceInstanceGroupCustomization>();

        for (Group group : groupList) {

            NetworkInstanceGroup networkInstanceGroup = new NetworkInstanceGroup();
            Metadata instanceMetadata = group.getMetadata();
            networkInstanceGroup.setModelName(instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_NAME));
            networkInstanceGroup
                    .setModelInvariantUUID(instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID));
            networkInstanceGroup.setModelUUID(instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
            networkInstanceGroup.setModelVersion(instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_VERSION));
            networkInstanceGroup.setToscaNodeType(group.getType());
            networkInstanceGroup.setRole(SubType.SUB_INTERFACE.toString()); // Set
            // Role
            networkInstanceGroup.setType(InstanceGroupType.L3_NETWORK); // Set
            // type
            networkInstanceGroup.setCollectionResource(collectionResource);

            // ****Build object to populate
            // Collection_Resource_Instance_Group_Customization table
            CollectionResourceInstanceGroupCustomization crInstanceGroupCustomization =
                    new CollectionResourceInstanceGroupCustomization();
            crInstanceGroupCustomization.setInstanceGroup(networkInstanceGroup);
            crInstanceGroupCustomization.setModelUUID(instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
            crInstanceGroupCustomization.setModelCustomizationUUID(
                    networkNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID));

            // Loop through the template policy to find the subinterface_network_quantity property name. Then extract
            // the value for it.
            List<Policy> policyList =
                    toscaResourceStructure.getSdcCsarHelper().getPoliciesOfOriginOfNodeTemplateByToscaPolicyType(
                            networkNodeTemplate, "org.openecomp.policies.scaling.Fixed");

            if (policyList != null) {
                for (Policy policy : policyList) {
                    for (String policyNetworkCollection : policy.getTargets()) {

                        if (policyNetworkCollection.equalsIgnoreCase(group.getName())) {

                            Map<String, Object> propMap = policy.getPolicyProperties();

                            if (propMap.get("quantity") != null) {

                                String quantity = toscaResourceStructure.getSdcCsarHelper()
                                        .getNodeTemplatePropertyLeafValue(networkNodeTemplate,
                                                getPropertyInput(propMap.get("quantity").toString()));

                                if (quantity != null) {
                                    crInstanceGroupCustomization
                                            .setSubInterfaceNetworkQuantity(Integer.parseInt(quantity));
                                }

                            }

                        }
                    }
                }
            }

            crInstanceGroupCustomization.setDescription(
                    toscaResourceStructure.getSdcCsarHelper().getNodeTemplatePropertyLeafValue(networkNodeTemplate,
                            instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_NAME)
                                    + "_network_collection_description"));
            crInstanceGroupCustomization.setFunction(
                    toscaResourceStructure.getSdcCsarHelper().getNodeTemplatePropertyLeafValue(networkNodeTemplate,
                            instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_NAME)
                                    + "_network_collection_function"));
            crInstanceGroupCustomization.setCollectionResourceCust(ncfc);
            collectionResourceInstanceGroupCustomizationList.add(crInstanceGroupCustomization);

            networkInstanceGroup
                    .setCollectionInstanceGroupCustomizations(collectionResourceInstanceGroupCustomizationList);

            networkInstanceGroupList.add(networkInstanceGroup);


            toscaResourceStructure.setCatalogNetworkInstanceGroup(networkInstanceGroupList);

            List<NodeTemplate> vlNodeList = toscaResourceStructure.getSdcCsarHelper()
                    .getNodeTemplateBySdcType(networkNodeTemplate, SdcTypes.VL);

            List<CollectionNetworkResourceCustomization> collectionNetworkResourceCustomizationList = new ArrayList<>();

            // *****Build object to populate the NetworkResource table
            NetworkResource networkResource = new NetworkResource();

            for (NodeTemplate vlNodeTemplate : vlNodeList) {

                String providerNetwork = toscaResourceStructure.getSdcCsarHelper().getNodeTemplatePropertyLeafValue(
                        vlNodeTemplate, SdcPropertyNames.PROPERTY_NAME_PROVIDERNETWORK_ISPROVIDERNETWORK);

                if ("true".equalsIgnoreCase(providerNetwork)) {
                    networkResource.setNeutronNetworkType(PROVIDER);
                } else {
                    networkResource.setNeutronNetworkType(BASIC);
                }

                networkResource
                        .setModelName(vlNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_NAME));

                networkResource.setModelInvariantUUID(
                        vlNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID));
                networkResource
                        .setModelUUID(vlNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
                networkResource
                        .setModelVersion(vlNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_VERSION));

                networkResource.setAicVersionMax(
                        vlNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_MAXINSTANCES));

                TempNetworkHeatTemplateLookup tempNetworkLookUp =
                        tempNetworkLookupRepo.findFirstBynetworkResourceModelName(
                                vlNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_NAME));

                if (tempNetworkLookUp != null) {

                    HeatTemplate heatTemplate =
                            heatRepo.findByArtifactUuid(tempNetworkLookUp.getHeatTemplateArtifactUuid());
                    networkResource.setHeatTemplate(heatTemplate);

                    networkResource.setAicVersionMin(tempNetworkLookUp.getAicVersionMin());

                }

                networkResource.setToscaNodeType(vlNodeTemplate.getType());
                networkResource.setDescription(
                        vlNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION));
                networkResource.setOrchestrationMode(HEAT);

                // Build object to populate the
                // Collection_Network_Resource_Customization table
                for (NodeTemplate memberNode : group.getMemberNodes()) {
                    collectionNetworkResourceCustomization.setModelInstanceName(memberNode.getName());
                }

                collectionNetworkResourceCustomization.setModelCustomizationUUID(
                        vlNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID));

                collectionNetworkResourceCustomization.setNetworkTechnology(
                        toscaResourceStructure.getSdcCsarHelper().getNodeTemplatePropertyLeafValue(vlNodeTemplate,
                                SdcPropertyNames.PROPERTY_NAME_NETWORKTECHNOLOGY));
                collectionNetworkResourceCustomization.setNetworkType(toscaResourceStructure.getSdcCsarHelper()
                        .getNodeTemplatePropertyLeafValue(vlNodeTemplate, SdcPropertyNames.PROPERTY_NAME_NETWORKTYPE));
                collectionNetworkResourceCustomization.setNetworkRole(toscaResourceStructure.getSdcCsarHelper()
                        .getNodeTemplatePropertyLeafValue(vlNodeTemplate, SdcPropertyNames.PROPERTY_NAME_NETWORKROLE));
                collectionNetworkResourceCustomization.setNetworkScope(toscaResourceStructure.getSdcCsarHelper()
                        .getNodeTemplatePropertyLeafValue(vlNodeTemplate, SdcPropertyNames.PROPERTY_NAME_NETWORKSCOPE));
                collectionNetworkResourceCustomization.setInstanceGroup(networkInstanceGroup);
                collectionNetworkResourceCustomization.setNetworkResource(networkResource);
                collectionNetworkResourceCustomization.setNetworkResourceCustomization(ncfc);

                collectionNetworkResourceCustomizationList.add(collectionNetworkResourceCustomization);
            }

        }

        return collectionNetworkResourceCustomization;
    }

    protected VnfcInstanceGroupCustomization createVNFCInstanceGroup(NodeTemplate vnfcNodeTemplate, Group group,
            VnfResourceCustomization vnfResourceCustomization, ToscaResourceStructure toscaResourceStructure) {

        Metadata instanceMetadata = group.getMetadata();
        // Populate InstanceGroup
        VFCInstanceGroup vfcInstanceGroup = new VFCInstanceGroup();

        vfcInstanceGroup.setModelName(instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_NAME));
        vfcInstanceGroup.setModelInvariantUUID(instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID));
        vfcInstanceGroup.setModelUUID(instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_UUID));
        vfcInstanceGroup.setModelVersion(instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_VERSION));
        vfcInstanceGroup.setToscaNodeType(group.getType());
        vfcInstanceGroup.setRole("SUB-INTERFACE"); // Set Role
        vfcInstanceGroup.setType(InstanceGroupType.VNFC); // Set type

        // Populate VNFCInstanceGroupCustomization
        VnfcInstanceGroupCustomization vfcInstanceGroupCustom = new VnfcInstanceGroupCustomization();

        vfcInstanceGroupCustom.setVnfResourceCust(vnfResourceCustomization);
        vnfResourceCustomization.getVnfcInstanceGroupCustomizations().add(vfcInstanceGroupCustom);

        vfcInstanceGroupCustom.setInstanceGroup(vfcInstanceGroup);
        vfcInstanceGroup.getVnfcInstanceGroupCustomizations().add(vfcInstanceGroupCustom);

        vfcInstanceGroupCustom.setDescription(instanceMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION));

        String getInputName = null;
        String groupProperty = toscaResourceStructure.getSdcCsarHelper().getGroupPropertyLeafValue(group,
                "vfc_instance_group_function");
        if (groupProperty != null) {
            int getInputIndex = groupProperty.indexOf("{get_input=");
            if (getInputIndex > -1) {
                getInputName = groupProperty.substring(getInputIndex + 11, groupProperty.length() - 1);
            }
        }
        vfcInstanceGroupCustom.setFunction(toscaResourceStructure.getSdcCsarHelper()
                .getNodeTemplatePropertyLeafValue(vnfcNodeTemplate, getInputName));
        vfcInstanceGroupCustom.setInstanceGroup(vfcInstanceGroup);


        return vfcInstanceGroupCustom;

    }

    protected VfModuleCustomization createVFModuleResource(Group group, NodeTemplate vfTemplate,
            ToscaResourceStructure toscaResourceStructure, VfResourceStructure vfResourceStructure,
            IVfModuleData vfModuleData, VnfResourceCustomization vnfResource, Service service,
            Set<CvnfcCustomization> existingCvnfcSet, Set<VnfcCustomization> existingVnfcSet) {

        VfModuleCustomization vfModuleCustomization =
                findExistingVfModuleCustomization(vnfResource, vfModuleData.getVfModuleModelCustomizationUUID());
        if (vfModuleCustomization == null) {
            VfModule vfModule = findExistingVfModule(vnfResource,
                    vfTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_VFMODULEMODELUUID));
            Metadata vfMetadata = group.getMetadata();
            if (vfModule == null)
                vfModule = createVfModule(group, toscaResourceStructure, vfModuleData, vfMetadata);

            vfModuleCustomization = createVfModuleCustomization(group, toscaResourceStructure, vfModule, vfModuleData);
            vfModuleCustomization.setVnfCustomization(vnfResource);
            setHeatInformationForVfModule(toscaResourceStructure, vfResourceStructure, vfModule, vfModuleCustomization,
                    vfMetadata);
            vfModuleCustomization.setVfModule(vfModule);
            vfModule.getVfModuleCustomization().add(vfModuleCustomization);
            vnfResource.getVfModuleCustomizations().add(vfModuleCustomization);
        } else {
            vfResourceStructure.setAlreadyDeployed(true);
        }

        // ******************************************************************************************************************
        // * Extract VFC's and CVFC's then add them to VFModule
        // ******************************************************************************************************************

        Set<CvnfcConfigurationCustomization> cvnfcConfigurationCustomizations =
                new HashSet<CvnfcConfigurationCustomization>();
        Set<CvnfcCustomization> cvnfcCustomizations = new HashSet<CvnfcCustomization>();
        Set<VnfcCustomization> vnfcCustomizations = new HashSet<VnfcCustomization>();

        // Only set the CVNFC if this vfModule group is a member of it.
        List<NodeTemplate> groupMembers =
                toscaResourceStructure.getSdcCsarHelper().getMembersOfVfModule(vfTemplate, group);
        String vfModuleMemberName = null;

        for (NodeTemplate node : groupMembers) {
            vfModuleMemberName = node.getName();
        }

        // Extract CVFC lists
        List<NodeTemplate> cvfcList =
                toscaResourceStructure.getSdcCsarHelper().getNodeTemplateBySdcType(vfTemplate, SdcTypes.CVFC);

        for (NodeTemplate cvfcTemplate : cvfcList) {
            boolean cvnfcVfModuleNameMatch = false;

            for (NodeTemplate node : groupMembers) {
                vfModuleMemberName = node.getName();

                if (vfModuleMemberName.equalsIgnoreCase(cvfcTemplate.getName())) {
                    cvnfcVfModuleNameMatch = true;
                    break;
                }
            }

            if (vfModuleMemberName != null && cvnfcVfModuleNameMatch) {

                // Extract associated VFC - Should always be just one
                List<NodeTemplate> vfcList =
                        toscaResourceStructure.getSdcCsarHelper().getNodeTemplateBySdcType(cvfcTemplate, SdcTypes.VFC);

                for (NodeTemplate vfcTemplate : vfcList) {

                    VnfcCustomization vnfcCustomization = new VnfcCustomization();
                    VnfcCustomization existingVnfcCustomization = null;

                    existingVnfcCustomization = findExistingVfc(existingVnfcSet,
                            vfcTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID));

                    if (existingVnfcCustomization == null) {
                        vnfcCustomization = new VnfcCustomization();
                    } else {
                        vnfcCustomization = existingVnfcCustomization;
                    }

                    // Only Add Abstract VNFC's to our DB, ignore all others
                    if (existingVnfcCustomization == null && vfcTemplate.getMetaData()
                            .getValue(SdcPropertyNames.PROPERTY_NAME_SUBCATEGORY).equalsIgnoreCase("Abstract")) {
                        vnfcCustomization.setModelCustomizationUUID(
                                vfcTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID));
                        vnfcCustomization.setModelInstanceName(vfcTemplate.getName());
                        vnfcCustomization.setModelInvariantUUID(
                                vfcTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID));
                        vnfcCustomization
                                .setModelName(vfcTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_NAME));
                        vnfcCustomization
                                .setModelUUID(vfcTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_UUID));

                        vnfcCustomization.setModelVersion(
                                testNull(vfcTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_VERSION)));
                        vnfcCustomization.setDescription(testNull(
                                vfcTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION)));
                        vnfcCustomization.setToscaNodeType(testNull(vfcTemplate.getType()));

                        vnfcCustomizations.add(vnfcCustomization);
                        existingVnfcSet.add(vnfcCustomization);
                    }

                    // This check is needed incase the VFC subcategory is
                    // something other than Abstract. In that case we want to
                    // skip adding that record to our DB.
                    if (vnfcCustomization.getModelCustomizationUUID() != null) {
                        CvnfcCustomization cvnfcCustomization = new CvnfcCustomization();
                        cvnfcCustomization.setModelCustomizationUUID(
                                cvfcTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID));
                        cvnfcCustomization.setModelInstanceName(cvfcTemplate.getName());
                        cvnfcCustomization.setModelInvariantUUID(
                                cvfcTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID));
                        cvnfcCustomization
                                .setModelName(cvfcTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_NAME));
                        cvnfcCustomization
                                .setModelUUID(cvfcTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_UUID));

                        cvnfcCustomization.setModelVersion(
                                testNull(cvfcTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_VERSION)));
                        cvnfcCustomization.setDescription(testNull(
                                cvfcTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION)));
                        cvnfcCustomization.setToscaNodeType(testNull(cvfcTemplate.getType()));

                        if (existingVnfcCustomization != null) {
                            cvnfcCustomization.setVnfcCustomization(existingVnfcCustomization);
                        } else {
                            cvnfcCustomization.setVnfcCustomization(vnfcCustomization);
                        }

                        cvnfcCustomization.setNfcFunction(toscaResourceStructure.getSdcCsarHelper()
                                .getNodeTemplatePropertyLeafValue(cvfcTemplate, "nfc_function"));
                        cvnfcCustomization.setNfcNamingCode(toscaResourceStructure.getSdcCsarHelper()
                                .getNodeTemplatePropertyLeafValue(cvfcTemplate, "nfc_naming_code"));
                        cvnfcCustomization.setVfModuleCustomization(vfModuleCustomization);

                        cvnfcCustomizations.add(cvnfcCustomization);
                        existingCvnfcSet.add(cvnfcCustomization);

                        // *****************************************************************************************************************************************
                        // * Extract Fabric Configuration
                        // *****************************************************************************************************************************************

                        List<NodeTemplate> fabricConfigList = toscaResourceStructure.getSdcCsarHelper()
                                .getNodeTemplateBySdcType(vfTemplate, SdcTypes.CONFIGURATION);

                        for (NodeTemplate fabricTemplate : fabricConfigList) {

                            ConfigurationResource fabricConfig = null;

                            ConfigurationResource existingConfig = findExistingConfiguration(service,
                                    fabricTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_UUID));

                            if (existingConfig == null) {

                                fabricConfig = createFabricConfiguration(fabricTemplate, toscaResourceStructure);

                            } else {
                                fabricConfig = existingConfig;
                            }

                            CvnfcConfigurationCustomization cvnfcConfigurationCustomization =
                                    createCvnfcConfigurationCustomization(fabricTemplate, toscaResourceStructure,
                                            vnfResource, vfModuleCustomization, cvnfcCustomization, fabricConfig,
                                            vfTemplate, vfModuleMemberName);
                            cvnfcConfigurationCustomizations.add(cvnfcConfigurationCustomization);

                            fabricConfig.setCvnfcConfigurationCustomization(cvnfcConfigurationCustomizations);
                        }
                        cvnfcCustomization.setCvnfcConfigurationCustomization(cvnfcConfigurationCustomizations);
                    }

                }

            }
        }
        vfModuleCustomization.setCvnfcCustomization(cvnfcCustomizations);

        return vfModuleCustomization;
    }

    protected CvnfcConfigurationCustomization createCvnfcConfigurationCustomization(NodeTemplate fabricTemplate,
            ToscaResourceStructure toscaResourceStruct, VnfResourceCustomization vnfResource,
            VfModuleCustomization vfModuleCustomization, CvnfcCustomization cvnfcCustomization,
            ConfigurationResource configResource, NodeTemplate vfTemplate, String vfModuleMemberName) {

        Metadata fabricMetadata = fabricTemplate.getMetaData();

        CvnfcConfigurationCustomization cvnfcConfigurationCustomization = new CvnfcConfigurationCustomization();

        cvnfcConfigurationCustomization.setConfigurationResource(configResource);

        cvnfcConfigurationCustomization.setCvnfcCustomization(cvnfcCustomization);

        cvnfcConfigurationCustomization
                .setModelCustomizationUUID(fabricMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID));
        cvnfcConfigurationCustomization.setModelInstanceName(fabricTemplate.getName());

        List<Policy> policyList = toscaResourceStruct.getSdcCsarHelper()
                .getPoliciesOfOriginOfNodeTemplateByToscaPolicyType(vfTemplate, "org.openecomp.policies.External");

        if (policyList != null) {
            for (Policy policy : policyList) {

                for (String policyCvfcTarget : policy.getTargets()) {

                    if (policyCvfcTarget.equalsIgnoreCase(vfModuleMemberName)) {

                        Map<String, Object> propMap = policy.getPolicyProperties();

                        if (propMap.get("type").toString().equalsIgnoreCase("Fabric Policy")) {
                            cvnfcConfigurationCustomization.setPolicyName(propMap.get("name").toString());
                        }
                    }
                }
            }
        }

        cvnfcConfigurationCustomization.setConfigurationFunction(
                toscaResourceStruct.getSdcCsarHelper().getNodeTemplatePropertyLeafValue(fabricTemplate, "function"));
        cvnfcConfigurationCustomization.setConfigurationRole(
                toscaResourceStruct.getSdcCsarHelper().getNodeTemplatePropertyLeafValue(fabricTemplate, "role"));
        cvnfcConfigurationCustomization.setConfigurationType(
                toscaResourceStruct.getSdcCsarHelper().getNodeTemplatePropertyLeafValue(fabricTemplate, "type"));

        return cvnfcConfigurationCustomization;
    }

    protected ConfigurationResource findExistingConfiguration(Service service, String modelUUID) {
        ConfigurationResource configResource = null;
        for (ConfigurationResourceCustomization configurationResourceCustom : service
                .getConfigurationCustomizations()) {
            if (configurationResourceCustom.getConfigurationResource() != null
                    && configurationResourceCustom.getConfigurationResource().getModelUUID().equals(modelUUID)) {
                configResource = configurationResourceCustom.getConfigurationResource();
            }
        }

        return configResource;
    }

    protected VfModuleCustomization findExistingVfModuleCustomization(VnfResourceCustomization vnfResource,
            String vfModuleModelCustomizationUUID) {
        VfModuleCustomization vfModuleCustomization = null;
        for (VfModuleCustomization vfModuleCustom : vnfResource.getVfModuleCustomizations()) {
            if (vfModuleCustom.getModelCustomizationUUID().equalsIgnoreCase(vfModuleModelCustomizationUUID)) {
                vfModuleCustomization = vfModuleCustom;
            }
        }
        return vfModuleCustomization;
    }

    protected VfModule findExistingVfModule(VnfResourceCustomization vnfResource, String modelUUID) {
        VfModule vfModule = null;
        for (VfModuleCustomization vfModuleCustom : vnfResource.getVfModuleCustomizations()) {
            if (vfModuleCustom.getVfModule() != null && vfModuleCustom.getVfModule().getModelUUID().equals(modelUUID)) {
                vfModule = vfModuleCustom.getVfModule();
            }
        }
        if (vfModule == null)
            vfModule = vfModuleRepo.findByModelUUID(modelUUID);

        return vfModule;
    }

    protected VfModuleCustomization createVfModuleCustomization(Group group,
            ToscaResourceStructure toscaResourceStructure, VfModule vfModule, IVfModuleData vfModuleData) {
        VfModuleCustomization vfModuleCustomization = new VfModuleCustomization();

        vfModuleCustomization.setModelCustomizationUUID(vfModuleData.getVfModuleModelCustomizationUUID());

        vfModuleCustomization.setVfModule(vfModule);

        String initialCount = toscaResourceStructure.getSdcCsarHelper().getGroupPropertyLeafValue(group,
                SdcPropertyNames.PROPERTY_NAME_INITIALCOUNT);
        if (initialCount != null && initialCount.length() > 0) {
            vfModuleCustomization.setInitialCount(Integer.valueOf(initialCount));
        }

        vfModuleCustomization.setInitialCount(Integer.valueOf(toscaResourceStructure.getSdcCsarHelper()
                .getGroupPropertyLeafValue(group, SdcPropertyNames.PROPERTY_NAME_INITIALCOUNT)));

        String availabilityZoneCount = toscaResourceStructure.getSdcCsarHelper().getGroupPropertyLeafValue(group,
                SdcPropertyNames.PROPERTY_NAME_AVAILABILITYZONECOUNT);
        if (availabilityZoneCount != null && availabilityZoneCount.length() > 0) {
            vfModuleCustomization.setAvailabilityZoneCount(Integer.valueOf(availabilityZoneCount));
        }

        vfModuleCustomization.setLabel(toscaResourceStructure.getSdcCsarHelper().getGroupPropertyLeafValue(group,
                SdcPropertyNames.PROPERTY_NAME_VFMODULELABEL));

        String maxInstances = toscaResourceStructure.getSdcCsarHelper().getGroupPropertyLeafValue(group,
                SdcPropertyNames.PROPERTY_NAME_MAXVFMODULEINSTANCES);
        if (maxInstances != null && maxInstances.length() > 0) {
            vfModuleCustomization.setMaxInstances(Integer.valueOf(maxInstances));
        }

        String minInstances = toscaResourceStructure.getSdcCsarHelper().getGroupPropertyLeafValue(group,
                SdcPropertyNames.PROPERTY_NAME_MINVFMODULEINSTANCES);
        if (minInstances != null && minInstances.length() > 0) {
            vfModuleCustomization.setMinInstances(Integer.valueOf(minInstances));
        }
        return vfModuleCustomization;
    }

    protected VfModule createVfModule(Group group, ToscaResourceStructure toscaResourceStructure,
            IVfModuleData vfModuleData, Metadata vfMetadata) {
        VfModule vfModule = new VfModule();
        String vfModuleModelUUID = vfModuleData.getVfModuleModelUUID();

        if (vfModuleModelUUID == null) {
            vfModuleModelUUID = testNull(toscaResourceStructure.getSdcCsarHelper().getMetadataPropertyValue(vfMetadata,
                    SdcPropertyNames.PROPERTY_NAME_VFMODULEMODELUUID));
        } else if (vfModuleModelUUID.indexOf('.') > -1) {
            vfModuleModelUUID = vfModuleModelUUID.substring(0, vfModuleModelUUID.indexOf('.'));
        }

        vfModule.setModelInvariantUUID(testNull(toscaResourceStructure.getSdcCsarHelper()
                .getMetadataPropertyValue(vfMetadata, SdcPropertyNames.PROPERTY_NAME_VFMODULEMODELINVARIANTUUID)));
        vfModule.setModelName(testNull(toscaResourceStructure.getSdcCsarHelper().getMetadataPropertyValue(vfMetadata,
                SdcPropertyNames.PROPERTY_NAME_VFMODULEMODELNAME)));
        vfModule.setModelUUID(vfModuleModelUUID);
        vfModule.setModelVersion(testNull(toscaResourceStructure.getSdcCsarHelper().getMetadataPropertyValue(vfMetadata,
                SdcPropertyNames.PROPERTY_NAME_VFMODULEMODELVERSION)));
        vfModule.setDescription(testNull(toscaResourceStructure.getSdcCsarHelper().getMetadataPropertyValue(vfMetadata,
                SdcPropertyNames.PROPERTY_NAME_DESCRIPTION)));

        String vfModuleType = toscaResourceStructure.getSdcCsarHelper().getGroupPropertyLeafValue(group,
                SdcPropertyNames.PROPERTY_NAME_VFMODULETYPE);
        if (vfModuleType != null && "Base".equalsIgnoreCase(vfModuleType)) {
            vfModule.setIsBase(true);
        } else {
            vfModule.setIsBase(false);
        }
        return vfModule;
    }

    protected void setHeatInformationForVfModule(ToscaResourceStructure toscaResourceStructure,
            VfResourceStructure vfResourceStructure, VfModule vfModule, VfModuleCustomization vfModuleCustomization,
            Metadata vfMetadata) {

        Optional<VfModuleStructure> matchingObject = vfResourceStructure.getVfModuleStructure().stream()
                .filter(vfModuleStruct -> vfModuleStruct.getVfModuleMetadata().getVfModuleModelUUID()
                        .equalsIgnoreCase(toscaResourceStructure.getSdcCsarHelper().getMetadataPropertyValue(vfMetadata,
                                SdcPropertyNames.PROPERTY_NAME_VFMODULEMODELUUID)))
                .findFirst();

        if (matchingObject.isPresent()) {
            List<HeatFiles> heatFilesList = new ArrayList<>();
            List<HeatTemplate> volumeHeatChildTemplates = new ArrayList<HeatTemplate>();
            List<HeatTemplate> heatChildTemplates = new ArrayList<HeatTemplate>();
            HeatTemplate parentHeatTemplate = new HeatTemplate();
            String parentArtifactType = null;
            Set<String> artifacts = new HashSet<>(matchingObject.get().getVfModuleMetadata().getArtifacts());
            for (VfModuleArtifact vfModuleArtifact : vfResourceStructure.getArtifactsMapByUUID().values()) {

                List<HeatTemplate> childNestedHeatTemplates = new ArrayList<HeatTemplate>();

                if (artifacts.contains(vfModuleArtifact.getArtifactInfo().getArtifactUUID())) {
                    checkVfModuleArtifactType(vfModule, vfModuleCustomization, heatFilesList, vfModuleArtifact,
                            childNestedHeatTemplates, parentHeatTemplate, vfResourceStructure);
                }

                if (vfModuleArtifact.getArtifactInfo().getArtifactType().equals(ASDCConfiguration.HEAT_NESTED)) {
                    parentArtifactType = identifyParentOfNestedTemplate(matchingObject.get(), vfModuleArtifact);

                    if (!childNestedHeatTemplates.isEmpty()) {

                        if (parentArtifactType != null
                                && parentArtifactType.equalsIgnoreCase(ASDCConfiguration.HEAT_VOL)) {
                            volumeHeatChildTemplates.add(childNestedHeatTemplates.get(0));
                        } else {
                            heatChildTemplates.add(childNestedHeatTemplates.get(0));
                        }
                    }
                }

            }
            if (!heatFilesList.isEmpty()) {
                vfModule.setHeatFiles(heatFilesList);
            }


            // Set all Child Templates related to HEAT_VOLUME
            if (!volumeHeatChildTemplates.isEmpty()) {
                if (vfModule.getVolumeHeatTemplate() != null) {
                    vfModule.getVolumeHeatTemplate().setChildTemplates(volumeHeatChildTemplates);
                } else {
                    logger.debug("VolumeHeatTemplate not set in setHeatInformationForVfModule()");
                }
            }

            // Set all Child Templates related to HEAT
            if (!heatChildTemplates.isEmpty()) {
                if (vfModule.getModuleHeatTemplate() != null) {
                    vfModule.getModuleHeatTemplate().setChildTemplates(heatChildTemplates);
                } else {
                    logger.debug("ModuleHeatTemplate not set in setHeatInformationForVfModule()");
                }
            }
        }
    }

    protected void checkVfModuleArtifactType(VfModule vfModule, VfModuleCustomization vfModuleCustomization,
            List<HeatFiles> heatFilesList, VfModuleArtifact vfModuleArtifact, List<HeatTemplate> nestedHeatTemplates,
            HeatTemplate parentHeatTemplate, VfResourceStructure vfResourceStructure) {
        if (vfModuleArtifact.getArtifactInfo().getArtifactType().equals(ASDCConfiguration.HEAT)) {
            vfModuleArtifact.incrementDeployedInDB();
            vfModule.setModuleHeatTemplate(vfModuleArtifact.getHeatTemplate());
        } else if (vfModuleArtifact.getArtifactInfo().getArtifactType().equals(ASDCConfiguration.HEAT_VOL)) {
            vfModule.setVolumeHeatTemplate(vfModuleArtifact.getHeatTemplate());
            VfModuleArtifact volVfModuleArtifact =
                    this.getHeatEnvArtifactFromGeneratedArtifact(vfResourceStructure, vfModuleArtifact);
            vfModuleCustomization.setVolumeHeatEnv(volVfModuleArtifact.getHeatEnvironment());
            vfModuleArtifact.incrementDeployedInDB();
        } else if (vfModuleArtifact.getArtifactInfo().getArtifactType().equals(ASDCConfiguration.HEAT_ENV)) {
            if (vfModuleArtifact.getHeatEnvironment().getName().contains("volume")) {
                vfModuleCustomization.setVolumeHeatEnv(vfModuleArtifact.getHeatEnvironment());
            } else {
                vfModuleCustomization.setHeatEnvironment(vfModuleArtifact.getHeatEnvironment());
            }
            vfModuleArtifact.incrementDeployedInDB();
        } else if (vfModuleArtifact.getArtifactInfo().getArtifactType().equals(ASDCConfiguration.HEAT_ARTIFACT)) {
            heatFilesList.add(vfModuleArtifact.getHeatFiles());
            vfModuleArtifact.incrementDeployedInDB();
        } else if (vfModuleArtifact.getArtifactInfo().getArtifactType().equals(ASDCConfiguration.HEAT_NESTED)) {
            nestedHeatTemplates.add(vfModuleArtifact.getHeatTemplate());
            vfModuleArtifact.incrementDeployedInDB();
        }
    }

    protected VnfResourceCustomization createVnfResource(NodeTemplate vfNodeTemplate,
            ToscaResourceStructure toscaResourceStructure, Service service) throws ArtifactInstallerException {
        VnfResourceCustomization vnfResourceCustomization = null;
        if (vnfResourceCustomization == null) {
            VnfResource vnfResource = findExistingVnfResource(service,
                    vfNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_UUID));

            if (vnfResource == null) {
                vnfResource = createVnfResource(vfNodeTemplate);
            }

            vnfResourceCustomization =
                    createVnfResourceCustomization(vfNodeTemplate, toscaResourceStructure, vnfResource);
            vnfResourceCustomization.setVnfResources(vnfResource);
            vnfResourceCustomization.setService(service);

            // setting resource input for vnf customization
            vnfResourceCustomization.setResourceInput(
                    getResourceInput(toscaResourceStructure, vnfResourceCustomization.getModelCustomizationUUID()));
            vnfResource.getVnfResourceCustomizations().add(vnfResourceCustomization);

        }
        return vnfResourceCustomization;
    }

    protected VnfResource findExistingVnfResource(Service service, String modelUUID) {
        VnfResource vnfResource = null;
        for (VnfResourceCustomization vnfResourceCustom : service.getVnfCustomizations()) {
            if (vnfResourceCustom.getVnfResources() != null
                    && vnfResourceCustom.getVnfResources().getModelUUID().equals(modelUUID)) {
                vnfResource = vnfResourceCustom.getVnfResources();
            }
        }
        if (vnfResource == null)
            vnfResource = vnfRepo.findResourceByModelUUID(modelUUID);

        return vnfResource;
    }

    protected VnfResourceCustomization createVnfResourceCustomization(NodeTemplate vfNodeTemplate,
            ToscaResourceStructure toscaResourceStructure, VnfResource vnfResource) {
        VnfResourceCustomization vnfResourceCustomization = new VnfResourceCustomization();
        vnfResourceCustomization.setModelCustomizationUUID(
                testNull(vfNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID)));
        vnfResourceCustomization.setModelInstanceName(vfNodeTemplate.getName());

        vnfResourceCustomization.setNfFunction(testNull(toscaResourceStructure.getSdcCsarHelper()
                .getNodeTemplatePropertyLeafValue(vfNodeTemplate, SdcPropertyNames.PROPERTY_NAME_NFFUNCTION)));
        vnfResourceCustomization.setNfNamingCode(testNull(toscaResourceStructure.getSdcCsarHelper()
                .getNodeTemplatePropertyLeafValue(vfNodeTemplate, "nf_naming_code")));
        vnfResourceCustomization.setNfRole(testNull(toscaResourceStructure.getSdcCsarHelper()
                .getNodeTemplatePropertyLeafValue(vfNodeTemplate, SdcPropertyNames.PROPERTY_NAME_NFROLE)));
        vnfResourceCustomization.setNfType(testNull(toscaResourceStructure.getSdcCsarHelper()
                .getNodeTemplatePropertyLeafValue(vfNodeTemplate, SdcPropertyNames.PROPERTY_NAME_NFTYPE)));

        vnfResourceCustomization.setMultiStageDesign(toscaResourceStructure.getSdcCsarHelper()
                .getNodeTemplatePropertyLeafValue(vfNodeTemplate, MULTI_STAGE_DESIGN));

        vnfResourceCustomization.setBlueprintName(testNull(toscaResourceStructure.getSdcCsarHelper()
                .getNodeTemplatePropertyLeafValue(vfNodeTemplate, SDNC_MODEL_NAME)));

        vnfResourceCustomization.setBlueprintVersion(testNull(toscaResourceStructure.getSdcCsarHelper()
                .getNodeTemplatePropertyLeafValue(vfNodeTemplate, SDNC_MODEL_VERSION)));

        String skipPostInstConfText = toscaResourceStructure.getSdcCsarHelper()
                .getNodeTemplatePropertyLeafValue(vfNodeTemplate, SKIP_POST_INST_CONF);
        if (skipPostInstConfText != null) {
            vnfResourceCustomization.setSkipPostInstConf(Boolean.parseBoolean(skipPostInstConfText));
        }

        vnfResourceCustomization.setVnfResources(vnfResource);
        vnfResourceCustomization.setAvailabilityZoneMaxCount(Integer.getInteger(
                vfNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_AVAILABILITYZONECOUNT)));

        CapabilityAssignments vnfCustomizationCapability =
                toscaResourceStructure.getSdcCsarHelper().getCapabilitiesOf(vfNodeTemplate);

        if (vnfCustomizationCapability != null) {
            CapabilityAssignment capAssign = vnfCustomizationCapability.getCapabilityByName(SCALABLE);

            if (capAssign != null) {
                vnfResourceCustomization.setMinInstances(Integer.getInteger(toscaResourceStructure.getSdcCsarHelper()
                        .getCapabilityPropertyLeafValue(capAssign, SdcPropertyNames.PROPERTY_NAME_MININSTANCES)));
                vnfResourceCustomization.setMaxInstances(Integer.getInteger(toscaResourceStructure.getSdcCsarHelper()
                        .getCapabilityPropertyLeafValue(capAssign, SdcPropertyNames.PROPERTY_NAME_MAXINSTANCES)));
            }

        }

        toscaResourceStructure.setCatalogVnfResourceCustomization(vnfResourceCustomization);

        return vnfResourceCustomization;
    }

    protected VnfResource createVnfResource(NodeTemplate vfNodeTemplate) {
        VnfResource vnfResource = new VnfResource();
        vnfResource.setModelInvariantUUID(
                testNull(vfNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID)));
        vnfResource.setModelName(testNull(vfNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_NAME)));
        vnfResource.setModelUUID(testNull(vfNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_UUID)));

        vnfResource.setModelVersion(
                testNull(vfNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_VERSION)));
        vnfResource.setDescription(
                testNull(vfNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION)));
        vnfResource.setOrchestrationMode(HEAT);
        vnfResource.setToscaNodeType(testNull(vfNodeTemplate.getType()));
        vnfResource.setAicVersionMax(
                testNull(vfNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_MAXINSTANCES)));
        vnfResource.setAicVersionMin(
                testNull(vfNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_MININSTANCES)));
        vnfResource.setCategory(vfNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_CATEGORY));
        vnfResource.setSubCategory(vfNodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_SUBCATEGORY));

        return vnfResource;
    }

    protected AllottedResourceCustomization createAllottedResource(NodeTemplate nodeTemplate,
            ToscaResourceStructure toscaResourceStructure, Service service) {
        AllottedResourceCustomization allottedResourceCustomization =
                allottedCustomizationRepo.findOneByModelCustomizationUUID(
                        nodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID));

        if (allottedResourceCustomization == null) {
            AllottedResource allottedResource = findExistingAllottedResource(service,
                    nodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_UUID));

            if (allottedResource == null)
                allottedResource = createAR(nodeTemplate);

            toscaResourceStructure.setAllottedResource(allottedResource);
            allottedResourceCustomization = createAllottedResourceCustomization(nodeTemplate, toscaResourceStructure);
            allottedResourceCustomization.setAllottedResource(allottedResource);
            allottedResource.getAllotedResourceCustomization().add(allottedResourceCustomization);
        }
        return allottedResourceCustomization;
    }

    protected AllottedResource findExistingAllottedResource(Service service, String modelUUID) {
        AllottedResource allottedResource = null;
        for (AllottedResourceCustomization allottedResourceCustom : service.getAllottedCustomizations()) {
            if (allottedResourceCustom.getAllottedResource() != null
                    && allottedResourceCustom.getAllottedResource().getModelUUID().equals(modelUUID)) {
                allottedResource = allottedResourceCustom.getAllottedResource();
            }
        }
        if (allottedResource == null)
            allottedResource = allottedRepo.findResourceByModelUUID(modelUUID);

        return allottedResource;
    }

    protected AllottedResourceCustomization createAllottedResourceCustomization(NodeTemplate nodeTemplate,
            ToscaResourceStructure toscaResourceStructure) {
        AllottedResourceCustomization allottedResourceCustomization = new AllottedResourceCustomization();
        allottedResourceCustomization.setModelCustomizationUUID(
                testNull(nodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID)));
        allottedResourceCustomization.setModelInstanceName(nodeTemplate.getName());


        allottedResourceCustomization.setNfFunction(testNull(toscaResourceStructure.getSdcCsarHelper()
                .getNodeTemplatePropertyLeafValue(nodeTemplate, SdcPropertyNames.PROPERTY_NAME_NFFUNCTION)));
        allottedResourceCustomization.setNfNamingCode(testNull(toscaResourceStructure.getSdcCsarHelper()
                .getNodeTemplatePropertyLeafValue(nodeTemplate, "nf_naming_code")));
        allottedResourceCustomization.setNfRole(testNull(toscaResourceStructure.getSdcCsarHelper()
                .getNodeTemplatePropertyLeafValue(nodeTemplate, SdcPropertyNames.PROPERTY_NAME_NFROLE)));
        allottedResourceCustomization.setNfType(testNull(toscaResourceStructure.getSdcCsarHelper()
                .getNodeTemplatePropertyLeafValue(nodeTemplate, SdcPropertyNames.PROPERTY_NAME_NFTYPE)));

        List<NodeTemplate> vfcNodes = toscaResourceStructure.getSdcCsarHelper()
                .getVfcListByVf(allottedResourceCustomization.getModelCustomizationUUID());

        if (vfcNodes != null) {
            for (NodeTemplate vfcNode : vfcNodes) {

                allottedResourceCustomization.setProvidingServiceModelUUID(toscaResourceStructure.getSdcCsarHelper()
                        .getNodeTemplatePropertyLeafValue(vfcNode, "providing_service_uuid"));
                allottedResourceCustomization
                        .setProvidingServiceModelInvariantUUID(toscaResourceStructure.getSdcCsarHelper()
                                .getNodeTemplatePropertyLeafValue(vfcNode, "providing_service_invariant_uuid"));
                allottedResourceCustomization.setProvidingServiceModelName(toscaResourceStructure.getSdcCsarHelper()
                        .getNodeTemplatePropertyLeafValue(vfcNode, "providing_service_name"));
            }
        }


        CapabilityAssignments arCustomizationCapability =
                toscaResourceStructure.getSdcCsarHelper().getCapabilitiesOf(nodeTemplate);

        if (arCustomizationCapability != null) {
            CapabilityAssignment capAssign = arCustomizationCapability.getCapabilityByName(SCALABLE);

            if (capAssign != null) {
                allottedResourceCustomization.setMinInstances(
                        Integer.getInteger(toscaResourceStructure.getSdcCsarHelper().getCapabilityPropertyLeafValue(
                                capAssign, SdcPropertyNames.PROPERTY_NAME_MININSTANCES)));
                allottedResourceCustomization.setMaxInstances(
                        Integer.getInteger(toscaResourceStructure.getSdcCsarHelper().getCapabilityPropertyLeafValue(
                                capAssign, SdcPropertyNames.PROPERTY_NAME_MAXINSTANCES)));
            }
        }
        return allottedResourceCustomization;
    }

    protected AllottedResource createAR(NodeTemplate nodeTemplate) {
        AllottedResource allottedResource = new AllottedResource();
        allottedResource
                .setModelUUID(testNull(nodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_UUID)));
        allottedResource.setModelInvariantUUID(
                testNull(nodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID)));
        allottedResource
                .setModelName(testNull(nodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_NAME)));
        allottedResource
                .setModelVersion(testNull(nodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_VERSION)));
        allottedResource.setToscaNodeType(testNull(nodeTemplate.getType()));
        allottedResource.setSubcategory(
                testNull(nodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_SUBCATEGORY)));
        allottedResource
                .setDescription(nodeTemplate.getMetaData().getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION));
        return allottedResource;
    }

    protected Set<HeatTemplateParam> extractHeatTemplateParameters(String yamlFile, String artifactUUID) {
        // Scan the payload downloadResult and extract the HeatTemplate
        // parameters
        YamlEditor yamlEditor = new YamlEditor(yamlFile.getBytes());
        return yamlEditor.getParameterList(artifactUUID);
    }

    protected String testNull(Object object) {

        if (object == null) {
            return null;
        } else if (object.equals("NULL")) {
            return null;
        } else if (object instanceof Integer) {
            return object.toString();
        } else if (object instanceof String) {
            return (String) object;
        } else {
            return "Type not recognized";
        }
    }

    protected static String identifyParentOfNestedTemplate(VfModuleStructure vfModuleStructure,
            VfModuleArtifact heatNestedArtifact) {

        if (vfModuleStructure.getArtifactsMap().get(ASDCConfiguration.HEAT) != null && vfModuleStructure
                .getArtifactsMap().get(ASDCConfiguration.HEAT).get(0).getArtifactInfo().getRelatedArtifacts() != null) {
            for (IArtifactInfo unknownArtifact : vfModuleStructure.getArtifactsMap().get(ASDCConfiguration.HEAT).get(0)
                    .getArtifactInfo().getRelatedArtifacts()) {
                if (heatNestedArtifact.getArtifactInfo().getArtifactUUID().equals(unknownArtifact.getArtifactUUID())) {
                    return ASDCConfiguration.HEAT;
                }

            }
        }

        if (vfModuleStructure.getArtifactsMap().get(ASDCConfiguration.HEAT_VOL) != null
                && vfModuleStructure.getArtifactsMap().get(ASDCConfiguration.HEAT_VOL).get(0).getArtifactInfo()
                        .getRelatedArtifacts() != null) {
            for (IArtifactInfo unknownArtifact : vfModuleStructure.getArtifactsMap().get(ASDCConfiguration.HEAT_VOL)
                    .get(0).getArtifactInfo().getRelatedArtifacts()) {
                if (heatNestedArtifact.getArtifactInfo().getArtifactUUID().equals(unknownArtifact.getArtifactUUID())) {
                    return ASDCConfiguration.HEAT_VOL;
                }

            }
        }

        // Does not belong to anything
        return null;

    }

    protected static String createVNFName(VfResourceStructure vfResourceStructure) {

        return vfResourceStructure.getNotification().getServiceName() + "/"
                + vfResourceStructure.getResourceInstance().getResourceInstanceName();
    }

    protected static String createVfModuleName(VfModuleStructure vfModuleStructure) {

        return createVNFName(vfModuleStructure.getParentVfResource()) + "::"
                + vfModuleStructure.getVfModuleMetadata().getVfModuleModelName();
    }

    protected String getPropertyInput(String propertyName) {

        String inputName = new String();

        if (propertyName != null) {
            int getInputIndex = propertyName.indexOf("{get_input=");
            if (getInputIndex > -1) {
                inputName = propertyName.substring(getInputIndex + 11, propertyName.length() - 1);
            }
        }

        return inputName;
    }

    // this method add provided vnfCustomization to service with
    // existing customization available in db.
    private void addVnfCustomization(Service service, VnfResourceCustomization vnfResourceCustomization) {
        List<Service> services = serviceRepo.findByModelUUID(service.getModelUUID());
        if (services.size() > 0) {
            // service exist in db
            Service existingService = services.get(0);
            List<VnfResourceCustomization> vnfCustomizations = existingService.getVnfCustomizations();
            vnfCustomizations.forEach(e -> service.getVnfCustomizations().add(e));
        }
        service.getVnfCustomizations().add(vnfResourceCustomization);
    }


    protected static Timestamp getCurrentTimeStamp() {

        return new Timestamp(new Date().getTime());
    }

}

