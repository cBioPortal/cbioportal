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

    @Autowired
    private StudyViewMapper studyViewMapper;

    @Test
    public void getFilteredSamples() {
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(Arrays.asList(STUDY_TCGA_PUB, STUDY_ACC_TCGA));
        var filteredSamples = studyViewMapper.getFilteredSamples(StudyViewFilterHelper.build(studyViewFilter, null, null));
        assertEquals(19, filteredSamples.size());

        ClinicalDataFilter customDataFilter = new ClinicalDataFilter();
        customDataFilter.setAttributeId("123");
        DataFilterValue value = new DataFilterValue();
        customDataFilter.setValues(List.of(value));
        studyViewFilter.setCustomDataFilters(List.of(customDataFilter));
        var filteredSamples1 = studyViewMapper.getFilteredSamples(StudyViewFilterHelper.build(studyViewFilter, null, new ArrayList<>()));
        assertEquals(0, filteredSamples1.size());

        CustomSampleIdentifier customSampleIdentifier = new CustomSampleIdentifier();
        customSampleIdentifier.setStudyId("acc_tcga");
        customSampleIdentifier.setSampleId("tcga-a1-a0sb-01");
        var filteredSamples2 = studyViewMapper.getFilteredSamples(StudyViewFilterHelper.build(studyViewFilter, null, Arrays.asList(customSampleIdentifier)));
        assertEquals(1, filteredSamples2.size());
    }
}
