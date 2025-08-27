package org.cbioportal.domain.clinical_data.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.cbioportal.legacy.model.ClinicalAttribute;
import org.junit.Test;

public class ClinicalAttributeUtilTest {

  @Test
  public void testCategorizeClinicalAttributes() {
    // Create test clinical attributes using existing data
    List<ClinicalAttribute> clinicalAttributes =
        Arrays.asList(
            // Sample-only attributes
            createClinicalAttribute("mutation_count", false), // sample-level only
            createClinicalAttribute("days_to_collection", false), // sample-level only

            // Patient-only attributes
            createClinicalAttribute("age", true), // patient-level only
            createClinicalAttribute("center", true), // patient-level only
            createClinicalAttribute("dead", true), // patient-level only

            // Conflicting attributes (same ID, different patient_attribute values)
            createClinicalAttribute("subtype", false), // sample-level subtype
            createClinicalAttribute("subtype", true) // patient-level subtype
            );

    var result = ClinicalAttributeUtil.categorizeClinicalAttributes(clinicalAttributes);

    // Verify sample attributes
    assertEquals("Should have 2 sample attributes", 2, result.sampleAttributeIds().size());
    assertTrue(
        "Should contain mutation_count", result.sampleAttributeIds().contains("mutation_count"));
    assertTrue(
        "Should contain days_to_collection",
        result.sampleAttributeIds().contains("days_to_collection"));

    // Verify patient attributes
    assertEquals("Should have 3 patient attributes", 3, result.patientAttributeIds().size());
    assertTrue("Should contain age", result.patientAttributeIds().contains("age"));
    assertTrue("Should contain center", result.patientAttributeIds().contains("center"));
    assertTrue("Should contain dead", result.patientAttributeIds().contains("dead"));

    // Verify conflicting attributes
    assertEquals("Should have 1 conflicting attribute", 1, result.conflictingAttributeIds().size());
    assertTrue("Should contain subtype", result.conflictingAttributeIds().contains("subtype"));
  }

  @Test
  public void testCategorizeClinicalAttributesEmptyList() {
    // Test with empty input
    var result = ClinicalAttributeUtil.categorizeClinicalAttributes(List.of());

    assertTrue("Sample attributes should be empty", result.sampleAttributeIds().isEmpty());
    assertTrue("Patient attributes should be empty", result.patientAttributeIds().isEmpty());
    assertTrue(
        "Conflicting attributes should be empty", result.conflictingAttributeIds().isEmpty());
  }

  @Test
  public void testCategorizeClinicalAttributesOnlySampleAttributes() {
    // Test with only sample attributes
    List<ClinicalAttribute> clinicalAttributes =
        Arrays.asList(
            createClinicalAttribute("mutation_count", false),
            createClinicalAttribute("sample_type", false),
            createClinicalAttribute("is_ffpe", false));

    var result = ClinicalAttributeUtil.categorizeClinicalAttributes(clinicalAttributes);

    assertEquals("Should have 3 sample attributes", 3, result.sampleAttributeIds().size());
    assertTrue("Patient attributes should be empty", result.patientAttributeIds().isEmpty());
    assertTrue(
        "Conflicting attributes should be empty", result.conflictingAttributeIds().isEmpty());
  }

  @Test
  public void testCategorizeClinicalAttributesOnlyPatientAttributes() {
    // Test with only patient attributes
    List<ClinicalAttribute> clinicalAttributes =
        Arrays.asList(
            createClinicalAttribute("age", true),
            createClinicalAttribute("os_months", true),
            createClinicalAttribute("dfs_status", true));

    var result = ClinicalAttributeUtil.categorizeClinicalAttributes(clinicalAttributes);

    assertTrue("Sample attributes should be empty", result.sampleAttributeIds().isEmpty());
    assertEquals("Should have 3 patient attributes", 3, result.patientAttributeIds().size());
    assertTrue(
        "Conflicting attributes should be empty", result.conflictingAttributeIds().isEmpty());
  }

  @Test
  public void testCategorizeClinicalAttributesMultipleConflictingAttributes() {
    // Test with multiple conflicting attributes
    List<ClinicalAttribute> clinicalAttributes =
        Arrays.asList(
            // First conflicting attribute
            createClinicalAttribute("subtype", false),
            createClinicalAttribute("subtype", true),

            // Second conflicting attribute
            createClinicalAttribute("grade", false),
            createClinicalAttribute("grade", true),

            // Regular attributes
            createClinicalAttribute("mutation_count", false),
            createClinicalAttribute("age", true));

    var result = ClinicalAttributeUtil.categorizeClinicalAttributes(clinicalAttributes);

    assertEquals("Should have 1 sample attribute", 1, result.sampleAttributeIds().size());
    assertEquals("Should have 1 patient attribute", 1, result.patientAttributeIds().size());
    assertEquals(
        "Should have 2 conflicting attributes", 2, result.conflictingAttributeIds().size());

    assertTrue(
        "Should contain subtype in conflicting",
        result.conflictingAttributeIds().contains("subtype"));
    assertTrue(
        "Should contain grade in conflicting", result.conflictingAttributeIds().contains("grade"));
  }

  @Test
  public void testCategorizeClinicalAttributesWithNullPatientAttribute() {
    // Test edge case where patientAttribute is null (should be treated as false)
    List<ClinicalAttribute> clinicalAttributes =
        Arrays.asList(
            createClinicalAttributeWithNullPatientAttribute("sample_attr"),
            createClinicalAttribute("age", true));

    var result = ClinicalAttributeUtil.categorizeClinicalAttributes(clinicalAttributes);

    assertEquals("Should have 1 sample attribute", 1, result.sampleAttributeIds().size());
    assertEquals("Should have 1 patient attribute", 1, result.patientAttributeIds().size());
    assertTrue(
        "Conflicting attributes should be empty", result.conflictingAttributeIds().isEmpty());
  }

  // Helper method to create ClinicalAttribute for testing
  private ClinicalAttribute createClinicalAttribute(
      String attributeId, boolean isPatientAttribute) {
    ClinicalAttribute attribute = new ClinicalAttribute();
    attribute.setAttrId(attributeId);
    attribute.setPatientAttribute(isPatientAttribute);
    return attribute;
  }

  // Helper method to create ClinicalAttribute with null patientAttribute
  private ClinicalAttribute createClinicalAttributeWithNullPatientAttribute(String attributeId) {
    ClinicalAttribute attribute = new ClinicalAttribute();
    attribute.setAttrId(attributeId);
    attribute.setPatientAttribute(null);
    return attribute;
  }
}
