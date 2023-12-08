package org.cbioportal.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.cbioportal.model.meta.GenericAssayMeta;
import org.cbioportal.service.GenericAssayService;
import org.cbioportal.service.exception.GenericAssayNotFoundException;
import org.cbioportal.web.config.PublicApiTags;
import org.cbioportal.web.config.annotation.PublicApi;
import org.cbioportal.web.parameter.GenericAssayMetaFilter;
import org.cbioportal.web.parameter.Projection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@PublicApi
@RestController()
@RequestMapping("/api")
@Validated
@Tag(name = PublicApiTags.GENERIC_ASSAYS, description = " ")
public class GenericAssayController {
    
    @Autowired
    private GenericAssayService genericAssayService;
    
    // PreAuthorize is removed for performance reason
    @RequestMapping(value = "/generic_assay_meta/fetch", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Fetch meta data for generic-assay by ID")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = GenericAssayMeta.class))))
    public ResponseEntity<List<GenericAssayMeta>> fetchGenericAssayMeta(
        @Parameter(required = true, description = "List of Molecular Profile ID or List of Stable ID")
        @Valid @RequestBody GenericAssayMetaFilter genericAssayMetaFilter,
        @Parameter(description = "Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection) throws GenericAssayNotFoundException {
            List<GenericAssayMeta> result;

            if (genericAssayMetaFilter.getGenericAssayStableIds() == null) {
                result = genericAssayService.getGenericAssayMetaByStableIdsAndMolecularIds(null, genericAssayMetaFilter.getMolecularProfileIds(), projection.name());
            } else if (genericAssayMetaFilter.getMolecularProfileIds() == null) {
                result = genericAssayService.getGenericAssayMetaByStableIdsAndMolecularIds(genericAssayMetaFilter.getGenericAssayStableIds(), null, projection.name());
            } else {
                result = genericAssayService.getGenericAssayMetaByStableIdsAndMolecularIds(genericAssayMetaFilter.getGenericAssayStableIds(), genericAssayMetaFilter.getMolecularProfileIds(), projection.name());
            }
            return new ResponseEntity<>(result, HttpStatus.OK);
    }

    // PreAuthorize is removed for performance reason
    @RequestMapping(value = "/generic-assay-meta/{molecularProfileId}", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Fetch meta data for generic-assay by ID")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = GenericAssayMeta.class))))
    public ResponseEntity<List<GenericAssayMeta>> getGenericAssayMeta(
        @Parameter(required = true, description = "Molecular Profile ID")
        @PathVariable String molecularProfileId,
        @Parameter(description = "Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection) throws GenericAssayNotFoundException {
        List<GenericAssayMeta> result;
        
        result = genericAssayService.getGenericAssayMetaByStableIdsAndMolecularIds(null, Arrays.asList(molecularProfileId), projection.name());

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/generic-assay-meta/generic-assay/{genericAssayStableId}", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Fetch meta data for generic-assay by ID")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = GenericAssayMeta.class))))
    public ResponseEntity<List<GenericAssayMeta>> getGenericAssayMeta_ga(
        @Parameter(required = false, description = "Generic Assay stable ID")
        @PathVariable String genericAssayStableId,
        @Parameter(description = "Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection) throws GenericAssayNotFoundException {
        List<GenericAssayMeta> result;         
        result = genericAssayService.getGenericAssayMetaByStableIdsAndMolecularIds(Arrays.asList(genericAssayStableId), null, projection.name());
            
        return new ResponseEntity<>(result, HttpStatus.OK);
    }


}
