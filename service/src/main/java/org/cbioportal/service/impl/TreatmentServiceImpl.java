package org.cbioportal.service.impl;

import org.cbioportal.model.*;
import org.cbioportal.persistence.TreatmentRepository;
import org.cbioportal.service.TreatmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Service
public class TreatmentServiceImpl implements TreatmentService {
    @Autowired
    TreatmentRepository treatmentRepository;
    
    @Override
    public List<SampleTreatmentRow> getAllSampleTreatmentRows(List<String> sampleIds, List<String> studyIds) {
        Map<String, List<ClinicalEventSample>> samplesByPatient = treatmentRepository.getSamplesByPatientId(sampleIds, studyIds);
        Map<String, List<Treatment>> treatmentsByPatient = treatmentRepository.getTreatmentsByPatientId(sampleIds, studyIds);

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
        private final Set<ClinicalEventSample> pre, post, unknown;
        private final String treatment;

        TreatmentRowTriplet(List<ClinicalEventSample> samples, String treatment) {
            this.treatment = treatment;
            post = new HashSet<>();
            pre = samples.stream()
                .filter(s -> s.getTimeTaken() != null)
                .collect(Collectors.toSet());
            unknown = samples.stream()
                .filter(s -> s.getTimeTaken() == null)
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
                    new SampleTreatmentRow(TemporalRelation.Post, treatment, post.size(), post),
                    new SampleTreatmentRow(TemporalRelation.Unknown, treatment, unknown.size(), unknown)
            );
        }
    }

    @Override
    public List<PatientTreatmentRow> getAllPatientTreatmentRows(List<String> sampleIds, List<String> studyIds) {
        Map<String, List<Treatment>> treatmentsByPatient = treatmentRepository.getTreatmentsByPatientId(sampleIds, studyIds);
        Map<String, List<ClinicalEventSample>> samplesByPatient = treatmentRepository
            .getSamplesByPatientId(sampleIds, studyIds)
            .entrySet()
            .stream()
            .filter(e -> treatmentsByPatient.containsKey(e.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Set<String> treatments = treatmentRepository.getAllUniqueTreatments(sampleIds, studyIds);
            
        return treatments.stream()
            .map(t -> createPatientTreatmentRowForTreatment(t, treatmentsByPatient, samplesByPatient))
            .collect(Collectors.toList());
    }

    private PatientTreatmentRow createPatientTreatmentRowForTreatment(
        String treatment,
        Map<String, List<Treatment>> treatmentsByPatient,
        Map<String, List<ClinicalEventSample>> samplesByPatient
    ) {
        // find all the patients that have received this treatments
        List<Map.Entry<String, List<Treatment>>> matchingPatients = matchingPatients(treatment, treatmentsByPatient);

        // from those patients, extract the unique samples
        Set<ClinicalEventSample> samples = matchingPatients
            .stream()
            .map(Map.Entry::getKey)
            .flatMap(patient -> samplesByPatient.getOrDefault(patient, new ArrayList<>()).stream())
            .collect(Collectors.toSet());


        return new PatientTreatmentRow(treatment, matchingPatients.size(), samples);
    }

    private List<Map.Entry<String, List<Treatment>>> matchingPatients(
        String treatment,
        Map<String, List<Treatment>> treatmentsByPatient
    ) {
        return treatmentsByPatient.entrySet().stream()
            .filter(p -> p.getValue().stream().anyMatch(t -> t.getTreatment().equals(treatment)))
            .collect(toList());
    }

    @Override
    public Boolean containsTreatmentData(List<String> samples, List<String> studies) {
        return treatmentRepository.getTreatmentCount(samples, studies) > 0;
    }
}
