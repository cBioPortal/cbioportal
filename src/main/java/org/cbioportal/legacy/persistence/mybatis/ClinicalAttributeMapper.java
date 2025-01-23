package org.cbioportal.legacy.persistence.mybatis;

import org.cbioportal.legacy.model.ClinicalAttribute;
import org.cbioportal.legacy.model.ClinicalAttributeCount;
import org.cbioportal.legacy.model.meta.BaseMeta;

import java.util.List;

public interface ClinicalAttributeMapper {

    List<ClinicalAttribute> getClinicalAttributes(List<String> studyIds, String projection, Integer limit, Integer offset,
                                              String sortBy, String direction);

    BaseMeta getMetaClinicalAttributes(List<String> studyIds);

    ClinicalAttribute getClinicalAttribute(String studyId, String clinicalAttributeId, String projection);

    List<ClinicalAttributeCount> getClinicalAttributeCountsBySampleIds(List<String> studyIds, List<String> sampleIds);

    List<ClinicalAttributeCount> getClinicalAttributeCountsBySampleListId(String sampleListId);
    
    List<ClinicalAttribute> getClinicalAttributesByStudyIdsAndAttributeIds(List<String> studyIds, List<String> attributeIds);
}
