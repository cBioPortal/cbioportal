package org.cbioportal.persistence.mysql;

import org.cbioportal.model.ClinicalEvent;
import org.cbioportal.model.ClinicalEventData;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.ClinicalEventRepository;
import org.cbioportal.persistence.mysql.util.OffsetCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Repository
@Profile("mysql")
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

    @Override
    public Map<String, Set<String>> getSamplesOfPatientsPerEventTypeInStudy(List<String> studyIds, List<String> sampleIds) {
        return clinicalEventMapper.getSamplesOfPatientsPerEventType(studyIds, sampleIds)
            .stream()
            .collect(groupingBy(ClinicalEvent::getEventType,
                Collectors.mapping(ClinicalEvent::getUniqueSampleKey, Collectors.toSet())));
    }

    @Override
    public List<ClinicalEvent> getPatientsDistinctClinicalEventInStudies(List<String> studyIds, List<String> patientIds) {
        return clinicalEventMapper.getPatientsDistinctClinicalEventInStudies(studyIds, patientIds);
    }
}
