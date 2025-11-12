package org.cbioportal.infrastructure.repository.clickhouse.clinical_data;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import org.cbioportal.domain.clinical_data.ClinicalData;
import org.cbioportal.domain.studyview.StudyViewFilterFactory;
import org.cbioportal.infrastructure.repository.clickhouse.AbstractTestcontainers;
import org.cbioportal.infrastructure.repository.clickhouse.config.MyBatisConfig;
import org.cbioportal.legacy.model.ClinicalDataCount;
import org.cbioportal.legacy.web.parameter.ClinicalDataFilter;
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
public class ClickhouseClinicalDataMapperTest {
  private static final String STUDY_ACC_TCGA = "acc_tcga";
  private static final String STUDY_GENIE_PUB = "study_genie_pub";

  @Autowired private ClickhouseClinicalDataMapper mapper;

  @Test
  public void getMutationCounts() {
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(STUDY_GENIE_PUB));

    var clinicalDataCountItems =
        mapper.getClinicalDataCounts(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            List.of("mutation_count"),
            Collections.emptyList(),
            Collections.emptyList());

    var mutationsCountsOptional =
        clinicalDataCountItems.stream()
            .filter(c -> c.getAttributeId().equals("mutation_count"))
            .findFirst();

    assertTrue(mutationsCountsOptional.isPresent());
    var mutationsCounts = mutationsCountsOptional.get().getCounts();

    assertEquals(6, mutationsCounts.size());
    assertEquals(1, findClinicalDataCount(mutationsCounts, "11"));
    assertEquals(1, findClinicalDataCount(mutationsCounts, "6"));
    assertEquals(2, findClinicalDataCount(mutationsCounts, "4"));
    assertEquals(4, findClinicalDataCount(mutationsCounts, "2"));
    assertEquals(2, findClinicalDataCount(mutationsCounts, "1"));
    // 1 empty string + 1 'NAN' + 15 samples with no data
    assertEquals(17, findClinicalDataCount(mutationsCounts, "NA"));
  }

  @Test
  public void getCenterCounts() {
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(STUDY_GENIE_PUB));

    var clinicalDataCounts =
        mapper.getClinicalDataCounts(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            Collections.emptyList(),
            List.of("center"),
            Collections.emptyList());

    var categoricalClinicalDataCountsOptional =
        clinicalDataCounts.stream().filter(c -> c.getAttributeId().equals("center")).findFirst();

    assertTrue(categoricalClinicalDataCountsOptional.isPresent());
    var categoricalClinicalDataCounts = categoricalClinicalDataCountsOptional.get().getCounts();

    assertEquals(7, categoricalClinicalDataCounts.size());
    assertEquals(3, findClinicalDataCount(categoricalClinicalDataCounts, "msk"));
    assertEquals(2, findClinicalDataCount(categoricalClinicalDataCounts, "dfci"));
    assertEquals(2, findClinicalDataCount(categoricalClinicalDataCounts, "chop"));
    assertEquals(1, findClinicalDataCount(categoricalClinicalDataCounts, "mda"));
    assertEquals(1, findClinicalDataCount(categoricalClinicalDataCounts, "ohsu"));
    assertEquals(1, findClinicalDataCount(categoricalClinicalDataCounts, "ucsf"));
    // 1 empty string + 1 'NA' + 12 samples with no data
    assertEquals(14, findClinicalDataCount(categoricalClinicalDataCounts, "NA"));
  }

  @Test
  public void getDeadCounts() {
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(STUDY_GENIE_PUB));

    var clinicalDataCounts =
        mapper.getClinicalDataCounts(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            Collections.emptyList(),
            List.of("dead"),
            Collections.emptyList());

    var categoricalClinicalDataCountsOptional =
        clinicalDataCounts.stream().filter(c -> c.getAttributeId().equals("dead")).findFirst();

    assertTrue(categoricalClinicalDataCountsOptional.isPresent());
    var categoricalClinicalDataCounts = categoricalClinicalDataCountsOptional.get().getCounts();

    assertEquals(10, categoricalClinicalDataCounts.size());
    assertEquals(1, findClinicalDataCount(categoricalClinicalDataCounts, "True"));
    assertEquals(1, findClinicalDataCount(categoricalClinicalDataCounts, "TRUE"));
    assertEquals(1, findClinicalDataCount(categoricalClinicalDataCounts, "true"));
    assertEquals(1, findClinicalDataCount(categoricalClinicalDataCounts, "False"));
    assertEquals(2, findClinicalDataCount(categoricalClinicalDataCounts, "FALSE"));
    assertEquals(1, findClinicalDataCount(categoricalClinicalDataCounts, "false"));
    assertEquals(1, findClinicalDataCount(categoricalClinicalDataCounts, "Not Released"));
    assertEquals(1, findClinicalDataCount(categoricalClinicalDataCounts, "Not Collected"));
    assertEquals(1, findClinicalDataCount(categoricalClinicalDataCounts, "Unknown"));
    // 1 empty string + 1 'N/A' + 12 samples with no data
    assertEquals(14, findClinicalDataCount(categoricalClinicalDataCounts, "NA"));
  }

  @Test
  public void getMutationAndCenterCounts() {
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(STUDY_GENIE_PUB));

    var combinedClinicalDataCounts =
        mapper.getClinicalDataCounts(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            List.of("mutation_count"),
            List.of("center"),
            Collections.emptyList());

    assertEquals(2, combinedClinicalDataCounts.size());
  }

  @Test
  public void getAgeCounts() {
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(STUDY_GENIE_PUB));

    var clinicalDataCountItems =
        mapper.getClinicalDataCounts(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            Collections.emptyList(),
            List.of("age"),
            Collections.emptyList());

    var ageCountsOptional =
        clinicalDataCountItems.stream().filter(c -> c.getAttributeId().equals("age")).findFirst();

    assertTrue(ageCountsOptional.isPresent());
    var ageCounts = ageCountsOptional.get().getCounts();

    assertAgeCounts(ageCounts);

    // 1 empty string + 1 'NAN' + 1 'N/A' + 1 patient without data
    assertEquals(4, findClinicalDataCount(ageCounts, "NA"));
  }

  @Test
  public void getAgeCountsForMultipleStudies() {
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(STUDY_GENIE_PUB, STUDY_ACC_TCGA));

    var clinicalDataCountItems =
        mapper.getClinicalDataCounts(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            Collections.emptyList(),
            List.of("age"),
            Collections.emptyList());

    var ageCountsOptional =
        clinicalDataCountItems.stream().filter(c -> c.getAttributeId().equals("age")).findFirst();

    assertTrue(ageCountsOptional.isPresent());
    var ageCounts = ageCountsOptional.get().getCounts();

    // everything should be exactly the same as single study (STUDY_GENIE_PUB) filter
    // except NA counts
    assertAgeCounts(ageCounts);

    // 1 empty string + 1 'NAN' + 1 'N/A' + 1 GENIE_PUB patient without data + 4 ACC_TCGA data
    // without data
    assertEquals(8, findClinicalDataCount(ageCounts, "NA"));
  }

  private void assertAgeCounts(List<ClinicalDataCount> ageCounts) {
    assertEquals(15, ageCounts.size());

    assertEquals(3, findClinicalDataCount(ageCounts, "<18"));
    assertEquals(1, findClinicalDataCount(ageCounts, "18"));
    assertEquals(1, findClinicalDataCount(ageCounts, "22"));
    assertEquals(2, findClinicalDataCount(ageCounts, "42"));
    assertEquals(1, findClinicalDataCount(ageCounts, "66"));
    assertEquals(1, findClinicalDataCount(ageCounts, "66"));
    assertEquals(1, findClinicalDataCount(ageCounts, "68"));
    assertEquals(1, findClinicalDataCount(ageCounts, "77"));
    assertEquals(1, findClinicalDataCount(ageCounts, "78"));
    assertEquals(1, findClinicalDataCount(ageCounts, "79"));
    assertEquals(2, findClinicalDataCount(ageCounts, "80"));
    assertEquals(2, findClinicalDataCount(ageCounts, "82"));
    assertEquals(1, findClinicalDataCount(ageCounts, "89"));
    assertEquals(2, findClinicalDataCount(ageCounts, ">89"));
    assertEquals(1, findClinicalDataCount(ageCounts, "UNKNOWN"));
  }

  @Test
  public void getMutationCountsFilteredByAge() {
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(STUDY_GENIE_PUB));

    // filter patients with age between 20 and 70
    // (there are 5 patients within this range, which are 307..311)
    ClinicalDataFilter filter = buildClinicalDataFilter("age", 20, 70);
    studyViewFilter.setClinicalDataFilters(List.of(filter));

    var clinicalDataCountItems =
        mapper.getClinicalDataCounts(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            List.of("mutation_count"),
            Collections.emptyList(),
            Collections.emptyList());

    var mutationsCountsOptional =
        clinicalDataCountItems.stream()
            .filter(c -> c.getAttributeId().equals("mutation_count"))
            .findFirst();

    assertTrue(mutationsCountsOptional.isPresent());
    var mutationCountsFiltered = mutationsCountsOptional.get().getCounts();

    assertEquals(3, mutationCountsFiltered.size());
    assertEquals(2, findClinicalDataCount(mutationCountsFiltered, "2"));
    assertEquals(2, findClinicalDataCount(mutationCountsFiltered, "1"));
    assertEquals(1, findClinicalDataCount(mutationCountsFiltered, "NA"));
  }

  @Test
  public void getMutationCountsFilteredByAgeWithOpenStartValues() {
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(STUDY_GENIE_PUB));

    // filter patients with age less than 20
    // (there are 4 patients within this range, which are 301, 302, 303, and 306)
    ClinicalDataFilter filter = buildClinicalDataFilter("age", null, 20);
    studyViewFilter.setClinicalDataFilters(List.of(filter));

    var clinicalDataCountItems =
        mapper.getClinicalDataCounts(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            List.of("mutation_count"),
            Collections.emptyList(),
            Collections.emptyList());

    var mutationsCountsOptional =
        clinicalDataCountItems.stream()
            .filter(c -> c.getAttributeId().equals("mutation_count"))
            .findFirst();

    assertTrue(mutationsCountsOptional.isPresent());
    var mutationCountsFiltered = mutationsCountsOptional.get().getCounts();

    assertEquals(4, mutationCountsFiltered.size());
    assertEquals(1, findClinicalDataCount(mutationCountsFiltered, "11")); // patient 301
    assertEquals(1, findClinicalDataCount(mutationCountsFiltered, "6")); // patient 302
    assertEquals(1, findClinicalDataCount(mutationCountsFiltered, "4")); // patient 303
    assertEquals(1, findClinicalDataCount(mutationCountsFiltered, "2")); // patient 306

    // no patients/samples with NA
    assertEquals(0, findClinicalDataCount(mutationCountsFiltered, "NA"));
  }

  @Test
  public void getMutationCountsFilteredByAgeWithOpenEndValues() {
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(STUDY_GENIE_PUB));

    // filter patients with age greater than 80
    // (there are 4 patients within this range, which are 317, 318, 319, 304, and 305)
    ClinicalDataFilter filter = buildClinicalDataFilter("age", 80, null);
    studyViewFilter.setClinicalDataFilters(List.of(filter));

    var clinicalDataCountItems =
        mapper.getClinicalDataCounts(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            List.of("mutation_count"),
            Collections.emptyList(),
            Collections.emptyList());

    var mutationsCountsOptional =
        clinicalDataCountItems.stream()
            .filter(c -> c.getAttributeId().equals("mutation_count"))
            .findFirst();

    assertTrue(mutationsCountsOptional.isPresent());
    var mutationCountsFiltered = mutationsCountsOptional.get().getCounts();

    assertEquals(3, mutationCountsFiltered.size());
    assertEquals(1, findClinicalDataCount(mutationCountsFiltered, "4")); // patient 304
    assertEquals(1, findClinicalDataCount(mutationCountsFiltered, "2")); // patient 305

    // patients/samples with NA data: 317, 318, and 319
    assertEquals(3, findClinicalDataCount(mutationCountsFiltered, "NA"));
  }

  @Test
  public void testDataCountConsistency() {
    // Test Sample type with specific IDs and attributes
    List<String> sampleUniqueIds =
        List.of("study_genie_pub_GENIE-TEST-301-01", "study_genie_pub_GENIE-TEST-302-01");
    List<String> sampleAttributeIds = List.of("mutation_count");

    List<ClinicalData> sampleData =
        mapper.fetchClinicalDataSummary(
            sampleUniqueIds, sampleAttributeIds, List.of(STUDY_GENIE_PUB), "sample");
    Integer sampleCount =
        mapper.fetchClinicalDataMeta(
            sampleUniqueIds, sampleAttributeIds, List.of(STUDY_GENIE_PUB), "sample");

    assertEquals("Sample data count should match", sampleData.size(), sampleCount.intValue());

    // Test Patient type - all data
    List<ClinicalData> allPatientData = mapper.fetchClinicalDataId(null, null, null, "patient");
    Integer allPatientCount = mapper.fetchClinicalDataMeta(null, null, null, "patient");

    assertEquals(
        "Patient data count should match", allPatientData.size(), allPatientCount.intValue());

    // Test Patient type with specific filters
    List<String> patientUniqueIds =
        List.of("study_genie_pub_GENIE-TEST-301", "study_genie_pub_GENIE-TEST-302");
    List<String> patientAttributeIds = List.of("age", "center");

    List<ClinicalData> filteredPatientData =
        mapper.fetchClinicalDataSummary(
            patientUniqueIds, patientAttributeIds, List.of(STUDY_GENIE_PUB), "patient");
    Integer filteredPatientCount =
        mapper.fetchClinicalDataMeta(
            patientUniqueIds, patientAttributeIds, List.of(STUDY_GENIE_PUB), "patient");

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

    List<ClinicalData> emptyData =
        mapper.fetchClinicalDataSummary(
            nonExistentIds, null, List.of("non_existent_study"), "sample");
    Integer emptyCount =
        mapper.fetchClinicalDataMeta(nonExistentIds, null, List.of("non_existent_study"), "sample");

    assertEquals("Empty data should have size 0", 0, emptyData.size());
    assertEquals("Empty count should be 0", 0, emptyCount.intValue());

    // Test with non-existent attribute IDs
    List<String> validIds = List.of("study_genie_pub_GENIE-TEST-301-01");
    List<String> nonExistentAttrs = List.of("non_existent_attribute");

    List<ClinicalData> noAttrData =
        mapper.fetchClinicalDataSummary(
            validIds, nonExistentAttrs, List.of("non_existent_study"), "sample");
    Integer noAttrCount =
        mapper.fetchClinicalDataMeta(
            validIds, nonExistentAttrs, List.of("non_existent_study"), "sample");

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
    List<ClinicalData> idData =
        mapper.fetchClinicalDataId(sameIds, sameAttributes, List.of(STUDY_GENIE_PUB), "sample");
    List<ClinicalData> summaryData =
        mapper.fetchClinicalDataSummary(
            sameIds, sameAttributes, List.of(STUDY_GENIE_PUB), "sample");
    List<ClinicalData> detailedData =
        mapper.fetchClinicalDataDetailed(
            sameIds, sameAttributes, List.of(STUDY_GENIE_PUB), "sample");
    Integer metaCount =
        mapper.fetchClinicalDataMeta(sameIds, sameAttributes, List.of(STUDY_GENIE_PUB), "sample");

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
  public void getConflictingAttributeCounts() {
    // Test conflicting attributes where same attribute name exists in both sample and patient
    // levels
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(STUDY_ACC_TCGA, STUDY_GENIE_PUB));

    var clinicalDataCountItems =
        mapper.getClinicalDataCounts(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            Collections.emptyList(), // no sample attributes
            Collections.emptyList(), // no patient attributes
            List.of("subtype") // only conflicting attributes
            );

    var subtypeCountsOptional =
        clinicalDataCountItems.stream()
            .filter(c -> c.getAttributeId().equals("subtype"))
            .findFirst();

    assertTrue("Subtype counts should be present", subtypeCountsOptional.isPresent());
    var subtypeCounts = subtypeCountsOptional.get().getCounts();

    // Expected: sample-level data from acc_tcga + patient-level data from study_genie_pub
    assertEquals("Should have 5 subtype categories", 5, subtypeCounts.size());

    assertEquals("Luminal A count", 2, findClinicalDataCount(subtypeCounts, "Luminal A"));
    assertEquals("Luminal B count", 2, findClinicalDataCount(subtypeCounts, "Luminal B"));
    assertEquals("HER2+ count", 2, findClinicalDataCount(subtypeCounts, "HER2+"));
    assertEquals(
        "Triple Negative count", 1, findClinicalDataCount(subtypeCounts, "Triple Negative"));

    // NA count calculated using total SAMPLE count due to isConflicting=true
    assertTrue("NA count should be > 0", findClinicalDataCount(subtypeCounts, "NA") > 0);
  }

  @Test
  public void getConflictingAttributeCountsWithSampleAndPatientAttributes() {
    // Test conflicting attributes combined with regular sample/patient attributes
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(STUDY_ACC_TCGA, STUDY_GENIE_PUB));

    var combinedClinicalDataCounts =
        mapper.getClinicalDataCounts(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            List.of("mutation_count"), // sample attribute
            List.of("center"), // patient attribute
            List.of("subtype") // conflicting attribute
            );

    // Verify all three attribute types are returned via UNION logic
    assertEquals("Should have 3 attributes", 3, combinedClinicalDataCounts.size());

    assertTrue(
        "mutation_count should be present",
        combinedClinicalDataCounts.stream()
            .anyMatch(c -> c.getAttributeId().equals("mutation_count")));
    assertTrue(
        "center should be present",
        combinedClinicalDataCounts.stream().anyMatch(c -> c.getAttributeId().equals("center")));
    assertTrue(
        "subtype should be present",
        combinedClinicalDataCounts.stream().anyMatch(c -> c.getAttributeId().equals("subtype")));
  }

  @Test
  public void getConflictingAttributeCountsWithFiltering() {
    // Test conflicting attributes work correctly with study view filtering
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(STUDY_ACC_TCGA, STUDY_GENIE_PUB));

    // Filter for patients with age > 75 (patients 304, 305, 312, 313, 314, 315, 316, 317, 318, 319)
    ClinicalDataFilter filter = buildClinicalDataFilter("age", 75, null);
    studyViewFilter.setClinicalDataFilters(List.of(filter));

    var filteredClinicalDataCounts =
        mapper.getClinicalDataCounts(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
            Collections.emptyList(),
            Collections.emptyList(),
            List.of("subtype"));

    var subtypeCountsOptional =
        filteredClinicalDataCounts.stream()
            .filter(c -> c.getAttributeId().equals("subtype"))
            .findFirst();

    assertTrue("Filtered subtype counts should be present", subtypeCountsOptional.isPresent());
    var subtypeCounts = subtypeCountsOptional.get().getCounts();

    // After filtering: 10 total samples, 4 with actual values, 6 NA
    assertEquals("Should have 5 subtype categories after filtering", 5, subtypeCounts.size());

    assertEquals(
        "Triple Negative count", 1, findClinicalDataCount(subtypeCounts, "Triple Negative"));
    assertEquals("Luminal A count", 1, findClinicalDataCount(subtypeCounts, "Luminal A"));
    assertEquals("HER2+ count", 1, findClinicalDataCount(subtypeCounts, "HER2+"));
    assertEquals("Luminal B count", 1, findClinicalDataCount(subtypeCounts, "Luminal B"));
    assertEquals("NA count", 6, findClinicalDataCount(subtypeCounts, "NA"));

    // Verify NA calculation uses sample count even with filtering (isConflicting=true)
    assertTrue(
        "Should have NA count with filtering", findClinicalDataCount(subtypeCounts, "NA") > 0);
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

  private ClinicalDataFilter buildClinicalDataFilter(
      String attributeId, Integer start, Integer end) {
    DataFilterValue value = new DataFilterValue();
    if (start != null) {
      value.setStart(BigDecimal.valueOf(start));
    }
    if (end != null) {
      value.setEnd(BigDecimal.valueOf(end));
    }

    ClinicalDataFilter filter = new ClinicalDataFilter();
    filter.setAttributeId(attributeId);
    filter.setValues(List.of(value));

    return filter;
  }

  private int findClinicalDataCount(List<ClinicalDataCount> counts, String attrValue) {
    var count = counts.stream().filter(c -> c.getValue().equals(attrValue)).findAny().orElse(null);

    return count == null ? 0 : count.getCount();
  }
}
