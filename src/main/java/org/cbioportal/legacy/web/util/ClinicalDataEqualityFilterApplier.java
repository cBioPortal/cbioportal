package org.cbioportal.legacy.web.util;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.cbioportal.legacy.model.ClinicalData;
import org.cbioportal.legacy.service.ClinicalDataService;
import org.cbioportal.legacy.service.PatientService;
import org.cbioportal.legacy.web.parameter.ClinicalDataFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ClinicalDataEqualityFilterApplier extends ClinicalDataFilterApplier {
  @Autowired
  public ClinicalDataEqualityFilterApplier(
      PatientService patientService,
      ClinicalDataService clinicalDataService,
      StudyViewFilterUtil studyViewFilterUtil) {
    super(patientService, clinicalDataService, studyViewFilterUtil);
  }

  @Autowired private StudyViewFilterUtil studyViewFilterUtil;

  @Override
  public Integer apply(
      List<ClinicalDataFilter> attributes,
      MultiKeyMap clinicalDataMap,
      String entityId,
      String studyId,
      boolean negateFilters) {
    return studyViewFilterUtil.getFilteredCountByDataEquality(
        attributes, clinicalDataMap, entityId, studyId, negateFilters);
  }

  public static MultiKeyMap<String, List<String>> buildClinicalDataMap(
      List<ClinicalData> clinicalDatas) {
    MultiKeyMap<String, List<String>> clinicalDataMap = new MultiKeyMap<>();

    clinicalDatas.forEach(
        clinicalData -> {
          if (!clinicalDataMap.containsKey(
              clinicalData.getStudyId(), clinicalData.getSampleId(), clinicalData.getAttrId())) {
            clinicalDataMap.put(
                clinicalData.getStudyId(),
                clinicalData.getSampleId(),
                clinicalData.getAttrId(),
                new ArrayList<>());
          }
          clinicalDataMap
              .get(clinicalData.getStudyId(), clinicalData.getSampleId(), clinicalData.getAttrId())
              .add(clinicalData.getAttrValue());
        });

    return clinicalDataMap;
  }
}
