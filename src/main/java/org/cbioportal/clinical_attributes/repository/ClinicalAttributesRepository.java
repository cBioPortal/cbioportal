package org.cbioportal.clinical_attributes.repository;

import org.cbioportal.legacy.model.ClinicalAttribute;
import org.cbioportal.legacy.web.parameter.ClinicalDataType;

import java.util.List;
import java.util.Map;

public interface ClinicalAttributesRepository {
    List<ClinicalAttribute> getClinicalAttributesForStudies(List<String> studyIds);
    Map<String, ClinicalDataType> getClinicalAttributeDatatypeMap();
}
