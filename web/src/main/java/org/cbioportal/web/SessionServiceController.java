package org.cbioportal.web;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Size;

import com.google.common.collect.ImmutableMap;
import org.cbioportal.service.util.CustomAttributeWithData;
import org.cbioportal.service.util.CustomDataSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.cbioportal.web.parameter.*;
import org.cbioportal.service.util.SessionServiceRequestHandler;
import org.cbioportal.session_service.domain.Session;
import org.cbioportal.session_service.domain.SessionType;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.QueryOperators;

@Controller
@RequestMapping("/session")
public class SessionServiceController {

    private static final Logger LOG = LoggerFactory.getLogger(SessionServiceController.class);
    
    @Autowired
    SessionServiceRequestHandler sessionServiceRequestHandler;

    @Autowired
    private ObjectMapper sessionServiceObjectMapper;

    @Value("${session.service.url:}")
    private String sessionServiceURL;

    private static Map<SessionPage, Class<? extends PageSettingsData>> pageToSettingsDataClass = ImmutableMap.of(
         SessionPage.study_view, StudyPageSettings.class,
         SessionPage.results_view, ResultsPageSettings.class
     );

    private boolean isAuthorized() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return !(authentication == null || (authentication instanceof AnonymousAuthenticationToken));
    }

    private String userName() {

        return ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
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

    private PageSettings getRecentlyUpdatePageSettings(String query) {

        RestTemplate restTemplate = new RestTemplate();

        HttpEntity<String> httpEntity = new HttpEntity<String>(query, sessionServiceRequestHandler.getHttpHeaders());

        ResponseEntity<List<PageSettings>> responseEntity = restTemplate.exchange(
                sessionServiceURL + SessionType.settings + "/query/fetch",
                HttpMethod.POST,
                httpEntity,
                new ParameterizedTypeReference<List<PageSettings>>() {});

        List<PageSettings> sessions = responseEntity.getBody();

        // sort last updated in descending order
        sessions.sort((PageSettings s1, PageSettings s2) -> s1.getData().getLastUpdated() > s2.getData()
                .getLastUpdated() ? -1 : 1);

        return sessions.isEmpty() ? null : sessions.get(0);
    }

    private ResponseEntity<Session> addSession(
        SessionType type, 
        Optional<SessionOperation> operation,
        JSONObject body
    ) {
        try {
            HttpEntity<?> httpEntity;
            if (type.equals(SessionType.virtual_study) || type.equals(SessionType.group)) {
                // JSON from file to Object
                VirtualStudyData virtualStudyData = sessionServiceObjectMapper.readValue(body.toString(), VirtualStudyData.class);

                if (isAuthorized()) {
                    virtualStudyData.setOwner(userName());
                    if ((operation.isPresent() && operation.get().equals(SessionOperation.save))
                            || type.equals(SessionType.group)) {
                        virtualStudyData.setUsers(Collections.singleton(userName()));
                    }
                }

                // use basic authentication for session service if set
                httpEntity = new HttpEntity<>(virtualStudyData, sessionServiceRequestHandler.getHttpHeaders());
            } else if (type.equals(SessionType.settings)) {
                if (!(isAuthorized())) {
                    return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
                }
                Class<? extends PageSettingsData> pageDataClass = pageToSettingsDataClass.get(
                    SessionPage.valueOf((String) body.get("page"))
                );
                PageSettingsData pageSettings = sessionServiceObjectMapper.readValue(
                    body.toString(),
                    pageDataClass
                );
                pageSettings.setOwner(userName());
                httpEntity = new HttpEntity<>(pageSettings, sessionServiceRequestHandler.getHttpHeaders());

            } else if(type.equals(SessionType.custom_data)) {
                // JSON from file to Object
                CustomAttributeWithData customData = sessionServiceObjectMapper.readValue(body.toString(), CustomAttributeWithData.class);

                if (isAuthorized()) {
                    customData.setOwner(userName());
                    customData.setUsers(Collections.singleton(userName()));
                }

                // use basic authentication for session service if set
                httpEntity = new HttpEntity<>(customData, sessionServiceRequestHandler.getHttpHeaders());
            } else if (type.equals(SessionType.custom_gene_list)) {
                if (!(isAuthorized())) {
                     return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
                }
                CustomGeneListData customGeneListData = sessionServiceObjectMapper.readValue(body.toString(), CustomGeneListData.class);
                customGeneListData.setUsers(Collections.singleton(userName()));
                httpEntity = new HttpEntity<>(customGeneListData, sessionServiceRequestHandler.getHttpHeaders());
            } else {
                httpEntity = new HttpEntity<>(body, sessionServiceRequestHandler.getHttpHeaders());
            }
            // returns {"id":"5799648eef86c0e807a2e965"}
            // using HashMap because converter is MappingJackson2HttpMessageConverter
            // (Jackson 2 is on classpath)
            // was String when default converter StringHttpMessageConverter was used
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Session> resp = restTemplate.exchange(sessionServiceURL + type, HttpMethod.POST, httpEntity,
                    Session.class);

            return new ResponseEntity<>(resp.getBody(), resp.getStatusCode());

        } catch (IOException e) {
            LOG.error("Error occurred", e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/{type}/{id}", method = RequestMethod.GET)
    public ResponseEntity<Session> getSession(@PathVariable SessionType type, @PathVariable String id) {

        try {
            String sessionDataJson = sessionServiceRequestHandler.getSessionDataJson(type, id);
            Session session;
            switch (type) {
                case virtual_study:
                    session = sessionServiceObjectMapper.readValue(sessionDataJson, VirtualStudy.class);
                    break;
                case settings:
                    session = sessionServiceObjectMapper.readValue(sessionDataJson, PageSettings.class);
                    break;
                case custom_data:
                    session = sessionServiceObjectMapper.readValue(sessionDataJson, CustomDataSession.class);
                    break;
                case custom_gene_list:
                    session = sessionServiceObjectMapper.readValue(sessionDataJson, CustomGeneList.class);
                    break;
                default:
                    session = sessionServiceObjectMapper.readValue(sessionDataJson, Session.class);
            }
            return new ResponseEntity<>(session, HttpStatus.OK);
        } catch (Exception exception) {
            LOG.error("Error occurred", exception);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/virtual_study", method = RequestMethod.GET)
    public ResponseEntity<List<VirtualStudy>> getUserStudies() throws JsonProcessingException {

        if (sessionServiceRequestHandler.isSessionServiceEnabled() && isAuthorized()) {
            try {

                BasicDBObject basicDBObject = new BasicDBObject();
                basicDBObject.put("data.users", Pattern.compile(userName(), Pattern.CASE_INSENSITIVE));

                RestTemplate restTemplate = new RestTemplate();

                HttpEntity<String> httpEntity = new HttpEntity<>(basicDBObject.toString(), sessionServiceRequestHandler.getHttpHeaders());
                
                ResponseEntity<List<VirtualStudy>> responseEntity = restTemplate.exchange(
                        sessionServiceURL + SessionType.virtual_study + "/query/fetch",
                        HttpMethod.POST,
                        httpEntity,
                        new ParameterizedTypeReference<List<VirtualStudy>>() {});

                return new ResponseEntity<>(responseEntity.getBody(), HttpStatus.OK);
            } catch (Exception exception) {
                LOG.error("Error occurred", exception);
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @RequestMapping(value = "/{type}", method = RequestMethod.POST)
    public ResponseEntity<Session> addSession(@PathVariable SessionType type, @RequestBody JSONObject body)
            throws IOException {

        return addSession(type, Optional.empty(), body);
    }

    @RequestMapping(value = "/virtual_study/save", method = RequestMethod.POST)
    public ResponseEntity<Session> addUserSavedVirtualStudy(@RequestBody JSONObject body) throws IOException {

        return addSession(SessionType.virtual_study, Optional.of(SessionOperation.save), body);
    }

    @RequestMapping(value = "/{type:virtual_study|group|custom_data|custom_gene_list}/{operation}/{id}", method = RequestMethod.GET)
    public void updateUsersInSession(@PathVariable SessionType type, @PathVariable String id,
            @PathVariable Operation operation, HttpServletResponse response) throws IOException {

        if (sessionServiceRequestHandler.isSessionServiceEnabled() && isAuthorized()) {
            HttpEntity<?> httpEntity;
            if (type.equals(SessionType.custom_data)) {
                String virtualStudyStr = sessionServiceObjectMapper.writeValueAsString(getSession(type, id).getBody());
                CustomDataSession customDataSession = sessionServiceObjectMapper.readValue(virtualStudyStr, CustomDataSession.class);
                CustomAttributeWithData customAttributeWithData = customDataSession.getData();
                Set<String> users = customAttributeWithData.getUsers();
                updateUserList(operation, users);
                customAttributeWithData.setUsers(users);
                httpEntity = new HttpEntity<>(customAttributeWithData, sessionServiceRequestHandler.getHttpHeaders());
             } else if (type.equals(SessionType.custom_gene_list)) {
                String customGeneListStr = sessionServiceObjectMapper.writeValueAsString(getSession(type, id).getBody());
                CustomGeneList customGeneList = sessionServiceObjectMapper.readValue(customGeneListStr, CustomGeneList.class);
                CustomGeneListData customGeneListData = customGeneList.getData();
                Set<String> users = customGeneListData.getUsers();
                updateUserList(operation, users);
                customGeneListData.setUsers(users);
                httpEntity = new HttpEntity<>(customGeneListData, sessionServiceRequestHandler.getHttpHeaders());                
             } else {
                String virtualStudyStr = sessionServiceObjectMapper.writeValueAsString(getSession(type, id).getBody());
                VirtualStudy virtualStudy = sessionServiceObjectMapper.readValue(virtualStudyStr, VirtualStudy.class);
                VirtualStudyData virtualStudyData = virtualStudy.getData();
                Set<String> users = virtualStudyData.getUsers();
                updateUserList(operation, users);
                virtualStudyData.setUsers(users);
                httpEntity = new HttpEntity<>(virtualStudyData, sessionServiceRequestHandler.getHttpHeaders());
            }

            RestTemplate restTemplate = new RestTemplate();
            restTemplate.put(sessionServiceURL + type + "/" + id, httpEntity);

            response.sendError(HttpStatus.OK.value());
        } else {
            response.sendError(HttpStatus.SERVICE_UNAVAILABLE.value());
        }

    }

    private void updateUserList(Operation operation, Set<String> users) {
        switch (operation) {
        case add: {
            users.add(userName());
            break;
        }
        case delete: {
            users.remove(userName());
            break;
        }
        }
    }

    @RequestMapping(value = "/groups/fetch", method = RequestMethod.POST)
    public ResponseEntity<List<VirtualStudy>> fetchUserGroups(
            @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE) @RequestBody List<String> studyIds) throws IOException {

        if (sessionServiceRequestHandler.isSessionServiceEnabled() && isAuthorized()) {
            // ignore origin studies order
            // add $size to make sure origin studies is not a subset
            List<BasicDBObject> basicDBObjects = new ArrayList<>();
            basicDBObjects
                .add(new BasicDBObject("data.users", Pattern.compile(userName(), Pattern.CASE_INSENSITIVE)));
            basicDBObjects.add(new BasicDBObject("data.origin",
                new BasicDBObject(QueryOperators.ALL, studyIds)));
            basicDBObjects.add(new BasicDBObject("data.origin",
                new BasicDBObject(QueryOperators.SIZE, studyIds.size())));

            BasicDBObject queryDBObject = new BasicDBObject(QueryOperators.AND, basicDBObjects);

            RestTemplate restTemplate = new RestTemplate();

            HttpEntity<String> httpEntity = new HttpEntity<>(queryDBObject.toString(), sessionServiceRequestHandler.getHttpHeaders());

            ResponseEntity<List<VirtualStudy>> responseEntity = restTemplate.exchange(
                    sessionServiceURL + SessionType.group + "/query/fetch",
                    HttpMethod.POST,
                    httpEntity,
                    new ParameterizedTypeReference<List<VirtualStudy>>() {});

            return new ResponseEntity<>(responseEntity.getBody(), HttpStatus.OK);

        }
        return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @RequestMapping(value = "/settings", method = RequestMethod.POST)
    public void updateUserPageSettings(@RequestBody PageSettingsData settingsData, HttpServletResponse response) {

        try {
            ObjectMapper objectMapper = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .setSerializationInclusion(Include.NON_NULL);
            if (sessionServiceRequestHandler.isSessionServiceEnabled() && isAuthorized()) {

                List<BasicDBObject> basicDBObjects = new ArrayList<>();
                basicDBObjects
                        .add(new BasicDBObject("data.owner", Pattern.compile(userName(), Pattern.CASE_INSENSITIVE)));
                basicDBObjects.add(new BasicDBObject("data.origin",
                        new BasicDBObject(QueryOperators.ALL, settingsData.getOrigin())));
                basicDBObjects.add(new BasicDBObject("data.origin",
                        new BasicDBObject(QueryOperators.SIZE, settingsData.getOrigin().size())));
                basicDBObjects.add(new BasicDBObject("data.page", settingsData.getPage().name()));

                BasicDBObject queryDBObject = new BasicDBObject(QueryOperators.AND, basicDBObjects);

                PageSettings pageSettings = getRecentlyUpdatePageSettings(queryDBObject.toString());

                JSONParser parser = new JSONParser();
                JSONObject jsonObject = (JSONObject) parser.parse(objectMapper.writeValueAsString(settingsData));

                if (pageSettings == null) {
                    addSession(SessionType.settings, Optional.empty(), jsonObject);
                } else {
                    updatedPageSettingSession(pageSettings, settingsData, response);
                }
                response.setStatus(HttpStatus.OK.value());
            } else {
                response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
            }
        } catch (IOException | ParseException e) {
            LOG.error("Error occurred", e);
            response.setStatus(HttpStatus.BAD_REQUEST.value());
        }
    }

    // updates only allowed for type page settings session 
    private void updatedPageSettingSession(
        PageSettings pageSettings, 
        @RequestBody PageSettingsData body,
        HttpServletResponse response
    ) throws IOException {

        if (isAuthorized()) {

            PageSettingsData pageSettingsData = pageSettings.getData();
            // only allow owner to update his session and see if the origin(studies) are same
            if (userName().equals(pageSettingsData.getOwner()) &&
                sameOrigin(pageSettingsData.getOrigin(), body.getOrigin())) {

                body.setCreated(pageSettingsData.getCreated());
                body.setOwner(pageSettingsData.getOwner());
                body.setOrigin(pageSettingsData.getOrigin());

                RestTemplate restTemplate = new RestTemplate();
                HttpEntity<Object> httpEntity = new HttpEntity<>(body, sessionServiceRequestHandler.getHttpHeaders());

                restTemplate.put(sessionServiceURL + pageSettings.getType() + "/" + pageSettings.getId(), httpEntity);
                response.setStatus(HttpStatus.OK.value());
            } else {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
            }
        } else {
            response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
        }
    }

    @RequestMapping(value = "/settings/fetch", method = RequestMethod.POST)
    public ResponseEntity<PageSettingsData> getPageSettings(@RequestBody PageSettingsIdentifier pageSettingsIdentifier) {

        try {
            if (sessionServiceRequestHandler.isSessionServiceEnabled() && isAuthorized()) {

                List<BasicDBObject> basicDBObjects = new ArrayList<>();
                basicDBObjects
                        .add(new BasicDBObject("data.owner", Pattern.compile(userName(), Pattern.CASE_INSENSITIVE)));
                basicDBObjects.add(new BasicDBObject("data.origin",
                        new BasicDBObject(QueryOperators.ALL, pageSettingsIdentifier.getOrigin())));
                basicDBObjects.add(new BasicDBObject("data.origin",
                        new BasicDBObject(QueryOperators.SIZE, pageSettingsIdentifier.getOrigin().size())));
                basicDBObjects.add(new BasicDBObject("data.page", pageSettingsIdentifier.getPage().name()));

                BasicDBObject queryDBObject = new BasicDBObject(QueryOperators.AND, basicDBObjects);

                PageSettings pageSettings = getRecentlyUpdatePageSettings(queryDBObject.toString());

                return new ResponseEntity<>(pageSettings == null ? null : pageSettings.getData(), HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
        } catch (Exception e) {
            LOG.error("Error occurred", e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/custom_data/fetch", method = RequestMethod.POST)
    public ResponseEntity<List<CustomDataSession>> fetchCustomProperties(
            @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE) @RequestBody List<String> studyIds) throws IOException {

        if (sessionServiceRequestHandler.isSessionServiceEnabled() && isAuthorized()) {

            List<BasicDBObject> basicDBObjects = new ArrayList<>();
            basicDBObjects.add(new BasicDBObject("data.users", Pattern.compile(userName(), Pattern.CASE_INSENSITIVE)));
            basicDBObjects.add(new BasicDBObject("data.origin", new BasicDBObject(QueryOperators.ALL, studyIds)));
            basicDBObjects.add(new BasicDBObject("data.origin", new BasicDBObject(QueryOperators.SIZE, studyIds.size())));

            BasicDBObject queryDBObject = new BasicDBObject(QueryOperators.AND, basicDBObjects);

            RestTemplate restTemplate = new RestTemplate();

            HttpEntity<String> httpEntity = new HttpEntity<>(queryDBObject.toString(),
                    sessionServiceRequestHandler.getHttpHeaders());

            ResponseEntity<List<CustomDataSession>> responseEntity = restTemplate.exchange(
                    sessionServiceURL + SessionType.custom_data + "/query/fetch",
                    HttpMethod.POST,
                    httpEntity,
                    new ParameterizedTypeReference<List<CustomDataSession>>() {});

            return new ResponseEntity<>(responseEntity.getBody(), HttpStatus.OK);

        }
        return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @RequestMapping(value = "/custom_gene_list/save", method = RequestMethod.POST)
    public ResponseEntity<Session> addUserSavedCustomGeneList(@RequestBody JSONObject body) throws IOException {
        return addSession(SessionType.custom_gene_list, Optional.of(SessionOperation.save), body);
    }
    
    @RequestMapping(value = "/custom_gene_list", method = RequestMethod.GET)
    public ResponseEntity<List<CustomGeneList>> fetchCustomGeneList() throws IOException {

        if (sessionServiceRequestHandler.isSessionServiceEnabled() && isAuthorized()) {

            BasicDBObject basicDBObject = new BasicDBObject();
            basicDBObject.put("data.users", Pattern.compile(userName(), Pattern.CASE_INSENSITIVE));

            RestTemplate restTemplate = new RestTemplate();

            HttpEntity<String> httpEntity = new HttpEntity<>(basicDBObject.toString(), sessionServiceRequestHandler.getHttpHeaders());
            
            ResponseEntity<List<CustomGeneList>> responseEntity = restTemplate.exchange(
                    sessionServiceURL + SessionType.custom_gene_list + "/query/fetch",
                    HttpMethod.POST,
                    httpEntity,
                    new ParameterizedTypeReference<List<CustomGeneList>>() {});

            return new ResponseEntity<>(responseEntity.getBody(), HttpStatus.OK);

        }
        return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
    }

}

enum Operation {
    add, delete;
}

enum SessionOperation {
    save, share;
}
