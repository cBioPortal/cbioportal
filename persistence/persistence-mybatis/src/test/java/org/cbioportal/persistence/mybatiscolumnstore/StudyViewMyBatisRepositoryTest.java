package org.cbioportal.persistence.mybatiscolumnstore;

import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.Sample;
import org.cbioportal.webparam.ClinicalDataFilter;
import org.cbioportal.webparam.DataFilterValue;
import org.cbioportal.webparam.StudyViewFilter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.util.*;
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
        List<Sample> samples = studyViewMyBatisRepository.getFilteredSamplesFromColumnstore(studyViewFilter);
        Assert.assertEquals(11, samples.size());
        
        // test sample numerical clinical attributes
        studyViewFilter = generateStudyViewFilter(
            new String[]{"msk_impact_2017"},
            Collections.singletonList(
                generateNumericalClinicalDataFilter("TUMOR_PURITY", new String[]{"20-35"})
            )
        );

        samples = studyViewMyBatisRepository.getFilteredSamplesFromColumnstore(studyViewFilter);
        Assert.assertEquals(2, samples.size());
    }

    @Test
    public void getMutatedGenes() {
        StudyViewFilter studyViewFilter = generateStudyViewFilter(new String[]{"msk_ch_2020"}, null);

        List<AlterationCountByGene> mutations = studyViewMyBatisRepository.getMutatedGenes(studyViewFilter);
        Assert.assertEquals(2, mutations.size());
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
    
    private final Function<String, DataFilterValue> mapNumericalValues = v -> {
        DataFilterValue dataFilterValue = new DataFilterValue();

        String[] parts = v.split("-");
        long start = Long.parseLong(parts[0]);
        long end = parts.length > 1 ? Long.parseLong(parts[1]) : start;
        dataFilterValue.setStart(BigDecimal.valueOf(start));
        dataFilterValue.setEnd(BigDecimal.valueOf(end));
        
        return dataFilterValue;
    };
}