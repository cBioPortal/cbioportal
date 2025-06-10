package org.cbioportal.legacy.service.util;

import static org.cbioportal.legacy.utils.removeme.Session.*;

import com.mongodb.BasicDBObject;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.cbioportal.legacy.utils.removeme.Session;
import org.cbioportal.legacy.web.parameter.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

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

    // add basic authentication in header
    HttpEntity<String> headers = new HttpEntity<>(getHttpHeaders());
    ResponseEntity<String> responseEntity =
        restTemplate.exchange(
            sessionServiceURL + type + "/" + id, HttpMethod.GET, headers, String.class);

    return responseEntity.getBody();
  }

  /**
   * Gets virtual study by id
   *
   * @param id - id of the virtual study to read
   * @return virtual study
   */
  public VirtualStudy getVirtualStudyById(String id) {
    ResponseEntity<VirtualStudy> responseEntity =
        new RestTemplate()
            .exchange(
                sessionServiceURL + "/virtual_study/" + id,
                HttpMethod.GET,
                new HttpEntity<>(getHttpHeaders()),
                VirtualStudy.class);
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
    basicDBObject.put("data.users", username);
    ResponseEntity<List<VirtualStudy>> responseEntity =
        new RestTemplate()
            .exchange(
                sessionServiceURL + "/virtual_study/query/fetch",
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
    ResponseEntity<VirtualStudy> responseEntity =
        new RestTemplate()
            .exchange(
                sessionServiceURL + "/virtual_study",
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
    new RestTemplate()
        .put(
            sessionServiceURL + "/virtual_study/" + virtualStudy.getId(),
            new HttpEntity<>(virtualStudy.getData(), getHttpHeaders()));
  }

  public List<PageSettings> getPageSettingsForUser(
      String username, Set<String> origin, String sessionPageName) {

    List<BasicDBObject> basicDBObjects = new ArrayList<>();
    basicDBObjects.add(
        new BasicDBObject("data.owner", Pattern.compile(username, Pattern.CASE_INSENSITIVE)));
    basicDBObjects.add(
        new BasicDBObject("data.origin", new BasicDBObject(QUERY_OPERATOR_ALL, origin)));
    basicDBObjects.add(
        new BasicDBObject("data.origin", new BasicDBObject(QUERY_OPERATOR_SIZE, origin.size())));
    basicDBObjects.add(new BasicDBObject("data.page", sessionPageName));

    BasicDBObject queryDBObject = new BasicDBObject(QUERY_OPERATOR_AND, basicDBObjects);

    ResponseEntity<List<PageSettings>> responseEntity =
        new RestTemplate()
            .exchange(
                sessionServiceURL + Session.SessionType.settings + "/query/fetch",
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
        new BasicDBObject("data.users", Pattern.compile(username, Pattern.CASE_INSENSITIVE)));
    basicDBObjects.add(
        new BasicDBObject("data.origin", new BasicDBObject(QUERY_OPERATOR_ALL, studyIds)));
    basicDBObjects.add(
        new BasicDBObject("data.origin", new BasicDBObject(QUERY_OPERATOR_SIZE, studyIds.size())));

    BasicDBObject queryDBObject = new BasicDBObject(QUERY_OPERATOR_AND, basicDBObjects);

    RestTemplate restTemplate = new RestTemplate();

    HttpEntity<String> httpEntity = new HttpEntity<>(queryDBObject.toString(), getHttpHeaders());

    ResponseEntity<List<VirtualStudy>> responseEntity =
        restTemplate.exchange(
            sessionServiceURL + Session.SessionType.group + "/query/fetch",
            HttpMethod.POST,
            httpEntity,
            new ParameterizedTypeReference<List<VirtualStudy>>() {});

    return responseEntity.getBody();
  }

  public List<CustomGeneList> getCustomGeneListsForUser(String username) {
    BasicDBObject basicDBObject = new BasicDBObject();
    basicDBObject.put("data.users", Pattern.compile(username, Pattern.CASE_INSENSITIVE));

    RestTemplate restTemplate = new RestTemplate();

    HttpEntity<String> httpEntity = new HttpEntity<>(basicDBObject.toString(), getHttpHeaders());

    ResponseEntity<List<CustomGeneList>> responseEntity =
        restTemplate.exchange(
            sessionServiceURL + Session.SessionType.custom_gene_list + "/query/fetch",
            HttpMethod.POST,
            httpEntity,
            new ParameterizedTypeReference<List<CustomGeneList>>() {});

    return responseEntity.getBody();
  }

  public List<CustomDataSession> getCustomDataSessionForUser(
      String username, List<String> studyIds) {
    List<BasicDBObject> basicDBObjects = new ArrayList<>();
    basicDBObjects.add(
        new BasicDBObject("data.users", Pattern.compile(username, Pattern.CASE_INSENSITIVE)));
    basicDBObjects.add(
        new BasicDBObject("data.origin", new BasicDBObject(QUERY_OPERATOR_ALL, studyIds)));
    basicDBObjects.add(
        new BasicDBObject("data.origin", new BasicDBObject(QUERY_OPERATOR_SIZE, studyIds.size())));

    BasicDBObject queryDBObject = new BasicDBObject(QUERY_OPERATOR_AND, basicDBObjects);

    RestTemplate restTemplate = new RestTemplate();

    HttpEntity<String> httpEntity = new HttpEntity<>(queryDBObject.toString(), getHttpHeaders());

    ResponseEntity<List<CustomDataSession>> responseEntity =
        restTemplate.exchange(
            sessionServiceURL + Session.SessionType.custom_data + "/query/fetch",
            HttpMethod.POST,
            httpEntity,
            new ParameterizedTypeReference<List<CustomDataSession>>() {});

    return responseEntity.getBody();
  }

  public <T extends Serializable> ResponseEntity<Session> createSession(
      SessionType type, T payload) {
    RestTemplate restTemplate = new RestTemplate();
    HttpEntity<?> httpEntity = new HttpEntity<>(payload, getHttpHeaders());

    ResponseEntity<Session> responseEntity =
        restTemplate.exchange(sessionServiceURL + type, HttpMethod.POST, httpEntity, Session.class);

    return new ResponseEntity<>(responseEntity.getBody(), responseEntity.getStatusCode());
  }

  public <T extends Serializable> void updateUsers(SessionType type, String id, T payload) {
    RestTemplate restTemplate = new RestTemplate();
    HttpEntity<?> httpEntity = new HttpEntity<>(payload, getHttpHeaders());

    restTemplate.put(sessionServiceURL + type + "/" + id, httpEntity);
  }

  public void updatePageSettings(SessionType type, String id, PageSettingsData body) {
    RestTemplate restTemplate = new RestTemplate();
    HttpEntity<Object> httpEntity = new HttpEntity<>(body, getHttpHeaders());

    restTemplate.put(sessionServiceURL + type + "/" + id, httpEntity);
  }
}
