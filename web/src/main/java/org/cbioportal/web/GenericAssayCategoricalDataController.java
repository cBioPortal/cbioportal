package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.cbioportal.model.EnrichmentType;
import org.cbioportal.model.GenericAssayData;
import org.cbioportal.model.GenericAssayCategoricalEnrichment;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.service.GenericAssayCategoricalDataService;
import org.cbioportal.service.GenericAssayService;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.exception.GenericAssayNotFoundException;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.web.parameter.GenericAssayFilter;
import org.cbioportal.web.parameter.HeaderKeyConstants;
import org.cbioportal.web.parameter.MolecularProfileCasesGroupFilter;
import org.cbioportal.web.parameter.Projection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
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
@Api(tags = "Generic Assay Categorical Data", description = " ")
public class GenericAssayCategoricalDataController {

    @Autowired
    private GenericAssayCategoricalDataService genericAssayCategoricalDataService;

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/generic-assay-enrichments/categorical/fetch",
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
            genericAssayCategoricalDataService.getGenericAssayCategoricalEnrichments(
                molecularProfileIds.iterator().next(), groupCaseIdentifierSet, enrichmentType),
            HttpStatus.OK);
    }

}
