package org.cbioportal.service;

import org.cbioportal.model.ClinicalEvent;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.exception.PatientNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;

import java.util.List;

public interface ClinicalEventService {
    
    List<ClinicalEvent> getAllClinicalEventsOfPatientInStudy(String studyId, String patientId, String projection, 
                                                             Integer pageSize, Integer pageNumber, String sortBy, 
                                                             String direction) 
        throws PatientNotFoundException, StudyNotFoundException;

    BaseMeta getMetaPatientClinicalEvents(String studyId, String patientId) 
        throws PatientNotFoundException, StudyNotFoundException;
}
