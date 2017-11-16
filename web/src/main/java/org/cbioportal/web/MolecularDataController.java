package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.cbioportal.model.GeneMolecularData;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.MolecularDataService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.web.config.annotation.PublicApi;
import org.cbioportal.web.parameter.MolecularDataFilter;
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
@Api(tags = "Molecular Data", description = " ")
public class MolecularDataController {
    
    @Autowired
    private MolecularDataService molecularDataService;
    
    @RequestMapping(value = "/molecular-profiles/{molecularProfileId}/molecular-data", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get all molecular data in a molecular profile")
    public ResponseEntity<List<GeneMolecularData>> getAllMolecularDataInMolecularProfile(
        @ApiParam(required = true, value = "Molecular Profile ID e.g. acc_tcga_rna_seq_v2_mrna")
        @PathVariable String molecularProfileId,
        @ApiParam(required = true, value = "Sample List ID e.g. acc_tcga_all")
        @RequestParam String sampleListId,
        @ApiParam(required = true, value = "Entrez Gene ID e.g. 1")
        @RequestParam Integer entrezGeneId,
        @ApiParam("Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection) throws MolecularProfileNotFoundException {

        if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, molecularDataService.getMetaMolecularData(
                molecularProfileId, sampleListId, Arrays.asList(entrezGeneId)).getTotalCount().toString());
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(molecularDataService.getMolecularData(molecularProfileId, sampleListId, 
                Arrays.asList(entrezGeneId), projection.name()), HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/molecular-profiles/{molecularProfileId}/molecular-data/fetch",
        method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch molecular data by sample ID(s)")
    public ResponseEntity<List<GeneMolecularData>> fetchAllMolecularDataInMolecularProfile(
        @ApiParam(required = true, value = "Molecular Profile ID e.g. acc_tcga_rna_seq_v2_mrna")
        @PathVariable String molecularProfileId,
        @ApiParam(required = true, value = "List of Sample IDs/Sample List ID and Entrez Gene IDs")
        @Valid @RequestBody MolecularDataFilter molecularDataFilter,
        @ApiParam("Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection) throws MolecularProfileNotFoundException {

        if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            BaseMeta baseMeta;

            if (molecularDataFilter.getSampleListId() != null) {
                baseMeta = molecularDataService.getMetaMolecularData(molecularProfileId, 
                    molecularDataFilter.getSampleListId(), molecularDataFilter.getEntrezGeneIds());
            } else {
                baseMeta = molecularDataService.fetchMetaMolecularData(molecularProfileId, 
                    molecularDataFilter.getSampleIds(), molecularDataFilter.getEntrezGeneIds());
            }
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, baseMeta.getTotalCount().toString());
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            List<GeneMolecularData> geneMolecularDataList;
            if (molecularDataFilter.getSampleListId() != null) {
                geneMolecularDataList = molecularDataService.getMolecularData(molecularProfileId,
                    molecularDataFilter.getSampleListId(), molecularDataFilter.getEntrezGeneIds(), projection.name());
            } else {
                geneMolecularDataList = molecularDataService.fetchMolecularData(molecularProfileId,
                    molecularDataFilter.getSampleIds(), molecularDataFilter.getEntrezGeneIds(), projection.name());
            }

            return new ResponseEntity<>(geneMolecularDataList, HttpStatus.OK);
        }
    }
}
