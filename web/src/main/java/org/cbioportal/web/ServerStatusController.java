package org.cbioportal.web;

import org.cbioportal.service.ServerStatusService;
import org.cbioportal.service.impl.ServerStatusServiceImpl.ServerStatusMessage;
import org.cbioportal.web.config.annotation.InternalApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@InternalApi
@RestController
@Validated
@Api(tags = "Server running status", description = "This end point does not require authentication")
public class ServerStatusController {

    @Autowired
    private ServerStatusService serverStatusService;

    @RequestMapping(value = "/health", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get the running status of the server")
    public ResponseEntity<ServerStatusMessage> getServerStatus() {
        return new ResponseEntity<>(serverStatusService.getServerStatus(), HttpStatus.OK);
    }

}
