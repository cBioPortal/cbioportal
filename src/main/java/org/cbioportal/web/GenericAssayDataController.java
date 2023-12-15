package org.cbioportal.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
<<<<<<<< HEAD:src/main/java/org/cbioportal/web/GenericAssayDataController.java
========
import springfox.documentation.annotations.ApiIgnore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

>>>>>>>> master:web/src/main/java/org/cbioportal/web/GenericAssayController.java
import org.cbioportal.model.GenericAssayData;
import org.cbioportal.service.GenericAssayService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.web.config.PublicApiTags;
import org.cbioportal.web.config.annotation.PublicApi;
import org.cbioportal.web.parameter.GenericAssayDataMultipleStudyFilter;
import org.cbioportal.web.parameter.GenericAssayFilter;
import org.cbioportal.web.parameter.HeaderKeyConstants;
import org.cbioportal.web.parameter.Projection;
import org.cbioportal.web.parameter.SampleMolecularIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@PublicApi
@RestController
@RequestMapping("/api")
@Validated
@Tag(name = PublicApiTags.GENERIC_ASSAY_DATA, description = " ")
public class GenericAssayDataController {

    @Autowired
    private GenericAssayService genericAssayService;
<<<<<<<< HEAD:src/main/java/org/cbioportal/web/GenericAssayDataController.java

    @PreAuthorize("hasPermission(#molecularProfileId, 'MolecularProfileId', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/generic-assay-data/{molecularProfileId}/generic-assay/{genericAssayStableId}", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get generic_assay_data in a molecular profile")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = GenericAssayData.class))))
    public ResponseEntity<List<GenericAssayData>> getGenericAssayDataInMolecularProfile(
        @Parameter(required = true, description = "Molecular Profile ID")
        @PathVariable String molecularProfileId,
        @Parameter(required = true, description = "Generic Assay stable ID")
        @PathVariable String genericAssayStableId,
        @Parameter(description = "Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection) throws MolecularProfileNotFoundException {

        List<GenericAssayData> result;
        result = filterEmptyGenericAssayData(genericAssayService.fetchGenericAssayData(molecularProfileId,
            null, Arrays.asList(genericAssayStableId) , projection.name()));

        if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, String.valueOf(result.size()));
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
========
    
    // PreAuthorize is removed for performance reason
    @RequestMapping(value = "/generic_assay_meta/fetch", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch meta data for generic-assay by ID")
    public ResponseEntity<List<GenericAssayMeta>> fetchGenericAssayMeta(
        @ApiParam(required = true, value = "List of Molecular Profile ID or List of Stable ID")
        @Valid @RequestBody GenericAssayMetaFilter genericAssayMetaFilter,
        @ApiParam("Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection) throws GenericAssayNotFoundException {
            List<GenericAssayMeta> result;

            if (genericAssayMetaFilter.getGenericAssayStableIds() == null) {
                result = genericAssayService.getGenericAssayMetaByStableIdsAndMolecularIds(null, genericAssayMetaFilter.getMolecularProfileIds(), projection.name());
            } else if (genericAssayMetaFilter.getMolecularProfileIds() == null) {
                result = genericAssayService.getGenericAssayMetaByStableIdsAndMolecularIds(genericAssayMetaFilter.getGenericAssayStableIds(), null, projection.name());
            } else {
                result = genericAssayService.getGenericAssayMetaByStableIdsAndMolecularIds(genericAssayMetaFilter.getGenericAssayStableIds(), genericAssayMetaFilter.getMolecularProfileIds(), projection.name());
            }
>>>>>>>> master:web/src/main/java/org/cbioportal/web/GenericAssayController.java
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
    }
<<<<<<<< HEAD:src/main/java/org/cbioportal/web/GenericAssayDataController.java
    
    @PreAuthorize("hasPermission(#molecularProfileId, 'MolecularProfileId', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/generic_assay_data/{molecularProfileId}/fetch",
        method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "fetch generic_assay_data in a molecular profile")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = GenericAssayData.class))))
    public ResponseEntity<List<GenericAssayData>> fetchGenericAssayDataInMolecularProfile(
        @Parameter(required = true, description = "Molecular Profile ID")
        @PathVariable String molecularProfileId,
        @Parameter(required = true, description = "List of Sample IDs/Sample List ID and Generic Assay IDs")
        @Valid @RequestBody GenericAssayFilter genericAssayDataFilter,
        @Parameter(description = "Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection) throws MolecularProfileNotFoundException {
========

    // PreAuthorize is removed for performance reason
    @RequestMapping(value = "/generic-assay-meta/{molecularProfileId}", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch meta data for generic-assay by ID")
    public ResponseEntity<List<GenericAssayMeta>> getGenericAssayMeta(
        @ApiParam(required = true, value = "Molecular Profile ID")
        @PathVariable String molecularProfileId,
        @ApiParam("Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection) throws GenericAssayNotFoundException {
        List<GenericAssayMeta> result;
        
        result = genericAssayService.getGenericAssayMetaByStableIdsAndMolecularIds(null, Arrays.asList(molecularProfileId), projection.name());
>>>>>>>> master:web/src/main/java/org/cbioportal/web/GenericAssayController.java

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

<<<<<<<< HEAD:src/main/java/org/cbioportal/web/GenericAssayDataController.java
    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/generic_assay_data/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Fetch generic_assay_data")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = GenericAssayData.class))))
    public ResponseEntity<List<GenericAssayData>> fetchGenericAssayDataInMultipleMolecularProfiles(
        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @RequestAttribute(required = false, value = "interceptedGenericAssayDataMultipleStudyFilter") GenericAssayDataMultipleStudyFilter interceptedGenericAssayDataMultipleStudyFilter,
        @Parameter(required = true, description = "List of Molecular Profile ID and Sample ID pairs or List of Molecular" +
            "Profile IDs and Generic Assay IDs")
        @Valid @RequestBody(required = false) GenericAssayDataMultipleStudyFilter genericAssayDataMultipleStudyFilter,
        @Parameter(description = "Level of detail of the response")
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
========
    @RequestMapping(value = "/generic-assay-meta/generic-assay/{genericAssayStableId}", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch meta data for generic-assay by ID")
    public ResponseEntity<List<GenericAssayMeta>> getGenericAssayMeta_ga(
        @ApiParam(required = false, value = "Generic Assay stable ID")
        @PathVariable String genericAssayStableId,
        @ApiParam("Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection) throws GenericAssayNotFoundException {
        List<GenericAssayMeta> result;         
        result = genericAssayService.getGenericAssayMetaByStableIdsAndMolecularIds(Arrays.asList(genericAssayStableId), null, projection.name());
            
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

>>>>>>>> master:web/src/main/java/org/cbioportal/web/GenericAssayController.java

}
