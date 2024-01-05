package org.cbioportal.persistence.clickhouse;

import java.util.ArrayList;
import java.util.List;

import org.cbioportal.model.SampleList;
import org.cbioportal.model.SampleListToSampleId;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.SampleListRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("clickhouse")
public class SampleListClickHouseRepository implements SampleListRepository {

	@Override
	public List<SampleList> getAllSampleLists(String projection, Integer pageSize, Integer pageNumber, String sortBy,
			String direction) {
		// TODO Auto-generated method stub
		return new ArrayList<SampleList>();
	}

	@Override
	public BaseMeta getMetaSampleLists() {
		// TODO Auto-generated method stub
		return new BaseMeta();
	}

	@Override
	public SampleList getSampleList(String sampleListId) {
		// TODO Auto-generated method stub
		return new SampleList();
	}

	@Override
	public List<SampleList> getSampleLists(List<String> sampleListIds, String projection) {
		// TODO Auto-generated method stub
		return new ArrayList<SampleList>();
	}

	@Override
	public List<SampleList> getAllSampleListsInStudies(List<String> studyIds, String projection, Integer pageSize,
			Integer pageNumber, String sortBy, String direction) {
		// TODO Auto-generated method stub
		return new ArrayList<SampleList>();
	}

	@Override
	public BaseMeta getMetaSampleListsInStudy(String studyId) {
		// TODO Auto-generated method stub
		return new BaseMeta();
	}

	@Override
	public List<String> getAllSampleIdsInSampleList(String sampleListId) {
		// TODO Auto-generated method stub
		return new ArrayList<String>();
	}

	@Override
	public List<SampleListToSampleId> getSampleListSampleIds(List<Integer> sampleListIds) {
		// TODO Auto-generated method stub
		return new ArrayList<SampleListToSampleId>();
	}

}
