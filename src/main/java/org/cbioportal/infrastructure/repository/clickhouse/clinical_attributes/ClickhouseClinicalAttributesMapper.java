package org.cbioportal.infrastructure.repository.clickhouse.clinical_attributes;

import org.cbioportal.legacy.model.ClinicalAttribute;

import java.util.List;

public interface ClickhouseClinicalAttributesMapper {
    List<ClinicalAttribute> getClinicalAttributes();
    List<ClinicalAttribute> getClinicalAttributesForStudies(List<String> studyIds);
}
