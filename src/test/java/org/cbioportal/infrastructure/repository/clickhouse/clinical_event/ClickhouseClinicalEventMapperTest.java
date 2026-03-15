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
import org.cbioportal.legacy.model.meta.BaseMeta;
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
  private static final String PATIENT_ID = "tcga-a1-a0sb";

  @Autowired private ClickhouseClinicalEventMapper mapper;

  @Test
  public void getClinicalEventTypeCounts() {
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB));

    StudyViewFilterContext studyViewFilterContext =
        StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null);

    var clinicalEventTypeCounts = mapper.getClinicalEventTypeCounts(studyViewFilterContext);

    assertEquals(5, clinicalEventTypeCounts.size());

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
  public void getPatientClinicalEventsForPatientWithAndWithoutAttributes() {
    // Patient tcga-a1-a0sb (patient_id=1) has 2 clinical events:
    //   - status (id=1) with 2 attributes
    //   - IMAGING (id=5) with no attributes
    List<ClinicalEvent> result =
        mapper.getPatientClinicalEvents(
            STUDY_TCGA_PUB, PATIENT_ID, "SUMMARY", null, null, null, null);

    assertEquals(2, result.size());

    // Verify status event has attributes
    Optional<ClinicalEvent> statusEvent =
        result.stream().filter(e -> "status".equals(e.getEventType())).findFirst();
    assertTrue(statusEvent.isPresent());
    assertEquals(STUDY_TCGA_PUB, statusEvent.get().getStudyId());
    assertEquals(PATIENT_ID, statusEvent.get().getPatientId());
    assertEquals((Integer) 123, statusEvent.get().getStartDate());
    assertEquals((Integer) 0, statusEvent.get().getStopDate());
    List<ClinicalEventData> attrs = statusEvent.get().getAttributes();
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

    // Verify IMAGING event has empty attributes list (not a list with a null entry)
    Optional<ClinicalEvent> imagingEvent =
        result.stream().filter(e -> "IMAGING".equals(e.getEventType())).findFirst();
    assertTrue(imagingEvent.isPresent());
    assertEquals((Integer) 500, imagingEvent.get().getStartDate());
    assertEquals((Integer) 600, imagingEvent.get().getStopDate());
    assertNotNull(imagingEvent.get().getAttributes());
    assertEquals(0, imagingEvent.get().getAttributes().size());
  }

  @Test
  public void getPatientClinicalEventsForPatientWithMultipleEvents() {
    // Patient tcga-a1-a0sd (patient_id=2) has 3 clinical events: SPECIMEN, Treatment, Seqencing
    List<ClinicalEvent> result =
        mapper.getPatientClinicalEvents(
            STUDY_TCGA_PUB, "tcga-a1-a0sd", "SUMMARY", null, null, null, null);

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
  public void getPatientClinicalEventsWithPagination() {
    // Patient tcga-a1-a0sd has 3 events; request only 2 (limit=2, offset=0)
    List<ClinicalEvent> result =
        mapper.getPatientClinicalEvents(
            STUDY_TCGA_PUB, "tcga-a1-a0sd", "SUMMARY", 2, 0, null, null);

    assertEquals(2, result.size());

    // Request page 2 (limit=2, offset=2) — should return 1 remaining event
    List<ClinicalEvent> page2 =
        mapper.getPatientClinicalEvents(
            STUDY_TCGA_PUB, "tcga-a1-a0sd", "SUMMARY", 2, 2, null, null);

    assertEquals(1, page2.size());
  }

  @Test
  public void getPatientClinicalEventsIdProjection() {
    List<ClinicalEvent> result =
        mapper.getPatientClinicalEventsIdProjection(STUDY_TCGA_PUB, PATIENT_ID, null, null);

    assertEquals(2, result.size());

    // ID projection should not include startDate, stopDate, or attributes
    ClinicalEvent event = result.get(0);
    assertNotNull(event.getEventType());
    assertNotNull(event.getPatientId());
    assertNotNull(event.getStudyId());
    assertNull(event.getStartDate());
    assertNull(event.getStopDate());
    assertTrue(event.getAttributes() == null || event.getAttributes().isEmpty());
  }

  @Test
  public void getPatientClinicalEventsReturnsEmptyForNonexistentPatient() {
    List<ClinicalEvent> result =
        mapper.getPatientClinicalEvents(
            STUDY_TCGA_PUB, "nonexistent-patient", "SUMMARY", null, null, null, null);
    assertEquals(0, result.size());
  }

  @Test
  public void getMetaPatientClinicalEventsReturnsTotalCount() {
    BaseMeta meta = mapper.getMetaPatientClinicalEvents(STUDY_TCGA_PUB, PATIENT_ID);
    assertNotNull(meta);
    assertEquals((Integer) 2, meta.getTotalCount());
  }

  @Test
  public void getMetaPatientClinicalEventsReturnsZeroForNonexistentPatient() {
    BaseMeta meta = mapper.getMetaPatientClinicalEvents(STUDY_TCGA_PUB, "nonexistent-patient");
    assertNotNull(meta);
    assertEquals((Integer) 0, meta.getTotalCount());
  }
}
