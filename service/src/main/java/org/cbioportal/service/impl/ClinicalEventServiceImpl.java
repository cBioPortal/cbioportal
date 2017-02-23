package org.cbioportal.service.impl;

import org.cbioportal.model.ClinicalEvent;
import org.cbioportal.model.ClinicalEventData;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.ClinicalEventRepository;
import org.cbioportal.service.ClinicalEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClinicalEventServiceImpl implements ClinicalEventService {
    
    @Autowired
    private ClinicalEventRepository clinicalEventRepository;
    
    @Override
    public List<ClinicalEvent> getAllClinicalEventsOfPatientInStudy(String studyId, String patientId, String projection, 
                                                                    Integer pageSize, Integer pageNumber, String sortBy, 
                                                                    String direction) {

        List<ClinicalEvent> clinicalEvents = clinicalEventRepository.getAllClinicalEventsOfPatientInStudy(studyId,
            patientId, projection, pageSize, pageNumber, sortBy, direction);

        if (!projection.equals("ID")) {

            List<ClinicalEventData> clinicalEventDataList = clinicalEventRepository.getDataOfClinicalEvents(
                clinicalEvents.stream().map(ClinicalEvent::getClinicalEventId).collect(Collectors.toList()));

            clinicalEvents.forEach(c -> c.setAttributes(clinicalEventDataList.stream().filter(a -> 
                a.getClinicalEventId().equals(c.getClinicalEventId())).collect(Collectors.toList())));
        }
        
        return clinicalEvents;
    }

    @Override
    public BaseMeta getMetaPatientClinicalEvents(String studyId, String patientId) {
        
        return clinicalEventRepository.getMetaPatientClinicalEvents(studyId, patientId);
    }
}
