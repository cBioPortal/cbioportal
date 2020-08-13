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

package org.cbioportal.security.spring.authentication.token.oauth2;

import org.cbioportal.model.DataAccessToken;
import org.cbioportal.service.DataAccessTokenService;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class OAuth2DataAccessTokenServiceImpl implements DataAccessTokenService {

    @Value("${dat.oauth2.issuer}")
    private String issuer;

    @Value("${dat.oauth2.clientId}")
    private String clientId;

    @Value("${dat.oauth2.clientSecret}")
    private String clientSecret;

    @Value("${dat.oauth2.accessTokenUri}")
    private String accessTokenUri;

    @Value("${dat.oauth2.userAuthorizationUri}")
    private String userAuthorizationUri;

    @Value("${dat.oauth2.redirectUri}")
    private String redirectUri;

    @Autowired
    private RestTemplate template;

    @Autowired
    private JwtTokenVerifierBuilder jwtTokenVerifierBuilder;

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
        map.add("scope", "openid offline_access"); // `openid must be included according to OIDC standards

        HttpEntity<MultiValueMap<String, String>> offlineRequest = new HttpEntity<>(map, headers);

        ResponseEntity<String> response = template.postForEntity(accessTokenUri, offlineRequest, String.class);

        String offlineToken = "";
        try {
            JsonNode json = new ObjectMapper().readTree(response.getBody());
            offlineToken = json.get("refresh_token").asText();
        } catch (Exception e) {
            throw new BadCredentialsException("Offline token could not be retrieved using access_code: "+ accessCode );
        }

        return new DataAccessToken(offlineToken);
    }

    @Override
    public DataAccessToken createDataAccessToken(final String username, final boolean allowRevocationOfOtherTokens) {
        throw new UnsupportedOperationException(
                "this implementation of (pure) JWT Data Access Tokens does not allow retrieval of stored tokens");
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
        final String kid = JwtHelper.headers(token).get("kid");
        try {

            final Jwt tokenDecoded = JwtHelper.decodeAndVerify(token, jwtTokenVerifierBuilder.build(kid));
            final String claims = tokenDecoded.getClaims();
            final JsonNode claimsMap = new ObjectMapper().readTree(claims);

            hasValidIssuer(claimsMap);
            hasValidClientId(claimsMap);

        } catch (Exception e) {
            throw new BadCredentialsException("Token is not valid (wrong key, issuer, or audience).");
        }
        return true;
    }

    @Override
    public String getUsername(final String token) {

        final Jwt tokenDecoded = JwtHelper.decode(token);

        final String claims = tokenDecoded.getClaims();
        JsonNode claimsMap;
        try {
            claimsMap = new ObjectMapper().readTree(claims);
        } catch (IOException e) {
            throw new BadCredentialsException("User name could not be found in offline token.");
        }

        if (! claimsMap.has("sub")) {
            throw new BadCredentialsException("User name could not be found in offline token.");
        }

        String userName = claimsMap.get("sub").asText();
        return userName;
    }

    @Override
    public Date getExpiration(final String token) {
        return null;
    }

    private void hasValidIssuer(final JsonNode claimsMap) throws BadCredentialsException {
        if (!claimsMap.get("iss").asText().equals(issuer)) {
            throw new BadCredentialsException("Wrong Issuer found in token");
        }
    }

    private void hasValidClientId(final JsonNode claimsMap) throws BadCredentialsException {
        if (!claimsMap.get("aud").asText().equals(clientId)) {
            throw new BadCredentialsException("Wrong clientId found in token");
        }
    }

    @Override
    public Authentication createAuthenticationRequest(String offlineToken) {
        // validity of the offline token is checked by the OAuth2 authentication server
        String principal = getUsername(offlineToken);
        return new OAuth2BearerAuthenticationToken(principal, offlineToken);
    }

}
