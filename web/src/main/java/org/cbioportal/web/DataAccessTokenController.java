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

import org.cbioportal.model.DataAccessToken;
import org.cbioportal.service.DataAccessTokenService;
import org.cbioportal.service.DataAccessTokenServiceFactory;
import org.cbioportal.service.exception.DataAccessTokenNoUserIdentityException;
import org.cbioportal.service.exception.DataAccessTokenProhibitedUserException;
import org.cbioportal.service.impl.UnauthDataAccessTokenServiceImpl;
import org.cbioportal.web.config.annotation.InternalApi;

import io.swagger.annotations.*;
import java.util.*;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

@InternalApi
@RestController
@Validated
@Api(tags = "Data Access Tokens", description = " ")
public class DataAccessTokenController {

    private final List<String> SUPPORTED_DAT_METHODS = Arrays.asList("uuid", "jwt", "none");

    @Value("${dat.method:none}") // default value is none
    private String datMethod;

    @Autowired
    private DataAccessTokenServiceFactory dataAccessTokenServiceFactory;

    private DataAccessTokenService tokenService;
    @PostConstruct
    public void postConstruct() {
        if (datMethod == null || !SUPPORTED_DAT_METHODS.contains(datMethod)) {
            throw new RuntimeException("Specified data access token method, " + datMethod + " is not supported");
        } else {
            this.tokenService = this.dataAccessTokenServiceFactory.getDataAccessTokenService(this.datMethod);
        }
    }

    @Value("${dat.unauth_users:anonymousUser}")
    private String[] USERS_WHO_CANNOT_USE_TOKENS;
    private Set<String> usersWhoCannotUseTokenSet;

    @Autowired
    private void initializeUsersWhoCannotUseTokenSet() {
        usersWhoCannotUseTokenSet = new HashSet<>(Arrays.asList(USERS_WHO_CANNOT_USE_TOKENS));
    }

    @RequestMapping(method = RequestMethod.POST, value = "/data-access-tokens", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DataAccessToken> createDataAccessToken(Authentication authentication,
                                    @RequestParam(required = false) Boolean allowRevocationOfOtherTokens) throws HttpClientErrorException {
        DataAccessToken createdToken;
        if (allowRevocationOfOtherTokens != null) {
            createdToken = tokenService.createDataAccessToken(getAuthenticatedUser(authentication), allowRevocationOfOtherTokens);
        }
        else {
            createdToken = tokenService.createDataAccessToken(getAuthenticatedUser(authentication));
        }
        if (createdToken == null) {
            return new ResponseEntity<>(new DataAccessToken(null), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(createdToken, HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/data-access-tokens")
    public ResponseEntity<List<DataAccessToken>> getAllDataAccessTokens(Authentication authentication) {
        List<DataAccessToken> allDataAccessTokens = tokenService.getAllDataAccessTokens(getAuthenticatedUser(authentication));
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
    public void revokeDataAccessToken(
            @ApiParam(required = true, value = "token") @PathVariable String token) {
        tokenService.revokeDataAccessToken(token);
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
}
