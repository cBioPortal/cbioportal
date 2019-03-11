package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.Collection;
import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.service.CopyNumberEnrichmentService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.web.parameter.CopyNumberEnrichmentEventType;
import org.cbioportal.web.parameter.MultipleStudiesEnrichmentFilter;
import org.cbioportal.web.parameter.EnrichmentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestAttribute;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;

@InternalApi
@RestController
@Validated
@Api(tags = "Copy Number Enrichments", description = " ")
public class CopyNumberEnrichmentController {

    @Autowired
    private CopyNumberEnrichmentService copyNumberEnrichmentService;

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', 'read')")
    @RequestMapping(value = "/copy-number-enrichments/fetch",
        method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch copy number enrichments in a molecular profile")
    public ResponseEntity<List<AlterationEnrichment>> fetchCopyNumberEnrichments(
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @RequestAttribute(required = false, value = "interceptedMultiStudyEnrichmentFilter") MultipleStudiesEnrichmentFilter interceptedMultiStudyEnrichmentFilter,
        @ApiParam("Type of the copy number event")
        @RequestParam(defaultValue = "HOMDEL") CopyNumberEnrichmentEventType copyNumberEventType,
        @ApiParam("Type of the enrichment e.g. SAMPLE or PATIENT")
        @RequestParam(defaultValue = "SAMPLE") EnrichmentType enrichmentType,
        @ApiParam(required = true, value = "List of entities")
        @Valid @RequestBody(required = false) MultipleStudiesEnrichmentFilter multipleStudiesEnrichmentFilter) throws MolecularProfileNotFoundException {
        return new ResponseEntity<>(
                copyNumberEnrichmentService.getCopyNumberEnrichments(interceptedMultiStudyEnrichmentFilter.getMolecularProfileCaseSet1(),
                        interceptedMultiStudyEnrichmentFilter.getMolecularProfileCaseSet2(), copyNumberEventType.getAlterationTypes(), enrichmentType.name()),
                HttpStatus.OK);
    }
}
