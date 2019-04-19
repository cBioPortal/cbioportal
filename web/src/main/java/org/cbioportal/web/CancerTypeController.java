package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.cbioportal.model.TypeOfCancer;
import org.cbioportal.service.CancerTypeService;
import org.cbioportal.service.exception.CancerTypeNotFoundException;
import org.cbioportal.web.config.annotation.PublicApi;
import org.cbioportal.web.parameter.Direction;
import org.cbioportal.web.parameter.HeaderKeyConstants;
import org.cbioportal.web.parameter.PagingConstants;
import org.cbioportal.web.parameter.Projection;
import org.cbioportal.web.parameter.sort.CancerTypeSortBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;

@PublicApi
@RestController
@Validated
@Api(tags = "A. Cancer Types", description = " ")
public class CancerTypeController {

    @Autowired
    private CancerTypeService cancerTypeService;

    @RequestMapping(value = "/cancer-types", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get all cancer types")
    public ResponseEntity<List<TypeOfCancer>> getAllCancerTypes(
        @ApiParam("Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection,
        @ApiParam("Page size of the result list")
        @Max(PagingConstants.MAX_PAGE_SIZE)
        @Min(PagingConstants.MIN_PAGE_SIZE)
        @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
        @ApiParam("Page number of the result list")
        @Min(PagingConstants.MIN_PAGE_NUMBER)
        @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber,
        @ApiParam("Name of the property that the result list is sorted by")
        @RequestParam(required = false) CancerTypeSortBy sortBy,
        @ApiParam("Direction of the sort")
        @RequestParam(defaultValue = "ASC") Direction direction) {

        if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, cancerTypeService.getMetaCancerTypes().getTotalCount()
                .toString());
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(
                cancerTypeService.getAllCancerTypes(projection.name(), pageSize, pageNumber,
                    sortBy == null ? null : sortBy.getOriginalValue(), direction.name()), HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/cancer-types/{cancerTypeId}", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get a cancer type")
    public ResponseEntity<TypeOfCancer> getCancerType(
        @ApiParam(required = true, value = "Cancer Type ID e.g. acc")
        @PathVariable String cancerTypeId) throws CancerTypeNotFoundException {

        return new ResponseEntity<>(cancerTypeService.getCancerType(cancerTypeId), HttpStatus.OK);
    }
}
