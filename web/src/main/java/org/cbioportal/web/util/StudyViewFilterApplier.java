package org.cbioportal.web.util;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.cbioportal.model.*;
import org.cbioportal.model.MolecularProfile.MolecularAlterationType;
import org.cbioportal.service.*;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.service.util.MolecularProfileUtil;
import org.cbioportal.web.util.appliers.*;
import org.cbioportal.webparam.ClinicalDataFilter;
import org.cbioportal.webparam.ClinicalDataType;
import org.cbioportal.webparam.DataBinCountFilter;
import org.cbioportal.webparam.DataBinFilter;
import org.cbioportal.webparam.DataBinMethod;
import org.cbioportal.webparam.DataFilter;
import org.cbioportal.webparam.DiscreteCopyNumberEventType;
import org.cbioportal.webparam.GeneFilter;
import org.cbioportal.webparam.GeneFilterQuery;
import org.cbioportal.webparam.GeneIdType;
import org.cbioportal.webparam.GenericAssayDataBinCountFilter;
import org.cbioportal.webparam.GenericAssayDataBinFilter;
import org.cbioportal.webparam.GenericAssayDataFilter;
import org.cbioportal.webparam.GenomicDataBinCountFilter;
import org.cbioportal.webparam.GenomicDataBinFilter;
import org.cbioportal.webparam.GenomicDataFilter;
import org.cbioportal.webparam.Projection;
import org.cbioportal.webparam.SampleIdentifier;
import org.cbioportal.webparam.StructuralVariantFilter;
import org.cbioportal.webparam.StudyViewFilter;
import org.cbioportal.webparam.StudyViewGeneFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class StudyViewFilterApplier {
    @Autowired
    private ApplicationContext applicationContext;
    StudyViewFilterApplier instance;
    
    // This gets initialized and overwritten. We do this because Spring's unit tests
    // don't know how to autowire this, even though production Spring does. If we 
    // don't give this an initial value, we get NPEs.
    @Autowired
    private List<StudyViewSubFilterApplier> subFilterAppliers = new ArrayList<>();
    
    @PostConstruct
    private void init() {
        instance = applicationContext.getBean(StudyViewFilterApplier.class);
    }

    @Autowired
    private SampleService sampleService;
    @Autowired
    private MutationService mutationService;
    @Autowired
    private DiscreteCopyNumberService discreteCopyNumberService;
    @Autowired
    private MolecularProfileService molecularProfileService;
    @Autowired
    private GenePanelService genePanelService;
    @Autowired
    private ClinicalDataEqualityFilterApplier clinicalDataEqualityFilterApplier;
    @Autowired
    private ClinicalDataIntervalFilterApplier clinicalDataIntervalFilterApplier;
    @Autowired
    private CustomDataFilterApplier customDataFilterApplier;
    @Autowired
    private StudyViewFilterUtil studyViewFilterUtil;
    @Autowired
    private GeneService geneService;
    @Autowired
    private ClinicalAttributeService clinicalAttributeService;
    @Autowired
    private SampleListService sampleListService;
    @Autowired
    private MolecularDataService molecularDataService;
    @Autowired
    private GenericAssayService genericAssayService;
    @Autowired
    private DataBinner dataBinner;
    @Autowired
    private StructuralVariantService structuralVariantService;
    @Autowired
    private MolecularProfileUtil molecularProfileUtil;

    Function<Sample, SampleIdentifier> sampleToSampleIdentifier = new Function<Sample, SampleIdentifier>() {

        public SampleIdentifier apply(Sample sample) {
            SampleIdentifier sampleIdentifier = new SampleIdentifier();
            sampleIdentifier.setSampleId(sample.getStableId());
            sampleIdentifier.setStudyId(sample.getCancerStudyIdentifier());
            return sampleIdentifier;
        }
    };

    public List<SampleIdentifier> apply(StudyViewFilter studyViewFilter) {
        return (instance == null ? this : instance).cachedApply(studyViewFilter);
    }

    @Cacheable(
        cacheResolver = "generalRepositoryCacheResolver",
        condition = "@cacheEnabledConfig.getEnabled()"
    )
    public List<SampleIdentifier> cachedApply(StudyViewFilter studyViewFilter) {
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

        List<String> studyIds = sampleIdentifiers.stream().map(SampleIdentifier::getStudyId).distinct()
                .collect(Collectors.toList());

        List<ClinicalDataFilter> clinicalDataEqualityFilters = new ArrayList<>();
        List<ClinicalDataFilter> clinicalDataIntervalFilters = new ArrayList<>();

        List<ClinicalDataFilter> clinicalDataFilters = studyViewFilter.getClinicalDataFilters();

        if (!CollectionUtils.isEmpty(clinicalDataFilters)) {
            List<String> attributeIds = clinicalDataFilters.stream().map(ClinicalDataFilter::getAttributeId)
                    .collect(Collectors.toList());
            List<ClinicalAttribute> clinicalAttributes = clinicalAttributeService
                    .getClinicalAttributesByStudyIdsAndAttributeIds(studyIds, attributeIds);

            Map<String, ClinicalAttribute> clinicalAttributeMap = clinicalAttributes.stream()
                    .collect(Collectors.toMap(ClinicalAttribute::getAttrId, Function.identity(), (a, b) -> {
                        return a.getDatatype().equals("STRING") ? a : b;
                    }));

            clinicalDataFilters.forEach(clinicalDataFilter -> {
                String attributeId = clinicalDataFilter.getAttributeId();
                if (clinicalAttributeMap.containsKey(attributeId)) {
                    if (clinicalAttributeMap.get(attributeId).getDatatype().equals("STRING")) {
                        clinicalDataEqualityFilters.add(clinicalDataFilter);
                    } else {
                        clinicalDataIntervalFilters.add(clinicalDataFilter);
                    }
                }
            });
        }

        if (!CollectionUtils.isEmpty(clinicalDataEqualityFilters)) {
            sampleIdentifiers = equalityFilterClinicalData(sampleIdentifiers, clinicalDataEqualityFilters, negateFilters);
        }

        if (!CollectionUtils.isEmpty(clinicalDataIntervalFilters)) {
            sampleIdentifiers = intervalFilterClinicalData(sampleIdentifiers, clinicalDataIntervalFilters, negateFilters);
        }

        if (!CollectionUtils.isEmpty(studyViewFilter.getCustomDataFilters())) {
            sampleIdentifiers = customDataFilterApplier.apply(sampleIdentifiers, studyViewFilter.getCustomDataFilters(),
                    negateFilters);
        }

        List<MolecularProfile> molecularProfiles = null;
        if (!CollectionUtils.isEmpty(studyViewFilter.getGeneFilters())
                || !CollectionUtils.isEmpty(studyViewFilter.getGenomicDataFilters())
                || !CollectionUtils.isEmpty(studyViewFilter.getGenericAssayDataFilters())
                || !CollectionUtils.isEmpty(studyViewFilter.getGenomicProfiles())) {

            molecularProfiles = molecularProfileService.getMolecularProfilesInStudies(studyIds, "SUMMARY");
        }

        List<GenomicDataFilter> genomicDataEqualityFilters = new ArrayList<>();
        List<GenomicDataFilter> genomicDataIntervalFilters = new ArrayList<>();

        List<GenomicDataFilter> genomicDataFilters = studyViewFilter.getGenomicDataFilters();

        if (!CollectionUtils.isEmpty(genomicDataFilters)) {
            Map<String, MolecularProfile> molecularProfileMapByType = molecularProfileUtil
                .categorizeMolecularProfilesByStableIdSuffixes(molecularProfiles)
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get(0)));

            genomicDataFilters.forEach(genomicDataFilter -> {
                String profileType = genomicDataFilter.getProfileType();
                if (molecularProfileMapByType.containsKey(profileType)) {
                    if (molecularProfileMapByType.get(profileType).getDatatype().equals("DISCRETE")) {
                        genomicDataEqualityFilters.add(genomicDataFilter);
                    } else {
                        genomicDataIntervalFilters.add(genomicDataFilter);
                    }
                }
            });
        }
        
        if (!CollectionUtils.isEmpty(genomicDataEqualityFilters)) {
            sampleIdentifiers = equalityFilterExpressionData(sampleIdentifiers, molecularProfiles,
                genomicDataEqualityFilters, negateFilters);
        }
        
        if (!CollectionUtils.isEmpty(genomicDataIntervalFilters)) {
            sampleIdentifiers = intervalFilterExpressionData(sampleIdentifiers, molecularProfiles,
                genomicDataIntervalFilters, negateFilters);
        }

        sampleIdentifiers = intervalFilterExpressionData(sampleIdentifiers, molecularProfiles,
                studyViewFilter.getGenericAssayDataFilters(), negateFilters);

        if (!CollectionUtils.isEmpty(studyViewFilter.getGeneFilters())) {
            Map<String, MolecularProfile> molecularProfileMap = molecularProfiles.stream()
                    .collect(Collectors.toMap(MolecularProfile::getStableId, Function.identity()));
            List<StudyViewGeneFilter> mutatedGeneFilters = new ArrayList<>();
            List<StudyViewGeneFilter> structuralVariantGeneFilters = new ArrayList<>();
            List<StudyViewGeneFilter> cnaGeneFilters = new ArrayList<>();

            splitGeneFiltersByMolecularAlterationType(studyViewFilter.getGeneFilters(), molecularProfileMap,
                    mutatedGeneFilters, structuralVariantGeneFilters, cnaGeneFilters);

            if ((mutatedGeneFilters.size() + structuralVariantGeneFilters.size() + cnaGeneFilters.size()) == studyViewFilter
                    .getGeneFilters().size()) {
                if (!mutatedGeneFilters.isEmpty()) {
                    sampleIdentifiers = filterMutatedGenes(mutatedGeneFilters, molecularProfileMap, sampleIdentifiers);
                }
                if (!structuralVariantGeneFilters.isEmpty()) {
                    sampleIdentifiers = filterStructuralVariantGenes(structuralVariantGeneFilters, molecularProfileMap,
                            sampleIdentifiers);
                }
                if (!cnaGeneFilters.isEmpty()) {
                    sampleIdentifiers = filterCNAGenes(cnaGeneFilters, molecularProfileMap, sampleIdentifiers);
                }

            } else {
                return new ArrayList<>();
            }
        }

        if (!CollectionUtils.isEmpty(studyViewFilter.getGenomicProfiles())) {
            Map<String, List<SampleIdentifier>> groupStudySampleIdentifiers = sampleIdentifiers.stream()
                    .collect(Collectors.groupingBy(SampleIdentifier::getStudyId));

            Map<String, List<MolecularProfile>> molecularProfileSet = molecularProfileUtil
                    .categorizeMolecularProfilesByStableIdSuffixes(molecularProfiles);

            List<MolecularProfileCaseIdentifier> molecularProfileSampleIdentifiers = new ArrayList<>();

            studyViewFilter.getGenomicProfiles().stream().forEach(profileValues -> {
                profileValues.stream().forEach(profileValue -> {
                    molecularProfileSet.getOrDefault(profileValue, new ArrayList<>()).stream().forEach(profile -> {
                        groupStudySampleIdentifiers.getOrDefault(profile.getCancerStudyIdentifier(), new ArrayList<>())
                                .forEach(sampleIdentifier -> {
                                    MolecularProfileCaseIdentifier profileCaseIdentifier = new MolecularProfileCaseIdentifier();
                                    profileCaseIdentifier.setMolecularProfileId(profile.getStableId());
                                    profileCaseIdentifier.setCaseId(sampleIdentifier.getSampleId());
                                    molecularProfileSampleIdentifiers.add(profileCaseIdentifier);
                                });
                    });

                });
            });

            List<GenePanelData> genePanelData = genePanelService
                    .fetchGenePanelDataInMultipleMolecularProfiles(molecularProfileSampleIdentifiers);

            for (List<String> profileValues : studyViewFilter.getGenomicProfiles()) {
                Map<String, MolecularProfile> profileMap = profileValues.stream().flatMap(
                        profileValue -> molecularProfileSet.getOrDefault(profileValue, new ArrayList<>()).stream())
                        .collect(Collectors.toMap(MolecularProfile::getStableId, Function.identity()));

                Set<SampleIdentifier> filteredSampleIdentifiers = new HashSet<>();
                genePanelData.forEach(datum -> {
                    if (datum.getProfiled() && profileMap.containsKey(datum.getMolecularProfileId())) {
                        SampleIdentifier sampleIdentifier = new SampleIdentifier();
                        sampleIdentifier.setStudyId(datum.getStudyId());
                        sampleIdentifier.setSampleId(datum.getSampleId());
                        filteredSampleIdentifiers.add(sampleIdentifier);
                    }
                });
                sampleIdentifiers.retainAll(filteredSampleIdentifiers);
            }
        }

        if (!CollectionUtils.isEmpty(studyViewFilter.getCaseLists())) {
            List<SampleList> sampleLists = sampleListService.getAllSampleListsInStudies(studyIds,
                    Projection.DETAILED.name());
            Map<String, List<SampleList>> groupedSampleListByListType = studyViewFilterUtil
                    .categorizeSampleLists(sampleLists);

            for (List<String> sampleListTypes : studyViewFilter.getCaseLists()) {
                List<SampleIdentifier> filteredSampleIdentifiers = sampleListTypes.stream()
                        .flatMap(sampleListType -> groupedSampleListByListType
                                .getOrDefault(sampleListType, new ArrayList<>()).stream().flatMap(sampleList -> {
                                    return sampleList.getSampleIds().stream().map(sampleId -> {
                                        SampleIdentifier sampleIdentifier = new SampleIdentifier();
                                        sampleIdentifier.setStudyId(sampleList.getCancerStudyIdentifier());
                                        sampleIdentifier.setSampleId(sampleId);
                                        return sampleIdentifier;
                                    });
                                }))
                        .collect(Collectors.toList());

                sampleIdentifiers.retainAll(filteredSampleIdentifiers);
            }
        }

        return chainSubFilters(studyViewFilter, sampleIdentifiers);
    }
    
    private List<SampleIdentifier> chainSubFilters(StudyViewFilter studyViewFilter, List<SampleIdentifier> sampleIdentifiers) {
        for (StudyViewSubFilterApplier subFilterApplier : subFilterAppliers) {
            if (!sampleIdentifiers.isEmpty() && subFilterApplier.shouldApplyFilter(studyViewFilter)) {
                sampleIdentifiers = subFilterApplier.filter(sampleIdentifiers, studyViewFilter);
            }
        }
        
        return sampleIdentifiers;
    }

    private List<SampleIdentifier> intervalFilterClinicalData(List<SampleIdentifier> sampleIdentifiers,
                                                              List<ClinicalDataFilter> clinicalDataIntervalFilters,
                                                              Boolean negateFilters) {
        return clinicalDataIntervalFilterApplier.apply(sampleIdentifiers, clinicalDataIntervalFilters, negateFilters);
    }

    private List<SampleIdentifier> equalityFilterClinicalData(List<SampleIdentifier> sampleIdentifiers,
                                                              List<ClinicalDataFilter> clinicalDataEqualityFilters,
                                                              Boolean negateFilters) {
        return clinicalDataEqualityFilterApplier.apply(sampleIdentifiers, clinicalDataEqualityFilters, negateFilters);
    }

    private List<SampleIdentifier> filterMutatedGenes(List<StudyViewGeneFilter> mutatedGenefilters,
            Map<String, MolecularProfile> molecularProfileMap, List<SampleIdentifier> sampleIdentifiers) {

        if (sampleIdentifiers == null || sampleIdentifiers.isEmpty()) {
            return new ArrayList<>();
        }

        for (GeneFilter genefilter : mutatedGenefilters) {

            List<MolecularProfile> filteredMolecularProfiles = genefilter
                    .getMolecularProfileIds()
                    .stream()
                    .map(molecularProfileId -> molecularProfileMap.get(molecularProfileId))
                    .collect(Collectors.toList());

            Map<String, List<MolecularProfile>> mapByStudyId = filteredMolecularProfiles
                    .stream()
                    .collect(Collectors.groupingBy(MolecularProfile::getCancerStudyIdentifier));

            for (List<GeneFilterQuery> geneQueries : genefilter.getGeneQueries()) {
                List<String> studyIds = new ArrayList<>();
                List<String> sampleIds = new ArrayList<>();

                List<String> hugoGeneSymbols = geneQueries
                        .stream()
                        .map(GeneFilterQuery::getHugoGeneSymbol)
                        .collect(Collectors.toList());

                Map<String, Integer> symbolToEntrezGeneId = geneService
                    .fetchGenes(new ArrayList<>(hugoGeneSymbols),
                        GeneIdType.HUGO_GENE_SYMBOL.name(), Projection.SUMMARY.name())
                    .stream()
                    .collect(Collectors.toMap(Gene::getHugoGeneSymbol, Gene::getEntrezGeneId));

                geneQueries.removeIf(
                    q -> !symbolToEntrezGeneId.containsKey(q.getHugoGeneSymbol())
                );

                geneQueries.stream().forEach(
                    q -> q.setEntrezGeneId(symbolToEntrezGeneId.get(q.getHugoGeneSymbol()))
                );
                studyViewFilterUtil.extractStudyAndSampleIds(sampleIdentifiers, studyIds, sampleIds);

                List<String> molecularProfileIds = new ArrayList<>();
                int removedSampleCount = 0;
                for (int i = 0; i < studyIds.size(); i++) {
                    String studyId = studyIds.get(i);
                    if (mapByStudyId.containsKey(studyId)) {
                        molecularProfileIds.add(mapByStudyId.get(studyId).get(0).getStableId());
                    } else {
                        sampleIds.remove(i - removedSampleCount);
                        removedSampleCount++;
                    }
                }

                sampleIdentifiers = mutationService
                        .getMutationsInMultipleMolecularProfilesByGeneQueries(molecularProfileIds, sampleIds, geneQueries,
                                Projection.ID.name(), null, null, null, null)
                        .stream()
                        .map(m -> {
                            SampleIdentifier sampleIdentifier = new SampleIdentifier();
                            sampleIdentifier.setSampleId(m.getSampleId());
                            sampleIdentifier.setStudyId(m.getStudyId());
                            return sampleIdentifier;
                        })
                        .distinct()
                        .collect(Collectors.toList());
            }

        }
        return sampleIdentifiers;
    }

    private List<SampleIdentifier> filterStructuralVariantGenes(List<StudyViewGeneFilter> svGenefilters,
            Map<String, MolecularProfile> molecularProfileMap, List<SampleIdentifier> sampleIdentifiers) {
            
        if (sampleIdentifiers == null || sampleIdentifiers.isEmpty()) {
            return new ArrayList<>();
        }

        for (GeneFilter genefilter : svGenefilters) {

            List<MolecularProfile> filteredMolecularProfiles = genefilter
                    .getMolecularProfileIds()
                    .stream()
                    .map(molecularProfileId -> molecularProfileMap.get(molecularProfileId))
                    .collect(Collectors.toList());

            Map<String, List<MolecularProfile>> mapByStudyId = filteredMolecularProfiles
                    .stream()
                    .collect(Collectors.groupingBy(MolecularProfile::getCancerStudyIdentifier));

            for (List<GeneFilterQuery> geneQueries : genefilter.getGeneQueries()) {
                List<String> studyIds = new ArrayList<>();
                List<String> sampleIds = new ArrayList<>();

                List<String> hugoGeneSymbols = geneQueries
                    .stream()
                    .map(GeneFilterQuery::getHugoGeneSymbol)
                    .collect(Collectors.toList());

                Map<String, Integer> symbolToEntrezGeneId = geneService
                    .fetchGenes(new ArrayList<>(hugoGeneSymbols),
                        GeneIdType.HUGO_GENE_SYMBOL.name(), Projection.SUMMARY.name())
                    .stream()
                    .collect(Collectors.toMap(Gene::getHugoGeneSymbol, Gene::getEntrezGeneId));

                geneQueries.removeIf(
                    q -> !symbolToEntrezGeneId.containsKey(q.getHugoGeneSymbol())
                );

                geneQueries.stream().forEach(
                    q -> q.setEntrezGeneId(symbolToEntrezGeneId.get(q.getHugoGeneSymbol()))
                );

                studyViewFilterUtil.extractStudyAndSampleIds(sampleIdentifiers, studyIds, sampleIds);

                List<String> molecularProfileIds = new ArrayList<>();
                int removedSampleCount = 0;
                for (int i = 0; i < studyIds.size(); i++) {
                    String studyId = studyIds.get(i);
                    if (mapByStudyId.containsKey(studyId)) {
                        molecularProfileIds.add(mapByStudyId.get(studyId).get(0).getStableId());
                    } else {
                        sampleIds.remove(i - removedSampleCount);
                        removedSampleCount++;
                    }
                }

                sampleIdentifiers = structuralVariantService
                        .fetchStructuralVariantsByGeneQueries(molecularProfileIds, sampleIds, geneQueries)
                        .stream()
                        .map(m -> {
                            SampleIdentifier sampleIdentifier = new SampleIdentifier();
                            sampleIdentifier.setSampleId(m.getSampleId());
                            sampleIdentifier.setStudyId(m.getStudyId());
                            return sampleIdentifier;
                        })
                        .distinct()
                        .collect(Collectors.toList());
            }

        }
        return sampleIdentifiers;
    }

    private List<SampleIdentifier> filterCNAGenes(List<StudyViewGeneFilter> cnaGeneFilters,
            Map<String, MolecularProfile> molecularProfileMap, List<SampleIdentifier> sampleIdentifiers) {
        
        if (sampleIdentifiers == null || sampleIdentifiers.isEmpty()) {
            return new ArrayList<>();
        }

        for (GeneFilter geneFilter : cnaGeneFilters) {

            List<MolecularProfile> filteredMolecularProfiles = geneFilter.getMolecularProfileIds().stream()
                    .map(molecularProfileId -> molecularProfileMap.get(molecularProfileId))
                    .collect(Collectors.toList());

            for (List<GeneFilterQuery> geneQueries : geneFilter.getGeneQueries()) {

                List<String> studyIds = new ArrayList<>();
                List<String> sampleIds = new ArrayList<>();
                studyViewFilterUtil.extractStudyAndSampleIds(sampleIdentifiers, studyIds, sampleIds);

                List<String> molecularProfileIds = new ArrayList<>();
                Map<String, List<MolecularProfile>> mapByStudyId = filteredMolecularProfiles.stream()
                        .collect(Collectors.groupingBy(MolecularProfile::getCancerStudyIdentifier));
                int removedSampleCount = 0;
                for (int i = 0; i < studyIds.size(); i++) {
                    String studyId = studyIds.get(i);
                    if (mapByStudyId.containsKey(studyId)) {
                        molecularProfileIds.add(mapByStudyId.get(studyId).get(0).getStableId());
                    } else {
                        sampleIds.remove(i - removedSampleCount);
                        removedSampleCount++;
                    }
                }

                List<DiscreteCopyNumberData> resultList = DiscreteCopyNumberEventType.ALL
                        .getAlterationTypes().stream().flatMap(alterationType -> {

                            List<GeneFilterQuery> filteredGeneQueries = geneQueries.stream()
                                .filter(geneQuery -> geneQuery.getAlterations().stream()
                                    .filter(alteration -> alteration.getCode() == alterationType)
                                    .count() > 0)
                                .collect(Collectors.toList());

                            List<String> hugoGeneSymbols = filteredGeneQueries.stream()
                                    .map(GeneFilterQuery::getHugoGeneSymbol).collect(Collectors.toList());

                            Map<String, Integer> symbolToEntrezGeneId = geneService
                                .fetchGenes(new ArrayList<>(hugoGeneSymbols),
                                    GeneIdType.HUGO_GENE_SYMBOL.name(), Projection.SUMMARY.name())
                                .stream().collect(Collectors.toMap(x -> x.getHugoGeneSymbol(), x -> x.getEntrezGeneId()));

                            filteredGeneQueries.removeIf(
                                q -> !symbolToEntrezGeneId.containsKey(q.getHugoGeneSymbol())
                            );

                            filteredGeneQueries.stream().forEach(
                                q -> q.setEntrezGeneId(symbolToEntrezGeneId.get(q.getHugoGeneSymbol()))
                            );

                            List<DiscreteCopyNumberData> copyNumberDatas = new ArrayList<>();
                            if (!filteredGeneQueries.isEmpty()) {
                                copyNumberDatas = discreteCopyNumberService
                                    .getDiscreteCopyNumbersInMultipleMolecularProfilesByGeneQueries(
                                        molecularProfileIds,
                                        sampleIds,
                                        filteredGeneQueries,
                                        Projection.ID.name());
                            }
                            return copyNumberDatas.stream();
                        }).collect(Collectors.toList());

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

    private void splitGeneFiltersByMolecularAlterationType(List<StudyViewGeneFilter> genefilters,
            Map<String, MolecularProfile> molecularProfileMap, List<StudyViewGeneFilter> mutatedGeneFilters,
            List<StudyViewGeneFilter> structuralVariantGeneFilters, List<StudyViewGeneFilter> cnaGeneFilters) {

        for (StudyViewGeneFilter genefilter : genefilters) {

            List<MolecularProfile> filteredMolecularProfiles = genefilter.getMolecularProfileIds().stream()
                    // need this filter criteria since profile id might be present
                    // in filter but the study might already been filtered out
                    .filter(molecularProfileMap::containsKey)
                    .map(molecularProfileMap::get)
                    .collect(Collectors.toList());

            Set<MolecularProfile.MolecularAlterationType> alterationTypes = filteredMolecularProfiles.stream()
                    .map(MolecularProfile::getMolecularAlterationType)
                    .collect(Collectors.toSet());

            Set<String> dataTypes = filteredMolecularProfiles.stream().map(MolecularProfile::getDatatype)
                    .collect(Collectors.toSet());

            Set<String> filteredMolecularProfileIds = filteredMolecularProfiles
                    .stream()
                    .map(MolecularProfile::getStableId)
                    .collect(Collectors.toSet());
            genefilter.setMolecularProfileIds(filteredMolecularProfileIds);

            if (alterationTypes.size() == 1) {
                MolecularProfile.MolecularAlterationType alterationType = alterationTypes.iterator().next();

                if (alterationType.equals(MolecularAlterationType.STRUCTURAL_VARIANT)) {
                    structuralVariantGeneFilters.add(genefilter);
                } else if (alterationType == MolecularAlterationType.MUTATION_EXTENDED) {
                    mutatedGeneFilters.add(genefilter);
                } else if (alterationType == MolecularProfile.MolecularAlterationType.COPY_NUMBER_ALTERATION
                        && dataTypes.size() == 1 && dataTypes.iterator().next().equals("DISCRETE")) {
                    cnaGeneFilters.add(genefilter);
                }
            }
        }
    }

    public List<String> getUniqkeyKeys(List<String> studyIds, List<String> caseIds) {
        List<String> uniqkeyKeys = new ArrayList<String>();
        for (int i = 0; i < caseIds.size(); i++) {
            uniqkeyKeys.add(studyViewFilterUtil.getCaseUniqueKey(studyIds.get(i), caseIds.get(i)));
        }
        return uniqkeyKeys;
    }

    public <T extends DataBinCountFilter, S extends DataBinFilter, U extends DataBin> List<U> getDataBins(
        DataBinMethod dataBinMethod, T dataBinCountFilter) {
        List<S> dataBinFilters = fetchDataBinFilters(dataBinCountFilter);

        StudyViewFilter studyViewFilter = dataBinCountFilter.getStudyViewFilter();

        if (dataBinFilters.size() == 1) {
            removeSelfFromFilter(dataBinFilters.get(0), studyViewFilter);
        }

        List<U> resultDataBins;
        List<String> filteredSampleIds = new ArrayList<>();
        List<String> filteredStudyIds = new ArrayList<>();
        List<Binnable> filteredData = fetchData(dataBinCountFilter, studyViewFilter, filteredSampleIds,
                filteredStudyIds);

        List<String> filteredUniqueSampleKeys = getUniqkeyKeys(filteredStudyIds, filteredSampleIds);

        Map<String, List<Binnable>> filteredClinicalDataByAttributeId = filteredData.stream()
                .collect(Collectors.groupingBy(Binnable::getAttrId));

        if (dataBinMethod == DataBinMethod.STATIC) {

            StudyViewFilter filter = studyViewFilter == null ? null : new StudyViewFilter();
            if (filter != null) {
                filter.setStudyIds(studyViewFilter.getStudyIds());
                filter.setSampleIdentifiers(studyViewFilter.getSampleIdentifiers());
            }

            List<String> unfilteredSampleIds = new ArrayList<>();
            List<String> unfilteredStudyIds = new ArrayList<>();
            List<Binnable> unfilteredData = fetchData(dataBinCountFilter, filter, unfilteredSampleIds,
                    unfilteredStudyIds);

            List<String> unFilteredUniqueSampleKeys = getUniqkeyKeys(unfilteredSampleIds, unfilteredStudyIds);

            Map<String, List<Binnable>> unfilteredDataByAttributeId = unfilteredData.stream()
                    .collect(Collectors.groupingBy(Binnable::getAttrId));

            resultDataBins = dataBinFilters.stream().flatMap(dataBinFilter -> {
                String attributeId = getAttributeUniqueKey(dataBinFilter);
                return dataBinner
                        .calculateClinicalDataBins(dataBinFilter, ClinicalDataType.SAMPLE,
                                filteredClinicalDataByAttributeId.getOrDefault(attributeId, Collections.emptyList()),
                                unfilteredDataByAttributeId.getOrDefault(attributeId, Collections.emptyList()),
                                filteredUniqueSampleKeys, unFilteredUniqueSampleKeys)
                        .stream().map(dataBin -> (U) transform(dataBinFilter, dataBin));

            }).collect(Collectors.toList());

        } else { // dataBinMethod == DataBinMethod.DYNAMIC
            resultDataBins = (List<U>) dataBinFilters.stream().flatMap(dataBinFilter -> {
                return dataBinner
                        .calculateDataBins(dataBinFilter, ClinicalDataType.SAMPLE,
                                filteredClinicalDataByAttributeId.getOrDefault(getAttributeUniqueKey(dataBinFilter),
                                        Collections.emptyList()),
                                filteredUniqueSampleKeys)
                        .stream().map(dataBin -> (U) transform(dataBinFilter, dataBin));
            }).collect(Collectors.toList());
        }

        return resultDataBins;
    }

    private <S extends DataBinCountFilter> List<Binnable> fetchData(
        S dataBinCountFilter,
        StudyViewFilter studyViewFilter, 
        List<String> sampleIds, 
        List<String> studyIds
    ) {

        List<SampleIdentifier> filteredSampleIdentifiers = apply(studyViewFilter);
        studyViewFilterUtil.extractStudyAndSampleIds(filteredSampleIdentifiers, studyIds, sampleIds);

        List<MolecularProfile> molecularProfiles = molecularProfileService.getMolecularProfilesInStudies(studyIds,
                "SUMMARY");

        Map<String, List<MolecularProfile>> molecularProfileMap = molecularProfileUtil
            .categorizeMolecularProfilesByStableIdSuffixes(molecularProfiles);

        if (dataBinCountFilter instanceof GenomicDataBinCountFilter) {
            GenomicDataBinCountFilter genomicDataBinCountFilter = (GenomicDataBinCountFilter) dataBinCountFilter;
            List<GenomicDataBinFilter> genomicDataBinFilters = genomicDataBinCountFilter.getGenomicDataBinFilters();

            Set<String> hugoGeneSymbols = genomicDataBinFilters.stream().map(GenomicDataBinFilter::getHugoGeneSymbol)
                    .collect(Collectors.toSet());

            Map<String, Integer> geneSymbolIdMap = geneService
                    .fetchGenes(new ArrayList<>(hugoGeneSymbols), GeneIdType.HUGO_GENE_SYMBOL.name(),
                            Projection.SUMMARY.name())
                    .stream().collect(Collectors.toMap(Gene::getHugoGeneSymbol, Gene::getEntrezGeneId));

            return genomicDataBinFilters.stream().flatMap(genomicDataFilter -> {

                Map<String, String> studyIdToMolecularProfileIdMap = molecularProfileMap
                        .getOrDefault(genomicDataFilter.getProfileType(), new ArrayList<MolecularProfile>()).stream()
                        .collect(Collectors.toMap(MolecularProfile::getCancerStudyIdentifier,
                                MolecularProfile::getStableId));

                return invokeDataFunc(sampleIds, studyIds,
                        Arrays.asList(geneSymbolIdMap.get(genomicDataFilter.getHugoGeneSymbol()).toString()),
                        studyIdToMolecularProfileIdMap, genomicDataFilter, fetchMolecularData);
            }).collect(Collectors.toList());
        } else if (dataBinCountFilter instanceof GenericAssayDataBinCountFilter) {

            GenericAssayDataBinCountFilter genomicDataBinCountFilter = (GenericAssayDataBinCountFilter) dataBinCountFilter;
            List<GenericAssayDataBinFilter> genericAssayDataBinFilters = genomicDataBinCountFilter
                    .getGenericAssayDataBinFilters();

            return genericAssayDataBinFilters.stream().flatMap(genericAssayDataBinFilter -> {

                Map<String, String> studyIdToMolecularProfileIdMap = molecularProfileMap
                        .getOrDefault(genericAssayDataBinFilter.getProfileType(), new ArrayList<MolecularProfile>())
                        .stream().collect(Collectors.toMap(MolecularProfile::getCancerStudyIdentifier,
                                MolecularProfile::getStableId));

                return invokeDataFunc(sampleIds, studyIds, Arrays.asList(genericAssayDataBinFilter.getStableId()),
                        studyIdToMolecularProfileIdMap, genericAssayDataBinFilter, fetchGenericAssayData);

            }).collect(Collectors.toList());

        }

        return new ArrayList<>();
    }

    private <S extends DataBinFilter> Stream<ClinicalData> invokeDataFunc(List<String> sampleIds, List<String> studyIds,
            List<String> stableIds, Map<String, String> studyIdToMolecularProfileIdMap, S genomicDataFilter,
            FourParameterFunction<List<String>, List<String>, List<String>, String, List<ClinicalData>> dataFunc) {

        List<String> mappedSampleIds = new ArrayList<>();
        List<String> mappedProfileIds = new ArrayList<>();

        for (int i = 0; i < sampleIds.size(); i++) {
            String studyId = studyIds.get(i);
            if (studyIdToMolecularProfileIdMap.containsKey(studyId)) {
                mappedSampleIds.add(sampleIds.get(i));
                mappedProfileIds.add(studyIdToMolecularProfileIdMap.get(studyId));
            }
        }

        if (mappedSampleIds.isEmpty()) {
            return Stream.of();
        }
        return dataFunc.apply(mappedProfileIds, mappedSampleIds, stableIds, getAttributeUniqueKey(genomicDataFilter))
                .stream();
    }

    @FunctionalInterface
    private interface FourParameterFunction<T, U, V, W, R> {
        public R apply(T t, U u, V v, W w);
    }

    FourParameterFunction<List<String>, List<String>, List<String>, String, List<ClinicalData>> fetchMolecularData = (
            mappedProfileIds, mappedSampleIds, stableIds, attributeId) -> {
        return molecularDataService.getMolecularDataInMultipleMolecularProfiles(mappedProfileIds, mappedSampleIds,
                stableIds.stream().map(Integer::parseInt).collect(Collectors.toList()), Projection.SUMMARY.name())
                .stream().map(geneMolecularData -> {
                    ClinicalData clinicalData = new ClinicalData();
                    clinicalData.setAttrId(attributeId);
                    clinicalData.setAttrValue(geneMolecularData.getValue());
                    clinicalData.setPatientId(geneMolecularData.getPatientId());
                    clinicalData.setSampleId(geneMolecularData.getSampleId());
                    clinicalData.setStudyId(geneMolecularData.getStudyId());
                    return clinicalData;
                }).collect(Collectors.toList());
    };

    FourParameterFunction<List<String>, List<String>, List<String>, String, List<ClinicalData>> fetchGenericAssayData = (
            mappedProfileIds, mappedSampleIds, stableIds, attributeId) -> {

        try {
            return genericAssayService
                    .fetchGenericAssayData(mappedProfileIds, mappedSampleIds, stableIds, Projection.SUMMARY.name())
                    .stream().map(genericAssayData -> {
                        ClinicalData clinicalData = new ClinicalData();
                        clinicalData.setAttrId(attributeId);
                        clinicalData.setAttrValue(genericAssayData.getValue());
                        clinicalData.setPatientId(genericAssayData.getPatientId());
                        clinicalData.setSampleId(genericAssayData.getSampleId());
                        clinicalData.setStudyId(genericAssayData.getStudyId());
                        return clinicalData;
                    }).collect(Collectors.toList());
        } catch (MolecularProfileNotFoundException e) {
            return new ArrayList<>();
        }
    };

    private <S extends DataBinFilter, T extends DataBinCountFilter> List<S> fetchDataBinFilters(T dataBinCountFilter) {
        if (dataBinCountFilter instanceof GenomicDataBinCountFilter) {
            return (List<S>) ((GenomicDataBinCountFilter) dataBinCountFilter).getGenomicDataBinFilters();
        } else if (dataBinCountFilter instanceof GenericAssayDataBinCountFilter) {
            return (List<S>) ((GenericAssayDataBinCountFilter) dataBinCountFilter).getGenericAssayDataBinFilters();
        }
        return new ArrayList<>();
    }

    private <S extends DataBinFilter> void removeSelfFromFilter(S dataBinFilter, StudyViewFilter studyViewFilter) {
        if (studyViewFilter != null) {
            if (dataBinFilter instanceof GenomicDataBinFilter) {
                GenomicDataBinFilter genomicDataBinFilter = (GenomicDataBinFilter) dataBinFilter;
                if (studyViewFilter.getGenomicDataFilters() != null) {
                    studyViewFilter.getGenomicDataFilters().removeIf(f -> {
                        return f.getHugoGeneSymbol().equals(genomicDataBinFilter.getHugoGeneSymbol())
                                && f.getProfileType().equals(genomicDataBinFilter.getProfileType());
                    });
                }
            } else if (dataBinFilter instanceof GenericAssayDataBinFilter) {
                GenericAssayDataBinFilter genericAssayDataBinFilter = (GenericAssayDataBinFilter) dataBinFilter;
                if (studyViewFilter.getGenericAssayDataFilters() != null) {
                    studyViewFilter.getGenericAssayDataFilters().removeIf(f -> {
                        return f.getStableId().equals(genericAssayDataBinFilter.getStableId())
                                && f.getProfileType().equals(genericAssayDataBinFilter.getProfileType());
                    });
                }
            }
        }
    }

    private <S extends DataBinFilter> String getAttributeUniqueKey(S dataBinFilter) {
        if (dataBinFilter instanceof GenomicDataBinFilter) {
            GenomicDataBinFilter genomicDataBinFilter = (GenomicDataBinFilter) dataBinFilter;
            return genomicDataBinFilter.getHugoGeneSymbol() + genomicDataBinFilter.getProfileType();
        } else if (dataBinFilter instanceof GenericAssayDataBinFilter) {
            GenericAssayDataBinFilter genericAssayDataBinFilter = (GenericAssayDataBinFilter) dataBinFilter;
            return genericAssayDataBinFilter.getStableId() + genericAssayDataBinFilter.getProfileType();
        }
        return null;
    }

    private <T extends DataBin, S extends DataBinFilter> T transform(S dataBinFilter, DataBin dataBin) {
        if (dataBinFilter instanceof GenomicDataBinFilter) {
            GenomicDataBinFilter genomicDataBinFilter = (GenomicDataBinFilter) dataBinFilter;
            return (T) dataBintoGenomicDataBin(genomicDataBinFilter, dataBin);
        } else if (dataBinFilter instanceof GenericAssayDataBinFilter) {
            GenericAssayDataBinFilter genericAssayDataBinFilter = (GenericAssayDataBinFilter) dataBinFilter;
            return (T) dataBintoGenericAssayDataBin(genericAssayDataBinFilter, dataBin);
        }
        return null;
    }

    private GenomicDataBin dataBintoGenomicDataBin(GenomicDataBinFilter genomicDataBinFilter, DataBin dataBin) {
        GenomicDataBin genomicDataBin = new GenomicDataBin();
        genomicDataBin.setCount(dataBin.getCount());
        genomicDataBin.setHugoGeneSymbol(genomicDataBinFilter.getHugoGeneSymbol());
        genomicDataBin.setProfileType(genomicDataBinFilter.getProfileType());
        if (dataBin.getSpecialValue() != null) {
            genomicDataBin.setSpecialValue(dataBin.getSpecialValue());
        }
        if (dataBin.getStart() != null) {
            genomicDataBin.setStart(dataBin.getStart());
        }
        if (dataBin.getEnd() != null) {
            genomicDataBin.setEnd(dataBin.getEnd());
        }
        return genomicDataBin;
    }

    private GenericAssayDataBin dataBintoGenericAssayDataBin(GenericAssayDataBinFilter genericAssayDataBinFilter,
                                                             DataBin dataBin) {
        GenericAssayDataBin genericAssayDataBin = new GenericAssayDataBin();
        genericAssayDataBin.setCount(dataBin.getCount());
        genericAssayDataBin.setStableId(genericAssayDataBinFilter.getStableId());
        genericAssayDataBin.setProfileType(genericAssayDataBinFilter.getProfileType());
        if (dataBin.getSpecialValue() != null) {
            genericAssayDataBin.setSpecialValue(dataBin.getSpecialValue());
        }
        if (dataBin.getStart() != null) {
            genericAssayDataBin.setStart(dataBin.getStart());
        }
        if (dataBin.getEnd() != null) {
            genericAssayDataBin.setEnd(dataBin.getEnd());
        }
        return genericAssayDataBin;
    }

    public <S extends DataFilter> List<SampleIdentifier> intervalFilterExpressionData(
        List<SampleIdentifier> sampleIdentifiers, List<MolecularProfile> molecularProfiles, List<S> dataFilters,
        Boolean negateFilters) {

        return filterExpressionData(sampleIdentifiers, molecularProfiles, dataFilters, negateFilters, clinicalDataIntervalFilterApplier);
    }

    public <S extends DataFilter> List<SampleIdentifier> equalityFilterExpressionData(
        List<SampleIdentifier> sampleIdentifiers, List<MolecularProfile> molecularProfiles, List<S> dataFilters,
        Boolean negateFilters) {

        return filterExpressionData(sampleIdentifiers, molecularProfiles, dataFilters, negateFilters, clinicalDataEqualityFilterApplier);
    }


    public <S extends DataFilter> List<SampleIdentifier> filterExpressionData(
        List<SampleIdentifier> sampleIdentifiers, List<MolecularProfile> molecularProfiles, List<S> dataFilters,
        Boolean negateFilters, ClinicalDataFilterApplier clinicalDataFilterApplier) {
        if (!CollectionUtils.isEmpty(dataFilters) && !CollectionUtils.isEmpty(sampleIdentifiers)) {
            List<ClinicalData> clinicalDatas =
                fetchDataAndTransformToClinicalDataList(sampleIdentifiers, molecularProfiles, dataFilters);
            List<ClinicalDataFilter> attributes =transformToClinicalDataFilter(dataFilters);

            MultiKeyMap clinicalDataMap = new MultiKeyMap();

            clinicalDatas.forEach(clinicalData -> {
                clinicalDataMap.put(clinicalData.getStudyId(), clinicalData.getSampleId(), clinicalData.getAttrId(),
                    clinicalData.getAttrValue());
            });

            List<SampleIdentifier> newSampleIdentifiers = new ArrayList<>();
            for (SampleIdentifier sampleIdentifier : sampleIdentifiers) {
                int count = clinicalDataFilterApplier.apply(attributes, clinicalDataMap,
                    sampleIdentifier.getSampleId(), sampleIdentifier.getStudyId(), negateFilters);

                if (count == attributes.size()) {
                    newSampleIdentifiers.add(sampleIdentifier);
                }
            }

            return newSampleIdentifiers;
        }

        return sampleIdentifiers;
    }
    
    private <S extends DataFilter> List<ClinicalData> fetchDataAndTransformToClinicalDataList(
        List<SampleIdentifier> sampleIdentifiers, List<MolecularProfile> molecularProfiles, List<S> dataFilters) {
        Map<String, List<MolecularProfile>> molecularProfileMap = molecularProfileUtil
            .categorizeMolecularProfilesByStableIdSuffixes(molecularProfiles);

        List<String> studyIds = new ArrayList<>();
        List<String> sampleIds = new ArrayList<>();
        studyViewFilterUtil.extractStudyAndSampleIds(sampleIdentifiers, studyIds, sampleIds);
        
        if (dataFilters.get(0) instanceof GenomicDataFilter) {
            List<GenomicDataFilter> genomicDataIntervalFilters = (List<GenomicDataFilter>) dataFilters;
            Set<String> hugoGeneSymbols = genomicDataIntervalFilters.stream()
                .map(GenomicDataFilter::getHugoGeneSymbol).collect(Collectors.toSet());
            Map<String, Integer> geneNameIdMap = geneService
                .fetchGenes(new ArrayList<>(hugoGeneSymbols), GeneIdType.HUGO_GENE_SYMBOL.name(),
                    Projection.SUMMARY.name())
                .stream().collect(Collectors.toMap(Gene::getHugoGeneSymbol, Gene::getEntrezGeneId));

            return genomicDataIntervalFilters.stream().flatMap(genomicDataFilter -> {

                Map<String, String> studyIdToMolecularProfileIdMap = molecularProfileMap
                    .getOrDefault(genomicDataFilter.getProfileType(), new ArrayList<>())
                    .stream().collect(Collectors.toMap(MolecularProfile::getCancerStudyIdentifier,
                        MolecularProfile::getStableId));

                GenomicDataBinFilter genomicDataBinFilter = new GenomicDataBinFilter();
                genomicDataBinFilter.setHugoGeneSymbol(genomicDataFilter.getHugoGeneSymbol());
                genomicDataBinFilter.setProfileType(genomicDataFilter.getProfileType());
                return invokeDataFunc(sampleIds, studyIds,
                    Collections.singletonList(geneNameIdMap.get(genomicDataFilter.getHugoGeneSymbol()).toString()),
                    studyIdToMolecularProfileIdMap, genomicDataBinFilter, fetchMolecularData);
            }).collect(Collectors.toList());

        } else {

            return ((List<GenericAssayDataFilter>) dataFilters).stream().flatMap(genericAssayDataFilter -> {

                Map<String, String> studyIdToMolecularProfileIdMap = molecularProfileMap
                    .getOrDefault(genericAssayDataFilter.getProfileType(), new ArrayList<MolecularProfile>())
                    .stream().collect(Collectors.toMap(MolecularProfile::getCancerStudyIdentifier,
                        MolecularProfile::getStableId));
                GenericAssayDataBinFilter genericAssayDataBinFilter = new GenericAssayDataBinFilter();
                genericAssayDataBinFilter.setStableId(genericAssayDataFilter.getStableId());
                genericAssayDataBinFilter.setProfileType(genericAssayDataFilter.getProfileType());

                // get original data stream from invokeDataFunc
                Stream<ClinicalData> dataStream = invokeDataFunc(sampleIds, studyIds, Collections.singletonList(genericAssayDataBinFilter.getStableId()),
                    studyIdToMolecularProfileIdMap, genericAssayDataBinFilter, fetchGenericAssayData);
                // For patient level generic assay profile, only keep the one sample per patient
                List<MolecularProfile> profiles = molecularProfileMap.getOrDefault(genericAssayDataFilter.getProfileType(), new ArrayList<>());
                if (profiles.size() > 0 && profiles.get(0).getPatientLevel()) {
                    dataStream = dataStream.collect(Collectors.groupingBy(ClinicalData::getPatientId)).values().stream()
                        .flatMap(d -> d.stream().limit(1));
                }
                // don't change anything for non patient level data
                return dataStream;
            }).collect(Collectors.toList());

        }
    }

    private <S extends DataFilter> List<ClinicalDataFilter> transformToClinicalDataFilter(List<S> dataFilters) {
        List<ClinicalDataFilter> attributes;
        attributes = dataFilters.stream().map(dataFilter -> {
            String attributeId;
            if (dataFilter instanceof GenomicDataFilter) {
                GenomicDataFilter genomicDataFilter = (GenomicDataFilter) dataFilter;
                attributeId = studyViewFilterUtil.getGenomicDataFilterUniqueKey(genomicDataFilter.getHugoGeneSymbol(), genomicDataFilter.getProfileType());
            } else {
                GenericAssayDataFilter genericAssayDataFilter = (GenericAssayDataFilter) dataFilter;
                attributeId = studyViewFilterUtil.getGenericAssayDataFilterUniqueKey(
                    genericAssayDataFilter.getStableId(), genericAssayDataFilter.getProfileType());
            }

            ClinicalDataFilter clinicalDataFilter = new ClinicalDataFilter();
            clinicalDataFilter.setAttributeId(attributeId);
            clinicalDataFilter.setValues(dataFilter.getValues());
            return clinicalDataFilter;
        }).collect(Collectors.toList());
        return attributes;
    }
}
