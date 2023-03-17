package org.cbioportal.service.util;

import java.nio.charset.Charset;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.cbioportal.session_service.domain.SessionType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class SessionServiceRequestHandler {

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

}
