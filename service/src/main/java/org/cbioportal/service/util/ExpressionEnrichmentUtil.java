package org.cbioportal.service.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.inference.OneWayAnova;
import org.apache.commons.math3.stat.inference.TestUtils;
import org.cbioportal.model.ExpressionEnrichment;
import org.cbioportal.model.GenericAssayEnrichment;
import org.cbioportal.model.GenericAssayMolecularAlteration;
import org.cbioportal.model.GenomicEnrichment;
import org.cbioportal.model.GroupStatistics;
import org.cbioportal.model.MolecularAlteration;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.model.MolecularProfileSamples;
import org.cbioportal.model.Sample;
import org.cbioportal.persistence.MolecularDataRepository;
import org.cbioportal.service.SampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExpressionEnrichmentUtil {

	@Autowired
	private SampleService sampleService;
	@Autowired
	private MolecularDataRepository molecularDataRepository;

	private static final double LOG2 = Math.log(2);
	private static final String RNA_SEQ = "rna_seq";

	public <T extends MolecularAlteration, S extends ExpressionEnrichment> List<S> getEnrichments(
			MolecularProfile molecularProfile,
			Map<String, List<MolecularProfileCaseIdentifier>> molecularProfileCaseSets, String enrichmentType,
			Iterable<T> maItr) {
		List<S> expressionEnrichments = new ArrayList<>();

		Map<String, List<Integer>> groupIndicesMap = getGroupIndicesMap(molecularProfileCaseSets, enrichmentType,
				molecularProfile);
		for (MolecularAlteration ma : maItr) {

			List<GroupStatistics> groupsStatistics = new ArrayList<GroupStatistics>();
			// used for p-value calculation
			List<double[]> groupedValues = new ArrayList<double[]>();

			for (Entry<String, List<Integer>> group : groupIndicesMap.entrySet()) {

				// get expression values to all the indices in the group
				List<String> molecularDataValues = group.getValue().stream()
						.map(sampleIndex -> ma.getSplitValues()[sampleIndex]).collect(Collectors.toList());

				// ignore group if there are less than 2 values or non numeric values
				if (molecularDataValues.size() < 2
						|| molecularDataValues.stream().filter(a -> !NumberUtils.isNumber(a)).count() > 0) {
					continue;
				}

				double[] values = getAlterationValues(molecularDataValues, molecularProfile.getStableId());

				GroupStatistics groupStatistics = new GroupStatistics();
				double alteredMean = StatUtils.mean(values);
				double alteredStandardDeviation = calculateStandardDeviation(values);

				// ignore if mean or standard deviation are not numbers
				if (Double.isNaN(alteredMean) || Double.isNaN(alteredStandardDeviation)) {
					continue;
				}

				groupedValues.add(values);
				groupStatistics.setName(group.getKey());
				groupStatistics.setMeanExpression(BigDecimal.valueOf(alteredMean));
				groupStatistics.setStandardDeviation(BigDecimal.valueOf(alteredStandardDeviation));
				groupsStatistics.add(groupStatistics);
			}

			// calculate p-value and add enrichment if atleast 2 groups have data
			if (groupsStatistics.size() > 1) {
				double pValue = calculatePValue(groupedValues);
				if (Double.isNaN(pValue)) {
					continue;
				}
				S expressionEnrichment = null;
				if (ma instanceof GenericAssayMolecularAlteration) {
					GenericAssayEnrichment genericAssayEnrichment = new GenericAssayEnrichment();
					genericAssayEnrichment.setStableId(ma.getStableId());
					expressionEnrichment = (S) genericAssayEnrichment;
				} else {
					GenomicEnrichment genomicEnrichment = new GenomicEnrichment();
					genomicEnrichment.setEntrezGeneId(Integer.valueOf(ma.getStableId()));
					expressionEnrichment = (S) genomicEnrichment;
				}
				expressionEnrichment.setpValue(BigDecimal.valueOf(pValue));
				expressionEnrichment.setGroupsStatistics(groupsStatistics);
				expressionEnrichments.add(expressionEnrichment);
			}
		}
		return expressionEnrichments;
	}

	private double[] getAlterationValues(List<String> molecularDataValues, String molecularProfileId) {

		if (molecularProfileId.contains(RNA_SEQ)) {
			return molecularDataValues.stream().mapToDouble(d -> {
				double datum = Double.parseDouble(d);
				// reset to 0 if there are any negative values and then do log1p
				return Math.log1p(datum < 0 ? 0 : datum) / LOG2;
			}).toArray();
		} else {
			return molecularDataValues.stream().mapToDouble(g -> Double.parseDouble(g)).toArray();
		}
	}

	private double calculatePValue(List<double[]> alteredValues) {

		if (alteredValues.size() == 2) {
			return TestUtils.tTest(alteredValues.get(0), alteredValues.get(1));
		} else {
			// calculate Anova statisitcs if there are more than 2 groups
			OneWayAnova oneWayAnova = new OneWayAnova();
			return oneWayAnova.anovaPValue(alteredValues);
		}
	}

	private double calculateStandardDeviation(double[] values) {

		DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics();
		for (double value : values) {
			descriptiveStatistics.addValue(value);
		}
		return descriptiveStatistics.getStandardDeviation();
	}

	/**
	 * 
	 * This method maps valid samples in molecularProfileCaseSets to indices in
	 * genetic_alteration.VALUES column. Recall this column of the
	 * genetic_alteration table is a comma separated list of scalar values. Each
	 * value in this list is associated with a sample at the same position found in
	 * the genetic_profile_samples.ORDERED_SAMPLE_LIST column.
	 * 
	 * @param molecularProfileCaseSets
	 * @param enrichmentType
	 * @param molecularProfile
	 * @return
	 */
	private Map<String, List<Integer>> getGroupIndicesMap(
			Map<String, List<MolecularProfileCaseIdentifier>> molecularProfileCaseSets, String enrichmentType,
			MolecularProfile molecularProfile) {

		MolecularProfileSamples commaSeparatedSampleIdsOfMolecularProfile = molecularDataRepository
				.getCommaSeparatedSampleIdsOfMolecularProfile(molecularProfile.getStableId());

		List<Integer> internalSampleIds = Arrays.stream(commaSeparatedSampleIdsOfMolecularProfile.getSplitSampleIds())
				.mapToInt(Integer::parseInt).boxed().collect(Collectors.toList());

		Map<Integer, Integer> internalSampleIdToIndexMap = IntStream.range(0, internalSampleIds.size()).boxed()
				.collect(Collectors.toMap(internalSampleIds::get, Function.identity()));

		Map<String, List<Integer>> selectedCaseIdToInternalIdsMap = getCaseIdToInternalIdsMap(molecularProfileCaseSets,
				enrichmentType, molecularProfile);

		// this block map caseIds(sampleIds or patientids) to sampleIndices which
		// represents the position fount in the
		// genetic_profile_samples.ORDERED_SAMPLE_LIST column
		Map<String, List<Integer>> groupIndicesMap = molecularProfileCaseSets.entrySet().stream()
				.collect(Collectors.toMap(entity -> entity.getKey(), entity -> {
					List<Integer> sampleIndices = new ArrayList<>();
					entity.getValue().forEach(molecularProfileCaseIdentifier -> {
						// consider only valid samples
						if (selectedCaseIdToInternalIdsMap.containsKey(molecularProfileCaseIdentifier.getCaseId())) {
							List<Integer> sampleInternalIds = selectedCaseIdToInternalIdsMap
									.get(molecularProfileCaseIdentifier.getCaseId());

							// only consider samples which are profiled for the give molecular profile id
							sampleInternalIds.forEach(sampleInternalId -> {
								if (internalSampleIdToIndexMap.containsKey(sampleInternalId)) {
									sampleIndices.add(internalSampleIdToIndexMap.get(sampleInternalId));
								}
							});
						}
					});
					return sampleIndices;
				}));
		return groupIndicesMap;
	}

	private Map<String, List<Integer>> getCaseIdToInternalIdsMap(
			Map<String, List<MolecularProfileCaseIdentifier>> molecularProfileCaseSets, String enrichmentType,
			MolecularProfile molecularProfile) {

		if (enrichmentType.equals("PATIENT")) {
			List<String> patientIds = molecularProfileCaseSets.values().stream()
					.flatMap(molecularProfileCaseSet -> molecularProfileCaseSet.stream()
							.map(MolecularProfileCaseIdentifier::getCaseId))
					.collect(Collectors.toList());

			List<Sample> samples = sampleService
					.getAllSamplesOfPatientsInStudy(molecularProfile.getCancerStudyIdentifier(), patientIds, "SUMMARY");

			return samples.stream().collect(Collectors.groupingBy(Sample::getPatientStableId,
					Collectors.mapping(Sample::getInternalId, Collectors.toList())));
		} else {
			List<String> sampleIds = new ArrayList<>();
			List<String> studyIds = new ArrayList<>();

			molecularProfileCaseSets.values().forEach(molecularProfileCaseIdentifiers -> {
				molecularProfileCaseIdentifiers.forEach(molecularProfileCaseIdentifier -> {
					sampleIds.add(molecularProfileCaseIdentifier.getCaseId());
					studyIds.add(molecularProfile.getCancerStudyIdentifier());
				});
			});
			List<Sample> samples = sampleService.fetchSamples(studyIds, sampleIds, "ID");

			return samples.stream()
					.collect(Collectors.toMap(Sample::getStableId, x -> Arrays.asList(x.getInternalId())));
		}
	}
}
