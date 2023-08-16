package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.cbioportal.model.EnrichmentType;
import org.cbioportal.model.GenericAssayBinaryEnrichment;
import org.cbioportal.model.GenericAssayCategoricalEnrichment;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.service.ExpressionEnrichmentService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.web.parameter.MolecularProfileCasesGroupFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestAttribute;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@InternalApi
@RestController
@Validated
@Api(tags = "Generic Assay Enrichment Data", description = " ")
public class GenericAssayEnrichmentController {
    @Autowired
    private ExpressionEnrichmentService expressionEnrichmentService;

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/generic-assay-categorical-enrichments/fetch",
        method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch generic assay categorical data enrichments in a molecular profile")
    public ResponseEntity<List<GenericAssayCategoricalEnrichment>> fetchGenericAssayCategoricalDataEnrichmentInMultipleMolecularProfiles(
        @ApiIgnore
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @ApiParam("Type of the enrichment e.g. SAMPLE or PATIENT")
        @RequestParam(defaultValue = "SAMPLE") EnrichmentType enrichmentType,
        @ApiParam(required = true, value = "List of groups containing sample and molecular profile identifiers")
        @Valid @RequestBody(required = false) List<MolecularProfileCasesGroupFilter> groups,
        @ApiIgnore
        @Valid @RequestAttribute(required = false, value = "interceptedMolecularProfileCasesGroupFilters") List<MolecularProfileCasesGroupFilter> interceptedMolecularProfileCasesGroupFilters)
        throws MolecularProfileNotFoundException, UnsupportedOperationException {

        return new ResponseEntity<>(fetchExpressionEnrichments(enrichmentType, interceptedMolecularProfileCasesGroupFilters),
            HttpStatus.OK);
    }

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/generic-assay-binary-enrichments/fetch",
        method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch generic assay binary data enrichments in a molecular profile")
    public ResponseEntity<List<GenericAssayBinaryEnrichment>> fetchGenericAssayBinaryDataEnrichmentInMultipleMolecularProfiles(
        @ApiIgnore
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @ApiParam("Type of the enrichment e.g. SAMPLE or PATIENT")
        @RequestParam(defaultValue = "SAMPLE") EnrichmentType enrichmentType,
        @ApiParam(required = true, value = "List of groups containing sample and molecular profile identifiers")
        @Valid @RequestBody(required = false) List<MolecularProfileCasesGroupFilter> groups,
        @ApiIgnore
        @Valid @RequestAttribute(required = false, value = "interceptedMolecularProfileCasesGroupFilters") List<MolecularProfileCasesGroupFilter> interceptedMolecularProfileCasesGroupFilters)
        throws MolecularProfileNotFoundException, UnsupportedOperationException {

        Map<String, List<MolecularProfileCaseIdentifier>> groupCaseIdentifierSet = interceptedMolecularProfileCasesGroupFilters
            .stream().collect(Collectors.toMap(MolecularProfileCasesGroupFilter::getName,
                MolecularProfileCasesGroupFilter::getMolecularProfileCaseIdentifiers));

        Set<String> molecularProfileIds = groupCaseIdentifierSet.values().stream()
            .flatMap(molecularProfileCaseSet -> molecularProfileCaseSet.stream()
                .map(MolecularProfileCaseIdentifier::getMolecularProfileId))
            .collect(Collectors.toSet());

        if (molecularProfileIds.size() > 1) {
            throw new UnsupportedOperationException("Multi-study enrichments is not yet implemented");
        }

        return new ResponseEntity<>(
            expressionEnrichmentService.getGenericAssayBinaryEnrichments(
                molecularProfileIds.iterator().next(), groupCaseIdentifierSet, enrichmentType),
            HttpStatus.OK);
    }

    private List<GenericAssayCategoricalEnrichment> fetchExpressionEnrichments(EnrichmentType enrichmentType,
                                                                               List<MolecularProfileCasesGroupFilter> interceptedMolecularProfileCasesGroupFilters
    ) throws MolecularProfileNotFoundException {
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
        return expressionEnrichmentService.getGenericAssayCategoricalEnrichments(
            molecularProfileIds.iterator().next(), groupCaseIdentifierSet, enrichmentType);
    }
}
