package org.mskcc.cbio.portal.web;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Size;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cbioportal.web.parameter.PagingConstants;
import org.json.simple.JSONObject;
import org.mskcc.cbio.portal.model.virtualstudy.VirtualStudy;
import org.mskcc.cbio.portal.model.virtualstudy.VirtualStudyData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping("/proxy/session")
public class ProxySessionServiceController {
	
	private static final Log LOG = LogFactory.getLog(ProxySessionServiceController.class);


    @Value("${session.service.url:}")
    private String sessionServiceURL;

    @Value("${session.service.user:}")
    private String sessionServiceUser;

    @Value("${session.service.password:}")
    private String sessionServicePassword;
    
    
    @RequestMapping(value = "/{type}/{id}", method = RequestMethod.GET)
    public @ResponseBody Object getSession(
            @PathVariable SessionType type, 
            @PathVariable String      id,
            HttpServletResponse       response) throws IOException {
        
        try {
            RestTemplate restTemplate = new RestTemplate();

            // add basic authentication in header
            HttpEntity<String> headers = new HttpEntity<String>(getHttpHeaders());
            ResponseEntity<HashMap> responseEntity = restTemplate.exchange(sessionServiceURL + type + "/" + id,
                                                                        HttpMethod.GET,
                                                                        headers,
                                                                        HashMap.class);
            
            if (type.equals(SessionType.virtual_study) || type.equals(SessionType.group)) {
                ObjectMapper mapper = new ObjectMapper()
                        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                
                String virtualStudyStr = mapper.writeValueAsString(responseEntity.getBody());
                
                return mapper.readValue(virtualStudyStr, VirtualStudy.class);
                
            }
            return responseEntity.getBody();
        } catch (Exception exception) {
            response.sendError(HttpStatus.NOT_FOUND.value());
        }
        return null;
    }

    private Boolean isBasicAuthEnabled() {
        return sessionServiceUser != null && !sessionServiceUser.equals("") && sessionServicePassword != null && !sessionServicePassword.equals("");
    }
    
    private Boolean isSessionServiceEnabled() {
    	return !StringUtils.isEmpty(sessionServiceURL);
    }

    private HttpHeaders getHttpHeaders() {
        return new HttpHeaders() {{
            if (isBasicAuthEnabled()) {
                String auth = sessionServiceUser + ":" + sessionServicePassword;
                byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
                String authHeader = "Basic " + new String(encodedAuth);
                set( "Authorization", authHeader);
            }
            set( "Content-Type", "application/json");
        }};
     }
    
    @RequestMapping(value = "/virtual_study", method = RequestMethod.GET)
    public @ResponseBody List<VirtualStudy> getUserStudies() throws JsonProcessingException {
        List<VirtualStudy> virtualStudies = new ArrayList<VirtualStudy>();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (isSessionServiceEnabled()
                && !(authentication == null || authentication instanceof AnonymousAuthenticationToken)) {

            Map<String, String> map = new HashMap<>();
            map.put("data.users", ((UserDetails) authentication.getPrincipal()).getUsername());

            ObjectMapper mapper = new ObjectMapper();
            String query = mapper.writeValueAsString(map);

            RestTemplate restTemplate = new RestTemplate();

            HttpEntity<Object> httpEntity = new HttpEntity<Object>(query, getHttpHeaders());

            ResponseEntity<List<VirtualStudy>> responseEntity = restTemplate.exchange(
                    sessionServiceURL + "virtual_study/query?field=data.users&value="
                            + ((UserDetails) authentication.getPrincipal()).getUsername(),
                    HttpMethod.GET, httpEntity, new ParameterizedTypeReference<List<VirtualStudy>>() {
                    });

            virtualStudies = responseEntity.getBody();
        }
        return virtualStudies;

    }
    
    @RequestMapping(value = "/{type}", method = RequestMethod.POST)
    public ResponseEntity<HashMap> addSession(@PathVariable SessionType type,
                                               @RequestBody  JSONObject  body) throws IOException {
        
        return addSession(type, Optional.empty(), body);
    }
    
    @RequestMapping(value = "/virtual_study/save", method = RequestMethod.POST)
    public ResponseEntity<HashMap> addUserSavedVirtualStudy(@RequestBody  JSONObject body) throws IOException {
        
        return addSession(SessionType.virtual_study, Optional.of(SessionOperation.save), body);
    }

	@RequestMapping(value = "/virtual_study/add/{id}", method = RequestMethod.GET)
	public void addUsertoVirtualStudy(@PathVariable String id, HttpServletResponse response) throws IOException {

		updateVirtualStudyUsers(id, Operation.add, response);
	}

	@RequestMapping(value = "/virtual_study/delete/{id}", method = RequestMethod.GET)
	public void removeUserFromVirtualStudy(@PathVariable String id, HttpServletResponse response) throws IOException {

		updateVirtualStudyUsers(id, Operation.delete, response);

	}
    
	private ResponseEntity<HashMap> addSession(SessionType type, Optional<SessionOperation> operation, JSONObject body) {
		try {
			HttpEntity httpEntity = null;
			if (type.equals(SessionType.virtual_study) || type.equals(SessionType.group)) {
				ObjectMapper mapper = new ObjectMapper();
				// JSON from file to Object
				VirtualStudyData virtualStudyData = mapper.readValue(body.toString(), VirtualStudyData.class);

				Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

				if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
					String userName = ((UserDetails) authentication.getPrincipal()).getUsername();
					virtualStudyData.setOwner(userName);
					if ((operation.isPresent() && operation.get().equals(SessionOperation.save))
							|| type.equals(SessionType.group)) {
						virtualStudyData.setUsers(Collections.singleton(userName));
					}
				}
				
				// use basic authentication for session service if set
				httpEntity = new HttpEntity<VirtualStudyData>(virtualStudyData, getHttpHeaders());
			} else {
				httpEntity = new HttpEntity<JSONObject>(body, getHttpHeaders());
			}
			// returns {"id":"5799648eef86c0e807a2e965"}
			// using HashMap because converter is MappingJackson2HttpMessageConverter
			// (Jackson 2 is on classpath)
			// was String when default converter StringHttpMessageConverter was used
			RestTemplate restTemplate = new RestTemplate();
			return restTemplate.exchange(sessionServiceURL + type, HttpMethod.POST,
					httpEntity, HashMap.class);

		} catch (IOException e) {
			LOG.error(e);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}
    
    
	private void updateVirtualStudyUsers(String id, Operation operation, HttpServletResponse response)
			throws IOException {

		try {

			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

			if (isSessionServiceEnabled()
					&& !(authentication == null || authentication instanceof AnonymousAuthenticationToken)) {

				String user = ((UserDetails) authentication.getPrincipal()).getUsername();

				ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
						false);
				String virtualStudyStr = mapper.writeValueAsString(getSession(SessionType.virtual_study, id, response));
				VirtualStudy virtualStudy = mapper.readValue(virtualStudyStr, VirtualStudy.class);
				Set<String> users = virtualStudy.getData().getUsers();
				
				switch (operation) {
					case add: {
						users.add(user);
						break;
					}
					case delete: {
						users.remove(user);
						break;
					}
				}
				virtualStudy.getData().setUsers(users);
				RestTemplate restTemplate = new RestTemplate();
				HttpEntity<Object> httpEntity = new HttpEntity<Object>(virtualStudy.getData(), getHttpHeaders());

				restTemplate.exchange(sessionServiceURL + SessionType.virtual_study + "/" + id, HttpMethod.PUT,
						httpEntity, HashMap.class);
			} else {
				response.sendError(HttpStatus.SERVICE_UNAVAILABLE.value());
			}

		} catch (Exception e) {
			LOG.error(e);
			response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

	}

	@RequestMapping(value = "/groups/fetch", method = RequestMethod.POST)
	public ResponseEntity<List<VirtualStudy>> fetchUserGroups(
			@Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE) @RequestBody List<String> studyIds,
			HttpServletResponse response) throws IOException {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (isSessionServiceEnabled() && !(authentication == null
				|| (authentication instanceof AnonymousAuthenticationToken))) {

			Map<String, Object> map = new HashMap<>();
			map.put("data.users", ((UserDetails) authentication.getPrincipal()).getUsername());
			map.put("data.origin", studyIds);

			ObjectMapper mapper = new ObjectMapper();
			String query = mapper.writeValueAsString(map);

			RestTemplate restTemplate = new RestTemplate();

			HttpEntity<String> httpEntity = new HttpEntity<String>(query, getHttpHeaders());

			return restTemplate.exchange(
					sessionServiceURL + SessionType.group + "/query/fetch",
					HttpMethod.POST,
					httpEntity,
					new ParameterizedTypeReference<List<VirtualStudy>>() {});

		}
		return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
	}
	
	@RequestMapping(value = "/{type}/{id}", method = RequestMethod.POST)
	public void updateSession(
			@PathVariable SessionType type,
			@PathVariable String id,
            @RequestBody  JSONObject  body,
			HttpServletResponse response) throws IOException {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		// updates only allowed for type group for now
		List<SessionType> validSessionTypes = Arrays.asList(SessionType.group);

		if (validSessionTypes.contains(type) && isSessionServiceEnabled() || !(authentication == null
				|| (authentication instanceof AnonymousAuthenticationToken))) {

			ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
					false);
			String virtualStudyStr = mapper.writeValueAsString(getSession(type, id, response));
			VirtualStudy virtualStudy = mapper.readValue(virtualStudyStr, VirtualStudy.class);

			String user = ((UserDetails) authentication.getPrincipal()).getUsername();
			if (user == virtualStudy.getData().getOwner() && virtualStudy.getType() == type.toString()) {

				VirtualStudyData updatedVirtualStudyData = mapper.readValue(body.toString(), VirtualStudyData.class);
				updatedVirtualStudyData.setCreated(virtualStudy.getData().getCreated());
				updatedVirtualStudyData.setOwner(virtualStudy.getData().getOwner());
				updatedVirtualStudyData.setUsers(virtualStudy.getData().getUsers());

				virtualStudy.setData(updatedVirtualStudyData);
				RestTemplate restTemplate = new RestTemplate();
				HttpEntity<Object> httpEntity = new HttpEntity<Object>(updatedVirtualStudyData, getHttpHeaders());
				
				restTemplate.put(sessionServiceURL + type + "/" + id, httpEntity);
				
			} else {
				response.setStatus(HttpStatus.UNAUTHORIZED.value());
			}
		}
		response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
	}
}

enum SessionType {
    main_session,
    virtual_study,
    group,
    comparison_session;
}

enum Operation {
    add,
    delete;
}

enum SessionOperation {
    save,
    share;
}
