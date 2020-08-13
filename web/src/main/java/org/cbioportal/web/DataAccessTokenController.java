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
import org.cbioportal.service.exception.DataAccessTokenNoUserIdentityException;
import org.cbioportal.service.exception.DataAccessTokenProhibitedUserException;
import org.cbioportal.web.config.annotation.InternalApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@InternalApi
@RequestMapping // replaces @RestController; controller is created conditionally in DataAccessTokenConfig of security-spring module
@ResponseBody   // needed when not using @RestController annotation
@Validated
@Api(tags = "Data Access Tokens", description = " ")
public class DataAccessTokenController {

    @Value("${dat.uuid_revoke_other_tokens:false}")
    private Boolean allowRevocationOfOtherTokens;

    @Value("${dat.unauth_users:anonymousUser}")
    private String[] USERS_WHO_CANNOT_USE_TOKENS;

    @Autowired
    private DataAccessTokenService tokenService;
    private Set<String> usersWhoCannotUseTokenSet;

    @Autowired
    private void initializeUsersWhoCannotUseTokenSet() {
        usersWhoCannotUseTokenSet = new HashSet<>(Arrays.asList(USERS_WHO_CANNOT_USE_TOKENS));
    }

    private String fileName = "cbioportal_data_access_token.txt";

    @RequestMapping(method = RequestMethod.POST, value = "/data-access-tokens", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DataAccessToken> createDataAccessToken(Authentication authentication,
    @RequestParam(required = false) Boolean myAllowRevocationOfOtherTokens) throws HttpClientErrorException {
        String userName = getAuthenticatedUser(authentication);
        DataAccessToken token = createDataAccessToken(userName, myAllowRevocationOfOtherTokens);
        if (token == null) {
            return new ResponseEntity<>(token, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(token, HttpStatus.CREATED);
    }

        @RequestMapping(method = RequestMethod.GET, value = "/data-access-tokens")
        public ResponseEntity<List<DataAccessToken>> getAllDataAccessTokens(HttpServletRequest request,
        Authentication authentication) {
            String userName = getAuthenticatedUser(authentication);
            List<DataAccessToken> allDataAccessTokens = tokenService.getAllDataAccessTokens(userName);
            return new ResponseEntity<>(allDataAccessTokens, HttpStatus.OK);
        }

    @RequestMapping(method = RequestMethod.GET, value = "/data-access-tokens/{token}")
    public ResponseEntity<DataAccessToken> getDataAccessToken(
    @ApiParam(required = true, value = "token") @PathVariable String token) {
        DataAccessToken dataAccessToken = tokenService.getDataAccessTokenInfo(token);
        return new ResponseEntity<>(dataAccessToken, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/data-access-tokens")
    public void revokeAllDataAccessTokens(Authentication authentication) {
        tokenService.revokeAllDataAccessTokens(getAuthenticatedUser(authentication));
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/data-access-tokens/{token}")
    public void revokeDataAccessToken(@ApiParam(required = true, value = "token") @PathVariable String token) {
        tokenService.revokeDataAccessToken(token);
    }

    // this is the entrypoint for the cBioPortal frontend to download a single user token
    @RequestMapping("/data-access-token")
    public ResponseEntity<String> downloadDataAccessToken(Authentication authentication,
        HttpServletRequest request, HttpServletResponse response) throws IOException {

        // for other methods add header to trigger download of the token by the browser
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        String userName = getAuthenticatedUser(authentication);
        DataAccessToken token = createDataAccessToken(userName, allowRevocationOfOtherTokens);
        if (token == null) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(token.toString(), HttpStatus.CREATED);
    }

    private String getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new DataAccessTokenNoUserIdentityException();
        }
        String username = authentication.getName();
        if (usersWhoCannotUseTokenSet.contains(username)) {
            throw new DataAccessTokenProhibitedUserException();
        }
        return username;
    }

    private DataAccessToken createDataAccessToken(String userName, Boolean myAllowRevocationOfOtherTokens) {
        DataAccessToken token;
        if (myAllowRevocationOfOtherTokens != null) {
            token = tokenService.createDataAccessToken(userName, myAllowRevocationOfOtherTokens);
        }  else {
            token = tokenService.createDataAccessToken(userName);
        }
        return token;
    }

}
