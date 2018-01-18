package org.cbioportal.web;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cbioportal.model.virtualstudy.VirtualStudy;
import org.cbioportal.model.virtualstudy.VirtualStudyData;
import org.cbioportal.web.config.annotation.InternalApi;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;

@InternalApi
@RestController
@Validated
@Api(tags = "Session Management" , description = " ")
public class SessionServiceController {

	
	@Value("${session.service.url:''}")
	private String sessionServiceURL;

	@RequestMapping(value = "/session/{type}/{id}", method = RequestMethod.GET)
	public @ResponseBody Map getSessionService(
			@PathVariable SessionType type, 
			@PathVariable String id,
			HttpMethod method,
			HttpServletResponse response)
			throws URISyntaxException, IOException {
		
		try {
			RestTemplate restTemplate = new RestTemplate();

			ResponseEntity<HashMap> responseEntity = restTemplate.exchange(sessionServiceURL + type + "/" + id, method, null,
					HashMap.class);

			return responseEntity.getBody();
		} catch (Exception exception) {
			String errorMessage = "Unexpected error: " + exception.getLocalizedMessage();
			response.sendError(503, errorMessage);
		}
		return null;
	}
	
	@RequestMapping(value = {"/session/{type}", "/session/{type}/{operation}"}, method = RequestMethod.POST)
	public @ResponseBody Map addSessionService(
			@PathVariable SessionType type, 
			@PathVariable Optional<SessionOperation> operation,
			@RequestBody JSONObject body) throws IOException {
		
		HttpEntity httpEntity = new HttpEntity<JSONObject>(body);
		if (type.equals(SessionType.virtual_study)) {
			ObjectMapper mapper = new ObjectMapper();
			// JSON from file to Object
			VirtualStudyData virtualStudyData = mapper.readValue(body.toString(), VirtualStudyData.class);
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
				virtualStudyData.setOwner(authentication.getName());
				if(operation.isPresent() && operation.get().equals(SessionOperation.save)) {
					virtualStudyData.setUsers(Collections.singleton(authentication.getName()));
				}
			}
			httpEntity = new HttpEntity<VirtualStudyData>(virtualStudyData);
		}
		// returns {"id":"5799648eef86c0e807a2e965"}
		// using HashMap because converter is MappingJackson2HttpMessageConverter
		// (Jackson 2 is on classpath)
		// was String when default converter StringHttpMessageConverter was used
		RestTemplate restTemplate = new RestTemplate();

		ResponseEntity<HashMap> responseEntity = restTemplate.exchange(sessionServiceURL + type, HttpMethod.POST, httpEntity,
				HashMap.class);

		return responseEntity.getBody();

	}
	  
	@RequestMapping(value = "/session/virtual_study", method = RequestMethod.GET)
	public @ResponseBody List<VirtualStudy> getUserStudies(HttpServletRequest request,
			HttpServletResponse response) {
		List<VirtualStudy> virtualStudies = new ArrayList<>();
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<VirtualStudy[]> responseEntity = restTemplate.getForEntity(
					sessionServiceURL + "virtual_study/query?field=data.users&value=" + authentication.getName(),
					VirtualStudy[].class);
			virtualStudies = Arrays.asList(responseEntity.getBody());

		}
		return virtualStudies;
	}
	
	@RequestMapping(value = "/session/virtual_study/{id}", method = RequestMethod.PUT)
	public void addUsertoVirtualStudy(@PathVariable String id, 
									HttpServletResponse response) throws IOException {
		
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
			updateVirutalStudyUser(id, authentication.getName(), Operation.delete, response);
		} else {
			response.sendError(HttpStatus.SERVICE_UNAVAILABLE.value());
		}
	}
	
	@RequestMapping(value = "/session/virtual_study/{id}", method = RequestMethod.DELETE)
	public void removeUserFromVirtualStudy(@PathVariable String id,
										  HttpServletRequest request,
										  HttpServletResponse response) throws IOException {
		
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
			updateVirutalStudyUser(id, authentication.getName(), Operation.delete, response);
		} else {
			response.sendError(HttpStatus.SERVICE_UNAVAILABLE.value());
		}
		
	}
	
	
	private void updateVirutalStudyUser(String id,
			   String user,
			   Operation operation,
			   HttpServletResponse response) throws IOException {
		try {
			ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
					false);
			String virtualStudyStr = mapper
					.writeValueAsString(getSessionService(SessionType.virtual_study, id, HttpMethod.GET, response));
			VirtualStudy virtualStudy = mapper.readValue(virtualStudyStr, VirtualStudy.class);
			Set<String> users = virtualStudy.getData().getUsers();
			switch (operation) {
			case add: {
				users.add(user);
			}
			case delete: {
				users.remove(user);
			}
			}
			virtualStudy.getData().setUsers(users);
			System.out.println(virtualStudy.getData());
			System.out.println(virtualStudy.getData().getUsers());
			new RestTemplate().put(sessionServiceURL + "virtual_study/" + id, virtualStudy.getData());

		} catch (Exception e) {
			e.printStackTrace();
			response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}
}

enum SessionType {
    main_session,
    virtual_study;
}

enum Operation {
    add,
    delete;
}

enum SessionOperation {
	save,
	share;
}
