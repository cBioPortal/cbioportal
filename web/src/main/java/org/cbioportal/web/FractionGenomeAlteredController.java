package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.cbioportal.model.FractionGenomeAltered;
import org.cbioportal.service.FractionGenomeAlteredService;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.web.parameter.FractionGenomeAlteredFilter;
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
import org.springframework.security.access.prepost.PreAuthorize;

import javax.validation.Valid;
import java.util.List;

@InternalApi
@RestController
@Validated
@Api(tags = "Fraction Genome Altered", description = " ")
public class FractionGenomeAlteredController {
    
    @Autowired
    private FractionGenomeAlteredService fractionGenomeAlteredService;

    @PreAuthorize("hasPermission(#studyId, 'CancerStudy', 'read')")
    @RequestMapping(value = "/studies/{studyId}/fraction-genome-altered/fetch",
        method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch fraction genome altered")
    public ResponseEntity<List<FractionGenomeAltered>> fetchFractionGenomeAltered(
        @ApiParam(required = true, value = "Study ID e.g. acc_tcga")
        @PathVariable String studyId,
        @ApiParam(required = true, value = "List of Sample IDs/Sample List ID")
        @Valid @RequestBody FractionGenomeAlteredFilter fractionGenomeAlteredFilter) {

        List<FractionGenomeAltered> fractionGenomeAlteredList;
        if (fractionGenomeAlteredFilter.getSampleListId() != null) {
            fractionGenomeAlteredList = fractionGenomeAlteredService.getFractionGenomeAltered(studyId, 
                fractionGenomeAlteredFilter.getSampleListId());
        } else {
            fractionGenomeAlteredList = fractionGenomeAlteredService.fetchFractionGenomeAltered(studyId, 
                fractionGenomeAlteredFilter.getSampleIds());
        }
        
        return new ResponseEntity<>(fractionGenomeAlteredList, HttpStatus.OK);
    }
}
