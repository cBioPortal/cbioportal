package org.cbioportal.persistence;

import org.cbioportal.model.Patient;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface PatientRepository {

    List<Patient> getAllPatients(String keyword, String projection, Integer pageSize, Integer pageNumber, String sortBy,
        String direction);

    BaseMeta getMetaPatients(String keyword);

    List<Patient> getAllPatientsInStudy(String studyId, String projection, Integer pageSize, Integer pageNumber, 
                                        String sortBy, String direction);

    BaseMeta getMetaPatientsInStudy(String studyId);

    Patient getPatientInStudy(String studyId, String patientId);

    List<Patient> fetchPatients(List<String> studyIds, List<String> patientIds, String projection);

    BaseMeta fetchMetaPatients(List<String> studyIds, List<String> patientIds);

    List<Patient> getPatientsOfSamples(List<String> studyIds, List<String> sampleIds);
}
