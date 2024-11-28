package org.cbioportal.web;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;

import org.cbioportal.service.util.SessionServiceRequestHandler;
import org.cbioportal.utils.removeme.Session;
import org.cbioportal.web.parameter.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestTemplate;
import org.cbioportal.web.util.StudyViewFilterApplier;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableMap;

/**
 * Controller responsible for handling general session and settings-related endpoints.
 */
@RestController
@RequestMapping("/api/session")
public class SessionServiceController {

    private SessionServiceRequestHandler sessionServiceRequestHandler;
    private ObjectMapper sessionServiceObjectMapper;
    private StudyViewFilterApplier studyViewFilterApplier;

    @Value("${session.service.url:}")
    private String sessionServiceURL;

    private static Map<SessionPage, Class<? extends PageSettingsData>> pageToSettingsDataClass = ImmutableMap.of(
         SessionPage.study_view, StudyPageSettings.class,
         SessionPage.results_view, ResultsPageSettings.class
     );

    public SessionServiceController(SessionServiceRequestHandler sessionServiceRequestHandler,
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

        HttpEntity<String> httpEntity = new HttpEntity<>(query, sessionServiceRequestHandler.getHttpHeaders());

        ResponseEntity<List<PageSettings>> responseEntity = restTemplate.exchange(
                sessionServiceURL + Session.SessionType.settings + "/query/fetch",
                HttpMethod.POST,
                httpEntity,
                new ParameterizedTypeReference<List<PageSettings>>() {});

        List<PageSettings> sessions = responseEntity.getBody();

        // Sort by last updated in descending order
        sessions.sort((PageSettings s1, PageSettings s2) -> s1.getData().getLastUpdated() > s2.getData()
                .getLastUpdated() ? -1 : 1);

        return sessions.isEmpty() ? null : sessions.get(0);
    }

    /**
     * Adds a new session of a specified type.
     */
    @RequestMapping(value = "/{type}", method = RequestMethod.POST)
    public ResponseEntity<Session> addSession(@PathVariable Session.SessionType type, @RequestBody JSONObject body)
            throws IOException {
        return addSession(type, Optional.empty(), body);
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
            if (type.equals(Session.SessionType.settings)) {
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
            } else {
                httpEntity = new HttpEntity<>(body, sessionServiceRequestHandler.getHttpHeaders());
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
     * Updates user page settings.
     */
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
                        new BasicDBObject("$all", settingsData.getOrigin())));
                basicDBObjects.add(new BasicDBObject("data.origin",
                        new BasicDBObject("$size", settingsData.getOrigin().size())));
                basicDBObjects.add(new BasicDBObject("data.page", settingsData.getPage().name()));

                BasicDBObject queryDBObject = new BasicDBObject("$and", basicDBObjects);

                PageSettings pageSettings = getRecentlyUpdatePageSettings(queryDBObject.toString());

                JSONParser parser = new JSONParser();
                JSONObject jsonObject = (JSONObject) parser.parse(objectMapper.writeValueAsString(settingsData));

                if (pageSettings == null) {
                    addSession(Session.SessionType.settings, Optional.empty(), jsonObject);
                } else {
                    updatedPageSettingSession(pageSettings, settingsData, response);
                }
                response.setStatus(HttpStatus.OK.value());
            } else {
                response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
            }
        } catch (IOException | ParseException e) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
        }
    }

    /**
     * Updates an existing page setting session.
     */
    private void updatedPageSettingSession(
        PageSettings pageSettings,
        @RequestBody PageSettingsData body,
        HttpServletResponse response
    ) throws IOException {

        if (isAuthorized()) {

            PageSettingsData pageSettingsData = pageSettings.getData();
            // Only allow owner to update their session and check if the origin (studies) are the same
            if (userName().equals(pageSettingsData.getOwner()) &&
                sameOrigin(pageSettingsData.getOrigin(), body.getOrigin())) {

                body.setCreated(pageSettingsData.getCreated());
                body.setOwner(pageSettingsData.getOwner());
                body.setOrigin(pageSettingsData.getOrigin());

                RestTemplate restTemplate = new RestTemplate();
                HttpEntity<Object> httpEntity = new HttpEntity<>(body, sessionServiceRequestHandler.getHttpHeaders());

                Session.SessionType type = pageSettings.getType() == null ? Session.SessionType.settings : pageSettings.getType();
                restTemplate.put(sessionServiceURL + type + "/" + pageSettings.getId(), httpEntity);
                response.setStatus(HttpStatus.OK.value());
            } else {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
            }
        } else {
            response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
        }
    }

    /**
     * Retrieves page settings for the user.
     */
    @RequestMapping(value = "/settings/fetch", method = RequestMethod.POST)
    public ResponseEntity<PageSettingsData> getPageSettings(@RequestBody PageSettingsIdentifier pageSettingsIdentifier) {

        try {
            if (sessionServiceRequestHandler.isSessionServiceEnabled() && isAuthorized()) {

                List<BasicDBObject> basicDBObjects = new ArrayList<>();
                basicDBObjects
                    .add(new BasicDBObject("data.owner", Pattern.compile(userName(), Pattern.CASE_INSENSITIVE)));
                basicDBObjects.add(new BasicDBObject("data.origin",
                        new BasicDBObject("$all", pageSettingsIdentifier.getOrigin())));
                basicDBObjects.add(new BasicDBObject("data.origin",
                        new BasicDBObject("$size", pageSettingsIdentifier.getOrigin().size())));
                basicDBObjects.add(new BasicDBObject("data.page", pageSettingsIdentifier.getPage().name()));

                BasicDBObject queryDBObject = new BasicDBObject("$and", basicDBObjects);

                PageSettings pageSettings = getRecentlyUpdatePageSettings(queryDBObject.toString());

                return new ResponseEntity<>(pageSettings == null ? null : pageSettings.getData(), HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // Enum for session operations
    enum SessionOperation {
        save, share;
    }
}
