package org.cbioportal.web;

import java.util.List;

import org.cbioportal.model.GeneticData;
import org.cbioportal.model.GeneticEntity.EntityType;
import org.cbioportal.service.GeneticDataService;
import org.cbioportal.web.exception.PageSizeTooBigException;
import org.cbioportal.web.parameter.GeneticDataFilterCriteria;
import org.cbioportal.web.parameter.HeaderKeyConstants;
import org.cbioportal.web.parameter.PagingConstants;
import org.cbioportal.web.parameter.Projection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@Api(tags = "Genetic Data", description = " ")
public class GeneticDataController {

	@Autowired
    private GeneticDataService geneticDataService;
	
    @RequestMapping(value = "/studies/{studyId}/samples/{sampleId}/genetic-data", method = RequestMethod.GET)
    @ApiOperation("Get all genetic data of a sample in a study")
    public ResponseEntity<List<GeneticData>> getAllGeneticDataInSampleInStudy(@PathVariable String studyId,
                                                                                               @PathVariable String sampleId,
                                                                                               @RequestParam String geneticProfileId,
                                                                                               @RequestParam(defaultValue = "SUMMARY") Projection projection,
                                                                                               @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
                                                                                               @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber) {
    	//TODO remove? I don't think there is any use case for this method...
        throw new UnsupportedOperationException();
    }

    @RequestMapping(value = "/studies/{studyId}/patients/{patientId}/genetic-data", method = RequestMethod.GET)
    @ApiOperation("Get all genetic data of a patient in a study")
    public ResponseEntity<List<GeneticData>> getAllGeneticDataInPatientInStudy(@PathVariable String studyId,
                                                                                                @PathVariable String patientId,
                                                                                                @RequestParam String geneticProfileId,
                                                                                                @RequestParam(defaultValue = "SUMMARY") Projection projection,
                                                                                                @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
                                                                                                @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber) {
    	//TODO remove? I don't think there is any use case for this method...
        throw new UnsupportedOperationException();
    }

    @RequestMapping(value = "/genetic-profiles/{geneticProfileId}/genetic-data", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get all genetic data in a genetic profile")
    public ResponseEntity<List<GeneticData>> getAllGeneticDataInGeneticProfile(
    		@ApiParam(required = true, value = "Genetic profile ID, e.g. brca_tcga_mrna")
    		@PathVariable String geneticProfileId,
    		@ApiParam("Level of detail of the response, e.g. META, SUMMARY or DETAILED")
    		@RequestParam(defaultValue = "SUMMARY") Projection projection,
    		@ApiParam("Page size of the result list")
	        @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
	        @ApiParam("Page number of the result list")
	        @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber) {

    	if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, geneticDataService.getMetaGeneticDataInGeneticProfile(geneticProfileId)
                    .getTotalCount().toString());
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(
            		geneticDataService.getAllGeneticDataInGeneticProfile(geneticProfileId, projection.name(), pageSize, pageNumber), HttpStatus.OK);
        }
    }
    
    
    @RequestMapping(value = "/genetic-profiles/{geneticProfileId}/fetch", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch genetic data items by profile Id, gene ids and sample ids")
    public ResponseEntity<List<GeneticData>> fetchGeneticDataItems(
    		@ApiParam(required = true, value = "Genetic profile ID, e.g. brca_tcga_mrna")
    		@PathVariable String geneticProfileId,
            @ApiParam(required = true, value ="Entity type. Possible values: GENE or GENESET")
    		@RequestParam(defaultValue = "GENE") EntityType geneticEntityType,
    		@ApiParam("Level of detail of the response")
            @RequestParam(defaultValue = "SUMMARY") Projection projection,
            @ApiParam(required = true, value = "Search criteria to return the values for a given set of samples and genetic entity items (e.g. genes). "
            		+ "geneticEntityIds: The list of identifiers for the genetic entities of interest. "
            		+ "If entity type is GENE: list of Entrez Gene IDs. If entity type is GENESET: list of gene set identifiers. "
            		+ "Use one of these if you want to specify a subset of samples:"
            		+ "(1) sampleListId: Identifier of pre-defined sample list with samples to query, e.g. brca_tcga_all " 
            		+ "or (2) sampleIds: custom list of samples or patients to query, e.g. TCGA-BH-A1EO-01, TCGA-AR-A1AR-01")
            @RequestBody GeneticDataFilterCriteria geneticDataFilterCriteria,
            @ApiParam("Page size of the result list")
	        @RequestParam(defaultValue = PagingConstants.DEFAULT_MAX_PAGE_SIZE) Integer pageSize, 
	        @ApiParam("Page number of the result list")
	        @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber) throws PageSizeTooBigException {

    	if (projection == Projection.META) {
            //TODO 
    		return null;
        } else {
        	if (geneticDataFilterCriteria.getSampleListId() != null && geneticDataFilterCriteria.getSampleListId().trim().length() > 0) {
        		return new ResponseEntity<>(
                		geneticDataService.fetchGeneticDataInGeneticProfile(geneticProfileId, geneticEntityType,
                				geneticDataFilterCriteria.getGeneticEntityIds(), geneticDataFilterCriteria.getSampleListId(),
                				projection.name(), pageSize, pageNumber), HttpStatus.OK);
        	} else if (geneticDataFilterCriteria.getSampleIds() != null) {
        		return new ResponseEntity<>(
                		geneticDataService.fetchGeneticDataInGeneticProfile(geneticProfileId, geneticEntityType,
                				geneticDataFilterCriteria.getGeneticEntityIds(), geneticDataFilterCriteria.getSampleIds(),
                				projection.name(), pageSize, pageNumber), HttpStatus.OK);
        	} else {
        		return new ResponseEntity<>(
                		geneticDataService.fetchGeneticDataInGeneticProfile(geneticProfileId, geneticEntityType,
                				geneticDataFilterCriteria.getGeneticEntityIds(), 
                				projection.name(), pageSize, pageNumber), HttpStatus.OK);
        	}
        }
    }

    @RequestMapping(value = "/genetic-data/query", method = RequestMethod.POST)
    @ApiOperation("Query genetic data by example")
    public ResponseEntity<List<GeneticData>> queryGeneticDataByExample(@RequestParam(defaultValue = "SUMMARY") Projection projection,
                                                                                        @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
                                                                                        @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber,
                                                                                        @RequestBody GeneticData exampleGenericData) {

        throw new UnsupportedOperationException();
    }
}
