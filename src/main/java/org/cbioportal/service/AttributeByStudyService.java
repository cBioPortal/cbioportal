package org.cbioportal.service;

import java.util.List;
import org.cbioportal.model.ClinicalAttribute;

public interface AttributeByStudyService {
  List<ClinicalAttribute> getClinicalAttributesByStudyIdsAndAttributeIds(
      List<String> studyIds, List<String> attributeIds);
}
