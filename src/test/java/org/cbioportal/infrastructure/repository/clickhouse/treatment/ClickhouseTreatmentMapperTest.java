package org.cbioportal.infrastructure.repository.clickhouse.treatment;

import static org.junit.Assert.assertEquals;

import java.util.List;
import org.cbioportal.domain.studyview.StudyViewFilterFactory;
import org.cbioportal.infrastructure.repository.clickhouse.AbstractTestcontainers;
import org.cbioportal.infrastructure.repository.clickhouse.config.MyBatisConfig;
import org.cbioportal.legacy.model.TemporalRelation;
import org.cbioportal.legacy.web.parameter.StudyViewFilter;
import org.cbioportal.legacy.web.parameter.filter.AndedPatientTreatmentFilters;
import org.cbioportal.legacy.web.parameter.filter.AndedSampleTreatmentFilters;
import org.cbioportal.legacy.web.parameter.filter.OredPatientTreatmentFilters;
import org.cbioportal.legacy.web.parameter.filter.OredSampleTreatmentFilters;
import org.cbioportal.legacy.web.parameter.filter.PatientTreatmentFilter;
import org.cbioportal.legacy.web.parameter.filter.SampleTreatmentFilter;
import org.cbioportal.shared.enums.ProjectionType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@Import(MyBatisConfig.class)
@DataJpaTest
@DirtiesContext
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = AbstractTestcontainers.Initializer.class)
public class ClickhouseTreatmentMapperTest {
  private static final String STUDY_TCGA_PUB = "study_tcga_pub";

  @Autowired private ClickhouseTreatmentMapper mapper;

  @Test
  public void getPatientTreatments() {

    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB));

    var patientTreatmentCounts =
        mapper.getPatientTreatmentCounts(
            StudyViewFilterFactory.make(
                studyViewFilter, null, studyViewFilter.getStudyIds(), null));

    var patientTreatments =
        mapper.getPatientTreatments(
            StudyViewFilterFactory.make(
                studyViewFilter, null, studyViewFilter.getStudyIds(), null));

    assertEquals(1, patientTreatmentCounts);
    assertEquals("madeupanib", patientTreatments.getFirst().treatment());

    PatientTreatmentFilter filter = new PatientTreatmentFilter();
    filter.setTreatment("madeupanib");

    OredPatientTreatmentFilters oredPatientTreatmentFilters = new OredPatientTreatmentFilters();
    oredPatientTreatmentFilters.setFilters(List.of(filter));

    AndedPatientTreatmentFilters andedPatientTreatmentFilters = new AndedPatientTreatmentFilters();
    andedPatientTreatmentFilters.setFilters(List.of(oredPatientTreatmentFilters));
    studyViewFilter.setPatientTreatmentFilters(andedPatientTreatmentFilters);

    patientTreatmentCounts =
        mapper.getPatientTreatmentCounts(
            StudyViewFilterFactory.make(
                studyViewFilter, null, studyViewFilter.getStudyIds(), null));

    patientTreatments =
        mapper.getPatientTreatments(
            StudyViewFilterFactory.make(
                studyViewFilter, null, studyViewFilter.getStudyIds(), null));

    assertEquals(1, patientTreatmentCounts);
    assertEquals("madeupanib", patientTreatments.getFirst().treatment());
  }

  @Test
  public void getTotalSampleTreatmentCounts() {
    // sample treatment counts without sample treatment filters

    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB));

    var totalSampleTreatmentCount =
        mapper.getTotalSampleTreatmentCounts(
            StudyViewFilterFactory.make(
                studyViewFilter, null, studyViewFilter.getStudyIds(), null));

    var sampleTreatmentCounts =
        mapper.getSampleTreatmentCounts(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            ProjectionType.SUMMARY.name());

    var sampleTreatmentCountsWithSampleLists =
        mapper.getSampleTreatmentCounts(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            ProjectionType.DETAILED.name());

    assertEquals(1, totalSampleTreatmentCount);
    assertEquals("madeupanib", sampleTreatmentCounts.getFirst().treatment());
    assertEquals(1, sampleTreatmentCounts.getFirst().postSampleCount());
    assertEquals(0, sampleTreatmentCounts.getFirst().preSampleCount());
    assertEquals(1, sampleTreatmentCountsWithSampleLists.getFirst().postSampleCount());
    assertEquals(0, sampleTreatmentCountsWithSampleLists.getFirst().preSampleCount());
    assertEquals(1, sampleTreatmentCountsWithSampleLists.getFirst().postSamples().size());
    assertEquals(0, sampleTreatmentCountsWithSampleLists.getFirst().preSamples().size());

    assertEquals(
        "tcga-a1-a0sd-01",
        sampleTreatmentCountsWithSampleLists.getFirst().postSamples().getFirst().getSampleId());
    assertEquals(
        "tcga-a1-a0sd",
        sampleTreatmentCountsWithSampleLists.getFirst().postSamples().getFirst().getPatientId());
    assertEquals(
        "study_tcga_pub",
        sampleTreatmentCountsWithSampleLists.getFirst().postSamples().getFirst().getStudyId());
    assertEquals(
        233,
        sampleTreatmentCountsWithSampleLists
            .getFirst()
            .postSamples()
            .getFirst()
            .getTimeTaken()
            .intValue());

    // sample treatment counts with sample treatment filters

    // PRE treatment test
    SampleTreatmentFilter filter = new SampleTreatmentFilter();
    filter.setTreatment("madeupanib");
    filter.setTime(TemporalRelation.Pre);

    OredSampleTreatmentFilters oredSampleTreatmentFilters = new OredSampleTreatmentFilters();
    oredSampleTreatmentFilters.setFilters(List.of(filter));

    AndedSampleTreatmentFilters andedSampleTreatmentFilters = new AndedSampleTreatmentFilters();
    andedSampleTreatmentFilters.setFilters(List.of(oredSampleTreatmentFilters));
    studyViewFilter.setSampleTreatmentFilters(andedSampleTreatmentFilters);

    totalSampleTreatmentCount =
        mapper.getTotalSampleTreatmentCounts(
            StudyViewFilterFactory.make(
                studyViewFilter, null, studyViewFilter.getStudyIds(), null));

    sampleTreatmentCounts =
        mapper.getSampleTreatmentCounts(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            ProjectionType.SUMMARY.name());

    sampleTreatmentCountsWithSampleLists =
        mapper.getSampleTreatmentCounts(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            ProjectionType.DETAILED.name());

    assertEquals(0, totalSampleTreatmentCount);
    assertEquals(0, sampleTreatmentCounts.size());
    assertEquals(0, sampleTreatmentCountsWithSampleLists.size());

    // POST treatment test
    filter.setTime(TemporalRelation.Post);

    totalSampleTreatmentCount =
        mapper.getTotalSampleTreatmentCounts(
            StudyViewFilterFactory.make(
                studyViewFilter, null, studyViewFilter.getStudyIds(), null));

    sampleTreatmentCounts =
        mapper.getSampleTreatmentCounts(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            ProjectionType.SUMMARY.name());

    sampleTreatmentCountsWithSampleLists =
        mapper.getSampleTreatmentCounts(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            ProjectionType.DETAILED.name());

    assertEquals(1, totalSampleTreatmentCount);
    assertEquals(1, sampleTreatmentCounts.size());
    assertEquals(1, sampleTreatmentCountsWithSampleLists.size());
    assertEquals("madeupanib", sampleTreatmentCounts.getFirst().treatment());
    assertEquals(1, sampleTreatmentCounts.getFirst().postSampleCount());
    assertEquals(0, sampleTreatmentCounts.getFirst().preSampleCount());
    assertEquals(1, sampleTreatmentCountsWithSampleLists.getFirst().postSampleCount());
    assertEquals(0, sampleTreatmentCountsWithSampleLists.getFirst().preSampleCount());
    assertEquals(1, sampleTreatmentCountsWithSampleLists.getFirst().postSamples().size());
    assertEquals(0, sampleTreatmentCountsWithSampleLists.getFirst().preSamples().size());
  }
}
