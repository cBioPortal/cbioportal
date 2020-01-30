package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.service.CopyNumberEnrichmentService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.web.parameter.CopyNumberEnrichmentEventType;
import org.cbioportal.web.parameter.EnrichmentType;
import org.cbioportal.web.parameter.MolecularProfileCasesGroupFilter;
import org.springframework.beans.factory.annotation.Autowired;
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
import springfox.documentation.annotations.ApiIgnore;

@InternalApi
@RestController
@Validated
@Api(tags = "Copy Number Enrichments", description = " ")
public class CopyNumberEnrichmentController {
    @Autowired
    private CopyNumberEnrichmentService copyNumberEnrichmentService;

    @PreAuthorize(
        "hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', 'read')"
    )
    @RequestMapping(
        value = "/copy-number-enrichments/fetch",
        method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiOperation("Fetch copy number enrichments in a molecular profile")
    public ResponseEntity<List<AlterationEnrichment>> fetchCopyNumberEnrichments(
        @ApiIgnore @RequestAttribute( // prevent reference to this attribute in the swagger-ui interface
            required = false,
            value = "involvedCancerStudies"
        ) Collection<String> involvedCancerStudies,
        @ApiIgnore @Valid @RequestAttribute( // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
            required = false,
            value = "interceptedMolecularProfileCasesGroupFilters"
        ) List<MolecularProfileCasesGroupFilter> interceptedMolecularProfileCasesGroupFilters,
        @ApiParam("Type of the copy number event") @RequestParam(
            defaultValue = "HOMDEL"
        ) CopyNumberEnrichmentEventType copyNumberEventType,
        @ApiParam(
            "Type of the enrichment e.g. SAMPLE or PATIENT"
        ) @RequestParam(defaultValue = "SAMPLE") EnrichmentType enrichmentType,
        @ApiParam(
            required = true,
            value = "List of groups containing sample identifiers"
        ) @Valid @RequestBody(
            required = false
        ) List<MolecularProfileCasesGroupFilter> groups
    )
        throws MolecularProfileNotFoundException {
        Map<String, List<MolecularProfileCaseIdentifier>> groupCaseIdentifierSet = interceptedMolecularProfileCasesGroupFilters
            .stream()
            .collect(
                Collectors.toMap(
                    MolecularProfileCasesGroupFilter::getName,
                    MolecularProfileCasesGroupFilter::getMolecularProfileCaseIdentifiers
                )
            );

        return new ResponseEntity<>(
            copyNumberEnrichmentService.getCopyNumberEnrichments(
                groupCaseIdentifierSet,
                copyNumberEventType.getAlterationTypes(),
                enrichmentType.name()
            ),
            HttpStatus.OK
        );
    }
}
