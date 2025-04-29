package org.cbioportal.application.file.export;

import org.cbioportal.application.file.export.exporters.ClinicalTimelineDataTypeExporter;
import org.cbioportal.application.file.export.exporters.ExportDetails;
import org.cbioportal.application.file.export.services.ClinicalAttributeDataService;
import org.cbioportal.application.file.model.ClinicalEvent;
import org.cbioportal.application.file.model.ClinicalEventData;
import org.cbioportal.application.file.utils.CloseableIterator;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class ClinicalTimelineDataTypeExporterTests {

    ClinicalAttributeDataService clinicalDataAttributeDataService = new ClinicalAttributeDataService(null) {
        @Override
        public boolean hasClinicalTimelineData(String studyId) {
            return true;
        }

        @Override
        public List<String> getDistinctClinicalEventKeys(String studyId) {
            return List.of("KEY1", "KEY2");
        }

        @Override
        public CloseableIterator<ClinicalEvent> getClinicalEvents(String studyId) {
            return CloseableIterator.empty();
        }

        @Override
        public CloseableIterator<ClinicalEventData> getClinicalEventData(String studyId) {
            return CloseableIterator.empty();
        }
    };

    @Test
    public void testNoClinicalEvents() {
        var factory = new InMemoryFileWriterFactory();

        ClinicalTimelineDataTypeExporter exporter = new ClinicalTimelineDataTypeExporter(new ClinicalAttributeDataService(null) {
            @Override
            public boolean hasClinicalTimelineData(String studyId) {
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

        ClinicalTimelineDataTypeExporter exporter = new ClinicalTimelineDataTypeExporter(new ClinicalAttributeDataService(null) {
            @Override
            public CloseableIterator<ClinicalEvent> getClinicalEvents(String studyId) {
                ClinicalEvent event1 = new ClinicalEvent();
                event1.setClinicalEventId(1);
                event1.setPatientId("PATIENT_1");
                ClinicalEvent event3 = new ClinicalEvent();
                event3.setClinicalEventId(3);
                event3.setPatientId("PATIENT_2");
                return new SimpleCloseableIterator<>(List.of(event1, event3));
            }

            @Override
            public CloseableIterator<ClinicalEventData> getClinicalEventData(String studyId) {
                ClinicalEventData eventData = new ClinicalEventData();
                eventData.setClinicalEventId(2); // Mismatched ID
                eventData.setKey("KEY1");
                eventData.setValue("VALUE1");
                return new SimpleCloseableIterator<>(List.of(eventData));
            }

            @Override
            public boolean hasClinicalTimelineData(String studyId) {
                return true;
            }

            @Override
            public List<String> getDistinctClinicalEventKeys(String studyId) {
                return List.of("KEY1");
            }
        });

        RuntimeException exception = assertThrows(RuntimeException.class, () -> exporter.exportData(factory, new ExportDetails("TEST_STUDY_ID")));
        assertThat(exception.getMessage(), containsString("Clinical event IDs are not matching"));
    }

    @Test
    public void testExport() {
        var factory = new InMemoryFileWriterFactory();

        ClinicalTimelineDataTypeExporter exporter = new ClinicalTimelineDataTypeExporter(new ClinicalAttributeDataService(null) {
            @Override
            public CloseableIterator<ClinicalEvent> getClinicalEvents(String studyId) {
                ClinicalEvent event1 = new ClinicalEvent();
                event1.setClinicalEventId(1);
                event1.setPatientId("PATIENT_1");
                event1.setEventType("TYPE_1");

                ClinicalEvent event2 = new ClinicalEvent();
                event2.setClinicalEventId(2);
                event2.setPatientId("PATIENT_2");
                event2.setEventType("TYPE_2");

                return new SimpleCloseableIterator<>(List.of(event1, event2));
            }

            @Override
            public CloseableIterator<ClinicalEventData> getClinicalEventData(String studyId) {
                ClinicalEventData eventData1 = new ClinicalEventData();
                eventData1.setClinicalEventId(1);
                eventData1.setKey("KEY1");
                eventData1.setValue("VALUE1");

                ClinicalEventData eventData2 = new ClinicalEventData();
                eventData2.setClinicalEventId(2);
                eventData2.setKey("KEY2");
                eventData2.setValue("VALUE2");

                return new SimpleCloseableIterator<>(List.of(eventData1, eventData2));
            }

            @Override
            public boolean hasClinicalTimelineData(String studyId) {
                return true;
            }

            @Override
            public List<String> getDistinctClinicalEventKeys(String studyId) {
                return List.of("KEY1", "KEY2");
            }
        });

        boolean exported = exporter.exportData(factory, new ExportDetails("TEST_STUDY_ID"));

        assertTrue(exported);
        var fileContents = factory.getFileContents();
        assertEquals(Set.of("meta_clinical_timeline.txt", "data_clinical_timeline.txt"), fileContents.keySet());

        assertEquals("cancer_study_identifier: TEST_STUDY_ID\n"
            + "genetic_alteration_type: CLINICAL\n"
            + "datatype: TIMELINE\n"
            + "data_filename: data_clinical_timeline.txt\n", fileContents.get("meta_clinical_timeline.txt").toString());

        assertEquals("""
            PATIENT_ID\tSTART_DATE\tSTOP_DATE\tEVENT_TYPE\tKEY1\tKEY2
            PATIENT_1\t\t\tTYPE_1\tVALUE1\t
            PATIENT_2\t\t\tTYPE_2\t\tVALUE2
            """, fileContents.get("data_clinical_timeline.txt").toString());
    }

    @Test
    public void testClinicalEventDataKeyIsNull() {
        var factory = new InMemoryFileWriterFactory();

        ClinicalTimelineDataTypeExporter exporter = new ClinicalTimelineDataTypeExporter(new ClinicalAttributeDataService(null) {
            @Override
            public CloseableIterator<ClinicalEvent> getClinicalEvents(String studyId) {
                ClinicalEvent event = new ClinicalEvent();
                event.setClinicalEventId(1);
                event.setPatientId("PATIENT_1");
                return new SimpleCloseableIterator<>(List.of(event));
            }

            @Override
            public CloseableIterator<ClinicalEventData> getClinicalEventData(String studyId) {
                ClinicalEventData eventData = new ClinicalEventData();
                eventData.setClinicalEventId(1);
                eventData.setKey(null); // Null key to trigger the exception
                eventData.setValue("VALUE1");
                return new SimpleCloseableIterator<>(List.of(eventData));
            }

            @Override
            public boolean hasClinicalTimelineData(String studyId) {
                return true;
            }

            @Override
            public List<String> getDistinctClinicalEventKeys(String studyId) {
                return List.of("KEY1");
            }
        });

        RuntimeException exception = assertThrows(RuntimeException.class, () -> exporter.exportData(factory, new ExportDetails("TEST_STUDY_ID")));
        assertThat(exception.getMessage(), containsString("Clinical event data key is null"));
    }
}