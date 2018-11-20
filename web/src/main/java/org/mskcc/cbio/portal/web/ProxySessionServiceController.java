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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONObject;
import org.mskcc.cbio.portal.model.virtualstudy.VirtualStudy;
import org.mskcc.cbio.portal.model.virtualstudy.VirtualStudyData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

@Controller
@RequestMapping("/proxy/session")
public class ProxySessionServiceController {

    @Value("${session.service.url:}")
    private String sessionServiceURL;

    @Value("${session.service.user:}")
    private String sessionServiceUser;

    @Value("${session.service.password:}")
    private String sessionServicePassword;
    
    
    @RequestMapping(value = "/{type}/{id}", method = RequestMethod.GET)
    public @ResponseBody Object getSessionService(
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
            
            if (type.equals(SessionType.virtual_study)) {
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

    public Boolean isBasicAuthEnabled() {
        return sessionServiceUser != null && !sessionServiceUser.equals("") && sessionServicePassword != null && !sessionServicePassword.equals("");
    }

    public HttpHeaders getHttpHeaders() {
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
    public @ResponseBody List<VirtualStudy> getUserStudies() {
        List<VirtualStudy> virtualStudies = new ArrayList<>();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!StringUtils.isEmpty(sessionServiceURL) && authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<Object> headers = new HttpEntity<Object>(getHttpHeaders());
            ResponseEntity<VirtualStudy[]> responseEntity = restTemplate.exchange(
                    sessionServiceURL + "virtual_study/query?field=data.users&value=" + 
                    ((UserDetails)authentication.getPrincipal()).getUsername(),
                    HttpMethod.GET,
                    headers,
                    VirtualStudy[].class);
            virtualStudies = Arrays.asList(responseEntity.getBody());
        }
        return virtualStudies;
        
    }
    
    @RequestMapping(value = "/{type}", method = RequestMethod.POST)
    public @ResponseBody Map addSessionService(@PathVariable SessionType type,
                                               @RequestBody  JSONObject  body) throws IOException {
        
        return addSession(type, Optional.empty(), body);

    }
    
    @RequestMapping(value = "/virtual_study/save", method = RequestMethod.POST)
    public @ResponseBody Map addUserSavedVirtualStudy(@RequestBody  JSONObject body) throws IOException {
        
        return addSession(SessionType.virtual_study, Optional.of(SessionOperation.save), body);

    }

    @RequestMapping(value = "/virtual_study/add/{id}", method = RequestMethod.GET)
    public void addUsertoVirtualStudy(@PathVariable String   id, 
                                      HttpServletResponse    response) throws IOException {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (!StringUtils.isEmpty(sessionServiceURL) && authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
                updateVirtualStudyUser(id,
                                       ((UserDetails)authentication.getPrincipal()).getUsername(),
                                       Operation.add,
                                       response);
        } else {
            response.sendError(HttpStatus.SERVICE_UNAVAILABLE.value());
        }
        
    }
    
    @RequestMapping(value = "/virtual_study/delete/{id}", method = RequestMethod.GET)
    public void removeUserFromVirtualStudy(@PathVariable String id,
                                           HttpServletResponse  response) throws IOException {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (!StringUtils.isEmpty(sessionServiceURL) && authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
            updateVirtualStudyUser(id,
                                   ((UserDetails)authentication.getPrincipal()).getUsername(),
                                   Operation.delete,
                                   response);
        } else {
            response.sendError(HttpStatus.SERVICE_UNAVAILABLE.value());
        }
        
    }
    
    private Map addSession(SessionType type, 
                           Optional<SessionOperation> operation,
                           JSONObject body) throws IOException, JsonParseException, JsonMappingException {
        
        HttpEntity httpEntity = null;
        if (type.equals(SessionType.virtual_study)) {
            ObjectMapper mapper = new ObjectMapper();
            // JSON from file to Object
            VirtualStudyData virtualStudyData = mapper.readValue(body.toString(), VirtualStudyData.class);
            
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
                    String userName = ((UserDetails)authentication.getPrincipal()).getUsername();
                virtualStudyData.setOwner(userName);
                if(operation.isPresent() && operation.get().equals(SessionOperation.save)) {
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
        ResponseEntity<HashMap> responseEntity = restTemplate.exchange(sessionServiceURL + type, HttpMethod.POST, httpEntity, HashMap.class);

        return responseEntity.getBody();
    }
    
    
    private void updateVirtualStudyUser(String              id,
                                        String              user,
                                        Operation           operation,
                                        HttpServletResponse response) throws IOException {
        
        try {
            ObjectMapper mapper = new ObjectMapper()
                                           .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
            String virtualStudyStr = mapper
                    .writeValueAsString(getSessionService(SessionType.virtual_study, id, response));
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

            restTemplate.exchange(sessionServiceURL + SessionType.virtual_study + "/" + id, HttpMethod.PUT, httpEntity, HashMap.class);

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
