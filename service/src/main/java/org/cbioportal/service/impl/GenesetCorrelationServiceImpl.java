package org.cbioportal.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.Gene;
import org.cbioportal.model.GenesetCorrelation;
import org.cbioportal.model.GenesetData;
import org.cbioportal.model.GeneticData;
import org.cbioportal.model.GeneticProfile;
import org.cbioportal.model.Sample;
import org.cbioportal.persistence.SampleListRepository;
import org.cbioportal.service.GeneService;
import org.cbioportal.service.GenesetCorrelationService;
import org.cbioportal.service.GenesetDataService;
import org.cbioportal.service.GeneticDataService;
import org.cbioportal.service.GeneticProfileService;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GenesetCorrelationServiceImpl implements GenesetCorrelationService {

    @Autowired
    private GeneticDataService geneticDataService;
    @Autowired
    private GenesetDataService genesetDataService;
    @Autowired
    private GeneticProfileService geneticProfileService;
    @Autowired
    private GeneService geneService;
    @Autowired
    private SampleService sampleService;
    @Autowired
    private SampleListRepository sampleListRepository;


	@Override
	public List<GenesetCorrelation> fetchCorrelatedGenes(String genesetId, String geneticProfileId,
			double correlationThreshold) throws GeneticProfileNotFoundException {

		// get sample ids from study
		CancerStudy cancerStudy = geneticProfileService.getGeneticProfile(geneticProfileId).getCancerStudy();
		List<Sample> samples = sampleService.fetchSamples(Arrays.asList(cancerStudy.getCancerStudyIdentifier()), null, "SUMMARY");
		// convert to string list:
		List<String> sampleIds = samples.stream().map(o -> o.getStableId()).collect( Collectors.toList() );
		return fetchCorrelatedGenes(genesetId, geneticProfileId, sampleIds, correlationThreshold);
	}
	
	@Override
	public List<GenesetCorrelation> fetchCorrelatedGenes(String genesetId, String geneticProfileId, String sampleListId,
			double correlationThreshold) throws GeneticProfileNotFoundException {

		// get sample ids from sampleList
		List<String> sampleIds = sampleListRepository.getAllSampleIdsInSampleList(sampleListId);		
		return fetchCorrelatedGenes(genesetId, geneticProfileId, sampleIds, correlationThreshold);
	}
    
	@Override
	public List<GenesetCorrelation> fetchCorrelatedGenes(String genesetId, String geneticProfileId, List<String> sampleIds,
			double correlationThreshold) throws GeneticProfileNotFoundException {
		SpearmansCorrelation spearmansCorrelation = new SpearmansCorrelation();
		List<GenesetCorrelation> result = new ArrayList<GenesetCorrelation>();
		
		// find the genes in the geneset
		List<Gene> genes = geneService.getGenesByGenesetId(genesetId);
		
        // the geneset data:
        List<GenesetData> genesetData = genesetDataService.fetchGenesetData(geneticProfileId, sampleIds, Arrays.asList(genesetId));
        double[] genesetValues = getGenesetValues(sampleIds, genesetData);
        // find the expression profile related to the given geneticProfileId
        List<GeneticProfile> expressionProfilesReferredByGenesetProfile = geneticProfileService.getGeneticProfilesReferredBy(geneticProfileId);
        //we expect only 1 in this case (a geneset only refers to 1 expression profile, so give error otherwise):
        if (expressionProfilesReferredByGenesetProfile.size() != 1) {
        	throw new RuntimeException("Unexpected error: give geneset profile refers to " + expressionProfilesReferredByGenesetProfile.size() + " profile(s)");
        }
        GeneticProfile expressionProfile = expressionProfilesReferredByGenesetProfile.get(0); 
        
        // TODO : if this turns out to take too long, we can always implement this loop in parallel to improve performance. Multi-threading is easy in this scenario.
		// get genetic data for each gene and calculate correlation
        for (Gene gene : genes) {
        	Integer entrezGeneId = gene.getEntrezGeneId();
        	List<GeneticData> geneData = geneticDataService.fetchGeneticData(expressionProfile.getStableId(), sampleIds, 
        			Arrays.asList(entrezGeneId), "SUMMARY");
        	FilteredGeneAndGenesetValues geneAndGenesetValues = getAndFilterValues(sampleIds, geneData, genesetValues);
        	
        	double correlationValue = 0;
        	// arrays need to be at least 2 long to calculate correlation: 
        	if (geneAndGenesetValues.geneValues.length > 2) {
	    		// calculate spearman correlation
	        	correlationValue = spearmansCorrelation.correlation(geneAndGenesetValues.geneValues, geneAndGenesetValues.genesetValues);
        	}
    		// filter out the ones below correlationThreshold
        	if (Math.abs(correlationValue) < correlationThreshold) {
        		continue;
        	}
        	GenesetCorrelation genesetCorrelationItem = new GenesetCorrelation();
        	genesetCorrelationItem.setEntrezGeneId(entrezGeneId);
        	genesetCorrelationItem.setHugoGeneSymbol(gene.getHugoGeneSymbol());
        	genesetCorrelationItem.setCorrelationValue(correlationValue);
        	result.add(genesetCorrelationItem);
        }        
		// return sorted
        sortResult(result);
		return result;
	}


	private void sortResult(List<GenesetCorrelation> result) {
        Comparator<GenesetCorrelation> comparator = new Comparator<GenesetCorrelation>() {
            @Override
            public int compare(GenesetCorrelation o1, GenesetCorrelation o2) {
            	
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
            }
        };
		Collections.sort(result, comparator);		
	}


	/**
	 * Prepares the gene values, but also filters both them and genesetValues, removing samples where the value is not present in 
	 * either gene or gene set dimension. 
	 * 
	 * @param sampleIds
	 * @param geneticDataItems
	 * @param genesetValues
	 * @return
	 */
	private FilteredGeneAndGenesetValues getAndFilterValues(List<String> sampleIds, List<GeneticData> geneticDataItems, double[] genesetValues) {
		
		//index geneData values
		Map<String, Double> sampleValues = new HashMap<String, Double>();
		for (GeneticData geneticDataItem : geneticDataItems) {
			double value = Double.NaN;
			if (NumberUtils.isNumber(geneticDataItem.getValue())) {
				value = Double.parseDouble(geneticDataItem.getValue());
			}
			sampleValues.put(geneticDataItem.getSampleId(), value);
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
		// final result:
		FilteredGeneAndGenesetValues filteredGeneAndGenesetValues = new FilteredGeneAndGenesetValues(); 
		filteredGeneAndGenesetValues.geneValues = geneValueList.stream().mapToDouble(d -> d).toArray();
		filteredGeneAndGenesetValues.genesetValues = genesetValueList.stream().mapToDouble(d -> d).toArray();
		
		return filteredGeneAndGenesetValues;
	}


	private double[] getGenesetValues(List<String> sampleIds, List<GenesetData> genesetDataItems) {

		//index genesetData values
		Map<String, Double> sampleValues = new HashMap<String, Double>();
		for (GenesetData genesetDataItem : genesetDataItems) {
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
	
	class FilteredGeneAndGenesetValues {
		double[] genesetValues;
		double[] geneValues;
	}
}
