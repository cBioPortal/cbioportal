package org.cbioportal.domain.clinical_data_enrichment.usecase;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.math.NumberUtils;
import org.cbioportal.domain.clinical_attributes.ClinicalAttribute;
import org.cbioportal.domain.clinical_attributes.repository.ClinicalAttributesRepository;
import org.cbioportal.domain.clinical_attributes.util.ClinicalAttributeUtil;
import org.cbioportal.domain.clinical_attributes.util.ClinicalAttributeUtil.CategorizedClinicalAttributeIds;
import org.cbioportal.domain.clinical_data.ClinicalData;
import org.cbioportal.domain.clinical_data.ClinicalDataType;
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
        clinicalAttributesRepository.getClinicalAttributesForStudies(studyIds);

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

  private List<ClinicalDataEnrichment> processNumericalData(
      List<ClinicalAttribute> numericalAttributes, List<List<Sample>> groupedSamples) {

    // Categorize attributes by level (sample/patient/conflicting)
    CategorizedClinicalAttributeIds categorized =
        ClinicalAttributeUtil.categorizeClinicalAttributes(numericalAttributes);

    List<String> sampleAttributeIds = categorized.sampleAttributeIds();
    List<String> patientAttributeIds = categorized.patientAttributeIds();
    List<String> conflictingAttributeIds = categorized.conflictingAttributeIds();

    // Process each group of samples to get numerical clinical data
    List<Map<String, List<Double>>> dataByGroupAndAttribute = new ArrayList<>();

    for (List<Sample> groupSamples : groupedSamples) {
      Map<String, List<Double>> groupData =
          getNumericalClinicalData(
              groupSamples, sampleAttributeIds, patientAttributeIds, conflictingAttributeIds);
      dataByGroupAndAttribute.add(groupData);
    }

    // Apply Kruskal-Wallis statistical test
    return new ArrayList<>(
        ClinicalDataEnrichmentUtil.performKruskalWallisTest(
            numericalAttributes, dataByGroupAndAttribute));
  }

  private List<ClinicalDataEnrichment> processCategoricalData(
      List<ClinicalAttribute> categoricalAttributes, List<List<Sample>> groupedSamples) {

    // Categorize attributes by level (sample/patient/conflicting)
    CategorizedClinicalAttributeIds categorized =
        ClinicalAttributeUtil.categorizeClinicalAttributes(categoricalAttributes);

    List<String> sampleAttributeIds = categorized.sampleAttributeIds();
    List<String> patientAttributeIds = categorized.patientAttributeIds();
    List<String> conflictingAttributeIds = categorized.conflictingAttributeIds();

    // Process each group of samples
    List<Map<String, ClinicalDataCountItem>> countsByGroupAndAttribute = new ArrayList<>();

    for (List<Sample> groupSamples : groupedSamples) {
      Map<String, ClinicalDataCountItem> groupCountMap =
          getCategoricalClinicalDataCounts(
              groupSamples, sampleAttributeIds, patientAttributeIds, conflictingAttributeIds);

      countsByGroupAndAttribute.add(groupCountMap);
    }

    // Apply Chi-squared statistical test
    return new ArrayList<>(
        ClinicalDataEnrichmentUtil.performChiSquaredTest(
            categoricalAttributes, countsByGroupAndAttribute));
  }

  private Map<String, List<Double>> getNumericalClinicalData(
      List<Sample> samples,
      List<String> sampleAttributeIds,
      List<String> patientAttributeIds,
      List<String> conflictingAttributeIds) {

    List<ClinicalData> allClinicalData = new ArrayList<>();

    // Build unique IDs
    List<String> sampleUniqueIds =
        samples.stream().map(s -> s.cancerStudyIdentifier() + "_" + s.stableId()).toList();

    List<String> patientUniqueIds =
        samples.stream()
            .map(s -> s.cancerStudyIdentifier() + "_" + s.patientStableId())
            .distinct()
            .toList();

    // Fetch sample-level clinical data
    if (!sampleAttributeIds.isEmpty()) {
      allClinicalData.addAll(
          clinicalDataRepository.fetchClinicalDataSummary(
              sampleUniqueIds, sampleAttributeIds, ClinicalDataType.SAMPLE));
    }

    // Fetch patient-level clinical data
    if (!patientAttributeIds.isEmpty()) {
      allClinicalData.addAll(
          clinicalDataRepository.fetchClinicalDataSummary(
              patientUniqueIds, patientAttributeIds, ClinicalDataType.PATIENT));
    }

    // Fetch conflicting attributes (patient data mapped to sample level)
    if (!conflictingAttributeIds.isEmpty()) {
      allClinicalData.addAll(
          clinicalDataRepository.fetchClinicalDataSummary(
              patientUniqueIds, conflictingAttributeIds, ClinicalDataType.PATIENT));
    }

    // Filter numerical values and group by attribute
    return allClinicalData.stream()
        .map(
            data ->
                new AbstractMap.SimpleEntry<>(
                    data.attrId(), NumberUtils.toDouble(data.attrValue(), Double.NaN)))
        .filter(entry -> !Double.isNaN(entry.getValue()))
        .collect(
            Collectors.groupingBy(
                AbstractMap.SimpleEntry::getKey,
                Collectors.mapping(AbstractMap.SimpleEntry::getValue, Collectors.toList())));
  }

  private Map<String, ClinicalDataCountItem> getCategoricalClinicalDataCounts(
      List<Sample> samples,
      List<String> sampleAttributeIds,
      List<String> patientAttributeIds,
      List<String> conflictingAttributeIds) {
    // Build unique IDs for this group
    List<String> sampleUniqueIds =
        samples.stream().map(s -> s.cancerStudyIdentifier() + "_" + s.stableId()).toList();

    List<String> patientUniqueIds =
        samples.stream()
            .map(s -> s.cancerStudyIdentifier() + "_" + s.patientStableId())
            .distinct() // Remove duplicates since multiple samples can belong to same patient
            .toList();

    // Get clinical data counts for this group
    List<ClinicalDataCountItem> groupCounts =
        clinicalDataRepository.getClinicalDataCounts(
            sampleUniqueIds,
            patientUniqueIds,
            sampleAttributeIds,
            patientAttributeIds,
            conflictingAttributeIds);

    // Convert to map for easy lookup
    return groupCounts.stream()
        .collect(Collectors.toMap(ClinicalDataCountItem::getAttributeId, item -> item));
  }
}
