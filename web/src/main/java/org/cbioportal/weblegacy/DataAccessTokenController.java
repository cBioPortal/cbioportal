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

import io.swagger.annotations.ApiParam;
import java.net.*;
import java.util.*;
import org.cbioportal.model.DataAccessToken;
import org.cbioportal.service.DataAccessTokenService;
import org.cbioportal.service.exception.MaxNumberTokensExceededException;
import org.cbioportal.service.exception.TokenNotFoundException;
import org.cbioportal.web.error.ErrorResponse;
import org.cbioportal.weblegacy.exception.DataAccessTokenNoUserIdentityException;
import org.cbioportal.weblegacy.exception.DataAccessTokenProhibitedUserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;

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
    private String[] USERS_WHO_CANNOT_USE_TOKENS = {"anonymousUser", "servcbioportal"};
    private Set<String> usersWhoCannotUseTokenSet = null;

    @Autowired
    private void initializeUsersWhoCannotUseTokenSet() {
        usersWhoCannotUseTokenSet = new HashSet<>(Arrays.asList(USERS_WHO_CANNOT_USE_TOKENS));
    }

    @RequestMapping(method = RequestMethod.POST, value = "/dataAccessToken", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DataAccessToken> createDataAccessToken(Authentication authentication,
                                    @RequestParam(required = false) Boolean allowRevocationOfOtherTokens) throws HttpClientErrorException {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.APPLICATION_JSON);
        DataAccessToken createdToken = dataAccessTokenService.createDataAccessToken(getAuthenticatedUser(authentication), allowRevocationOfOtherTokens);
        if (createdToken == null) {
            return new ResponseEntity<>(new DataAccessToken(null), responseHeaders, HttpStatus.NOT_FOUND);
        }
        URI location = constructLocationForDataAccessToken(createdToken);
        responseHeaders.setLocation(location);
        return new ResponseEntity<>(createdToken, responseHeaders, HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/dataAccessTokens")
    public ResponseEntity<List<DataAccessToken>> getAllDataAccessTokens(Authentication authentication) {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.APPLICATION_JSON);
        List<DataAccessToken> allDataAccessTokens = dataAccessTokenService.getAllDataAccessTokens(getAuthenticatedUser(authentication));
        return new ResponseEntity<>(allDataAccessTokens, responseHeaders, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/dataAccessToken/{token}")
    public ResponseEntity<DataAccessToken> getDataAccessToken(
            @ApiParam(required = true, value = "token") @PathVariable String token) {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.APPLICATION_JSON);
        DataAccessToken dataAccessToken = dataAccessTokenService.getDataAccessTokenInfo(token);
        return new ResponseEntity<>(dataAccessToken, responseHeaders, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/dataAccessTokens")
    public void revokeAllDataAccessTokens(Authentication authentication) {
        dataAccessTokenService.revokeAllDataAccessTokens(getAuthenticatedUser(authentication));
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/dataAccessToken/{token}")
    public void revokeDataAccessToken(
            @ApiParam(required = true, value = "token") @PathVariable String token) {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.APPLICATION_JSON);
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

    private URI constructLocationForDataAccessToken(DataAccessToken createdToken) {
        URI location = null;
        String urlEncodedString = URLEncoder.encode(createdToken.getToken()); 
        try { 
            location = new URI("/api-legacy/dataAccessToken/" + urlEncodedString);
        } catch (URISyntaxException e) {
        }
        if (location == null) {
            return null;
        }
        return location;
    }

    @ExceptionHandler(TokenNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTokenNotFoundException() {
        ErrorResponse response = new ErrorResponse("Specified token can not be found");
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DataAccessTokenNoUserIdentityException.class)
    public ResponseEntity<ErrorResponse> handleDataAccessTokenNoUserIdentityException() {
        ErrorResponse response = new ErrorResponse("No authenticated identity found while processing request");
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(DataAccessTokenProhibitedUserException.class)
    public ResponseEntity<ErrorResponse> handleDataAccessTokenProhibitedUserException() {
        ErrorResponse response = new ErrorResponse("You are prohibited from using Data Access Tokens");
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedOperation() {
        ErrorResponse response = new ErrorResponse("This server does not support this operation for Data Access Tokens");
        return new ResponseEntity<>(response, HttpStatus.NOT_IMPLEMENTED);
    }

    @ExceptionHandler(MaxNumberTokensExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxNumberTokensExceedeException() {
        ErrorResponse response = new ErrorResponse("User has reached maximum number of tokens. Tokens must be expire or be revoked before requesting a new one");
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }
}
