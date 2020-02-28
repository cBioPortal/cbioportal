package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.DatedSample;
import org.cbioportal.model.Treatment;

import java.util.List;

public interface TreatmentMapper {
    List<Treatment> getAllTreatments(List<String> sampleIds, List<String> studyIds);

    List<DatedSample> getAllSamples(List<String> sampleIds, List<String> studyIds);
    
    List<String> getAllUniqueTreatments(List<String> sampleIds, List<String> studyIds);
}
