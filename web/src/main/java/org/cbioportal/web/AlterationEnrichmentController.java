package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.cbioportal.model.*;
import org.cbioportal.model.util.Select;
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
import java.util.stream.Stream;

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

        Stream<MutationEventType> selectedMutations = alterationEventTypes.getMutationEventTypes().entrySet().stream()
            .filter(e -> e.getValue())
            .map(e -> e.getKey());
        Select<MutationEventType> mutationEventTypes = allOptionsSelected(alterationEventTypes.getMutationEventTypes()) ?
            Select.all() : Select.byValues(selectedMutations);

        Stream<CNA> selectedCnas = alterationEventTypes.getCopyNumberAlterationEventTypes().entrySet().stream()
            .filter(e -> e.getValue())
            .map(e -> e.getKey());
        Select<CNA> cnaEventTypes = allOptionsSelected(alterationEventTypes.getCopyNumberAlterationEventTypes()) ?
            Select.all() : Select.byValues(selectedCnas);

        List<AlterationEnrichment> alterationEnrichments = alterationEnrichmentService.getAlterationEnrichments(
            groupCaseIdentifierSet,
            mutationEventTypes,
            cnaEventTypes,
            enrichmentType);

        return new ResponseEntity<>(alterationEnrichments, HttpStatus.OK);
    }
    
    private boolean allOptionsSelected(Map<?, Boolean> options) {
        return options.entrySet().stream().allMatch(e -> e.getValue());
    }
}

