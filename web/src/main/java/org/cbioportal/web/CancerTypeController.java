package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.cbioportal.model.TypeOfCancer;
import org.cbioportal.web.parameter.PagingConstants;
import org.cbioportal.web.parameter.Projection;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Api(tags = "Cancer Types", description = " ")
public class CancerTypeController {

    @RequestMapping(value = "/cancer-types", method = RequestMethod.GET)
    @ApiOperation("Get all cancer types")
    public ResponseEntity<List<TypeOfCancer>> getAllCancerTypes(@RequestParam(defaultValue = "SUMMARY") Projection projection,
                                                                @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
                                                                @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber) {

        throw new UnsupportedOperationException();
    }

    @RequestMapping(value = "/cancer-types/{cancerTypeId}", method = RequestMethod.GET)
    @ApiOperation("Get a cancer type")
    public ResponseEntity<TypeOfCancer> getCancerType(@PathVariable String cancerTypeId) {

        throw new UnsupportedOperationException();
    }
}
