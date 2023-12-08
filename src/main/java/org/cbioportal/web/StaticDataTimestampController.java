package org.cbioportal.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@InternalApi
@RestController
@Validated
@Tag(name = "Timestamps", description = " ")
public class StaticDataTimestampController {
    public static final List<String> TIMESTAMP_TABLES = Arrays.asList("gene", "reference_genome_gene");
    @Autowired
    private StaticDataTimestampService service;
    
    @RequestMapping(value = "/api/timestamps", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get the last time each static resource was updated")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(schema = @Schema(implementation = Object.class)))
    public ResponseEntity<Map<String, String>> getAllTimestamps() {
        return new ResponseEntity<>(service.getTimestamps(TIMESTAMP_TABLES), HttpStatus.OK);
    }
    
}
