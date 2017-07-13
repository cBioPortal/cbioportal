package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.cbioportal.model.GeneGeneticData;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.GeneticDataService;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;
import org.cbioportal.web.config.annotation.PublicApi;
import org.cbioportal.web.parameter.GeneticDataFilter;
import org.cbioportal.web.parameter.HeaderKeyConstants;
import org.cbioportal.web.parameter.Projection;
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

import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;

@PublicApi
@RestController
@Validated
@Api(tags = "Genetic Data", description = " ")
public class GeneticDataController {
    
    @Autowired
    private GeneticDataService geneticDataService;
    
    @RequestMapping(value = "/genetic-profiles/{geneticProfileId}/genetic-data", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get all genetic data in a genetic profile")
    public ResponseEntity<List<GeneGeneticData>> getAllGeneticDataInGeneticProfile(
        @ApiParam(required = true, value = "Genetic Profile ID e.g. acc_tcga_rna_seq_v2_mrna")
        @PathVariable String geneticProfileId,
        @ApiParam(required = true, value = "Sample List ID e.g. acc_tcga_all")
        @RequestParam String sampleListId,
        @ApiParam(required = true, value = "Entrez Gene ID e.g. 1")
        @RequestParam Integer entrezGeneId,
        @ApiParam("Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection) throws GeneticProfileNotFoundException {

        if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, geneticDataService.getMetaGeneticData(geneticProfileId, 
                sampleListId, Arrays.asList(entrezGeneId)).getTotalCount().toString());
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(geneticDataService.getGeneticData(geneticProfileId, sampleListId, 
                Arrays.asList(entrezGeneId), projection.name()), HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/genetic-profiles/{geneticProfileId}/genetic-data/fetch",
        method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch discrete copy number alterations in a genetic profile by sample ID")
    public ResponseEntity<List<GeneGeneticData>> fetchAllGeneticDataInGeneticProfile(
        @ApiParam(required = true, value = "Genetic Profile ID e.g. acc_tcga_rna_seq_v2_mrna")
        @PathVariable String geneticProfileId,
        @ApiParam(required = true, value = "List of Sample IDs/Sample List ID and Entrez Gene IDs")
        @Valid @RequestBody GeneticDataFilter geneticDataFilter,
        @ApiParam("Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection) throws GeneticProfileNotFoundException {

        if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            BaseMeta baseMeta;

            if (geneticDataFilter.getSampleListId() != null) {
                baseMeta = geneticDataService.getMetaGeneticData(
                    geneticProfileId, geneticDataFilter.getSampleListId(),
                    geneticDataFilter.getEntrezGeneIds());
            } else {
                baseMeta = geneticDataService.fetchMetaGeneticData(geneticProfileId,
                    geneticDataFilter.getSampleIds(), geneticDataFilter.getEntrezGeneIds());
            }
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, baseMeta.getTotalCount().toString());
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            List<GeneGeneticData> geneGeneticDataList;
            if (geneticDataFilter.getSampleListId() != null) {
                geneGeneticDataList = geneticDataService.getGeneticData(geneticProfileId, 
                    geneticDataFilter.getSampleListId(), geneticDataFilter.getEntrezGeneIds(), projection.name());
            } else {
                geneGeneticDataList = geneticDataService.fetchGeneticData(geneticProfileId, 
                    geneticDataFilter.getSampleIds(), geneticDataFilter.getEntrezGeneIds(), projection.name());
            }

            return new ResponseEntity<>(geneGeneticDataList, HttpStatus.OK);
        }
    }
}
