package org.cbioportal.legacy.web;

import static org.cbioportal.legacy.web.PublicVirtualStudiesController.ALL_USERS;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Size;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.cbioportal.legacy.service.VirtualStudyService;
import org.cbioportal.legacy.service.util.CustomAttributeWithData;
import org.cbioportal.legacy.service.util.CustomDataSession;
import org.cbioportal.legacy.service.util.SessionServiceRequestHandler;
import org.cbioportal.legacy.utils.removeme.Session;
import org.cbioportal.legacy.web.parameter.CustomGeneList;
import org.cbioportal.legacy.web.parameter.CustomGeneListData;
import org.cbioportal.legacy.web.parameter.PageSettings;
import org.cbioportal.legacy.web.parameter.PageSettingsData;
import org.cbioportal.legacy.web.parameter.PageSettingsIdentifier;
import org.cbioportal.legacy.web.parameter.PagingConstants;
import org.cbioportal.legacy.web.parameter.ResultsPageSettings;
import org.cbioportal.legacy.web.parameter.SessionPage;
import org.cbioportal.legacy.web.parameter.StudyPageSettings;
import org.cbioportal.legacy.web.parameter.VirtualStudy;
import org.cbioportal.legacy.web.parameter.VirtualStudyData;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.HttpClientErrorException;

@Controller
@RequestMapping("/api/session")
public class SessionServiceController {

  private static final Logger LOG = LoggerFactory.getLogger(SessionServiceController.class);

  SessionServiceRequestHandler sessionServiceRequestHandler;

  private ObjectMapper sessionServiceObjectMapper;
  private final VirtualStudyService virtualStudyService;

  public SessionServiceController(
      SessionServiceRequestHandler sessionServiceRequestHandler,
      ObjectMapper sessionServiceObjectMapper,
      VirtualStudyService virtualStudyService) {
    this.sessionServiceRequestHandler = sessionServiceRequestHandler;
    this.sessionServiceObjectMapper = sessionServiceObjectMapper;
    this.virtualStudyService = virtualStudyService;
  }

  private static Map<SessionPage, Class<? extends PageSettingsData>> pageToSettingsDataClass =
      ImmutableMap.of(
          SessionPage.study_view, StudyPageSettings.class,
          SessionPage.results_view, ResultsPageSettings.class);

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

  private ResponseEntity<Session> addSession(
      Session.SessionType type, Optional<SessionOperation> operation, JSONObject body) {
    try {
      Serializable payload;
      if (type.equals(Session.SessionType.virtual_study)
          || type.equals(Session.SessionType.group)) {
        // JSON from file to Object
        VirtualStudyData virtualStudyData =
            sessionServiceObjectMapper.readValue(body.toString(), VirtualStudyData.class);
        // TODO sanitize what's supplied. e.g. anonymous user should not specify the users field!

        if (isAuthorized()) {
          String userName = userName();
          if (userName.equals(ALL_USERS)) {
            throw new IllegalStateException(
                "Illegal username " + ALL_USERS + " for assigning virtual studies.");
          }
          virtualStudyData.setOwner(userName);
          if ((operation.isPresent() && operation.get().equals(SessionOperation.save))
              || type.equals(Session.SessionType.group)) {
            virtualStudyData.setUsers(Collections.singleton(userName));
          }
        }

        // use basic authentication for session service if set
        payload = virtualStudyData;
      } else if (type.equals(Session.SessionType.settings)) {
        if (!(isAuthorized())) {
          return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
        }
        Class<? extends PageSettingsData> pageDataClass =
            pageToSettingsDataClass.get(SessionPage.valueOf((String) body.get("page")));
        PageSettingsData pageSettings =
            sessionServiceObjectMapper.readValue(body.toString(), pageDataClass);
        pageSettings.setOwner(userName());
        payload = pageSettings;

      } else if (type.equals(Session.SessionType.custom_data)) {
        // JSON from file to Object
        CustomAttributeWithData customData =
            sessionServiceObjectMapper.readValue(body.toString(), CustomAttributeWithData.class);

        if (isAuthorized()) {
          customData.setOwner(userName());
          customData.setUsers(Collections.singleton(userName()));
        }

        // use basic authentication for session service if set
        payload = customData;
      } else if (type.equals(Session.SessionType.custom_gene_list)) {
        if (!(isAuthorized())) {
          return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
        }
        CustomGeneListData customGeneListData =
            sessionServiceObjectMapper.readValue(body.toString(), CustomGeneListData.class);
        customGeneListData.setUsers(Collections.singleton(userName()));
        payload = customGeneListData;
      } else {
        payload = body;
      }
      // returns {"id":"5799648eef86c0e807a2e965"}
      // using HashMap because converter is MappingJackson2HttpMessageConverter
      // (Jackson 2 is on classpath)
      // was String when default converter StringHttpMessageConverter was used
      return sessionServiceRequestHandler.createSession(type, payload);

    } catch (IOException e) {
      LOG.error("Error occurred", e);
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
  }

  @RequestMapping(value = "/{type}/{id}", method = RequestMethod.GET)
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content = @Content(schema = @Schema(implementation = Session.class)))
  public ResponseEntity<Session> getSession(
      @PathVariable Session.SessionType type, @PathVariable String id) {

    try {
      String sessionDataJson = sessionServiceRequestHandler.getSessionDataJson(type, id);
      Session session;
      switch (type) {
        case virtual_study:
          session = virtualStudyService.getVirtualStudy(id);
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
    } catch (HttpClientErrorException.NotFound exception) {
      LOG.error("Session not found", exception);
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } catch (Exception exception) {
      LOG.error("Error occurred", exception);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @RequestMapping(value = "/virtual_study", method = RequestMethod.GET)
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content =
          @Content(array = @ArraySchema(schema = @Schema(implementation = VirtualStudy.class))))
  public ResponseEntity<List<VirtualStudy>> getUserStudies() throws JsonProcessingException {

    if (sessionServiceRequestHandler.isSessionServiceEnabled() && isAuthorized()) {
      try {
        List<VirtualStudy> virtualStudyList = virtualStudyService.getUserVirtualStudies(userName());
        return new ResponseEntity<>(virtualStudyList, HttpStatus.OK);
      } catch (Exception exception) {
        LOG.error("Error occurred", exception);
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
      }
    }
    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @RequestMapping(value = "/{type}", method = RequestMethod.POST)
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content = @Content(schema = @Schema(implementation = Session.class)))
  public ResponseEntity<Session> addSession(
      @PathVariable Session.SessionType type, @RequestBody JSONObject body) throws IOException {
    // FIXME? anonymous user can create sessions. Do we really want that?
    // https://github.com/cBioPortal/cbioportal/issues/10843
    return addSession(type, Optional.empty(), body);
  }

  @RequestMapping(value = "/virtual_study/save", method = RequestMethod.POST)
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content = @Content(schema = @Schema(implementation = Session.class)))
  public ResponseEntity<Session> addUserSavedVirtualStudy(@RequestBody JSONObject body)
      throws IOException {
    // FIXME? anonymous user can create virtual studies. Do we really want that?
    // https://github.com/cBioPortal/cbioportal/issues/10843
    return addSession(Session.SessionType.virtual_study, Optional.of(SessionOperation.save), body);
  }

  @RequestMapping(
      value = "/{type:virtual_study|group|custom_data|custom_gene_list}/{operation}/{id}",
      method = RequestMethod.GET)
  public void updateUsersInSession(
      @PathVariable Session.SessionType type,
      @PathVariable String id,
      @PathVariable Operation operation,
      HttpServletResponse response)
      throws IOException {

    if (sessionServiceRequestHandler.isSessionServiceEnabled() && isAuthorized()) {
      Serializable payload;
      if (type.equals(Session.SessionType.custom_data)) {
        String virtualStudyStr =
            sessionServiceObjectMapper.writeValueAsString(getSession(type, id).getBody());
        CustomDataSession customDataSession =
            sessionServiceObjectMapper.readValue(virtualStudyStr, CustomDataSession.class);
        CustomAttributeWithData customAttributeWithData = customDataSession.getData();
        Set<String> users = customAttributeWithData.getUsers();
        updateUserList(operation, users);
        customAttributeWithData.setUsers(users);
        payload = customAttributeWithData;
      } else if (type.equals(Session.SessionType.custom_gene_list)) {
        String customGeneListStr =
            sessionServiceObjectMapper.writeValueAsString(getSession(type, id).getBody());
        CustomGeneList customGeneList =
            sessionServiceObjectMapper.readValue(customGeneListStr, CustomGeneList.class);
        CustomGeneListData customGeneListData = customGeneList.getData();
        Set<String> users = customGeneListData.getUsers();
        updateUserList(operation, users);
        customGeneListData.setUsers(users);
        payload = customGeneListData;
      } else {
        String virtualStudyStr =
            sessionServiceObjectMapper.writeValueAsString(getSession(type, id).getBody());
        VirtualStudy virtualStudy =
            sessionServiceObjectMapper.readValue(virtualStudyStr, VirtualStudy.class);
        VirtualStudyData virtualStudyData = virtualStudy.getData();
        Set<String> users = virtualStudyData.getUsers();
        updateUserList(operation, users);
        virtualStudyData.setUsers(users);
        payload = virtualStudyData;
      }

      sessionServiceRequestHandler.updateUsers(type, id, payload);

      response.sendError(HttpStatus.OK.value());
    } else {
      response.sendError(HttpStatus.SERVICE_UNAVAILABLE.value());
    }
  }

  private void updateUserList(Operation operation, Set<String> users) {
    switch (operation) {
      case add:
        {
          users.add(userName());
          break;
        }
      case delete:
        {
          users.remove(userName());
          break;
        }
    }
  }

  @RequestMapping(value = "/groups/fetch", method = RequestMethod.POST)
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content =
          @Content(array = @ArraySchema(schema = @Schema(implementation = VirtualStudy.class))))
  public ResponseEntity<List<VirtualStudy>> fetchUserGroups(
      @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE) @RequestBody List<String> studyIds)
      throws IOException {

    if (sessionServiceRequestHandler.isSessionServiceEnabled() && isAuthorized()) {
      List<VirtualStudy> virtualStudyList =
          sessionServiceRequestHandler.getVirtualStudiesForUser(userName(), studyIds);
      return new ResponseEntity<>(virtualStudyList, HttpStatus.OK);
    }
    return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
  }

  @RequestMapping(value = "/settings", method = RequestMethod.POST)
  public void updateUserPageSettings(
      @RequestBody PageSettingsData settingsData, HttpServletResponse response) {

    try {
      ObjectMapper objectMapper =
          new ObjectMapper()
              .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
              .setSerializationInclusion(Include.NON_NULL);
      if (sessionServiceRequestHandler.isSessionServiceEnabled() && isAuthorized()) {
        PageSettings pageSettings =
            sessionServiceRequestHandler.getRecentlyUpdatePageSettings(
                userName(), settingsData.getOrigin(), settingsData.getPage().name());
        JSONParser parser = new JSONParser();
        JSONObject jsonObject =
            (JSONObject) parser.parse(objectMapper.writeValueAsString(settingsData));

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
      LOG.error("Error occurred", e);
      response.setStatus(HttpStatus.BAD_REQUEST.value());
    }
  }

  // updates only allowed for type page settings session
  private void updatedPageSettingSession(
      PageSettings pageSettings, @RequestBody PageSettingsData body, HttpServletResponse response)
      throws IOException {

    if (isAuthorized()) {

      PageSettingsData pageSettingsData = pageSettings.getData();
      // only allow owner to update his session and see if the origin(studies) are same
      if (userName().equals(pageSettingsData.getOwner())
          && sameOrigin(pageSettingsData.getOrigin(), body.getOrigin())) {

        body.setCreated(pageSettingsData.getCreated());
        body.setOwner(pageSettingsData.getOwner());
        body.setOrigin(pageSettingsData.getOrigin());

        Session.SessionType type =
            pageSettings.getType() == null ? Session.SessionType.settings : pageSettings.getType();
        sessionServiceRequestHandler.updatePageSettings(type, pageSettings.getId(), body);
        response.setStatus(HttpStatus.OK.value());
      } else {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
      }
    } else {
      response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
    }
  }

  @RequestMapping(value = "/settings/fetch", method = RequestMethod.POST)
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content = @Content(schema = @Schema(implementation = PageSettingsData.class)))
  public ResponseEntity<PageSettingsData> getPageSettings(
      @RequestBody PageSettingsIdentifier pageSettingsIdentifier) {

    try {
      if (sessionServiceRequestHandler.isSessionServiceEnabled() && isAuthorized()) {
        PageSettings pageSettings =
            sessionServiceRequestHandler.getRecentlyUpdatePageSettings(
                userName(),
                pageSettingsIdentifier.getOrigin(),
                pageSettingsIdentifier.getPage().name());
        return new ResponseEntity<>(
            pageSettings == null ? null : pageSettings.getData(), HttpStatus.OK);
      }
      return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
    } catch (Exception e) {
      LOG.error("Error occurred", e);
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
  }

  @RequestMapping(value = "/custom_data/fetch", method = RequestMethod.POST)
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content =
          @Content(
              array = @ArraySchema(schema = @Schema(implementation = CustomDataSession.class))))
  public ResponseEntity<List<CustomDataSession>> fetchCustomProperties(
      @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE) @RequestBody List<String> studyIds)
      throws IOException {

    if (sessionServiceRequestHandler.isSessionServiceEnabled() && isAuthorized()) {
      List<CustomDataSession> customDataSessionList =
          sessionServiceRequestHandler.getCustomDataSessionForUser(userName(), studyIds);
      return new ResponseEntity<>(customDataSessionList, HttpStatus.OK);
    }
    return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
  }

  @RequestMapping(value = "/custom_gene_list/save", method = RequestMethod.POST)
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content = @Content(schema = @Schema(implementation = Session.class)))
  public ResponseEntity<Session> addUserSavedCustomGeneList(@RequestBody JSONObject body)
      throws IOException {
    return addSession(
        Session.SessionType.custom_gene_list, Optional.of(SessionOperation.save), body);
  }

  @RequestMapping(value = "/custom_gene_list", method = RequestMethod.GET)
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content =
          @Content(array = @ArraySchema(schema = @Schema(implementation = CustomGeneList.class))))
  public ResponseEntity<List<CustomGeneList>> fetchCustomGeneList() throws IOException {

    if (sessionServiceRequestHandler.isSessionServiceEnabled() && isAuthorized()) {
      List<CustomGeneList> customGeneLists =
          sessionServiceRequestHandler.getCustomGeneListsForUser(userName());
      return new ResponseEntity<>(customGeneLists, HttpStatus.OK);
    }
    return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
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
