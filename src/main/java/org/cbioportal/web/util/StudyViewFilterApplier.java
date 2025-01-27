package org.cbioportal.web.util;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.cbioportal.model.Binnable;
import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.DataBin;
import org.cbioportal.model.DiscreteCopyNumberData;
import org.cbioportal.model.Gene;
import org.cbioportal.model.GeneFilter;
import org.cbioportal.model.GeneFilterQuery;
import org.cbioportal.model.GenePanel;
import org.cbioportal.model.GenePanelData;
import org.cbioportal.model.GenePanelToGene;
import org.cbioportal.model.GenericAssayDataBin;
import org.cbioportal.model.GenomicDataBin;
import org.cbioportal.model.MolecularData;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.MolecularProfile.MolecularAlterationType;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.model.Mutation;
import org.cbioportal.model.MutationFilterOption;
import org.cbioportal.model.Sample;
import org.cbioportal.model.SampleList;
import org.cbioportal.model.UniqueKeyBase;
import org.cbioportal.service.ClinicalAttributeService;
import org.cbioportal.service.DiscreteCopyNumberService;
import org.cbioportal.service.GenePanelService;
import org.cbioportal.service.GeneService;
import org.cbioportal.service.GenericAssayService;
import org.cbioportal.service.MolecularDataService;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.MutationService;
import org.cbioportal.service.SampleListService;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.StructuralVariantService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.service.util.MolecularProfileUtil;
import org.cbioportal.web.parameter.ClinicalDataFilter;
import org.cbioportal.web.parameter.ClinicalDataType;
import org.cbioportal.web.parameter.DataBinCountFilter;
import org.cbioportal.web.parameter.DataBinFilter;
import org.cbioportal.web.parameter.DataBinMethod;
import org.cbioportal.web.parameter.DataFilter;
import org.cbioportal.web.parameter.DataFilterValue;
import org.cbioportal.web.parameter.DiscreteCopyNumberEventType;
import org.cbioportal.web.parameter.GeneIdType;
import org.cbioportal.web.parameter.GenericAssayDataBinCountFilter;
import org.cbioportal.web.parameter.GenericAssayDataBinFilter;
import org.cbioportal.web.parameter.GenericAssayDataFilter;
import org.cbioportal.web.parameter.GenomicDataBinCountFilter;
import org.cbioportal.web.parameter.GenomicDataBinFilter;
import org.cbioportal.web.parameter.GenomicDataFilter;
import org.cbioportal.web.parameter.MutationDataFilter;
import org.cbioportal.web.parameter.MutationOption;
import org.cbioportal.web.parameter.Projection;
import org.cbioportal.web.parameter.SampleIdentifier;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.cbioportal.web.util.appliers.StudyViewSubFilterApplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class StudyViewFilterApplier {
    @Autowired
    private ApplicationContext applicationContext;

    private StudyViewFilterApplier instance;

    // This gets initialized and overwritten. We do this because Spring's unit tests
    // don't know how to autowire this, even though production Spring does. If we 
    // don't give this an initial value, we get NPEs.
    @Autowired
    private final List<StudyViewSubFilterApplier> subFilterAppliers = new ArrayList<>();

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


    private StudyViewFilterApplier getInstance() {
        if (Objects.isNull(instance)) {
            instance = applicationContext.getBean(StudyViewFilterApplier.class);
        }
        return instance;
    }

    Function<Sample, SampleIdentifier> sampleToSampleIdentifier = new Function<Sample, SampleIdentifier>() {

        public SampleIdentifier apply(Sample sample) {
            return studyViewFilterUtil.buildSampleIdentifier(sample.getCancerStudyIdentifier(), sample.getStableId());
        }
    };

    public List<SampleIdentifier> apply(StudyViewFilter studyViewFilter) {
        return this.getInstance().cachedApply(studyViewFilter);
    }

    @Cacheable(
        cacheResolver = "generalRepositoryCacheResolver",
        condition = "@cacheEnabledConfig.getEnabled()"
    )
    public List<SampleIdentifier> cachedApply(StudyViewFilter studyViewFilter) {
        return this.apply(studyViewFilter, false);
    }

    public List<SampleIdentifier> apply(StudyViewFilter studyViewFilter, boolean negateFilters) {

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
            || !CollectionUtils.isEmpty(studyViewFilter.getMutationDataFilters())
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
            List<GeneFilter> mutatedGeneFilters = new ArrayList<GeneFilter>();
            List<GeneFilter> structuralVariantGeneFilters = new ArrayList<GeneFilter>();
            List<GeneFilter> cnaGeneFilters = new ArrayList<GeneFilter>();

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

            // gene panel data is { profileId, sampleId, isProfiled   }
            // it tells us whether a particular sample is profiled by a given molecularprofile
            // we can use the gene panel to find out what genes were profiled
            for (List<String> profileValues : studyViewFilter.getGenomicProfiles()) {

                // using the profileIds from the filter we get a map, stableId to profile entity
                Map<String, MolecularProfile> profileMap = profileValues.stream().flatMap(
                        profileValue -> molecularProfileSet.getOrDefault(profileValue, new ArrayList<>()).stream())
                    .collect(Collectors.toMap(MolecularProfile::getStableId, Function.identity()));
                
                Set<SampleIdentifier> filteredSampleIdentifiers = new HashSet<>();
                
                // for each sample/profile combo, we need to find out whether
                // the profileMap contains that profile id
                genePanelData.forEach(datum -> {
                    if (datum.getProfiled() && profileMap.containsKey(datum.getMolecularProfileId())) {
                        SampleIdentifier sampleIdentifier =
                            studyViewFilterUtil.buildSampleIdentifier(datum.getStudyId(), datum.getSampleId());
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
                            return sampleList.getSampleIds().stream().map(sampleId ->
                                studyViewFilterUtil.buildSampleIdentifier(
                                    sampleList.getCancerStudyIdentifier(),
                                    sampleId));
                        }))
                    .toList();

                sampleIdentifiers.retainAll(filteredSampleIdentifiers);
            }
        }

        List<MutationDataFilter> mutationOptionDataFilters = new ArrayList<>();
        List<MutationDataFilter> mutationTypeDataFilters = new ArrayList<>();

        List<MutationDataFilter> mutationDataFilters = studyViewFilter.getMutationDataFilters();

        if (!CollectionUtils.isEmpty(mutationDataFilters)) {
            mutationDataFilters.forEach(mutationDataFilter -> {
                if (mutationDataFilter.getCategorization() == MutationOption.MUTATED) {
                    mutationOptionDataFilters.add(mutationDataFilter);
                } else {
                    mutationTypeDataFilters.add(mutationDataFilter);
                }
            });
        }

        if (!CollectionUtils.isEmpty(mutationOptionDataFilters)) {
            sampleIdentifiers = filterMutationData(sampleIdentifiers, molecularProfiles,
                mutationOptionDataFilters, negateFilters, clinicalDataEqualityFilterApplier);
        }

        if (!CollectionUtils.isEmpty(mutationTypeDataFilters)) {
            sampleIdentifiers = filterMutationData(sampleIdentifiers, molecularProfiles,
                mutationTypeDataFilters, negateFilters, clinicalDataEqualityFilterApplier);
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
                                                              boolean negateFilters) {
        return clinicalDataIntervalFilterApplier.apply(sampleIdentifiers, clinicalDataIntervalFilters, negateFilters);
    }

    private List<SampleIdentifier> equalityFilterClinicalData(List<SampleIdentifier> sampleIdentifiers,
                                                              List<ClinicalDataFilter> clinicalDataEqualityFilters,
                                                              boolean negateFilters) {
        return clinicalDataEqualityFilterApplier.apply(sampleIdentifiers, clinicalDataEqualityFilters, negateFilters);
    }

    private List<SampleIdentifier> filterMutatedGenes(List<GeneFilter> mutatedGenefilters,
                                                      Map<String, MolecularProfile> molecularProfileMap, List<SampleIdentifier> sampleIdentifiers) {

        if (sampleIdentifiers == null || sampleIdentifiers.isEmpty()) {
            return new ArrayList<>();
        }

        for (GeneFilter genefilter : mutatedGenefilters) {

            List<MolecularProfile> filteredMolecularProfiles = genefilter
                .getMolecularProfileIds()
                .stream()
                .map(molecularProfileMap::get)
                .toList();

            Map<String, List<MolecularProfile>> mapByStudyId = filteredMolecularProfiles
                .stream()
                .collect(Collectors.groupingBy(MolecularProfile::getCancerStudyIdentifier));

            for (List<GeneFilterQuery> geneQueries : genefilter.getGeneQueries()) {
                List<String> studyIds = new ArrayList<>();
                List<String> sampleIds = new ArrayList<>();

                List<String> hugoGeneSymbols = geneQueries
                    .stream()
                    .map(GeneFilterQuery::getHugoGeneSymbol)
                    .toList();

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
                    .map(m -> studyViewFilterUtil.buildSampleIdentifier(m.getStudyId(), m.getSampleId()))
                    .distinct()
                    .collect(Collectors.toList());
            }

        }
        return sampleIdentifiers;
    }

    private List<SampleIdentifier> filterStructuralVariantGenes(List<GeneFilter> svGenefilters,
                                                                Map<String, MolecularProfile> molecularProfileMap, List<SampleIdentifier> sampleIdentifiers) {

        if (sampleIdentifiers == null || sampleIdentifiers.isEmpty()) {
            return new ArrayList<>();
        }

        for (GeneFilter genefilter : svGenefilters) {

            List<MolecularProfile> filteredMolecularProfiles = genefilter
                .getMolecularProfileIds()
                .stream()
                .map(molecularProfileMap::get)
                .toList();

            Map<String, List<MolecularProfile>> mapByStudyId = filteredMolecularProfiles
                .stream()
                .collect(Collectors.groupingBy(MolecularProfile::getCancerStudyIdentifier));

            for (List<GeneFilterQuery> geneQueries : genefilter.getGeneQueries()) {
                List<String> studyIds = new ArrayList<>();
                List<String> sampleIds = new ArrayList<>();

                List<String> hugoGeneSymbols = geneQueries
                    .stream()
                    .map(GeneFilterQuery::getHugoGeneSymbol)
                    .toList();

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
                    .map(m -> studyViewFilterUtil.buildSampleIdentifier(m.getStudyId(), m.getSampleId()))
                    .distinct()
                    .collect(Collectors.toList());
            }

        }
        return sampleIdentifiers;
    }

    private List<SampleIdentifier> filterCNAGenes(List<GeneFilter> cnaGeneFilters,
                                                  Map<String, MolecularProfile> molecularProfileMap, List<SampleIdentifier> sampleIdentifiers) {

        if (sampleIdentifiers == null || sampleIdentifiers.isEmpty()) {
            return new ArrayList<>();
        }

        for (GeneFilter geneFilter : cnaGeneFilters) {

            List<MolecularProfile> filteredMolecularProfiles = geneFilter.getMolecularProfileIds().stream()
                .map(molecularProfileMap::get)
                .toList();

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
                            .filter(geneQuery -> geneQuery.getAlterations().stream().anyMatch(alteration -> alteration.getCode() == alterationType))
                            .collect(Collectors.toList());

                        List<String> hugoGeneSymbols = filteredGeneQueries.stream()
                            .map(GeneFilterQuery::getHugoGeneSymbol).toList();

                        Map<String, Integer> symbolToEntrezGeneId = geneService
                            .fetchGenes(new ArrayList<>(hugoGeneSymbols),
                                GeneIdType.HUGO_GENE_SYMBOL.name(), Projection.SUMMARY.name())
                            .stream().collect(Collectors.toMap(Gene::getHugoGeneSymbol, Gene::getEntrezGeneId));

                        filteredGeneQueries.removeIf(
                            q -> !symbolToEntrezGeneId.containsKey(q.getHugoGeneSymbol())
                        );

                        filteredGeneQueries.forEach(
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
                    }).toList();

                sampleIdentifiers = resultList.stream().map(
                    d -> studyViewFilterUtil.buildSampleIdentifier(d.getStudyId(), d.getSampleId())
                ).distinct().collect(Collectors.toList());
            }
        }

        return sampleIdentifiers;
    }

    private List<SampleIdentifier> filterMutationData(List<SampleIdentifier> sampleIdentifiers,
                                                      List<MolecularProfile> molecularProfiles, List<MutationDataFilter> mutationDataFilters,
                                                      boolean negateFilters, ClinicalDataFilterApplier clinicalDataFilterApplier) {
        if (CollectionUtils.isNotEmpty(mutationDataFilters) && CollectionUtils.isNotEmpty(sampleIdentifiers)) {
            List<ClinicalData> clinicalDatas =
                fetchMutationDataAndTransformToClinicalDataList(sampleIdentifiers, molecularProfiles, mutationDataFilters);


            MultiKeyMap<String, ?> clinicalDataMap;
            if (clinicalDataFilterApplier instanceof ClinicalDataEqualityFilterApplier) {
                clinicalDataMap = ClinicalDataEqualityFilterApplier.buildClinicalDataMap(clinicalDatas);
            } else {
                clinicalDataMap = ClinicalDataIntervalFilterApplier.buildClinicalDataMap(clinicalDatas);
            }

            List<SampleIdentifier> newSampleIdentifiers = new ArrayList<>();

            // loop through each mutationDataFilter and filter data
            for (MutationDataFilter mutationDataFilter : mutationDataFilters) {
                // loop through each list of DataFilterValue and do union or intersection selections
                for (List<DataFilterValue> values : mutationDataFilter.getValues()) {
                    ClinicalDataFilter clinicalDataFilter = new ClinicalDataFilter();
                    clinicalDataFilter.setAttributeId(mutationDataFilter.getHugoGeneSymbol() + mutationDataFilter.getProfileType());
                    clinicalDataFilter.setValues(values);

                    List<ClinicalDataFilter> attributes = Collections.singletonList(clinicalDataFilter);

                    // union selection: filter all samples that have at least one value from a list of DataFilterValue, e.g. Missense_Mutation, In_Shift_Del, ...
                    List<SampleIdentifier> filteredSampleIdentifiers = filterSampleIdentifiers(
                        sampleIdentifiers, attributes, clinicalDataMap, clinicalDataFilterApplier, negateFilters
                    );

                    if (newSampleIdentifiers.isEmpty()) {
                        newSampleIdentifiers = filteredSampleIdentifiers;
                    } else {
                        // intersection selection: retain shared samples from each selection for all mutationDataFilter
                        newSampleIdentifiers.retainAll(filteredSampleIdentifiers);
                    }
                }
            }

            return newSampleIdentifiers.stream().distinct().toList();
        }

        return sampleIdentifiers;
    }

    private void splitGeneFiltersByMolecularAlterationType(List<GeneFilter> genefilters,
                                                           Map<String, MolecularProfile> molecularProfileMap, List<GeneFilter> mutatedGeneFilters,
                                                           List<GeneFilter> structuralVariantGeneFilters, List<GeneFilter> cnaGeneFilters) {

        for (GeneFilter genefilter : genefilters) {

            List<MolecularProfile> filteredMolecularProfiles = genefilter.getMolecularProfileIds().stream()
                // need this filter criteria since profile id might be present
                // in filter but the study might already been filtered out
                .filter(molecularProfileMap::containsKey)
                .map(molecularProfileMap::get)
                .toList();

            Set<MolecularAlterationType> alterationTypes = filteredMolecularProfiles.stream()
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
                MolecularAlterationType alterationType = alterationTypes.iterator().next();

                if (alterationType.equals(MolecularAlterationType.STRUCTURAL_VARIANT)) {
                    structuralVariantGeneFilters.add(genefilter);
                } else if (alterationType == MolecularAlterationType.MUTATION_EXTENDED) {
                    mutatedGeneFilters.add(genefilter);
                } else if (alterationType == MolecularAlterationType.COPY_NUMBER_ALTERATION
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
                String attributeId = studyViewFilterUtil.getDataBinFilterUniqueKey(dataBinFilter);
                return dataBinner
                    .calculateClinicalDataBins(dataBinFilter, ClinicalDataType.SAMPLE,
                        filteredClinicalDataByAttributeId.getOrDefault(attributeId, Collections.emptyList()),
                        unfilteredDataByAttributeId.getOrDefault(attributeId, Collections.emptyList()),
                        filteredUniqueSampleKeys, unFilteredUniqueSampleKeys)
                    .stream().map(dataBin -> (U) transform(dataBinFilter, dataBin));

            }).collect(Collectors.toList());

        } else { // dataBinMethod == DataBinMethod.DYNAMIC
            resultDataBins = dataBinFilters.stream().flatMap(dataBinFilter ->
                dataBinner
                    .calculateDataBins(dataBinFilter, ClinicalDataType.SAMPLE,
                        filteredClinicalDataByAttributeId.getOrDefault(studyViewFilterUtil.getDataBinFilterUniqueKey(dataBinFilter),
                            Collections.emptyList()),
                        filteredUniqueSampleKeys)
                    .stream().map(dataBin -> (U) transform(dataBinFilter, dataBin))
            ).collect(Collectors.toList());
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
            Projection.SUMMARY.name());

        Map<String, List<MolecularProfile>> molecularProfileMap = molecularProfileUtil
            .categorizeMolecularProfilesByStableIdSuffixes(molecularProfiles);

        if (dataBinCountFilter instanceof GenomicDataBinCountFilter genomicDataBinCountFilter) {
            List<GenomicDataBinFilter> genomicDataBinFilters = genomicDataBinCountFilter.getGenomicDataBinFilters();

            Set<String> hugoGeneSymbols = genomicDataBinFilters.stream().map(GenomicDataBinFilter::getHugoGeneSymbol)
                .collect(Collectors.toSet());

            Map<String, Integer> geneSymbolIdMap = geneService
                .fetchGenes(new ArrayList<>(hugoGeneSymbols), GeneIdType.HUGO_GENE_SYMBOL.name(),
                    Projection.SUMMARY.name())
                .stream().collect(Collectors.toMap(Gene::getHugoGeneSymbol, Gene::getEntrezGeneId));

            return genomicDataBinFilters.stream().flatMap(genomicDataBinFilter -> {

                Map<String, String> studyIdToMolecularProfileIdMap = molecularProfileMap
                    .getOrDefault(genomicDataBinFilter.getProfileType(), new ArrayList<>()).stream()
                    .collect(Collectors.toMap(MolecularProfile::getCancerStudyIdentifier,
                        MolecularProfile::getStableId));

                return invokeDataFunc(sampleIds, studyIds,
                    List.of(geneSymbolIdMap.get(genomicDataBinFilter.getHugoGeneSymbol()).toString()),
                    studyIdToMolecularProfileIdMap, studyViewFilterUtil.getDataBinFilterUniqueKey(genomicDataBinFilter),
                    fetchMolecularData);
            }).collect(Collectors.toList());
        } else if (dataBinCountFilter instanceof GenericAssayDataBinCountFilter genomicDataBinCountFilter) {

            List<GenericAssayDataBinFilter> genericAssayDataBinFilters = genomicDataBinCountFilter
                .getGenericAssayDataBinFilters();

            return genericAssayDataBinFilters.stream().flatMap(genericAssayDataBinFilter -> {

                Map<String, String> studyIdToMolecularProfileIdMap = molecularProfileMap
                    .getOrDefault(genericAssayDataBinFilter.getProfileType(), new ArrayList<>())
                    .stream().collect(Collectors.toMap(MolecularProfile::getCancerStudyIdentifier,
                        MolecularProfile::getStableId));

                return invokeDataFunc(sampleIds, studyIds, Collections.singletonList(genericAssayDataBinFilter.getStableId()),
                    studyIdToMolecularProfileIdMap, studyViewFilterUtil.getDataBinFilterUniqueKey(genericAssayDataBinFilter),
                    fetchGenericAssayData);

            }).collect(Collectors.toList());

        }

        return new ArrayList<>();
    }

    private Stream<ClinicalData> invokeDataFunc(List<String> sampleIds, List<String> studyIds,
                                                List<String> stableIds, Map<String, String> studyIdToMolecularProfileIdMap, String attributeId,
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
        return dataFunc.apply(mappedProfileIds, mappedSampleIds, stableIds, attributeId)
            .stream();
    }

    @FunctionalInterface
    private interface FourParameterFunction<T, U, V, W, R> {
        R apply(T t, U u, V v, W w);
    }

    FourParameterFunction<List<String>, List<String>, List<String>, String, List<ClinicalData>> fetchMolecularData = (
        mappedProfileIds, mappedSampleIds, stableIds, attributeId) ->
        molecularDataService.getMolecularDataInMultipleMolecularProfiles(mappedProfileIds, mappedSampleIds,
                stableIds.stream().map(Integer::parseInt).toList(), Projection.SUMMARY.name())
            .stream().map(geneMolecularData ->
                transformDataToClinicalData(geneMolecularData, attributeId, geneMolecularData.getValue()))
            .collect(Collectors.toList());

    FourParameterFunction<List<String>, List<String>, List<String>, String, List<ClinicalData>> fetchGenericAssayData = (
        mappedProfileIds, mappedSampleIds, stableIds, attributeId) -> {

        try {
            return genericAssayService
                .fetchGenericAssayData(mappedProfileIds, mappedSampleIds, stableIds, Projection.SUMMARY.name())
                .stream().map(genericAssayData ->
                    transformDataToClinicalData(genericAssayData, attributeId, genericAssayData.getValue())
                ).collect(Collectors.toList());
        } catch (MolecularProfileNotFoundException e) {
            return new ArrayList<>();
        }
    };

    FourParameterFunction<List<String>, List<String>, List<String>, String, List<ClinicalData>> fetchMutationData = (
        mappedProfileIds, mappedSampleIds, stableIds, attributeId) ->
        mutationService.getMutationsInMultipleMolecularProfiles(mappedProfileIds, mappedSampleIds,
                stableIds.stream().map(Integer::parseInt).toList(), Projection.DETAILED.name(),
                null, null, null, null)
            .stream().map(mutationData -> transformDataToClinicalData(mutationData, attributeId, mutationData.getMutationType().toUpperCase())
            ).collect(Collectors.toList());

    FourParameterFunction<List<String>, List<String>, List<String>, String, List<ClinicalData>> fetchMutatedData = (
        mappedProfileIds, mappedSampleIds, stableIds, attributeId) ->
        mutationService.getMutationsInMultipleMolecularProfiles(mappedProfileIds, mappedSampleIds,
                stableIds.stream().map(Integer::parseInt).toList(), Projection.DETAILED.name(),
                null, null, null, null)
            .stream().map(mutationData -> transformDataToClinicalData(mutationData, attributeId, MutationFilterOption.MUTATED.name())
            ).collect(Collectors.toList());

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
            if (dataBinFilter instanceof GenomicDataBinFilter genomicDataBinFilter &&
                studyViewFilter.getGenomicDataFilters() != null) {
                studyViewFilter.getGenomicDataFilters().removeIf(f ->
                    f.getHugoGeneSymbol().equals(genomicDataBinFilter.getHugoGeneSymbol())
                        && f.getProfileType().equals(genomicDataBinFilter.getProfileType())
                );
            } else if (dataBinFilter instanceof GenericAssayDataBinFilter genericAssayDataBinFilter &&
                studyViewFilter.getGenericAssayDataFilters() != null) {
                studyViewFilter.getGenericAssayDataFilters().removeIf(f ->
                    f.getStableId().equals(genericAssayDataBinFilter.getStableId())
                        && f.getProfileType().equals(genericAssayDataBinFilter.getProfileType())
                );
            }
        }
    }

    private <T extends DataBin, S extends DataBinFilter> T transform(S dataBinFilter, DataBin dataBin) {
        if (dataBinFilter instanceof GenomicDataBinFilter genomicDataBinFilter) {
            return (T) dataBintoGenomicDataBin(genomicDataBinFilter, dataBin);
        } else if (dataBinFilter instanceof GenericAssayDataBinFilter genericAssayDataBinFilter) {
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
        boolean negateFilters) {

        return filterExpressionData(sampleIdentifiers, molecularProfiles, dataFilters, negateFilters, clinicalDataIntervalFilterApplier);
    }

    public <S extends DataFilter> List<SampleIdentifier> equalityFilterExpressionData(
        List<SampleIdentifier> sampleIdentifiers, List<MolecularProfile> molecularProfiles, List<S> dataFilters,
        boolean negateFilters) {

        return filterExpressionData(sampleIdentifiers, molecularProfiles, dataFilters, negateFilters, clinicalDataEqualityFilterApplier);
    }


    public <S extends DataFilter> List<SampleIdentifier> filterExpressionData(
        List<SampleIdentifier> sampleIdentifiers, List<MolecularProfile> molecularProfiles, List<S> dataFilters,
        boolean negateFilters, ClinicalDataFilterApplier clinicalDataFilterApplier) {
        if (!CollectionUtils.isEmpty(dataFilters) && !CollectionUtils.isEmpty(sampleIdentifiers)) {
            List<ClinicalData> clinicalDatas =
                fetchDataAndTransformToClinicalDataList(sampleIdentifiers, molecularProfiles, dataFilters);
            List<ClinicalDataFilter> attributes = transformToClinicalDataFilter(dataFilters);

            MultiKeyMap<String, ?> clinicalDataMap;
            if (clinicalDataFilterApplier instanceof ClinicalDataEqualityFilterApplier) {
                clinicalDataMap = ClinicalDataEqualityFilterApplier.buildClinicalDataMap(clinicalDatas);
            } else {
                clinicalDataMap = ClinicalDataIntervalFilterApplier.buildClinicalDataMap(clinicalDatas);
            }

            return filterSampleIdentifiers(sampleIdentifiers, attributes, clinicalDataMap, clinicalDataFilterApplier, negateFilters).stream().distinct().toList();
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

        if (dataFilters.getFirst() instanceof GenomicDataFilter) {
            List<GenomicDataFilter> genomicDataIntervalFilters = (List<GenomicDataFilter>) dataFilters;
            Set<String> hugoGeneSymbols = genomicDataIntervalFilters.stream()
                .map(GenomicDataFilter::getHugoGeneSymbol).collect(Collectors.toSet());
            Map<String, Integer> geneNameIdMap = geneService
                .fetchGenes(new ArrayList<>(hugoGeneSymbols), GeneIdType.HUGO_GENE_SYMBOL.name(),
                    Projection.SUMMARY.name())
                .stream().collect(Collectors.toMap(Gene::getHugoGeneSymbol, Gene::getEntrezGeneId));

            return genomicDataIntervalFilters.stream().flatMap(genomicDataFilter -> {

                List<MolecularProfile> subMolecularProfiles = molecularProfileMap
                    .getOrDefault(genomicDataFilter.getProfileType(), new ArrayList<>());
                Map<String, String> studyIdToMolecularProfileIdMap = subMolecularProfiles
                    .stream().collect(Collectors.toMap(MolecularProfile::getCancerStudyIdentifier,
                        MolecularProfile::getStableId));

                return invokeDataFunc(sampleIds, studyIds,
                    Collections.singletonList(geneNameIdMap.get(genomicDataFilter.getHugoGeneSymbol()).toString()),
                    studyIdToMolecularProfileIdMap, studyViewFilterUtil.getDataFilterUniqueKey(genomicDataFilter),
                    fetchMolecularData);
            }).collect(Collectors.toList());

        } else {
            return ((List<GenericAssayDataFilter>) dataFilters).stream().flatMap(genericAssayDataFilter -> {

                Map<String, String> studyIdToMolecularProfileIdMap = molecularProfileMap
                    .getOrDefault(genericAssayDataFilter.getProfileType(), new ArrayList<MolecularProfile>())
                    .stream().collect(Collectors.toMap(MolecularProfile::getCancerStudyIdentifier,
                        MolecularProfile::getStableId));

                // get original data stream from invokeDataFunc
                Stream<ClinicalData> dataStream = invokeDataFunc(sampleIds, studyIds, Collections.singletonList(genericAssayDataFilter.getStableId()),
                    studyIdToMolecularProfileIdMap, studyViewFilterUtil.getDataFilterUniqueKey(genericAssayDataFilter), fetchGenericAssayData);
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

    private List<ClinicalData> fetchMutationDataAndTransformToClinicalDataList(
        List<SampleIdentifier> sampleIdentifiers, List<MolecularProfile> molecularProfiles, List<MutationDataFilter> mutationDataFilters
    ) {
        Map<String, List<MolecularProfile>> molecularProfileMap = molecularProfileUtil
            .categorizeMolecularProfilesByStableIdSuffixes(molecularProfiles);

        List<String> studyIds = new ArrayList<>();
        List<String> sampleIds = new ArrayList<>();
        studyViewFilterUtil.extractStudyAndSampleIds(sampleIdentifiers, studyIds, sampleIds);

        Set<String> hugoGeneSymbols = mutationDataFilters.stream()
            .map(MutationDataFilter::getHugoGeneSymbol).collect(Collectors.toSet());
        Map<String, Integer> geneNameIdMap = geneService
            .fetchGenes(new ArrayList<>(hugoGeneSymbols), GeneIdType.HUGO_GENE_SYMBOL.name(),
                Projection.SUMMARY.name())
            .stream().collect(Collectors.toMap(Gene::getHugoGeneSymbol, Gene::getEntrezGeneId));

        return mutationDataFilters.stream().flatMap(mutationDataFilter -> {
            List<MolecularProfile> subMolecularProfiles = molecularProfileMap
                .getOrDefault(mutationDataFilter.getProfileType(), new ArrayList<>());
            Map<String, String> studyIdToMolecularProfileIdMap = subMolecularProfiles
                .stream().collect(Collectors.toMap(MolecularProfile::getCancerStudyIdentifier,
                    MolecularProfile::getStableId));

            if (mutationDataFilter.getCategorization() == MutationOption.MUTATED) {
                return fetchMutatedAndWildTypeData(
                    sampleIdentifiers, geneNameIdMap, mutationDataFilter,
                    studyIdToMolecularProfileIdMap);
            } else {
                // fetch mutation type data
                return invokeDataFunc(sampleIds, studyIds, Collections.singletonList(
                        geneNameIdMap.get(mutationDataFilter.getHugoGeneSymbol()).toString()),
                    studyIdToMolecularProfileIdMap,
                    studyViewFilterUtil.getMutationDataFilterUniqueKey(mutationDataFilter),
                    fetchMutationData);
            }
        }).collect(Collectors.toList());
    }

    private <S extends DataFilter> List<ClinicalDataFilter> transformToClinicalDataFilter(List<S> dataFilters) {
        List<ClinicalDataFilter> attributes;
        attributes = dataFilters.stream().map(dataFilter -> {
            ClinicalDataFilter clinicalDataFilter = new ClinicalDataFilter();
            clinicalDataFilter.setAttributeId(studyViewFilterUtil.getDataFilterUniqueKey(dataFilter));
            clinicalDataFilter.setValues(dataFilter.getValues());
            return clinicalDataFilter;
        }).collect(Collectors.toList());
        return attributes;
    }

    private List<SampleIdentifier> fetchProfiledMutationDataByGene(List<String> studyIds, List<String> sampleIds, Integer entrezGeneId) {
        List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers =
            molecularProfileService.getMutationProfileCaseIdentifiers(studyIds, sampleIds);

        List<GenePanelData> genePanelDataList = genePanelService
            .fetchGenePanelDataInMultipleMolecularProfiles(molecularProfileCaseIdentifiers);

        Function<GenePanelData, SampleIdentifier> sampleIdentifierBuilder = sample ->
            studyViewFilterUtil.buildSampleIdentifier(sample.getStudyId(), sample.getSampleId());

        Map<String, Set<SampleIdentifier>> casesWithDataInGenePanel = new HashMap<>();
        // loop through all membership records -- ignore any where g.getGenePanelId == null
        for (GenePanelData genePanelDataRecord : genePanelDataList) {
            String associatedGenePanel = genePanelDataRecord.getGenePanelId();
            if (associatedGenePanel != null) {
                casesWithDataInGenePanel.putIfAbsent(associatedGenePanel, new HashSet<>());
                Set<SampleIdentifier> casesForThisGenePanel = casesWithDataInGenePanel.get(associatedGenePanel);
                casesForThisGenePanel.add(sampleIdentifierBuilder.apply(genePanelDataRecord));
            }
        }

        List<GenePanel> genePanels = new ArrayList<>();
        if (!casesWithDataInGenePanel.isEmpty()) {
            genePanels = genePanelService.fetchGenePanels(new ArrayList<>(casesWithDataInGenePanel.keySet()), Projection.DETAILED.name());
        }

        List<GenePanelData> genePanelData = genePanelDataList
            .stream()
            .filter(GenePanelData::getProfiled)
            .toList();

        Set<SampleIdentifier> profiledCases = genePanelData
            .stream()
            // there can be duplicate patient or sample id, append study id
            .map(sampleIdentifierBuilder)
            .collect(Collectors.toSet());

        // here we look for cases where none of the profiles have gene panel ids
        // a case with at least one profile with gene panel id is considered as a case with gene panel data
        // so a case is considered without panel data only if none of the profiles has a gene panel id

        // first identify cases with gene panel data
        Set<SampleIdentifier> casesWithPanelData = genePanelData
            .stream()
            .filter(g -> g.getGenePanelId() != null)
            // there can be duplicate patient or sample id, append study id
            .map(sampleIdentifierBuilder)
            .collect(Collectors.toSet());

        // find all unique cases
        Set<SampleIdentifier> casesWithoutPanelData = genePanelData
            .stream()
            // there can be duplicate patient or sample id, append study id
            .map(sampleIdentifierBuilder)
            .collect(Collectors.toSet());

        // removing cases with panel data from all unique cases gives us the cases without panel data
        casesWithoutPanelData.removeAll(casesWithPanelData);

        List<String> genePanelIds = genePanels.stream().flatMap(genePanel -> {
            List<GenePanelToGene> genePanelToGenes = genePanel.getGenes();
            return genePanelToGenes
                .stream()
                .filter(genePanelToGene -> genePanelToGene.getEntrezGeneId().equals(entrezGeneId))
                .map(GenePanelToGene::getGenePanelId);
        }).toList();

        List<SampleIdentifier> newSampleIdentifiers = new ArrayList<>();
        // different calculations depending on if gene is linked to gene panels
        if (CollectionUtils.isNotEmpty(genePanelIds)) {
            // for every gene panel associated containing the gene, use the sum of unique cases
            // as well as cases without panel data
            for (String genePanelId : genePanelIds) {
                newSampleIdentifiers.addAll(casesWithDataInGenePanel.get(genePanelId));
            }
            newSampleIdentifiers.addAll(casesWithoutPanelData);
        } else {
            // we use profiledCasesCount instead of casesWithoutPanelData to
            // prevent a divide by zero error which can happen for targeted studies
            // in which certain genes have events that are not captured by the panel.
            newSampleIdentifiers.addAll(profiledCases);
        }

        return newSampleIdentifiers;
    }

    private Stream<ClinicalData> fetchMutatedAndWildTypeData(
        List<SampleIdentifier> sampleIdentifiers, Map<String, Integer> geneNameIdMap, MutationDataFilter mutationDataFilter,
        Map<String, String> studyIdToMolecularProfileIdMap) {
        List<String> studyIds = new ArrayList<>();
        List<String> sampleIds = new ArrayList<>();
        studyViewFilterUtil.extractStudyAndSampleIds(sampleIdentifiers, studyIds, sampleIds);

        // mutated
        List<ClinicalData> mutatedClinicalDatas = invokeDataFunc(sampleIds, studyIds,
            Collections.singletonList(geneNameIdMap.get(mutationDataFilter.getHugoGeneSymbol()).toString()),
            studyIdToMolecularProfileIdMap, studyViewFilterUtil.getMutationDataFilterUniqueKey(mutationDataFilter),
            fetchMutatedData).toList();

        List<SampleIdentifier> mutatedSampleIdentifiers = mutatedClinicalDatas
            .stream()
            .map(datum -> studyViewFilterUtil.buildSampleIdentifier(datum.getStudyId(), datum.getSampleId())
            ).toList();

        List<ClinicalData> clinicalDatas = new ArrayList<>(mutatedClinicalDatas);

        // not profiled
        List<SampleIdentifier> profiledSampleIdentifiers = fetchProfiledMutationDataByGene(
            studyIds, sampleIds, geneNameIdMap.get(mutationDataFilter.getHugoGeneSymbol()));

        List<SampleIdentifier> notProfiledSampleIdentifiers = sampleIdentifiers
            .stream()
            .filter(s -> profiledSampleIdentifiers.stream().noneMatch(p -> p.equals(s)))
            .toList();

        List<ClinicalData> notProfiledClinicalDatas = studyViewFilterUtil.transformSampleIdentifiersToClinicalData(
            notProfiledSampleIdentifiers,
            studyViewFilterUtil.getMutationDataFilterUniqueKey(mutationDataFilter),
            MutationFilterOption.NOT_PROFILED.name()
        );

        clinicalDatas.addAll(notProfiledClinicalDatas);

        // not mutated
        List<SampleIdentifier> notMutatedSampleIdentifiers = profiledSampleIdentifiers
            .stream()
            .filter(p -> mutatedSampleIdentifiers.stream().noneMatch(m -> m.equals(p)))
            .toList();

        List<ClinicalData> notMutatedClinicalDatas = studyViewFilterUtil.transformSampleIdentifiersToClinicalData(
            notMutatedSampleIdentifiers,
            studyViewFilterUtil.getMutationDataFilterUniqueKey(mutationDataFilter),
            MutationFilterOption.NOT_MUTATED.name()
        );

        clinicalDatas.addAll(notMutatedClinicalDatas);

        return clinicalDatas.stream();
    }

    private <S extends UniqueKeyBase> ClinicalData transformDataToClinicalData(S data, String attributeId, String attributeValue) {
        ClinicalData clinicalData = new ClinicalData();

        if (data instanceof MolecularData molecularData) {
            clinicalData.setPatientId(molecularData.getPatientId());
            clinicalData.setSampleId(molecularData.getSampleId());
            clinicalData.setStudyId(molecularData.getStudyId());
        } else if (data instanceof Mutation mutationData) {
            clinicalData.setPatientId(mutationData.getPatientId());
            clinicalData.setSampleId(mutationData.getSampleId());
            clinicalData.setStudyId(mutationData.getStudyId());
        } else {
            return clinicalData;
        }

        clinicalData.setAttrValue(attributeValue);
        clinicalData.setAttrId(attributeId);

        return clinicalData;
    }

    private List<SampleIdentifier> filterSampleIdentifiers(
        List<SampleIdentifier> sampleIdentifiers,
        List<ClinicalDataFilter> attributes,
        MultiKeyMap<String, ?> clinicalDataMap,
        ClinicalDataFilterApplier clinicalDataFilterApplier,
        boolean negateFilters
    ) {
        return sampleIdentifiers.stream()
            .filter(sampleIdentifier -> {
                int count = clinicalDataFilterApplier.apply(
                    attributes,
                    clinicalDataMap,
                    sampleIdentifier.getSampleId(),
                    sampleIdentifier.getStudyId(),
                    negateFilters
                );
                return count == attributes.size();
            })
            .collect(Collectors.toList());
    }
}
