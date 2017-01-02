package org.cbioportal.service;

import org.cbioportal.model.Patient;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.exception.PatientNotFoundException;

import java.util.List;

public interface PatientService {

    List<Patient> getAllPatientsInStudy(String studyId, String projection, Integer pageSize, Integer pageNumber,
                                        String sortBy, String direction);

    BaseMeta getMetaPatientsInStudy(String studyId);

    Patient getPatientInStudy(String studyId, String patientId) throws PatientNotFoundException;

    List<Patient> fetchPatients(List<String> studyIds, List<String> patientIds, String projection);

    BaseMeta fetchMetaPatients(List<String> studyIds, List<String> patientIds);
}
