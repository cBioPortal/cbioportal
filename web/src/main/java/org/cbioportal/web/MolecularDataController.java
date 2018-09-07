package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import org.apache.commons.lang.math.NumberUtils;
import org.cbioportal.model.GeneMolecularData;
import org.cbioportal.model.NumericGeneMolecularData;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.MolecularDataService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.web.config.annotation.PublicApi;
import org.cbioportal.web.parameter.MolecularDataFilter;
import org.cbioportal.web.parameter.MolecularDataMultipleStudyFilter;
import org.cbioportal.web.parameter.HeaderKeyConstants;
import org.cbioportal.web.parameter.Projection;
import org.cbioportal.web.parameter.SampleMolecularIdentifier;
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
import org.springframework.security.access.prepost.PreAuthorize;

import javax.validation.Valid;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@PublicApi
@RestController
@Validated
@Api(tags = "Molecular Data", description = " ")
public class MolecularDataController {
    
    @Autowired
    private MolecularDataService molecularDataService;

    @PreAuthorize("hasPermission(#molecularProfileId, 'MolecularProfile', 'read')")
    @RequestMapping(value = "/molecular-profiles/{molecularProfileId}/molecular-data", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get all molecular data in a molecular profile")
    public ResponseEntity<List<NumericGeneMolecularData>> getAllMolecularDataInMolecularProfile(
        @ApiParam(required = true, value = "Molecular Profile ID e.g. acc_tcga_rna_seq_v2_mrna")
        @PathVariable String molecularProfileId,
        @ApiParam(required = true, value = "Sample List ID e.g. acc_tcga_all")
        @RequestParam String sampleListId,
        @ApiParam(required = true, value = "Entrez Gene ID e.g. 1")
        @RequestParam Integer entrezGeneId,
        @ApiParam("Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection) throws MolecularProfileNotFoundException {

        List<NumericGeneMolecularData> result = filterNonNumberMolecularData(molecularDataService.getMolecularData(
            molecularProfileId, sampleListId, Arrays.asList(entrezGeneId), projection.name()));

        if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, String.valueOf(result.size()));
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
    }

    @PreAuthorize("hasPermission(#molecularProfileId, 'MolecularProfile', 'read')")
    @RequestMapping(value = "/molecular-profiles/{molecularProfileId}/molecular-data/fetch",
        method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch molecular data in a molecular profile")
    public ResponseEntity<List<NumericGeneMolecularData>> fetchAllMolecularDataInMolecularProfile(
        @ApiParam(required = true, value = "Molecular Profile ID e.g. acc_tcga_rna_seq_v2_mrna")
        @PathVariable String molecularProfileId,
        @ApiParam(required = true, value = "List of Sample IDs/Sample List ID and Entrez Gene IDs")
        @Valid @RequestBody MolecularDataFilter molecularDataFilter,
        @ApiParam("Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection) throws MolecularProfileNotFoundException {

        List<NumericGeneMolecularData> result;
        if (molecularDataFilter.getSampleListId() != null) {
            result = filterNonNumberMolecularData(molecularDataService.getMolecularData(molecularProfileId,
                molecularDataFilter.getSampleListId(), molecularDataFilter.getEntrezGeneIds(), projection.name()));
        } else {
            result = filterNonNumberMolecularData(molecularDataService.fetchMolecularData(molecularProfileId,
                molecularDataFilter.getSampleIds(), molecularDataFilter.getEntrezGeneIds(), projection.name()));
        }

        if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, String.valueOf(result.size()));
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
    }

    @PreAuthorize("hasPermission(#molecularDataMultipleStudyFilter, 'MolecularDataMultipleStudyFilter', 'read')")
    @RequestMapping(value = "/molecular-data/fetch", method = RequestMethod.POST, 
    consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch molecular data")
    public ResponseEntity<List<NumericGeneMolecularData>> fetchMolecularDataInMultipleMolecularProfiles(
        @ApiParam(required = true, value = "List of Molecular Profile ID and Sample ID pairs or List of Molecular" + 
            "Profile IDs and Entrez Gene IDs")
        @Valid @RequestBody MolecularDataMultipleStudyFilter molecularDataMultipleStudyFilter,
        @ApiParam("Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection) {

        List<NumericGeneMolecularData> result;
        if (molecularDataMultipleStudyFilter.getMolecularProfileIds() != null) {
            result = filterNonNumberMolecularData(molecularDataService.getMolecularDataInMultipleMolecularProfiles(
                molecularDataMultipleStudyFilter.getMolecularProfileIds(), null, 
                molecularDataMultipleStudyFilter.getEntrezGeneIds(), projection.name()));
        } else {

            List<String> molecularProfileIds = new ArrayList<>();
            List<String> sampleIds = new ArrayList<>();
            extractMolecularProfileAndSampleIds(molecularDataMultipleStudyFilter, molecularProfileIds, sampleIds);
            result = filterNonNumberMolecularData(molecularDataService.getMolecularDataInMultipleMolecularProfiles(molecularProfileIds,
                sampleIds, molecularDataMultipleStudyFilter.getEntrezGeneIds(), projection.name()));
        }
        
        if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, String.valueOf(result.size()));
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
    }

    private void extractMolecularProfileAndSampleIds(MolecularDataMultipleStudyFilter molecularDataMultipleStudyFilter,
                                                     List<String> molecularProfileIds, List<String> sampleIds) {
        
        for (SampleMolecularIdentifier sampleMolecularIdentifier :
            molecularDataMultipleStudyFilter.getSampleMolecularIdentifiers()) {

            molecularProfileIds.add(sampleMolecularIdentifier.getMolecularProfileId());
            sampleIds.add(sampleMolecularIdentifier.getSampleId());
        }
    }

    private List<NumericGeneMolecularData> filterNonNumberMolecularData(List<GeneMolecularData> geneMolecularDataList) {

        List<NumericGeneMolecularData> result = new ArrayList<>();
        geneMolecularDataList.forEach(g -> {
            String value = g.getValue();
            if (NumberUtils.isNumber(value)) {
                NumericGeneMolecularData data = new NumericGeneMolecularData();
                data.setEntrezGeneId(g.getEntrezGeneId());
                data.setGene(g.getGene());
                data.setMolecularProfileId(g.getMolecularProfileId());
                data.setPatientId(g.getPatientId());
                data.setSampleId(g.getSampleId());
                data.setStudyId(g.getStudyId());
                data.setValue(new BigDecimal(g.getValue()));
                result.add(data);
            }
        });

        return result;
    }
}
