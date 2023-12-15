package org.cbioportal.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.cbioportal.model.DiscreteCopyNumberData;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.DiscreteCopyNumberService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.web.config.PublicApiTags;
import org.cbioportal.web.config.annotation.PublicApi;
import org.cbioportal.web.parameter.DiscreteCopyNumberEventType;
import org.cbioportal.web.parameter.DiscreteCopyNumberFilter;
import org.cbioportal.web.parameter.HeaderKeyConstants;
import org.cbioportal.web.parameter.Projection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@PublicApi
@RestController()
@RequestMapping("/api")
@Validated
@Tag(name = PublicApiTags.DISCRETE_COPY_NUMBER_ALTERATIONS, description = " ")
public class DiscreteCopyNumberController {

    @Autowired
    private DiscreteCopyNumberService discreteCopyNumberService;

    @PreAuthorize("hasPermission(#molecularProfileId, 'MolecularProfileId', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/molecular-profiles/{molecularProfileId}/discrete-copy-number", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get discrete copy number alterations in a molecular profile")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = DiscreteCopyNumberData.class))))
    public ResponseEntity<List<DiscreteCopyNumberData>> getDiscreteCopyNumbersInMolecularProfile(
        @Parameter(required = true, description = "Molecular Profile ID e.g. acc_tcga_gistic")
        @PathVariable String molecularProfileId,
        @Parameter(required = true, description = "Sample List ID e.g. acc_tcga_all")
        @RequestParam String sampleListId,
        @Parameter(description = "Type of the copy number event")
        @RequestParam(defaultValue = "HOMDEL_AND_AMP") DiscreteCopyNumberEventType discreteCopyNumberEventType,
        @Parameter(description = "Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection) throws MolecularProfileNotFoundException {

        if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, discreteCopyNumberService
                .getMetaDiscreteCopyNumbersInMolecularProfileBySampleListId(molecularProfileId, sampleListId, null,
                    discreteCopyNumberEventType.getAlterationTypes()).getTotalCount().toString());
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(
                discreteCopyNumberService.getDiscreteCopyNumbersInMolecularProfileBySampleListId(molecularProfileId,
                    sampleListId, null, discreteCopyNumberEventType.getAlterationTypes(), projection.name()),
                HttpStatus.OK);
        }
    }

    @PreAuthorize("hasPermission(#molecularProfileId, 'MolecularProfileId', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/molecular-profiles/{molecularProfileId}/discrete-copy-number/fetch",
        method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Fetch discrete copy number alterations in a molecular profile by sample ID")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = DiscreteCopyNumberData.class))))
    public ResponseEntity<List<DiscreteCopyNumberData>> fetchDiscreteCopyNumbersInMolecularProfile(
        @Parameter(required = true, description = "Molecular Profile ID e.g. acc_tcga_gistic")
        @PathVariable String molecularProfileId,
        @Parameter(description = "Type of the copy number event")
        @RequestParam(defaultValue = "HOMDEL_AND_AMP") DiscreteCopyNumberEventType discreteCopyNumberEventType,
        @Parameter(required = true, description = "List of Sample IDs/Sample List ID and Entrez Gene IDs")
        @Valid @RequestBody DiscreteCopyNumberFilter discreteCopyNumberFilter,
        @Parameter(description = "Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection)
        throws MolecularProfileNotFoundException {

        if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            BaseMeta baseMeta;

            if (discreteCopyNumberFilter.getSampleListId() != null) {
                baseMeta = discreteCopyNumberService.getMetaDiscreteCopyNumbersInMolecularProfileBySampleListId(
                    molecularProfileId, discreteCopyNumberFilter.getSampleListId(),
                    discreteCopyNumberFilter.getEntrezGeneIds(), discreteCopyNumberEventType.getAlterationTypes());
            } else {
                baseMeta = discreteCopyNumberService.fetchMetaDiscreteCopyNumbersInMolecularProfile(molecularProfileId,
                    discreteCopyNumberFilter.getSampleIds(), discreteCopyNumberFilter.getEntrezGeneIds(),
                    discreteCopyNumberEventType.getAlterationTypes());
            }
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, baseMeta.getTotalCount().toString());
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            List<DiscreteCopyNumberData> discreteCopyNumberDataList;
            if (discreteCopyNumberFilter.getSampleListId() != null) {
                discreteCopyNumberDataList = discreteCopyNumberService
                    .getDiscreteCopyNumbersInMolecularProfileBySampleListId(molecularProfileId,
                        discreteCopyNumberFilter.getSampleListId(), discreteCopyNumberFilter.getEntrezGeneIds(),
                        discreteCopyNumberEventType.getAlterationTypes(), projection.name());
            } else {
                discreteCopyNumberDataList = discreteCopyNumberService.fetchDiscreteCopyNumbersInMolecularProfile(
                    molecularProfileId, discreteCopyNumberFilter.getSampleIds(),
                    discreteCopyNumberFilter.getEntrezGeneIds(), discreteCopyNumberEventType.getAlterationTypes(),
                    projection.name());
            }

            return new ResponseEntity<>(discreteCopyNumberDataList, HttpStatus.OK);
        }
    }

}
