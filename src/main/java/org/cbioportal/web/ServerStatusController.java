package org.cbioportal.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.cbioportal.service.ServerStatusService;
import org.cbioportal.service.impl.ServerStatusServiceImpl.ServerStatusMessage;
import org.cbioportal.web.config.annotation.PublicApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@PublicApi
@RestController
@Validated
@Tag(name = "Server running status", description = "This end point does not require authentication")
public class ServerStatusController {

    @Autowired
    private ServerStatusService serverStatusService;

    @RequestMapping(value = "/api/health", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get the running status of the server")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(schema = @Schema(implementation = ServerStatusMessage.class)))
    public ResponseEntity<ServerStatusMessage> getServerStatus() {
        return new ResponseEntity<>(serverStatusService.getServerStatus(), HttpStatus.OK);
    }

}
