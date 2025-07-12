package org.cbioportal.legacy.service.util;

import java.util.List;
import org.cbioportal.legacy.model.ClinicalData;

public class ClinicalDataUtil {
  private ClinicalDataUtil() {}

  public static ClinicalData convertToLegacyClinicalData(
      org.cbioportal.domain.clinical_data.ClinicalData clinicalData) {
    ClinicalData deprecatedClinicalData = new ClinicalData();
    deprecatedClinicalData.setInternalId(clinicalData.internalId());
    deprecatedClinicalData.setSampleId(clinicalData.sampleId());
    deprecatedClinicalData.setPatientId(clinicalData.patientId());
    deprecatedClinicalData.setStudyId(clinicalData.studyId());
    deprecatedClinicalData.setAttrId(clinicalData.attrId());
    deprecatedClinicalData.setClinicalAttribute(
        ClinicalAttributeUtil.convertToLegacyClinicalAttribute(clinicalData.clinicalAttribute()));
    return deprecatedClinicalData;
  }

  public static List<ClinicalData> convertToLegacyClinicalDataList(
      List<org.cbioportal.domain.clinical_data.ClinicalData> clinicalDataList) {
    return clinicalDataList.stream().map(ClinicalDataUtil::convertToLegacyClinicalData).toList();
  }
}
