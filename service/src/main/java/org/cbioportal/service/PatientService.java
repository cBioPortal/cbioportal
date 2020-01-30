package org.cbioportal.service;

import java.util.List;
import org.cbioportal.model.Patient;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.exception.PatientNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;

public interface PatientService {
    List<Patient> getAllPatients(
        String keyword,
        String projection,
        Integer pageSize,
        Integer pageNumber,
        String sortBy,
        String direction
    );

    BaseMeta getMetaPatients(String keyword);

    List<Patient> getAllPatientsInStudy(
        String studyId,
        String projection,
        Integer pageSize,
        Integer pageNumber,
        String sortBy,
        String direction
    )
        throws StudyNotFoundException;

    BaseMeta getMetaPatientsInStudy(String studyId)
        throws StudyNotFoundException;

    Patient getPatientInStudy(String studyId, String patientId)
        throws PatientNotFoundException, StudyNotFoundException;

    List<Patient> fetchPatients(
        List<String> studyIds,
        List<String> patientIds,
        String projection
    );

    BaseMeta fetchMetaPatients(List<String> studyIds, List<String> patientIds);

    List<Patient> getPatientsOfSamples(
        List<String> studyIds,
        List<String> sampleIds
    );
}
