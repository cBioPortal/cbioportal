package org.cbioportal.persistence.clickhouse;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.cbioportal.model.ClinicalEventKeyCode;
import org.cbioportal.model.ClinicalEventSample;
import org.cbioportal.model.Treatment;
import org.cbioportal.persistence.TreatmentRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("clickhouse")
public class TreatmentClickHouseRepository implements TreatmentRepository {

	@Override
	public Map<String, List<Treatment>> getTreatmentsByPatientId(List<String> sampleIds, List<String> studyIds,
			ClinicalEventKeyCode key) {
		// TODO Auto-generated method stub
		return new HashMap<String, List<Treatment>>();
	}

	@Override
	public List<Treatment> getTreatments(List<String> sampleIds, List<String> studyIds, ClinicalEventKeyCode key) {
		// TODO Auto-generated method stub
		return new ArrayList<Treatment>();
	}

	@Override
	public Map<String, List<ClinicalEventSample>> getSamplesByPatientId(List<String> sampleIds, List<String> studyIds) {
		// TODO Auto-generated method stub
		return new HashMap<String, List<ClinicalEventSample>>();
	}

	@Override
	public Map<String, List<ClinicalEventSample>> getShallowSamplesByPatientId(List<String> sampleIds,
			List<String> studyIds) {
		// TODO Auto-generated method stub
		return new HashMap<String, List<ClinicalEventSample>>();
	}

	@Override
	public Boolean hasTreatmentData(List<String> studies, ClinicalEventKeyCode key) {
		// TODO Auto-generated method stub
		return Boolean.FALSE;
	}

	@Override
	public Boolean hasSampleTimelineData(List<String> studies) {
		// TODO Auto-generated method stub
		return Boolean.FALSE;
	}

}
