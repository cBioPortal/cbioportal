package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.cbioportal.model.GenePanel;
import org.cbioportal.model.GenericAssayData;
import org.cbioportal.service.GenericAssayService;
import org.cbioportal.service.exception.GenePanelNotFoundException;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.web.config.PublicApiTags;
import org.cbioportal.web.config.annotation.PublicApi;
import org.cbioportal.web.parameter.*;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@PublicApi
@RestController
@Validated
@Api(tags = PublicApiTags.GENERIC_ASSAY_DATA, description = " ")
public class GenericAssayDataController {

    @Autowired
    private GenericAssayService genericAssayService;

    @PreAuthorize("hasPermission(#molecularProfileId, 'MolecularProfileId', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/generic-assay-data/{molecularProfileId}/generic-assay/{genericAssayStableId}", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get generic_assay_data in a molecular profile")
    public ResponseEntity<List<GenericAssayData>> getGenericAssayDataInMolecularProfile(
        @ApiParam(required = true, value = "Molecular Profile ID")
        @PathVariable String molecularProfileId,
        @ApiParam(required = true, value = "Generic Assay stable ID")
        @PathVariable String genericAssayStableId,
        @ApiParam("Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection) throws MolecularProfileNotFoundException {

        List<GenericAssayData> result;
        result = filterEmptyGenericAssayData(genericAssayService.fetchGenericAssayData(molecularProfileId,
            null, Arrays.asList(genericAssayStableId) , projection.name()));

        if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, String.valueOf(result.size()));
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
    }

    @PreAuthorize("hasPermission(#molecularProfileId, 'MolecularProfileId', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/generic_assay_data/{molecularProfileId}/fetch",
        method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("fetch generic_assay_data in a molecular profile")
    public ResponseEntity<List<GenericAssayData>> fetchGenericAssayDataInMolecularProfileRedirect(
        @ApiParam(required = true, value = "Molecular Profile ID")
        @PathVariable String molecularProfileId,
        @ApiParam(required = true, value = "List of Sample IDs/Sample List ID and Generic Assay IDs")
        @Valid @RequestBody GenericAssayFilter genericAssayDataFilter,
        @ApiParam("Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection) throws MolecularProfileNotFoundException {

        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", "/generic-assay-data/" + molecularProfileId + "/fetch");
        return new ResponseEntity<>(headers, HttpStatus.PERMANENT_REDIRECT);
    }
    
    @PreAuthorize("hasPermission(#molecularProfileId, 'MolecularProfileId', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/generic-assay-data/{molecularProfileId}/fetch",
        method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("fetch generic_assay_data in a molecular profile")
    public ResponseEntity<List<GenericAssayData>> fetchGenericAssayDataInMolecularProfile(
        @ApiParam(required = true, value = "Molecular Profile ID")
        @PathVariable String molecularProfileId,
        @ApiParam(required = true, value = "List of Sample IDs/Sample List ID and Generic Assay IDs")
        @Valid @RequestBody GenericAssayFilter genericAssayDataFilter,
        @ApiParam("Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection) throws MolecularProfileNotFoundException {

        List<GenericAssayData> result;
        if (genericAssayDataFilter.getSampleListId() != null) {
            result = filterEmptyGenericAssayData(genericAssayService.getGenericAssayData(molecularProfileId,
                genericAssayDataFilter.getSampleListId(), genericAssayDataFilter.getGenericAssayStableIds(), projection.name()));
        } else {
            result = filterEmptyGenericAssayData(genericAssayService.fetchGenericAssayData(molecularProfileId,
                genericAssayDataFilter.getSampleIds(), genericAssayDataFilter.getGenericAssayStableIds(), projection.name()));
        }

        if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, String.valueOf(result.size()));
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
    }

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/generic_assay_data/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch generic_assay_data")
    public ResponseEntity<List<GenericAssayData>> fetchGenericAssayDataInMultipleMolecularProfilesRedirect(
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @RequestAttribute(required = false, value = "interceptedGenericAssayDataMultipleStudyFilter") GenericAssayDataMultipleStudyFilter interceptedGenericAssayDataMultipleStudyFilter,
        @ApiParam(required = true, value = "List of Molecular Profile ID and Sample ID pairs or List of Molecular" +
            "Profile IDs and Generic Assay IDs")
        @Valid @RequestBody(required = false) GenericAssayDataMultipleStudyFilter genericAssayDataMultipleStudyFilter,
        @ApiParam("Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection) throws MolecularProfileNotFoundException {

        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", "/generic-assay-data/fetch");
        return new ResponseEntity<>(headers, HttpStatus.PERMANENT_REDIRECT);
    }

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/generic-assay-data/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch generic-assay-data")
    public ResponseEntity<List<GenericAssayData>> fetchGenericAssayDataInMultipleMolecularProfiles(
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @RequestAttribute(required = false, value = "interceptedGenericAssayDataMultipleStudyFilter") GenericAssayDataMultipleStudyFilter interceptedGenericAssayDataMultipleStudyFilter,
        @ApiParam(required = true, value = "List of Molecular Profile ID and Sample ID pairs or List of Molecular" +
            "Profile IDs and Generic Assay IDs")
        @Valid @RequestBody(required = false) GenericAssayDataMultipleStudyFilter genericAssayDataMultipleStudyFilter,
        @ApiParam("Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection) throws MolecularProfileNotFoundException {

        List<GenericAssayData> result;
        if (interceptedGenericAssayDataMultipleStudyFilter.getMolecularProfileIds() != null) {
            result = filterEmptyGenericAssayData(genericAssayService.fetchGenericAssayData(
                interceptedGenericAssayDataMultipleStudyFilter.getMolecularProfileIds(), null,
                interceptedGenericAssayDataMultipleStudyFilter.getGenericAssayStableIds(), projection.name()));
        } else {

            List<String> molecularProfileIds = new ArrayList<>();
            List<String> sampleIds = new ArrayList<>();
            extractMolecularProfileAndSampleIds(interceptedGenericAssayDataMultipleStudyFilter, molecularProfileIds, sampleIds);
            result = filterEmptyGenericAssayData(genericAssayService.fetchGenericAssayData(molecularProfileIds,
                sampleIds, interceptedGenericAssayDataMultipleStudyFilter.getGenericAssayStableIds(), projection.name()));
        }

        if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, String.valueOf(result.size()));
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
    }
    
    private void extractMolecularProfileAndSampleIds(GenericAssayDataMultipleStudyFilter molecularDataMultipleStudyFilter, List<String> molecularProfileIds, List<String> sampleIds) {
        for (SampleMolecularIdentifier sampleMolecularIdentifier : molecularDataMultipleStudyFilter.getSampleMolecularIdentifiers()) {
            molecularProfileIds.add(sampleMolecularIdentifier.getMolecularProfileId());
            sampleIds.add(sampleMolecularIdentifier.getSampleId());
        }
    }

    private List<GenericAssayData> filterEmptyGenericAssayData(List<GenericAssayData> genericAssayDataList) {
        return genericAssayDataList.stream()
            .filter(g -> StringUtils.isNotEmpty(g.getValue()) && !g.getValue().equals("NA"))
            .collect(Collectors.toList());
    }
}
