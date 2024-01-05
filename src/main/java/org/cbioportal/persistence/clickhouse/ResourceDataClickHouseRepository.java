package org.cbioportal.persistence.clickhouse;

import java.util.ArrayList;
import java.util.List;

import org.cbioportal.model.ResourceData;
import org.cbioportal.persistence.ResourceDataRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("clickhouse")
public class ResourceDataClickHouseRepository implements ResourceDataRepository {

	@Override
	public List<ResourceData> getAllResourceDataOfSampleInStudy(String studyId, String sampleId, String resourceId,
			String projection, Integer pageSize, Integer pageNumber, String sortBy, String direction) {
		// TODO Auto-generated method stub
		return new ArrayList<ResourceData>();
	}

	@Override
	public List<ResourceData> getAllResourceDataOfPatientInStudy(String studyId, String patientId, String resourceId,
			String projection, Integer pageSize, Integer pageNumber, String sortBy, String direction) {
		// TODO Auto-generated method stub
		return new ArrayList<ResourceData>();
	}

	@Override
	public List<ResourceData> getAllResourceDataForStudy(String studyId, String resourceId, String projection,
			Integer pageSize, Integer pageNumber, String sortBy, String direction) {
		// TODO Auto-generated method stub
		return new ArrayList<ResourceData>();
	}

}
