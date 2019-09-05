package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ClinicalAttributeCount;
import org.cbioportal.model.meta.BaseMeta;

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
