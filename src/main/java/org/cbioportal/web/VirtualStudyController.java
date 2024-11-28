package org.cbioportal.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import org.cbioportal.service.util.SessionServiceRequestHandler;
import org.cbioportal.utils.removeme.Session;
import org.cbioportal.web.parameter.*;
import org.cbioportal.web.util.StudyViewFilterApplier;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.cbioportal.web.PublicVirtualStudiesController.ALL_USERS;

/**
 * Controller responsible for handling virtual study-related endpoints.
 */
@RestController
@RequestMapping("/api/session")
public class VirtualStudyController {

    private SessionServiceRequestHandler sessionServiceRequestHandler;
    private ObjectMapper sessionServiceObjectMapper;
    private StudyViewFilterApplier studyViewFilterApplier;

    @Value("${session.service.url:}")
    private String sessionServiceURL;

    public VirtualStudyController(SessionServiceRequestHandler sessionServiceRequestHandler,
                                  ObjectMapper sessionServiceObjectMapper,
                                  StudyViewFilterApplier studyViewFilterApplier) {
        this.sessionServiceRequestHandler = sessionServiceRequestHandler;
        this.sessionServiceObjectMapper = sessionServiceObjectMapper;
        this.studyViewFilterApplier = studyViewFilterApplier;
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
     * Adds a new user-saved virtual study.
     */
    @RequestMapping(value = "/virtual_study/save", method = RequestMethod.POST)
    public ResponseEntity<Session> addUserSavedVirtualStudy(@RequestBody JSONObject body) throws IOException {
        return addSession(Session.SessionType.virtual_study, Optional.of(SessionOperation.save), body);
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
            if (type.equals(Session.SessionType.virtual_study)) {
                // Deserialize JSON to VirtualStudyData
                VirtualStudyData virtualStudyData = sessionServiceObjectMapper.readValue(body.toString(), VirtualStudyData.class);

                if (isAuthorized()) {
                    String userName = userName();
                    if (userName.equals(ALL_USERS)) {
                        throw new IllegalStateException("Illegal username " + ALL_USERS + " for assigning virtual studies.");
                    }
                    virtualStudyData.setOwner(userName);
                    if (operation.isPresent() && operation.get().equals(SessionOperation.save)) {
                        virtualStudyData.setUsers(Collections.singleton(userName));
                    }
                }

                httpEntity = new HttpEntity<>(virtualStudyData, sessionServiceRequestHandler.getHttpHeaders());
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
     * Retrieves all virtual studies for the logged-in user.
     */
    @RequestMapping(value = "/virtual_study", method = RequestMethod.GET)
    public ResponseEntity<List<VirtualStudy>> getUserStudies() {
        if (sessionServiceRequestHandler.isSessionServiceEnabled() && isAuthorized()) {
            try {
                BasicDBObject basicDBObject = new BasicDBObject();
                basicDBObject.put("data.users", Pattern.compile(userName(), Pattern.CASE_INSENSITIVE));

                RestTemplate restTemplate = new RestTemplate();

                HttpEntity<String> httpEntity = new HttpEntity<>(basicDBObject.toString(), sessionServiceRequestHandler.getHttpHeaders());

                ResponseEntity<List<VirtualStudy>> responseEntity = restTemplate.exchange(
                        sessionServiceURL + Session.SessionType.virtual_study + "/query/fetch",
                        HttpMethod.POST,
                        httpEntity,
                        new ParameterizedTypeReference<List<VirtualStudy>>() {});

                List<VirtualStudy> virtualStudyList = responseEntity.getBody();
                return new ResponseEntity<>(virtualStudyList, HttpStatus.OK);
            } catch (Exception exception) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Retrieves a specific virtual study by ID.
     */
    @RequestMapping(value = "/virtual_study/{id}", method = RequestMethod.GET)
    public ResponseEntity<Session> getSession(@PathVariable String id) {
        try {
            String sessionDataJson = sessionServiceRequestHandler.getSessionDataJson(Session.SessionType.virtual_study, id);
            VirtualStudy virtualStudy = sessionServiceObjectMapper.readValue(sessionDataJson, VirtualStudy.class);
            VirtualStudyData virtualStudyData = virtualStudy.getData();
            if (Boolean.TRUE.equals(virtualStudyData.getDynamic())) {
                populateVirtualStudySamples(virtualStudyData);
            }
            return new ResponseEntity<>(virtualStudy, HttpStatus.OK);
        } catch (Exception exception) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Updates users in a virtual study session.
     */
    @RequestMapping(value = "/virtual_study/{operation}/{id}", method = RequestMethod.GET)
    public void updateUsersInSession(@PathVariable String id,
                                     @PathVariable Operation operation, HttpServletResponse response) throws IOException {

        if (sessionServiceRequestHandler.isSessionServiceEnabled() && isAuthorized()) {
            HttpEntity<?> httpEntity;
            String virtualStudyStr = sessionServiceObjectMapper.writeValueAsString(getSession(id).getBody());
            VirtualStudy virtualStudy = sessionServiceObjectMapper.readValue(virtualStudyStr, VirtualStudy.class);
            VirtualStudyData virtualStudyData = virtualStudy.getData();
            Set<String> users = virtualStudyData.getUsers();
            updateUserList(operation, users);
            virtualStudyData.setUsers(users);
            httpEntity = new HttpEntity<>(virtualStudyData, sessionServiceRequestHandler.getHttpHeaders());

            RestTemplate restTemplate = new RestTemplate();
            restTemplate.put(sessionServiceURL + Session.SessionType.virtual_study + "/" + id, httpEntity);

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

    /**
     * Fetches user groups (virtual studies).
     */
    @RequestMapping(value = "/groups/fetch", method = RequestMethod.POST)
    public ResponseEntity<List<VirtualStudy>> fetchUserGroups(@RequestBody List<String> studyIds) throws IOException {

        if (sessionServiceRequestHandler.isSessionServiceEnabled() && isAuthorized()) {
            List<BasicDBObject> basicDBObjects = new ArrayList<>();
            basicDBObjects
                .add(new BasicDBObject("data.users", Pattern.compile(userName(), Pattern.CASE_INSENSITIVE)));
            basicDBObjects.add(new BasicDBObject("data.origin",
                new BasicDBObject("$all", studyIds)));
            basicDBObjects.add(new BasicDBObject("data.origin",
                new BasicDBObject("$size", studyIds.size())));

            BasicDBObject queryDBObject = new BasicDBObject("$and", basicDBObjects);

            RestTemplate restTemplate = new RestTemplate();

            HttpEntity<String> httpEntity = new HttpEntity<>(queryDBObject.toString(), sessionServiceRequestHandler.getHttpHeaders());

            ResponseEntity<List<VirtualStudy>> responseEntity = restTemplate.exchange(
                    sessionServiceURL + Session.SessionType.group + "/query/fetch",
                    HttpMethod.POST,
                    httpEntity,
                    new ParameterizedTypeReference<List<VirtualStudy>>() {});

            return new ResponseEntity<>(responseEntity.getBody(), HttpStatus.OK);

        }
        return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * Populates virtual study samples if the study is dynamic.
     */
    private void populateVirtualStudySamples(VirtualStudyData virtualStudyData) {
        List<SampleIdentifier> sampleIdentifiers = studyViewFilterApplier.apply(virtualStudyData.getStudyViewFilter());
        Set<VirtualStudySamples> virtualStudySamples = extractVirtualStudySamples(sampleIdentifiers);
        virtualStudyData.setStudies(virtualStudySamples);
    }

    /**
     * Transforms a list of sample identifiers into a set of virtual study samples.
     */
    private Set<VirtualStudySamples> extractVirtualStudySamples(List<SampleIdentifier> sampleIdentifiers) {
        Map<String, Set<String>> sampleIdsByStudyId = groupSampleIdsByStudyId(sampleIdentifiers);
        return sampleIdsByStudyId.entrySet().stream().map(entry -> {
            VirtualStudySamples vss = new VirtualStudySamples();
            vss.setId(entry.getKey());
            vss.setSamples(entry.getValue());
            return vss;
        }).collect(Collectors.toSet());
    }

    /**
     * Groups sample IDs by their study ID.
     */
    private Map<String, Set<String>> groupSampleIdsByStudyId(List<SampleIdentifier> sampleIdentifiers) {
        return sampleIdentifiers
            .stream()
            .collect(
                Collectors.groupingBy(
                    SampleIdentifier::getStudyId,
                    Collectors.mapping(SampleIdentifier::getSampleId, Collectors.toSet())));
    }

    // Enums for operations
    enum Operation {
        add, delete;
    }

    enum SessionOperation {
        save, share;
    }
}
