package org.cbioportal.infrastructure.repository.clickhouse.clinical_event;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.domain.studyview.StudyViewFilterFactory;
import org.cbioportal.infrastructure.repository.clickhouse.AbstractTestcontainers;
import org.cbioportal.infrastructure.repository.clickhouse.config.MyBatisConfig;
import org.cbioportal.legacy.model.ClinicalEvent;
import org.cbioportal.legacy.model.ClinicalEventData;
import org.cbioportal.legacy.web.parameter.DataFilter;
import org.cbioportal.legacy.web.parameter.DataFilterValue;
import org.cbioportal.legacy.web.parameter.StudyViewFilter;
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
public class ClickhouseClinicalEventMapperTest {
  private static final String STUDY_TCGA_PUB = "study_tcga_pub";
  private static final String PATIENT_ID = "TCGA-A1-A0SB";

  @Autowired private ClickhouseClinicalEventMapper mapper;

  @Test
  public void getClinicalEventTypeCounts() {
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB));

    StudyViewFilterContext studyViewFilterContext =
        StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null);

    var clinicalEventTypeCounts = mapper.getClinicalEventTypeCounts(studyViewFilterContext);

    assertEquals(4, clinicalEventTypeCounts.size());

    var clinicalEventTypeCountOptional =
        clinicalEventTypeCounts.stream()
            .filter(ce -> ce.getEventType().equals("Treatment"))
            .findFirst();

    assertTrue(clinicalEventTypeCountOptional.isPresent());
    assertEquals(1, clinicalEventTypeCountOptional.get().getCount().intValue());

    DataFilter dataFilter = new DataFilter();
    DataFilterValue dataFilterValue = new DataFilterValue();
    dataFilterValue.setValue("Treatment");
    dataFilter.setValues(List.of(dataFilterValue));
    studyViewFilter.setClinicalEventFilters(List.of(dataFilter));

    clinicalEventTypeCounts =
        mapper.getClinicalEventTypeCounts(
            StudyViewFilterFactory.make(
                studyViewFilter, null, studyViewFilter.getStudyIds(), null));

    assertEquals(3, clinicalEventTypeCounts.size());

    clinicalEventTypeCountOptional =
        clinicalEventTypeCounts.stream()
            .filter(ce -> ce.getEventType().equals("status"))
            .findFirst();

    assertFalse(clinicalEventTypeCountOptional.isPresent());
  }

  @Test
  public void getPatientClinicalEventsForPatientWithOneEvent() {
    // Patient tcga-a1-a0sb (patient_id=1) has 1 clinical event: status
    List<ClinicalEvent> result = mapper.getPatientClinicalEvents(STUDY_TCGA_PUB, "tcga-a1-a0sb");

    assertEquals(1, result.size());

    ClinicalEvent statusEvent = result.get(0);
    assertEquals("status", statusEvent.getEventType());
    assertEquals(STUDY_TCGA_PUB, statusEvent.getStudyId());
    assertEquals("tcga-a1-a0sb", statusEvent.getPatientId());
    assertEquals((Integer) 123, statusEvent.getStartDate());
    assertEquals((Integer) 0, statusEvent.getStopDate());

    // Verify attributes: status=radiographic_progression, SAMPLE_ID=tcga-a1-a0sb-01
    List<ClinicalEventData> attrs = statusEvent.getAttributes();
    assertEquals(2, attrs.size());
    assertTrue(
        attrs.stream()
            .anyMatch(
                a ->
                    "status".equals(a.getKey())
                        && "radiographic_progression".equals(a.getValue())));
    assertTrue(
        attrs.stream()
            .anyMatch(
                a -> "SAMPLE_ID".equals(a.getKey()) && "tcga-a1-a0sb-01".equals(a.getValue())));
  }

  @Test
  public void getPatientClinicalEventsForPatientWithMultipleEvents() {
    // Patient tcga-a1-a0sd (patient_id=2) has 3 clinical events: SPECIMEN, Treatment, Seqencing
    List<ClinicalEvent> result = mapper.getPatientClinicalEvents(STUDY_TCGA_PUB, "tcga-a1-a0sd");

    assertEquals(3, result.size());

    // Verify event types
    List<String> eventTypes =
        result.stream().map(ClinicalEvent::getEventType).sorted().collect(Collectors.toList());
    assertEquals(List.of("SPECIMEN", "Seqencing", "Treatment"), eventTypes);

    // Verify SPECIMEN event details
    Optional<ClinicalEvent> specimenEvent =
        result.stream().filter(e -> "SPECIMEN".equals(e.getEventType())).findFirst();
    assertTrue(specimenEvent.isPresent());
    assertEquals((Integer) 233, specimenEvent.get().getStartDate());
    assertEquals((Integer) 345, specimenEvent.get().getStopDate());
    assertEquals(2, specimenEvent.get().getAttributes().size());

    // Verify Treatment event has the most attributes (4)
    Optional<ClinicalEvent> treatmentEvent =
        result.stream().filter(e -> "Treatment".equals(e.getEventType())).findFirst();
    assertTrue(treatmentEvent.isPresent());
    assertEquals(4, treatmentEvent.get().getAttributes().size());
    assertTrue(
        treatmentEvent.get().getAttributes().stream()
            .anyMatch(a -> "AGENT".equals(a.getKey()) && "madeupanib".equals(a.getValue())));

    // Verify total attributes across all events: 2 + 4 + 1 = 7
    int totalAttributes = result.stream().mapToInt(e -> e.getAttributes().size()).sum();
    assertEquals(7, totalAttributes);
  }

  @Test
  public void getPatientClinicalEventsReturnsEmptyForNonexistentPatient() {
    List<ClinicalEvent> result =
        mapper.getPatientClinicalEvents(STUDY_TCGA_PUB, "nonexistent-patient");
    assertEquals(0, result.size());
  }
}
