package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.Collection;
import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.service.MutationEnrichmentService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.web.parameter.EnrichmentType;
import org.cbioportal.web.parameter.MolecularProfileCasesGroup;
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

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.RequestAttribute;
import springfox.documentation.annotations.ApiIgnore;

@InternalApi
@RestController
@Validated
@Api(tags = "Mutation Enrichments", description = " ")
public class MutationEnrichmentController {

    @Autowired
    private MutationEnrichmentService mutationEnrichmentService;

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', 'read')")
    @RequestMapping(value = "/mutation-enrichments/fetch",
        method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch mutation enrichments in a molecular profile")
    public ResponseEntity<List<AlterationEnrichment>> fetchMutationEnrichments(
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @RequestAttribute(required = false, value = "interceptedGroups") List<MolecularProfileCasesGroup> interceptedGroups,
        @ApiParam("Type of the enrichment e.g. SAMPLE or PATIENT")
        @RequestParam(defaultValue = "SAMPLE") EnrichmentType enrichmentType,
        @ApiParam(required = true, value = "List of groups")
        @Valid @RequestBody(required = false)List<MolecularProfileCasesGroup> groups) throws MolecularProfileNotFoundException {

        Map<String, List<MolecularProfileCaseIdentifier>> groupCaseIdentifierSet = interceptedGroups.stream()
                .collect(Collectors.toMap(MolecularProfileCasesGroup::getName,
                        MolecularProfileCasesGroup::getMolecularProfileCaseIdentifiers));

        return new ResponseEntity<>(
                mutationEnrichmentService.getMutationEnrichments(groupCaseIdentifierSet, enrichmentType.name()),
                HttpStatus.OK);
    }
}
