package org.cbioportal.service.impl;

import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.CancerStudyTags;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.StudyRepository;
import org.cbioportal.service.StudyService;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;

@Service
public class StudyServiceImpl implements StudyService {

    @Autowired
    private StudyRepository studyRepository;
    @Value("${authenticate:false}")
    private String AUTHENTICATE;

    @Override
    @PostFilter("hasPermission(filterObject, 'read')")
    public List<CancerStudy> getAllStudies(String keyword, String projection, Integer pageSize, Integer pageNumber,
                                           String sortBy, String direction) {

        List<CancerStudy> allStudies = studyRepository.getAllStudies(keyword, projection, pageSize, pageNumber, sortBy, direction);
        // copy the list before returning so @PostFilter doesn't taint the list stored in the mybatis second-level cache
        return (AUTHENTICATE.equals("false")) ? allStudies : new ArrayList<CancerStudy>(allStudies);
    }

    @Override
    public BaseMeta getMetaStudies(String keyword) {
        return studyRepository.getMetaStudies(keyword);
    }

    @Override
    public CancerStudy getStudy(String studyId) throws StudyNotFoundException {

        CancerStudy cancerStudy = studyRepository.getStudy(studyId, "DETAILED");
        if (cancerStudy == null) {
            throw new StudyNotFoundException(studyId);
        }

        return cancerStudy;
    }

    @Override
	public List<CancerStudy> fetchStudies(List<String> studyIds, String projection) {
        
        return studyRepository.fetchStudies(studyIds, projection);
	}

    @Override
	public BaseMeta fetchMetaStudies(List<String> studyIds) {
        
        return studyRepository.fetchMetaStudies(studyIds);
	}
    
    @Override
    public CancerStudyTags getTags(String studyId) {

        return studyRepository.getTags(studyId);
    }
}
