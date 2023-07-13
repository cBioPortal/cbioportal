package org.cbioportal.service.impl;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.cbioportal.model.*;
import org.cbioportal.persistence.TreatmentRepository;
import org.cbioportal.service.TreatmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TreatmentServiceImpl implements TreatmentService {
    @Autowired
    TreatmentRepository treatmentRepository;
    
    private Pair<List<String>, List<String>> filterIds(List<String> sampleIds, List<String> studyIds, ClinicalEventKeyCode key) {
        if (sampleIds == null || studyIds == null || sampleIds.size() != studyIds.size()) {
            return new ImmutablePair<>(sampleIds, studyIds);
        }
        Set<String> studiesWithTreatments = studyIds.stream()
            .distinct()
            .filter(studyId -> treatmentRepository.hasTreatmentData(Collections.singletonList(studyId), key))
            .collect(Collectors.toSet());
        
        ArrayList<String> filteredSampleIds = new ArrayList<>();
        ArrayList<String> filteredStudyIds = new ArrayList<>();
        for (int i = 0; i < sampleIds.size(); i++) {
            String studyId = studyIds.get(i);
            String sampleId = sampleIds.get(i);
            if (studiesWithTreatments.contains(studyId)) {
                filteredSampleIds.add(sampleId);
                filteredStudyIds.add(studyId);
            }
        }
        return new ImmutablePair<>(filteredSampleIds, filteredStudyIds);
    }

    @Override
    public List<SampleTreatmentRow> getAllSampleTreatmentRows(List<String> sampleIds, List<String> studyIds, ClinicalEventKeyCode key) {
        Pair<List<String>, List<String>> filteredIds = filterIds(sampleIds, studyIds, key);
        sampleIds = filteredIds.getLeft();
        studyIds = filteredIds.getRight();

        Map<String, List<ClinicalEventSample>> samplesByPatient =
            treatmentRepository.getSamplesByPatientId(sampleIds, studyIds);
        Map<String, List<Treatment>> treatmentsByPatient =
            treatmentRepository.getTreatmentsByPatientId(sampleIds, studyIds, key);

        Stream<SampleTreatmentRow> rows = samplesByPatient.keySet().stream()
            .flatMap(patientId -> getSampleTreatmentRowsForPatient(patientId, samplesByPatient, treatmentsByPatient))
            .filter(row -> row.getCount() != 0);
        return flattenRows(rows);
    }
    
    private Stream<SampleTreatmentRow> getSampleTreatmentRowsForPatient(
            String patientId,
            Map<String, List<ClinicalEventSample>> samplesByPatient,
            Map<String, List<Treatment>> treatmentsByPatient
    ) {
        List<Treatment> treatments = treatmentsByPatient.getOrDefault(patientId, new ArrayList<>());
        List<ClinicalEventSample> samples = samplesByPatient.get(patientId);

        Map<String, TreatmentRowTriplet> rows = new HashMap<>();

        for (Treatment treatment : treatments) {
            TreatmentRowTriplet triplet;
            
            if (!rows.containsKey(treatment.getTreatment())) {
                triplet = new TreatmentRowTriplet(samples, treatment.getTreatment());
                rows.put(treatment.getTreatment(), triplet);
            } else {
                triplet = rows.get(treatment.getTreatment());
            }
            
            triplet.moveSamplesToPost(treatment);
        }

        return rows.values().stream().flatMap(TreatmentRowTriplet::toRows);
    }

    private List<SampleTreatmentRow> flattenRows(Stream<SampleTreatmentRow> rows) {
        Map<String, SampleTreatmentRow> uniqueRows = new HashMap<>();
        rows.forEach(rowToAdd -> {
            if (uniqueRows.containsKey(rowToAdd.key())) {
                uniqueRows.get(rowToAdd.key()).add(rowToAdd);
            } else {
                uniqueRows.put(rowToAdd.key(), rowToAdd);
            }
        });

        return new ArrayList<>(uniqueRows.values());
    }


    /**
     * For a given treatment, you can have samples that are taken
     * before (pre), after (post), or that don't have a date (unknown)
     * 
     * This class accepts an initial list of samples and a treatment.
     * At the start, all samples are considered pre, as there hasn't been
     * any treatment start / stop times.
     * 
     * You then call moveSamplesToPost on this with a series of matching
     * treatments. Each call will move samples taken 
     */
    private static class TreatmentRowTriplet {
        private final Set<ClinicalEventSample> pre, post;
        private final String treatment;

        TreatmentRowTriplet(List<ClinicalEventSample> samples, String treatment) {
            this.treatment = treatment;
            post = new HashSet<>();
            pre = samples.stream()
                .filter(s -> s.getTimeTaken() != null)
                .collect(Collectors.toSet());
        }

        /**
         * Moves any samples marked as pre that were taken after the treatment
         * started to post.
         * 
         * @param treatment a treatment with a start value. It is assumed that
         *                  the treatment matches the treatment stored in this triplet
         */
        void moveSamplesToPost(Treatment treatment) {
            for (Iterator<ClinicalEventSample> iterator = pre.iterator(); iterator.hasNext(); ) {
                ClinicalEventSample clinicalEventSample = iterator.next();
                // edge case: is a sample taken the same day a treatment starts pre or post?
                // We're saying pre here
                if (clinicalEventSample.getTimeTaken() > treatment.getStart()) {
                    iterator.remove();
                    post.add(clinicalEventSample);
                }
            }
        }
        
        Stream<SampleTreatmentRow> toRows() {
            return Stream.of(
                    new SampleTreatmentRow(TemporalRelation.Pre, treatment, pre.size(), pre),
                    new SampleTreatmentRow(TemporalRelation.Post, treatment, post.size(), post)
                    // We made the decision to filter out unknown rows. I'm leaving this line of code
                    // to document this decision.
                    // new SampleTreatmentRow(TemporalRelation.Unknown, treatment, unknown.size(), unknown)
            );
        }
    }
    @Override
    public List<PatientTreatmentRow> getAllPatientTreatmentRows(
        List<String> sampleIds, List<String> studyIds, ClinicalEventKeyCode key
    ) {
        Pair<List<String>, List<String>> filteredIds = filterIds(sampleIds, studyIds, key);
        sampleIds = filteredIds.getLeft();
        studyIds = filteredIds.getRight();

        Map<String, List<ClinicalEventSample>> samplesByPatient = treatmentRepository
            .getShallowSamplesByPatientId(sampleIds, studyIds);

        Map<String, List<Treatment>> treatmentSet = treatmentRepository.getTreatments(sampleIds, studyIds, key)
            .stream()
            .collect(Collectors.groupingBy(Treatment::getTreatment));

        /*
            This logic transforms treatmentSet to list of PatientTreatmentRow. transformation steps:
            - key in treatmentSet is going to be treatment
            - get all unique patient ids -> this is going to give count
            - get all clinicalEventSamples using above unique patient ids
         */
        return treatmentSet.entrySet()
            .stream()
            .map(entry -> {
                String treatment = entry.getKey();
                Set<String> patientIds = entry.getValue().stream().map(Treatment::getPatientId).collect(Collectors.toSet());
                Set<ClinicalEventSample> clinicalEventSamples = patientIds
                    .stream()
                    .flatMap(patientId -> samplesByPatient.getOrDefault(patientId, new ArrayList<>()).stream())
                    .collect(Collectors.toSet());
                return new PatientTreatmentRow(treatment, patientIds.size(), clinicalEventSamples);
            })
            .collect(Collectors.toList());
    }

    @Override
    public Boolean containsTreatmentData(List<String> studies, ClinicalEventKeyCode key) {
        return treatmentRepository.hasTreatmentData(studies, key);
    }

    @Override
    public Boolean containsSampleTreatmentData(List<String> studyIds, ClinicalEventKeyCode key) {
        studyIds = studyIds.stream()
            .filter(studyId -> treatmentRepository.hasTreatmentData(Collections.singletonList(studyId), key))
            .collect(Collectors.toList());
        return studyIds.size() > 0 && treatmentRepository.hasSampleTimelineData(studyIds);
    }
}
