package org.cbioportal.persistence.clickhouse.mapper;

import java.util.List;

import org.cbioportal.model.ClinicalEventSample;
import org.cbioportal.model.Treatment;

public interface TreatmentMapper {
	List<Treatment> getAllTreatments(List<String> sampleIds, List<String> studyIds, String key);

    List<ClinicalEventSample> getAllSamples(List<String> sampleIds, List<String> studyIds);
    
    List<ClinicalEventSample> getAllShallowSamples(List<String> sampleIds, List<String> studyIds);

    Boolean hasTreatmentData(List<String> sampleIds, List<String> studyIds, String key);

    Boolean hasSampleTimelineData(List<String> sampleIds, List<String> studyIds);
}
