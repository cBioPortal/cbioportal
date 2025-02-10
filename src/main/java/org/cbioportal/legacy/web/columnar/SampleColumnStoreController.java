package org.cbioportal.legacy.web.columnar;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.cbioportal.legacy.model.Sample;
import org.cbioportal.legacy.service.SampleColumnarService;
import org.cbioportal.legacy.service.exception.SampleListNotFoundException;
import org.cbioportal.legacy.utils.config.annotation.ConditionalOnProperty;
import org.cbioportal.legacy.web.parameter.Projection;
import org.cbioportal.legacy.web.parameter.SampleFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

@RestController()
@RequestMapping("/api")
@Validated
@ConditionalOnProperty(name = "clickhouse_mode", havingValue = "true")
public class SampleColumnStoreController {
    @Autowired
    private SampleColumnarService sampleService;
    
    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
    @RequestMapping(
        value = "/column-store/samples/fetch",
        method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(description = "Fetch samples by ID")
    @ApiResponse(
        responseCode = "200",
        description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = Sample.class)))
    )
    public ResponseEntity<List<Sample>> fetchSamples(
        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies")
        Collection<String> involvedCancerStudies,
        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid
        @RequestAttribute(required = false, value = "interceptedSampleFilter")
        SampleFilter interceptedSampleFilter,
        @Parameter(required = true, description = "List of sample identifiers")
        @Valid
        @RequestBody(required = false)
        SampleFilter sampleFilter,
        @Parameter(description = "Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY")
        Projection projection
    ) throws SampleListNotFoundException {
        if (projection == Projection.META) {
            HttpHeaders responseHeaders = sampleService.fetchMetaSamples(interceptedSampleFilter);
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        }
        else {
            List<Sample> samples = sampleService.fetchSamples(interceptedSampleFilter, projection.name());
            return new ResponseEntity<>(samples, HttpStatus.OK);
        }
    }
}
