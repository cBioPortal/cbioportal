package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.cbioportal.model.PhylogeneticTree;
import org.cbioportal.service.PhylogeneticTreeService;
import org.cbioportal.service.exception.PatientNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.cbioportal.web.config.annotation.PublicApi;
import org.cbioportal.web.parameter.Direction;
import org.cbioportal.web.parameter.HeaderKeyConstants;
import org.cbioportal.web.parameter.PagingConstants;
import org.cbioportal.web.parameter.Projection;
import org.cbioportal.web.parameter.PatientIdentifier;
// import org.cbioportal.web.parameter.sort.PhylogeneticTreeSortBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
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

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@PublicApi
@RestController
@Validated
@Api(tags = "Phylogenetic Trees", description = " ")
public class PhylogeneticTreeController {

    private static final int PHYLOGENETIC_TREE_MAX_PAGE_SIZE = 20000;
    private static final String PHYLOGENETIC_TREE_DEFAULT_PAGE_SIZE = "20000";

    @Autowired
    private PhylogeneticTreeService phylogeneticTreeService;

    @RequestMapping(value = "/studies/{studyId}/patients/{patientId}/tree-structure", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get phylogenetic tree for a patient in a study")
    public ResponseEntity<List<PhylogeneticTree>> getPhylogeneticTreesInPatientInStudy(
        @ApiParam(required = true, value = "Study ID e.g. acc_tcga")
        @PathVariable String studyId,
        @ApiParam(required = true, value = "Patient ID e.g. TCGA-OR-A5J2")
        @PathVariable String patientId,
        @ApiParam("Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection,
        @ApiParam("Page size of the result list")
        @Max(PHYLOGENETIC_TREE_MAX_PAGE_SIZE)
        @Min(PagingConstants.MIN_PAGE_SIZE)
        @RequestParam(defaultValue = PHYLOGENETIC_TREE_DEFAULT_PAGE_SIZE) Integer pageSize,
        @ApiParam("Page number of the result list")
        @Min(PagingConstants.MIN_PAGE_NUMBER)
        @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber) throws PatientNotFoundException, 
        StudyNotFoundException {

        // if (projection == Projection.META) {
        //     HttpHeaders responseHeaders = new HttpHeaders();
        //     responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, phylogeneticTreeService
        //         .getMetaPhylogeneticTreesInPatientInStudy(studyId, patientId).getTotalCount().toString());
        //     return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        // } else {
        //     return new ResponseEntity<>(
        //         phylogeneticTreeService.getPhylogeneticTreesInPatientInStudy(studyId, patientId,
        //             projection.name(), pageSize, pageNumber, sortBy == null ? null : sortBy.getOriginalValue(),
        //             direction.name()), HttpStatus.OK);
        // }


        return new ResponseEntity<>(
            phylogeneticTreeService.getPhylogeneticTreesInPatientInStudy(studyId, patientId,
                projection.name(), pageSize, pageNumber), HttpStatus.OK);
        
    }

    @RequestMapping(value = "/tree-structure/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch phylogenetic tree by patient ID")
    public ResponseEntity<List<PhylogeneticTree>> fetchPhylogeneticTrees(
        @ApiParam(required = true, value = "List of patient identifiers")
        @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
        @RequestBody List<PatientIdentifier> patientIdentifiers,
        @ApiParam("Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection) {

        List<String> studyIds = new ArrayList<>();
        List<String> patientIds = new ArrayList<>();

        for (PatientIdentifier patientIdentifier : patientIdentifiers) {
            studyIds.add(patientIdentifier.getStudyId());
            patientIds.add(patientIdentifier.getPatientId());
        }

        // if (projection == Projection.META) {
        //     HttpHeaders responseHeaders = new HttpHeaders();
        //     responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, phylogeneticTreeService
        //         .fetchMetaPhylogeneticTrees(studyIds, patientIds).getTotalCount().toString());
        //     return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        // } else {
        //     return new ResponseEntity<>(
        //         phylogeneticTreeService.fetchPhylogeneticTrees(studyIds, patientIds, projection.name()), 
        //         HttpStatus.OK);
        // }


        return new ResponseEntity<>(
            phylogeneticTreeService.fetchPhylogeneticTrees(studyIds, patientIds, projection.name()), 
            HttpStatus.OK);
        
    }
}
