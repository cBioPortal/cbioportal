package org.cbioportal.service.util;

import static org.cbioportal.utils.removeme.Session.*;


import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

import com.mongodb.BasicDBObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.cbioportal.web.parameter.VirtualStudy;
import org.cbioportal.web.parameter.VirtualStudyData;
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

    @Value("${session.service.url:}")
    private String sessionServiceURL;

    @Value("${session.service.user:}")
    private String sessionServiceUser;

    @Value("${session.service.password:}")
    private String sessionServicePassword;

    private Boolean isBasicAuthEnabled() {
        return isSessionServiceEnabled() && sessionServicePassword != null && !sessionServicePassword.equals("");
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
        ResponseEntity<String> responseEntity = restTemplate.exchange(sessionServiceURL + type + "/" + id,
                HttpMethod.GET, headers, String.class);

        return responseEntity.getBody();
    }

    /**
     * Gets virtual study by id
     * @param id - id of the virtual study to read
     * @return virtual study
     */
    public VirtualStudy getVirtualStudyById(String id) {
        ResponseEntity<VirtualStudy> responseEntity = new RestTemplate()
            .exchange(sessionServiceURL + "/virtual_study/" + id,
                HttpMethod.GET,
                new HttpEntity<>(getHttpHeaders()),
                VirtualStudy.class);
        HttpStatusCode statusCode = responseEntity.getStatusCode();
        VirtualStudy virtualStudy = responseEntity.getBody();
        if (!statusCode.is2xxSuccessful() || virtualStudy == null) {
            LOG.error("The downstream server replied with statusCode={} and body={}." +
                    " Replying with the same status code to the client.",
                statusCode, virtualStudy);
            throw new IllegalStateException("The downstream server response is not successful");
        }
        return responseEntity.getBody();
    }

    /**
     * Get list of virtual studies accessible to user
     * @param username - user for whom get list of virtual studies
     * @return - list of virtual studies
     */
    public List<VirtualStudy> getVirtualStudiesAccessibleToUser(String username) {
        BasicDBObject basicDBObject = new BasicDBObject();
        basicDBObject.put("data.users", username);
        ResponseEntity<List<VirtualStudy>> responseEntity = new RestTemplate().exchange(
            sessionServiceURL + "/virtual_study/query/fetch",
            HttpMethod.POST,
            new HttpEntity<>(basicDBObject.toString(), getHttpHeaders()),
            new ParameterizedTypeReference<>() {
            });

        return responseEntity.getBody();
    }

    /**
     * Creates a virtual study out of virtual study definition (aka virtualStudyData)
     * @param virtualStudyData - definition of virtual study
     * @return virtual study object with id and the virtualStudyData
     */
    public VirtualStudy createVirtualStudy(VirtualStudyData virtualStudyData) {
        ResponseEntity<VirtualStudy> responseEntity = new RestTemplate().exchange(
            sessionServiceURL + "/virtual_study",
            HttpMethod.POST,
            new HttpEntity<>(virtualStudyData, getHttpHeaders()),
            new ParameterizedTypeReference<>() {
            });
        
        return responseEntity.getBody();
    }


    /**
     * Soft delete of the virtual study by de-associating all assigned users.
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
     * @param virtualStudy - virtual study to update
     */
    public void updateVirtualStudy(VirtualStudy virtualStudy) {
        new RestTemplate()
            .put(sessionServiceURL + "/virtual_study/" + virtualStudy.getId(),
                new HttpEntity<>(virtualStudy.getData(), getHttpHeaders()));
    }
}
