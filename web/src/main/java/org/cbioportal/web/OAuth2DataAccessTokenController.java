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

package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.cbioportal.model.DataAccessToken;
import org.cbioportal.service.DataAccessTokenService;
import org.cbioportal.service.exception.DataAccessTokenProhibitedUserException;
import org.cbioportal.web.config.annotation.InternalApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@InternalApi
@RequestMapping // replaces @RestController; controller is created conditionally in DataAccessTokenConfig of security-spring module
@ResponseBody   // needed when not using @RestController annotation
@Validated
@Api(tags = "Data Access Tokens", description = " ")
public class OAuth2DataAccessTokenController {

    @Value("${dat.oauth2.userAuthorizationUri}")
    private String userAuthorizationUri;

    @Value("${dat.oauth2.redirectUri}")
    private String redirectUri;

    @Value("${dat.oauth2.clientId}")
    private String clientId;

    @Autowired
    private DataAccessTokenService tokenService;
    private String authorizationUrl;
    private String fileName = "cbioportal_data_access_token.txt";

    @PostConstruct
    public void postConstruct() throws UnsupportedEncodingException {

        String scopeEncoded = URLEncoder.encode("openid offline_access", StandardCharsets.UTF_8.toString());
        String clientIdEncoded = URLEncoder.encode(clientId, StandardCharsets.UTF_8.toString());
        String redirUriEncode = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8.toString());

        authorizationUrl = String.format("%s?response_type=%s&scope=%s&client_id=%s&redirect_uri=%s", userAuthorizationUri, "code", scopeEncoded, clientIdEncoded, redirUriEncode);
    }

    // this is the entrypoint for the cBioPortal frontend to download a single user token
    @RequestMapping("/data-access-token")
    public ResponseEntity<String> downloadDataAccessToken(Authentication authentication,
        HttpServletRequest request, HttpServletResponse response) throws IOException {

        // redirect to authentication endpoint
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", authorizationUrl);
        return new ResponseEntity<>(headers, HttpStatus.FOUND);

    }

    // retrieve and trigger download OAuth2 offline token
    @RequestMapping("/data-access-token/oauth2")
    public ResponseEntity<String> downloadOAuth2DataAccessToken(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String accessCode = request.getParameter("code");
        DataAccessToken offlineToken = tokenService.createDataAccessToken(accessCode);

        if (offlineToken == null) {
            throw new DataAccessTokenProhibitedUserException();
        }

        // add header to trigger download of the token by the browser
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        return new ResponseEntity<>(offlineToken.toString(), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/data-access-tokens", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DataAccessToken> createDataAccessToken(Authentication authentication) throws HttpClientErrorException {
        throw new UnsupportedOperationException("this endpoint is does not apply to OAuth2 data access token method.");
    }

    @RequestMapping(method = RequestMethod.GET, value = "/data-access-tokens")
    public ResponseEntity<List<DataAccessToken>> getAllDataAccessTokens(HttpServletRequest request,
    Authentication authentication) {
        throw new UnsupportedOperationException("this endpoint is does not apply to OAuth2 data access token method.");
    }

    @RequestMapping(method = RequestMethod.GET, value = "/data-access-tokens/{token}")
    public ResponseEntity<DataAccessToken> getDataAccessToken(
        @ApiParam(required = true, value = "token") @PathVariable String token) {
        throw new UnsupportedOperationException("this endpoint is does not apply to OAuth2 data access token method.");

    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/data-access-tokens")
    public void revokeAllDataAccessTokens(Authentication authentication) {
        throw new UnsupportedOperationException("this endpoint is does not apply to OAuth2 data access token method.");
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/data-access-tokens/{token}")
    public void revokeDataAccessToken(@ApiParam(required = true, value = "token") @PathVariable String token) {
        throw new UnsupportedOperationException("this endpoint is does not apply to OAuth2 data access token method.");
    }

}
