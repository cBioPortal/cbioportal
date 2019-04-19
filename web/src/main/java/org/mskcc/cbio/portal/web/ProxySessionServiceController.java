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
import org.cbioportal.web.parameter.PageSession;
import org.cbioportal.web.parameter.PagingConstants;
import org.cbioportal.web.parameter.SessionType;
import org.cbioportal.web.parameter.SettingsIdentifier;
import org.cbioportal.web.parameter.StudyPageSettings;
import org.cbioportal.web.parameter.VirtualStudy;
import org.cbioportal.web.parameter.VirtualStudyData;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
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

import com.fasterxml.jackson.annotation.JsonInclude.Include;
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
    
    private boolean isAuthorized() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return !(authentication == null || (authentication instanceof AnonymousAuthenticationToken));
    }

    private String userName() {
        return ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
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
	
    @RequestMapping(value = "/{type:virtual_study|group}/{operation}/{id}", method = RequestMethod.GET)
    public void updateUsersInVirtualStudy(@PathVariable SessionType type, @PathVariable String id,
            @PathVariable Operation operation, HttpServletResponse response) throws IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // updates only allowed for type group for now
        List<SessionType> validSessionTypes = Arrays.asList(SessionType.virtual_study, SessionType.group);

        if (validSessionTypes.contains(type) && isSessionServiceEnabled()
                && !(authentication == null || authentication instanceof AnonymousAuthenticationToken)) {

            String user = ((UserDetails) authentication.getPrincipal()).getUsername();

            ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                    false);
            String virtualStudyStr = mapper.writeValueAsString(getSession(type, id, response));
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

            restTemplate.put(sessionServiceURL + type + "/" + id, httpEntity);
            
            response.sendError(HttpStatus.OK.value());
            
        } else {
            response.sendError(HttpStatus.SERVICE_UNAVAILABLE.value());
        }

    }
    
    private ResponseEntity<HashMap> addSession(SessionType type, Optional<SessionOperation> operation, JSONObject body) {
        try {
            HttpEntity httpEntity = null;
            ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                    false).setSerializationInclusion(Include.NON_NULL);
            if (type.equals(SessionType.virtual_study) || type.equals(SessionType.group)) {
                // JSON from file to Object
                VirtualStudyData virtualStudyData = mapper.readValue(body.toString(), VirtualStudyData.class);

                if (isAuthorized()) {
                    virtualStudyData.setOwner(userName());
                    if ((operation.isPresent() && operation.get().equals(SessionOperation.save))
                            || type.equals(SessionType.group)) {
                        virtualStudyData.setUsers(Collections.singleton(userName()));
                    }
                }
                
                // use basic authentication for session service if set
                httpEntity = new HttpEntity<VirtualStudyData>(virtualStudyData, getHttpHeaders());
            } else if(type.equals(SessionType.settings)){
                if (!(isAuthorized())) {
                    return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
                }
                StudyPageSettings studyPageSettings = mapper.readValue(body.toString(), StudyPageSettings.class);
                studyPageSettings.setOwner(userName());
                studyPageSettings.setUsers(Collections.singleton(userName()));
                httpEntity = new HttpEntity<StudyPageSettings>(studyPageSettings, getHttpHeaders());

            }else {
                httpEntity = new HttpEntity<JSONObject>(body, getHttpHeaders());
            }
            // returns {"id":"5799648eef86c0e807a2e965"}
            // using HashMap because converter is MappingJackson2HttpMessageConverter
            // (Jackson 2 is on classpath)
            // was String when default converter StringHttpMessageConverter was used
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<HashMap> resp =  restTemplate.exchange(sessionServiceURL + type, HttpMethod.POST,
                    httpEntity, HashMap.class);
            
            return new ResponseEntity<>(resp.getBody(), resp.getStatusCode());

        } catch (IOException e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
    
    
	@RequestMapping(value = "/groups/fetch", method = RequestMethod.POST)
	public ResponseEntity<List<VirtualStudy>> fetchUserGroups(
			@Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE) @RequestBody List<String> studyIds,
			HttpServletResponse response) throws IOException {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (isSessionServiceEnabled() && !(authentication == null
				|| (authentication instanceof AnonymousAuthenticationToken))) {

			String username =  ((UserDetails) authentication.getPrincipal()).getUsername();

			ObjectMapper mapper = new ObjectMapper();

			//ignore origin studies order
            String query = "{ $and: [{ \"data.users\": \"" + username + "\" }, { \"data.origin\": { $all: "
                    + mapper.writeValueAsString(studyIds) + " } }, { \"data.origin\": { $size: " + studyIds.size()
                    + " } } ]}";

			RestTemplate restTemplate = new RestTemplate();

			HttpEntity<String> httpEntity = new HttpEntity<String>(query, getHttpHeaders());
			
            ResponseEntity<List<VirtualStudy>> responseEntity = restTemplate.exchange(
                    sessionServiceURL + SessionType.group + "/query/fetch", HttpMethod.POST, httpEntity,
                    new ParameterizedTypeReference<List<VirtualStudy>>() {
                    });

            List<VirtualStudy> virtualStudies = responseEntity.getBody();

            return new ResponseEntity<>(virtualStudies, HttpStatus.OK);

		}
		return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
	}
	
	
	   @RequestMapping(value = "/{type}/{id}", method = RequestMethod.POST)
	    public void updateSession(
	            @PathVariable SessionType type,
	            @PathVariable String id,
	            @RequestBody  JSONObject  body,
	            HttpServletResponse response) throws IOException {

	        // updates only allowed for type group and settings
	        List<SessionType> validSessionTypes = Arrays.asList(SessionType.group, SessionType.settings);

	        if (validSessionTypes.contains(type) && isAuthorized()) {

	            ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
	                    false);
	            String sessionString = mapper.writeValueAsString(getSession(type, id, response));
	            
	            if(type.equals(SessionType.group)) {
	                VirtualStudy session = mapper.readValue(sessionString, VirtualStudy.class);
	                VirtualStudyData virtualStudyData = session.getData();
	                //only allow owner to update virtual study
	                if (userName().equals(virtualStudyData.getOwner()) && session.getType().equals(type)) {

	                    VirtualStudyData updatedVirtualStudyData = mapper.readValue(body.toString(), VirtualStudyData.class);
	                    updatedVirtualStudyData.setCreated(virtualStudyData.getCreated());
	                    updatedVirtualStudyData.setOwner(virtualStudyData.getOwner());
	                    updatedVirtualStudyData.setUsers(virtualStudyData.getUsers());

	                    RestTemplate restTemplate = new RestTemplate();
	                    HttpEntity<Object> httpEntity = new HttpEntity<Object>(updatedVirtualStudyData, getHttpHeaders());
	                    
	                    restTemplate.put(sessionServiceURL + type + "/" + id, httpEntity);
	                    response.setStatus(HttpStatus.OK.value());
	                } else {
	                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
	                }
	            } else {
	                PageSession session = mapper.readValue(sessionString, PageSession.class);
	                StudyPageSettings studyPageSettings = session.getData();
                    
	                StudyPageSettings updatedPageSettings = mapper.readValue(body.toString(), StudyPageSettings.class);
	                // only allow owner to update his session and see if the origin(studies) are same
	                if (userName().equals(studyPageSettings.getOwner()) && session.getType().equals(type)
	                        && sameOrigin(studyPageSettings.getOrigin(), updatedPageSettings.getOrigin())) {

	                    updatedPageSettings.setCreated(studyPageSettings.getCreated());
	                    updatedPageSettings.setOwner(studyPageSettings.getOwner());
	                    updatedPageSettings.setUsers(studyPageSettings.getUsers());
	                    updatedPageSettings.setOrigin(studyPageSettings.getOrigin());

	                    RestTemplate restTemplate = new RestTemplate();
	                    HttpEntity<Object> httpEntity = new HttpEntity<Object>(updatedPageSettings, getHttpHeaders());

	                    restTemplate.put(sessionServiceURL + type + "/" + id, httpEntity);
	                    response.setStatus(HttpStatus.OK.value());
	                } else {
	                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
	                }
	            }
	            
	        } else {
	            response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
	        }
	    }
	
    @RequestMapping(value = "/settings", method = RequestMethod.POST)
    public void updateUserPageSettings(@RequestBody StudyPageSettings settingsData, HttpServletResponse response) {

        try {
            ObjectMapper objectMapper = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .setSerializationInclusion(Include.NON_NULL);
            if (isSessionServiceEnabled() && isAuthorized()) {
                Map<String, Object> map = new HashMap<>();
                map.put("data.users", userName());
                map.put("data.page", settingsData.getPage());
                map.put("data.origin", settingsData.getOrigin());

                String query = objectMapper.writeValueAsString(map);

                RestTemplate restTemplate = new RestTemplate();

                HttpEntity<String> httpEntity = new HttpEntity<String>(query, getHttpHeaders());

                ResponseEntity<List<PageSession>> responseEntity = restTemplate.exchange(
                        sessionServiceURL + SessionType.settings + "/query/fetch", HttpMethod.POST, httpEntity,
                        new ParameterizedTypeReference<List<PageSession>>() {
                        });

                List<PageSession> sessions = responseEntity.getBody();

                // sort last updated in descending order
                sessions.sort((PageSession s1, PageSession s2) -> {
                    return ((StudyPageSettings) s1.getData()).getLastUpdated() > ((StudyPageSettings) s2.getData())
                            .getLastUpdated() ? -1 : 1;
                });
                JSONParser parser = new JSONParser();
                JSONObject jsonObject = (JSONObject) parser.parse(objectMapper.writeValueAsString(settingsData));

                if (sessions.isEmpty()) {
                    addSession(SessionType.settings, null, jsonObject);

                } else {
                    updateSession(SessionType.settings, sessions.get(0).getId(), jsonObject, response);
                }
                response.setStatus(HttpStatus.OK.value());
            } else {
                response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
            }
        } catch (IOException | ParseException e) {
            LOG.error(e);
            response.setStatus(HttpStatus.BAD_REQUEST.value());
        }
    }

    @RequestMapping(value = "/settings/fetch", method = RequestMethod.POST)
    public ResponseEntity<StudyPageSettings> getStudyPageUserSettings(
            @RequestBody SettingsIdentifier settingsIdentifier, HttpServletResponse response) {

        try {
            if (isSessionServiceEnabled() && isAuthorized()) {

                Map<String, Object> map = new HashMap<>();
                map.put("data.users", userName());
                map.put("data.page", settingsIdentifier.getPage());
                map.put("data.origin", settingsIdentifier.getOrigin());

                ObjectMapper mapper = new ObjectMapper();
                String query = mapper.writeValueAsString(map);

                RestTemplate restTemplate = new RestTemplate();

                HttpEntity<String> httpEntity = new HttpEntity<String>(query, getHttpHeaders());

                ResponseEntity<List<PageSession>> responseEntity = restTemplate.exchange(
                        sessionServiceURL + SessionType.settings + "/query/fetch", HttpMethod.POST, httpEntity,
                        new ParameterizedTypeReference<List<PageSession>>() {
                        });

                List<PageSession> sessions = responseEntity.getBody();

                // sort last updated in descending order
                sessions.sort((PageSession s1, PageSession s2) -> {
                    return ((StudyPageSettings) s1.getData()).getLastUpdated() > ((StudyPageSettings) s2.getData())
                            .getLastUpdated() ? -1 : 1;
                });

                return new ResponseEntity<>(sessions.isEmpty() ? null : (StudyPageSettings) sessions.get(0).getData(),
                        HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
        } catch (IOException e) {
            LOG.error(e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    private boolean sameOrigin(Set<String> set1, Set<String> set2) {
        if (set1 == null || set2 == null) {
            return false;
        }
        if (set1.size() != set2.size()) {
            return false;
        }
        return set1.containsAll(set2);
    }
}

enum Operation {
    add,
    delete;
}

enum SessionOperation {
    save,
    share;
}
