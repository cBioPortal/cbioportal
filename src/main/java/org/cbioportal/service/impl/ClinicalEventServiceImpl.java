package org.cbioportal.service.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalEvent;
import org.cbioportal.model.ClinicalEventData;
import org.cbioportal.model.ClinicalEventTypeCount;
import org.cbioportal.model.Patient;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.ClinicalEventRepository;
import org.cbioportal.service.ClinicalEventService;
import org.cbioportal.service.PatientService;
import org.cbioportal.service.exception.PatientNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.cbioportal.web.parameter.ClinicalEventRequestIdentifier;
import org.cbioportal.web.parameter.OccurrencePosition;
import org.cbioportal.web.parameter.SurvivalRequest;
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
                                              SurvivalRequest survivalRequest) {
        List<ClinicalEvent> startClinicalEventsMeta = getToClinicalEvents(survivalRequest.getStartEventRequestIdentifier());
        List<ClinicalEvent> patientStartEvents = clinicalEventRepository.getTimelineEvents(studyIds, patientIds, startClinicalEventsMeta);

        // only fetch end timeline events for patients that have endClinicalEventsMeta and start timeline events
        List<ClinicalEvent> patientEndEvents = filterClinicalEvents(patientStartEvents, survivalRequest.getEndEventRequestIdentifier());

        ToIntFunction<ClinicalEvent> startPositionIdentifier = getPositionIdentifier(survivalRequest.getStartEventRequestIdentifier().getPosition());
        ToIntFunction<ClinicalEvent> endPositionIdentifier = getPositionIdentifier(survivalRequest.getEndEventRequestIdentifier().getPosition());
        Map<String, ClinicalEvent> patientEndEventsById = patientEndEvents.stream().collect(Collectors.toMap(ClinicalEventServiceImpl::getKey, Function.identity()));

        // filter out cases where start event is less than end event
        patientStartEvents = patientStartEvents.stream()
            .filter(event ->
                Optional.ofNullable(patientEndEventsById.get(getKey(event)))
                    .map(endPositionIdentifier::applyAsInt)
                    .map(endDate -> startPositionIdentifier.applyAsInt(event) < endDate)
                    .orElse(true)
            ).toList();

        List<ClinicalEvent> patientCensoredEvents = filterClinicalEvents(patientStartEvents, survivalRequest.getCensoredEventRequestIdentifier());
        Map<String, ClinicalEvent> patientCensoredEventsById = patientCensoredEvents.stream().collect(Collectors.toMap(ClinicalEventServiceImpl::getKey, Function.identity()));

        return patientStartEvents.stream()
            .flatMap(event -> {
                ClinicalData clinicalDataMonths = buildClinicalSurvivalMonths(attributeIdPrefix, event, survivalRequest, patientEndEventsById, patientCensoredEventsById);
                if (clinicalDataMonths == null) return Stream.empty();
                ClinicalData clinicalDataStatus = buildClinicalSurvivalStatus(attributeIdPrefix, event, patientEndEventsById);

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

    private static List<ClinicalEvent> getToClinicalEvents(ClinicalEventRequestIdentifier clinicalEventRequestIdentifier) {
        return clinicalEventRequestIdentifier.getClinicalEventRequests().stream().map(x -> {
            ClinicalEvent clinicalEvent = new ClinicalEvent();
            clinicalEvent.setEventType(x.getEventType());
            clinicalEvent.setAttributes(x.getAttributes());

            return clinicalEvent;
        }).toList();
    }

    private ToIntFunction<ClinicalEvent> getPositionIdentifier(OccurrencePosition position) {
        return position.equals(OccurrencePosition.FIRST) ? ClinicalEvent::getStartDate : ClinicalEvent::getStopDate;
    }

    private List<ClinicalEvent> filterClinicalEvents(List<ClinicalEvent> patientEvents,
                                                     ClinicalEventRequestIdentifier clinicalEventRequestIdentifier) {
        List<String> filteredStudyIds = new ArrayList<>();
        List<String> filteredPatientIds = new ArrayList<>();
        for (ClinicalEvent clinicalEvent : patientEvents) {
            filteredStudyIds.add(clinicalEvent.getStudyId());
            filteredPatientIds.add(clinicalEvent.getPatientId());
        }

        List<ClinicalEvent> clinicalEventsMeta = new ArrayList<>();
        if (clinicalEventRequestIdentifier != null) {
            clinicalEventsMeta = getToClinicalEvents(clinicalEventRequestIdentifier);
        }

        // only fetch end timeline events for patients that have endClinicalEventsMeta and start timeline events
        List<ClinicalEvent> queriedPatientEvents = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(filteredStudyIds)) {
            queriedPatientEvents = clinicalEventRepository.getTimelineEvents(filteredStudyIds, filteredPatientIds, clinicalEventsMeta);
        }
        return queriedPatientEvents;
    }

    private ClinicalData buildClinicalSurvivalMonths(String attributeIdPrefix, ClinicalEvent event, SurvivalRequest survivalRequest, Map<String, ClinicalEvent> patientEndEventsById, Map<String, ClinicalEvent> patientCensoredEventsById) {
        final String SURVIVAL_MONTH_ATTRIBUTE = attributeIdPrefix + "_MONTHS";
        ClinicalData clinicalDataMonths = new ClinicalData();
        clinicalDataMonths.setStudyId(event.getStudyId());
        clinicalDataMonths.setPatientId(event.getPatientId());
        clinicalDataMonths.setAttrId(SURVIVAL_MONTH_ATTRIBUTE);

        ToIntFunction<ClinicalEvent> startPositionIdentifier = getPositionIdentifier(survivalRequest.getStartEventRequestIdentifier().getPosition());
        ToIntFunction<ClinicalEvent> endPositionIdentifier = survivalRequest.getEndEventRequestIdentifier() == null ? ClinicalEvent::getStopDate : getPositionIdentifier(survivalRequest.getEndEventRequestIdentifier().getPosition());
        ToIntFunction<ClinicalEvent> censoredPositionIdentifier = survivalRequest.getCensoredEventRequestIdentifier() == null ? ClinicalEvent::getStopDate : getPositionIdentifier(survivalRequest.getCensoredEventRequestIdentifier().getPosition());

        int startDate = startPositionIdentifier.applyAsInt(event);
        int endDate;
        if (patientEndEventsById.containsKey(getKey(event))) {
            endDate = endPositionIdentifier.applyAsInt(patientEndEventsById.get(getKey(event)));
        } else {
            // ignore cases where patient does not have censored timeline events or
            // stop date of start event is less than start date of censored events
            if (!patientCensoredEventsById.containsKey(getKey(event)) ||
                startDate >= censoredPositionIdentifier.applyAsInt(patientCensoredEventsById.get(getKey(event)))
            ) {
                return null;
            }

            endDate = censoredPositionIdentifier.applyAsInt(patientCensoredEventsById.get(getKey(event)));
        }
        final String SURVIVAL_MONTH = String.valueOf((endDate - startDate) / 30.4);
        clinicalDataMonths.setAttrValue(SURVIVAL_MONTH);

        return clinicalDataMonths;
    }

    private ClinicalData buildClinicalSurvivalStatus(String attributeIdPrefix, ClinicalEvent event, Map<String, ClinicalEvent> patientEndEventsById) {

        ClinicalData clinicalDataStatus = new ClinicalData();
        clinicalDataStatus.setStudyId(event.getStudyId());
        clinicalDataStatus.setPatientId(event.getPatientId());
        clinicalDataStatus.setAttrId(attributeIdPrefix + "_STATUS");

        if (patientEndEventsById.containsKey(getKey(event))) {
            clinicalDataStatus.setAttrValue("1:EVENT");
        } else {
            clinicalDataStatus.setAttrValue("0:CENSORED");
        }

        return clinicalDataStatus;
    }
}
