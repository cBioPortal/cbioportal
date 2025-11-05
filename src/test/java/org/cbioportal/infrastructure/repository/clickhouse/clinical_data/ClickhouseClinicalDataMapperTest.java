package org.cbioportal.infrastructure.repository.clickhouse.clinical_data;

import static org.junit.Assert.*;

import java.util.List;
import org.cbioportal.domain.clinical_data.ClinicalData;
import org.cbioportal.domain.studyview.StudyViewFilterFactory;
import org.cbioportal.infrastructure.repository.clickhouse.AbstractTestcontainers;
import org.cbioportal.infrastructure.repository.clickhouse.config.MyBatisConfig;
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
public class ClickhouseClinicalDataMapperTest {
  private static final String STUDY_ACC_TCGA = "acc_tcga";
  private static final String STUDY_GENIE_PUB = "study_genie_pub";

  @Autowired private ClickhouseClinicalDataMapper mapper;

  @Test
  public void testDataCountConsistency() {
    // Test Sample type with specific IDs and attributes
    List<String> sampleUniqueIds =
        List.of("study_genie_pub_GENIE-TEST-301-01", "study_genie_pub_GENIE-TEST-302-01");
    List<String> sampleAttributeIds = List.of("mutation_count");

    List<ClinicalData> sampleData =
        mapper.fetchClinicalDataSummary(sampleUniqueIds, sampleAttributeIds, "sample");
    Integer sampleCount =
        mapper.fetchClinicalDataMeta(sampleUniqueIds, sampleAttributeIds, "sample");

    assertEquals("Sample data count should match", sampleData.size(), sampleCount.intValue());

    // Test Patient type - all data
    List<ClinicalData> allPatientData = mapper.fetchClinicalDataId(null, null, "patient");
    Integer allPatientCount = mapper.fetchClinicalDataMeta(null, null, "patient");

    assertEquals(
        "Patient data count should match", allPatientData.size(), allPatientCount.intValue());

    // Test Patient type with specific filters
    List<String> patientUniqueIds =
        List.of("study_genie_pub_GENIE-TEST-301", "study_genie_pub_GENIE-TEST-302");
    List<String> patientAttributeIds = List.of("age", "center");

    List<ClinicalData> filteredPatientData =
        mapper.fetchClinicalDataSummary(patientUniqueIds, patientAttributeIds, "patient");
    Integer filteredPatientCount =
        mapper.fetchClinicalDataMeta(patientUniqueIds, patientAttributeIds, "patient");

    assertEquals(
        "Filtered patient data count should match",
        filteredPatientData.size(),
        filteredPatientCount.intValue());

    // Expected: 2 patients × 2 attributes = 4 records (if both patients have both attributes)
    assertEquals(
        "Should have 4 records for 2 patients × 2 attributes", 4, filteredPatientData.size());
  }

  @Test
  public void testEmptyAndNullConditions() {
    // Test with non-existent IDs - should return empty results
    List<String> nonExistentIds = List.of("non_existent_study_non_existent_id");

    List<ClinicalData> emptyData = mapper.fetchClinicalDataSummary(nonExistentIds, null, "sample");
    Integer emptyCount = mapper.fetchClinicalDataMeta(nonExistentIds, null, "sample");

    assertEquals("Empty data should have size 0", 0, emptyData.size());
    assertEquals("Empty count should be 0", 0, emptyCount.intValue());

    // Test with non-existent attribute IDs
    List<String> validIds = List.of("study_genie_pub_GENIE-TEST-301-01");
    List<String> nonExistentAttrs = List.of("non_existent_attribute");

    List<ClinicalData> noAttrData =
        mapper.fetchClinicalDataSummary(validIds, nonExistentAttrs, "sample");
    Integer noAttrCount = mapper.fetchClinicalDataMeta(validIds, nonExistentAttrs, "sample");

    assertEquals("No attribute data should have size 0", 0, noAttrData.size());
    assertEquals("No attribute count should be 0", 0, noAttrCount.intValue());
  }

  @Test
  public void testProjectionTypesConsistency() {
    // Use same parameters for all projection types
    List<String> sameIds =
        List.of("study_genie_pub_GENIE-TEST-301-01", "study_genie_pub_GENIE-TEST-302-01");
    List<String> sameAttributes = List.of("mutation_count");

    // Test all projection types return same count
    List<ClinicalData> idData = mapper.fetchClinicalDataId(sameIds, sameAttributes, "sample");
    List<ClinicalData> summaryData =
        mapper.fetchClinicalDataSummary(sameIds, sameAttributes, "sample");
    List<ClinicalData> detailedData =
        mapper.fetchClinicalDataDetailed(sameIds, sameAttributes, "sample");
    Integer metaCount = mapper.fetchClinicalDataMeta(sameIds, sameAttributes, "sample");

    // All should have the same count
    assertEquals(
        "ID and SUMMARY projection should have same count", idData.size(), summaryData.size());
    assertEquals(
        "SUMMARY and DETAILED projection should have same count",
        summaryData.size(),
        detailedData.size());
    assertEquals(
        "DETAILED projection and meta count should match",
        detailedData.size(),
        metaCount.intValue());

    // Verify projection differences in returned data
    if (!idData.isEmpty()) {
      ClinicalData idResult = idData.getFirst();
      ClinicalData summaryResult = summaryData.getFirst();
      ClinicalData detailedResult = detailedData.getFirst();

      // ID projection should not have attrValue
      assertNull("ID projection should not have attrValue", idResult.attrValue());

      // SUMMARY projection should have attrValue
      assertNotNull("SUMMARY projection should have attrValue", summaryResult.attrValue());

      // DETAILED projection should have clinical attribute info
      assertNotNull(
          "DETAILED projection should have clinical attribute", detailedResult.clinicalAttribute());

      // Basic fields should be consistent across projections
      assertEquals("AttrId should be consistent", idResult.attrId(), summaryResult.attrId());
      assertEquals(
          "PatientId should be consistent", idResult.patientId(), detailedResult.patientId());
    }
  }

  @Test
  public void getPatientClinicalDataFromStudyViewFilter() {
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(STUDY_GENIE_PUB));
    List<String> attributeIds = List.of("age");

    List<ClinicalData> data =
        mapper.getPatientClinicalDataFromStudyViewFilter(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            attributeIds);

    assertFalse("Patients should have age clinical data", data.isEmpty());
  }

  @Test
  public void getSampleClinicalDataFromStudyViewFilter() {
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(STUDY_GENIE_PUB));
    List<String> attributeIds = List.of("mutation_count");

    List<ClinicalData> data =
        mapper.getSampleClinicalDataFromStudyViewFilter(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            attributeIds);

    assertFalse("Samples should have mutation_count clinical data", data.isEmpty());
  }
}
