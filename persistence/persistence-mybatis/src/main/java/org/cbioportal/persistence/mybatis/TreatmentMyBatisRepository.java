package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.DatedSample;
import org.cbioportal.model.Treatment;
import org.cbioportal.persistence.TreatmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TreatmentMyBatisRepository implements TreatmentRepository {
    @Autowired
    private TreatmentMapper treatmentMapper;
    
    @Override
    public List<Treatment> getAllTreatments(List<String> sampleIds, List<String> studyIds) {
        return treatmentMapper.getAllTreatments(sampleIds, studyIds);
    }

    @Override
    public List<DatedSample> getAllSamples(List<String> sampleIds, List<String> studyIds) {
        return treatmentMapper.getAllSamples(sampleIds, studyIds);
    }

    @Override
    public List<String> getAllUniqueTreatments(List<String> sampleIds, List<String> studyIds) {
        return treatmentMapper.getAllUniqueTreatments(sampleIds, studyIds);
    }
}
