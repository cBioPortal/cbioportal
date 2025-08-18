package org.cbioportal.legacy.service.util;

import java.util.Collections;
import java.util.List;
import org.cbioportal.legacy.model.ClinicalData;

public abstract class ClinicalDataUtil {
  private ClinicalDataUtil() {}

  /**
   * Converts new ClinicalData domain model to legacy ClinicalData model. This is a temporary
   * conversion method needed for legacy services that haven't been migrated to use the new clean
   * architecture ClinicalData domain model.
   *
   * <p>TODO: Remove this method once all legacy services are migrated to use the new ClinicalData
   * domain model
   */
  public static ClinicalData convertToLegacyClinicalData(
      org.cbioportal.domain.clinical_data.ClinicalData clinicalData) {
    if (clinicalData == null) {
      return null;
    }

    ClinicalData legacyClinicalData = new ClinicalData();
    legacyClinicalData.setInternalId(clinicalData.internalId());
    legacyClinicalData.setSampleId(clinicalData.sampleId());
    legacyClinicalData.setPatientId(clinicalData.patientId());
    legacyClinicalData.setStudyId(clinicalData.studyId());
    legacyClinicalData.setAttrId(clinicalData.attrId());
    legacyClinicalData.setAttrValue(clinicalData.attrValue());

    if (clinicalData.clinicalAttribute() != null) {
      legacyClinicalData.setClinicalAttribute(
          ClinicalAttributeUtil.convertToLegacyClinicalAttribute(clinicalData.clinicalAttribute()));
    }

    return legacyClinicalData;
  }

  /**
   * Converts a list of new ClinicalData domain models to legacy ClinicalData models. This is a
   * temporary conversion method needed for legacy services that haven't been migrated to use the
   * new clean architecture ClinicalData domain model.
   *
   * <p>TODO: Remove this method once all legacy services are migrated to use the new ClinicalData
   * domain model
   */
  public static List<ClinicalData> convertToLegacyClinicalDataList(
      List<org.cbioportal.domain.clinical_data.ClinicalData> clinicalDataList) {
    if (clinicalDataList == null) {
      return Collections.emptyList();
    }

    return clinicalDataList.stream().map(ClinicalDataUtil::convertToLegacyClinicalData).toList();
  }
}
