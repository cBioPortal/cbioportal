package org.cbioportal.web.util;

import java.nio.charset.Charset;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.cbioportal.session_service.domain.Session;
import org.cbioportal.session_service.domain.SessionType;
import org.cbioportal.web.parameter.CustomDataSession;
import org.cbioportal.web.parameter.PageSettings;
import org.cbioportal.web.parameter.VirtualStudy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    private Boolean isSessionServiceEnabled() {
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

    public Session getSession(SessionType type, String id) throws Exception {

        RestTemplate restTemplate = new RestTemplate();

        // add basic authentication in header
        HttpEntity<String> headers = new HttpEntity<String>(getHttpHeaders());
        ResponseEntity<String> responseEntity = restTemplate.exchange(sessionServiceURL + type + "/" + id,
                HttpMethod.GET, headers, String.class);

        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        Session session;

        if (type.equals(SessionType.virtual_study) || type.equals(SessionType.group)) {
            session = mapper.readValue(responseEntity.getBody(), VirtualStudy.class);
        } else if (type.equals(SessionType.settings)) {
            session = mapper.readValue(responseEntity.getBody(), PageSettings.class);
        } else if (type.equals(SessionType.custom_data)) {
            session = mapper.readValue(responseEntity.getBody(), CustomDataSession.class);
        } else {
            session = mapper.readValue(responseEntity.getBody(), Session.class);
        }

        return session;
    }

}
