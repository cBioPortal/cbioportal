package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.cbioportal.model.*;
import org.cbioportal.service.AlterationEnrichmentService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.web.parameter.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@InternalApi
@RestController
@Validated
@Api(tags = "Alteration Enrichments", description = " ")
public class AlterationEnrichmentController {

    @Autowired
    private AlterationEnrichmentService alterationEnrichmentService;

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', 'read')")
    @PostMapping(value = "/alteration-enrichments/fetch",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch alteration enrichments in molecular profiles")
    public ResponseEntity<List<AlterationEnrichment>> fetchAlterationEnrichments(
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @ApiIgnore
        // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid @RequestAttribute(required = false, value = "interceptedMolecularProfileCasesGroupFilters") List<MolecularProfileCasesGroupFilter> interceptedMolecularProfileCasesGroupFilters,
        @ApiIgnore
        @Valid @RequestAttribute(required = false, value = "alterationEventTypes") AlterationEventTypeFilter alterationEventTypes,
        @ApiParam("Type of the enrichment e.g. SAMPLE or PATIENT")
        @RequestParam(defaultValue = "SAMPLE") EnrichmentType enrichmentType,
        @ApiParam(required = true, value = "List of groups containing sample identifiers and list of Alteration Types")
        @Valid @RequestBody(required = false) MolecularProfileCasesGroupAndAlterationTypeFilter groupsAndAlterationTypes) throws MolecularProfileNotFoundException {

        Map<String, List<MolecularProfileCaseIdentifier>> groupCaseIdentifierSet = interceptedMolecularProfileCasesGroupFilters.stream()
            .collect(Collectors.toMap(MolecularProfileCasesGroupFilter::getName,
                MolecularProfileCasesGroupFilter::getMolecularProfileCaseIdentifiers));

        List<AlterationEnrichment> alterationEnrichments = alterationEnrichmentService.getAlterationEnrichments(
            groupCaseIdentifierSet,
            alterationEventTypes.getMutationTypeSelect(),
            alterationEventTypes.getCNAEventTypeSelect(),
            enrichmentType);

        return new ResponseEntity<>(alterationEnrichments, HttpStatus.OK);
    }
}

