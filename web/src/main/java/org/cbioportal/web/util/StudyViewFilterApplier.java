package org.cbioportal.web.util;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
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
                                  ClinicalAttributeService clinicalAttributeService) {
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

        if (!CollectionUtils.isEmpty(studyViewFilter.getGeneFilters())) {

            Set<String> molecularProfileIds = studyViewFilter.getGeneFilters().stream()
                    .flatMap(geneFilter -> geneFilter.getMolecularProfileIds().stream()).collect(Collectors.toSet());

            List<MolecularProfile> molecularProfiles = molecularProfileService
                    .getMolecularProfiles(new ArrayList<String>(molecularProfileIds), "SUMMARY");
            Map<String, MolecularProfile> molecularProfileMap = molecularProfiles.stream()
                    .collect(Collectors.toMap(MolecularProfile::getStableId, Function.identity()));

            sampleIdentifiers = filterMutatedOrFusionGenes(studyViewFilter.getGeneFilters(), molecularProfileMap,
                    MolecularAlterationType.MUTATION_EXTENDED, sampleIdentifiers);
            sampleIdentifiers = filterMutatedOrFusionGenes(studyViewFilter.getGeneFilters(), molecularProfileMap,
                    MolecularAlterationType.FUSION, sampleIdentifiers);
            sampleIdentifiers = filterCNAGenes(studyViewFilter.getGeneFilters(), molecularProfileMap,
                    sampleIdentifiers);
        }

        Boolean withMutationData = studyViewFilter.getWithMutationData();
        if (withMutationData != null && !sampleIdentifiers.isEmpty()) {
            sampleIdentifiers = filterByProfiled(sampleIdentifiers, withMutationData, molecularProfileService::getFirstMutationProfileIds);
        }

        Boolean withCNAData = studyViewFilter.getWithCNAData();
        if (withCNAData != null && !sampleIdentifiers.isEmpty()) {
            sampleIdentifiers = filterByProfiled(sampleIdentifiers, withCNAData, molecularProfileService::getFirstDiscreteCNAProfileIds);
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

    private List<SampleIdentifier> filterMutatedOrFusionGenes(List<GeneFilter> genefilters,
            Map<String, MolecularProfile> molecularProfileMap, MolecularAlterationType MolecularAlterationFilterType,
            List<SampleIdentifier> sampleIdentifiers) {

        for (GeneFilter genefilter : genefilters) {

            List<MolecularProfile> filteredMolecularProfiles = genefilter.getMolecularProfileIds().stream()
                    .map(molecularProfileId -> molecularProfileMap.get(molecularProfileId))
                    .collect(Collectors.toList());

            Set<MolecularAlterationType> alterationTypes = filteredMolecularProfiles.stream()
                    .map(MolecularProfile::getMolecularAlterationType).collect(Collectors.toSet());

            if (alterationTypes.size() == 1 && alterationTypes.iterator().next() == MolecularAlterationFilterType) {
                for (List<SingleGeneQuery> geneQueries : genefilter.getSingleGeneQueries()) {
                    List<String> studyIds = new ArrayList<>();
                    List<String> sampleIds = new ArrayList<>();

                    List<String> hugoGeneSymbols = geneQueries.stream().map(SingleGeneQuery::getHugoGeneSymbol)
                            .collect(Collectors.toList());

                    List<Integer> entrezGeneIds = geneService
                            .fetchGenes(hugoGeneSymbols, GeneIdType.HUGO_GENE_SYMBOL.name(), Projection.SUMMARY.name())
                            .stream().map(gene -> gene.getEntrezGeneId()).collect(Collectors.toList());

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

                    List<Mutation> mutations = mutationService.getMutationsInMultipleMolecularProfiles(
                            molecularProfileIds, sampleIds, entrezGeneIds, Projection.ID.name(), null, null, null,
                            null);

                    sampleIdentifiers = mutations.stream().map(m -> {
                        SampleIdentifier sampleIdentifier = new SampleIdentifier();
                        sampleIdentifier.setSampleId(m.getSampleId());
                        sampleIdentifier.setStudyId(m.getStudyId());
                        return sampleIdentifier;
                    }).distinct().collect(Collectors.toList());

                }
            } else {
                new ArrayList<SampleIdentifier>();
            }
        }

        return sampleIdentifiers;
    }

    private List<SampleIdentifier> filterCNAGenes(List<GeneFilter> cnaGenes,
            Map<String, MolecularProfile> molecularProfileMap, List<SampleIdentifier> sampleIdentifiers) {

        for (GeneFilter copyNumberGeneFilter : cnaGenes) {

            List<MolecularProfile> filteredMolecularProfiles = copyNumberGeneFilter.getMolecularProfileIds().stream()
                    .map(molecularProfileId -> molecularProfileMap.get(molecularProfileId))
                    .collect(Collectors.toList());

            Set<MolecularAlterationType> alterationTypes = filteredMolecularProfiles.stream()
                    .map(MolecularProfile::getMolecularAlterationType).collect(Collectors.toSet());

            if (alterationTypes.size() == 1
                    && alterationTypes.iterator().next() == MolecularAlterationType.COPY_NUMBER_ALTERATION
                    && filteredMolecularProfiles.get(0).getDatatype().equals("DISCRETE")) {

                for (List<SingleGeneQuery> geneQueries : copyNumberGeneFilter.getSingleGeneQueries()) {

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
            } else {
                new ArrayList<SampleIdentifier>();
            }
        }

        return sampleIdentifiers;
    }

}
