package org.cbioportal.domain.clinical_data_enrichment.usecase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.math.NumberUtils;
import org.cbioportal.domain.clinical_attributes.ClinicalAttribute;
import org.cbioportal.domain.clinical_attributes.repository.ClinicalAttributesRepository;
import org.cbioportal.domain.clinical_attributes.util.ClinicalAttributeUtil;
import org.cbioportal.domain.clinical_attributes.util.ClinicalAttributeUtil.CategorizedClinicalAttributeIds;
import org.cbioportal.domain.clinical_data.ClinicalData;
import org.cbioportal.domain.clinical_data.repository.ClinicalDataRepository;
import org.cbioportal.domain.clinical_data_enrichment.ClinicalDataEnrichment;
import org.cbioportal.domain.clinical_data_enrichment.util.ClinicalDataEnrichmentUtil;
import org.cbioportal.domain.sample.Sample;
import org.cbioportal.domain.sample.repository.SampleRepository;
import org.cbioportal.legacy.model.ClinicalDataCountItem;
import org.cbioportal.legacy.web.parameter.GroupFilter;
import org.cbioportal.legacy.web.parameter.SampleIdentifier;
import org.cbioportal.shared.enums.ProjectionType;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Use case for performing clinical data enrichment analysis.
 *
 * <p>This use case orchestrates the entire enrichment analysis workflow:
 *
 * <ul>
 *   <li>Fetches and groups samples based on the provided filter
 *   <li>Retrieves clinical attributes and categorizes them by type and level
 *   <li>Performs statistical analysis on numerical data (Kruskal-Wallis test)
 *   <li>Performs statistical analysis on categorical data (Chi-squared test)
 * </ul>
 */
@Service
@Profile("clickhouse")
public class FetchClinicalDataEnrichmentsUseCase {

  private final SampleRepository sampleRepository;
  private final ClinicalDataRepository clinicalDataRepository;
  private final ClinicalAttributesRepository clinicalAttributesRepository;

  public FetchClinicalDataEnrichmentsUseCase(
      SampleRepository sampleRepository,
      ClinicalDataRepository clinicalDataRepository,
      ClinicalAttributesRepository clinicalAttributesRepository) {
    this.sampleRepository = sampleRepository;
    this.clinicalDataRepository = clinicalDataRepository;
    this.clinicalAttributesRepository = clinicalAttributesRepository;
  }

  /**
   * Executes the clinical data enrichment analysis for the provided sample groups.
   *
   * @param groupFilter filter containing multiple groups of sample identifiers
   * @return list of clinical data enrichments with statistical test results
   */
  public List<ClinicalDataEnrichment> execute(GroupFilter groupFilter) {

    // Extract all studyIds and sampleIds from all groups
    List<String> allStudyIds =
        groupFilter.getGroups().stream()
            .flatMap(
                group -> group.getSampleIdentifiers().stream().map(SampleIdentifier::getStudyId))
            .toList();

    List<String> allSampleIds =
        groupFilter.getGroups().stream()
            .flatMap(
                group -> group.getSampleIdentifiers().stream().map(SampleIdentifier::getSampleId))
            .toList();

    // Fetch all samples
    List<Sample> samples =
        sampleRepository.fetchSamples(allStudyIds, allSampleIds, ProjectionType.SUMMARY);

    // Build HashMap for fast sample lookup using uniqueId
    Map<String, Sample> sampleLookup = new HashMap<>();
    samples.forEach(
        sample -> {
          String uniqueId = sample.cancerStudyIdentifier() + "_" + sample.stableId();
          sampleLookup.put(uniqueId, sample);
        });

    // Regroup samples according to original group structure
    List<List<Sample>> groupedSamples =
        groupFilter.getGroups().stream()
            .map(
                group ->
                    group.getSampleIdentifiers().stream()
                        .map(
                            sampleIdentifier -> {
                              String uniqueId =
                                  sampleIdentifier.getStudyId()
                                      + "_"
                                      + sampleIdentifier.getSampleId();
                              return sampleLookup.get(uniqueId);
                            })
                        .filter(Objects::nonNull)
                        .toList())
            .filter(validSamples -> !validSamples.isEmpty())
            .toList();

    if (groupedSamples.isEmpty()) {
      return new ArrayList<>();
    }

    // Get unique study IDs and fetch clinical attributes
    List<String> studyIds = samples.stream().map(Sample::cancerStudyIdentifier).distinct().toList();

    List<ClinicalAttribute> allAttributes =
        clinicalAttributesRepository.getClinicalAttributesForStudiesDetailed(studyIds);

    // Remove duplicate attributes based on attrId + patientAttribute
    Map<String, ClinicalAttribute> uniqueAttributeMap =
        allAttributes.stream()
            .collect(
                Collectors.toMap(
                    attr -> attr.attrId() + attr.patientAttribute(),
                    attr -> attr,
                    (existing, replacement) -> replacement));

    List<ClinicalAttribute> uniqueAttributes = new ArrayList<>(uniqueAttributeMap.values());

    // Process enrichments
    List<ClinicalDataEnrichment> enrichments = new ArrayList<>();

    // Process numerical attributes (NUMBER datatype)
    List<ClinicalAttribute> numericalAttributes =
        uniqueAttributes.stream().filter(attr -> "NUMBER".equals(attr.datatype())).toList();

    if (!numericalAttributes.isEmpty()) {
      enrichments.addAll(processNumericalData(numericalAttributes, groupedSamples));
    }

    // Process categorical attributes (STRING datatype)
    List<ClinicalAttribute> categoricalAttributes =
        uniqueAttributes.stream().filter(attr -> "STRING".equals(attr.datatype())).toList();

    if (!categoricalAttributes.isEmpty()) {
      enrichments.addAll(processCategoricalData(categoricalAttributes, groupedSamples));
    }

    return enrichments;
  }

  /**
   * Processes numerical clinical data for enrichment analysis.
   *
   * <p>This method handles all numerical attributes (datatype = NUMBER) by:
   *
   * <ol>
   *   <li>Categorizing attributes by level (sample/patient/conflicting)
   *   <li>Fetching all numerical data in a single optimized query
   *   <li>Regrouping data by sample group and attribute for statistical testing
   *   <li>Performing Kruskal-Wallis test (or Wilcoxon for 2 groups) to identify significant
   *       differences
   * </ol>
   *
   * @param numericalAttributes list of clinical attributes with NUMBER datatype
   * @param groupedSamples list of sample groups to compare
   * @return list of enrichments for numerical attributes with p-values and test statistics
   */
  private List<ClinicalDataEnrichment> processNumericalData(
      List<ClinicalAttribute> numericalAttributes, List<List<Sample>> groupedSamples) {

    // Categorize attributes by level (sample/patient/conflicting)
    CategorizedClinicalAttributeIds categorized =
        ClinicalAttributeUtil.categorizeClinicalAttributes(numericalAttributes);

    List<String> sampleAttributeIds = categorized.sampleAttributeIds();
    List<String> patientAttributeIds = categorized.patientAttributeIds();
    List<String> conflictingAttributeIds = categorized.conflictingAttributeIds();

    // Fetch all clinical data for all groups
    Map<String, Map<String, Double>> allNumericalDataBySampleAndAttributes =
        fetchAllNumericalData(
            groupedSamples, sampleAttributeIds, patientAttributeIds, conflictingAttributeIds);

    // Regroup data by group and attribute
    List<Map<String, List<Double>>> dataByGroupAndAttribute = new ArrayList<>();
    for (List<Sample> groupSamples : groupedSamples) {
      Map<String, List<Double>> groupDataByAttribute = new HashMap<>();
      Map<String, Set<String>> processedPatientsPerAttribute = new HashMap<>();

      for (Sample sample : groupSamples) {
        String sampleUniqueId = buildSampleUniqueId(sample);
        String patientUniqueId = buildPatientUniqueId(sample);
        Map<String, Double> sampleData = allNumericalDataBySampleAndAttributes.get(sampleUniqueId);

        if (sampleData != null) {
          addSampleDataToGroup(
              sampleData,
              patientUniqueId,
              patientAttributeIds,
              groupDataByAttribute,
              processedPatientsPerAttribute);
        }
      }

      dataByGroupAndAttribute.add(groupDataByAttribute);
    }

    // Apply Kruskal-Wallis statistical test
    return new ArrayList<>(
        ClinicalDataEnrichmentUtil.performKruskalWallisTest(
            numericalAttributes, dataByGroupAndAttribute));
  }

  /**
   * Adds all attribute values from a sample to the group data.
   *
   * <p>For patient-level attributes, ensures each patient is counted only once per attribute. For
   * sample-level attributes, all values are included.
   *
   * @param sampleData map of attribute ID -> value for this sample
   * @param patientUniqueId unique ID of the patient for this sample
   * @param patientAttributeIds list of patient-level attribute IDs
   * @param groupDataByAttribute map to accumulate values: attribute ID -> list of values
   * @param processedPatientsPerAttribute tracking map: attribute ID -> set of processed patient IDs
   */
  private void addSampleDataToGroup(
      Map<String, Double> sampleData,
      String patientUniqueId,
      List<String> patientAttributeIds,
      Map<String, List<Double>> groupDataByAttribute,
      Map<String, Set<String>> processedPatientsPerAttribute) {

    for (Map.Entry<String, Double> entry : sampleData.entrySet()) {
      String attributeId = entry.getKey();
      Double value = entry.getValue();

      if (patientAttributeIds.contains(attributeId)) {
        // Patient-level attribute: deduplicate by patient
        Set<String> processedPatients =
            processedPatientsPerAttribute.computeIfAbsent(attributeId, k -> new HashSet<>());

        if (!processedPatients.contains(patientUniqueId)) {
          groupDataByAttribute.computeIfAbsent(attributeId, k -> new ArrayList<>()).add(value);
          processedPatients.add(patientUniqueId);
        }
      } else {
        // Sample-level attribute: add every value
        groupDataByAttribute.computeIfAbsent(attributeId, k -> new ArrayList<>()).add(value);
      }
    }
  }

  /**
   * Processes categorical clinical data for enrichment analysis.
   *
   * <p>This method handles all categorical attributes (datatype = STRING) by:
   *
   * <ol>
   *   <li>Categorizing attributes by level (sample/patient/conflicting)
   *   <li>Fetching categorical data counts for each group
   *   <li>Performing Chi-squared test to identify significant differences in category distributions
   * </ol>
   *
   * @param categoricalAttributes list of clinical attributes with STRING datatype
   * @param groupedSamples list of sample groups to compare
   * @return list of enrichments for categorical attributes with p-values and test statistics
   */
  private List<ClinicalDataEnrichment> processCategoricalData(
      List<ClinicalAttribute> categoricalAttributes, List<List<Sample>> groupedSamples) {

    // Categorize attributes by level (sample/patient/conflicting)
    CategorizedClinicalAttributeIds categorized =
        ClinicalAttributeUtil.categorizeClinicalAttributes(categoricalAttributes);

    List<String> sampleAttributeIds = categorized.sampleAttributeIds();
    List<String> patientAttributeIds = categorized.patientAttributeIds();
    List<String> conflictingAttributeIds = categorized.conflictingAttributeIds();

    // Fetch categorical data counts for all groups
    List<Map<String, ClinicalDataCountItem>> countsByGroupAndAttribute =
        fetchAllCategoricalData(
            groupedSamples, sampleAttributeIds, patientAttributeIds, conflictingAttributeIds);

    // Apply Chi-squared statistical test
    return new ArrayList<>(
        ClinicalDataEnrichmentUtil.performChiSquaredTest(
            categoricalAttributes, countsByGroupAndAttribute));
  }

  /**
   * Fetches all numerical clinical data for all groups.
   *
   * <p>This method collects all unique sample and patient IDs across all groups, performs a single
   * database query to fetch all required clinical data, and returns it indexed by sample unique ID
   * and attribute ID for efficient in-memory regrouping.
   *
   * @param groupedSamples list of sample groups
   * @param sampleAttributeIds sample-level attribute IDs to fetch
   * @param patientAttributeIds patient-level attribute IDs to fetch
   * @param conflictingAttributeIds conflicting attribute IDs to fetch
   * @return map of sample unique ID -> (attribute ID -> value)
   */
  private Map<String, Map<String, Double>> fetchAllNumericalData(
      List<List<Sample>> groupedSamples,
      List<String> sampleAttributeIds,
      List<String> patientAttributeIds,
      List<String> conflictingAttributeIds) {

    // Collect all unique sample and patient IDs across all groups
    List<String> allSampleUniqueIds =
        groupedSamples.stream()
            .flatMap(List::stream)
            .map(this::buildSampleUniqueId)
            .distinct()
            .toList();

    List<String> allPatientUniqueIds =
        groupedSamples.stream()
            .flatMap(List::stream)
            .map(this::buildPatientUniqueId)
            .distinct()
            .toList();

    // Build a map from patient unique ID to all their sample unique IDs
    // This is needed to map patient-level data to samples
    Map<String, List<String>> patientToSampleIds = new HashMap<>();
    for (List<Sample> group : groupedSamples) {
      for (Sample sample : group) {
        String patientUniqueId = buildPatientUniqueId(sample);
        String sampleUniqueId = buildSampleUniqueId(sample);
        patientToSampleIds
            .computeIfAbsent(patientUniqueId, k -> new ArrayList<>())
            .add(sampleUniqueId);
      }
    }

    // Fetch all clinical data in a single optimized query
    List<ClinicalData> allClinicalData =
        clinicalDataRepository.fetchClinicalDataSummaryForEnrichments(
            allSampleUniqueIds,
            allPatientUniqueIds,
            sampleAttributeIds,
            patientAttributeIds,
            conflictingAttributeIds);

    // Build index: sampleUniqueId -> (attributeId -> value)
    return indexClinicalDataBySample(allClinicalData, patientToSampleIds);
  }

  /**
   * Indexes clinical data by sample unique ID and attribute ID.
   *
   * <p>Converts patient-level data to sample-level by mapping each patient's data to all their
   * samples.
   *
   * @param allClinicalData list of clinical data to index
   * @param patientToSampleIds mapping from patient unique ID to their sample unique IDs
   * @return map of sample unique ID -> (attribute ID -> value)
   */
  private Map<String, Map<String, Double>> indexClinicalDataBySample(
      List<ClinicalData> allClinicalData, Map<String, List<String>> patientToSampleIds) {

    Map<String, Map<String, Double>> result = new HashMap<>();

    for (ClinicalData data : allClinicalData) {
      if (!NumberUtils.isCreatable(data.attrValue())) {
        continue; // Skip non-numerical values
      }
      Double value = Double.valueOf(data.attrValue());

      boolean isPatientLevel = data.sampleId() == null;

      if (isPatientLevel) {
        // Patient-level data: map to all samples of this patient
        String patientUniqueId = data.studyId() + "_" + data.patientId();
        List<String> sampleIds = patientToSampleIds.get(patientUniqueId);
        if (sampleIds != null) {
          for (String sampleId : sampleIds) {
            result.computeIfAbsent(sampleId, k -> new HashMap<>()).put(data.attrId(), value);
          }
        }
      } else {
        // Sample-level data
        String sampleUniqueId = data.studyId() + "_" + data.sampleId();
        result.computeIfAbsent(sampleUniqueId, k -> new HashMap<>()).put(data.attrId(), value);
      }
    }

    return result;
  }

  /**
   * Fetches categorical clinical data counts for all groups.
   *
   * @param groupedSamples list of sample groups
   * @param sampleAttributeIds sample-level attribute IDs
   * @param patientAttributeIds patient-level attribute IDs
   * @param conflictingAttributeIds conflicting attribute IDs
   * @return list of maps, one per group, containing attribute ID -> count item
   */
  private List<Map<String, ClinicalDataCountItem>> fetchAllCategoricalData(
      List<List<Sample>> groupedSamples,
      List<String> sampleAttributeIds,
      List<String> patientAttributeIds,
      List<String> conflictingAttributeIds) {

    List<Map<String, ClinicalDataCountItem>> result = new ArrayList<>();

    // For categorical data with counts, the count aggregation is group-specific
    for (List<Sample> groupSamples : groupedSamples) {
      // Build unique IDs for this group
      List<String> sampleUniqueIds = groupSamples.stream().map(this::buildSampleUniqueId).toList();

      List<String> patientUniqueIds =
          groupSamples.stream()
              .map(this::buildPatientUniqueId)
              .distinct() // Remove duplicates since multiple samples can belong to same patient
              .toList();

      // Get clinical data counts for this group
      List<ClinicalDataCountItem> groupCounts =
          clinicalDataRepository.getClinicalDataCountsForEnrichments(
              sampleUniqueIds,
              patientUniqueIds,
              sampleAttributeIds,
              patientAttributeIds,
              conflictingAttributeIds);

      // Convert to map for easy lookup
      result.add(
          groupCounts.stream()
              .collect(Collectors.toMap(ClinicalDataCountItem::getAttributeId, item -> item)));
    }

    return result;
  }

  /**
   * Builds a unique identifier for a sample.
   *
   * @param sample the sample
   * @return unique ID in format "studyId_sampleId"
   */
  private String buildSampleUniqueId(Sample sample) {
    return sample.cancerStudyIdentifier() + "_" + sample.stableId();
  }

  /**
   * Builds a unique identifier for a patient.
   *
   * @param sample the sample (contains patient information)
   * @return unique ID in format "studyId_patientId"
   */
  private String buildPatientUniqueId(Sample sample) {
    return sample.cancerStudyIdentifier() + "_" + sample.patientStableId();
  }
}
