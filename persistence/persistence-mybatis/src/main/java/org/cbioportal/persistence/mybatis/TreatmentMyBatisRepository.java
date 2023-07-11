package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.*;
import org.cbioportal.persistence.TreatmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;

@Repository
public class TreatmentMyBatisRepository implements TreatmentRepository {
    @Autowired
    private TreatmentMapper treatmentMapper;
    
    @Override
    public Map<String, List<Treatment>> getTreatmentsByPatientId(List<String> sampleIds, List<String> studyIds, ClinicalEventKeyCode key) {
        return getTreatments(sampleIds, studyIds, key)
            .stream()
            .collect(groupingBy(Treatment::getPatientId));
    }

    @Override
    public List<Treatment> getTreatments(List<String> sampleIds, List<String> studyIds, ClinicalEventKeyCode key) {
        return treatmentMapper.getAllTreatments(sampleIds, studyIds, key.getKey())
            .stream()
            .flatMap(treatment -> splitIfDelimited(treatment, key))
            .collect(Collectors.toList());
    }

    private Stream<Treatment> splitIfDelimited(Treatment unsplitTreatment, ClinicalEventKeyCode key) {
        if (key.isDelimited()) {
            return Arrays.stream(unsplitTreatment.getTreatment().split(key.getDelimiter()))
                .map(treatmentName -> {
                    Treatment treatment = new Treatment();
                    treatment.setTreatment(treatmentName);
                    treatment.setStudyId(unsplitTreatment.getStudyId());
                    treatment.setPatientId(unsplitTreatment.getPatientId());
                    treatment.setStart(unsplitTreatment.getStart());
                    treatment.setStop(unsplitTreatment.getStop());
                    treatment.setPatientId(unsplitTreatment.getPatientId());
                    return treatment;
                });
        }
        return Stream.of(unsplitTreatment);
    }

    @Override
    public Map<String, List<ClinicalEventSample>> getSamplesByPatientId(List<String> sampleIds, List<String> studyIds) {
        return treatmentMapper.getAllSamples(sampleIds, studyIds)
            .stream()
            .sorted(Comparator.comparing(ClinicalEventSample::getTimeTaken)) // put earliest events first
            .distinct() // uniqueness determined by sample id, patient id, and study id
            // combined, the sort and distinct produce the earliest clinical event row for each unique sample
            .collect(groupingBy(ClinicalEventSample::getPatientId));
    }

    public Map<String, List<ClinicalEventSample>> getShallowSamplesByPatientId(List<String> sampleIds, List<String> studyIds) {
        return treatmentMapper.getAllShallowSamples(sampleIds, studyIds)
            .stream()
            .distinct()
            .collect(groupingBy(ClinicalEventSample::getPatientId));
    }

    @Override
    public Boolean hasTreatmentData(List<String> studies, ClinicalEventKeyCode key) {
        return treatmentMapper.hasTreatmentData(null, studies, key.getKey());
    }

    @Override
    public Boolean hasSampleTimelineData(List<String> studies) {
        return treatmentMapper.hasSampleTimelineData(null, studies);
    }
}
