package org.cbioportal.web.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.cbioportal.model.DiscreteCopyNumberData;
import org.cbioportal.model.Mutation;
import org.cbioportal.model.Sample;
import org.cbioportal.model.ClinicalDataCountItem.ClinicalDataType;
import org.cbioportal.service.DiscreteCopyNumberService;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.MutationService;
import org.cbioportal.service.SampleService;
import org.cbioportal.web.parameter.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StudyViewFilterApplier {

    
    private SampleService sampleService;
    
    private MutationService mutationService;
    
    private DiscreteCopyNumberService discreteCopyNumberService;
    
    private MolecularProfileService molecularProfileService;
    
    private ClinicalDataEqualityFilterApplier clinicalDataEqualityFilterApplier;
    
    private ClinicalDataIntervalFilterApplier clinicalDataIntervalFilterApplier;
    
    private StudyViewFilterUtil studyViewFilterUtil;

    @Autowired
    public StudyViewFilterApplier(SampleService sampleService, 
                                  MutationService mutationService, 
                                  DiscreteCopyNumberService discreteCopyNumberService, 
                                  MolecularProfileService molecularProfileService, 
                                  ClinicalDataEqualityFilterApplier clinicalDataEqualityFilterApplier, 
                                  ClinicalDataIntervalFilterApplier clinicalDataIntervalFilterApplier, 
                                  StudyViewFilterUtil studyViewFilterUtil) 
    {
        this.sampleService = sampleService;
        this.mutationService = mutationService;
        this.discreteCopyNumberService = discreteCopyNumberService;
        this.molecularProfileService = molecularProfileService;
        this.clinicalDataEqualityFilterApplier = clinicalDataEqualityFilterApplier;
        this.clinicalDataIntervalFilterApplier = clinicalDataIntervalFilterApplier;
        this.studyViewFilterUtil = studyViewFilterUtil;
    }

    Function<Sample, SampleIdentifier> sampleToSampleIdentifier = new Function<Sample, SampleIdentifier>() {
        
        public SampleIdentifier apply(Sample sample) {
            SampleIdentifier sampleIdentifier = new SampleIdentifier();
            sampleIdentifier.setSampleId(sample.getStableId());
            sampleIdentifier.setStudyId(sample.getCancerStudyIdentifier());
            return sampleIdentifier;
        }
    };

    public List<SampleIdentifier> apply(StudyViewFilter studyViewFilter) {
        return this.apply(studyViewFilter, false);
    }
    
    public List<SampleIdentifier> apply(StudyViewFilter studyViewFilter, Boolean negateFilters) {

        List<SampleIdentifier> sampleIdentifiers = new ArrayList<>();
        if (studyViewFilter == null) {
            return sampleIdentifiers;
        }

        if (studyViewFilter != null && studyViewFilter.getSampleIdentifiers() != null && !studyViewFilter.getSampleIdentifiers().isEmpty()) {
            List<String> studyIds = new ArrayList<>();
            List<String> sampleIds = new ArrayList<>();
            studyViewFilterUtil.extractStudyAndSampleIds(studyViewFilter.getSampleIdentifiers(), studyIds, sampleIds);
            sampleIdentifiers = sampleService.fetchSamples(studyIds, sampleIds, Projection.ID.name()).stream()
                .map(sampleToSampleIdentifier).collect(Collectors.toList());;
        } else {
            sampleIdentifiers = sampleService.getAllSamplesInStudies(studyViewFilter.getStudyIds(), Projection.ID.name(), 
                null, null, null, null).stream().map(sampleToSampleIdentifier).collect(Collectors.toList());
        }
        
        List<ClinicalDataEqualityFilter> clinicalDataEqualityFilters = studyViewFilter.getClinicalDataEqualityFilters();
        if (clinicalDataEqualityFilters != null) {
            sampleIdentifiers = equalityFilterClinicalData(sampleIdentifiers, clinicalDataEqualityFilters, ClinicalDataType.SAMPLE, negateFilters);
            sampleIdentifiers = equalityFilterClinicalData(sampleIdentifiers, clinicalDataEqualityFilters, ClinicalDataType.PATIENT, negateFilters);
        }

        List<ClinicalDataIntervalFilter> clinicalDataIntervalFilters = studyViewFilter.getClinicalDataIntervalFilters();
        if (clinicalDataIntervalFilters != null) {
            sampleIdentifiers = intervalFilterClinicalData(sampleIdentifiers, clinicalDataIntervalFilters, ClinicalDataType.SAMPLE, negateFilters);
            sampleIdentifiers = intervalFilterClinicalData(sampleIdentifiers, clinicalDataIntervalFilters, ClinicalDataType.PATIENT, negateFilters);
        }

        List<MutationGeneFilter> mutatedGenes = studyViewFilter.getMutatedGenes();
        if (mutatedGenes != null && !sampleIdentifiers.isEmpty()) {
            for (MutationGeneFilter molecularProfileGeneFilter : mutatedGenes) {
                List<String> studyIds = new ArrayList<>();
                List<String> sampleIds = new ArrayList<>();
                studyViewFilterUtil.extractStudyAndSampleIds(sampleIdentifiers, studyIds, sampleIds);
                List<Mutation> mutations = mutationService.getMutationsInMultipleMolecularProfiles(molecularProfileService
                    .getFirstMutationProfileIds(studyIds, sampleIds), sampleIds, molecularProfileGeneFilter.getEntrezGeneIds(), 
                    Projection.ID.name(), null, null, null, null);
                
                sampleIdentifiers = mutations.stream().map(m -> {
                    SampleIdentifier sampleIdentifier = new SampleIdentifier();
                    sampleIdentifier.setSampleId(m.getSampleId());
                    sampleIdentifier.setStudyId(m.getStudyId());
                    return sampleIdentifier;
                }).distinct().collect(Collectors.toList());
            }
        }

        List<CopyNumberGeneFilter> cnaGenes = studyViewFilter.getCnaGenes();
        if (cnaGenes != null && !sampleIdentifiers.isEmpty()) {
            for (CopyNumberGeneFilter copyNumberGeneFilter : cnaGenes) {
                
                List<String> studyIds = new ArrayList<>();
                List<String> sampleIds = new ArrayList<>();
                studyViewFilterUtil.extractStudyAndSampleIds(sampleIdentifiers, studyIds, sampleIds);
                List<Integer> ampEntrezGeneIds = copyNumberGeneFilter.getAlterations().stream().filter(a -> 
                    a.getAlteration() == 2).map(CopyNumberGeneFilterElement::getEntrezGeneId).collect(Collectors.toList());
                List<DiscreteCopyNumberData> ampCNAList = new ArrayList<>();
                if (!ampEntrezGeneIds.isEmpty()) {
                    ampCNAList = discreteCopyNumberService
                        .getDiscreteCopyNumbersInMultipleMolecularProfiles(molecularProfileService.getFirstDiscreteCNAProfileIds(
                            studyIds, sampleIds), sampleIds, ampEntrezGeneIds, Arrays.asList(2), Projection.ID.name());
                }

                List<Integer> delEntrezGeneIds = copyNumberGeneFilter.getAlterations().stream().filter(a -> 
                    a.getAlteration() == -2).map(CopyNumberGeneFilterElement::getEntrezGeneId).collect(Collectors.toList());
                List<DiscreteCopyNumberData> delCNAList = new ArrayList<>();
                if (!delEntrezGeneIds.isEmpty()) {
                    delCNAList = discreteCopyNumberService
                        .getDiscreteCopyNumbersInMultipleMolecularProfiles(molecularProfileService.getFirstDiscreteCNAProfileIds(
                            studyIds, sampleIds), sampleIds, delEntrezGeneIds, Arrays.asList(-2), Projection.ID.name());
                }

                List<DiscreteCopyNumberData> resultList = new ArrayList<>();
                resultList.addAll(ampCNAList);
                resultList.addAll(delCNAList);
                sampleIdentifiers = resultList.stream().map(d -> {
                    SampleIdentifier sampleIdentifier = new SampleIdentifier();
                    sampleIdentifier.setSampleId(d.getSampleId());
                    sampleIdentifier.setStudyId(d.getStudyId());
                    return sampleIdentifier;
                }).distinct().collect(Collectors.toList());
            }
        }

        return sampleIdentifiers;
    }
    
    private List<SampleIdentifier> intervalFilterClinicalData(List<SampleIdentifier> sampleIdentifiers,
                                                              List<ClinicalDataIntervalFilter> clinicalDataIntervalFilters, 
                                                              ClinicalDataType filterClinicalDataType,
                                                              Boolean negateFilters)
    {
        List<ClinicalDataIntervalFilter> attributes = clinicalDataIntervalFilters.stream()
            .filter(c-> c.getClinicalDataType().equals(filterClinicalDataType)).collect(Collectors.toList());
        
        return clinicalDataIntervalFilterApplier.apply(sampleIdentifiers, attributes, filterClinicalDataType, negateFilters);
    }

    private List<SampleIdentifier> equalityFilterClinicalData(List<SampleIdentifier> sampleIdentifiers, 
                                                              List<ClinicalDataEqualityFilter> clinicalDataEqualityFilters, 
                                                              ClinicalDataType filterClinicalDataType,
                                                              Boolean negateFilters) 
    {
        List<ClinicalDataEqualityFilter> attributes = clinicalDataEqualityFilters.stream()
            .filter(c-> c.getClinicalDataType().equals(filterClinicalDataType)).collect(Collectors.toList());

        return clinicalDataEqualityFilterApplier.apply(sampleIdentifiers, attributes, filterClinicalDataType, negateFilters);
    }
}
