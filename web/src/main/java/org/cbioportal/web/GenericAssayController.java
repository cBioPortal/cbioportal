package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import springfox.documentation.annotations.ApiIgnore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.cbioportal.model.GenericAssayData;
import org.cbioportal.model.meta.GenericAssayMeta;
import org.cbioportal.service.GenericAssayService;
import org.cbioportal.service.exception.GenericAssayNotFoundException;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.web.config.PublicApiTags;
import org.cbioportal.web.config.annotation.PublicApi;
import org.cbioportal.web.parameter.GenericAssayFilter;
import org.cbioportal.web.parameter.HeaderKeyConstants;
import org.cbioportal.web.parameter.Projection;
import org.cbioportal.web.parameter.GenericAssayDataMultipleStudyFilter;
import org.cbioportal.web.parameter.GenericAssayMetaFilter;
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

@PublicApi
@RestController
@Validated
@Api(tags = PublicApiTags.GENERIC_ASSAYS, description = " ")
public class GenericAssayController {
    
    @Autowired
    private GenericAssayService genericAssayService;
    
    // PreAuthorize is removed for performance reason
    @RequestMapping(value = "/generic-assay-meta/fetch", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
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
            return new ResponseEntity<>(result, HttpStatus.OK);
    }
    
    @RequestMapping(value = "/generic_assay_meta/fetch", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch meta data for generic-assay by ID")
    public ResponseEntity<List<GenericAssayMeta>> fetchGenericAssayMetaRedirect(
        @ApiParam(required = true, value = "List of Molecular Profile ID or List of Stable ID")
        @Valid @RequestBody GenericAssayMetaFilter genericAssayMetaFilter,
        @ApiParam("Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection) throws GenericAssayNotFoundException {
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", "/generic-assay-meta/fetch");
        return new ResponseEntity<>(headers, HttpStatus.PERMANENT_REDIRECT);
    }

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

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

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


}
