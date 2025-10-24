package org.cbioportal.application.file.repositories;

import java.util.List;
import java.util.Set;
import org.cbioportal.application.file.model.ClinicalAttribute;
import org.cbioportal.application.file.model.ClinicalAttributeValue;
import org.cbioportal.application.file.model.ClinicalEvent;
import org.cbioportal.application.file.model.ClinicalEventData;
import org.cbioportal.application.file.utils.CloseableIterator;

/**
 * Repository interface for retrieving clinical attribute data, clinical event data, and related
 * information. This interface provides methods to access clinical sample and patient attributes,
 * their values, and clinical events associated with specific studies and samples.
 */
public interface ClinicalAttributeDataRepository {

  /**
   * Retrieves clinical sample attributes for a given study.
   *
   * @param studyId the identifier of the study
   * @return a list of clinical sample attributes
   */
  List<ClinicalAttribute> getClinicalSampleAttributes(String studyId);

  /**
   * Retrieves clinical sample attribute values for a given study and set of sample IDs.
   *
   * @param studyId the identifier of the study
   * @param sampleIds a set of sample IDs to filter the attribute values; null set means all samples
   * @return an iterator containing clinical attribute values for the specified samples
   */
  CloseableIterator<ClinicalAttributeValue> getClinicalSampleAttributeValues(
      String studyId, Set<String> sampleIds);

  /**
   * Retrieves clinical patient attributes for a given study.
   *
   * @param studyId the identifier of the study
   * @return a list of clinical patient attributes
   */
  List<ClinicalAttribute> getClinicalPatientAttributes(String studyId);

  /**
   * Retrieves clinical patient attribute values for a given study and set of sample IDs.
   *
   * @param studyId the identifier of the study
   * @param sampleIds a set of sample IDs to filter the attribute values; null set means all samples
   * @return an iterator containing clinical patient attribute values for the specified samples
   */
  CloseableIterator<ClinicalAttributeValue> getClinicalPatientAttributeValues(
      String studyId, Set<String> sampleIds);

  /**
   * Checks if clinical patient attributes exist for the specified study and sample IDs.
   *
   * @param studyId
   * @param sampleIds
   * @return true if clinical patient attributes exist, false otherwise
   */
  boolean hasClinicalPatientAttributes(String studyId, Set<String> sampleIds);

  /**
   * Checks if clinical sample attributes exist for the specified study and sample IDs.
   *
   * @param studyId
   * @param sampleIds
   * @return true if clinical sample attributes exist, false otherwise
   */
  boolean hasClinicalSampleAttributes(String studyId, Set<String> sampleIds);

  /**
   * Checks if clinical timeline data exists for the specified study and sample IDs.
   *
   * @param studyId
   * @param sampleIds
   * @return true if clinical timeline data exists, false otherwise
   */
  boolean hasClinicalTimelineData(String studyId, Set<String> sampleIds);

  /**
   * Retrieves distinct clinical event keys for a given study and event type.
   *
   * @param studyId the identifier of the study
   * @param eventType the type of clinical event
   * @return a list of distinct clinical event keys
   */
  List<String> getDistinctClinicalEventKeys(String studyId, String eventType);

  /**
   * Retrieves clinical event data for a given study, event type, and set of sample IDs.
   *
   * @param studyId the identifier of the study
   * @param eventType the type of clinical event
   * @param sampleIds a set of sample IDs to filter the event data; null set means all samples
   * @return an iterator containing clinical event data for the specified parameters
   */
  CloseableIterator<ClinicalEventData> getClinicalEventData(
      String studyId, String eventType, Set<String> sampleIds);

  /**
   * Retrieves clinical events for a given study, event type, and set of sample IDs.
   *
   * @param studyId
   * @param eventType
   * @param sampleIds
   * @return an iterator containing clinical events for the specified parameters
   */
  CloseableIterator<ClinicalEvent> getClinicalEvents(
      String studyId, String eventType, Set<String> sampleIds);

  /**
   * Retrieves distinct clinical event types for a given study.
   *
   * @param studyId the identifier of the study
   * @return a list of distinct clinical event types
   */
  List<String> getDistinctEventTypes(String studyId);
}
