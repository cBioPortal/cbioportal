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
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.web.parameter.*;
import org.cbioportal.web.parameter.GeneFilter.SingleGeneQuery;
import org.cbioportal.web.util.appliers.PatientTreatmentFilterApplier;
import org.cbioportal.web.util.appliers.SampleTreatmentFilterApplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StudyViewFilterApplier {

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
    private PatientTreatmentFilterApplier patientTreatmentFilterApplier;
    @Autowired
    private CustomDataFilterApplier customDataFilterApplier;
    @Autowired
    private SampleTreatmentFilterApplier sampleTreatmentFilterApplier;

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

        sampleIdentifiers = intervalFilterExpressionData(sampleIdentifiers, molecularProfiles,
                studyViewFilter.getGenomicDataFilters(), negateFilters);

        sampleIdentifiers = intervalFilterExpressionData(sampleIdentifiers, molecularProfiles,
                studyViewFilter.getGenericAssayDataFilters(), negateFilters);

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
        
        if (
            studyViewFilter.getSampleTreatmentFilters() != null && 
            !studyViewFilter.getSampleTreatmentFilters().getFilters().isEmpty()
        ) {
            sampleIdentifiers = sampleTreatmentFilterApplier.filter(
                studyViewFilter.getSampleTreatmentFilters(),
                sampleIdentifiers
            );
        }

        if (
            studyViewFilter.getPatientTreatmentFilters() != null && 
            !studyViewFilter.getPatientTreatmentFilters().getFilters().isEmpty()
        ) {
            sampleIdentifiers = patientTreatmentFilterApplier.filter(
                sampleIdentifiers,
                studyViewFilter.getPatientTreatmentFilters()
            );
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

                List<DiscreteCopyNumberData> resultList = DiscreteCopyNumberEventType.ALL
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
                    // need this filter criteria since profile id might be present
                    // in filter but the study might already been filtered out
                    .filter(molecularProfileMap::containsKey)
                    .map(molecularProfileMap::get)
                    .collect(Collectors.toList());

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

            if (alterationTypes.size() == 1 && dataTypes.size() == 1) {
                MolecularAlterationType alterationType = alterationTypes.iterator().next();
                String dataType = dataTypes.iterator().next();
                if (alterationType == MolecularAlterationType.MUTATION_EXTENDED) {
                    mutatedGeneFilters.add(genefilter);
                } else if (alterationType.equals(MolecularAlterationType.STRUCTURAL_VARIANT) && dataType.equals("FUSION")) {
                    // TODO: cleanup once fusion/structural data is fixed in database
                    // until then rename fusion with mutation profile
                    filteredMolecularProfileIds = filteredMolecularProfiles
                            .stream()
                            .map(molecularProfile -> molecularProfile.getCancerStudyIdentifier() + "_mutations")
                            .collect(Collectors.toSet());

                    GeneFilter filter = new GeneFilter();
                    filter.setGeneQueries(genefilter.getGeneQueries());
                    filter.setMolecularProfileIds(filteredMolecularProfileIds);
                    fusionGeneFilters.add(filter);
                } else if (alterationType == MolecularAlterationType.COPY_NUMBER_ALTERATION
                        && dataType.equals("DISCRETE")) {
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

        List<U> resultDataBins = new ArrayList<>();
        List<String> filteredSampleIds = new ArrayList<>();
        List<String> filteredStudyIds = new ArrayList<>();
        List<ClinicalData> filteredData = fetchData(dataBinCountFilter, studyViewFilter, filteredSampleIds,
                filteredStudyIds);

        List<String> filteredUniqueSampleKeys = getUniqkeyKeys(filteredStudyIds, filteredSampleIds);

        Map<String, List<ClinicalData>> filteredClinicalDataByAttributeId = filteredData.stream()
                .collect(Collectors.groupingBy(ClinicalData::getAttrId));

        if (dataBinMethod == DataBinMethod.STATIC) {

            StudyViewFilter filter = studyViewFilter == null ? null : new StudyViewFilter();
            if (filter != null) {
                filter.setStudyIds(studyViewFilter.getStudyIds());
                filter.setSampleIdentifiers(studyViewFilter.getSampleIdentifiers());
            }

            List<String> unfilteredSampleIds = new ArrayList<>();
            List<String> unfilteredStudyIds = new ArrayList<>();
            List<ClinicalData> unfilteredData = fetchData(dataBinCountFilter, filter, unfilteredSampleIds,
                    unfilteredStudyIds);

            List<String> unFilteredUniqueSampleKeys = getUniqkeyKeys(unfilteredSampleIds, unfilteredStudyIds);

            Map<String, List<ClinicalData>> unfilteredDataByAttributeId = unfilteredData.stream()
                    .collect(Collectors.groupingBy(ClinicalData::getAttrId));

            resultDataBins = (List<U>) dataBinFilters.stream().flatMap(dataBinFilter -> {
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

    private <S extends DataBinCountFilter> List<ClinicalData> fetchData(S dataBinCountFilter,
            StudyViewFilter studyViewFilter, List<String> sampleIds, List<String> studyIds) {

        List<SampleIdentifier> filteredSampleIdentifiers = apply(studyViewFilter);
        studyViewFilterUtil.extractStudyAndSampleIds(filteredSampleIdentifiers, studyIds, sampleIds);

        List<MolecularProfile> molecularProfiles = molecularProfileService.getMolecularProfilesInStudies(studyIds,
                "SUMMARY");

        Map<String, List<MolecularProfile>> molecularProfileMap = studyViewFilterUtil
                .categorizeMolecularPorfiles(molecularProfiles);

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

        if (!CollectionUtils.isEmpty(dataFilters) && !CollectionUtils.isEmpty(sampleIdentifiers)) {

            Map<String, List<MolecularProfile>> molecularProfileMap = studyViewFilterUtil
                    .categorizeMolecularPorfiles(molecularProfiles);

            List<String> studyIds = new ArrayList<>();
            List<String> sampleIds = new ArrayList<>();
            studyViewFilterUtil.extractStudyAndSampleIds(sampleIdentifiers, studyIds, sampleIds);
            List<ClinicalData> clinicalDatas = new ArrayList<>();
            List<ClinicalDataFilter> attributes = new ArrayList<>();
            if (dataFilters.get(0) instanceof GenomicDataFilter) {
                List<GenomicDataFilter> genomicDataIntervalFilters = (List<GenomicDataFilter>) dataFilters;
                Set<String> hugoGeneSymbols = genomicDataIntervalFilters.stream()
                        .map(GenomicDataFilter::getHugoGeneSymbol).collect(Collectors.toSet());
                Map<String, Integer> geneNameIdMap = geneService
                        .fetchGenes(new ArrayList<>(hugoGeneSymbols), GeneIdType.HUGO_GENE_SYMBOL.name(),
                                Projection.SUMMARY.name())
                        .stream().collect(Collectors.toMap(Gene::getHugoGeneSymbol, Gene::getEntrezGeneId));

                clinicalDatas = genomicDataIntervalFilters.stream().flatMap(genomicDataFilter -> {

                    Map<String, String> studyIdToMolecularProfileIdMap = molecularProfileMap
                            .getOrDefault(genomicDataFilter.getProfileType(), new ArrayList<MolecularProfile>())
                            .stream().collect(Collectors.toMap(MolecularProfile::getCancerStudyIdentifier,
                                    MolecularProfile::getStableId));

                    GenomicDataBinFilter genomicDataBinFilter = new GenomicDataBinFilter();
                    genomicDataBinFilter.setHugoGeneSymbol(genomicDataFilter.getHugoGeneSymbol());
                    genomicDataBinFilter.setProfileType(genomicDataFilter.getProfileType());
                    return invokeDataFunc(sampleIds, studyIds,
                            Arrays.asList(geneNameIdMap.get(genomicDataFilter.getHugoGeneSymbol()).toString()),
                            studyIdToMolecularProfileIdMap, genomicDataBinFilter, fetchMolecularData);
                }).collect(Collectors.toList());

                attributes = genomicDataIntervalFilters.stream().map(genomicDataIntervalFilter -> {
                    String attributeId = studyViewFilterUtil.getGenomicDataFilterUniqueKey(
                            genomicDataIntervalFilter.getHugoGeneSymbol(), genomicDataIntervalFilter.getProfileType());
                    ClinicalDataFilter clinicalDataIntervalFilter = new ClinicalDataFilter();
                    clinicalDataIntervalFilter.setAttributeId(attributeId);
                    clinicalDataIntervalFilter.setValues(genomicDataIntervalFilter.getValues());
                    return clinicalDataIntervalFilter;
                }).collect(Collectors.toList());
            } else {
                List<GenericAssayDataFilter> genericAssayDataFilters = (List<GenericAssayDataFilter>) dataFilters;

                clinicalDatas = genericAssayDataFilters.stream().flatMap(genericAssayDataFilter -> {

                    Map<String, String> studyIdToMolecularProfileIdMap = molecularProfileMap
                            .getOrDefault(genericAssayDataFilter.getProfileType(), new ArrayList<MolecularProfile>())
                            .stream().collect(Collectors.toMap(MolecularProfile::getCancerStudyIdentifier,
                                    MolecularProfile::getStableId));
                    GenericAssayDataBinFilter genericAssayDataBinFilter = new GenericAssayDataBinFilter();
                    genericAssayDataBinFilter.setStableId(genericAssayDataFilter.getStableId());
                    genericAssayDataBinFilter.setProfileType(genericAssayDataFilter.getProfileType());

                    return invokeDataFunc(sampleIds, studyIds, Arrays.asList(genericAssayDataBinFilter.getStableId()),
                            studyIdToMolecularProfileIdMap, genericAssayDataBinFilter, fetchGenericAssayData);
                }).collect(Collectors.toList());

                attributes = genericAssayDataFilters.stream().map(genomicDataIntervalFilter -> {
                    String attributeId = studyViewFilterUtil.getGenomicDataFilterUniqueKey(
                            genomicDataIntervalFilter.getStableId(), genomicDataIntervalFilter.getProfileType());
                    ClinicalDataFilter clinicalDataIntervalFilter = new ClinicalDataFilter();
                    clinicalDataIntervalFilter.setAttributeId(attributeId);
                    clinicalDataIntervalFilter.setValues(genomicDataIntervalFilter.getValues());
                    return clinicalDataIntervalFilter;
                }).collect(Collectors.toList());

            }

            MultiKeyMap clinicalDataMap = new MultiKeyMap();

            clinicalDatas.forEach(clinicalData -> {
                clinicalDataMap.put(clinicalData.getStudyId(), clinicalData.getSampleId(), clinicalData.getAttrId(),
                        clinicalData.getAttrValue());
            });

            List<SampleIdentifier> newSampleIdentifiers = new ArrayList<>();
            for (SampleIdentifier sampleIdentifier : sampleIdentifiers) {
                int count = clinicalDataIntervalFilterApplier.apply(attributes, clinicalDataMap,
                        sampleIdentifier.getSampleId(), sampleIdentifier.getStudyId(), negateFilters);

                if (count == attributes.size()) {
                    newSampleIdentifiers.add(sampleIdentifier);
                }
            }

            return newSampleIdentifiers;
        }

        return sampleIdentifiers;
    }
}
