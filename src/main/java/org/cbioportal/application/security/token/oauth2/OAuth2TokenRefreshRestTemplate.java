/*
 * Copyright (c) 2020 The Hyve B.V.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cbioportal.application.security.token.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
public class OAuth2TokenRefreshRestTemplate {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Value("${dat.oauth2.clientId:}")
  private String clientId;

  @Value("${dat.oauth2.clientSecret:}")
  private String clientSecret;

  @Value("${dat.oauth2.accessTokenUri:}")
  private String accessTokenUri;

  private final RestTemplate template;

  @Autowired
  public OAuth2TokenRefreshRestTemplate(RestTemplate template) {
    this.template = template;
  }

  public String getAccessToken(String offlineToken) throws BadCredentialsException {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("grant_type", "refresh_token");
    map.add("client_id", clientId);
    map.add("client_secret", clientSecret);
    map.add("refresh_token", offlineToken);

    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

    ResponseEntity<String> response = null;
    try {
      response = template.postForEntity(accessTokenUri, request, String.class);
      String accessToken =
          new ObjectMapper().readTree(response.getBody()).get("access_token").asText();
      logger.debug("Received access token from authentication server:\n{}", accessToken);
      return accessToken;
    } catch (Exception e) {
      logger.error(
          "Authentication server did not return an access token. Server response:\n{}", response);
      throw new BadCredentialsException("Authentication server did not return an access token.");
    }
  }
}
