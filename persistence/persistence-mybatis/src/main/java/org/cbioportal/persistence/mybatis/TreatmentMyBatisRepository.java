package org.cbioportal.persistence.mybatis;

import static java.util.stream.Collectors.groupingBy;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cbioportal.model.ClinicalEventSample;
import org.cbioportal.model.Treatment;
import org.cbioportal.persistence.TreatmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class TreatmentMyBatisRepository implements TreatmentRepository {
    @Autowired
    private TreatmentMapper treatmentMapper;
    
    @Override
    public Map<String, List<Treatment>> getTreatmentsByPatientId(List<String> sampleIds, List<String> studyIds) {
        return treatmentMapper.getAllTreatments(sampleIds, studyIds)
            .stream()
            .collect(groupingBy(Treatment::getPatientId));
    }

    @Override
    public Map<String, List<ClinicalEventSample>> getSamplesByPatientId(List<String> sampleIds, List<String> studyIds) {
        return treatmentMapper.getAllSamples(sampleIds, studyIds)
            .stream()
            .distinct()
            .collect(groupingBy(ClinicalEventSample::getPatientId));
    }

    @Override
    public Set<String> getAllUniqueTreatments(List<String> sampleIds, List<String> studyIds) {
        return treatmentMapper.getAllUniqueTreatments(sampleIds, studyIds);
    }

    @Override
    public Integer getTreatmentCount(List<String> samples, List<String> studies) {
        return treatmentMapper.getTreatmentCount(samples, studies);
    }
}
