package org.cbioportal.service.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalEvent;
import org.cbioportal.model.ClinicalEventData;
import org.cbioportal.model.ClinicalEventTypeCount;
import org.cbioportal.model.Patient;
import org.cbioportal.model.SurvivalEvent;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.ClinicalEventRepository;
import org.cbioportal.service.ClinicalEventService;
import org.cbioportal.service.PatientService;
import org.cbioportal.service.exception.PatientNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ClinicalEventServiceImpl implements ClinicalEventService {
    
    @Autowired
    private ClinicalEventRepository clinicalEventRepository;
    @Autowired
    private PatientService patientService;
    
    @Override
    public List<ClinicalEvent> getAllClinicalEventsOfPatientInStudy(String studyId, String patientId, String projection, 
                                                                    Integer pageSize, Integer pageNumber, String sortBy, 
                                                                    String direction) throws PatientNotFoundException, 
        StudyNotFoundException {
        
        patientService.getPatientInStudy(studyId, patientId);

        List<ClinicalEvent> clinicalEvents = clinicalEventRepository.getAllClinicalEventsOfPatientInStudy(studyId,
            patientId, projection, pageSize, pageNumber, sortBy, direction);

        if (!projection.equals("ID") && !clinicalEvents.isEmpty() ) {

            List<ClinicalEventData> clinicalEventDataList = clinicalEventRepository.getDataOfClinicalEvents(
                clinicalEvents.stream().map(ClinicalEvent::getClinicalEventId).collect(Collectors.toList()));

            clinicalEvents.forEach(c -> c.setAttributes(clinicalEventDataList.stream().filter(a -> 
                a.getClinicalEventId().equals(c.getClinicalEventId())).collect(Collectors.toList())));
        }
        
        return clinicalEvents;
    }

    @Override
    public BaseMeta getMetaPatientClinicalEvents(String studyId, String patientId) throws PatientNotFoundException, 
        StudyNotFoundException {

        patientService.getPatientInStudy(studyId, patientId);
        
        return clinicalEventRepository.getMetaPatientClinicalEvents(studyId, patientId);
    }

    @Override
    public List<ClinicalEvent> getAllClinicalEventsInStudy(String studyId, String projection, Integer pageSize,
                                                           Integer pageNumber, String sortBy, String direction)
        {

        List<ClinicalEvent> clinicalEvents = clinicalEventRepository.getAllClinicalEventsInStudy(studyId,
            projection, pageSize, pageNumber, sortBy, direction);

        if (!projection.equals("ID")) {

            List<ClinicalEventData> clinicalEventDataList = clinicalEventRepository.getDataOfClinicalEvents(
                clinicalEvents.stream().map(ClinicalEvent::getClinicalEventId).collect(Collectors.toList()));

            clinicalEvents.forEach(c -> c.setAttributes(clinicalEventDataList.stream().filter(a -> 
                a.getClinicalEventId().equals(c.getClinicalEventId())).collect(Collectors.toList())));
        }
        
        return clinicalEvents;
    }

    @Override
    public BaseMeta getMetaClinicalEvents(String studyId) throws StudyNotFoundException {
        return clinicalEventRepository.getMetaClinicalEvents(studyId);
    }

    @Override
    public Map<String, Set<String>> getPatientsSamplesPerClinicalEventType(List<String> studyIds, List<String> sampleIds) {

        return clinicalEventRepository.getSamplesOfPatientsPerEventTypeInStudy(studyIds, sampleIds);
    }

    @Override
    public List<ClinicalEventTypeCount> getClinicalEventTypeCounts(List<String> studyIds, List<String> sampleIds) {
        
        List<Patient> patients = patientService.getPatientsOfSamples(studyIds, sampleIds);
        
        List<String> studies = patients.stream().map(Patient::getCancerStudyIdentifier)
            .collect(Collectors.toList());
        List<String> patientIds = patients.stream().map(Patient::getStableId)
            .collect(Collectors.toList());
        
        List<ClinicalEvent> clinicalEvents = clinicalEventRepository.getPatientsDistinctClinicalEventInStudies(studies, patientIds);
        
        Map<String, Integer> clinicalEventTypeCountMap = new HashMap<>();
        for(ClinicalEvent e : clinicalEvents) {
            clinicalEventTypeCountMap.
                put(e.getEventType(), 
                    clinicalEventTypeCountMap.getOrDefault(e.getEventType(),0) + 1);
        }
        
        return clinicalEventTypeCountMap.entrySet()
            .stream()
            .map(e -> new ClinicalEventTypeCount(e.getKey(),e.getValue()))
            .collect(Collectors.toList());
    }

    @Override
    public List<ClinicalData> getSurvivalData(List<String> studyIds,
                                              List<String> patientIds,
                                              String attributeIdPrefix,
                                              SurvivalEvent survivalEvent) {
        List<ClinicalEvent> startClinicalEventsMeta = survivalEvent.getStartClinicalEventsMeta();
        ToIntFunction<ClinicalEvent> startPositionIdentifier = survivalEvent.getStartPositionIdentifier();
        List<ClinicalEvent> endClinicalEventsMeta = survivalEvent.getEndClinicalEventsMeta();
        ToIntFunction<ClinicalEvent> endPositionIdentifier = survivalEvent.getEndPositionIdentifier();
        List<ClinicalEvent> censoredClinicalEventsMeta = survivalEvent.getCensoredClinicalEventsMeta();
        ToIntFunction<ClinicalEvent> censoredPositionIdentifier = survivalEvent.getCensoredPositionIdentifier();

        List<ClinicalEvent> patientStartEvents = clinicalEventRepository.getTimelineEvents(studyIds, patientIds, startClinicalEventsMeta);

        List<String> filteredStudyIds = new ArrayList<>();
        List<String> filteredPatientIds = new ArrayList<>();
        for (ClinicalEvent clinicalEvent : patientStartEvents) {
            filteredStudyIds.add(clinicalEvent.getStudyId());
            filteredPatientIds.add(clinicalEvent.getPatientId());
        }

        // only fetch end timeline events for patients that have endClinicalEventsMeta and start timeline events
        List<ClinicalEvent> patientEndEvents = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(endClinicalEventsMeta) && CollectionUtils.isNotEmpty(filteredStudyIds)) {
            patientEndEvents = clinicalEventRepository.getTimelineEvents(filteredStudyIds, filteredPatientIds, endClinicalEventsMeta);
        }

        Map<String, ClinicalEvent> patientEndEventsById = patientEndEvents.stream().collect(Collectors.toMap(ClinicalEventServiceImpl::getKey, Function.identity()));

        // filter out cases where start event is less than end event
        patientStartEvents = patientStartEvents.stream()
            .filter(event ->
                Optional.ofNullable(patientEndEventsById.get(getKey(event)))
                    .map(endPositionIdentifier::applyAsInt)
                    .map(endDate -> startPositionIdentifier.applyAsInt(event) < endDate)
                    .orElse(true)
            ).toList();

        filteredStudyIds.clear();
        filteredPatientIds.clear();
        // get all the studies that have start events but not end events
        for (ClinicalEvent clinicalEvent : patientStartEvents.stream().filter(event -> !patientEndEventsById.containsKey(getKey(event))).toList()) {
            filteredStudyIds.add(clinicalEvent.getStudyId());
            filteredPatientIds.add(clinicalEvent.getPatientId());
        }

        List<ClinicalEvent> patientCensoredEvents = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(filteredStudyIds)) {
            patientCensoredEvents = clinicalEventRepository.getTimelineEvents(filteredStudyIds, filteredPatientIds, censoredClinicalEventsMeta);
        }

        Map<String, ClinicalEvent> patientCensoredEventsById = patientCensoredEvents.stream().collect(Collectors.toMap(ClinicalEventServiceImpl::getKey, Function.identity()));

        return patientStartEvents.stream()
            .flatMap(event -> {
                ClinicalData clinicalDataMonths = new ClinicalData();
                clinicalDataMonths.setStudyId(event.getStudyId());
                clinicalDataMonths.setPatientId(event.getPatientId());
                clinicalDataMonths.setAttrId(attributeIdPrefix + "_MONTHS");

                ClinicalData clinicalDataStatus = new ClinicalData();
                clinicalDataStatus.setStudyId(event.getStudyId());
                clinicalDataStatus.setPatientId(event.getPatientId());
                clinicalDataStatus.setAttrId(attributeIdPrefix + "_STATUS");

                int startDate = startPositionIdentifier.applyAsInt(event);
                int endDate;
                if (patientEndEventsById.containsKey(getKey(event))) {
                    clinicalDataStatus.setAttrValue("1:EVENT");
                    endDate = endPositionIdentifier.applyAsInt(patientEndEventsById.get(getKey(event)));
                } else {
                    // ignore cases where patient does not have censored timeline events or
                    // stop date of start event is less than start date of censored events
                    if (!patientCensoredEventsById.containsKey(getKey(event)) ||
                        startDate >= censoredPositionIdentifier.applyAsInt(patientCensoredEventsById.get(getKey(event)))
                    ) {
                        return Stream.empty();
                    }

                    clinicalDataStatus.setAttrValue("0:CENSORED");
                    endDate = censoredPositionIdentifier.applyAsInt(patientCensoredEventsById.get(getKey(event)));
                }
                clinicalDataMonths.setAttrValue(String.valueOf((endDate - startDate) / 30.4));

                return Stream.of(clinicalDataMonths, clinicalDataStatus);
            }).toList();
    }

    @Override
    public List<ClinicalEvent> getClinicalEventsMeta(List<String> studyIds, List<String> patientIds, List<ClinicalEvent> clinicalEvents) {
        return clinicalEventRepository.getClinicalEventsMeta(studyIds, patientIds, clinicalEvents);
    }

    private static String getKey(ClinicalEvent clinicalEvent) {
        return clinicalEvent.getStudyId() + clinicalEvent.getPatientId();
    }
}
