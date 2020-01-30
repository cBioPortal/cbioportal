package org.cbioportal.service;

import java.util.List;
import org.cbioportal.model.ClinicalEvent;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.exception.PatientNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;

public interface ClinicalEventService {
    List<ClinicalEvent> getAllClinicalEventsOfPatientInStudy(
        String studyId,
        String patientId,
        String projection,
        Integer pageSize,
        Integer pageNumber,
        String sortBy,
        String direction
    )
        throws PatientNotFoundException, StudyNotFoundException;

    BaseMeta getMetaPatientClinicalEvents(String studyId, String patientId)
        throws PatientNotFoundException, StudyNotFoundException;

    List<ClinicalEvent> getAllClinicalEventsInStudy(
        String studyId,
        String projection,
        Integer pageSize,
        Integer pageNumber,
        String sortBy,
        String direction
    )
        throws StudyNotFoundException;

    BaseMeta getMetaClinicalEvents(String studyId)
        throws StudyNotFoundException;
}
