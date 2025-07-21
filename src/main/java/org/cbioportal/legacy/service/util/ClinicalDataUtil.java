package org.cbioportal.legacy.service.util;

import java.util.Collections;
import java.util.List;
import org.cbioportal.legacy.model.ClinicalData;

public class ClinicalDataUtil {
  private ClinicalDataUtil() {}

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

  public static List<ClinicalData> convertToLegacyClinicalDataList(
      List<org.cbioportal.domain.clinical_data.ClinicalData> clinicalDataList) {
    if (clinicalDataList == null) {
      return Collections.emptyList();
    }

    return clinicalDataList.stream().map(ClinicalDataUtil::convertToLegacyClinicalData).toList();
  }
}
