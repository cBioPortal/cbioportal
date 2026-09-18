package org.cbioportal.legacy.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.cbioportal.legacy.service.DatabaseSwitchService;
import org.cbioportal.legacy.service.exception.CacheOperationException;
import org.cbioportal.legacy.service.exception.DatabaseSwitchException;
import org.cbioportal.legacy.web.config.annotation.InternalApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@InternalApi
@Tag(name = "Database")
public class DatabaseController {

  @Autowired private DatabaseSwitchService databaseSwitchService;

  @Value("${database.endpoint.api-key:not set}")
  private String requiredApiKey;

  @Value("${database.endpoint.enabled:false}")
  private boolean databaseEndpointEnabled;

  @RequestMapping(
      value = "/api/database",
      method = RequestMethod.GET,
      produces = MediaType.TEXT_PLAIN_VALUE)
  @Operation(summary = "Get the database currently in use")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content = @Content(schema = @Schema(implementation = String.class)))
  public ResponseEntity<String> getActiveDatabase(
      @Parameter(
              description =
                  "Secret API key passed in HTTP header. The key is configured in application.properties of the portal instance.")
          @RequestHeader(value = "X-API-KEY")
          String providedApiKey) {
    if (!databaseEndpointEnabled) {
      return new ResponseEntity<>(
          "Database endpoint is disabled for this instance.", HttpStatus.NOT_FOUND);
    }
    if ("not set".equals(requiredApiKey) || !requiredApiKey.equals(providedApiKey)) {
      return new ResponseEntity<>("", HttpStatus.UNAUTHORIZED);
    }
    String activeDatabase = databaseSwitchService.getActiveDatabase();
    return new ResponseEntity<>(
        activeDatabase == null ? "(default from spring.datasource.url)" : activeDatabase,
        HttpStatus.OK);
  }

  @RequestMapping(
      value = "/api/database",
      method = RequestMethod.PUT,
      produces = MediaType.TEXT_PLAIN_VALUE)
  @Operation(summary = "Point this running instance at a different database, without restarting")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content = @Content(schema = @Schema(implementation = String.class)))
  public ResponseEntity<String> switchDatabase(
      @Parameter(
              description =
                  "Secret API key passed in HTTP header. The key is configured in application.properties of the portal instance.")
          @RequestHeader(value = "X-API-KEY")
          String providedApiKey,
      @Parameter(description = "Name of the database to switch to") @RequestParam String database)
      throws DatabaseSwitchException, CacheOperationException {
    if (!databaseEndpointEnabled) {
      return new ResponseEntity<>(
          "Database endpoint is disabled for this instance.", HttpStatus.NOT_FOUND);
    }
    if ("not set".equals(requiredApiKey) || !requiredApiKey.equals(providedApiKey)) {
      return new ResponseEntity<>("", HttpStatus.UNAUTHORIZED);
    }
    try {
      databaseSwitchService.switchDatabase(database);
    } catch (IllegalArgumentException e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
    return new ResponseEntity<>("Now using database: " + database, HttpStatus.OK);
  }
}
