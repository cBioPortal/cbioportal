package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.Patient;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.PatientRepository;
import org.cbioportal.persistence.PersistenceConstants;
import org.cbioportal.persistence.mybatis.util.OffsetCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.accept.PathExtensionContentNegotiationStrategy;

import java.util.Arrays;
import java.util.List;

@Repository
public class PatientMyBatisRepository implements PatientRepository {

    @Autowired
    private PatientMapper patientMapper;
    @Autowired
    private OffsetCalculator offsetCalculator;

    @Override
    public List<Patient> getAllPatientsInStudy(String studyId, String projection, Integer pageSize, Integer pageNumber,
                                               String sortBy, String direction) {

        return patientMapper.getPatients(Arrays.asList(studyId), null, projection, pageSize,
            offsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
    }

    @Override
    public BaseMeta getMetaPatientsInStudy(String studyId) {

        return patientMapper.getMetaPatients(Arrays.asList(studyId), null);
    }

    @Override
    public Patient getPatientInStudy(String studyId, String patientId) {

        return patientMapper.getPatient(studyId, patientId, PersistenceConstants.DETAILED_PROJECTION);
    }

    @Override
    public List<Patient> fetchPatients(List<String> studyIds, List<String> patientIds, String projection) {

        return patientMapper.getPatients(studyIds, patientIds, projection, 0, 0, null, null);
    }

    @Override
    public BaseMeta fetchMetaPatients(List<String> studyIds, List<String> patientIds) {

        return patientMapper.getMetaPatients(studyIds, patientIds);
    }

	@Override
	public List<Patient> getPatientsOfSamples(List<String> studyIds, List<String> sampleIds) {
        
        return patientMapper.getPatientsOfSamples(studyIds, sampleIds);
	}
}
