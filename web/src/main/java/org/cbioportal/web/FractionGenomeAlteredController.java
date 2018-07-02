package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.cbioportal.model.FractionGenomeAltered;
import org.cbioportal.service.FractionGenomeAlteredService;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.web.parameter.FractionGenomeAlteredFilter;
import org.cbioportal.web.parameter.PagingConstants;
import org.cbioportal.web.parameter.SampleIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import java.util.ArrayList;
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
            List<String> studyIds = new ArrayList<>();
            fractionGenomeAlteredFilter.getSampleIds().forEach(s -> studyIds.add(studyId));
            fractionGenomeAlteredList = fractionGenomeAlteredService.fetchFractionGenomeAltered(studyIds, 
                fractionGenomeAlteredFilter.getSampleIds());
        }
        
        return new ResponseEntity<>(fractionGenomeAlteredList, HttpStatus.OK);
    }

    @PreAuthorize("hasPermission(#sampleIdentifiers, 'List<SampleIdentifier>', 'read')")
    @RequestMapping(value = "/fraction-genome-altered/fetch",
        method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch fraction genome altered in multiple studies")
    public ResponseEntity<List<FractionGenomeAltered>> fetchFractionGenomeAlteredInMultipleStudies(
        @ApiParam(required = true, value = "List of Sample Identifiers")
        @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
        @RequestBody List<SampleIdentifier> sampleIdentifiers) {

        List<String> studyIds = new ArrayList<>();
        List<String> sampleIds = new ArrayList<>();
        for (SampleIdentifier sampleIdentifier : sampleIdentifiers) {
            studyIds.add(sampleIdentifier.getStudyId());
            sampleIds.add(sampleIdentifier.getSampleId());
        }
        
        return new ResponseEntity<>(fractionGenomeAlteredService.fetchFractionGenomeAltered(studyIds, sampleIds), 
            HttpStatus.OK);
    }
}
