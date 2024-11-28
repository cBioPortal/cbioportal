package org.cbioportal.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import org.cbioportal.service.util.SessionServiceRequestHandler;
import org.cbioportal.utils.removeme.Session;
import org.cbioportal.web.parameter.*;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestTemplate;
import org.cbioportal.service.util.CustomDataSession; 
import org.cbioportal.service.util.CustomAttributeWithData;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Controller responsible for handling custom data and custom gene list endpoints.
 */
@RestController
@RequestMapping("/api/session")
public class CustomDataController {

    private SessionServiceRequestHandler sessionServiceRequestHandler;
    private ObjectMapper sessionServiceObjectMapper;

    @Value("${session.service.url:}")
    private String sessionServiceURL;

    public CustomDataController(SessionServiceRequestHandler sessionServiceRequestHandler,
                                ObjectMapper sessionServiceObjectMapper) {
        this.sessionServiceRequestHandler = sessionServiceRequestHandler;
        this.sessionServiceObjectMapper = sessionServiceObjectMapper;
    }

    // Helper methods for authentication
    private boolean isAuthorized() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return !(authentication == null || (authentication instanceof AnonymousAuthenticationToken));
    }

    private String userName() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    /**
     * Adds a new custom gene list.
     */
    @RequestMapping(value = "/custom_gene_list/save", method = RequestMethod.POST)
    public ResponseEntity<Session> addUserSavedCustomGeneList(@RequestBody JSONObject body) throws IOException {
        return addSession(Session.SessionType.custom_gene_list, Optional.of(SessionOperation.save), body);
    }

    /**
     * Internal method to add a session.
     */
    private ResponseEntity<Session> addSession(
        Session.SessionType type,
        Optional<SessionOperation> operation,
        JSONObject body
    ) {
        try {
            HttpEntity<?> httpEntity;
            if (type.equals(Session.SessionType.custom_gene_list)) {
                if (!(isAuthorized())) {
                    return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
                }
                CustomGeneListData customGeneListData = sessionServiceObjectMapper.readValue(body.toString(), CustomGeneListData.class);
                customGeneListData.setUsers(Collections.singleton(userName()));
                httpEntity = new HttpEntity<>(customGeneListData, sessionServiceRequestHandler.getHttpHeaders());
            } else {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Session> resp = restTemplate.exchange(sessionServiceURL + type, HttpMethod.POST, httpEntity,
                    Session.class);

            return new ResponseEntity<>(resp.getBody(), resp.getStatusCode());

        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Fetches custom gene lists for the user.
     */
    @RequestMapping(value = "/custom_gene_list", method = RequestMethod.GET)
    public ResponseEntity<List<CustomGeneList>> fetchCustomGeneList() throws IOException {

        if (sessionServiceRequestHandler.isSessionServiceEnabled() && isAuthorized()) {

            BasicDBObject basicDBObject = new BasicDBObject();
            basicDBObject.put("data.users", Pattern.compile(userName(), Pattern.CASE_INSENSITIVE));

            RestTemplate restTemplate = new RestTemplate();

            HttpEntity<String> httpEntity = new HttpEntity<>(basicDBObject.toString(), sessionServiceRequestHandler.getHttpHeaders());

            ResponseEntity<List<CustomGeneList>> responseEntity = restTemplate.exchange(
                    sessionServiceURL + Session.SessionType.custom_gene_list + "/query/fetch",
                    HttpMethod.POST,
                    httpEntity,
                    new ParameterizedTypeReference<List<CustomGeneList>>() {});

            return new ResponseEntity<>(responseEntity.getBody(), HttpStatus.OK);

        }
        return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * Fetches custom properties for the user.
     */
    @RequestMapping(value = "/custom_data/fetch", method = RequestMethod.POST)
    public ResponseEntity<List<CustomDataSession>> fetchCustomProperties(@RequestBody List<String> studyIds) throws IOException {

        if (sessionServiceRequestHandler.isSessionServiceEnabled() && isAuthorized()) {

            List<BasicDBObject> basicDBObjects = new ArrayList<>();
            basicDBObjects.add(new BasicDBObject("data.users", Pattern.compile(userName(), Pattern.CASE_INSENSITIVE)));
            basicDBObjects.add(new BasicDBObject("data.origin", new BasicDBObject("$all", studyIds)));
            basicDBObjects.add(new BasicDBObject("data.origin", new BasicDBObject("$size", studyIds.size())));

            BasicDBObject queryDBObject = new BasicDBObject("$and", basicDBObjects);

            RestTemplate restTemplate = new RestTemplate();

            HttpEntity<String> httpEntity = new HttpEntity<>(queryDBObject.toString(),
                    sessionServiceRequestHandler.getHttpHeaders());

            ResponseEntity<List<CustomDataSession>> responseEntity = restTemplate.exchange(
                    sessionServiceURL + Session.SessionType.custom_data + "/query/fetch",
                    HttpMethod.POST,
                    httpEntity,
                    new ParameterizedTypeReference<List<CustomDataSession>>() {});

            return new ResponseEntity<>(responseEntity.getBody(), HttpStatus.OK);

        }
        return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * Updates users in custom data or custom gene list session.
     */
    @RequestMapping(value = "/{type:custom_data|custom_gene_list}/{operation}/{id}", method = RequestMethod.GET)
    public void updateUsersInSession(@PathVariable Session.SessionType type, @PathVariable String id,
                                     @PathVariable Operation operation, HttpServletResponse response) throws IOException, Exception {

        if (sessionServiceRequestHandler.isSessionServiceEnabled() && isAuthorized()) {
            HttpEntity<?> httpEntity;
            if (type.equals(Session.SessionType.custom_data)) {
                String sessionDataStr = sessionServiceRequestHandler.getSessionDataJson(type, id);
                CustomDataSession customDataSession = sessionServiceObjectMapper.readValue(sessionDataStr, CustomDataSession.class);
                CustomAttributeWithData customAttributeWithData = customDataSession.getData();
                Set<String> users = customAttributeWithData.getUsers();
                updateUserList(operation, users);
                customAttributeWithData.setUsers(users);
                httpEntity = new HttpEntity<>(customAttributeWithData, sessionServiceRequestHandler.getHttpHeaders());
            } else if (type.equals(Session.SessionType.custom_gene_list)) {
                String customGeneListStr = sessionServiceRequestHandler.getSessionDataJson(type, id);
                CustomGeneList customGeneList = sessionServiceObjectMapper.readValue(customGeneListStr, CustomGeneList.class);
                CustomGeneListData customGeneListData = customGeneList.getData();
                Set<String> users = customGeneListData.getUsers();
                updateUserList(operation, users);
                customGeneListData.setUsers(users);
                httpEntity = new HttpEntity<>(customGeneListData, sessionServiceRequestHandler.getHttpHeaders());
            } else {
                response.sendError(HttpStatus.BAD_REQUEST.value());
                return;
            }

            RestTemplate restTemplate = new RestTemplate();
            restTemplate.put(sessionServiceURL + type + "/" + id, httpEntity);

            response.sendError(HttpStatus.OK.value());
        } else {
            response.sendError(HttpStatus.SERVICE_UNAVAILABLE.value());
        }

    }

    /**
     * Helper method to update user list based on the operation.
     */
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

    // Enums for operations
    enum Operation {
        add, delete;
    }

    enum SessionOperation {
        save, share;
    }
}
