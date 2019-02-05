package org.cbioportal.service.impl;

import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ClinicalAttributeCount;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.ClinicalAttributeRepository;
import org.cbioportal.service.ClinicalAttributeService;
import org.cbioportal.service.StudyService;
import org.cbioportal.service.exception.ClinicalAttributeNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;

@Service
public class ClinicalAttributeServiceImpl implements ClinicalAttributeService {

    @Autowired
    private ClinicalAttributeRepository clinicalAttributeRepository;
    @Autowired
    private StudyService studyService;
    @Value("${authenticate:false}")
    private String AUTHENTICATE;

    @Override
    @PostFilter("hasPermission(filterObject.cancerStudyIdentifier, 'CancerStudy', 'read')")
    public List<ClinicalAttribute> getAllClinicalAttributes(String projection, Integer pageSize, Integer pageNumber,
                                                            String sortBy, String direction) {
        
        List<ClinicalAttribute> clinicalAttributes = clinicalAttributeRepository.getAllClinicalAttributes(projection, pageSize, pageNumber, sortBy,
                                                                                                          direction);
        // copy the list before returning so @PostFilter doesn't taint the list stored in the mybatis second-level cache
        return (AUTHENTICATE.equals("false")) ? clinicalAttributes : new ArrayList<ClinicalAttribute>(clinicalAttributes);
    }

    @Override
    public BaseMeta getMetaClinicalAttributes() {

        return clinicalAttributeRepository.getMetaClinicalAttributes();
    }

    @Override
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
    public List<ClinicalAttribute> getAllClinicalAttributesInStudy(String studyId, String projection, Integer pageSize,
                                                                   Integer pageNumber, String sortBy,
                                                                   String direction) throws StudyNotFoundException {

        studyService.getStudy(studyId);

        return clinicalAttributeRepository.getAllClinicalAttributesInStudy(studyId, projection, pageSize, pageNumber,
                sortBy, direction);
    }

    @Override
    public BaseMeta getMetaClinicalAttributesInStudy(String studyId) throws StudyNotFoundException {

        studyService.getStudy(studyId);

        return clinicalAttributeRepository.getMetaClinicalAttributesInStudy(studyId);
    }

    @Override
    public List<ClinicalAttribute> fetchClinicalAttributes(List<String> studyIds, String projection) {
        
        return clinicalAttributeRepository.fetchClinicalAttributes(studyIds, projection);
        }

    @Override
    public BaseMeta fetchMetaClinicalAttributes(List<String> studyIds) {
        
        return clinicalAttributeRepository.fetchMetaClinicalAttributes(studyIds);
        }

    @Override
    public List<ClinicalAttributeCount> getClinicalAttributeCountsBySampleIds(List<String> studyIds, List<String> sampleIds) {
        
        return clinicalAttributeRepository.getClinicalAttributeCountsBySampleIds(studyIds, sampleIds);
    }

    @Override
    public List<ClinicalAttributeCount> getClinicalAttributeCountsBySampleListId(String sampleListId) {

        return clinicalAttributeRepository.getClinicalAttributeCountsBySampleListId(sampleListId);
    }
}
