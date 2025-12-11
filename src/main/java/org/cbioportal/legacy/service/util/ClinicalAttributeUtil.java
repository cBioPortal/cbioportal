package org.cbioportal.legacy.service.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.cbioportal.legacy.model.ClinicalAttribute;
import org.springframework.stereotype.Component;

@Component
public abstract class ClinicalAttributeUtil {

  ClinicalAttributeUtil() {}

  public static void extractCategorizedClinicalAttributes(
      List<ClinicalAttribute> clinicalAttributes,
      List<String> sampleAttributeIds,
      List<String> patientAttributeIds,
      List<String> conflictingPatientAttributeIds) {

    Set<String> sampleAttributeIdsSet = new HashSet<String>();
    Set<String> patientAttributeIdsSet = new HashSet<String>();
    Set<String> conflictingPatientAttributeIdsSet = new HashSet<String>();

    Map<String, Map<Boolean, List<ClinicalAttribute>>> groupedAttributesByIdAndType =
        clinicalAttributes.stream()
            .collect(
                Collectors.groupingBy(
                    ClinicalAttribute::getAttrId,
                    Collectors.groupingBy(ClinicalAttribute::getPatientAttribute)));

    groupedAttributesByIdAndType
        .entrySet()
        .forEach(
            entry -> {
              if (entry.getValue().keySet().size() == 1) {
                entry.getValue().entrySet().stream()
                    .forEach(
                        x -> {
                          if (x.getKey()) {
                            patientAttributeIdsSet.add(entry.getKey());
                          } else {
                            sampleAttributeIdsSet.add(entry.getKey());
                          }
                        });
              } else {
                entry
                    .getValue()
                    .entrySet()
                    .forEach(
                        x -> {
                          if (x.getKey()) {
                            conflictingPatientAttributeIdsSet.add(entry.getKey());
                          } else {
                            sampleAttributeIdsSet.add(entry.getKey());
                          }
                        });
              }
            });

    sampleAttributeIds.addAll(sampleAttributeIdsSet);
    patientAttributeIds.addAll(patientAttributeIdsSet);
    conflictingPatientAttributeIds.addAll(conflictingPatientAttributeIdsSet);
  }

  /**
   * Converts new ClinicalAttribute domain model to legacy ClinicalAttribute model. This is a
   * temporary conversion method needed for legacy services that haven't been migrated to use the
   * new clean architecture ClinicalAttribute domain model.
   *
   * <p>TODO: Remove this method once all legacy services are migrated to use the new
   * ClinicalAttribute domain model
   */
  public static ClinicalAttribute convertToLegacyClinicalAttribute(
      org.cbioportal.domain.clinical_attributes.ClinicalAttribute clinicalAttribute) {
    if (clinicalAttribute == null) {
      return null;
    }

    ClinicalAttribute legacyClinicalAttribute = new ClinicalAttribute();
    legacyClinicalAttribute.setAttrId(clinicalAttribute.attrId());
    legacyClinicalAttribute.setDisplayName(clinicalAttribute.displayName());
    legacyClinicalAttribute.setDescription(clinicalAttribute.description());
    legacyClinicalAttribute.setDatatype(clinicalAttribute.datatype());
    legacyClinicalAttribute.setPatientAttribute(clinicalAttribute.patientAttribute());
    legacyClinicalAttribute.setPriority(clinicalAttribute.priority());
    legacyClinicalAttribute.setCancerStudyId(clinicalAttribute.cancerStudyId());
    legacyClinicalAttribute.setCancerStudyIdentifier(clinicalAttribute.cancerStudyIdentifier());
    return legacyClinicalAttribute;
  }

  public static List<ClinicalAttribute> convertToLegacyClinicalAttributeList(
      List<org.cbioportal.domain.clinical_attributes.ClinicalAttribute> clinicalDataList) {
    if (clinicalDataList == null) {
      return Collections.emptyList();
    }

    return clinicalDataList.stream()
        .map(ClinicalAttributeUtil::convertToLegacyClinicalAttribute)
        .toList();
  }
}
