package org.cbioportal.persistence.mybatisclickhouse;

import org.cbioportal.persistence.helper.StudyViewFilterHelper;
import org.cbioportal.persistence.mybatisclickhouse.config.MyBatisConfig;
import org.cbioportal.web.parameter.ClinicalDataFilter;
import org.cbioportal.web.parameter.CustomSampleIdentifier;
import org.cbioportal.web.parameter.DataFilterValue;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@Import(MyBatisConfig.class)
@DataJpaTest
@DirtiesContext
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = AbstractTestcontainers.Initializer.class)
public class FilteredSamplesTest extends AbstractTestcontainers {

    private static final String STUDY_TCGA_PUB = "study_tcga_pub";
    private static final String STUDY_ACC_TCGA = "acc_tcga";
    private static final String STUDY_GENIE_PUB = "study_genie_pub";

    @Autowired
    private StudyViewMapper studyViewMapper;

    @Test
    public void getFilteredSamples() {
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(Arrays.asList(STUDY_TCGA_PUB, STUDY_ACC_TCGA));
        var filteredSamples = studyViewMapper.getFilteredSamples(StudyViewFilterHelper.build(studyViewFilter, null, null, studyViewFilter.getStudyIds()));
        assertEquals(19, filteredSamples.size());

        ClinicalDataFilter customDataFilter = new ClinicalDataFilter();
        customDataFilter.setAttributeId("123");
        DataFilterValue value = new DataFilterValue();
        customDataFilter.setValues(List.of(value));
        studyViewFilter.setCustomDataFilters(List.of(customDataFilter));
        var filteredSamples1 = studyViewMapper.getFilteredSamples(StudyViewFilterHelper.build(studyViewFilter, null, new ArrayList<>(), studyViewFilter.getStudyIds()));
        assertEquals(0, filteredSamples1.size());

        CustomSampleIdentifier customSampleIdentifier = new CustomSampleIdentifier();
        customSampleIdentifier.setStudyId("acc_tcga");
        customSampleIdentifier.setSampleId("tcga-a1-a0sb-01");
        var filteredSamples2 = studyViewMapper.getFilteredSamples(StudyViewFilterHelper.build(studyViewFilter, null, Arrays.asList(customSampleIdentifier), studyViewFilter.getStudyIds()));
        assertEquals(1, filteredSamples2.size());
    }

    @Test
    public void getSamplesFilteredByClinicalData() {
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(Arrays.asList(STUDY_GENIE_PUB, STUDY_ACC_TCGA));

        // samples of patients with AGE <= 20.0
        studyViewFilter.setClinicalDataFilters(
            List.of(
                newClinicalDataFilter(
                    "age", List.of(
                        newDataFilterValue(null, 20.0, null)
                    )
                )
            )
        );
        var filteredSamples1 = studyViewMapper.getFilteredSamples(StudyViewFilterHelper.build(studyViewFilter, null, new ArrayList<>(), studyViewFilter.getStudyIds()));
        assertEquals(4, filteredSamples1.size());

        // samples of patients with AGE <= 20.0 or (80.0, 82.0]
        studyViewFilter.setClinicalDataFilters(
            List.of(
                newClinicalDataFilter(
                    "age", List.of(
                        newDataFilterValue(null, 20.0, null),
                        newDataFilterValue(80.0, 82.0, null)
                    )
                )
            )
        );
        var filteredSamples2 = studyViewMapper.getFilteredSamples(StudyViewFilterHelper.build(studyViewFilter, null, new ArrayList<>(), studyViewFilter.getStudyIds()));
        assertEquals(6, filteredSamples2.size());

        // samples of patients with UNKNOWN AGE 
        studyViewFilter.setClinicalDataFilters(
            List.of(
                newClinicalDataFilter(
                    "age", List.of(
                        newDataFilterValue(null, null, "Unknown")
                    )
                )
            )
        );
        var filteredSamples3 = studyViewMapper.getFilteredSamples(StudyViewFilterHelper.build(studyViewFilter, null, new ArrayList<>(), studyViewFilter.getStudyIds()));
        assertEquals(1, filteredSamples3.size());

        // samples of patients with AGE <= 20.0 or (80.0, 82.0] or UNKNOWN
        // this is a mixed list of filters of both numerical and non-numerical values
        studyViewFilter.setClinicalDataFilters(
            List.of(
                newClinicalDataFilter(
                    "age", List.of(
                        newDataFilterValue(null, 20.0, null),
                        newDataFilterValue(80.0, 82.0, null),
                        newDataFilterValue(null, null, "unknown")
                    )
                )
            )
        );
        var filteredSamples4 = studyViewMapper.getFilteredSamples(StudyViewFilterHelper.build(studyViewFilter, null, new ArrayList<>(), studyViewFilter.getStudyIds()));
        assertEquals(7, filteredSamples4.size());
        
        // NA filter
        studyViewFilter.setClinicalDataFilters(
            List.of(
                newClinicalDataFilter(
                    "age", List.of(
                        newDataFilterValue(null, null, "NA")
                    )
                )
            )
        );
        var filteredSamples5 = studyViewMapper.getFilteredSamples(StudyViewFilterHelper.build(studyViewFilter, null, new ArrayList<>(), studyViewFilter.getStudyIds()));
        // 4 acc_tcga + 7 study_genie_pub samples with "NA" AGE data or no AGE data 
        assertEquals(11, filteredSamples5.size());
        
        // NA + UNKNOWN
        studyViewFilter.setClinicalDataFilters(
            List.of(
                newClinicalDataFilter(
                    "age", List.of(
                        newDataFilterValue(null, null, "NA"),
                        newDataFilterValue(null, null, "UNKNOWN")
                    )
                )
            )
        );
        var filteredSamples6 = studyViewMapper.getFilteredSamples(StudyViewFilterHelper.build(studyViewFilter, null, new ArrayList<>(), studyViewFilter.getStudyIds()));
        // 11 NA + 1 UNKNOWN
        assertEquals(12, filteredSamples6.size());
    }
    
    private DataFilterValue newDataFilterValue(Double start, Double end, String value) {
        DataFilterValue dataFilterValue = new DataFilterValue();
        
        dataFilterValue.setStart(start == null ? null : new BigDecimal(start));
        dataFilterValue.setEnd(end == null ? null: new BigDecimal(end));
        dataFilterValue.setValue(value);
        
        return dataFilterValue;
    }

    private ClinicalDataFilter newClinicalDataFilter(String attributeId, List<DataFilterValue> values) {
        ClinicalDataFilter clinicalDataFilter = new ClinicalDataFilter();
        
        clinicalDataFilter.setAttributeId(attributeId);
        clinicalDataFilter.setValues(values);
        
        return clinicalDataFilter;
    }
}
