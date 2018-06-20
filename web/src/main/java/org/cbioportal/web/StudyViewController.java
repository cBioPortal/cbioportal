package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.MolecularProfileSampleCount;
import org.cbioportal.model.MutationCountByGene;
import org.cbioportal.model.Sample;
import org.cbioportal.service.ClinicalDataService;
import org.cbioportal.service.DiscreteCopyNumberService;
import org.cbioportal.service.GenePanelService;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.MutationService;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.web.parameter.ClinicalDataType;
import org.cbioportal.web.parameter.Projection;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.cbioportal.web.util.StudyViewFilterApplier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@InternalApi
@RestController
@Validated
@Api(tags = "Study View", description = " ")
public class StudyViewController {

    @Autowired
    private StudyViewFilterApplier studyViewFilterApplier;
    @Autowired
    private ClinicalDataService clinicalDataService;
    @Autowired
    private MutationService mutationService;
    @Autowired
    private MolecularProfileService molecularProfileService;
    @Autowired
    private DiscreteCopyNumberService discreteCopyNumberService;
    @Autowired
    private SampleService sampleService;
    @Autowired
    private GenePanelService genePanelService;

    @RequestMapping(value = "/studies/{studyId}/attributes/{attributeId}/clinical-data-counts/fetch", method = RequestMethod.POST, 
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch clinical data counts by study view filter")
    public ResponseEntity<List<ClinicalDataCount>> fetchClinicalDataCounts(
        @ApiParam(required = true, value = "Study ID e.g. acc_tcga") 
        @PathVariable String studyId,
        @ApiParam(required = true, value = "Attribute ID e.g. CANCER_TYPE")
        @PathVariable String attributeId,
        @ApiParam("Type of the clinical data")
        @RequestParam(defaultValue = "SAMPLE") ClinicalDataType clinicalDataType,
        @ApiParam(required = true, value = "Clinical data count filter")
        @Valid @RequestBody StudyViewFilter studyViewFilter) throws StudyNotFoundException, 
        MolecularProfileNotFoundException {

        if (studyViewFilter.getClinicalDataEqualityFilters() != null) {
            studyViewFilter.getClinicalDataEqualityFilters().removeIf(f -> f.getAttributeId().equals(attributeId));
        }
        List<String> filteredSampleIds = studyViewFilterApplier.apply(studyId, studyViewFilter);

        if (filteredSampleIds.isEmpty()) {
            return new ResponseEntity<>(null, HttpStatus.OK);
        }
        return new ResponseEntity<>(clinicalDataService.fetchClinicalDataCounts(studyId, filteredSampleIds, 
            Arrays.asList(attributeId), clinicalDataType.name()).get(attributeId), HttpStatus.OK);
    }

    @RequestMapping(value = "/molecular-profiles/{molecularProfileId}/mutated-genes/fetch", method = RequestMethod.POST, 
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch mutated genes by study view filter")
    public ResponseEntity<List<MutationCountByGene>> fetchMutatedGenes(
        @ApiParam(required = true, value = "Molecular Profile ID e.g. acc_tcga_mutations")
        @PathVariable String molecularProfileId,
        @ApiParam(required = true, value = "Study view filter")
        @Valid @RequestBody StudyViewFilter studyViewFilter) throws MolecularProfileNotFoundException, StudyNotFoundException {

        String studyId = molecularProfileService.getMolecularProfile(molecularProfileId).getCancerStudyIdentifier();
        List<String> filteredSampleIds = studyViewFilterApplier.apply(studyId, studyViewFilter);
        List<MutationCountByGene> result = new ArrayList<>();
        if (!filteredSampleIds.isEmpty()) {
            result = mutationService.getSampleCountByEntrezGeneIdsAndSampleIds(molecularProfileId, 
                filteredSampleIds, null, true);
            result.sort((a, b) -> b.getCountByEntity() - a.getCountByEntity());
        }
        
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/molecular-profiles/{molecularProfileId}/cna-genes/fetch", method = RequestMethod.POST, 
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch CNA genes by study view filter")
    public ResponseEntity<List<CopyNumberCountByGene>> fetchCNAGenes(
        @ApiParam(required = true, value = "Molecular Profile ID e.g. acc_tcga_gistic")
        @PathVariable String molecularProfileId,
        @ApiParam(required = true, value = "Study view filter")
        @Valid @RequestBody StudyViewFilter studyViewFilter) throws MolecularProfileNotFoundException, StudyNotFoundException {

        String studyId = molecularProfileService.getMolecularProfile(molecularProfileId).getCancerStudyIdentifier();
        List<String> filteredSampleIds = studyViewFilterApplier.apply(studyId, studyViewFilter);
        List<CopyNumberCountByGene> result = new ArrayList<>();
        if (!filteredSampleIds.isEmpty()) {
            result = discreteCopyNumberService.getSampleCountByGeneAndAlterationAndSampleIds(molecularProfileId, 
                filteredSampleIds, null, Arrays.asList(-2, 2), true);
            result.sort((a, b) -> b.getCountByEntity() - a.getCountByEntity());
        }
        
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/studies/{studyId}/samples/fetch", method = RequestMethod.POST, 
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch sample IDs by study view filter")
    public ResponseEntity<List<Sample>> fetchSampleIds(
        @ApiParam(required = true, value = "Study ID e.g. acc_tcga") 
        @PathVariable String studyId,
        @ApiParam(required = true, value = "Study view filter")
        @Valid @RequestBody StudyViewFilter studyViewFilter) throws StudyNotFoundException, 
        MolecularProfileNotFoundException {
        
        List<String> sampleIds = studyViewFilterApplier.apply(studyId, studyViewFilter);
        List<String> studyIds = new ArrayList<>();
        sampleIds.forEach(s -> studyIds.add(studyId));
        return new ResponseEntity<>(sampleService.fetchSamples(studyIds, sampleIds, Projection.ID.name()), HttpStatus.OK);
    }

    @RequestMapping(value = "/molecular-profiles/{molecularProfileId}/sample-counts/fetch", method = RequestMethod.POST, 
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch sample IDs by study view filter")
    public ResponseEntity<MolecularProfileSampleCount> fetchMolecularProfileSampleCounts(
        @ApiParam(required = true, value = "Molecular Profile ID e.g. acc_tcga_mutations")
        @PathVariable String molecularProfileId,
        @ApiParam(required = true, value = "Study view filter")
        @Valid @RequestBody StudyViewFilter studyViewFilter) throws StudyNotFoundException, 
        MolecularProfileNotFoundException {
        
        String studyId = molecularProfileService.getMolecularProfile(molecularProfileId).getCancerStudyIdentifier();
        List<String> sampleIds = studyViewFilterApplier.apply(studyId, studyViewFilter);
        MolecularProfileSampleCount molecularProfileSampleCount = new MolecularProfileSampleCount();
        molecularProfileSampleCount.setNumberOfProfiledSamples(Math.toIntExact(genePanelService.fetchGenePanelData(
            molecularProfileId, sampleIds).stream().filter(g -> g.getProfiled()).count()));
        molecularProfileSampleCount.setNumberOfUnprofiledSamples(sampleIds.size() - 
            molecularProfileSampleCount.getNumberOfProfiledSamples());

        return new ResponseEntity<>(molecularProfileSampleCount, HttpStatus.OK);
    }
}
