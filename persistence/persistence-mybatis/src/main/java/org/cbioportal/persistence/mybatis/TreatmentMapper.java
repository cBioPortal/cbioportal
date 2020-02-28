package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.DatedSample;
import org.cbioportal.model.Treatment;

import java.util.List;
import java.util.Set;

public interface TreatmentMapper {
    List<Treatment> getAllTreatments(List<String> sampleIds, List<String> studyIds, Set<String> treatments);

    List<DatedSample> getAllSamples(List<String> sampleIds, List<String> studyIds);
    
    Set<String> getAllUniqueTreatments(List<String> sampleIds, List<String> studyIds);
}
