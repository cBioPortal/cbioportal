package org.cbioportal.legacy.persistence.mybatis;

import org.cbioportal.legacy.model.Patient;
import org.cbioportal.legacy.model.meta.BaseMeta;

import java.util.List;

public interface PatientMapper {

    List<Patient> getPatients(List<String> studyIds, List<String> patientIds, String keyword, String projection, 
        Integer limit, Integer offset, String sortBy, String direction);

    BaseMeta getMetaPatients(List<String> studyIds, List<String> patientIds, String keyword);

    Patient getPatient(String studyId, String patientId, String projection);

	List<Patient> getPatientsOfSamples(List<String> studyIds, List<String> sampleIds);
}
