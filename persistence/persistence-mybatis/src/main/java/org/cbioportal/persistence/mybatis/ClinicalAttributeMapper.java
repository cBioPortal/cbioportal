package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface ClinicalAttributeMapper {

    List<ClinicalAttribute> getClinicalAttributes(List<String> studyIds, String projection, Integer limit, Integer offset,
                                              String sortBy, String direction);

    BaseMeta getMetaClinicalAttributes(List<String> studyIds);

    ClinicalAttribute getClinicalAttribute(String studyId, String clinicalAttributeId, String projection);

    List<ClinicalAttribute> getAllClinicalAttributesInStudiesBySampleIds(List<String> studyIds, List<String> sampleIds,
                                                                         String projection, String sortBy, String direction);

    List<ClinicalAttribute> getAllClinicalAttributesInStudiesBySampleListId(String sampleListId, String projection,
                                                                            String sortBy, String direction);
}
