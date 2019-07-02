package org.cbioportal.persistence;

import org.cbioportal.model.ClinicalEvent;
import org.cbioportal.model.ClinicalEventData;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface ClinicalEventRepository {

    List<ClinicalEvent> getAllClinicalEventsOfPatientInStudy(String studyId, String patientId, String projection,
                                                             Integer pageSize, Integer pageNumber, String sortBy,
                                                             String direction);

    BaseMeta getMetaPatientClinicalEvents(String studyId, String patientId);
    
    List<ClinicalEventData> getDataOfClinicalEvents(List<Integer> clinicalEventIds);

    List<ClinicalEvent> getAllClinicalEventsInStudy(String studyId, String projection, Integer pageSize,
                                                    Integer pageNumber, String sortBy, String direction);
    
    BaseMeta getMetaClinicalEvents(String studyId);
}
