/*
 * Copyright (c) 2016 The Hyve B.V.
 * This code is licensed under the GNU Affero General Public License (AGPL),
 * version 3, or (at your option) any later version.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.cbioportal.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.Gene;
import org.cbioportal.model.GenesetCorrelation;
import org.cbioportal.model.GenesetMolecularData;
import org.cbioportal.model.GeneMolecularData;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.Sample;
import org.cbioportal.service.SampleListService;
import org.cbioportal.service.GenesetService;
import org.cbioportal.service.GenesetCorrelationService;
import org.cbioportal.service.GenesetDataService;
import org.cbioportal.service.MolecularDataService;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.exception.GenesetNotFoundException;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.service.exception.SampleListNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GenesetCorrelationServiceImpl implements GenesetCorrelationService {

	@Autowired
	private MolecularDataService molecularDataService;
	@Autowired
	private GenesetDataService genesetDataService;
	@Autowired
	private MolecularProfileService molecularProfileService;
	@Autowired
	private GenesetService genesetService;
	@Autowired
	private SampleService sampleService;
	@Autowired
	private SampleListService sampleListService;


	public List<GenesetCorrelation> fetchCorrelatedGenes(String genesetId, String molecularProfileId,
			double correlationThreshold) throws MolecularProfileNotFoundException, GenesetNotFoundException {

		// get sample ids from study
		CancerStudy cancerStudy = molecularProfileService.getMolecularProfile(molecularProfileId).getCancerStudy();
		List<Sample> samples = sampleService.fetchSamples(Arrays.asList(cancerStudy.getCancerStudyIdentifier()), null, "SUMMARY");
		// convert to string list:
		List<String> sampleIds = samples.stream().map(o -> o.getStableId()).collect( Collectors.toList() );
		return fetchCorrelatedGenes(genesetId, molecularProfileId, sampleIds, correlationThreshold);
	}

	public List<GenesetCorrelation> fetchCorrelatedGenes(String genesetId, String molecularProfileId, 
                                                         String sampleListId, double correlationThreshold) 
        throws MolecularProfileNotFoundException, SampleListNotFoundException, GenesetNotFoundException {

		// get sample ids from sampleList
		List<String> sampleIds = sampleListService.getAllSampleIdsInSampleList(sampleListId);		
		return fetchCorrelatedGenes(genesetId, molecularProfileId, sampleIds, correlationThreshold);
	}

	public List<GenesetCorrelation> fetchCorrelatedGenes(String genesetId, String molecularProfileId, 
                                                         List<String> sampleIds, double correlationThreshold) 
        throws MolecularProfileNotFoundException, GenesetNotFoundException {
	    
		List<GenesetCorrelation> result = new ArrayList<GenesetCorrelation>();

		// find the genes in the geneset
		List<Gene> genes = genesetService.getGenesByGenesetId(genesetId);

		// the geneset data:
		List<GenesetMolecularData> genesetData = genesetDataService.fetchGenesetData(molecularProfileId, sampleIds, 
            Arrays.asList(genesetId));
		double[] genesetValues = getGenesetValues(sampleIds, genesetData);
		// find the expression profile related to the given geneticProfileId
		List<MolecularProfile> expressionProfilesReferredByGenesetProfile = 
            molecularProfileService.getMolecularProfilesReferredBy(molecularProfileId);
		//we expect only 1 in this case (a geneset only refers to 1 expression profile, so give error otherwise):
		if (expressionProfilesReferredByGenesetProfile.size() != 1) {
			throw new RuntimeException("Unexpected error: given geneset profile refers to " + 
                expressionProfilesReferredByGenesetProfile.size() + " profile(s). Should refer to only 1");
		}
		MolecularProfile expressionProfile = expressionProfilesReferredByGenesetProfile.get(0);
		MolecularProfile zscoresProfile = getLinkedZscoreProfile(expressionProfile);

		// TODO : if this turns out to take too long, we can always implement this loop in parallel to improve 
        // performance. Multi-threading is easy in this scenario.
		// get genetic data for each gene and calculate correlation
		for (Gene gene : genes) {
			Integer entrezGeneId = gene.getEntrezGeneId();
			List<GeneMolecularData> geneData = molecularDataService.fetchMolecularData(expressionProfile.getStableId(), 
                sampleIds, Arrays.asList(entrezGeneId), "SUMMARY");
			double correlationValue = calculateCorrelation(sampleIds, geneData, genesetValues);
			// filter out the ones below correlationThreshold
			if (correlationValue < correlationThreshold) {
				continue;
			}
			GenesetCorrelation genesetCorrelationItem = new GenesetCorrelation();
			genesetCorrelationItem.setEntrezGeneId(entrezGeneId);
			genesetCorrelationItem.setHugoGeneSymbol(gene.getHugoGeneSymbol());
			genesetCorrelationItem.setCorrelationValue(correlationValue);
			genesetCorrelationItem.setExpressionMolecularProfileId(expressionProfile.getStableId());
			genesetCorrelationItem.setzScoreMolecularProfileId(zscoresProfile.getStableId());
			result.add(genesetCorrelationItem);
		}        
		// return sorted
		sortResult(result);
		return result;
	}


	private MolecularProfile getLinkedZscoreProfile(MolecularProfile expressionProfile) throws MolecularProfileNotFoundException {

		//Find the related z-score profile via the genetic_profile_link table:
		List<MolecularProfile> referringProfiles = molecularProfileService.getMolecularProfilesReferringTo(expressionProfile.getStableId());
		MolecularProfile zscoresProfile = null;
		for (MolecularProfile referringProfile : referringProfiles) {
			//use the first z-score profile we can find in this list of referring profiles (normally there should be only 1 anyway):
			if (referringProfile.getDatatype().equals("Z-SCORE")) {
				zscoresProfile = referringProfile;
				break;
			}
		}
		//if none found, give clear error message...something is wrong with this study:
		if (zscoresProfile == null) {
			throw new IllegalArgumentException("The expression profile [" + expressionProfile.getStableId() + "] linked to the given "
					+ "gene set scores profile does not have a corresponding z-scores profile in this study.");
		}
		return zscoresProfile;
	}

	private void sortResult(List<GenesetCorrelation> result) {

		result.sort((GenesetCorrelation o1, GenesetCorrelation o2)-> {
			//descending order, also check for NaN
			if (o1.getCorrelationValue().isNaN())
				return 1;
			if (o2.getCorrelationValue().isNaN())
				return -1;

			if (o1.getCorrelationValue() < o2.getCorrelationValue())
				return 1;
			if (o1.getCorrelationValue() > o2.getCorrelationValue())
				return -1;

			return 0;
		});
	}


	/**
	 * Calculates the Spearman correlation between the genesetValues list and the list built up from  
	 * geneGeneticDataItems for the given sampleIds.
	 * 
	 * Before calculating the correlation, this method prepares the gene values, filtering both them and genesetValues,
	 * removing samples where the value is not present in either gene or gene set dimension.
	 * 
	 * @param sampleIds: samples over which to calculate correlation
	 * @param geneGeneticDataItems: gene (expression) values for the set of samples
	 * @param genesetValues: gene set scores for the set of samples
	 * 
	 * @return: Spearman's correlation value between values in geneGeneticDataItems and genesetValues.
	 */
	private double calculateCorrelation(List<String> sampleIds, List<GeneMolecularData> geneGeneticDataItems, double[] genesetValues) {
		
		//index geneData values
		Map<String, Double> sampleValues = new HashMap<String, Double>();
		for (GeneMolecularData geneGeneticDataItem : geneGeneticDataItems) {
			double value = Double.NaN;
			if (NumberUtils.isNumber(geneGeneticDataItem.getValue())) {
				value = Double.parseDouble(geneGeneticDataItem.getValue());
			}
			sampleValues.put(geneGeneticDataItem.getSampleId(), value);
		}
		//get values
		List<Double> geneValueList = new ArrayList<Double>();
		List<Double> genesetValueList = new ArrayList<Double>();
		for (int i = 0; i < sampleIds.size(); i++) {
			String sampleId = sampleIds.get(i);
			Double value = sampleValues.get(sampleId);
			if (value == null) {
				value = Double.NaN; //set to NaN when value is not available for this sample
			}
			// if both this value and genesetValues[i] are NaN, then skip this item (since it will not be allowed by the correlation method):
			if (!Double.isNaN(value) && !Double.isNaN(genesetValues[i])) {
				geneValueList.add(value);
				// build up genesetValues is same way:
				genesetValueList.add(genesetValues[i]);
			}
		}
		// final filtered lists:
		double[] geneValuesFiltered = geneValueList.stream().mapToDouble(d -> d).toArray();
		double[] genesetValuesFiltered = genesetValueList.stream().mapToDouble(d -> d).toArray();
		double correlationValue = 0;
		// arrays need to be at least 2 long to calculate correlation: 
		if (geneValuesFiltered.length >= 2) {
			// calculate spearman correlation
			SpearmansCorrelation spearmansCorrelation = new SpearmansCorrelation();
			correlationValue = spearmansCorrelation.correlation(geneValuesFiltered, genesetValuesFiltered);
		}
		return correlationValue;
	}


	private double[] getGenesetValues(List<String> sampleIds, List<GenesetMolecularData> genesetDataItems) {

		//index genesetData values
		Map<String, Double> sampleValues = new HashMap<String, Double>();
		for (GenesetMolecularData genesetDataItem : genesetDataItems) {
			double value = Double.NaN;
			if (NumberUtils.isNumber(genesetDataItem.getValue())) {
				value = Double.parseDouble(genesetDataItem.getValue());
			}
			sampleValues.put(genesetDataItem.getSampleId(), value);
		}
		//get values
		double[] result = new double[sampleIds.size()];
		for (int i = 0; i < sampleIds.size(); i++) {
			String sampleId = sampleIds.get(i);
			Double value = sampleValues.get(sampleId);
			if (value != null) {
				result[i] = value;
			} else {
				result[i] = Double.NaN; //set to NaN when value is not available for this sample
			}
		}
		return result;
	}
}
