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

import javax.validation.Valid;
import java.util.List;

@InternalApi
@RestController
@Validated
@Api(tags = "Fraction Genome Altered", description = " ")
public class FractionGenomeAlteredController {
    
    @Autowired
    private FractionGenomeAlteredService fractionGenomeAlteredService;

    @RequestMapping(value = "/molecular-profiles/{molecularProfileId}/fraction-genome-altered/fetch",
        method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch fraction genome altered")
    public ResponseEntity<List<FractionGenomeAltered>> fetchFractionGenomeAltered(
        @ApiParam(required = true, value = "Molecular Profile ID")
        @RequestParam(required = true) String molecularProfileId,
        @ApiParam(required = true, value = "List of Sample IDs/Sample List ID")
        @Valid @RequestBody FractionGenomeAlteredFilter fractionGenomeAlteredFilter,
        @ApiParam(required = true, value = "Cutoff")
        @RequestParam(defaultValue = "0.2") Double cutoff) {

        List<FractionGenomeAltered> fractionGenomeAlteredList;
        if (fractionGenomeAlteredFilter.getSampleListId() != null) {
            fractionGenomeAlteredList = fractionGenomeAlteredService.getFractionGenomeAltered(molecularProfileId,
                fractionGenomeAlteredFilter.getSampleListId(), cutoff);
        } else {
            fractionGenomeAlteredList = fractionGenomeAlteredService.fetchFractionGenomeAltered(molecularProfileId,
                fractionGenomeAlteredFilter.getSampleIds(), cutoff);
        }
        
        return new ResponseEntity<>(fractionGenomeAlteredList, HttpStatus.OK);
    }
}
