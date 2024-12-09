package org.cbioportal.service.impl;

import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.AlterationCountByStructuralVariant;
import org.cbioportal.model.AlterationFilter;
import org.cbioportal.model.CNA;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.Gene;
import org.cbioportal.model.GeneMolecularData;
import org.cbioportal.model.GenePanelData;
import org.cbioportal.model.GenericAssayData;
import org.cbioportal.model.GenericAssayDataCount;
import org.cbioportal.model.GenericAssayDataCountItem;
import org.cbioportal.model.GenomicDataCount;
import org.cbioportal.model.GenomicDataCountItem;
import org.cbioportal.model.Gistic;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.model.MutSig;
import org.cbioportal.model.MutationFilterOption;
import org.cbioportal.model.util.Select;
import org.cbioportal.service.AlterationCountService;
import org.cbioportal.service.GenePanelService;
import org.cbioportal.service.GeneService;
import org.cbioportal.service.GenericAssayService;
import org.cbioportal.service.MolecularDataService;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.MutationService;
import org.cbioportal.service.SignificantCopyNumberRegionService;
import org.cbioportal.service.SignificantlyMutatedGeneService;
import org.cbioportal.service.StudyViewService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.cbioportal.service.util.MolecularProfileUtil;
import org.cbioportal.web.parameter.GeneIdType;
import org.cbioportal.web.parameter.Projection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class StudyViewServiceImpl implements StudyViewService {
    private static final List<CNA> CNA_TYPES_AMP_AND_HOMDEL = Collections.unmodifiableList(Arrays.asList(CNA.AMP, CNA.HOMDEL));
    private final MolecularProfileService molecularProfileService;
    private final GenePanelService genePanelService;
    private final MolecularProfileUtil molecularProfileUtil;
    private final AlterationCountService alterationCountService;
    private final SignificantlyMutatedGeneService significantlyMutatedGeneService;
    private final SignificantCopyNumberRegionService significantCopyNumberRegionService;
    private final GenericAssayService genericAssayService;
    private final GeneService geneService;
    private final MolecularDataService molecularDataService;
    private final MutationService mutationService;

    // constructor dependency injections
    @Autowired
    public StudyViewServiceImpl(MolecularProfileService molecularProfileService, GenePanelService genePanelService, MolecularProfileUtil molecularProfileUtil, AlterationCountService alterationCountService, SignificantlyMutatedGeneService significantlyMutatedGeneService, SignificantCopyNumberRegionService significantCopyNumberRegionService, GenericAssayService genericAssayService, GeneService geneService, MolecularDataService molecularDataService, MutationService mutationService) {
        this.molecularProfileService = molecularProfileService;
        this.genePanelService = genePanelService;
        this.molecularProfileUtil = molecularProfileUtil;
        this.alterationCountService = alterationCountService;
        this.significantlyMutatedGeneService = significantlyMutatedGeneService;
        this.significantCopyNumberRegionService = significantCopyNumberRegionService;
        this.genericAssayService = genericAssayService;
        this.geneService = geneService;
        this.molecularDataService = molecularDataService;
        this.mutationService = mutationService;
    }

    @Override
    public List<GenomicDataCount> getGenomicDataCounts(List<String> studyIds, List<String> sampleIds) {
        List<MolecularProfileCaseIdentifier> molecularProfileSampleIdentifiers =
            molecularProfileService.getMolecularProfileCaseIdentifiers(studyIds, sampleIds);

        
        // first get all molecular profiles
        List<MolecularProfile> molecularProfiles = molecularProfileService
            .getMolecularProfilesInStudies(new ArrayList<>(new HashSet<>(studyIds)), Projection.SUMMARY.name());
        Map<String, MolecularProfile> molecularProfileMap = molecularProfiles
            .stream()
            .collect(Collectors.toMap(MolecularProfile::getStableId, Function.identity()));

        // get gene panels
        Map<String, Integer> molecularProfileCaseCountSet = genePanelService
            .fetchGenePanelDataInMultipleMolecularProfiles(molecularProfileSampleIdentifiers)
            .stream()
            .filter(GenePanelData::getProfiled)
            .collect(Collectors.groupingBy(GenePanelData::getMolecularProfileId))
            .entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> (int) entry.getValue().stream().map(d -> molecularProfileMap.get(entry.getKey()).getPatientLevel() ? d.getPatientId() : d.getSampleId()).distinct().count()));

        return molecularProfileUtil
            .categorizeMolecularProfilesByStableIdSuffixes(molecularProfiles)
            .entrySet()
            .stream()
            .map(entry -> {
                GenomicDataCount dataCount = new GenomicDataCount();
                dataCount.setValue(entry.getKey());

                Integer count = entry
                    .getValue()
                    .stream()
                    .mapToInt(molecularProfile -> molecularProfileCaseCountSet.getOrDefault(molecularProfile.getStableId(), 0))
                    .sum();

                dataCount.setCount(count);
                dataCount.setLabel(entry.getValue().get(0).getName());
                return dataCount;
            })
            .filter(dataCount -> dataCount.getCount() > 0)
            .toList();
    }

    @Override
    public List<AlterationCountByGene> getMutationAlterationCountByGenes(List<String> studyIds,
                                                                         List<String> sampleIds,
                                                                         AlterationFilter alterationFilter)
        throws StudyNotFoundException {
        List<MolecularProfileCaseIdentifier> caseIdentifiers =
            molecularProfileService.getFirstMutationProfileCaseIdentifiers(studyIds, sampleIds);
        List<AlterationCountByGene> alterationCountByGenes = alterationCountService.getSampleMutationGeneCounts(
            caseIdentifiers,
            Select.all(),
            true,
            false,
            alterationFilter).getFirst();
        annotateDataWithQValue(studyIds, alterationCountByGenes);
        return alterationCountByGenes;
    }

    @Override
    public List<GenomicDataCountItem> getMutationCountsByGeneSpecific(List<String> studyIds,
                                                                      List<String> sampleIds,
                                                                      List<Pair<String, String>> genomicDataFilters,
                                                                      AlterationFilter alterationFilter) {
        List<MolecularProfileCaseIdentifier> caseIdentifiers =
            molecularProfileService.getMutationProfileCaseIdentifiers(studyIds, sampleIds);

        Set<String> hugoGeneSymbols = genomicDataFilters.stream().map(Pair::getKey)
            .collect(Collectors.toSet());

        List<Integer> entrezGeneIds = geneService
            .fetchGenes(new ArrayList<>(hugoGeneSymbols), GeneIdType.HUGO_GENE_SYMBOL.name(),
                Projection.SUMMARY.name())
            .stream()
            .map(Gene::getEntrezGeneId)
            .toList();

        List<AlterationCountByGene> alterationCountByGenes = alterationCountService.getSampleMutationGeneCounts(
            caseIdentifiers,
            Select.byValues(entrezGeneIds),
            true,
            false,
            alterationFilter).getFirst();

        return genomicDataFilters
            .stream()
            .flatMap(genomicDataFilter -> {
                GenomicDataCountItem genomicDataCountItem = new GenomicDataCountItem();
                String hugoGeneSymbol = genomicDataFilter.getKey();
                String profileType = genomicDataFilter.getValue();
                genomicDataCountItem.setHugoGeneSymbol(hugoGeneSymbol);
                genomicDataCountItem.setProfileType(profileType);

                Optional<AlterationCountByGene> filteredAlterationCount = alterationCountByGenes
                    .stream()
                    .filter(g -> StringUtils.isNotEmpty(g.getHugoGeneSymbol()) && g.getHugoGeneSymbol().equals(hugoGeneSymbol))
                    .findFirst();
                
                int totalCount = sampleIds.size();
                int mutatedCount = 0;
                int profiledCount = 0;
                
                if(filteredAlterationCount.isPresent()) {
                    mutatedCount = filteredAlterationCount.get().getNumberOfAlteredCases();
                    profiledCount = filteredAlterationCount.get().getNumberOfProfiledCases();
                }

                List<GenomicDataCount> genomicDataCounts = new ArrayList<>();

                GenomicDataCount genomicDataCountMutated = new GenomicDataCount();
                genomicDataCountMutated.setLabel(MutationFilterOption.MUTATED.getSelectedOption());
                genomicDataCountMutated.setValue(MutationFilterOption.MUTATED.name());
                genomicDataCountMutated.setCount(mutatedCount);
                genomicDataCountMutated.setUniqueCount(mutatedCount);
                if (genomicDataCountMutated.getCount() > 0) genomicDataCounts.add(genomicDataCountMutated);

                GenomicDataCount genomicDataCountWildType = new GenomicDataCount();
                genomicDataCountWildType.setLabel(MutationFilterOption.NOT_MUTATED.getSelectedOption());
                genomicDataCountWildType.setValue(MutationFilterOption.NOT_MUTATED.name());
                genomicDataCountWildType.setCount(profiledCount - mutatedCount);
                genomicDataCountWildType.setUniqueCount(profiledCount - mutatedCount);
                if (genomicDataCountWildType.getCount() > 0) genomicDataCounts.add(genomicDataCountWildType);

                GenomicDataCount genomicDataCountNotProfiled = new GenomicDataCount();
                genomicDataCountNotProfiled.setLabel(MutationFilterOption.NOT_PROFILED.getSelectedOption());
                genomicDataCountNotProfiled.setValue(MutationFilterOption.NOT_PROFILED.name());
                genomicDataCountNotProfiled.setCount(totalCount - profiledCount);
                genomicDataCountNotProfiled.setUniqueCount(totalCount - profiledCount);
                if (genomicDataCountNotProfiled.getCount() > 0) genomicDataCounts.add(genomicDataCountNotProfiled);

                genomicDataCountItem.setCounts(genomicDataCounts);

                return Stream.of(genomicDataCountItem);
            }).toList();
    }

    @Override
    public List<GenomicDataCountItem> getMutationTypeCountsByGeneSpecific(List<String> studyIds,
                                                                          List<String> sampleIds,
                                                                          List<Pair<String, String>> genomicDataFilters) {
        Set<String> hugoGeneSymbols = genomicDataFilters.stream().map(Pair::getKey)
            .collect(Collectors.toSet());

        Map<String, Integer> geneSymbolIdMap = geneService
            .fetchGenes(new ArrayList<>(hugoGeneSymbols), GeneIdType.HUGO_GENE_SYMBOL.name(),
                Projection.SUMMARY.name())
            .stream().collect(Collectors.toMap(Gene::getHugoGeneSymbol, Gene::getEntrezGeneId));

        return genomicDataFilters
            .stream()
            .flatMap(genomicDataFilter -> {
                String hugoGeneSymbol = genomicDataFilter.getKey();
                String profileType = genomicDataFilter.getValue();

                List<Integer> stableIds = Collections.singletonList(geneSymbolIdMap.get(hugoGeneSymbol));

                Pair<List<String>, List<String>> sampleAndProfileIds = getMappedSampleAndProfileIds(studyIds, sampleIds, profileType);
                List<String> mappedSampleIds = sampleAndProfileIds.getFirst();
                List<String> mappedProfileIds = sampleAndProfileIds.getSecond();

                if (mappedSampleIds.isEmpty() || mappedProfileIds.isEmpty()) {
                    return Stream.of();
                }

                GenomicDataCountItem genomicDataCountItem = mutationService.getMutationCountsByType(mappedProfileIds,
                    mappedSampleIds, stableIds, profileType);

                return Stream.ofNullable(genomicDataCountItem);
            }).toList();
    }

    @Override
    public List<AlterationCountByGene> getStructuralVariantAlterationCountByGenes(List<String> studyIds,
                                                                                  List<String> sampleIds,
                                                                                  AlterationFilter alterationFilter)
        throws StudyNotFoundException {
        List<MolecularProfileCaseIdentifier> caseIdentifiers =
            molecularProfileService.getFirstStructuralVariantProfileCaseIdentifiers(studyIds, sampleIds);
        List<AlterationCountByGene> alterationCountByGenes = alterationCountService.getSampleStructuralVariantGeneCounts(
            caseIdentifiers,
            Select.all(),
            true,
            false,
            alterationFilter).getFirst();
        annotateDataWithQValue(studyIds, alterationCountByGenes);
        return alterationCountByGenes;
    }

    @Override
    public List<AlterationCountByStructuralVariant> getStructuralVariantAlterationCounts(List<String> studyIds,
                                                                                         List<String> sampleIds,
                                                                                         AlterationFilter annotationFilters) {
        List<MolecularProfileCaseIdentifier> caseIdentifiers =
            molecularProfileService.getFirstStructuralVariantProfileCaseIdentifiers(studyIds, sampleIds);
        return alterationCountService.getSampleStructuralVariantCounts(caseIdentifiers,
            true,
            false,
            annotationFilters).getFirst();
    }

    private void annotateDataWithQValue(List<String> studyIds, List<AlterationCountByGene> alterationCountByGenes)
        throws StudyNotFoundException {
        Set<String> distinctStudyIds = new HashSet<>(studyIds);
        if (!alterationCountByGenes.isEmpty() && distinctStudyIds.size() == 1) {
            Map<Integer, MutSig> mutSigMap =
                significantlyMutatedGeneService.getSignificantlyMutatedGenes(
                        studyIds.get(0),
                        Projection.SUMMARY.name(),
                        null,
                        null,
                        null,
                        null)
                    .stream()
                    .collect(Collectors.toMap(MutSig::getEntrezGeneId, Function.identity()));
            alterationCountByGenes.forEach(r -> {
                if (mutSigMap.containsKey(r.getEntrezGeneId())) {
                    r.setqValue(mutSigMap.get(r.getEntrezGeneId()).getqValue());
                }
            });
        }
    }

    @Override
    public List<CopyNumberCountByGene> getCNAAlterationCountByGenes(List<String> studyIds,
                                                                    List<String> sampleIds,
                                                                    AlterationFilter alterationFilter)
        throws StudyNotFoundException {
        List<MolecularProfileCaseIdentifier> caseIdentifiers =
            molecularProfileService.getFirstDiscreteCNAProfileCaseIdentifiers(studyIds, sampleIds);

        List<CopyNumberCountByGene> copyNumberCountByGenes = alterationCountService.getSampleCnaGeneCounts(
            caseIdentifiers,
            Select.all(),
            true,
            false,
            alterationFilter).getFirst();
        Set<String> distinctStudyIds = new HashSet<>(studyIds);
        if (distinctStudyIds.size() == 1 && !copyNumberCountByGenes.isEmpty()) {
            List<Gistic> gisticList = significantCopyNumberRegionService.getSignificantCopyNumberRegions(
                studyIds.get(0),
                Projection.SUMMARY.name(),
                null,
                null,
                null,
                null);
            MultiKeyMap gisticMap = new MultiKeyMap();
            gisticList.forEach(g -> g.getGenes().forEach(gene -> {
                Gistic gistic = (Gistic) gisticMap.get(gene.getEntrezGeneId(), g.getAmp());
                if (gistic == null || g.getqValue().compareTo(gistic.getqValue()) < 0) {
                    gisticMap.put(gene.getEntrezGeneId(), g.getAmp(), g);
                }
            }));
            copyNumberCountByGenes.forEach(r -> {
                if (gisticMap.containsKey(r.getEntrezGeneId(), r.getAlteration().equals(2))) {
                    r.setqValue(((Gistic) gisticMap.get(r.getEntrezGeneId(), r.getAlteration().equals(2))).getqValue());
                }
            });
        }
        return copyNumberCountByGenes;
    }

    @Override
    public List<GenomicDataCountItem> getCNAAlterationCountsByGeneSpecific(List<String> studyIds,
                                                                           List<String> sampleIds,
                                                                           List<Pair<String, String>> genomicDataFilters) {
        Set<String> hugoGeneSymbols = genomicDataFilters.stream().map(Pair::getKey)
            .collect(Collectors.toSet());

        Map<String, Integer> geneSymbolIdMap = geneService
            .fetchGenes(new ArrayList<>(hugoGeneSymbols), GeneIdType.HUGO_GENE_SYMBOL.name(),
                Projection.SUMMARY.name())
            .stream().collect(Collectors.toMap(Gene::getHugoGeneSymbol, Gene::getEntrezGeneId));

        return genomicDataFilters
            .stream()
            .flatMap(genomicDataFilter -> {
                GenomicDataCountItem genomicDataCountItem = new GenomicDataCountItem();
                String hugoGeneSymbol = genomicDataFilter.getKey();
                String profileType = genomicDataFilter.getValue();
                genomicDataCountItem.setHugoGeneSymbol(hugoGeneSymbol);
                genomicDataCountItem.setProfileType(profileType);

                List<String> stableIds = List.of(geneSymbolIdMap.get(hugoGeneSymbol).toString());

                Pair<List<String>, List<String>> sampleAndProfileIds = getMappedSampleAndProfileIds(studyIds, sampleIds, profileType);
                List<String> mappedSampleIds = sampleAndProfileIds.getFirst();
                List<String> mappedProfileIds = sampleAndProfileIds.getSecond();

                if (mappedSampleIds.isEmpty()) {
                    return Stream.of();
                }

                List<GeneMolecularData> geneMolecularDataList = molecularDataService.getMolecularDataInMultipleMolecularProfiles(mappedProfileIds, mappedSampleIds,
                    stableIds.stream().map(Integer::parseInt).toList(), Projection.SUMMARY.name());

                List<GenomicDataCount> genomicDataCounts = geneMolecularDataList
                    .stream()
                    .filter(g -> StringUtils.isNotEmpty(g.getValue()) && !g.getValue().equals("NA"))
                    .collect(Collectors.groupingBy(GeneMolecularData::getValue))
                    .entrySet()
                    .stream()
                    .map(entry -> {
                        Integer alteration = Integer.valueOf(entry.getKey());
                        List<GeneMolecularData> geneMolecularData = entry.getValue();
                        int count = geneMolecularData.size();

                        String label = CNA.getByCode(alteration.shortValue()).getDescription();

                        GenomicDataCount genomicDataCount = new GenomicDataCount();
                        genomicDataCount.setLabel(label);
                        genomicDataCount.setValue(String.valueOf(alteration));
                        genomicDataCount.setCount(count);

                        return genomicDataCount;
                    }).collect(Collectors.toList());

                int totalCount = genomicDataCounts.stream().mapToInt(GenomicDataCount::getCount).sum();
                int naCount = sampleIds.size() - totalCount;

                if (naCount > 0) {
                    GenomicDataCount genomicDataCount = new GenomicDataCount();
                    genomicDataCount.setLabel("NA");
                    genomicDataCount.setValue("NA");
                    genomicDataCount.setCount(naCount);
                    genomicDataCounts.add(genomicDataCount);
                }

                genomicDataCountItem.setCounts(genomicDataCounts);
                return Stream.of(genomicDataCountItem);
            }).toList();
    }

    @Override
    public List<GenericAssayDataCountItem> fetchGenericAssayDataCounts(List<String> sampleIds, List<String> studyIds,
                                                                       List<String> stableIds, List<String> profileTypes) {
        if (stableIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<GenericAssayData> data = profileTypes.stream().flatMap(profileType -> {
            Pair<List<String>, List<String>> sampleAndProfileIds = getMappedSampleAndProfileIds(studyIds, sampleIds, profileType);
            List<String> mappedSampleIds = sampleAndProfileIds.getFirst();
            List<String> mappedProfileIds = sampleAndProfileIds.getSecond();

            try {
                return genericAssayService.fetchGenericAssayData(mappedProfileIds, mappedSampleIds, stableIds, Projection.SUMMARY.name()).stream();
            } catch (MolecularProfileNotFoundException e) {
                return new ArrayList<GenericAssayData>().stream();
            }
        }).toList();

        return data
            .stream()
            .filter(g -> StringUtils.isNotEmpty(g.getValue()) && !g.getValue().equals("NA"))
            .collect(Collectors.groupingBy(GenericAssayData::getGenericAssayStableId))
            .entrySet()
            .stream()
            .map(entry -> {
                int totalCount = entry.getValue().size();
                int naCount = sampleIds.size() - totalCount;
                List<GenericAssayDataCount> counts = entry.getValue()
                    .stream()
                    .collect(Collectors.groupingBy(GenericAssayData::getValue))
                    .entrySet()
                    .stream()
                    .map(datum -> {
                        GenericAssayDataCount dataCount = new GenericAssayDataCount();
                        dataCount.setValue(datum.getKey());
                        dataCount.setCount(datum.getValue().size());
                        return dataCount;
                    }).collect(Collectors.toList());

                if (naCount > 0) {
                    GenericAssayDataCount dataCount = new GenericAssayDataCount();
                    dataCount.setValue("NA");
                    dataCount.setCount(naCount);
                    counts.add(dataCount);
                }

                GenericAssayDataCountItem genericAssayDataCountItem = new GenericAssayDataCountItem();
                genericAssayDataCountItem.setStableId(entry.getKey());
                genericAssayDataCountItem.setCounts(counts);
                return genericAssayDataCountItem;
            }).toList();
    }
    
    private Pair<List<String>, List<String>> getMappedSampleAndProfileIds(List<String> studyIds, List<String> sampleIds, String profileType) {
        List<MolecularProfile> molecularProfiles = molecularProfileService.getMolecularProfilesInStudies(studyIds,
            Projection.SUMMARY.name());

        Map<String, List<MolecularProfile>> molecularProfileMap = molecularProfileUtil
            .categorizeMolecularProfilesByStableIdSuffixes(molecularProfiles);

        Map<String, String> studyIdToMolecularProfileIdMap = molecularProfileMap
            .getOrDefault(profileType, new ArrayList<MolecularProfile>()).stream()
            .collect(Collectors.toMap(MolecularProfile::getCancerStudyIdentifier,
                MolecularProfile::getStableId));

        List<String> mappedSampleIds = new ArrayList<>();
        List<String> mappedProfileIds = new ArrayList<>();

        for (int i = 0; i < sampleIds.size(); i++) {
            String studyId = studyIds.get(i);
            
            // add samples only if the studyId is existed in the map
            if (studyIdToMolecularProfileIdMap.containsKey(studyId)) {
                mappedSampleIds.add(sampleIds.get(i));
                mappedProfileIds.add(studyIdToMolecularProfileIdMap.get(studyId));
            }
        }

        return new Pair<>(mappedSampleIds, mappedProfileIds);
    }
}
