package org.cbioportal.persistence.clickhouse;

import java.util.Arrays;
import java.util.List;

import org.cbioportal.model.Patient;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.PatientRepository;
import org.cbioportal.persistence.PersistenceConstants;
import org.cbioportal.persistence.clickhouse.mapper.PatientMapper;
import org.cbioportal.persistence.clickhouse.util.OffsetCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("clickhouse")
public class PatientClickHouseRepository implements PatientRepository {
	
    @Autowired
    private PatientMapper patientMapper;

	@Override
	public List<Patient> getAllPatients(String keyword, String projection, Integer pageSize, Integer pageNumber,
			String sortBy, String direction) {
        return patientMapper.getPatients(null, null, keyword, projection, pageSize,
                OffsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
	}

	@Override
	public BaseMeta getMetaPatients(String keyword) {
		return patientMapper.getMetaPatients(null, null, keyword);
	}

	@Override
	public List<Patient> getAllPatientsInStudy(String studyId, String projection, Integer pageSize, Integer pageNumber,
			String sortBy, String direction) {
        return patientMapper.getPatients(Arrays.asList(studyId), null, null, projection, pageSize,
                OffsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
	}

	@Override
	public BaseMeta getMetaPatientsInStudy(String studyId) {
		return patientMapper.getMetaPatients(Arrays.asList(studyId), null, null);
	}

	@Override
	public Patient getPatientInStudy(String studyId, String patientId) {
		return patientMapper.getPatient(studyId, patientId, PersistenceConstants.DETAILED_PROJECTION);
	}

	@Override
	public List<Patient> fetchPatients(List<String> studyIds, List<String> patientIds, String projection) {
		return patientMapper.getPatients(studyIds, patientIds, null, projection, 0, 0, null, null);
	}

	@Override
	public BaseMeta fetchMetaPatients(List<String> studyIds, List<String> patientIds) {
		return patientMapper.getMetaPatients(studyIds, patientIds, null);
	}

	@Override
	public List<Patient> getPatientsOfSamples(List<String> studyIds, List<String> sampleIds) {
		return patientMapper.getPatientsOfSamples(studyIds, sampleIds);
	}

}
