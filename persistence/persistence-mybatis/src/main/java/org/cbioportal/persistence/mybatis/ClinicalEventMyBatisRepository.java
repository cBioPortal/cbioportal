package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.ClinicalEvent;
import org.cbioportal.model.ClinicalEventData;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.ClinicalEventRepository;
import org.cbioportal.persistence.mybatis.util.OffsetCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ClinicalEventMyBatisRepository implements ClinicalEventRepository {

    @Autowired
    private ClinicalEventMapper clinicalEventMapper;
    @Autowired
    private OffsetCalculator offsetCalculator;
    
    @Override
    public List<ClinicalEvent> getAllClinicalEventsOfPatientInStudy(String studyId, String patientId, String projection, 
                                                                    Integer pageSize, Integer pageNumber, String sortBy, 
                                                                    String direction) {
        return clinicalEventMapper.getPatientClinicalEvent(studyId, patientId, projection, pageSize, 
            offsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
    }

    @Override
    public BaseMeta getMetaPatientClinicalEvents(String studyId, String patientId) {
        
        return clinicalEventMapper.getMetaPatientClinicalEvent(studyId, patientId);
    }

    @Override
    public List<ClinicalEventData> getDataOfClinicalEvents(List<Integer> clinicalEventIds) {
        
        return clinicalEventMapper.getDataOfClinicalEvents(clinicalEventIds);
    }

    @Override
    public List<ClinicalEvent> getAllClinicalEventsInStudy(String studyId, String projection, Integer pageSize,
                                                           Integer pageNumber, String sortBy, String direction) {
        return clinicalEventMapper.getStudyClinicalEvent(studyId, projection, pageSize, 
            offsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
    }

    @Override
    public BaseMeta getMetaClinicalEvents(String studyId) {
        
        return clinicalEventMapper.getMetaClinicalEvent(studyId);
    }
}
