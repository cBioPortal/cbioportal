package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.DatedSample;
import org.cbioportal.model.Treatment;
import org.cbioportal.persistence.TreatmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.groupingBy;

@Repository
public class TreatmentMyBatisRepository implements TreatmentRepository {
    @Autowired
    private TreatmentMapper treatmentMapper;
    
    @Override
    public Map<String, List<Treatment>> getTreatmentsByPatient(List<String> sampleIds, List<String> studyIds, Set<String> treatments) {
        return treatmentMapper.getAllTreatments(sampleIds, studyIds, treatments)
            .stream()
            .sorted(Comparator.comparingInt(Treatment::getStart))
            .collect(groupingBy(Treatment::getPatientId));
    }

    @Override
    public Map<String, List<DatedSample>> getSamplesByPatient(List<String> sampleIds, List<String> studyIds) {
        return treatmentMapper.getAllSamples(sampleIds, studyIds)
            .stream()
            .collect(groupingBy(DatedSample::getPatientId));
    }

    @Override
    public Set<String> getAllUniqueTreatments(List<String> sampleIds, List<String> studyIds) {
        return treatmentMapper.getAllUniqueTreatments(sampleIds, studyIds);
    }
}
