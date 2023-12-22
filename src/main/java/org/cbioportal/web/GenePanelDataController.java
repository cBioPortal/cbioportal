package org.cbioportal.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.cbioportal.model.GenePanelData;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.service.GenePanelService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.web.config.PublicApiTags;
import org.cbioportal.web.config.annotation.PublicApi;
import org.cbioportal.web.parameter.GenePanelDataFilter;
import org.cbioportal.web.parameter.GenePanelDataMultipleStudyFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@PublicApi
@RestController
@RequestMapping("/api")
@Validated
@Tag(name = PublicApiTags.GENE_PANEL_DATA, description = " ")
public class GenePanelDataController {

    @Autowired
    private GenePanelService genePanelService;

    @PreAuthorize("hasPermission(#molecularProfileId, 'MolecularProfileId', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/molecular-profiles/{molecularProfileId}/gene-panel-data/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get gene panel data")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = GenePanelData.class))))
    public ResponseEntity<List<GenePanelData>> getGenePanelData(
        @Parameter(required = true, description = "Molecular Profile ID e.g. nsclc_unito_2016_mutations")
        @PathVariable String molecularProfileId,
        @Parameter(required = true, description = "List of Sample IDs/Sample List ID and Entrez Gene IDs")
        @Valid @RequestBody GenePanelDataFilter genePanelDataFilter) throws MolecularProfileNotFoundException {

        List<GenePanelData> genePanelDataList;
        if (genePanelDataFilter.getSampleListId() != null) {
            genePanelDataList = genePanelService.getGenePanelData(molecularProfileId,
                genePanelDataFilter.getSampleListId());
        } else {
            genePanelDataList = genePanelService.fetchGenePanelData(molecularProfileId,
                genePanelDataFilter.getSampleIds());
        }

        return new ResponseEntity<>(genePanelDataList, HttpStatus.OK);
    }

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/gene-panel-data/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Fetch gene panel data")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = GenePanelData.class))))
    public ResponseEntity<List<GenePanelData>> fetchGenePanelDataInMultipleMolecularProfiles(
        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid @RequestAttribute(required = false, value = "interceptedGenePanelDataMultipleStudyFilter") GenePanelDataMultipleStudyFilter interceptedGenePanelDataMultipleStudyFilter,
        @Parameter(required = true, description = "Gene panel data filter object")
        @RequestBody(required = false) GenePanelDataMultipleStudyFilter genePanelDataMultipleStudyFilter) {

        List<GenePanelData> genePanelDataList;
        if(CollectionUtils.isEmpty(interceptedGenePanelDataMultipleStudyFilter.getMolecularProfileIds())) {
            List<MolecularProfileCaseIdentifier> molecularProfileSampleIdentifiers = interceptedGenePanelDataMultipleStudyFilter.getSampleMolecularIdentifiers()
                .stream()
                .map(sampleMolecularIdentifier -> {
                    MolecularProfileCaseIdentifier profileCaseIdentifier = new MolecularProfileCaseIdentifier();
                    profileCaseIdentifier.setMolecularProfileId(sampleMolecularIdentifier.getMolecularProfileId());
                    profileCaseIdentifier.setCaseId(sampleMolecularIdentifier.getSampleId());
                    return profileCaseIdentifier;
                })
                .collect(Collectors.toList());

            genePanelDataList = genePanelService.fetchGenePanelDataInMultipleMolecularProfiles(molecularProfileSampleIdentifiers);
        } else {
            genePanelDataList = genePanelService.fetchGenePanelDataByMolecularProfileIds(new HashSet<>(interceptedGenePanelDataMultipleStudyFilter.getMolecularProfileIds()));
        }
        
        return new ResponseEntity<>(genePanelDataList, HttpStatus.OK);
    }
}
