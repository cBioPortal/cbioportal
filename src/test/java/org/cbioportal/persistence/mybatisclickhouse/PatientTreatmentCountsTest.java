package org.cbioportal.persistence.mybatisclickhouse;

import org.cbioportal.persistence.helper.StudyViewFilterHelper;
import org.cbioportal.persistence.mybatisclickhouse.config.MyBatisConfig;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.cbioportal.web.parameter.filter.AndedPatientTreatmentFilters;
import org.cbioportal.web.parameter.filter.OredPatientTreatmentFilters;
import org.cbioportal.web.parameter.filter.PatientTreatmentFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@Import(MyBatisConfig.class)
@DataJpaTest
@DirtiesContext
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = AbstractTestcontainers.Initializer.class)
public class PatientTreatmentCountsTest extends AbstractTestcontainers {

    private static final String STUDY_TCGA_PUB = "study_tcga_pub";

    @Autowired
    private StudyViewMapper studyViewMapper;

    @Test
    public void getPatientTreatmentReportCounts() {
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB));


        var patientTreatmentCounts = studyViewMapper.getPatientTreatmentCounts(StudyViewFilterHelper.build(studyViewFilter, null, null, studyViewFilter.getStudyIds()));

        var patientTreatments = studyViewMapper.getPatientTreatments(StudyViewFilterHelper.build(studyViewFilter, null, null, studyViewFilter.getStudyIds()));

        assertEquals(1, patientTreatmentCounts);
        assertEquals("madeupanib", patientTreatments.getFirst().treatment());

        PatientTreatmentFilter filter = new PatientTreatmentFilter();
        filter.setTreatment("madeupanib");

        OredPatientTreatmentFilters oredPatientTreatmentFilters = new OredPatientTreatmentFilters();
        oredPatientTreatmentFilters.setFilters(List.of(filter));

        AndedPatientTreatmentFilters andedPatientTreatmentFilters = new AndedPatientTreatmentFilters();
        andedPatientTreatmentFilters.setFilters(List.of(oredPatientTreatmentFilters));
        studyViewFilter.setPatientTreatmentFilters(andedPatientTreatmentFilters);

        patientTreatmentCounts = studyViewMapper.getPatientTreatmentCounts(StudyViewFilterHelper.build(studyViewFilter, null, null, studyViewFilter.getStudyIds()));

        patientTreatments = studyViewMapper.getPatientTreatments(StudyViewFilterHelper.build(studyViewFilter, null, null, studyViewFilter.getStudyIds()));

        assertEquals(1, patientTreatmentCounts);
        assertEquals("madeupanib", patientTreatments.getFirst().treatment());

    }
}
