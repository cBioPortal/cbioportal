package org.cbioportal.service;

import org.cbioportal.model.ClinicalAttribute;

import java.util.List;

public interface AttributeByStudyService {
    List<ClinicalAttribute> getClinicalAttributesByStudyIdsAndAttributeIds(List<String> studyIds, List<String> attributeIds);
}
