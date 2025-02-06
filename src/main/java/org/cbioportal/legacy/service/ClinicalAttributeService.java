package org.cbioportal.legacy.service;

import org.cbioportal.legacy.model.ClinicalAttribute;
import org.cbioportal.legacy.model.ClinicalAttributeCount;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.service.exception.ClinicalAttributeNotFoundException;
import org.cbioportal.legacy.service.exception.StudyNotFoundException;

import java.util.List;

public interface ClinicalAttributeService extends AttributeByStudyService {

    List<ClinicalAttribute> getAllClinicalAttributes(String projection, Integer pageSize, Integer pageNumber,
                                                     String sortBy, String direction);

    BaseMeta getMetaClinicalAttributes();

    ClinicalAttribute getClinicalAttribute(String studyId, String clinicalAttributeId)
        throws ClinicalAttributeNotFoundException, StudyNotFoundException;

    List<ClinicalAttribute> getAllClinicalAttributesInStudy(String studyId, String projection, Integer pageSize,
                                                     Integer pageNumber, String sortBy, String direction) throws StudyNotFoundException;

    BaseMeta getMetaClinicalAttributesInStudy(String studyId) throws StudyNotFoundException;

    List<ClinicalAttribute> fetchClinicalAttributes(List<String> studyIds, String projection);

	BaseMeta fetchMetaClinicalAttributes(List<String> studyIds);

    List<ClinicalAttributeCount> getClinicalAttributeCountsBySampleIds(List<String> studyIds, List<String> sampleIds);

    List<ClinicalAttributeCount> getClinicalAttributeCountsBySampleListId(String sampleListId);
    
}
