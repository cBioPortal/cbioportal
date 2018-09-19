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

package org.cbioportal.weblegacy;

import java.net.*;
import java.util.*;
import org.cbioportal.service.DataAccessTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

@RestController
public class DataAccessTokenController {

    @Autowired
    // use @Qualifier to ensure we get tokenService bean from applicationContext-security.xml
    // tokenSerice bean in security.xml file has same name so would be picked anyway by default,
    // but this avoids a NoUniqueBeanDefinitionException
    @Qualifier("tokenService")
    private DataAccessTokenService dataAccessTokenService;

//      TODO: figure out how to read this from the properties file
//    @Value("${security.data_tokens.unauth_users}")
//    private String[] USERS_WHO_CANNOT_USE_TOKENS = {"anonymousUser", "servcbioportal";
    private String[] USERS_WHO_CANNOT_USE_TOKENS = {"anonymousUser", "servcbioportal"};
    private Set<String> usersWhoCannotUseTokenSet = null;

    @Autowired
    private void initializeUsersWhoCannotUseTokenSet() {
        usersWhoCannotUseTokenSet = new HashSet<>(Arrays.asList(USERS_WHO_CANNOT_USE_TOKENS));
    }

    @RequestMapping(method = RequestMethod.POST, value = "/dataAccessToken")
    public ResponseEntity<String> createDataAccessToken(Authentication authentication,
                                    @RequestParam(required = false) Boolean allowRevocationOfOtherTokens) {
        HttpHeaders responseHeaders = new HttpHeaders();
        String createdToken = dataAccessTokenService.createDataAccessToken(getAuthenticatedUser(authentication), allowRevocationOfOtherTokens);
        if (createdToken == null) {
            return new ResponseEntity<String>("Unable to create a new token.", responseHeaders, HttpStatus.NOT_FOUND);
        }
        URI location = constructLocationForDataAccessToken(createdToken);
        responseHeaders.setLocation(location);
        String responseBody = "Data Access Token successfully created:\n" + createdToken + "\nadditional information for this resource accessible here:\n" + location.toString() + "\n";
        return new ResponseEntity<String>(responseBody, responseHeaders, HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/dataAccessToken")
    public String getDataAccessToken(Authentication authentication,
                                    @RequestParam(required = false) Long minimumTimeBeforeExpiration) {
        return dataAccessTokenService.getDataAccessToken(getAuthenticatedUser(authentication));
    }

    @RequestMapping(method = RequestMethod.GET, value = "/dataAccessToken/all")
    public List<String> getDataAccessTokenAll(Authentication authentication) {
        return dataAccessTokenService.getAllDataAccessTokens(getAuthenticatedUser(authentication));
    }

    @RequestMapping(method = RequestMethod.GET, value = "/dataAccessToken/revokeAll")
    public void getDataAccessTokenRevokeAll(Authentication authentication) {
        dataAccessTokenService.revokeAllDataAccessTokens(getAuthenticatedUser(authentication));
    }

    private String getAuthenticatedUser(Authentication authentication) {
        if (authentication == null) {
            //TODO: this gets converted into a server error (when the exception propagates up to the the REST response generation) .. so figure out how to actually with 401 status
            throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "user authentication not available");
        }
        if (!authentication.isAuthenticated()) {
            throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "current user is not authenticated");
        }
        String username = authentication.getName();
        if (usersWhoCannotUseTokenSet.contains(username)) {
            throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "you are not authorized to use data access tokens");
        }
        return username;
    }

    private URI constructLocationForDataAccessToken(String createdToken) {
        URI location = null;
        String urlEncodedString = URLEncoder.encode(createdToken); 
        try { 
            location = new URI("/api-legacy/dataAccessToken/" + urlEncodedString);
        } catch (URISyntaxException e) {
        }
        if (location == null) {
            return null;
        }
        return location;
    }

}
