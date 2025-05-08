package org.cbioportal.legacy.persistence.mybatis;

import java.util.List;
import org.cbioportal.legacy.model.ClinicalEventSample;
import org.cbioportal.legacy.model.Treatment;

public interface TreatmentMapper {
  List<Treatment> getAllTreatments(List<String> sampleIds, List<String> studyIds, String key);

  List<ClinicalEventSample> getAllSamples(List<String> sampleIds, List<String> studyIds);

  List<ClinicalEventSample> getAllShallowSamples(List<String> sampleIds, List<String> studyIds);

  Boolean hasTreatmentData(List<String> sampleIds, List<String> studyIds, String key);

  Boolean hasSampleTimelineData(List<String> sampleIds, List<String> studyIds);
}
