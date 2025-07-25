package org.cbioportal.application.file.export;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Set;
import org.cbioportal.application.file.export.exporters.ClinicalTimelineDataTypeExporter;
import org.cbioportal.application.file.export.exporters.ExportDetails;
import org.cbioportal.application.file.export.services.ClinicalAttributeDataService;
import org.cbioportal.application.file.model.ClinicalEvent;
import org.cbioportal.application.file.model.ClinicalEventData;
import org.cbioportal.application.file.utils.CloseableIterator;
import org.junit.Test;

public class ClinicalTimelineDataTypeExporterTests {

  @Test
  public void testNoClinicalEvents() {
    var factory = new InMemoryFileWriterFactory();

    ClinicalTimelineDataTypeExporter exporter =
        new ClinicalTimelineDataTypeExporter(
            new ClinicalAttributeDataService(null) {
              @Override
              public boolean hasClinicalTimelineData(String studyId, Set<String> sampleIds) {
                return false;
              }
            });

    boolean exported = exporter.exportData(factory, new ExportDetails("TEST_STUDY_ID"));

    assertFalse(exported);
    assertTrue(factory.getFileContents().isEmpty());
  }

  @Test
  public void testMismatchedEventData() {
    var factory = new InMemoryFileWriterFactory();

    ClinicalTimelineDataTypeExporter exporter =
        new ClinicalTimelineDataTypeExporter(
            new ClinicalAttributeDataService(null) {
              @Override
              public CloseableIterator<ClinicalEvent> getClinicalEvents(
                  String studyId, String eventType, Set<String> sampleIds) {
                ClinicalEvent event1 = new ClinicalEvent();
                event1.setClinicalEventId(1);
                event1.setPatientId("PATIENT_1");
                ClinicalEvent event3 = new ClinicalEvent();
                event3.setClinicalEventId(3);
                event3.setPatientId("PATIENT_2");
                return new SimpleCloseableIterator<>(List.of(event1, event3));
              }

              @Override
              public CloseableIterator<ClinicalEventData> getClinicalEventData(
                  String studyId, String eventType, Set<String> sampleIds) {
                ClinicalEventData eventData = new ClinicalEventData();
                eventData.setClinicalEventId(2); // Mismatched ID
                eventData.setKey("KEY1");
                eventData.setValue("VALUE1");
                return new SimpleCloseableIterator<>(List.of(eventData));
              }

              @Override
              public boolean hasClinicalTimelineData(String studyId, Set<String> sampleIds) {
                return true;
              }

              @Override
              public List<String> getDistinctEventTypes(String studyId) {
                return List.of("TYPE_1");
              }

              @Override
              public List<String> getDistinctClinicalEventKeys(String studyId, String eventType) {
                return List.of("KEY1");
              }
            });

    ExportDetails exportDetails = new ExportDetails("TEST_STUDY_ID");

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> exporter.exportData(factory, exportDetails));
    assertThat(
        exception.getCause().getMessage(), containsString("Clinical event IDs are not matching"));
  }

  @Test
  public void testExport() {
    var factory = new InMemoryFileWriterFactory();
    ClinicalAttributeDataService clinicalDataAttributeDataService =
        mock(ClinicalAttributeDataService.class);
    doReturn(true)
        .when(clinicalDataAttributeDataService)
        .hasClinicalTimelineData("TEST_STUDY_ID", null);
    doReturn(List.of("TYPE_1", "TYPE_2"))
        .when(clinicalDataAttributeDataService)
        .getDistinctEventTypes("TEST_STUDY_ID");
    doReturn(List.of("KEY1"))
        .when(clinicalDataAttributeDataService)
        .getDistinctClinicalEventKeys("TEST_STUDY_ID", "TYPE_1");
    doReturn(List.of("STYLE_COLOR", "STYLE_SHAPE"))
        .when(clinicalDataAttributeDataService)
        .getDistinctClinicalEventKeys("TEST_STUDY_ID", "TYPE_2");

    ClinicalEvent event1 = new ClinicalEvent();
    event1.setClinicalEventId(1);
    event1.setPatientId("PATIENT_1");
    event1.setStartDate(100);
    event1.setStopDate(150);
    event1.setEventType("TYPE_1");

    ClinicalEvent event2 = new ClinicalEvent();
    event2.setClinicalEventId(2);
    event2.setPatientId("PATIENT_2");
    event2.setStartDate(200);
    event2.setStopDate(250);
    event2.setEventType("TYPE_1");

    var events1 = new SimpleCloseableIterator<>(List.of(event1, event2));
    doReturn(events1)
        .when(clinicalDataAttributeDataService)
        .getClinicalEvents("TEST_STUDY_ID", "TYPE_1", null);

    ClinicalEventData eventData1 = new ClinicalEventData();
    eventData1.setClinicalEventId(1);
    eventData1.setKey("KEY1");
    eventData1.setValue("VALUE1");

    ClinicalEventData eventData2 = new ClinicalEventData();
    eventData2.setClinicalEventId(2);
    eventData2.setKey("KEY1");
    eventData2.setValue("VALUE2");

    var eventDataPoints1 = new SimpleCloseableIterator<>(List.of(eventData1, eventData2));
    doReturn(eventDataPoints1)
        .when(clinicalDataAttributeDataService)
        .getClinicalEventData("TEST_STUDY_ID", "TYPE_1", null);

    ClinicalEvent event3 = new ClinicalEvent();
    event3.setClinicalEventId(3);
    event3.setPatientId("PATIENT_3");
    event3.setStartDate(300);
    event3.setStopDate(350);
    event3.setEventType("TYPE_2");

    ClinicalEvent event4 = new ClinicalEvent();
    event4.setClinicalEventId(4);
    event4.setPatientId("PATIENT_4");
    event4.setStartDate(400);
    event4.setStopDate(480);
    event4.setEventType("TYPE_2");

    var events2 = new SimpleCloseableIterator<>(List.of(event3, event4));
    doReturn(events2)
        .when(clinicalDataAttributeDataService)
        .getClinicalEvents("TEST_STUDY_ID", "TYPE_2", null);

    ClinicalEventData eventData3 = new ClinicalEventData();
    eventData3.setClinicalEventId(3);
    eventData3.setKey("STYLE_COLOR");
    eventData3.setValue("#ff0000");

    ClinicalEventData eventData4 = new ClinicalEventData();
    eventData4.setClinicalEventId(4);
    eventData4.setKey("STYLE_SHAPE");
    eventData4.setValue("square");

    var eventDataPoints2 = new SimpleCloseableIterator<>(List.of(eventData3, eventData4));
    doReturn(eventDataPoints2)
        .when(clinicalDataAttributeDataService)
        .getClinicalEventData("TEST_STUDY_ID", "TYPE_2", null);

    ClinicalTimelineDataTypeExporter exporter =
        new ClinicalTimelineDataTypeExporter(clinicalDataAttributeDataService);

    boolean exported = exporter.exportData(factory, new ExportDetails("TEST_STUDY_ID"));

    assertTrue(exported);
    var fileContents = factory.getFileContents();
    assertEquals(
        Set.of(
            "meta_clinical_timeline_TYPE_1.txt",
            "data_clinical_timeline_TYPE_1.txt",
            "meta_clinical_timeline_TYPE_2.txt",
            "data_clinical_timeline_TYPE_2.txt"),
        fileContents.keySet());

    assertEquals(
        """
            cancer_study_identifier: TEST_STUDY_ID
            genetic_alteration_type: CLINICAL
            datatype: TIMELINE
            data_filename: data_clinical_timeline_TYPE_1.txt
            """,
        fileContents.get("meta_clinical_timeline_TYPE_1.txt").toString());

    assertEquals(
        """
            PATIENT_ID\tSTART_DATE\tSTOP_DATE\tEVENT_TYPE\tKEY1
            PATIENT_1\t100\t150\tTYPE_1\tVALUE1
            PATIENT_2\t200\t250\tTYPE_1\tVALUE2
            """,
        fileContents.get("data_clinical_timeline_TYPE_1.txt").toString());

    assertEquals(
        """
            cancer_study_identifier: TEST_STUDY_ID
            genetic_alteration_type: CLINICAL
            datatype: TIMELINE
            data_filename: data_clinical_timeline_TYPE_2.txt
            """,
        fileContents.get("meta_clinical_timeline_TYPE_2.txt").toString());

    assertEquals(
        """
            PATIENT_ID\tSTART_DATE\tSTOP_DATE\tEVENT_TYPE\tSTYLE_COLOR\tSTYLE_SHAPE
            PATIENT_3\t300\t350\tTYPE_2\t#ff0000\tcircle
            PATIENT_4\t400\t480\tTYPE_2\t#1f77b4\tsquare
            """,
        fileContents.get("data_clinical_timeline_TYPE_2.txt").toString());
  }

  @Test
  public void testClinicalEventDataKeyIsNull() {
    var factory = new InMemoryFileWriterFactory();

    ClinicalTimelineDataTypeExporter exporter =
        new ClinicalTimelineDataTypeExporter(
            new ClinicalAttributeDataService(null) {
              @Override
              public CloseableIterator<ClinicalEvent> getClinicalEvents(
                  String studyId, String eventType, Set<String> sampleIds) {
                ClinicalEvent event = new ClinicalEvent();
                event.setClinicalEventId(1);
                event.setPatientId("PATIENT_1");
                return new SimpleCloseableIterator<>(List.of(event));
              }

              @Override
              public CloseableIterator<ClinicalEventData> getClinicalEventData(
                  String studyId, String eventType, Set<String> sampleIds) {
                ClinicalEventData eventData = new ClinicalEventData();
                eventData.setClinicalEventId(1);
                eventData.setKey(null); // Null key to trigger the exception
                eventData.setValue("VALUE1");
                return new SimpleCloseableIterator<>(List.of(eventData));
              }

              @Override
              public boolean hasClinicalTimelineData(String studyId, Set<String> sampleIds) {
                return true;
              }

              @Override
              public List<String> getDistinctEventTypes(String studyId) {
                return List.of("EVENT_TYPE_1");
              }

              @Override
              public List<String> getDistinctClinicalEventKeys(String studyId, String eventType) {
                return List.of("KEY1");
              }
            });
    ExportDetails exportDetails = new ExportDetails("TEST_STUDY_ID");

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> exporter.exportData(factory, exportDetails));
    assertThat(exception.getMessage(), containsString("Clinical event data key is null"));
  }
}
