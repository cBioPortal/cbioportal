package org.cbioportal.web.util;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.MultiKeyMap;
import org.cbioportal.model.*;
import org.cbioportal.model.MolecularProfile.MolecularAlterationType;
import org.cbioportal.service.*;
import org.cbioportal.web.parameter.*;
import org.cbioportal.web.parameter.GeneFilter.SingleGeneQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StudyViewFilterApplier {

    private SampleService sampleService;
    private MutationService mutationService;
    private DiscreteCopyNumberService discreteCopyNumberService;
    private MolecularProfileService molecularProfileService;
    private GenePanelService genePanelService;
    private ClinicalDataEqualityFilterApplier clinicalDataEqualityFilterApplier;
    private ClinicalDataIntervalFilterApplier clinicalDataIntervalFilterApplier;
    private StudyViewFilterUtil studyViewFilterUtil;
    private GeneService geneService;
    private ClinicalAttributeService clinicalAttributeService;
    private MolecularDataService molecularDataService;
    private SampleListService sampleListService;

    @Autowired
    public StudyViewFilterApplier(SampleService sampleService,
                                  MutationService mutationService,
                                  DiscreteCopyNumberService discreteCopyNumberService,
                                  MolecularProfileService molecularProfileService,
                                  GenePanelService genePanelService,
                                  ClinicalDataService clinicalDataService,
                                  ClinicalDataEqualityFilterApplier clinicalDataEqualityFilterApplier,
                                  ClinicalDataIntervalFilterApplier clinicalDataIntervalFilterApplier,
                                  StudyViewFilterUtil studyViewFilterUtil,
                                  GeneService geneService,
                                  ClinicalAttributeService clinicalAttributeService,
                                  MolecularDataService molecularDataService,
                                  SampleListService sampleListService) {
        this.sampleService = sampleService;
        this.mutationService = mutationService;
        this.discreteCopyNumberService = discreteCopyNumberService;
        this.molecularProfileService = molecularProfileService;
        this.genePanelService = genePanelService;
        this.clinicalDataEqualityFilterApplier = clinicalDataEqualityFilterApplier;
        this.clinicalDataIntervalFilterApplier = clinicalDataIntervalFilterApplier;
        this.studyViewFilterUtil = studyViewFilterUtil;
        this.geneService = geneService;
        this.clinicalAttributeService = clinicalAttributeService;
        this.molecularDataService = molecularDataService;
        this.sampleListService = sampleListService;
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

        List<MolecularProfile> molecularProfiles = null;

        if (!CollectionUtils.isEmpty(studyViewFilter.getGeneFilters())
                || !CollectionUtils.isEmpty(studyViewFilter.getGenomicDataFilters())
                || !CollectionUtils.isEmpty(studyViewFilter.getGenomicProfiles())) {

            molecularProfiles = molecularProfileService.getMolecularProfilesInStudies(studyIds, "SUMMARY");
        }

        List<GenomicDataFilter> genomicDataIntervalFilters = studyViewFilter.getGenomicDataFilters();
        if (genomicDataIntervalFilters != null) {
            sampleIdentifiers = intervalFilterGenomicData(sampleIdentifiers, molecularProfiles, genomicDataIntervalFilters, negateFilters);
        }

        if (!CollectionUtils.isEmpty(studyViewFilter.getGeneFilters())) {
            Map<String, MolecularProfile> molecularProfileMap = molecularProfiles.stream()
                    .collect(Collectors.toMap(MolecularProfile::getStableId, Function.identity()));
            List<GeneFilter> mutatedGeneFilters = new ArrayList<GeneFilter>();
            List<GeneFilter> fusionGeneFilters = new ArrayList<GeneFilter>();
            List<GeneFilter> cnaGeneFilters = new ArrayList<GeneFilter>();

            splitGeneFiltersByMolecularAlterationType(studyViewFilter.getGeneFilters(), molecularProfileMap,
                    mutatedGeneFilters, fusionGeneFilters, cnaGeneFilters);

            if ((mutatedGeneFilters.size() + fusionGeneFilters.size() + cnaGeneFilters.size()) == studyViewFilter
                    .getGeneFilters().size()) {
                if (!mutatedGeneFilters.isEmpty()) {
                    sampleIdentifiers = filterMutatedOrFusionGenes(mutatedGeneFilters, molecularProfileMap,
                            MolecularAlterationType.MUTATION_EXTENDED, sampleIdentifiers);
                }
                if (!fusionGeneFilters.isEmpty()) {
                    sampleIdentifiers = filterMutatedOrFusionGenes(fusionGeneFilters, molecularProfileMap,
                            MolecularAlterationType.FUSION, sampleIdentifiers);
                }
                if (!cnaGeneFilters.isEmpty()) {
                    sampleIdentifiers = filterCNAGenes(cnaGeneFilters, molecularProfileMap, sampleIdentifiers);
                }

            } else {
                return new ArrayList<SampleIdentifier>();
            }
        }

        if (!CollectionUtils.isEmpty(studyViewFilter.getGenomicProfiles())) {
            Map<String, List<SampleIdentifier>> groupStudySampleIdentifiers = sampleIdentifiers.stream()
                    .collect(Collectors.groupingBy(SampleIdentifier::getStudyId));

            Map<String, List<MolecularProfile>> molecularProfileSet = studyViewFilterUtil
                    .categorizeMolecularPorfiles(molecularProfiles);

            List<String> queryMolecularProfileIds = new ArrayList<>();
            List<String> querySampleIds = new ArrayList<>();

            studyViewFilter.getGenomicProfiles().stream().forEach(profileValues -> {
                profileValues.stream().forEach(profileValue -> {
                    molecularProfileSet.getOrDefault(profileValue, new ArrayList<>()).stream().forEach(profile -> {
                        groupStudySampleIdentifiers.getOrDefault(profile.getCancerStudyIdentifier(), new ArrayList<>())
                                .stream().forEach(sampleIdentifier -> {
                                    queryMolecularProfileIds.add(profile.getStableId());
                                    querySampleIds.add(sampleIdentifier.getSampleId());
                                });
                    });

                });
            });

            List<GenePanelData> genePanelData = genePanelService
                    .fetchGenePanelDataInMultipleMolecularProfiles(queryMolecularProfileIds, querySampleIds);

            studyViewFilter.getGenomicProfiles().stream().flatMap(profileValues -> profileValues.stream());

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

        return sampleIdentifiers;
    }

    private List<SampleIdentifier> intervalFilterClinicalData(List<SampleIdentifier> sampleIdentifiers,
                                                              List<ClinicalDataFilter> clinicalDataIntervalFilters,
                                                              Boolean negateFilters) {
        return clinicalDataIntervalFilterApplier.apply(sampleIdentifiers, clinicalDataIntervalFilters, negateFilters);
    }
    
    private List<SampleIdentifier> intervalFilterGenomicData(List<SampleIdentifier> sampleIdentifiers,
            List<MolecularProfile> molecularProfiles,
            List<GenomicDataFilter> genomicDataIntervalFilters, Boolean negateFilters) {

        if (!genomicDataIntervalFilters.isEmpty() && !sampleIdentifiers.isEmpty()) {

            Map<String, List<MolecularProfile>> molecularProfileMap = studyViewFilterUtil
                    .categorizeMolecularPorfiles(molecularProfiles);

            List<String> studyIds = new ArrayList<>();
            List<String> sampleIds = new ArrayList<>();
            studyViewFilterUtil.extractStudyAndSampleIds(sampleIdentifiers, studyIds, sampleIds);

            Set<String> hugoGeneSymbols = genomicDataIntervalFilters.stream()
                    .map(GenomicDataFilter::getHugoGeneSymbol).collect(Collectors.toSet());

            Map<String, Integer> geneNameIdMap = geneService
                    .fetchGenes(new ArrayList<>(hugoGeneSymbols), GeneIdType.HUGO_GENE_SYMBOL.name(),
                            Projection.SUMMARY.name())
                    .stream().collect(Collectors.toMap(Gene::getHugoGeneSymbol, Gene::getEntrezGeneId));

            List<ClinicalData> clinicalDatas = genomicDataIntervalFilters.stream()
                    .flatMap(genomicDataIntervalFilter -> {
                        
                        Map<String, String> studyIdToMolecularProfileIdMap = molecularProfileMap.getOrDefault(genomicDataIntervalFilter
                                .getProfileType(), new ArrayList<MolecularProfile>())
                                .stream()
                                .collect(Collectors.toMap(MolecularProfile::getCancerStudyIdentifier,
                                        MolecularProfile::getStableId));

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

                        String attributeId = studyViewFilterUtil.getGenomicDataFilterUniqueKey(genomicDataIntervalFilter.getHugoGeneSymbol(), genomicDataIntervalFilter.getProfileType());
                        return molecularDataService
                                .getMolecularDataInMultipleMolecularProfiles(mappedProfileIds, mappedSampleIds,
                                        Arrays.asList(geneNameIdMap.get(genomicDataIntervalFilter.getHugoGeneSymbol())),
                                        Projection.SUMMARY.name())
                                .stream().map(geneMolecularData -> {
                                    ClinicalData clinicalData = new ClinicalData();
                                    clinicalData.setAttrId(attributeId);
                                    clinicalData.setAttrValue(geneMolecularData.getValue());
                                    clinicalData.setPatientId(geneMolecularData.getPatientId());
                                    clinicalData.setSampleId(geneMolecularData.getSampleId());
                                    clinicalData.setStudyId(geneMolecularData.getStudyId());
                                    return clinicalData;
                                });

                    }).collect(Collectors.toList());

            MultiKeyMap clinicalDataMap = new MultiKeyMap();

            clinicalDatas.forEach(clinicalData -> {
                if (clinicalDataMap.containsKey(clinicalData.getSampleId(), clinicalData.getStudyId())) {
                    ((List<ClinicalData>) clinicalDataMap.get(clinicalData.getSampleId(), clinicalData.getStudyId()))
                            .add(clinicalData);
                } else {
                    List<ClinicalData> clinicalDatasTemp = new ArrayList<>();
                    clinicalDatasTemp.add(clinicalData);
                    clinicalDataMap.put(clinicalData.getSampleId(), clinicalData.getStudyId(), clinicalDatasTemp);
                }
            });

            List<ClinicalDataFilter> attributes = genomicDataIntervalFilters.stream()
                    .map(genomicDataIntervalFilter -> {
                        String attributeId = studyViewFilterUtil.getGenomicDataFilterUniqueKey(genomicDataIntervalFilter.getHugoGeneSymbol(), genomicDataIntervalFilter.getProfileType());
                        ClinicalDataFilter clinicalDataIntervalFilter = new ClinicalDataFilter();
                        clinicalDataIntervalFilter.setAttributeId(attributeId);
                        clinicalDataIntervalFilter.setValues(genomicDataIntervalFilter.getValues());
                        return clinicalDataIntervalFilter;
                    }).collect(Collectors.toList());

            List<String> ids = new ArrayList<>();
            List<String> studyIdsOfIds = new ArrayList<>();
            int index = 0;
            for (String entityId : sampleIds) {
                String studyId = studyIds.get(index);

                int count = clinicalDataIntervalFilterApplier.apply(attributes, clinicalDataMap, entityId, studyId,
                        negateFilters);

                if (count == attributes.size()) {
                    ids.add(entityId);
                    studyIdsOfIds.add(studyId);
                }
                index++;
            }

            Set<String> idsSet = new HashSet<>(ids);
            idsSet.retainAll(new HashSet<>(sampleIds));
            List<SampleIdentifier> newSampleIdentifiers = new ArrayList<>();
            for (int i = 0; i < ids.size(); i++) {
                SampleIdentifier sampleIdentifier = new SampleIdentifier();
                sampleIdentifier.setSampleId(ids.get(i));
                sampleIdentifier.setStudyId(studyIdsOfIds.get(i));
                newSampleIdentifiers.add(sampleIdentifier);
            }

            return newSampleIdentifiers;
        }

        return sampleIdentifiers;
    }

    private List<SampleIdentifier> equalityFilterClinicalData(List<SampleIdentifier> sampleIdentifiers,
                                                              List<ClinicalDataFilter> clinicalDataEqualityFilters,
                                                              Boolean negateFilters) {
        return clinicalDataEqualityFilterApplier.apply(sampleIdentifiers, clinicalDataEqualityFilters, negateFilters);
    }

    private List<SampleIdentifier> filterMutatedOrFusionGenes(List<GeneFilter> mutatedGenefilters,
            Map<String, MolecularProfile> molecularProfileMap, MolecularAlterationType molecularAlterationFilterType,
            List<SampleIdentifier> sampleIdentifiers) {

        for (GeneFilter genefilter : mutatedGenefilters) {

            List<MolecularProfile> filteredMolecularProfiles = genefilter.getMolecularProfileIds().stream()
                    .map(molecularProfileId -> molecularProfileMap.get(molecularProfileId))
                    .collect(Collectors.toList());

            Map<String, List<MolecularProfile>> mapByStudyId = filteredMolecularProfiles.stream()
                    .collect(Collectors.groupingBy(MolecularProfile::getCancerStudyIdentifier));

            for (List<SingleGeneQuery> geneQueries : genefilter.getSingleGeneQueries()) {
                List<String> studyIds = new ArrayList<>();
                List<String> sampleIds = new ArrayList<>();

                List<String> hugoGeneSymbols = geneQueries.stream().map(SingleGeneQuery::getHugoGeneSymbol)
                        .collect(Collectors.toList());

                List<Integer> entrezGeneIds = geneService
                        .fetchGenes(hugoGeneSymbols, GeneIdType.HUGO_GENE_SYMBOL.name(), Projection.SUMMARY.name())
                        .stream().map(gene -> gene.getEntrezGeneId()).collect(Collectors.toList());

                studyViewFilterUtil.extractStudyAndSampleIds(sampleIdentifiers, studyIds, sampleIds);

                List<Mutation> mutations = new ArrayList<Mutation>();
                // TODO: cleanup once https://github.com/cBioPortal/cbioportal/pull/6688 is
                // merged
                if (molecularAlterationFilterType.equals(MolecularAlterationType.FUSION)) {

                    mutations = mutationService.getFusionsInMultipleMolecularProfiles(
                            molecularProfileService.getFirstMutationProfileIds(studyIds, sampleIds), sampleIds,
                            entrezGeneIds, Projection.ID.name(), null, null, null, null);
                } else {

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

                    mutations = mutationService.getMutationsInMultipleMolecularProfiles(molecularProfileIds, sampleIds,
                            entrezGeneIds, Projection.ID.name(), null, null, null, null);
                }

                sampleIdentifiers = mutations.stream().map(m -> {
                    SampleIdentifier sampleIdentifier = new SampleIdentifier();
                    sampleIdentifier.setSampleId(m.getSampleId());
                    sampleIdentifier.setStudyId(m.getStudyId());
                    return sampleIdentifier;
                }).distinct().collect(Collectors.toList());
            }

        }
        return sampleIdentifiers;
    }

    private List<SampleIdentifier> filterCNAGenes(List<GeneFilter> cnaGeneFilters,
            Map<String, MolecularProfile> molecularProfileMap, List<SampleIdentifier> sampleIdentifiers) {

        for (GeneFilter geneFilter : cnaGeneFilters) {

            List<MolecularProfile> filteredMolecularProfiles = geneFilter.getMolecularProfileIds().stream()
                    .map(molecularProfileId -> molecularProfileMap.get(molecularProfileId))
                    .collect(Collectors.toList());

            for (List<SingleGeneQuery> geneQueries : geneFilter.getSingleGeneQueries()) {

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

                List<DiscreteCopyNumberData> resultList = DiscreteCopyNumberEventType.HOMDEL_AND_AMP
                        .getAlterationTypes().stream().flatMap(alterationType -> {

                            List<SingleGeneQuery> filteredGeneQueries = geneQueries.stream()
                                    .filter(geneQuery -> geneQuery.getAlterations().stream()
                                            .filter(alteration -> alteration.getCode() == alterationType)
                                            .count() > 0)
                                    .collect(Collectors.toList());

                            List<String> hugoGeneSymbols = filteredGeneQueries.stream()
                                    .map(SingleGeneQuery::getHugoGeneSymbol).collect(Collectors.toList());

                            List<Integer> entrezGeneIds = geneService
                                    .fetchGenes(new ArrayList<>(hugoGeneSymbols),
                                            GeneIdType.HUGO_GENE_SYMBOL.name(), Projection.SUMMARY.name())
                                    .stream().map(gene -> gene.getEntrezGeneId()).collect(Collectors.toList());

                            List<DiscreteCopyNumberData> copyNumberDatas = new ArrayList<>();
                            if (!entrezGeneIds.isEmpty()) {
                                copyNumberDatas = discreteCopyNumberService
                                        .getDiscreteCopyNumbersInMultipleMolecularProfiles(molecularProfileIds,
                                                sampleIds, entrezGeneIds, Arrays.asList(alterationType),
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

    private void splitGeneFiltersByMolecularAlterationType(List<GeneFilter> genefilters,
            Map<String, MolecularProfile> molecularProfileMap, List<GeneFilter> mutatedGeneFilters,
            List<GeneFilter> fusionGeneFilters, List<GeneFilter> cnaGeneFilters) {

        for (GeneFilter genefilter : genefilters) {

            List<MolecularProfile> filteredMolecularProfiles = genefilter.getMolecularProfileIds().stream()
                    .map(molecularProfileId -> molecularProfileMap.get(molecularProfileId))
                    .collect(Collectors.toList());

            Set<MolecularAlterationType> alterationTypes = filteredMolecularProfiles.stream()
                    .map(MolecularProfile::getMolecularAlterationType).collect(Collectors.toSet());

            Set<String> dataTypes = filteredMolecularProfiles.stream().map(MolecularProfile::getDatatype)
                    .collect(Collectors.toSet());

            if (alterationTypes.size() == 1 && dataTypes.size() == 1) {
                MolecularAlterationType alterationType = alterationTypes.iterator().next();
                String dataType = dataTypes.iterator().next();
                if (alterationType == MolecularAlterationType.MUTATION_EXTENDED) {
                    mutatedGeneFilters.add(genefilter);
                } else if (alterationType.equals(MolecularAlterationType.STRUCTURAL_VARIANT) && dataType.equals("FUSION")) {
                    // TODO: cleanup once fusion/structural data is fixed in database
                    // until then rename fusion with mutation profile
                    Set<String> molecularProfileIds = filteredMolecularProfiles
                            .stream()
                            .map(molecularProfile -> molecularProfile.getCancerStudyIdentifier() + "_mutations")
                            .collect(Collectors.toSet());

                    GeneFilter filter = new GeneFilter();
                    filter.setGeneQueries(genefilter.getGeneQueries());
                    filter.setMolecularProfileIds(molecularProfileIds);
                    fusionGeneFilters.add(filter);
                } else if (alterationType == MolecularAlterationType.COPY_NUMBER_ALTERATION
                        && dataType.equals("DISCRETE")) {
                    cnaGeneFilters.add(genefilter);
                }
            }
        }
    }
}
