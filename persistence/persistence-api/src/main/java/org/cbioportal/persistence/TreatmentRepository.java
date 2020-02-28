package org.cbioportal.persistence;

import org.cbioportal.model.DatedSample;
import org.cbioportal.model.Treatment;

import java.util.List;

public interface TreatmentRepository {
    public List<Treatment> getAllTreatments(List<String> sampleIds, List<String> studyIds);
    
    public List<DatedSample> getAllSamples(List<String> sampleIds, List<String> studyIds);

    List<String> getAllUniqueTreatments(List<String> sampleIds, List<String> studyIds);
}
