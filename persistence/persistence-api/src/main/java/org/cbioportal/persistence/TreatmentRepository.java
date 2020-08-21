package org.cbioportal.persistence;

import org.cbioportal.model.ClinicalEventSample;
import org.cbioportal.model.Treatment;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface TreatmentRepository {
    public Map<String, List<Treatment>> getTreatmentsByPatientId(List<String> sampleIds, List<String> studyIds);
    
    public Map<String, List<ClinicalEventSample>> getSamplesByPatientId(List<String> sampleIds, List<String> studyIds);

    Set<String> getAllUniqueTreatments(List<String> sampleIds, List<String> studyIds);

    Integer getTreatmentCount(List<String> samples, List<String> studies);
}
