package org.cbioportal.persistence.clickhouse;

import java.util.ArrayList;
import java.util.List;

import org.cbioportal.model.Patient;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.PatientRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("clickhouse")
public class PatientClickHouseRepository implements PatientRepository {

	@Override
	public List<Patient> getAllPatients(String keyword, String projection, Integer pageSize, Integer pageNumber,
			String sortBy, String direction) {
		// TODO Auto-generated method stub
		return new ArrayList<Patient>();
	}

	@Override
	public BaseMeta getMetaPatients(String keyword) {
		// TODO Auto-generated method stub
		return new BaseMeta();
	}

	@Override
	public List<Patient> getAllPatientsInStudy(String studyId, String projection, Integer pageSize, Integer pageNumber,
			String sortBy, String direction) {
		// TODO Auto-generated method stub
		return new ArrayList<Patient>();
	}

	@Override
	public BaseMeta getMetaPatientsInStudy(String studyId) {
		// TODO Auto-generated method stub
		return new BaseMeta();
	}

	@Override
	public Patient getPatientInStudy(String studyId, String patientId) {
		// TODO Auto-generated method stub
		return new Patient();
	}

	@Override
	public List<Patient> fetchPatients(List<String> studyIds, List<String> patientIds, String projection) {
		// TODO Auto-generated method stub
		return new ArrayList<Patient>();
	}

	@Override
	public BaseMeta fetchMetaPatients(List<String> studyIds, List<String> patientIds) {
		// TODO Auto-generated method stub
		return new BaseMeta();
	}

	@Override
	public List<Patient> getPatientsOfSamples(List<String> studyIds, List<String> sampleIds) {
		// TODO Auto-generated method stub
		return new ArrayList<Patient>();
	}

}
