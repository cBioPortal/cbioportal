package org.cbioportal.web.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections.map.MultiKeyMap;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.DiscreteCopyNumberData;
import org.cbioportal.model.GenePanelData;
import org.cbioportal.model.Mutation;
import org.cbioportal.model.Sample;
import org.cbioportal.model.ClinicalDataCountItem.ClinicalDataType;
import org.cbioportal.service.ClinicalDataService;
import org.cbioportal.service.DiscreteCopyNumberService;
import org.cbioportal.service.GenePanelService;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.MutationService;
import org.cbioportal.service.SampleService;
import org.cbioportal.web.parameter.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StudyViewFilterApplier {
    
    private static final String MUTATION_COUNT = "MUTATION_COUNT";

    private static final String FRACTION_GENOME_ALTERED = "FRACTION_GENOME_ALTERED";

    private SampleService sampleService;
    
    private MutationService mutationService;
    
    private DiscreteCopyNumberService discreteCopyNumberService;
    
    private MolecularProfileService molecularProfileService;

    private GenePanelService genePanelService;

    private ClinicalDataService clinicalDataService;
    
    private ClinicalDataEqualityFilterApplier clinicalDataEqualityFilterApplier;
    
    private ClinicalDataIntervalFilterApplier clinicalDataIntervalFilterApplier;
    
    private StudyViewFilterUtil studyViewFilterUtil;

    @Autowired
    public StudyViewFilterApplier(SampleService sampleService, 
                                  MutationService mutationService, 
                                  DiscreteCopyNumberService discreteCopyNumberService, 
                                  MolecularProfileService molecularProfileService, 
                                  GenePanelService genePanelService,
                                  ClinicalDataService clinicalDataService,
                                  ClinicalDataEqualityFilterApplier clinicalDataEqualityFilterApplier, 
                                  ClinicalDataIntervalFilterApplier clinicalDataIntervalFilterApplier, 
                                  StudyViewFilterUtil studyViewFilterUtil) 
    {
        this.sampleService = sampleService;
        this.mutationService = mutationService;
        this.discreteCopyNumberService = discreteCopyNumberService;
        this.molecularProfileService = molecularProfileService;
        this.genePanelService = genePanelService;
        this.clinicalDataService = clinicalDataService;
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
                .map(sampleToSampleIdentifier).collect(Collectors.toList());
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
            sampleIdentifiers = filterMutatedGenes(mutatedGenes, sampleIdentifiers);
        }

        List<CopyNumberGeneFilter> cnaGenes = studyViewFilter.getCnaGenes();
        if (cnaGenes != null && !sampleIdentifiers.isEmpty()) {
            sampleIdentifiers = filterCNAGenes(cnaGenes, sampleIdentifiers);
        }

        Boolean withMutationData = studyViewFilter.getWithMutationData();
        if (withMutationData != null && !sampleIdentifiers.isEmpty()) {
            sampleIdentifiers = filterByProfiled(sampleIdentifiers, withMutationData, molecularProfileService::getFirstMutationProfileIds);
        }

        Boolean withCNAData = studyViewFilter.getWithCNAData();
        if (withCNAData != null && !sampleIdentifiers.isEmpty()) {
            sampleIdentifiers = filterByProfiled(sampleIdentifiers, withCNAData, molecularProfileService::getFirstDiscreteCNAProfileIds);
        }

        RectangleBounds mutationCountVsCNASelection = studyViewFilter.getMutationCountVsCNASelection();
        if (mutationCountVsCNASelection != null && !sampleIdentifiers.isEmpty()) {
            sampleIdentifiers = filterMutationCountVsCNASelection(mutationCountVsCNASelection, sampleIdentifiers);
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

    private List<SampleIdentifier> filterByProfiled(List<SampleIdentifier> sampleIdentifiers, Boolean criteria,
        BiFunction<List<String>, List<String>, List<String>> molecularProfileGetter) {

        List<String> studyIds = new ArrayList<>();
        List<String> sampleIds = new ArrayList<>();
        studyViewFilterUtil.extractStudyAndSampleIds(sampleIdentifiers, studyIds, sampleIds);
        List<String> firstMutationProfileIds = molecularProfileGetter.apply(studyIds, sampleIds);
        List<GenePanelData> genePanelDataList = genePanelService.fetchGenePanelDataInMultipleMolecularProfiles(firstMutationProfileIds, 
            sampleIds).stream().filter(g -> g.getProfiled() == criteria).collect(Collectors.toList());
        return genePanelDataList.stream().map(d -> {
            SampleIdentifier sampleIdentifier = new SampleIdentifier();
            sampleIdentifier.setSampleId(d.getSampleId());
            sampleIdentifier.setStudyId(d.getStudyId());
            return sampleIdentifier;
        }).collect(Collectors.toList());
    }

    private List<SampleIdentifier> filterMutatedGenes(List<MutationGeneFilter> mutatedGenes, List<SampleIdentifier> sampleIdentifiers) {
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

        return sampleIdentifiers;
    }

    private List<SampleIdentifier> filterCNAGenes(List<CopyNumberGeneFilter> cnaGenes, List<SampleIdentifier> sampleIdentifiers) {
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

        return sampleIdentifiers;
    }

    private List<SampleIdentifier> filterMutationCountVsCNASelection(RectangleBounds mutationCountVsCNASelection, List<SampleIdentifier> sampleIdentifiers) {
        List<String> studyIds = new ArrayList<>();
        List<String> sampleIds = new ArrayList<>();
        studyViewFilterUtil.extractStudyAndSampleIds(sampleIdentifiers, studyIds, sampleIds);
        List<ClinicalData> clinicalDataList = clinicalDataService.fetchClinicalData(studyIds, sampleIds, 
            Arrays.asList(MUTATION_COUNT, FRACTION_GENOME_ALTERED), ClinicalDataType.SAMPLE.name(), Projection.SUMMARY.name());
        MultiKeyMap clinicalDataMap = new MultiKeyMap();
        for (ClinicalData clinicalData : clinicalDataList) {
            if (clinicalDataMap.containsKey(clinicalData.getSampleId(), clinicalData.getStudyId())) {
                ((List<ClinicalData>)clinicalDataMap.get(clinicalData.getSampleId(), clinicalData.getStudyId())).add(clinicalData);
            } else {
                List<ClinicalData> clinicalDatas = new ArrayList<>();
                clinicalDatas.add(clinicalData);
                clinicalDataMap.put(clinicalData.getSampleId(), clinicalData.getStudyId(), clinicalDatas);
            }
        }
        List<SampleIdentifier> filteredSampleIdentifiers = new ArrayList<>();
        int index = 0;
        for (String sampleId : sampleIds) {
            String studyId = studyIds.get(index);
            List<ClinicalData> entityClinicalData = (List<ClinicalData>)clinicalDataMap.get(sampleId, studyId);
            if (entityClinicalData == null) {
                continue;
            }
            Optional<ClinicalData> fractionGenomeAlteredData = entityClinicalData.stream().filter(c -> 
                c.getAttrId().equals(FRACTION_GENOME_ALTERED)).findFirst();
            Optional<ClinicalData> mutationCountData = entityClinicalData.stream().filter(c -> 
                c.getAttrId().equals(MUTATION_COUNT)).findFirst();
            
            if (fractionGenomeAlteredData.isPresent() && mutationCountData.isPresent()) {
                BigDecimal fractionGenomeAlteredValue = new BigDecimal(fractionGenomeAlteredData.get().getAttrValue());
                BigDecimal mutationCountValue = new BigDecimal(mutationCountData.get().getAttrValue());
                if (fractionGenomeAlteredValue.compareTo(mutationCountVsCNASelection.getxStart()) >= 0 && 
                    fractionGenomeAlteredValue.compareTo(mutationCountVsCNASelection.getxEnd()) < 0 && 
                    mutationCountValue.compareTo(mutationCountVsCNASelection.getyStart()) >= 0 &&
                    mutationCountValue.compareTo(mutationCountVsCNASelection.getyEnd()) < 0)  {

                    SampleIdentifier sampleIdentifier = new SampleIdentifier();
                    sampleIdentifier.setSampleId(sampleId);
                    sampleIdentifier.setStudyId(studyId);
                    filteredSampleIdentifiers.add(sampleIdentifier);
                }
            }
            
            index++;
        }
        return filteredSampleIdentifiers;
    }
}
