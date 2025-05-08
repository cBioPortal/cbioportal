package org.cbioportal.legacy.service;

import java.util.List;
import org.cbioportal.legacy.model.ClinicalAttribute;

public interface AttributeByStudyService {
  List<ClinicalAttribute> getClinicalAttributesByStudyIdsAndAttributeIds(
      List<String> studyIds, List<String> attributeIds);
}
