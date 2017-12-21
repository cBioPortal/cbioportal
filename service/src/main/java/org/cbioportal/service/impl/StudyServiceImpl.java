package org.cbioportal.service.impl;

import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.StudyRepository;
import org.cbioportal.service.StudyService;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudyServiceImpl implements StudyService {

    @Autowired
    private StudyRepository studyRepository;

    @Override
    @PostFilter("hasPermission(filterObject, 'read')")
    public List<CancerStudy> getAllStudies(String projection, Integer pageSize, Integer pageNumber,
                                           String sortBy, String direction) {

        return studyRepository.getAllStudies(projection, pageSize, pageNumber, sortBy, direction);
    }

    @Override
    public BaseMeta getMetaStudies() {
        return studyRepository.getMetaStudies();
    }

    @Override
    @PreAuthorize("hasPermission(#studyId, 'CancerStudy', 'read')")
    public CancerStudy getStudy(String studyId) throws StudyNotFoundException {

        CancerStudy cancerStudy = studyRepository.getStudy(studyId);
        if (cancerStudy == null) {
            throw new StudyNotFoundException(studyId);
        }

        return cancerStudy;
    }

    @Override
    @PreAuthorize("hasPermission(#studyIds, 'List<CancerStudyId>', 'read')")
	public List<CancerStudy> fetchStudies(List<String> studyIds, String projection) {
        
        return studyRepository.fetchStudies(studyIds, projection);
	}

    @Override
    @PreAuthorize("hasPermission(#studyIds, 'List<CancerStudyId>', 'read')")
	public BaseMeta fetchMetaStudies(List<String> studyIds) {
        
        return studyRepository.fetchMetaStudies(studyIds);
	}
}
