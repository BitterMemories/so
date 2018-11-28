package org.onap.so.client.namingservice;

import java.util.ArrayList;
import java.util.List;

import org.onap.namingservice.model.NameGenDeleteRequest;
import org.onap.namingservice.model.NameGenDeleteResponse;
import org.onap.namingservice.model.NameGenRequest;
import org.onap.namingservice.model.NameGenResponse;
import org.onap.so.client.exception.BadResponseException;
import org.onap.so.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;



@Component
public class NamingClient{
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, NamingClient.class);
	private static final String ENDPOINT = "mso.naming.endpoint";
	private static final String AUTH = "mso.naming.auth";
	
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
    private Environment env;
	@Autowired
	private NamingClientResponseValidator namingClientResponseValidator;
	
	public String postNameGenRequest(NameGenRequest request) throws BadResponseException {
		String targetUrl = env.getProperty(ENDPOINT);
		HttpHeaders headers = setHeaders(env.getProperty(AUTH)); 
		msoLogger.info("Sending postNameGenRequest to url: " + targetUrl);
		HttpEntity<NameGenRequest> requestEntity = new HttpEntity<>(request, headers);
		ResponseEntity<NameGenResponse> response = restTemplate.postForEntity(targetUrl, requestEntity, NameGenResponse.class);
		return namingClientResponseValidator.validateNameGenResponse(response);
	}

	public String deleteNameGenRequest(NameGenDeleteRequest request) throws BadResponseException {
		String targetUrl = env.getProperty(ENDPOINT);
		HttpHeaders headers = setHeaders(env.getProperty(AUTH)); 
		msoLogger.info("Sending deleteNameGenRequest to url: " + targetUrl);
		HttpEntity<NameGenDeleteRequest> requestEntity = new HttpEntity<>(request, headers);
		ResponseEntity<NameGenDeleteResponse> response = restTemplate.exchange(targetUrl, HttpMethod.DELETE, requestEntity, NameGenDeleteResponse.class);
		return namingClientResponseValidator.validateNameGenDeleteResponse(response);
	}

	private HttpHeaders setHeaders(String auth) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		List<MediaType> acceptableMediaTypes = new ArrayList<>();
		acceptableMediaTypes.add(MediaType.APPLICATION_JSON);
		headers.setAccept(acceptableMediaTypes);
		headers.add(HttpHeaders.AUTHORIZATION, auth);
		return headers;
	}
}