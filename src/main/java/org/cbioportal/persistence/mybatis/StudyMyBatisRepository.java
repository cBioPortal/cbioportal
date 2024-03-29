package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.CancerStudyTags;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.StudyRepository;
import org.cbioportal.persistence.mybatis.util.PaginationCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class StudyMyBatisRepository implements StudyRepository {

    @Autowired
    private StudyMapper studyMapper;

    @Override
    public List<CancerStudy> getAllStudies(String keyword, String projection, Integer pageSize, Integer pageNumber,
                                           String sortBy, String direction) {

        return studyMapper.getStudies(null, keyword, projection, pageSize, PaginationCalculator.offset(pageSize, pageNumber), 
            sortBy, direction);
    }

    @Override
    public BaseMeta getMetaStudies(String keyword) {
        return studyMapper.getMetaStudies(null, keyword);
    }

    @Override
    public CancerStudy getStudy(String studyId, String projection) {
        return studyMapper.getStudy(studyId, projection);
    }

	@Override
	public List<CancerStudy> fetchStudies(List<String> studyIds, String projection) {
        
        return studyMapper.getStudies(studyIds, null, projection, 0, 0, null, null);
	}

	@Override
	public BaseMeta fetchMetaStudies(List<String> studyIds) {
        
        return studyMapper.getMetaStudies(studyIds, null);
	}
	
	@Override
    public CancerStudyTags getTags(String studyId) {
        return studyMapper.getTags(studyId);
    }

    @Override
    public List<CancerStudyTags> getTagsForMultipleStudies(List<String> studyIds) {
        if (studyIds == null || studyIds.isEmpty()) {
            return new ArrayList<>();
        }
        return studyMapper.getTagsForMultipleStudies(studyIds);
    }
}
