package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.cbioportal.model.GenePanel;
import org.cbioportal.model.GenePanelData;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.service.GenePanelService;
import org.cbioportal.service.exception.GenePanelNotFoundException;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.web.config.PublicApiTags;
import org.cbioportal.web.config.annotation.PublicApi;
import org.cbioportal.web.parameter.*;
import org.cbioportal.web.parameter.sort.GenePanelSortBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@PublicApi
@RestController
@RequestMapping("/api")
@Validated
@Api(tags = PublicApiTags.GENE_PANEL_DATA, description = " ")
public class GenePanelDataController {

    @Autowired
    private GenePanelService genePanelService;

    @PreAuthorize("hasPermission(#molecularProfileId, 'MolecularProfileId', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/molecular-profiles/{molecularProfileId}/gene-panel-data/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get gene panel data")
    public ResponseEntity<List<GenePanelData>> getGenePanelData(
        @ApiParam(required = true, value = "Molecular Profile ID e.g. nsclc_unito_2016_mutations")
        @PathVariable String molecularProfileId,
        @ApiParam(required = true, value = "List of Sample IDs/Sample List ID and Entrez Gene IDs")
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
    @ApiOperation("Fetch gene panel data")
    public ResponseEntity<List<GenePanelData>> fetchGenePanelDataInMultipleMolecularProfiles(
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid @RequestAttribute(required = false, value = "interceptedGenePanelDataMultipleStudyFilter") GenePanelDataMultipleStudyFilter interceptedGenePanelDataMultipleStudyFilter,
        @ApiParam(required = true, value = "Gene panel data filter object")
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
