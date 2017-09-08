package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.cbioportal.model.Info;
import org.cbioportal.web.config.annotation.InternalApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@InternalApi
@RestController
@Validated
@Api(tags = "Info", description = " ")
public class InfoController {

    @Value("${portal.version}")
    private String portalVersion;
    
    @Value("${db.version}")
    private String dbVersion;

    @RequestMapping(value = "/info", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get information about the running instance")
    public ResponseEntity<Info> getInfo() {
        
        Info info = new Info();
        info.setPortalVersion(portalVersion);
        info.setDbVersion(dbVersion);
        return new ResponseEntity<>(info, HttpStatus.OK);
    }
}
