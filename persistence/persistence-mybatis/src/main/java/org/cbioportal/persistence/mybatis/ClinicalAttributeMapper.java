package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface ClinicalAttributeMapper {

    List<ClinicalAttribute> getAllClinicalAttributes(String studyId, String projection, Integer limit, Integer offset,
                                              String sortBy, String direction);

    BaseMeta getMetaClinicalAttributes(String studyId);

    ClinicalAttribute getClinicalAttribute(String studyId, String clinicalAttributeId, String projection);
}
