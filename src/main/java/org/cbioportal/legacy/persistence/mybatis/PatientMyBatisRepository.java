package org.cbioportal.legacy.persistence.mybatis;

import org.cbioportal.legacy.model.Patient;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.persistence.PatientRepository;
import org.cbioportal.legacy.persistence.PersistenceConstants;
import org.cbioportal.legacy.persistence.mybatis.util.PaginationCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public class PatientMyBatisRepository implements PatientRepository {

    @Autowired
    private PatientMapper patientMapper;

    @Override
    public List<Patient> getAllPatients(String keyword, String projection, Integer pageSize, Integer pageNumber,
            String sortBy, String direction) {
        return patientMapper.getPatients(null, null, keyword, projection, pageSize,
            PaginationCalculator.offset(pageSize, pageNumber), sortBy, direction);
    }

    @Override
    public BaseMeta getMetaPatients(String keyword) {

        return patientMapper.getMetaPatients(null, null, keyword);
    }

    @Override
    public List<Patient> getAllPatientsInStudy(String studyId, String projection, Integer pageSize, Integer pageNumber,
                                               String sortBy, String direction) {

        return patientMapper.getPatients(Arrays.asList(studyId), null, null, projection, pageSize,
            PaginationCalculator.offset(pageSize, pageNumber), sortBy, direction);
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
