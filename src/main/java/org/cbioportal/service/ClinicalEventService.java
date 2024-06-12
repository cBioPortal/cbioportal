package org.cbioportal.service;

import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalEvent;
import org.cbioportal.model.ClinicalEventTypeCount;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.exception.PatientNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.cbioportal.web.parameter.SurvivalRequest;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ClinicalEventService {
    
    List<ClinicalEvent> getAllClinicalEventsOfPatientInStudy(String studyId, String patientId, String projection, 
                                                             Integer pageSize, Integer pageNumber, String sortBy, 
                                                             String direction) 
        throws PatientNotFoundException, StudyNotFoundException;

    BaseMeta getMetaPatientClinicalEvents(String studyId, String patientId) 
        throws PatientNotFoundException, StudyNotFoundException;
    
    List<ClinicalEvent> getAllClinicalEventsInStudy(String studyId, String projection, Integer pageSize,
                                                    Integer pageNumber, String sortBy, String direction);
    
    BaseMeta getMetaClinicalEvents(String studyId) 
        throws StudyNotFoundException;

    Map<String, Set<String>> getPatientsSamplesPerClinicalEventType(List<String> studyIds, List<String> sampleIds); 
    
    List<ClinicalEventTypeCount> getClinicalEventTypeCounts(List<String> studyIds, List<String> sampleIds);

    List<ClinicalData> getSurvivalData(List<String> studyIds, List<String> patientIds,
                                       String attributeIdPrefix,
                                       SurvivalRequest survivalRequest);

    List<ClinicalEvent> getClinicalEventsMeta(List<String> studyIds, List<String> patientIds,
                                       List<ClinicalEvent> clinicalEvents);
}
