package org.cbioportal.service.impl;

import org.cbioportal.model.*;
import org.cbioportal.persistence.TreatmentRepository;
import org.cbioportal.service.TreatmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Service
public class TreatmentServiceImpl implements TreatmentService {
    @Autowired
    TreatmentRepository treatmentRepository;
    
    @Override
    public List<TreatmentRow> getAllTreatmentRows(List<String> sampleIds, List<String> studyIds) {
        Map<Integer, List<DatedSample>> samplesByPatient = treatmentRepository.getAllSamples(sampleIds, studyIds)
            .stream()
            .collect(groupingBy(DatedSample::getPatientId));

        Map<Integer, List<Treatment>> treatmentsByPatient = treatmentRepository.getAllTreatments(sampleIds, studyIds)
            .stream()
            .sorted(Comparator.comparingInt(Treatment::getStart))
            .collect(groupingBy(Treatment::getPatientId));

        return flattenAndSortRows(samplesByPatient.keySet().stream()
            .flatMap(patientId -> {
                List<Treatment> pTreatments = treatmentsByPatient.getOrDefault(patientId, new ArrayList<>());
                List<DatedSample> pSamples = samplesByPatient.get(patientId);
                return processPatient(pSamples, pTreatments);
            })
            .filter(row -> row.getCount() != 0));
    }
    
    private List<TreatmentRow> flattenAndSortRows(Stream<TreatmentRow> rows) {
        Map<String, TreatmentRow> uniqueRows = new HashMap<>();
        rows.forEach(rowToAdd -> {
            if (uniqueRows.containsKey(rowToAdd.getTreatment() + rowToAdd.getTime().name())) {
                TreatmentRow row = uniqueRows.get(rowToAdd.getTreatment() + rowToAdd.getTime().name());
                row.setCount(row.getCount() + rowToAdd.getCount());
            } else {
                uniqueRows.put(rowToAdd.getTreatment() + rowToAdd.getTime().name(), rowToAdd);
            }
        });

        List<TreatmentRow> flattenedRows = new ArrayList<>(uniqueRows.values());
        flattenedRows.sort(Comparator.comparing(a -> (a.getTreatment() + a.getTime().name())));
        return flattenedRows;
    }

    /**
     * 
     * @param samples a list of samples for a single patient
     * @param treatments a list of treatments for the same single patient
     * @return a stream of the resulting rows. For each unique treatment name in treatment
     * there will be 3 rows: pre, post, and unknown. Rows with a count of zero will be included
     */
    private Stream<TreatmentRow> processPatient(List<DatedSample> samples, List<Treatment> treatments) {
        Map<String, TreatmentRowTriplet> rows = new HashMap<>();

        for (Treatment treatment : treatments) {
            if (!rows.containsKey(treatment.getTreatment())) {
                rows.put(treatment.getTreatment(), new TreatmentRowTriplet(samples, treatment.getTreatment()));
            }
            TreatmentRowTriplet triplet = rows.get(treatment.getTreatment());
            triplet.movePostSamples(treatment);
        }
        
        return rows.values().stream().flatMap(TreatmentRowTriplet::toRows);
    }
    
    
    private static class TreatmentRowTriplet {
        private List<DatedSample> pre, post, unknown;
        private final String treatment;

        TreatmentRowTriplet(List<DatedSample> samples, String treatment) {
            this.treatment = treatment;
            post = new ArrayList<>();
            pre = samples.stream()
                .filter(s -> s.getTimeTaken() != null)
                .collect(toList());
            unknown = samples.stream()
                .filter(s -> s.getTimeTaken() == null)
                .collect(toList());
        }

        /**
         * Moves any samples marked as pre that were taken after the treatment
         * started to post.
         * 
         * @param treatment a treatment with a start value. It is assumed that
         *                  the treatment matches the treatment stored in this triplet
         */
        void movePostSamples(Treatment treatment) {
            for (Iterator<DatedSample> iterator = pre.iterator(); iterator.hasNext(); ) {
                DatedSample datedSample = iterator.next();
                // edge case: is a sample taken the same day a treatment starts pre or post?
                // We're saying pre here
                if (datedSample.getTimeTaken() > treatment.getStart()) {
                    iterator.remove();
                    post.add(datedSample);
                }
            }
        }
        
        Stream<TreatmentRow> toRows() {
            return Stream.of(
                    new TreatmentRow(TemporalRelation.Pre, treatment, pre.size()),
                    new TreatmentRow(TemporalRelation.Post, treatment, post.size()),
                    new TreatmentRow(TemporalRelation.Unknown, treatment, unknown.size())
            );
        }
    }
    
}
