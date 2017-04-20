package org.cbioportal.persistence;

import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface ClinicalAttributeRepository {

    List<ClinicalAttribute> getAllClinicalAttributes(String projection, Integer pageSize, Integer pageNumber,
                                                     String sortBy, String direction);

    BaseMeta getMetaClinicalAttributes();

    ClinicalAttribute getClinicalAttribute(String studyId, String clinicalAttributeId);

    List<ClinicalAttribute> getAllClinicalAttributesInStudy(String studyId, String projection, Integer pageSize,
                                                            Integer pageNumber, String sortBy, String direction);

    BaseMeta getMetaClinicalAttributesInStudy(String studyId);
}
