/*
 * Copyright (c) 2020 The Hyve B.V.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.cbioportal.application.security.token.oauth2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import org.cbioportal.legacy.model.DataAccessToken;
import org.cbioportal.legacy.service.DataAccessTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class OAuth2DataAccessTokenServiceImpl implements DataAccessTokenService {

  private static final Logger LOG = LoggerFactory.getLogger(OAuth2DataAccessTokenServiceImpl.class);

  @Value("${dat.oauth2.issuer}")
  private String issuer;

  @Value("${dat.oauth2.clientId}")
  private String clientId;

  @Value("${dat.oauth2.clientSecret}")
  private String clientSecret;

  @Value("${dat.oauth2.accessTokenUri}")
  private String accessTokenUri;

  @Value("${dat.oauth2.redirectUri}")
  private String redirectUri;

  @Value("${dat.oauth2.jwkUrl:}")
  private String jwkUrl;

  private final RestTemplate template;
  private DefaultJWTProcessor<SecurityContext> jwtProcessor;

  @Autowired
  public OAuth2DataAccessTokenServiceImpl(RestTemplate template) {
    this.template = template;
  }

  @PostConstruct
  public void init() {
    try {
      JWKSource<SecurityContext> keySource = new RemoteJWKSet<>(new URL(this.jwkUrl));
      JWSKeySelector<SecurityContext> keySelector =
          new JWSVerificationKeySelector<>(JWSAlgorithm.RS512, keySource);
      jwtProcessor = new DefaultJWTProcessor<>();
      jwtProcessor.setJWSKeySelector(keySelector);
    } catch (MalformedURLException e) {
      LOG.error("Invalid JWK URL: {}", this.jwkUrl, e);
      // Handle initialization failure, perhaps by preventing the application from starting
      // or by setting jwtProcessor to null and checking it in methods.
      throw new RuntimeException("Failed to initialize JWT processor due to invalid JWK URL", e);
    }
  }

  @Override
  // request offline token from authentication server via back channel
  public DataAccessToken createDataAccessToken(final String accessCode) {

    HttpHeaders headers = new HttpHeaders();

    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("grant_type", "authorization_code");
    map.add("code", accessCode);
    map.add("client_id", clientId);
    map.add("client_secret", clientSecret);
    map.add("redirect_uri", redirectUri);
    map.add(
        "scope", "openid offline_access"); // `openid must be included according to OIDC standards

    HttpEntity<MultiValueMap<String, String>> offlineRequest = new HttpEntity<>(map, headers);

    ResponseEntity<String> response =
        template.postForEntity(accessTokenUri, offlineRequest, String.class);

    String offlineToken = "";
    try {
      JsonNode json = new ObjectMapper().readTree(response.getBody());
      offlineToken = json.get("refresh_token").asText();
    } catch (Exception e) {
      throw new BadCredentialsException(
          "Offline token could not be retrieved using access_code: " + accessCode);
    }

    return new DataAccessToken(offlineToken);
  }

  @Override
  public List<DataAccessToken> getAllDataAccessTokens(final String username) {
    throw new UnsupportedOperationException(
        "this implementation of (pure) JWT Data Access Tokens does not allow retrieval of stored tokens");
  }

  @Override
  public DataAccessToken getDataAccessToken(final String username) {
    throw new UnsupportedOperationException(
        "this implementation of (pure) JWT Data Access Tokens does not allow retrieval of stored tokens");
  }

  @Override
  public DataAccessToken getDataAccessTokenInfo(final String token) {
    throw new UnsupportedOperationException(
        "this implementation of (pure) JWT Data Access Tokens does not allow this operation");
  }

  @Override
  public void revokeAllDataAccessTokens(final String username) {
    throw new UnsupportedOperationException(
        "this implementation of (pure) JWT Data Access Tokens does not allow revocation of tokens");
  }

  @Override
  public void revokeDataAccessToken(final String token) {
    throw new UnsupportedOperationException(
        "this implementation of (pure) JWT Data Access Tokens does not allow revocation of tokens");
  }

  @Override
  public Boolean isValid(final String token) {
    if (jwtProcessor == null) {
      LOG.error("JWT Processor not initialized, cannot validate token.");
      throw new BadCredentialsException(
          "Token validation system not initialized properly.");
    }
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);
      JWTClaimsSet claimsSet = jwtProcessor.process(signedJWT, null);

      hasValidIssuer(claimsSet);
      hasValidClientId(claimsSet);

    } catch (ParseException | BadJOSEException | JOSEException e) {
      LOG.warn("Token validation failed: {}", e.getMessage());
      throw new BadCredentialsException(
          "Token is not valid (parsing/signature/claims validation failed).", e);
    }
    return true;
  }

  @Override
  public String getUsername(final String token) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);
      JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet(); // No validation here, just parsing

      if (claimsSet.getSubject() == null) {
        throw new BadCredentialsException("User name (sub claim) could not be found in token.");
      }
      return claimsSet.getSubject();
    } catch (ParseException e) {
      LOG.warn("Token parsing failed while trying to get username: {}", e.getMessage());
      throw new BadCredentialsException("User name could not be found in token (parse error).", e);
    }
  }

  @Override
  public Date getExpiration(final String token) {
    // Nimbus JWT library can parse expiration time if needed.
    // Example:
    // try {
    //   SignedJWT signedJWT = SignedJWT.parse(token);
    //   JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
    //   return claimsSet.getExpirationTime();
    // } catch (ParseException e) {
    //   LOG.warn("Failed to parse token for expiration: {}", e.getMessage());
    //   return null;
    // }
    return null; // Current behavior is to return null
  }

  private void hasValidIssuer(final JWTClaimsSet claimsSet) throws BadCredentialsException {
    if (claimsSet.getIssuer() == null || !claimsSet.getIssuer().equals(issuer)) {
      throw new BadCredentialsException("Wrong Issuer found in token");
    }
  }

  private void hasValidClientId(final JWTClaimsSet claimsSet) throws BadCredentialsException {
    List<String> audience = claimsSet.getAudience();
    if (audience == null || !audience.contains(clientId)) {
      throw new BadCredentialsException("Wrong clientId (audience) found in token");
    }
  }

  @Override
  public Authentication createAuthenticationRequest(String offlineToken) {
    // validity of the offline token is checked by the OAuth2 authentication server
    return new OAuth2BearerAuthenticationToken(offlineToken);
  }
}
