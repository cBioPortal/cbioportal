package org.cbioportal.legacy.service.util;

import static org.cbioportal.legacy.utils.removeme.Session.*;

import com.mongodb.BasicDBObject;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.cbioportal.legacy.utils.removeme.Session;
import org.cbioportal.legacy.web.parameter.CustomGeneList;
import org.cbioportal.legacy.web.parameter.PageSettings;
import org.cbioportal.legacy.web.parameter.PageSettingsData;
import org.cbioportal.legacy.web.parameter.VirtualStudy;
import org.cbioportal.legacy.web.parameter.VirtualStudyData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class SessionServiceRequestHandler {

  private static final Logger LOG = LoggerFactory.getLogger(SessionServiceRequestHandler.class);

  private static final String QUERY_OPERATOR_ALL = "$all";
  private static final String QUERY_OPERATOR_SIZE = "$size";
  private static final String QUERY_OPERATOR_AND = "$and";

  @Value("${session.service.url:}")
  private String sessionServiceURL;

  @Value("${session.service.user:}")
  private String sessionServiceUser;

  @Value("${session.service.password:}")
  private String sessionServicePassword;

  private Boolean isBasicAuthEnabled() {
    return isSessionServiceEnabled()
        && sessionServicePassword != null
        && !sessionServicePassword.equals("");
  }

  public Boolean isSessionServiceEnabled() {
    return !StringUtils.isEmpty(sessionServiceURL);
  }

  public HttpHeaders getHttpHeaders() {

    return new HttpHeaders() {
      {
        if (isBasicAuthEnabled()) {
          String auth = sessionServiceUser + ":" + sessionServicePassword;
          byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
          String authHeader = "Basic " + new String(encodedAuth);
          set("Authorization", authHeader);
        }
        set("Content-Type", "application/json");
      }
    };
  }

  public String getSessionDataJson(SessionType type, String id) throws Exception {

    RestTemplate restTemplate = new RestTemplate();

    String url =
        UriComponentsBuilder.fromUriString(sessionServiceURL)
            .pathSegment(type.name())
            .pathSegment(id)
            .build()
            .toUriString();

    // add basic authentication in header
    HttpEntity<String> headers = new HttpEntity<>(getHttpHeaders());
    ResponseEntity<String> responseEntity =
        restTemplate.exchange(url, HttpMethod.GET, headers, String.class);

    return responseEntity.getBody();
  }

  /**
   * Gets virtual study by id
   *
   * @param id - id of the virtual study to read
   * @return virtual study
   */
  public VirtualStudy getVirtualStudyById(String id) {

    String url =
        UriComponentsBuilder.fromUriString(sessionServiceURL)
            .pathSegment("virtual_study")
            .pathSegment(id)
            .build()
            .toUriString();

    ResponseEntity<VirtualStudy> responseEntity =
        new RestTemplate()
            .exchange(url, HttpMethod.GET, new HttpEntity<>(getHttpHeaders()), VirtualStudy.class);
    HttpStatusCode statusCode = responseEntity.getStatusCode();
    VirtualStudy virtualStudy = responseEntity.getBody();
    if (!statusCode.is2xxSuccessful() || virtualStudy == null) {
      LOG.error(
          "The downstream server replied with statusCode={} and body={}."
              + " Replying with the same status code to the client.",
          statusCode,
          virtualStudy);
      throw new IllegalStateException("The downstream server response is not successful");
    }
    return responseEntity.getBody();
  }

  /**
   * Get list of virtual studies accessible to user
   *
   * @param username - user for whom get list of virtual studies
   * @return - list of virtual studies
   */
  public List<VirtualStudy> getVirtualStudiesAccessibleToUser(String username) {
    BasicDBObject basicDBObject = new BasicDBObject();
    basicDBObject.put(
        "data.users", Pattern.compile(Pattern.quote(username), Pattern.CASE_INSENSITIVE));

    String url =
        UriComponentsBuilder.fromUriString(sessionServiceURL)
            .pathSegment("virtual_study")
            .pathSegment("query")
            .pathSegment("fetch")
            .build()
            .toUriString();

    ResponseEntity<List<VirtualStudy>> responseEntity =
        new RestTemplate()
            .exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(basicDBObject.toString(), getHttpHeaders()),
                new ParameterizedTypeReference<>() {});

    return responseEntity.getBody();
  }

  /**
   * Creates a virtual study out of virtual study definition (aka virtualStudyData)
   *
   * @param virtualStudyData - definition of virtual study
   * @return virtual study object with id and the virtualStudyData
   */
  public VirtualStudy createVirtualStudy(VirtualStudyData virtualStudyData) {

    String url =
        UriComponentsBuilder.fromUriString(sessionServiceURL)
            .pathSegment("virtual_study")
            .build()
            .toUriString();

    ResponseEntity<VirtualStudy> responseEntity =
        new RestTemplate()
            .exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(virtualStudyData, getHttpHeaders()),
                new ParameterizedTypeReference<>() {});

    return responseEntity.getBody();
  }

  /**
   * Soft delete of the virtual study by de-associating all assigned users.
   *
   * @param id - id of virtual study to soft delete
   */
  public void softRemoveVirtualStudy(String id) {
    VirtualStudy virtualStudy = getVirtualStudyById(id);
    VirtualStudyData data = virtualStudy.getData();
    data.setUsers(Collections.emptySet());
    updateVirtualStudy(virtualStudy);
  }

  /**
   * Updates virtual study
   *
   * @param virtualStudy - virtual study to update
   */
  public void updateVirtualStudy(VirtualStudy virtualStudy) {

    String url =
        UriComponentsBuilder.fromUriString(sessionServiceURL)
            .pathSegment("virtual_study")
            .pathSegment(virtualStudy.getId())
            .build()
            .toUriString();

    new RestTemplate().put(url, new HttpEntity<>(virtualStudy.getData(), getHttpHeaders()));
  }

  private List<PageSettings> getPageSettingsForUser(
      String username, Set<String> origin, String sessionPageName) {

    List<BasicDBObject> basicDBObjects = new ArrayList<>();
    basicDBObjects.add(
        new BasicDBObject(
            "data.owner", Pattern.compile(Pattern.quote(username), Pattern.CASE_INSENSITIVE)));
    basicDBObjects.add(
        new BasicDBObject("data.origin", new BasicDBObject(QUERY_OPERATOR_ALL, origin)));
    basicDBObjects.add(
        new BasicDBObject("data.origin", new BasicDBObject(QUERY_OPERATOR_SIZE, origin.size())));
    basicDBObjects.add(new BasicDBObject("data.page", sessionPageName));

    BasicDBObject queryDBObject = new BasicDBObject(QUERY_OPERATOR_AND, basicDBObjects);

    String url =
        UriComponentsBuilder.fromUriString(sessionServiceURL)
            .pathSegment(Session.SessionType.settings.name())
            .pathSegment("query")
            .pathSegment("fetch")
            .build()
            .toUriString();

    ResponseEntity<List<PageSettings>> responseEntity =
        new RestTemplate()
            .exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<String>(queryDBObject.toString(), getHttpHeaders()),
                new ParameterizedTypeReference<List<PageSettings>>() {});

    return responseEntity.getBody();
  }

  public List<VirtualStudy> getVirtualStudiesForUser(String username, List<String> studyIds) {
    // ignore origin studies order
    // add $size to make sure origin studies is not a subset
    List<BasicDBObject> basicDBObjects = new ArrayList<>();
    basicDBObjects.add(
        new BasicDBObject(
            "data.users", Pattern.compile(Pattern.quote(username), Pattern.CASE_INSENSITIVE)));
    basicDBObjects.add(
        new BasicDBObject("data.origin", new BasicDBObject(QUERY_OPERATOR_ALL, studyIds)));
    basicDBObjects.add(
        new BasicDBObject("data.origin", new BasicDBObject(QUERY_OPERATOR_SIZE, studyIds.size())));

    BasicDBObject queryDBObject = new BasicDBObject(QUERY_OPERATOR_AND, basicDBObjects);

    RestTemplate restTemplate = new RestTemplate();

    HttpEntity<String> httpEntity = new HttpEntity<>(queryDBObject.toString(), getHttpHeaders());

    String url =
        UriComponentsBuilder.fromUriString(sessionServiceURL)
            .pathSegment(Session.SessionType.group.name())
            .pathSegment("query")
            .pathSegment("fetch")
            .build()
            .toUriString();

    ResponseEntity<List<VirtualStudy>> responseEntity =
        restTemplate.exchange(
            url,
            HttpMethod.POST,
            httpEntity,
            new ParameterizedTypeReference<List<VirtualStudy>>() {});

    return responseEntity.getBody();
  }

  public List<CustomGeneList> getCustomGeneListsForUser(String username) {
    BasicDBObject basicDBObject = new BasicDBObject();
    basicDBObject.put(
        "data.users", Pattern.compile(Pattern.quote(username), Pattern.CASE_INSENSITIVE));

    RestTemplate restTemplate = new RestTemplate();

    HttpEntity<String> httpEntity = new HttpEntity<>(basicDBObject.toString(), getHttpHeaders());

    String url =
        UriComponentsBuilder.fromUriString(sessionServiceURL)
            .pathSegment(Session.SessionType.custom_gene_list.name())
            .pathSegment("query")
            .pathSegment("fetch")
            .build()
            .toUriString();

    ResponseEntity<List<CustomGeneList>> responseEntity =
        restTemplate.exchange(
            url,
            HttpMethod.POST,
            httpEntity,
            new ParameterizedTypeReference<List<CustomGeneList>>() {});

    return responseEntity.getBody();
  }

  public List<CustomDataSession> getCustomDataSessionForUser(
      String username, List<String> studyIds) {
    List<BasicDBObject> basicDBObjects = new ArrayList<>();
    basicDBObjects.add(
        new BasicDBObject(
            "data.users", Pattern.compile(Pattern.quote(username), Pattern.CASE_INSENSITIVE)));
    basicDBObjects.add(
        new BasicDBObject("data.origin", new BasicDBObject(QUERY_OPERATOR_ALL, studyIds)));
    basicDBObjects.add(
        new BasicDBObject("data.origin", new BasicDBObject(QUERY_OPERATOR_SIZE, studyIds.size())));

    BasicDBObject queryDBObject = new BasicDBObject(QUERY_OPERATOR_AND, basicDBObjects);

    RestTemplate restTemplate = new RestTemplate();

    HttpEntity<String> httpEntity = new HttpEntity<>(queryDBObject.toString(), getHttpHeaders());

    String url =
        UriComponentsBuilder.fromUriString(sessionServiceURL)
            .pathSegment(Session.SessionType.custom_data.name())
            .pathSegment("query")
            .pathSegment("fetch")
            .build()
            .toUriString();

    ResponseEntity<List<CustomDataSession>> responseEntity =
        restTemplate.exchange(
            url,
            HttpMethod.POST,
            httpEntity,
            new ParameterizedTypeReference<List<CustomDataSession>>() {});

    return responseEntity.getBody();
  }

  public <T extends Serializable> ResponseEntity<Session> createSession(
      SessionType type, T payload) {
    RestTemplate restTemplate = new RestTemplate();
    HttpEntity<?> httpEntity = new HttpEntity<>(payload, getHttpHeaders());

    String url =
        UriComponentsBuilder.fromUriString(sessionServiceURL)
            .pathSegment(type.name())
            .build()
            .toUriString();

    ResponseEntity<Session> responseEntity =
        restTemplate.exchange(url, HttpMethod.POST, httpEntity, Session.class);

    return new ResponseEntity<>(responseEntity.getBody(), responseEntity.getStatusCode());
  }

  public <T extends Serializable> void updateUsers(SessionType type, String id, T payload) {
    RestTemplate restTemplate = new RestTemplate();
    HttpEntity<?> httpEntity = new HttpEntity<>(payload, getHttpHeaders());

    String url =
        UriComponentsBuilder.fromUriString(sessionServiceURL)
            .pathSegment(type.name())
            .pathSegment(id)
            .build()
            .toUriString();

    restTemplate.put(url, httpEntity);
  }

  public void updatePageSettings(SessionType type, String id, PageSettingsData body) {
    RestTemplate restTemplate = new RestTemplate();
    HttpEntity<Object> httpEntity = new HttpEntity<>(body, getHttpHeaders());

    String url =
        UriComponentsBuilder.fromUriString(sessionServiceURL)
            .pathSegment(type.name())
            .pathSegment(id)
            .build()
            .toUriString();

    restTemplate.put(url, httpEntity);
  }

  public PageSettings getRecentlyUpdatePageSettings(
      String username, Set<String> origin, String sessionPageName) {
    List<PageSettings> sessions = getPageSettingsForUser(username, origin, sessionPageName);
    // sort last updated in descending order
    sessions.sort(
        (PageSettings s1, PageSettings s2) ->
            s1.getData().getLastUpdated() > s2.getData().getLastUpdated() ? -1 : 1);

    return sessions.isEmpty() ? null : sessions.get(0);
  }
  
  /**
   * Gets virtual study by id if exists
   *
   * @param id - id of the virtual study to read
   * @return virtual study or empty if not found
   */
  public Optional<VirtualStudy> getVirtualStudyByIdIfExists(String id) {
      RestTemplate restTemplate = new RestTemplate();
      restTemplate.setErrorHandler(
          new DefaultResponseErrorHandler() {
              @Override
              public boolean hasError(ClientHttpResponse response) throws IOException {
                  return response.getStatusCode().is5xxServerError();
              }
          });

      String url =
          UriComponentsBuilder.fromUriString(sessionServiceURL)
              .pathSegment("virtual_study")
              .pathSegment(id)
              .build()
              .toUriString();

      ResponseEntity<VirtualStudy> responseEntity =
          restTemplate.exchange(
              url,
              HttpMethod.GET,
              new HttpEntity<>(getHttpHeaders()),
              VirtualStudy.class);
      return responseEntity.getStatusCode().is4xxClientError()
          || responseEntity.getBody() == null
          || responseEntity.getBody().getId() == null
          ? Optional.empty()
          : Optional.ofNullable(responseEntity.getBody());
  }
}
