package org.cbioportal.persistence.clickhouse;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.cbioportal.model.ClinicalEvent;
import org.cbioportal.model.ClinicalEventData;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.ClinicalEventRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("clickhouse")
public class ClinicalEventClickHouseRepository implements ClinicalEventRepository {

	@Override
	public List<ClinicalEvent> getAllClinicalEventsOfPatientInStudy(String studyId, String patientId, String projection,
			Integer pageSize, Integer pageNumber, String sortBy, String direction) {
		// TODO Auto-generated method stub
		return new ArrayList<ClinicalEvent>();
	}

	@Override
	public BaseMeta getMetaPatientClinicalEvents(String studyId, String patientId) {
		// TODO Auto-generated method stub
		return new BaseMeta();
	}

	@Override
	public List<ClinicalEventData> getDataOfClinicalEvents(List<Integer> clinicalEventIds) {
		// TODO Auto-generated method stub
		return new ArrayList<ClinicalEventData>();
	}

	@Override
	public List<ClinicalEvent> getAllClinicalEventsInStudy(String studyId, String projection, Integer pageSize,
			Integer pageNumber, String sortBy, String direction) {
		// TODO Auto-generated method stub
		return new ArrayList<ClinicalEvent>();
	}

	@Override
	public BaseMeta getMetaClinicalEvents(String studyId) {
		// TODO Auto-generated method stub
		return new BaseMeta();
	}

	@Override
	public Map<String, Set<String>> getSamplesOfPatientsPerEventTypeInStudy(List<String> studyIds,
			List<String> sampleIds) {
		// TODO Auto-generated method stub
		return new HashMap<String, Set<String>>();
	}

	@Override
	public List<ClinicalEvent> getPatientsDistinctClinicalEventInStudies(List<String> studyIds,
			List<String> patientIds) {
		// TODO Auto-generated method stub
		return new ArrayList<ClinicalEvent>();
	}

}
