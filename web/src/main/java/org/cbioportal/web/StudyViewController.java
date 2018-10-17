package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.cbioportal.model.*;
import org.cbioportal.service.*;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.web.parameter.*;
import org.cbioportal.web.util.DataBinner;
import org.cbioportal.web.util.StudyViewFilterApplier;
import org.cbioportal.web.util.StudyViewFilterUtil;
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
    private PatientService patientService;
    @Autowired
    private GenePanelService genePanelService;
    @Autowired
    private SignificantlyMutatedGeneService significantlyMutatedGeneService;
    @Autowired
    private SignificantCopyNumberRegionService significantCopyNumberRegionService;
    @Autowired
    private DataBinner dataBinner;
    @Autowired
    private StudyViewFilterUtil studyViewFilterUtil;

    @RequestMapping(value = "/attributes/{attributeId}/clinical-data-counts/fetch", method = RequestMethod.POST, 
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch clinical data counts by study view filter")
    public ResponseEntity<List<ClinicalDataCount>> fetchClinicalDataCounts(
        @ApiParam(required = true, value = "Attribute ID e.g. CANCER_TYPE")
        @PathVariable String attributeId,
        @ApiParam("Type of the clinical data")
        @RequestParam(defaultValue = "SAMPLE") ClinicalDataType clinicalDataType,
        @ApiParam(required = true, value = "Clinical data count filter")
        @Valid @RequestBody StudyViewFilter studyViewFilter) {

        studyViewFilterUtil.removeSelfFromFilter(attributeId, studyViewFilter);
        List<SampleIdentifier> filteredSampleIdentifiers = studyViewFilterApplier.apply(studyViewFilter);

        if (filteredSampleIdentifiers.isEmpty()) {
            return new ResponseEntity<>(null, HttpStatus.OK);
        }
        List<String> studyIds = new ArrayList<>();
        List<String> sampleIds = new ArrayList<>();
        studyViewFilterUtil.extractStudyAndSampleIds(filteredSampleIdentifiers, studyIds, sampleIds);
        return new ResponseEntity<>(clinicalDataService.fetchClinicalDataCounts(studyIds, sampleIds, 
            Arrays.asList(attributeId), clinicalDataType.name()).get(attributeId), HttpStatus.OK);
    }

    @RequestMapping(value = "/attributes/{attributeId}/clinical-data-bin-counts/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch clinical data bin counts by study view filter")
    public ResponseEntity<List<DataBin>> fetchClinicalDataBinCounts(
        @ApiParam(required = true, value = "Attribute ID e.g. AGE")
        @PathVariable String attributeId,
        @ApiParam("Type of the clinical data")
        @RequestParam(defaultValue = "SAMPLE") ClinicalDataType clinicalDataType,
        @ApiParam("Method for data binning")
        @RequestParam(defaultValue = "DYNAMIC") DataBinMethod dataBinMethod,
        @ApiParam("Whether to disable log scaling")
        @RequestParam(defaultValue = "false") Boolean disableLogScale,
        @ApiParam(required = true, value = "Study view filter")
        @Valid @RequestBody StudyViewFilter studyViewFilter) {

        studyViewFilterUtil.removeSelfFromFilter(attributeId, studyViewFilter);
        
        List<DataBin> clinicalDataBins = null;
        List<String> filteredIds = new ArrayList<>();
        List<ClinicalData> filteredClinicalData = fetchClinicalData(attributeId, clinicalDataType, studyViewFilter, filteredIds);
        
        if (dataBinMethod == DataBinMethod.STATIC) 
        {
            StudyViewFilter filter = studyViewFilter == null ? null : new StudyViewFilter();
            
            if (filter != null) {
                filter.setStudyIds(studyViewFilter.getStudyIds());
                filter.setSampleIdentifiers(studyViewFilter.getSampleIdentifiers());
            }
            
            List<String> unfilteredIds = new ArrayList<>();
            List<ClinicalData> unfilteredClinicalData = fetchClinicalData(attributeId, clinicalDataType, filter, unfilteredIds);
            
            if (unfilteredClinicalData != null) {
                clinicalDataBins = dataBinner.calculateClinicalDataBins(
                    attributeId, filteredClinicalData, unfilteredClinicalData, filteredIds, unfilteredIds, disableLogScale);
            }
        }
        else { // dataBinMethod == DataBinMethod.DYNAMIC
            if (filteredClinicalData != null) {
                clinicalDataBins = dataBinner.calculateClinicalDataBins(
                    attributeId, filteredClinicalData, filteredIds, disableLogScale);
            }
        }
        
        return new ResponseEntity<>(clinicalDataBins, HttpStatus.OK);
    }

    @RequestMapping(value = "/mutated-genes/fetch", method = RequestMethod.POST, 
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch mutated genes by study view filter")
    public ResponseEntity<List<MutationCountByGene>> fetchMutatedGenes(
        @ApiParam(required = true, value = "Study view filter")
        @Valid @RequestBody StudyViewFilter studyViewFilter) throws StudyNotFoundException {

        List<SampleIdentifier> filteredSampleIdentifiers = studyViewFilterApplier.apply(studyViewFilter);
        List<MutationCountByGene> result = new ArrayList<>();
        if (!filteredSampleIdentifiers.isEmpty()) {
            List<String> studyIds = new ArrayList<>();
            List<String> sampleIds = new ArrayList<>();
            studyViewFilterUtil.extractStudyAndSampleIds(filteredSampleIdentifiers, studyIds, sampleIds);
            result = mutationService.getSampleCountInMultipleMolecularProfiles(molecularProfileService
                .getFirstMutationProfileIds(studyIds, sampleIds), sampleIds, null, true);
            result.sort((a, b) -> b.getCountByEntity() - a.getCountByEntity());
            List<String> distinctStudyIds = studyIds.stream().distinct().collect(Collectors.toList());
            if (distinctStudyIds.size() == 1) {
                Map<Integer, MutSig> mutSigMap = significantlyMutatedGeneService.getSignificantlyMutatedGenes(
                    distinctStudyIds.get(0), Projection.SUMMARY.name(), null, null, null, null).stream().collect(
                        Collectors.toMap(MutSig::getEntrezGeneId, Function.identity()));
                result.forEach(r -> {
                    if (mutSigMap.containsKey(r.getEntrezGeneId())) {
                        r.setqValue(mutSigMap.get(r.getEntrezGeneId()).getqValue());
                    }
                });
            }
        }
        
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/cna-genes/fetch", method = RequestMethod.POST, 
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch CNA genes by study view filter")
    public ResponseEntity<List<CopyNumberCountByGene>> fetchCNAGenes(
        @ApiParam(required = true, value = "Study view filter")
        @Valid @RequestBody StudyViewFilter studyViewFilter) throws StudyNotFoundException {

        List<SampleIdentifier> filteredSampleIdentifiers = studyViewFilterApplier.apply(studyViewFilter);
        List<CopyNumberCountByGene> result = new ArrayList<>();
        if (!filteredSampleIdentifiers.isEmpty()) {
            List<String> studyIds = new ArrayList<>();
            List<String> sampleIds = new ArrayList<>();
            studyViewFilterUtil.extractStudyAndSampleIds(filteredSampleIdentifiers, studyIds, sampleIds);
            result = discreteCopyNumberService.getSampleCountInMultipleMolecularProfiles(molecularProfileService
                .getFirstDiscreteCNAProfileIds(studyIds, sampleIds), sampleIds, null, Arrays.asList(-2, 2), true);
            result.sort((a, b) -> b.getCountByEntity() - a.getCountByEntity());
            List<String> distinctStudyIds = studyIds.stream().distinct().collect(Collectors.toList());
            if (distinctStudyIds.size() == 1) {
                List<Gistic> gisticList = significantCopyNumberRegionService.getSignificantCopyNumberRegions(
                    distinctStudyIds.get(0), Projection.SUMMARY.name(), null, null, null, null);
                Map<Integer, Gistic> gisticMap = new HashMap<>();
                gisticList.forEach(g -> g.getGenes().forEach(gene -> {
                    Gistic gistic = gisticMap.get(gene.getEntrezGeneId());
                    if (gistic == null || g.getqValue().compareTo(gistic.getqValue()) < 0) {
                        gisticMap.put(gene.getEntrezGeneId(), g);
                    }
                }));
                result.forEach(r -> {
                    if (gisticMap.containsKey(r.getEntrezGeneId())) {
                        r.setqValue(gisticMap.get(r.getEntrezGeneId()).getqValue());
                    }
                });
            }
        }
        
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/filtered-samples/fetch", method = RequestMethod.POST, 
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch sample IDs by study view filter")
    public ResponseEntity<List<Sample>> fetchFilteredSamples(
        @ApiParam("Whether to negate the study view filters")
        @RequestParam(defaultValue = "false") Boolean negateFilters,
        @ApiParam(required = true, value = "Study view filter")
        @Valid @RequestBody StudyViewFilter studyViewFilter) {
        
        List<String> studyIds = new ArrayList<>();
        List<String> sampleIds = new ArrayList<>();
        
        studyViewFilterUtil.extractStudyAndSampleIds(
            studyViewFilterApplier.apply(studyViewFilter, negateFilters), studyIds, sampleIds);
        
        List<Sample> result = new ArrayList<>();
        if (!sampleIds.isEmpty()) {
            result = sampleService.fetchSamples(studyIds, sampleIds, Projection.ID.name());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/sample-counts/fetch", method = RequestMethod.POST, 
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch sample counts by study view filter")
    public ResponseEntity<MolecularProfileSampleCount> fetchMolecularProfileSampleCounts(
        @ApiParam(required = true, value = "Study view filter")
        @Valid @RequestBody StudyViewFilter studyViewFilter) {
        
        List<String> studyIds = new ArrayList<>();
        List<String> sampleIds = new ArrayList<>();
        studyViewFilterUtil.extractStudyAndSampleIds(studyViewFilterApplier.apply(studyViewFilter), studyIds, sampleIds);
        MolecularProfileSampleCount molecularProfileSampleCount = new MolecularProfileSampleCount();
        if (sampleIds.isEmpty()) {
            molecularProfileSampleCount.setNumberOfMutationProfiledSamples(0);
            molecularProfileSampleCount.setNumberOfMutationUnprofiledSamples(0);
            molecularProfileSampleCount.setNumberOfCNAProfiledSamples(0);
            molecularProfileSampleCount.setNumberOfCNAUnprofiledSamples(0);
        } else {
            int sampleCount = sampleIds.size();
            List<String> firstMutationProfileIds = molecularProfileService.getFirstMutationProfileIds(studyIds, sampleIds);
            if (!firstMutationProfileIds.isEmpty()) {
                molecularProfileSampleCount.setNumberOfMutationProfiledSamples(Math.toIntExact(genePanelService
                    .fetchGenePanelDataInMultipleMolecularProfiles(firstMutationProfileIds, sampleIds).stream().filter(
                        g -> g.getProfiled()).count()));
                molecularProfileSampleCount.setNumberOfMutationUnprofiledSamples(sampleCount - 
                    molecularProfileSampleCount.getNumberOfMutationProfiledSamples());
            }
            List<String> firstDiscreteCNAProfileIds = molecularProfileService.getFirstDiscreteCNAProfileIds(studyIds, sampleIds);
            if (!firstDiscreteCNAProfileIds.isEmpty()) {
                molecularProfileSampleCount.setNumberOfCNAProfiledSamples(Math.toIntExact(genePanelService
                    .fetchGenePanelDataInMultipleMolecularProfiles(firstDiscreteCNAProfileIds, sampleIds).stream().filter(
                        g -> g.getProfiled()).count()));
                molecularProfileSampleCount.setNumberOfCNAUnprofiledSamples(sampleCount - 
                    molecularProfileSampleCount.getNumberOfCNAProfiledSamples());
            }
        }
        return new ResponseEntity<>(molecularProfileSampleCount, HttpStatus.OK);
    }
    
    private List<ClinicalData> fetchClinicalData(String attributeId, 
                                                 ClinicalDataType clinicalDataType, 
                                                 StudyViewFilter studyViewFilter,
                                                 List<String> ids)
    {
        List<SampleIdentifier> filteredSampleIdentifiers = studyViewFilterApplier.apply(studyViewFilter);

        if (filteredSampleIdentifiers.isEmpty()) {
            return null;
        }

        List<String> studyIds = new ArrayList<>();
        List<String> sampleIds = new ArrayList<>();
        
        ids.clear();
        ids.addAll(extractIds(clinicalDataType, filteredSampleIdentifiers, studyIds, sampleIds));
        
        return clinicalDataService.fetchClinicalData(
            studyIds, ids, Collections.singletonList(attributeId), clinicalDataType.name(), Projection.SUMMARY.name());
    }
    
    private List<String> extractIds(ClinicalDataType clinicalDataType, 
                                    List<SampleIdentifier> filteredSampleIdentifiers,
                                    List<String> studyIds, 
                                    List<String> sampleIds)
    {
        studyViewFilterUtil.extractStudyAndSampleIds(filteredSampleIdentifiers, studyIds, sampleIds);

        return clinicalDataType == ClinicalDataType.SAMPLE ? sampleIds :
            patientService.getPatientsOfSamples(studyIds, sampleIds).stream().map(
                Patient::getStableId).collect(Collectors.toList());
    }
}
