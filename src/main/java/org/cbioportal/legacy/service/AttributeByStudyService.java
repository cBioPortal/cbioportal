package org.cbioportal.legacy.service;

import org.cbioportal.legacy.model.ClinicalAttribute;

import java.util.List;

public interface AttributeByStudyService {
    List<ClinicalAttribute> getClinicalAttributesByStudyIdsAndAttributeIds(List<String> studyIds, List<String> attributeIds);
}
