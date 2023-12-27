package org.cbioportal.persistence.clickhouse;

import java.util.List;

import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.CancerStudyTags;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.StudyRepository;
import org.cbioportal.persistence.clickhouse.mapper.StudyMapper;
import org.cbioportal.persistence.clickhouse.util.OffsetCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("clickhouse")
public class StudyClickHouseRepository implements StudyRepository {
	
	@Autowired
	private StudyMapper studyMapper;

	@Override
	public List<CancerStudy> getAllStudies(String keyword, String projection, Integer pageSize, Integer pageNumber,
			String sortBy, String direction) {
		return studyMapper.getStudies(null, keyword, projection, pageSize, OffsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
	}

	@Override
	public BaseMeta getMetaStudies(String keyword) {
		return studyMapper.getMetaStudies(null, keyword);
	}

	@Override
	public CancerStudy getStudy(String studyId, String projection) {
		// TODO Auto-generated method stub
		return new CancerStudy();
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CancerStudyTags> getTagsForMultipleStudies(List<String> studyIds) {
		// TODO Auto-generated method stub
		return null;
	}

}
