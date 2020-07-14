package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.model.CNA;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.service.CopyNumberEnrichmentService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.model.EnrichmentType;
import org.cbioportal.web.parameter.MolecularProfileCasesGroupFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        @Valid @RequestAttribute(required = false, value = "interceptedMolecularProfileCasesGroupFilters") List<MolecularProfileCasesGroupFilter> interceptedMolecularProfileCasesGroupFilters,
        @ApiParam("Type of the copy number event")
        @RequestParam(defaultValue = "HOMDEL") CNA copyNumberEventType,
        @ApiParam("Type of the enrichment e.g. SAMPLE or PATIENT")
        @RequestParam(defaultValue = "SAMPLE") EnrichmentType enrichmentType,
        @ApiParam(required = true, value = "List of groups containing sample identifiers")
        @Valid @RequestBody(required = false) List<MolecularProfileCasesGroupFilter> groups) throws MolecularProfileNotFoundException {

        Map<String, List<MolecularProfileCaseIdentifier>> groupCaseIdentifierSet = interceptedMolecularProfileCasesGroupFilters.stream()
                .collect(Collectors.toMap(MolecularProfileCasesGroupFilter::getName,
                        MolecularProfileCasesGroupFilter::getMolecularProfileCaseIdentifiers));

        return new ResponseEntity<>(
            copyNumberEnrichmentService.getCopyNumberEnrichments(
                groupCaseIdentifierSet,
                copyNumberEventType,
                enrichmentType), HttpStatus.OK);
    }
}
