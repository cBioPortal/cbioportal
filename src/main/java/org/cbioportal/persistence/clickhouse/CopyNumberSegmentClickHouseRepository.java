package org.cbioportal.persistence.clickhouse;

import java.util.ArrayList;
import java.util.List;

import org.cbioportal.model.CopyNumberSeg;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.CopyNumberSegmentRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("clickhouse")
public class CopyNumberSegmentClickHouseRepository implements CopyNumberSegmentRepository {

	@Override
	public List<CopyNumberSeg> getCopyNumberSegmentsInSampleInStudy(String studyId, String sampleId, String chromosome,
			String projection, Integer pageSize, Integer pageNumber, String sortBy, String direction) {
		// TODO Auto-generated method stub
		return new ArrayList<CopyNumberSeg>();
	}

	@Override
	public BaseMeta getMetaCopyNumberSegmentsInSampleInStudy(String studyId, String sampleId, String chromosome) {
		// TODO Auto-generated method stub
		BaseMeta bm = new BaseMeta();
		bm.setTotalCount(0);
		return bm;
	}

	@Override
	public List<Integer> fetchSamplesWithCopyNumberSegments(List<String> studyIds, List<String> sampleIds,
			String chromosome) {
		// TODO Auto-generated method stub
		ArrayList<Integer> returns = new ArrayList<Integer>();
		returns.add(Integer.valueOf(0));
		return returns;
	}

	@Override
	public List<CopyNumberSeg> fetchCopyNumberSegments(List<String> studyIds, List<String> sampleIds, String chromosome,
			String projection) {
		// TODO Auto-generated method stub
		return new ArrayList<CopyNumberSeg>();
	}

	@Override
	public BaseMeta fetchMetaCopyNumberSegments(List<String> studyIds, List<String> sampleIds, String chromosome) {
		// TODO Auto-generated method stub
		BaseMeta bm = new BaseMeta();
		bm.setTotalCount(0);
		return bm;
	}

	@Override
	public List<CopyNumberSeg> getCopyNumberSegmentsBySampleListId(String studyId, String sampleListId,
			String chromosome, String projection) {
		// TODO Auto-generated method stub
		return new ArrayList<CopyNumberSeg>();
	}

}
