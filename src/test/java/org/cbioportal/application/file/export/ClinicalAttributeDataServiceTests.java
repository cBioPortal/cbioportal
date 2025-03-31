package org.cbioportal.application.file.export;

import org.apache.ibatis.cursor.Cursor;
import org.cbioportal.application.file.export.mappers.ClinicalAttributeDataMapper;
import org.cbioportal.application.file.export.services.ClinicalAttributeDataService;
import org.cbioportal.application.file.model.ClinicalAttribute;
import org.cbioportal.application.file.model.ClinicalPatientAttributeValue;
import org.cbioportal.application.file.model.ClinicalSampleAttributeValue;
import org.cbioportal.application.file.utils.CloseableIterator;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.SequencedMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ClinicalAttributeDataServiceTests {

    ClinicalAttributeDataService clinicalDataAttributeDataService = new ClinicalAttributeDataService(new ClinicalAttributeDataMapper() {
        @Override
        public List<ClinicalAttribute> getClinicalSampleAttributes(String studyId) {
            return List.of(
                new ClinicalAttribute("test number sample displayName", "test number sample description", "NUMBER", "2", "TEST_NUMBER_SAMPLE_ATTRIBUTE_ID"),
                new ClinicalAttribute("test string sample displayName", "test string sample description", "STRING", "3", "TEST_STRING_SAMPLE_ATTRIBUTE_ID"));
        }

        @Override
        public Cursor<ClinicalSampleAttributeValue> getClinicalSampleAttributeValues(String studyId) {
            return new TestFakeCursor<>(
                new ClinicalSampleAttributeValue("TEST_PATIENT_ID_1", "TEST_SAMPLE_ID_1", "TEST_STRING_SAMPLE_ATTRIBUTE_ID", "A"),
                new ClinicalSampleAttributeValue("TEST_PATIENT_ID_1", "TEST_SAMPLE_ID_1", "TEST_NUMBER_SAMPLE_ATTRIBUTE_ID", "1"),
                new ClinicalSampleAttributeValue("TEST_PATIENT_ID_1", "TEST_SAMPLE_ID_2", "TEST_NUMBER_SAMPLE_ATTRIBUTE_ID", "2"),
                new ClinicalSampleAttributeValue("TEST_PATIENT_ID_2", "TEST_SAMPLE_ID_3", "TEST_STRING_SAMPLE_ATTRIBUTE_ID", "B"));
        }

        @Override
        public List<ClinicalAttribute> getClinicalPatientAttributes(String studyId) {
            return List.of(
                new ClinicalAttribute("test number patient displayName", "test number patient description", "NUMBER", "4", "TEST_NUMBER_PATIENT_ATTRIBUTE_ID"),
                new ClinicalAttribute("test string patient displayName", "test string patient description", "STRING", "5", "TEST_STRING_PATIENT_ATTRIBUTE_ID"));
        }

        @Override
        public Cursor<ClinicalPatientAttributeValue> getClinicalPatientAttributeValues(String studyId) {
            return new TestFakeCursor<>(
                new ClinicalPatientAttributeValue("TEST_PATIENT_ID_1", "TEST_STRING_PATIENT_ATTRIBUTE_ID", "C"),
                new ClinicalPatientAttributeValue("TEST_PATIENT_ID_1", "TEST_NUMBER_PATIENT_ATTRIBUTE_ID", "3"),
                new ClinicalPatientAttributeValue("TEST_PATIENT_ID_2", "TEST_STRING_PATIENT_ATTRIBUTE_ID", "D"));
        }
    });

    @Test
    public void testGetClinicalSampleAttributeData() {
        CloseableIterator<SequencedMap<ClinicalAttribute, String>> result = clinicalDataAttributeDataService.getClinicalSampleAttributeData("testStudyId");
        assertTrue(result.hasNext());
        SequencedMap<ClinicalAttribute, String> row1 = result.next();
        List<ClinicalAttribute> clinicalAttributes = new ArrayList<>(row1.keySet());
        assertEquals(4, clinicalAttributes.size());
        assertEquals(ClinicalAttribute.PATIENT_ID, clinicalAttributes.get(0));
        assertEquals(ClinicalAttribute.SAMPLE_ID, clinicalAttributes.get(1));
        ClinicalAttribute testNumberAttribute = clinicalAttributes.get(2);
        assertEquals("TEST_NUMBER_SAMPLE_ATTRIBUTE_ID", testNumberAttribute.getAttributeId());
        ClinicalAttribute testStringAttribute = clinicalAttributes.get(3);
        assertEquals("TEST_STRING_SAMPLE_ATTRIBUTE_ID", testStringAttribute.getAttributeId());

        assertTrue(result.hasNext());
        SequencedMap<ClinicalAttribute, String> row2 = result.next();
        assertTrue(result.hasNext());
        SequencedMap<ClinicalAttribute, String> row3 = result.next();

        assertEquals("TEST_PATIENT_ID_1", row1.get(ClinicalAttribute.PATIENT_ID));
        assertEquals("TEST_SAMPLE_ID_1", row1.get(ClinicalAttribute.SAMPLE_ID));
        assertEquals("1", row1.get(testNumberAttribute));
        assertEquals("A", row1.get(testStringAttribute));
        assertEquals("TEST_PATIENT_ID_1", row2.get(ClinicalAttribute.PATIENT_ID));
        assertEquals("TEST_SAMPLE_ID_2", row2.get(ClinicalAttribute.SAMPLE_ID));
        assertEquals("2", row2.get(testNumberAttribute));
        assertNull(row2.get(testStringAttribute));
        assertEquals("TEST_PATIENT_ID_2", row3.get(ClinicalAttribute.PATIENT_ID));
        assertEquals("TEST_SAMPLE_ID_3", row3.get(ClinicalAttribute.SAMPLE_ID));
        assertNull(row3.get(testNumberAttribute));
        assertEquals("B", row3.get(testStringAttribute));
    }

    @Test
    public void testGetClinicalPatientAttributeData() {
        CloseableIterator<SequencedMap<ClinicalAttribute, String>> result = clinicalDataAttributeDataService.getClinicalPatientAttributeData("testStudyId");

        assertTrue(result.hasNext());
        SequencedMap<ClinicalAttribute, String> row1 = result.next();
        List<ClinicalAttribute> clinicalAttributes = new ArrayList<>(row1.keySet());
        assertEquals(3, clinicalAttributes.size());
        assertEquals(ClinicalAttribute.PATIENT_ID, clinicalAttributes.get(0));
        ClinicalAttribute testNumberAttribute = clinicalAttributes.get(1);
        assertEquals("TEST_NUMBER_PATIENT_ATTRIBUTE_ID", testNumberAttribute.getAttributeId());
        ClinicalAttribute testStringAttribute = clinicalAttributes.get(2);
        assertEquals("TEST_STRING_PATIENT_ATTRIBUTE_ID", testStringAttribute.getAttributeId());

        assertTrue(result.hasNext());
        SequencedMap<ClinicalAttribute, String> row2 = result.next();
        assertEquals("TEST_PATIENT_ID_1", row1.get(ClinicalAttribute.PATIENT_ID));
        assertEquals("3", row1.get(testNumberAttribute));
        assertEquals("C", row1.get(testStringAttribute));
        assertEquals("TEST_PATIENT_ID_2", row2.get(ClinicalAttribute.PATIENT_ID));
        assertNull(row2.get(testNumberAttribute));
        assertEquals("D", row2.get(testStringAttribute));
    }
}
