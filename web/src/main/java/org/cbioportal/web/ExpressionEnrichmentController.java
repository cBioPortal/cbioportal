package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.*;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.cbioportal.model.ExpressionEnrichment;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.service.ExpressionEnrichmentService;
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
@Api(tags = "Expression Enrichments", description = " ")
public class ExpressionEnrichmentController {
    @Autowired
    private ExpressionEnrichmentService expressionEnrichmentService;

    @PreAuthorize(
        "hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', 'read')"
    )
    @RequestMapping(
        value = "/expression-enrichments/fetch",
        method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiOperation("Fetch expression enrichments in a molecular profile")
    public ResponseEntity<List<ExpressionEnrichment>> fetchExpressionEnrichments(
        @ApiIgnore @RequestAttribute( // prevent reference to this attribute in the swagger-ui interface
            required = false,
            value = "involvedCancerStudies"
        ) Collection<String> involvedCancerStudies,
        @ApiParam(
            "Type of the enrichment e.g. SAMPLE or PATIENT"
        ) @RequestParam(defaultValue = "SAMPLE") EnrichmentType enrichmentType,
        @ApiParam(
            required = true,
            value = "List of groups containing sample and molecular profile identifiers"
        ) @Valid @RequestBody(
            required = false
        ) List<MolecularProfileCasesGroupFilter> groups,
        @ApiIgnore @Valid @RequestAttribute( // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
            required = false,
            value = "interceptedMolecularProfileCasesGroupFilters"
        ) List<MolecularProfileCasesGroupFilter> interceptedMolecularProfileCasesGroupFilters
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

        Set<String> molecularProfileIds = groupCaseIdentifierSet
            .values()
            .stream()
            .flatMap(
                molecularProfileCaseSet ->
                    molecularProfileCaseSet
                        .stream()
                        .map(
                            MolecularProfileCaseIdentifier::getMolecularProfileId
                        )
            )
            .collect(Collectors.toSet());

        if (molecularProfileIds.size() > 1) {
            throw new UnsupportedOperationException(
                "Multi-study expression enrichments is not yet implemented"
            );
        }

        return new ResponseEntity<>(
            expressionEnrichmentService.getExpressionEnrichments(
                molecularProfileIds.iterator().next(),
                groupCaseIdentifierSet,
                enrichmentType.name()
            ),
            HttpStatus.OK
        );
    }
}
