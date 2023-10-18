package org.cbioportal.persistence.mybatiscolumnstore;

import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.Gene;
import org.cbioportal.model.Sample;
import org.cbioportal.model.util.Select;
import org.cbioportal.persistence.enums.ClinicalAttributeDataSource;
import org.cbioportal.persistence.enums.ClinicalAttributeDataType;
import org.cbioportal.webparam.CategorizedClinicalDataCountFilter;
import org.cbioportal.webparam.ClinicalDataFilter;
import org.cbioportal.webparam.DataFilterValue;
import org.cbioportal.webparam.GeneFilterQuery;
import org.cbioportal.webparam.StudyViewFilter;
import org.cbioportal.webparam.StudyViewGeneFilter;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/testContextDatabaseClickhouse.xml")
@Configurable
public class StudyViewMyBatisRepositoryTest {

    @Autowired
    private StudyViewMyBatisRepository studyViewMyBatisRepository;

    @Test
    public void getFilteredSamples() {
        StudyViewFilter studyViewFilter = generateStudyViewFilter(new String[]{"msk_ch_2020", "msk_impact_2017"}, null);
        
        // test total number of samples
        List<Sample> samples = studyViewMyBatisRepository.getFilteredSamplesFromColumnstore(studyViewFilter, CategorizedClinicalDataCountFilter.getBuilder().build());
        Assert.assertEquals(11, samples.size());
        
        // test sample numerical clinical attributes
        studyViewFilter = generateStudyViewFilter(
            new String[]{"msk_impact_2017"},
            Collections.singletonList(
                generateNumericalClinicalDataFilter("TUMOR_PURITY", new String[]{"20-35"})
            )
        );

        samples = studyViewMyBatisRepository.getFilteredSamplesFromColumnstore(studyViewFilter, CategorizedClinicalDataCountFilter.getBuilder().build());
        Assert.assertEquals(2, samples.size());
    }

    @Test
    public void getMutatedGenes() {
        StudyViewFilter studyViewFilter = generateStudyViewFilter(new String[]{"msk_ch_2020"}, null);

        List<AlterationCountByGene> mutations = studyViewMyBatisRepository.getMutatedGenes(studyViewFilter, CategorizedClinicalDataCountFilter.getBuilder().build());
        Assert.assertEquals(2, mutations.size());
    }

    @Test
    @Ignore
    public void getPatientClinicalDataFromStudyViewFilter() {
        StudyViewFilter studyViewFilter = generateStudyViewFilter(new String[]{"msk_ch_2020"}, null);
        List<ClinicalData> clinicalData = studyViewMyBatisRepository.getPatientClinicalDataFromStudyViewFilter(studyViewFilter, List.of("AGE"), CategorizedClinicalDataCountFilter.getBuilder().build());
        Assert.assertEquals(24146, clinicalData.size());

        List<ClinicalDataFilter> clinicalDataFilters = Arrays.asList(generateCategoricalClinicalDataFilter("SEX", new String[]{"Female"}));
        CategorizedClinicalDataCountFilter clinicalDataCountFilters = CategorizedClinicalDataCountFilter.getBuilder()
            .setPatientCategoricalClinicalDataFilters(clinicalDataFilters)
            .build();
        studyViewFilter = generateStudyViewFilter((new String[]{"msk_ch_2020"} ), clinicalDataFilters);
        clinicalData = studyViewMyBatisRepository.getPatientClinicalDataFromStudyViewFilter(studyViewFilter, List.of("AGE"), clinicalDataCountFilters);
        Assert.assertEquals(13121, clinicalData.size());

        // TODO how to apply sample identifiers when fetching patient clinical data? 
//        studyViewFilter = generateStudyViewFilter((new String[]{"msk_ch_2020"} ), clinicalDataFilters);
//        List<SampleIdentifier> sampleIdentifierFilters = new ArrayList<>();
//        SampleIdentifier sampleIdentifier = new SampleIdentifier();
//        sampleIdentifier.setSampleId("P-0000015");
//        sampleIdentifier.setStudyId("msk_ch_2020");
//        sampleIdentifierFilters.add(sampleIdentifier);
//        studyViewFilter.setSampleIdentifiers(sampleIdentifierFilters);
//        clinicalData = studyViewMyBatisRepository.getPatientClinicalDataFromStudyViewFilter(studyViewFilter, List.of("AGE"), clinicalDataCountFilters);
//        Assert.assertEquals(1, clinicalData.size());
    }
    
    @Test
    public void getMutatedGenesFiltered() {
        StudyViewFilter studyViewFilter = generateStudyViewFilter(new String[]{"msk_impact_2017"}, null);

        List<StudyViewGeneFilter> studyViewGeneFilters = new ArrayList<>();
        StudyViewGeneFilter mutationStudyViewGeneFilter = new StudyViewGeneFilter();
        mutationStudyViewGeneFilter.setMolecularProfileIds(new HashSet<>(Arrays.asList("msk_impact_2017_mutations")));
        
        mutationStudyViewGeneFilter.setGeneQueries(generateGeneFilters(List.of("SMARCD1", "TP53"), false));

        studyViewGeneFilters.add(mutationStudyViewGeneFilter);
        
        studyViewFilter.setGeneFilters(studyViewGeneFilters);
        
        List<AlterationCountByGene> mutations = studyViewMyBatisRepository.getMutatedGenes(studyViewFilter, CategorizedClinicalDataCountFilter.getBuilder().build());
        Assert.assertEquals(414, mutations.size());
    }
    
    private StudyViewFilter generateStudyViewFilter(
        String[] studyIds,
        List<ClinicalDataFilter> clinicalDataFilters
    ) {
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(Arrays.asList(studyIds));
        studyViewFilter.setClinicalDataFilters(clinicalDataFilters);
        
        return studyViewFilter;
    }
    
    private ClinicalDataFilter generateCategoricalClinicalDataFilter(String attributeId, String[] values) {
        return generateClinicalDataFilter(attributeId, values, mapCategoricalValues);
    }

    private ClinicalDataFilter generateNumericalClinicalDataFilter(String attributeId, String[] values) {
        return generateClinicalDataFilter(attributeId, values, mapNumericalValues);
    }

    private ClinicalDataFilter generateClinicalDataFilter(String attributeId, String[] values, Function<String, DataFilterValue> mapper) {
        ClinicalDataFilter clinicalDataFilter = new ClinicalDataFilter();
        List<DataFilterValue> dataFilterValues = Arrays.stream(values).map(mapper).collect(Collectors.toList());

        clinicalDataFilter.setValues(dataFilterValues);
        clinicalDataFilter.setAttributeId(attributeId);

        return clinicalDataFilter;
    }
    
    private final Function<String, DataFilterValue> mapCategoricalValues = v -> {
        DataFilterValue dataFilterValue = new DataFilterValue();
        dataFilterValue.setValue(v);
        return dataFilterValue;
    };
    
    private List<List<GeneFilterQuery>> generateGeneFilters(List<String> genesToFilter, boolean createOrRelationship) {
        
        boolean includeDriver = true;
        boolean includeVUS = true;
        boolean includeUnknownOncogenicity = true;
        boolean includeGermline = true;
        boolean includeSomatic = true;
        boolean includeUnknownStatus = true;
        Select<String> selectedTiers = Select.none();
        boolean includeUnknownTier = true;
        
        if(createOrRelationship) {
            List<GeneFilterQuery> ordGeneFilters = genesToFilter.stream().map(geneToFilter -> new GeneFilterQuery(geneToFilter,
                    null, null, includeDriver, includeVUS, includeUnknownOncogenicity, selectedTiers,
                    includeUnknownTier, includeGermline, includeSomatic, includeUnknownStatus))
                .collect(Collectors.toList());

            return List.of(ordGeneFilters);
        } else {
            List<List<GeneFilterQuery>> filter = new ArrayList<>();
            for (String geneToFilter : genesToFilter) {
               
                filter.add(List.of( new GeneFilterQuery(geneToFilter, null,
                    null, includeDriver, includeVUS, includeUnknownOncogenicity,  selectedTiers, includeUnknownTier,
                    includeGermline, includeSomatic, includeUnknownStatus)));
            }
            return filter;
        }
    }
    
    private final Function<String, DataFilterValue> mapNumericalValues = v -> {
        DataFilterValue dataFilterValue = new DataFilterValue();

        String[] parts = v.split("-");
        long start = Long.parseLong(parts[0]);
        long end = parts.length > 1 ? Long.parseLong(parts[1]) : start;
        dataFilterValue.setStart(BigDecimal.valueOf(start));
        dataFilterValue.setEnd(BigDecimal.valueOf(end));
        
        return dataFilterValue;
    };
    @Test
    @Ignore
    public void getClinicalAttributeNames() {
        List<String> names = studyViewMyBatisRepository.getClinicalDataAttributeNames(ClinicalAttributeDataSource.PATIENT, ClinicalAttributeDataType.CATEGORICAL);
        Assert.assertTrue(names.size() > 0);
        Assert.assertTrue(names.contains("OS_STATUS"));
    }
    
    @Test
    @Ignore
    public void getFilterSamplesWithClinicalDataFilter() {
        ClinicalDataFilter clinicalDataFilter = new ClinicalDataFilter();
        clinicalDataFilter.setAttributeId("TMB_NONSYNONYMOUS");
        DataFilterValue filterValue = new DataFilterValue();
        filterValue.setStart(BigDecimal.valueOf(0.033333333));
        filterValue.setEnd(BigDecimal.valueOf(0.033333333));
        clinicalDataFilter.setValues(List.of(filterValue));
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter = CategorizedClinicalDataCountFilter.getBuilder()
            .setSampleNumericalClinicalDataFilters(List.of(clinicalDataFilter))
            .build();

        List<String> studyIds = new ArrayList<>();
        studyIds.add("msk_ch_2020");
        studyViewFilter.setStudyIds(studyIds);
        
        List<ClinicalDataFilter> test = categorizedClinicalDataCountFilter.getSampleNumericalClinicalDataFilters();
        List<Sample> samples = studyViewMyBatisRepository.getFilteredSamplesFromColumnstore(studyViewFilter, categorizedClinicalDataCountFilter);
        Assert.assertEquals(4507, samples.size()); 
    }
    
    @Test
    //@Ignore
    public void getFilterSamplesWithCategoricalSampleClinicalDataFilter() {
        StudyViewFilter studyViewFilter = generateStudyViewFilter(
            new String[]{"msk_ch_2020"},null
        );
        CategorizedClinicalDataCountFilter clincalDataCountFilters = CategorizedClinicalDataCountFilter.getBuilder()
            .setSampleCategoricalClinicalDataFilters(Arrays.asList(generateCategoricalClinicalDataFilter("CANCER_TYPE", new String[]{"Bladder Cancer", "Breast Cancer"})))
            .build();
        
        List<Sample> filteredSamples = studyViewMyBatisRepository.getFilteredSamplesFromColumnstore(studyViewFilter, clincalDataCountFilters);
        Assert.assertEquals(1435, filteredSamples.size());
    }
    @Test
    @Ignore
    public void getFilterSamplesWithNumericPatientClinicalDataFilter() {
        StudyViewFilter studyViewFilter = generateStudyViewFilter(
            new String[]{"msk_ch_2020"},null
        );
        CategorizedClinicalDataCountFilter clincalDataCountFilters = CategorizedClinicalDataCountFilter.getBuilder()
            .setPatientNumericalClinicalDataFilters(Arrays.asList(generateNumericalClinicalDataFilter("AGE", new String[]{"60-65"})))
            .build();

        List<Sample> filteredSamples = studyViewMyBatisRepository.getFilteredSamplesFromColumnstore(studyViewFilter, clincalDataCountFilters);
        Assert.assertEquals(3343, filteredSamples.size());
    }
    @Test
    @Ignore
    public void getFilterSamplesWithPatientClinicalDataFilterAndSampleClinicalDataFilter() {
        StudyViewFilter studyViewFilter = generateStudyViewFilter(
            new String[]{"msk_ch_2020"},null
        );

        ClinicalDataFilter clinicalDataFilter = new ClinicalDataFilter();
        clinicalDataFilter.setAttributeId("TMB_NONSYNONYMOUS");
        DataFilterValue filterValue = new DataFilterValue();
        filterValue.setStart(BigDecimal.valueOf(0.033333333));
        filterValue.setEnd(BigDecimal.valueOf(0.033333333));
        clinicalDataFilter.setValues(List.of(filterValue));
        
        CategorizedClinicalDataCountFilter clincalDataCountFilters = CategorizedClinicalDataCountFilter.getBuilder()
            .setPatientNumericalClinicalDataFilters(List.of(generateNumericalClinicalDataFilter("AGE", new String[]{"60-65"})))
            .setSampleNumericalClinicalDataFilters(List.of(clinicalDataFilter))
            .setSampleCategoricalClinicalDataFilters(List.of(generateCategoricalClinicalDataFilter("CANCER_TYPE", new String[]{"Bladder Cancer"})))
            .build();

        List<Sample> filteredSamples = studyViewMyBatisRepository.getFilteredSamplesFromColumnstore(studyViewFilter, clincalDataCountFilters);
        Assert.assertEquals(21, filteredSamples.size());
    }

    @Test
    @Ignore
    public void getMutatedGenesWithPatientClinicalDataFilterAndSampleClinicalDataFilter() {
        StudyViewFilter studyViewFilter = generateStudyViewFilter(
            new String[]{"msk_ch_2020"},null
        );

        ClinicalDataFilter clinicalDataFilter = new ClinicalDataFilter();
        clinicalDataFilter.setAttributeId("TMB_NONSYNONYMOUS");
        DataFilterValue filterValue = new DataFilterValue();
        filterValue.setStart(BigDecimal.valueOf(0.033333333));
        filterValue.setEnd(BigDecimal.valueOf(0.033333333));
        clinicalDataFilter.setValues(List.of(filterValue));

        CategorizedClinicalDataCountFilter clinicalDataCountFilters = CategorizedClinicalDataCountFilter.getBuilder()
            .setPatientNumericalClinicalDataFilters(List.of(generateNumericalClinicalDataFilter("AGE", new String[]{"60-65"})))
            .setSampleNumericalClinicalDataFilters(List.of(clinicalDataFilter))
            .setSampleCategoricalClinicalDataFilters(List.of(generateCategoricalClinicalDataFilter("CANCER_TYPE", new String[]{"Bladder Cancer"})))
            .build();

        List<AlterationCountByGene> mutatedGenes = studyViewMyBatisRepository.getMutatedGenes(studyViewFilter, clinicalDataCountFilters);
        Assert.assertEquals(16, mutatedGenes.size());
    }
    
    @Test
    @Ignore
    public void getClinicalDataCounts() {
        StudyViewFilter studyViewFilter = generateStudyViewFilter(
            new String[]{"msk_ch_2020"},null
        );

        CategorizedClinicalDataCountFilter clinicalDataCountFilters = CategorizedClinicalDataCountFilter.getBuilder()
            .setPatientNumericalClinicalDataFilters(List.of(generateNumericalClinicalDataFilter("AGE", new String[]{"60-65"})))
            .setSampleCategoricalClinicalDataFilters(List.of(generateCategoricalClinicalDataFilter("CANCER_TYPE", new String[]{"Bladder Cancer"})))
            .build();
        
        List<ClinicalDataCount> counts = studyViewMyBatisRepository.getClinicalDataCounts(studyViewFilter, clinicalDataCountFilters,
            List.of("SEX"));
        
        Assert.assertEquals(counts.size(), 2);
    }
    
    @Test
    public void getFilteredSamplesWithGeneFilters() {
        StudyViewFilter studyViewFilter = generateStudyViewFilter(new String[]{"msk_impact_2017"}, null);

        List<StudyViewGeneFilter> studyViewGeneFilters = new ArrayList<>();
        StudyViewGeneFilter mutationStudyViewGeneFilter = new StudyViewGeneFilter();
        mutationStudyViewGeneFilter.setMolecularProfileIds(new HashSet<>(List.of("msk_impact_2017_mutations")));
        mutationStudyViewGeneFilter.setGeneQueries(generateGeneFilters(List.of("KRAS", "TP53"), false));
        studyViewGeneFilters.add(mutationStudyViewGeneFilter);

        studyViewFilter.setGeneFilters(studyViewGeneFilters);
        
        List<Sample> samples = studyViewMyBatisRepository.getFilteredSamplesFromColumnstore(studyViewFilter, CategorizedClinicalDataCountFilter.getBuilder().build());
        Assert.assertEquals(845, samples.size());

        mutationStudyViewGeneFilter.setGeneQueries(generateGeneFilters(List.of("SMARCD1", "TP53"), true));
        List<Sample> samplesFilteredViaOrRelationship = studyViewMyBatisRepository.getFilteredSamplesFromColumnstore(studyViewFilter, CategorizedClinicalDataCountFilter.getBuilder().build());
        Assert.assertEquals(4561, samplesFilteredViaOrRelationship.size()); 
    }

    @Test
    public void getFilteredSamplesWithMultipleStudyViewGeneFilters() {
        StudyViewFilter studyViewFilter = generateStudyViewFilter(new String[]{"msk_impact_2017"}, null);

        List<StudyViewGeneFilter> studyViewGeneFilters = new ArrayList<>();
        StudyViewGeneFilter mutationStudyViewGeneFilter = new StudyViewGeneFilter();
        mutationStudyViewGeneFilter.setMolecularProfileIds(new HashSet<>(List.of("msk_impact_2017_mutations")));
        mutationStudyViewGeneFilter.setGeneQueries(generateGeneFilters(List.of("TP53"), false));
        
        StudyViewGeneFilter cnaStudyViewGeneFilter = new StudyViewGeneFilter();
        cnaStudyViewGeneFilter.setMolecularProfileIds(new HashSet<>(List.of("msk_impact_2017_structural_variants")));
        cnaStudyViewGeneFilter.setGeneQueries(generateGeneFilters(List.of("BRAF"), true));
        studyViewGeneFilters.add(mutationStudyViewGeneFilter);
        studyViewGeneFilters.add(cnaStudyViewGeneFilter);

        studyViewFilter.setGeneFilters(studyViewGeneFilters);

        List<Sample> samples = studyViewMyBatisRepository.getFilteredSamplesFromColumnstore(studyViewFilter, CategorizedClinicalDataCountFilter.getBuilder().build());
        Assert.assertEquals(16, samples.size()); 
    }
}