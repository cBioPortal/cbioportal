/*
 * Copyright (c) 2018 Memorial Sloan-Kettering Cancer Center.
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

import org.cbioportal.model.DataAccessToken;
import org.cbioportal.service.DataAccessTokenService;
import org.cbioportal.service.exception.DataAccessTokenNoUserIdentityException;
import org.cbioportal.service.exception.DataAccessTokenProhibitedUserException;
import org.cbioportal.web.config.annotation.InternalApi;

import io.swagger.annotations.*;
import java.util.*;
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

    @Autowired
    // use @Qualifier to ensure we get tokenService bean from applicationContext-security.xml
    // tokenSerice bean in security.xml file has same name so would be picked anyway by default,
    // but this avoids a NoUniqueBeanDefinitionException
    @Qualifier("tokenService")
    private DataAccessTokenService dataAccessTokenService;

//      TODO: figure out how to read this from the properties file
//    @Value("${security.data_tokens.unauth_users}")
    private String[] USERS_WHO_CANNOT_USE_TOKENS = {"anonymousUser", "servcbioportal"};
    private Set<String> usersWhoCannotUseTokenSet = null;

    @Autowired
    private void initializeUsersWhoCannotUseTokenSet() {
        usersWhoCannotUseTokenSet = new HashSet<>(Arrays.asList(USERS_WHO_CANNOT_USE_TOKENS));
    }

    @RequestMapping(method = RequestMethod.POST, value = "/data-access-tokens", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DataAccessToken> createDataAccessToken(Authentication authentication,
                                    @RequestParam(required = false) Boolean allowRevocationOfOtherTokens) throws HttpClientErrorException {
        DataAccessToken createdToken;
        if (allowRevocationOfOtherTokens != null) {
            createdToken = dataAccessTokenService.createDataAccessToken(getAuthenticatedUser(authentication), allowRevocationOfOtherTokens);
        }
        else {
            createdToken = dataAccessTokenService.createDataAccessToken(getAuthenticatedUser(authentication));
        }
        if (createdToken == null) {
            return new ResponseEntity<>(new DataAccessToken(null), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(createdToken, HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/data-access-tokens")
    public ResponseEntity<List<DataAccessToken>> getAllDataAccessTokens(Authentication authentication) {
        List<DataAccessToken> allDataAccessTokens = dataAccessTokenService.getAllDataAccessTokens(getAuthenticatedUser(authentication));
        return new ResponseEntity<>(allDataAccessTokens, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/data-access-tokens/{token}")
    public ResponseEntity<DataAccessToken> getDataAccessToken(
            @ApiParam(required = true, value = "token") @PathVariable String token) {
        DataAccessToken dataAccessToken = dataAccessTokenService.getDataAccessTokenInfo(token);
        return new ResponseEntity<>(dataAccessToken, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/data-access-tokens")
    public void revokeAllDataAccessTokens(Authentication authentication) {
        dataAccessTokenService.revokeAllDataAccessTokens(getAuthenticatedUser(authentication));
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/data-access-tokens/{token}")
    public void revokeDataAccessToken(
            @ApiParam(required = true, value = "token") @PathVariable String token) {
        dataAccessTokenService.revokeDataAccessToken(token);
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
