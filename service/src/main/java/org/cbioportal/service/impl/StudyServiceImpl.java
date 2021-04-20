package org.cbioportal.service.impl;

import org.cbioportal.model.*;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.StudyRepository;
import org.cbioportal.service.CancerTypeService;
import org.cbioportal.service.StudyService;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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
        Map<String,CancerStudy> sortedAllStudiesByCancerStudyIdentifier = allStudies.stream().collect(Collectors.toMap(c -> c.getCancerStudyIdentifier(), c -> c, (e1, e2) -> e2, LinkedHashMap::new));

        if (keyword != null && (pageSize == null || allStudies.size() < pageSize)) {
            List<CancerStudy> primarySiteMatchingStudies = findPrimarySiteMatchingStudies(keyword);
            for (CancerStudy cancerStudy : primarySiteMatchingStudies) {
                if (!sortedAllStudiesByCancerStudyIdentifier.containsKey(cancerStudy.getCancerStudyIdentifier())) {
                    sortedAllStudiesByCancerStudyIdentifier.put(cancerStudy.getCancerStudyIdentifier(), cancerStudy);
                }
                if (pageSize != null && sortedAllStudiesByCancerStudyIdentifier.size() == pageSize) {
                    break;
                }
            }
        }
        // For authenticated portals it is essential to make a new list, such
        // that @PostFilter does not taint the list stored in the mybatis
        // second-level cache. When making changes to this make sure to copy the
        // allStudies list at least for the AUTHENTICATE.equals("true") case
        return sortedAllStudiesByCancerStudyIdentifier.values().stream().collect(Collectors.toList());
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

    @Override
    public List<StudyOverlap> getStudiesWithOverlappingSamples(List<CancerStudy> permittedStudies) {
        List<Integer> permittedStudyIds = toStudyIds(permittedStudies);
        List<MultiStudySample> samples = studyRepository.getSamplesBelongingToMultipleStudies(permittedStudyIds);
        // using internal ids to start because I'm pretty sure int comparison is going to be way faster
        Map<Integer, String> internalToExternalIdMapping = internalStudyIdToExternalStudyIdMap(permittedStudies);

        Map<Integer, Set<Integer>> studies = new HashMap<>();
        // all the studies that share this sample overlap
        for (MultiStudySample sample : samples) {
            for (Integer studyId : sample.getStudyIdentifiers()) {
                // so, for each of them, add that list of studies to its overlap set
                if (!studies.containsKey(studyId)) {
                    studies.put(studyId, new HashSet<>());
                }
                studies.get(studyId).addAll(sample.getStudyIdentifiers());
            }
        }

        return studies.entrySet().stream()
            .map(e -> convertEntryToOverlap(internalToExternalIdMapping, e))
            .collect(Collectors.toList());
    }

    private StudyOverlap convertEntryToOverlap(Map<Integer, String> internalToExternalIdMapping, Map.Entry<Integer, Set<Integer>> e) {
        StudyOverlap study = new StudyOverlap(internalToExternalIdMapping.get(e.getKey()));
        e.getValue().remove(e.getKey()); // remove this study from its overlap set
        Set<String> overlap = e.getValue().stream()
            .map(internalToExternalIdMapping::get)
            .collect(Collectors.toSet());
        study.setOverlappingStudyIds(overlap);
        return study;
    }

    private List<Integer> toStudyIds(List<CancerStudy> permittedStudies) {
        return permittedStudies.stream()
            .map(CancerStudy::getCancerStudyId)
            .collect(Collectors.toList());
    }

    private Map<Integer, String> internalStudyIdToExternalStudyIdMap(List<CancerStudy> studies) {
        Map<Integer, String> mapping = new HashMap<>();
        for (CancerStudy study : studies) {
            mapping.put(study.getCancerStudyId(), study.getCancerStudyIdentifier());
        }
        
        return mapping;
    }
}
