package org.cbioportal.web;

import java.util.*;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.cbioportal.model.ExpressionEnrichment;
import org.cbioportal.model.GenericAssayEnrichment;
import org.cbioportal.model.GenomicEnrichment;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.service.ExpressionEnrichmentService;
import org.cbioportal.service.exception.GenericAssayNotFoundException;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.web.parameter.EnrichmentType;
import org.cbioportal.web.parameter.MolecularProfileCasesGroupFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import springfox.documentation.annotations.ApiIgnore;

@InternalApi
@RestController
@Validated
@Api(tags = "Enrichments", description = " ")
public class ExpressionEnrichmentController {
    @Autowired
    private ExpressionEnrichmentService expressionEnrichmentService;
    
    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', 'read')")
    @RequestMapping(value = "/expression-enrichments/fetch",
        method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch genomic enrichments in a molecular profile")
    public ResponseEntity<List<GenomicEnrichment>> fetchGenomicEnrichments(
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @ApiParam("Type of the enrichment e.g. SAMPLE or PATIENT")
        @RequestParam(defaultValue = "SAMPLE") EnrichmentType enrichmentType,
        @ApiParam(required = true, value = "List of groups containing sample and molecular profile identifiers")
        @Valid @RequestBody(required = false) List<MolecularProfileCasesGroupFilter> groups,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid @RequestAttribute(required = false, value = "interceptedMolecularProfileCasesGroupFilters") List<MolecularProfileCasesGroupFilter> interceptedMolecularProfileCasesGroupFilters) throws MolecularProfileNotFoundException {

        return new ResponseEntity<>(
                fetchExpressionEnrichments(enrichmentType, interceptedMolecularProfileCasesGroupFilters, false),
                HttpStatus.OK);
    }

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', 'read')")
    @RequestMapping(value = "/generic-assay-enrichments/fetch",
        method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch generic assay enrichments in a molecular profile")
    public ResponseEntity<List<GenericAssayEnrichment>> fetchGenericAssayEnrichments(
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @ApiParam("Type of the enrichment e.g. SAMPLE or PATIENT")
        @RequestParam(defaultValue = "SAMPLE") EnrichmentType enrichmentType,
        @ApiParam(required = true, value = "List of groups containing sample and molecular profile identifiers")
        @Valid @RequestBody(required = false) List<MolecularProfileCasesGroupFilter> groups,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid @RequestAttribute(required = false, value = "interceptedMolecularProfileCasesGroupFilters") List<MolecularProfileCasesGroupFilter> interceptedMolecularProfileCasesGroupFilters)
                throws MolecularProfileNotFoundException, UnsupportedOperationException, GenericAssayNotFoundException {

        return new ResponseEntity<>(
                fetchExpressionEnrichments(enrichmentType, interceptedMolecularProfileCasesGroupFilters, true),
                HttpStatus.OK);
    }

    private <S extends ExpressionEnrichment> List<S> fetchExpressionEnrichments(EnrichmentType enrichmentType,
            List<MolecularProfileCasesGroupFilter> interceptedMolecularProfileCasesGroupFilters,
            Boolean isRequestForGenericAssayEnrichments) throws MolecularProfileNotFoundException {

        Map<String, List<MolecularProfileCaseIdentifier>> groupCaseIdentifierSet = interceptedMolecularProfileCasesGroupFilters
                .stream().collect(Collectors.toMap(MolecularProfileCasesGroupFilter::getName,
                        MolecularProfileCasesGroupFilter::getMolecularProfileCaseIdentifiers));

        Set<String> molecularProfileIds = groupCaseIdentifierSet.values().stream()
                .flatMap(molecularProfileCaseSet -> molecularProfileCaseSet.stream()
                        .map(MolecularProfileCaseIdentifier::getMolecularProfileId))
                .collect(Collectors.toSet());

        if (molecularProfileIds.size() > 1) {
            throw new UnsupportedOperationException("Multi-study expression enrichments is not yet implemented");
        }

        if (isRequestForGenericAssayEnrichments) {
            return (List<S>) expressionEnrichmentService.getGenericAssayEnrichments(
                    molecularProfileIds.iterator().next(), groupCaseIdentifierSet, enrichmentType.name());
        }

        return (List<S>) expressionEnrichmentService.getGenomicEnrichments(molecularProfileIds.iterator().next(),
                groupCaseIdentifierSet, enrichmentType.name());
    }
}
