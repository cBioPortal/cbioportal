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

package org.cbioportal.legacy.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.cbioportal.legacy.model.DataAccessToken;
import org.cbioportal.legacy.model.DataAccessTokenSummary;
import org.cbioportal.legacy.service.DataAccessTokenService;
import org.cbioportal.legacy.service.exception.DataAccessTokenNoUserIdentityException;
import org.cbioportal.legacy.service.exception.DataAccessTokenProhibitedUserException;
import org.cbioportal.legacy.service.exception.TokenNotFoundException;
import org.cbioportal.legacy.utils.config.annotation.ConditionalOnProperty;
import org.cbioportal.legacy.web.config.annotation.InternalApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

@InternalApi
@Validated
@RestController
@ConditionalOnProperty(
    name = "dat.method",
    havingValue = "oauth2",
    isNot = true,
    matchIfMissing = true)
@Tag(name = "Data Access Tokens", description = " ")
public class DataAccessTokenController {

  private String userRoleToAccessToken;

  @Value("${download_group:}") // default is empty string
  public void setUserRoleToAccessToken(String property) {
    userRoleToAccessToken = property;
  }

  private final DataAccessTokenService tokenService;
  private final Set<String> usersWhoCannotUseTokenSet;

  private static final String FILE_NAME = "cbioportal_data_access_token.txt";

  @Autowired
  public DataAccessTokenController(
      DataAccessTokenService tokenService,
      @Value("${dat.unauth_users:anonymousUser}") String usersWhoCannotUseTokens) {
    this.tokenService = tokenService;
    if (Objects.isNull(usersWhoCannotUseTokens)) {
      usersWhoCannotUseTokens = "";
    }
    usersWhoCannotUseTokenSet = new HashSet<>(List.of((usersWhoCannotUseTokens.split(","))));
  }

  @RequestMapping(method = RequestMethod.GET, value = "/api/data-access-token")
  @Operation(description = "Create a new data access token and download it as a file")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content = @Content(schema = @Schema(implementation = String.class)))
  public ResponseEntity<String> downloadDataAccessToken(
      Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
    // for other methods add header to trigger download of the token by the browser
    response.setHeader("Content-Disposition", "attachment; filename=" + FILE_NAME);
    String userName = getAuthenticatedUser(authentication);
    DataAccessToken token = tokenService.createDataAccessToken(userName);
    if (token == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    return new ResponseEntity<>(token.toString(), HttpStatus.CREATED);
  }

  // retrieve and trigger download OAuth2 offline token
  @RequestMapping(method = RequestMethod.GET, value = "/api/data-access-token/oauth2")
  @Parameter(hidden = true)
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content = @Content(schema = @Schema(implementation = String.class)))
  public ResponseEntity<String> downloadOAuth2DataAccessToken(
      HttpServletRequest request, HttpServletResponse response) throws IOException {
    throw new UnsupportedOperationException(
        "this endpoint is only enabled when dat is set to oauth2.");
  }

  @RequestMapping(
      method = RequestMethod.POST,
      value = "/api/data-access-tokens",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Create a new data access token")
  @ApiResponse(
      responseCode = "201",
      description = "OK",
      content = @Content(schema = @Schema(implementation = DataAccessToken.class)))
  public ResponseEntity<DataAccessToken> createDataAccessToken(Authentication authentication)
      throws HttpClientErrorException {
    String userName = getAuthenticatedUser(authentication);
    DataAccessToken token = tokenService.createDataAccessToken(userName);
    if (token == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<>(token, HttpStatus.CREATED);
  }

  @RequestMapping(
      method = RequestMethod.GET,
      value = "/api/data-access-tokens",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Retrieve all data access tokens")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content =
          @Content(array = @ArraySchema(schema = @Schema(implementation = DataAccessToken.class))))
  public ResponseEntity<List<DataAccessToken>> getAllDataAccessTokens(
      HttpServletRequest request, Authentication authentication) {
    String userName = getAuthenticatedUser(authentication);
    List<DataAccessToken> allDataAccessTokens = tokenService.getAllDataAccessTokens(userName);
    return new ResponseEntity<>(allDataAccessTokens, HttpStatus.OK);
  }

  @RequestMapping(method = RequestMethod.GET, value = "/api/data-access-tokens/{token}")
  @Operation(description = "Retrieve an existing data access token")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content = @Content(schema = @Schema(implementation = DataAccessToken.class)))
  public ResponseEntity<DataAccessToken> getDataAccessToken(
      @Parameter(required = true, description = "token") @PathVariable String token,
      Authentication authentication) {
    String userName = getAuthenticatedUser(authentication);
    DataAccessToken dataAccessToken = getOwnedDataAccessToken(token, userName);
    return new ResponseEntity<>(dataAccessToken, HttpStatus.OK);
  }

  @RequestMapping(
      method = RequestMethod.GET,
      value = "/api/data-access-tokens/id",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      description = "Retrieve summaries of the user's data access tokens, with masked token values")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content =
          @Content(
              array =
                  @ArraySchema(schema = @Schema(implementation = DataAccessTokenSummary.class))))
  public ResponseEntity<List<DataAccessTokenSummary>> getAllDataAccessTokenSummaries(
      Authentication authentication) {
    String userName = getAuthenticatedUser(authentication);
    List<DataAccessTokenSummary> summaries =
        tokenService.getAllDataAccessTokens(userName).stream()
            .map(DataAccessTokenSummary::fromToken)
            .toList();
    return new ResponseEntity<>(summaries, HttpStatus.OK);
  }

  @RequestMapping(method = RequestMethod.DELETE, value = "/api/data-access-tokens")
  @Operation(description = "Delete all data access tokens")
  public void revokeAllDataAccessTokens(Authentication authentication) {
    tokenService.revokeAllDataAccessTokens(getAuthenticatedUser(authentication));
  }

  @RequestMapping(method = RequestMethod.DELETE, value = "/api/data-access-tokens/{token}")
  @Operation(description = "Delete a data access token")
  public void revokeDataAccessToken(
      @Parameter(required = true, description = "token") @PathVariable String token,
      Authentication authentication) {
    String userName = getAuthenticatedUser(authentication);
    getOwnedDataAccessToken(token, userName);
    tokenService.revokeDataAccessToken(token);
  }

  @RequestMapping(method = RequestMethod.DELETE, value = "/api/data-access-tokens/id/{id}")
  @Operation(description = "Delete a data access token by its id")
  public void revokeDataAccessTokenById(
      @Parameter(required = true, description = "id of the token, as returned in its summary")
          @PathVariable
          String id,
      Authentication authentication) {
    String userName = getAuthenticatedUser(authentication);
    DataAccessToken dataAccessToken =
        tokenService.getAllDataAccessTokens(userName).stream()
            .filter(t -> DataAccessTokenSummary.computeId(t.getToken()).equals(id))
            .findFirst()
            .orElseThrow(
                () -> new TokenNotFoundException("Specified token id " + id + " does not exist"));
    tokenService.revokeDataAccessToken(dataAccessToken.getToken());
  }

  // Tokens belonging to other users are reported as not found so their existence is not revealed.
  private DataAccessToken getOwnedDataAccessToken(String token, String userName) {
    DataAccessToken dataAccessToken = tokenService.getDataAccessTokenInfo(token);
    if (dataAccessToken == null || !userName.equals(dataAccessToken.getUsername())) {
      throw new TokenNotFoundException("Specified token " + token + " does not exist");
    }
    return dataAccessToken;
  }

  private String getAuthenticatedUser(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new DataAccessTokenNoUserIdentityException();
    }
    String username = authentication.getName();
    if (usersWhoCannotUseTokenSet.contains(username)) {
      throw new DataAccessTokenProhibitedUserException();
    }
    if (StringUtils.isNotEmpty(userRoleToAccessToken)
        && !authentication
            .getAuthorities()
            .contains(new SimpleGrantedAuthority(userRoleToAccessToken))) {
      throw new DataAccessTokenProhibitedUserException();
    }
    return username;
  }
}
