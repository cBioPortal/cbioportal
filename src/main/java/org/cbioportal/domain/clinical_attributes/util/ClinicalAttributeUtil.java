package org.cbioportal.domain.clinical_attributes.util;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.cbioportal.domain.clinical_attributes.ClinicalAttribute;

public abstract class ClinicalAttributeUtil {
  private ClinicalAttributeUtil() {}

  /**
   * Categorizes clinical attributes into sample, patient, and conflicting categories based on their
   * attribute IDs and types across different studies.
   *
   * @param clinicalAttributes List of clinical attributes to categorize
   * @return CategorizedClinicalAttributes containing the categorized attribute IDs
   */
  public static CategorizedClinicalAttributeIds categorizeClinicalAttributes(
      List<ClinicalAttribute> clinicalAttributes) {

    // Group attributes by attribute ID
    Map<String, List<ClinicalAttribute>> clinicalAttributesById =
        clinicalAttributes.stream().collect(Collectors.groupingBy(ClinicalAttribute::attrId));

    Set<String> sampleAttributeIds = new HashSet<>();
    Set<String> patientAttributeIds = new HashSet<>();
    Set<String> conflictingAttributeIds = new HashSet<>();

    // For each attribute ID, check if it's sample, patient, or conflicting
    for (Map.Entry<String, List<ClinicalAttribute>> entry : clinicalAttributesById.entrySet()) {
      String attrId = entry.getKey();
      List<ClinicalAttribute> attributes = entry.getValue();

      boolean isSampleAttribute = false;
      boolean isPatientAttribute = false;

      for (ClinicalAttribute attribute : attributes) {
        if (Boolean.TRUE.equals(attribute.patientAttribute())) {
          isPatientAttribute = true;
        } else {
          isSampleAttribute = true;
        }
      }

      // Categorize based on the flags.
      // For conflicting attributes (both sample and patient level across studies), add to both
      // sampleAttributeIds and conflictingAttributeIds so that data is fetched from both the
      // sample-level table (for studies storing it as sample) and the patient-level table joined to
      // samples (for studies storing it as patient). This mirrors legacy behavior in
      // ClinicalAttributeUtil.extractCategorizedClinicalAttributes.
      if (isSampleAttribute && isPatientAttribute) {
        sampleAttributeIds.add(attrId);
        conflictingAttributeIds.add(attrId);
      } else if (isPatientAttribute) {
        patientAttributeIds.add(attrId);
      } else {
        sampleAttributeIds.add(attrId);
      }
    }

    return new CategorizedClinicalAttributeIds(
        sampleAttributeIds.stream().toList(),
        patientAttributeIds.stream().toList(),
        conflictingAttributeIds.stream().toList());
  }

  /** Record containing categorized clinical attribute IDs */
  public record CategorizedClinicalAttributeIds(
      List<String> sampleAttributeIds,
      List<String> patientAttributeIds,
      List<String> conflictingAttributeIds) {}
}
