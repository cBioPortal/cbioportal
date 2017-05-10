package org.cbioportal.service.impl;

import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.ClinicalAttributeRepository;
import org.cbioportal.service.ClinicalAttributeService;
import org.cbioportal.service.StudyService;
import org.cbioportal.service.exception.ClinicalAttributeNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClinicalAttributeServiceImpl implements ClinicalAttributeService {

    @Autowired
    private ClinicalAttributeRepository clinicalAttributeRepository;
    @Autowired
    private StudyService studyService;

    @Override
    @PostFilter("hasPermission(filterObject.cancerStudyIdentifier, 'CancerStudy', 'read')")
    public List<ClinicalAttribute> getAllClinicalAttributes(String projection, Integer pageSize, Integer pageNumber,
                                                            String sortBy, String direction) {

        return clinicalAttributeRepository.getAllClinicalAttributes(projection, pageSize, pageNumber, sortBy,
                direction);
    }

    @Override
    public BaseMeta getMetaClinicalAttributes() {

        return clinicalAttributeRepository.getMetaClinicalAttributes();
    }

    @Override
    @PreAuthorize("hasPermission(#studyId, 'CancerStudy', 'read')")
    public ClinicalAttribute getClinicalAttribute(String studyId, String clinicalAttributeId)
        throws ClinicalAttributeNotFoundException, StudyNotFoundException {

        studyService.getStudy(studyId);

        ClinicalAttribute clinicalAttribute = clinicalAttributeRepository.getClinicalAttribute(studyId,
                clinicalAttributeId);

        if (clinicalAttribute == null) {
            throw new ClinicalAttributeNotFoundException(studyId, clinicalAttributeId);
        }

        return clinicalAttribute;
    }

    @Override
    @PreAuthorize("hasPermission(#studyId, 'CancerStudy', 'read')")
    public List<ClinicalAttribute> getAllClinicalAttributesInStudy(String studyId, String projection, Integer pageSize,
                                                                   Integer pageNumber, String sortBy,
                                                                   String direction) throws StudyNotFoundException {

        studyService.getStudy(studyId);

        return clinicalAttributeRepository.getAllClinicalAttributesInStudy(studyId, projection, pageSize, pageNumber,
                sortBy, direction);
    }

    @Override
    @PreAuthorize("hasPermission(#studyId, 'CancerStudy', 'read')")
    public BaseMeta getMetaClinicalAttributesInStudy(String studyId) throws StudyNotFoundException {

        studyService.getStudy(studyId);

        return clinicalAttributeRepository.getMetaClinicalAttributesInStudy(studyId);
    }
}
