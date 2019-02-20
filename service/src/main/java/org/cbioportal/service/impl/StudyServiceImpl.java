package org.cbioportal.service.impl;

import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.CancerStudyTags;
import org.cbioportal.model.TypeOfCancer;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.StudyRepository;
import org.cbioportal.service.CancerTypeService;
import org.cbioportal.service.StudyService;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Service
public class StudyServiceImpl implements StudyService {

    @Autowired
    private StudyRepository studyRepository;
    @Autowired
    private CancerTypeService cancerTypeService;
    @Value("${authenticate:false}")
    private String AUTHENTICATE;

    @Override
    @PostFilter("hasPermission(filterObject, 'read')")
    public List<CancerStudy> getAllStudies(String keyword, String projection, Integer pageSize, Integer pageNumber,
                                           String sortBy, String direction) {

        List<CancerStudy> allStudies = studyRepository.getAllStudies(keyword, projection, pageSize, pageNumber, sortBy, direction);
        if (keyword != null && (pageSize == null || allStudies.size() < pageSize)) {
            List<CancerStudy> primarySiteMatchingStudies = findPrimarySiteMatchingStudies(keyword);
            if (pageSize != null) {
                int toIndex = primarySiteMatchingStudies.size() > pageSize - allStudies.size() ? 
                    pageSize - allStudies.size() : primarySiteMatchingStudies.size();
                primarySiteMatchingStudies = primarySiteMatchingStudies.subList(0, toIndex);
            }
            for (CancerStudy cancerStudy : primarySiteMatchingStudies) {
                if (!allStudies.stream().anyMatch(c -> c.getCancerStudyIdentifier().equals(cancerStudy.getCancerStudyIdentifier()))) {
                    allStudies.add(cancerStudy);
                }
            }
        }
        // copy the list before returning so @PostFilter doesn't taint the list stored in the mybatis second-level cache
        return (AUTHENTICATE.equals("false")) ? allStudies : new ArrayList<CancerStudy>(allStudies);
    }

    @Override
    public BaseMeta getMetaStudies(String keyword) {
        if (keyword == null) {
            return studyRepository.getMetaStudies(keyword);
        }
        else {
            BaseMeta baseMeta = new BaseMeta();
            baseMeta.setTotalCount(getAllStudies(keyword, "SUMMARY", null, null, null, null).size());
            return baseMeta;
        }
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

    @Override
    public List<CancerStudyTags> getTagsForMultipleStudies(List<String> studyIds) {

        return studyRepository.getTagsForMultipleStudies(studyIds);
    }

    private List<CancerStudy> findPrimarySiteMatchingStudies(String keyword) {

        List<CancerStudy> matchingStudies = new ArrayList<>();

        List<String> matchingCancerTypes = new ArrayList<>();
        for (Map.Entry<String, TypeOfCancer> entry : cancerTypeService.getPrimarySiteMap().entrySet()) {
            if (entry.getValue().getTypeOfCancerId().toLowerCase().contains(keyword.toLowerCase()) || 
                entry.getValue().getName().toLowerCase().contains(keyword.toLowerCase())) {
                matchingCancerTypes.add(entry.getKey());
            }
        }
        if (!matchingCancerTypes.isEmpty()) {
            List<CancerStudy> allUnfilteredStudies = studyRepository.getAllStudies(null, "SUMMARY", null, null, null, null);
            for (CancerStudy cancerStudy : allUnfilteredStudies) {
                if (matchingCancerTypes.contains(cancerStudy.getTypeOfCancerId())) {
                    matchingStudies.add(cancerStudy);
                }
            }
        }
        return matchingStudies;
    }
}
