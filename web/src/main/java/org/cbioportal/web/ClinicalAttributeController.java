package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.web.config.annotation.PublicApi;
import org.cbioportal.web.parameter.PagingConstants;
import org.cbioportal.web.parameter.Projection;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@PublicApi
@RestController
@Validated
@Api(tags = "Clinical Attributes", description = " ")
public class ClinicalAttributeController {

    @RequestMapping(value = "/clinical-attributes", method = RequestMethod.GET)
    @ApiOperation("Get all clinical attributes")
    public ResponseEntity<List<ClinicalAttribute>> getAllClinicalAttributes(@RequestParam(required = false) String studyId,
                                                                            @RequestParam(defaultValue = "SUMMARY") Projection projection,
                                                                            @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
                                                                            @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber) {

        throw new UnsupportedOperationException();
    }

    @RequestMapping(value = "/clinical-attributes/{clinicalAttributeId}", method = RequestMethod.GET)
    @ApiOperation("Get a clinical attribute")
    public ResponseEntity<ClinicalAttribute> getClinicalAttribute(@PathVariable String clinicalAttributeId) {

        throw new UnsupportedOperationException();
    }
}
