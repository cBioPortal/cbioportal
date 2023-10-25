package org.cbioportal.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import jakarta.validation.Valid; import java.util.*;
import java.util.stream.Collectors;

@InternalApi
@RestController()
@RequestMapping("/api")
@Validated
@Tag(name = "Alteration Enrichments", description = " ")
public class AlterationEnrichmentController {

    @Autowired
    private AlterationEnrichmentService alterationEnrichmentService;

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @PostMapping(value = "/alteration-enrichments/fetch",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary ="Fetch alteration enrichments in molecular profiles")
    public ResponseEntity<List<AlterationEnrichment>> fetchAlterationEnrichments(
        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @Parameter(hidden = true)
        // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid @RequestAttribute(required = false, value = "interceptedMolecularProfileCasesGroupFilters") List<MolecularProfileCasesGroupFilter> interceptedMolecularProfileCasesGroupFilters,
        @Parameter(hidden = true)
        @Valid @RequestAttribute(required = false, value = "alterationEventTypes") AlterationFilter alterationEventTypes,
        @Parameter(description = "Type of the enrichment e.g. SAMPLE or PATIENT")
        @RequestParam(defaultValue = "SAMPLE") EnrichmentType enrichmentType,
        @Parameter(required = true, description = "List of groups containing sample identifiers and list of Alteration Types")
        @Valid @RequestBody(required = false) MolecularProfileCasesGroupAndAlterationTypeFilter groupsAndAlterationTypes) throws MolecularProfileNotFoundException {

        Map<String, List<MolecularProfileCaseIdentifier>> groupCaseIdentifierSet = interceptedMolecularProfileCasesGroupFilters.stream()
            .collect(Collectors.toMap(MolecularProfileCasesGroupFilter::getName,
                MolecularProfileCasesGroupFilter::getMolecularProfileCaseIdentifiers));

        List<AlterationEnrichment> alterationEnrichments = alterationEnrichmentService.getAlterationEnrichments(
            groupCaseIdentifierSet,
            enrichmentType,
            alterationEventTypes);

        return new ResponseEntity<>(alterationEnrichments, HttpStatus.OK);
    }
}

