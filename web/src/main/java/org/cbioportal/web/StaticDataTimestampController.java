package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.cbioportal.model.TableTimestampPair;
import org.cbioportal.service.StaticDataTimestampService;
import org.cbioportal.web.config.annotation.InternalApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@InternalApi
@RestController
@Validated
@Api(tags = "Timestamps", description = " ")
public class StaticDataTimestampController {
    private static final List<String> tables = Arrays.asList("gene", "reference_genome_gene");
    @Autowired
    private StaticDataTimestampService service;
    
    @RequestMapping(value = "/timestamps", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get the last time each static resource was updated")
    public ResponseEntity<Map<String, String>> getAllTimestamps() {
        return new ResponseEntity<>(service.getTimestamps(tables), HttpStatus.OK);
    }
    
}
