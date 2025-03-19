package org.cbioportal.application.file.export;

import org.apache.commons.compress.utils.Lists;
import org.apache.ibatis.cursor.Cursor;
import org.cbioportal.application.file.export.mappers.ClinicalAttributeDataMapper;
import org.cbioportal.application.file.model.ClinicalAttribute;
import org.cbioportal.application.file.model.LongTable;
import org.junit.Test;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

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
        LongTable<ClinicalAttribute, String> result = clinicalDataAttributeDataService.getClinicalSampleAttributeData("testStudyId");
        List<ClinicalAttribute> clinicalAttributes = Lists.newArrayList(result.getColumns().iterator());
        assertEquals(4, clinicalAttributes.size());
        assertEquals(ClinicalAttribute.PATIENT_ID, clinicalAttributes.get(0));
        assertEquals(ClinicalAttribute.SAMPLE_ID, clinicalAttributes.get(1));
        ClinicalAttribute testNumberAttribute = clinicalAttributes.get(2);
        assertEquals("TEST_NUMBER_SAMPLE_ATTRIBUTE_ID", testNumberAttribute.getAttributeId());
        ClinicalAttribute testStringAttribute = clinicalAttributes.get(3);
        assertEquals("TEST_STRING_SAMPLE_ATTRIBUTE_ID", testStringAttribute.getAttributeId());

        List<Function<ClinicalAttribute, Optional<String>>> rows = Lists.newArrayList(result);
        assertEquals(3, rows.size());
        assertEquals(Optional.of("TEST_PATIENT_ID_1"), rows.get(0).apply(ClinicalAttribute.PATIENT_ID));
        assertEquals(Optional.of("TEST_SAMPLE_ID_1"), rows.get(0).apply(ClinicalAttribute.SAMPLE_ID));
        assertEquals(Optional.of("1"), rows.get(0).apply(testNumberAttribute));
        assertEquals(Optional.of("A"), rows.get(0).apply(testStringAttribute));
        assertEquals(Optional.of("TEST_PATIENT_ID_1"), rows.get(1).apply(ClinicalAttribute.PATIENT_ID));
        assertEquals(Optional.of("TEST_SAMPLE_ID_2"), rows.get(1).apply(ClinicalAttribute.SAMPLE_ID));
        assertEquals(Optional.of("2"), rows.get(1).apply(testNumberAttribute));
        assertEquals(Optional.empty(), rows.get(1).apply(testStringAttribute));
        assertEquals(Optional.of("TEST_PATIENT_ID_2"), rows.get(2).apply(ClinicalAttribute.PATIENT_ID));
        assertEquals(Optional.of("TEST_SAMPLE_ID_3"), rows.get(2).apply(ClinicalAttribute.SAMPLE_ID));
        assertEquals(Optional.empty(), rows.get(2).apply(testNumberAttribute));
        assertEquals(Optional.of("B"), rows.get(2).apply(testStringAttribute));
    }

    @Test
    public void testGetClinicalPatientAttributeData() {
        LongTable<ClinicalAttribute, String> result = clinicalDataAttributeDataService.getClinicalPatientAttributeData("testStudyId");
        List<ClinicalAttribute> clinicalAttributes = Lists.newArrayList(result.getColumns().iterator());
        assertEquals(3, clinicalAttributes.size());
        assertEquals(ClinicalAttribute.PATIENT_ID, clinicalAttributes.get(0));
        ClinicalAttribute testNumberAttribute = clinicalAttributes.get(1);
        assertEquals("TEST_NUMBER_PATIENT_ATTRIBUTE_ID", testNumberAttribute.getAttributeId());
        ClinicalAttribute testStringAttribute = clinicalAttributes.get(2);
        assertEquals("TEST_STRING_PATIENT_ATTRIBUTE_ID", testStringAttribute.getAttributeId());

        List<Function<ClinicalAttribute, Optional<String>>> rows = Lists.newArrayList(result);
        assertEquals(2, rows.size());
        assertEquals(Optional.of("TEST_PATIENT_ID_1"), rows.get(0).apply(ClinicalAttribute.PATIENT_ID));
        assertEquals(Optional.of("3"), rows.get(0).apply(testNumberAttribute));
        assertEquals(Optional.of("C"), rows.get(0).apply(testStringAttribute));
        assertEquals(Optional.of("TEST_PATIENT_ID_2"), rows.get(1).apply(ClinicalAttribute.PATIENT_ID));
        assertEquals(Optional.empty(), rows.get(1).apply(testNumberAttribute));
        assertEquals(Optional.of("D"), rows.get(1).apply(testStringAttribute));
    }
}
