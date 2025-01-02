package org.cbioportal.persistence.mybatisclickhouse;
import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.CaseListDataCount;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.ClinicalDataCountItem;
import org.cbioportal.model.ClinicalEventTypeCount;
import org.cbioportal.model.GenePanelToGene;
import org.cbioportal.model.GenericAssayDataCountItem;
import org.cbioportal.model.GenomicDataCountItem;
import org.cbioportal.model.GenomicDataCount;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.PatientTreatment;
import org.cbioportal.model.Sample;
import org.cbioportal.model.SampleToPanel;
import org.cbioportal.model.SampleTreatment;
import org.cbioportal.model.StudyViewFilterContext;
import org.cbioportal.persistence.StudyViewRepository;
import org.cbioportal.persistence.enums.DataSource;
import org.cbioportal.persistence.helper.AlterationFilterHelper;
import org.cbioportal.persistence.helper.StudyViewFilterHelper;
import org.cbioportal.service.util.StudyViewColumnarServiceUtil;
import org.cbioportal.utils.config.annotation.ConditionalOnProperty;
import org.cbioportal.web.parameter.ClinicalDataType;
import org.cbioportal.web.parameter.GenericAssayDataBinFilter;
import org.cbioportal.web.parameter.GenericAssayDataFilter;
import org.cbioportal.web.parameter.GenomicDataBinFilter;
import org.cbioportal.web.parameter.GenomicDataFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@ConditionalOnProperty(name = "clickhouse_mode", havingValue = "true")
public class StudyViewMyBatisRepository implements StudyViewRepository {

    private final StudyViewMapper studyViewMapper;
    private Map<DataSource, List<ClinicalAttribute>> clinicalAttributesMap = new EnumMap<>(DataSource.class);
    private Map<DataSource, List<MolecularProfile>> genericAssayProfilesMap = new EnumMap<>(DataSource.class);
    
    private static final List<String> FILTERED_CLINICAL_ATTR_VALUES = Collections.emptyList();
    private final StudyViewMapper mapper;
   
    @Autowired
    public StudyViewMyBatisRepository(StudyViewMapper mapper, StudyViewMapper studyViewMapper) {
        this.mapper = mapper;
        this.studyViewMapper = studyViewMapper;
    }
    
    @Override
    public List<Sample> getFilteredSamples(StudyViewFilterContext studyViewFilterContext) {
        return mapper.getFilteredSamples(createStudyViewFilterHelper(studyViewFilterContext));
    }

    @Override
    public List<String> getFilteredStudyIds(StudyViewFilterContext studyViewFilterContext) {
        return mapper.getFilteredStudyIds(createStudyViewFilterHelper(studyViewFilterContext));
    }

    @Override
    public List<AlterationCountByGene> getMutatedGenes(StudyViewFilterContext studyViewFilterContext) {
        return mapper.getMutatedGenes(createStudyViewFilterHelper(studyViewFilterContext),
            AlterationFilterHelper.build(studyViewFilterContext.studyViewFilter().getAlterationFilter()));
    }

    @Override
    public List<CopyNumberCountByGene> getCnaGenes(StudyViewFilterContext studyViewFilterContext) {
        return mapper.getCnaGenes(createStudyViewFilterHelper(studyViewFilterContext),
            AlterationFilterHelper.build(studyViewFilterContext.studyViewFilter().getAlterationFilter()));
    }

    @Override
    public List<AlterationCountByGene> getStructuralVariantGenes(StudyViewFilterContext studyViewFilterContext) {
        return mapper.getStructuralVariantGenes(createStudyViewFilterHelper(studyViewFilterContext),
            AlterationFilterHelper.build(studyViewFilterContext.studyViewFilter().getAlterationFilter()));
    }

    @Override
    public List<ClinicalDataCountItem> getClinicalDataCounts(StudyViewFilterContext studyViewFilterContext, List<String> filteredAttributes) {
        return mapper.getClinicalDataCounts(createStudyViewFilterHelper(studyViewFilterContext),
            filteredAttributes, FILTERED_CLINICAL_ATTR_VALUES);
    }

    @Override
    public List<GenomicDataCount> getMolecularProfileSampleCounts(StudyViewFilterContext studyViewFilterContext) {
        var sampleCounts = mapper.getMolecularProfileSampleCounts(createStudyViewFilterHelper(studyViewFilterContext));
        return StudyViewColumnarServiceUtil.mergeGenomicDataCounts(sampleCounts);
        
    }
    
    public StudyViewFilterHelper createStudyViewFilterHelper(StudyViewFilterContext studyViewFilterContext) {
        return StudyViewFilterHelper.build(
            studyViewFilterContext.studyViewFilter(),
            getGenericAssayProfilesMap(),
            studyViewFilterContext.customDataFilterSamples(),
            studyViewFilterContext.involvedCancerStudies()
        );    
    }
    
    @Override
    public List<ClinicalAttribute> getClinicalAttributes() {
        return mapper.getClinicalAttributes();
    }

    @Override
    public List<MolecularProfile> getGenericAssayProfiles() {
        return mapper.getGenericAssayProfiles();
    }

    @Override
    public List<MolecularProfile> getFilteredMolecularProfilesByAlterationType(StudyViewFilterContext studyViewFilterContext, String alterationType) {
        return studyViewMapper.getFilteredMolecularProfilesByAlterationType(createStudyViewFilterHelper(studyViewFilterContext), alterationType);
    }

    @Override
    public Map<String, ClinicalDataType> getClinicalAttributeDatatypeMap() {
        if (clinicalAttributesMap.isEmpty()) {
            buildClinicalAttributeNameMap();
        }
        
        Map<String, ClinicalDataType> attributeDatatypeMap = new HashMap<>();

        clinicalAttributesMap
            .get(DataSource.SAMPLE)
            .forEach(attribute -> attributeDatatypeMap.put(attribute.getAttrId(), ClinicalDataType.SAMPLE));

        clinicalAttributesMap
            .get(DataSource.PATIENT)
            .forEach(attribute -> attributeDatatypeMap.put(attribute.getAttrId(), ClinicalDataType.PATIENT));
        
        return attributeDatatypeMap;
    }

    @Override
    public List<ClinicalAttribute> getClinicalAttributesForStudies(List<String> studyIds) {
        return mapper.getClinicalAttributesForStudies(studyIds);
    }
    
    @Override
    public List<CaseListDataCount> getCaseListDataCountsPerStudy(StudyViewFilterContext studyViewFilterContext) {
        return mapper.getCaseListDataCountsPerStudy(createStudyViewFilterHelper(studyViewFilterContext));
    }

    @Override
    public List<ClinicalData> getSampleClinicalData(StudyViewFilterContext studyViewFilterContext, List<String> attributeIds) {
        return mapper.getSampleClinicalDataFromStudyViewFilter(createStudyViewFilterHelper(studyViewFilterContext), attributeIds);
    }
    
    @Override
    public List<ClinicalData> getPatientClinicalData(StudyViewFilterContext studyViewFilterContext, List<String> attributeIds) {
        return mapper.getPatientClinicalDataFromStudyViewFilter(createStudyViewFilterHelper(studyViewFilterContext), attributeIds);
    }
    
    @Override
    public Map<String, Integer> getTotalProfiledCounts(StudyViewFilterContext studyViewFilterContext, String alterationType, List<MolecularProfile> molecularProfiles) {
        return mapper.getTotalProfiledCounts(createStudyViewFilterHelper(studyViewFilterContext), alterationType, molecularProfiles)
            .stream()
            .collect(Collectors.groupingBy(AlterationCountByGene::getHugoGeneSymbol,
                Collectors.mapping(AlterationCountByGene::getNumberOfProfiledCases, Collectors.summingInt(Integer::intValue))));
    }

    @Override
    public int getFilteredSamplesCount(StudyViewFilterContext studyViewFilterContext) {
        return mapper.getFilteredSamplesCount(createStudyViewFilterHelper(studyViewFilterContext));
    }

    @Override
    public int getFilteredPatientCount(StudyViewFilterContext studyViewFilterContext) {
        return mapper.getFilteredPatientsCount(createStudyViewFilterHelper(studyViewFilterContext));
    }

    @Override
    public Map<String, Set<String>> getMatchingGenePanelIds(StudyViewFilterContext studyViewFilterContext, String alterationType) {
        return mapper.getMatchingGenePanelIds(createStudyViewFilterHelper(studyViewFilterContext), alterationType)
            .stream()
            .collect(Collectors.groupingBy(GenePanelToGene::getHugoGeneSymbol,
                Collectors.mapping(GenePanelToGene::getGenePanelId, Collectors.toSet())));
    }

    @Override
    public int getTotalProfiledCountsByAlterationType(StudyViewFilterContext studyViewFilterContext, String alterationType) {
       return mapper.getTotalProfiledCountByAlterationType(createStudyViewFilterHelper(studyViewFilterContext), alterationType); 
    }

    @Override
    public int getSampleProfileCountWithoutPanelData(StudyViewFilterContext studyViewFilterContext, String alterationType) {
        return mapper.getSampleProfileCountWithoutPanelData(createStudyViewFilterHelper(studyViewFilterContext), alterationType);
    }


    @Override
    public List<ClinicalEventTypeCount> getClinicalEventTypeCounts(StudyViewFilterContext studyViewFilterContext) {
        return mapper.getClinicalEventTypeCounts(createStudyViewFilterHelper(studyViewFilterContext));
    }

    @Override
    public List<PatientTreatment> getPatientTreatments(StudyViewFilterContext studyViewFilterContext) {
        return mapper.getPatientTreatments(createStudyViewFilterHelper(studyViewFilterContext));
    }
    
    @Override
    public int getTotalPatientTreatmentCount(StudyViewFilterContext studyViewFilterContext) {
       return mapper.getPatientTreatmentCounts(createStudyViewFilterHelper(studyViewFilterContext));
    }

    @Override
    public List<SampleTreatment> getSampleTreatments(StudyViewFilterContext studyViewFilterContext) {
        return mapper.getSampleTreatmentCounts(createStudyViewFilterHelper(studyViewFilterContext));
    }

    @Override
    public int getTotalSampleTreatmentCount(StudyViewFilterContext studyViewFilterContext) {
        return mapper.getTotalSampleTreatmentCounts(createStudyViewFilterHelper(studyViewFilterContext));
    }
    
    @Override
    public List<ClinicalDataCount> getGenomicDataBinCounts(StudyViewFilterContext studyViewFilterContext, List<GenomicDataBinFilter> genomicDataBinFilters) {
        return mapper.getGenomicDataBinCounts(createStudyViewFilterHelper(studyViewFilterContext), genomicDataBinFilters);
    }

    @Override
    public List<ClinicalDataCount> getGenericAssayDataBinCounts(StudyViewFilterContext studyViewFilterContext, List<GenericAssayDataBinFilter> genericAssayDataBinFilters) {
        return mapper.getGenericAssayDataBinCounts(createStudyViewFilterHelper(studyViewFilterContext), genericAssayDataBinFilters);
    }
    
    private void buildClinicalAttributeNameMap() {
        clinicalAttributesMap = this.getClinicalAttributes()
            .stream()
            .collect(Collectors.groupingBy(ca -> ca.getPatientAttribute().booleanValue() ? DataSource.PATIENT : DataSource.SAMPLE));
    }

    private void buildGenericAssayProfilesMap() {
        genericAssayProfilesMap = this.getGenericAssayProfiles()
            .stream()
            .collect(Collectors.groupingBy(ca -> ca.getPatientLevel().booleanValue() ? DataSource.PATIENT : DataSource.SAMPLE));
    }

    private Map<DataSource, List<MolecularProfile>> getGenericAssayProfilesMap() {
        if (genericAssayProfilesMap.isEmpty()) {
            buildGenericAssayProfilesMap();
        }
        return genericAssayProfilesMap;
    }
    
    @Override
    public List<GenomicDataCountItem> getCNACounts(StudyViewFilterContext studyViewFilterContext, List<GenomicDataFilter> genomicDataFilters) {
        return mapper.getCNACounts(createStudyViewFilterHelper(studyViewFilterContext), genomicDataFilters);
    }

    @Override
    public List<GenericAssayDataCountItem> getGenericAssayDataCounts(StudyViewFilterContext studyViewFilterContext, List<GenericAssayDataFilter> genericAssayDataFilters) {
        return mapper.getGenericAssayDataCounts(createStudyViewFilterHelper(studyViewFilterContext), genericAssayDataFilters);
    }

    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver"
    )
    public Map<String, Map<String, GenePanelToGene>> getGenePanelsToGenes(){
        List<GenePanelToGene> genesWithPanels = mapper.getGenePanelGenes();

        Map<String, Map<String, GenePanelToGene>> panelsToGeneMaps = genesWithPanels.stream()
            .collect(Collectors.groupingBy(
                GenePanelToGene::getGenePanelId,
                Collectors.toMap(
                    GenePanelToGene::getHugoGeneSymbol,
                    panelGene -> panelGene,
                    (existing, replacement) -> existing // handle duplicates by keeping the existing entry
                )
            ));
        
        return panelsToGeneMaps;
    }
    
//    private doIt(){
//        Map<String, Map<String, Integer>> alteredGenesWithCounts = new HashMap<>();
//
//        for (Map<String, Object> alteration : alterations) {
//            String hugoGeneSymbol = (String) alteration.get("hugo_gene_symbol");
//            int count = Integer.parseInt(alteration.get("count").toString());
//
//            if (!alteredGenesWithCounts.containsKey(hugoGeneSymbol)) {
//                alteredGenesWithCounts.put(hugoGeneSymbol, new HashMap<>());
//                alteredGenesWithCounts.get(hugoGeneSymbol).put("count", 0);
//            }
//
//            alteredGenesWithCounts.get(hugoGeneSymbol).put("count",
//                alteredGenesWithCounts.get(hugoGeneSymbol).get("count") + count);
//        }
//        
//    }
    
    @Override
    public HashMap<String, AlterationCountByGene> getAlterationEnrichmentCounts(List<String> sampleStableIds) {
        
        // we need a map of panels to genes which are profiled by them
        var panelToGeneMap = getGenePanelsToGenes();
        
        List<SampleToPanel> sampleToGenePanels = mapper.getSampleToGenePanels(sampleStableIds);
        
        // group the panels by the sample ids which they are associated with
        // this tells us for each sample, what gene panels were applied
        var samplesToPanelMap = sampleToGenePanels.stream()
            .collect(Collectors.groupingBy(
                    SampleToPanel::getSampleUniqueId,
                    Collectors.mapping(e->e.getGenePanelId(), Collectors.toSet())
                )
            );
       
        
         // many of the samples are governed by the same combination of panels
         // we want to group the samples by a key that represents the set of panels applied
        Map<String, List<String>> clumps = samplesToPanelMap.keySet().stream().collect(Collectors.groupingBy(
            sampleId->samplesToPanelMap.get(sampleId).stream().collect(Collectors.joining(","))
        ));


        var alterationCounts = mapper.getAlterationEnrichmentCounts(sampleStableIds);

        HashMap<String, AlterationCountByGene> alteredGenesWithCounts = new HashMap();

        // we need map of genes to alteration counts
        alterationCounts.stream().forEach((alterationCountByGene) -> {
            String hugoGeneSymbol = alterationCountByGene.getHugoGeneSymbol();
            int count = alterationCountByGene.getNumberOfAlteredCases();
            if (!alteredGenesWithCounts.containsKey(hugoGeneSymbol)) {
                var acg = new AlterationCountByGene();
                acg.setHugoGeneSymbol(hugoGeneSymbol);
                acg.setNumberOfAlteredCases(0);
                alteredGenesWithCounts.put(hugoGeneSymbol,acg);
            }
            // add the count to existing tally
            alteredGenesWithCounts.get(hugoGeneSymbol).setNumberOfAlteredCases(
                count + alteredGenesWithCounts.get(hugoGeneSymbol).getNumberOfAlteredCases()
            );
        
        });

        var geneCount = new HashMap<String,AlterationCountByGene>();
        
        clumps.entrySet().stream().forEach(entry->{
            
            var geneLists = Arrays.stream(entry.getKey().split(","))
                .map(panelId -> panelToGeneMap.get(panelId))
                .collect(Collectors.toList());

            Set<String> mergeGenes = geneLists.stream()
                .map(Map::keySet)
                .reduce((set1, set2) -> {
                    set1.retainAll(set2);
                    return set1;
                }).orElse(Collections.emptySet());
            
            mergeGenes.stream().forEach(
                gene->{
                    if (geneCount.containsKey(gene)) {
                        var count = geneCount.get(gene);
                        count.setNumberOfProfiledCases(count.getNumberOfProfiledCases() + entry.getValue().size());
                    } else {
                        var alterationCountByGene = new AlterationCountByGene();
                        alterationCountByGene.setHugoGeneSymbol(gene);
                        alterationCountByGene.setNumberOfProfiledCases(entry.getValue().size());
                        alterationCountByGene.setNumberOfAlteredCases(0);
                        geneCount.put(gene,alterationCountByGene);
                    }
                });
            
        });
        
        alteredGenesWithCounts.entrySet().stream().forEach(n->{
            if (geneCount.containsKey(n.getKey())) {
                n.getValue().setNumberOfProfiledCases(
                    geneCount.get(n.getKey()).getNumberOfProfiledCases()
                );
            }
        });
        
        return alteredGenesWithCounts;
    }

    public Map<String, Integer> getMutationCounts(StudyViewFilterContext studyViewFilterContext, GenomicDataFilter genomicDataFilter) {
        return mapper.getMutationCounts(createStudyViewFilterHelper(studyViewFilterContext), genomicDataFilter);
    }
    
    public List<GenomicDataCountItem> getMutationCountsByType(StudyViewFilterContext studyViewFilterContext, List<GenomicDataFilter> genomicDataFilters) {
        return mapper.getMutationCountsByType(createStudyViewFilterHelper(studyViewFilterContext), genomicDataFilters);
    }



}