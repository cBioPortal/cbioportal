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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.Geneset;
import org.cbioportal.model.GenesetMolecularData;
import org.cbioportal.model.GenesetHierarchyInfo;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.Sample;
import org.cbioportal.persistence.GenesetHierarchyRepository;
import org.cbioportal.service.SampleListService;
import org.cbioportal.service.GenesetDataService;
import org.cbioportal.service.GenesetHierarchyService;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.service.exception.SampleListNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GenesetHierarchyServiceImpl implements GenesetHierarchyService {

    @Autowired
    private GenesetDataService genesetDataService;
    @Autowired
    private MolecularProfileService geneticProfileService;
    @Autowired
    private GenesetHierarchyRepository genesetHierarchyRepository;
    @Autowired
    private SampleService sampleService;
    @Autowired
    private SampleListService sampleListService;
	
	public List<GenesetHierarchyInfo> fetchGenesetHierarchyInfo(String geneticProfileId, Integer percentile, 
			Double scoreThreshold, Double pvalueThreshold) throws MolecularProfileNotFoundException {
		
		// get sample ids from study
		CancerStudy cancerStudy = geneticProfileService.getMolecularProfile(geneticProfileId).getCancerStudy();
		List<Sample> samples = sampleService.fetchSamples(Arrays.asList(cancerStudy.getCancerStudyIdentifier()), null, "SUMMARY");
		// convert to string list:
		List<String> sampleIds = samples.stream().map(o -> o.getStableId()).collect( Collectors.toList() );
		return fetchGenesetHierarchyInfo(geneticProfileId, percentile, scoreThreshold, pvalueThreshold, sampleIds);
	}

	public List<GenesetHierarchyInfo> fetchGenesetHierarchyInfo(String geneticProfileId, Integer percentile,
			Double scoreThreshold, Double pvalueThreshold, String sampleListId) throws MolecularProfileNotFoundException, SampleListNotFoundException {
		
		// get sample ids from sampleList
		List<String> sampleIds = sampleListService.getAllSampleIdsInSampleList(sampleListId);		
		return fetchGenesetHierarchyInfo(geneticProfileId, percentile, scoreThreshold, pvalueThreshold, sampleIds);
	}

	public List<GenesetHierarchyInfo> fetchGenesetHierarchyInfo(String geneticProfileId, Integer percentile,
			Double scoreThreshold, Double pvalueThreshold, List<String> sampleIds) throws MolecularProfileNotFoundException {

		//validate: 
		MolecularProfile geneticProfile = geneticProfileService.getMolecularProfile(geneticProfileId);
		//also validate if profile is of geneset_score type:
		if (!geneticProfile.getDatatype().equals("GSVA-SCORE")) {
			throw new IllegalArgumentException("Genetic profile should be of DATA_TYPE = GSVA-SCORE, but found: " +
					geneticProfile.getDatatype());
		}
		
		//get list of genesets and respective score records for gene sets that have data for this profile:
		List<GenesetMolecularData> genesetScores = genesetDataService.fetchGenesetData(geneticProfileId, sampleIds, null); 
		List<GenesetMolecularData> genesetPvalues = getGenesetPvalues(geneticProfileId, sampleIds);
		
		return getGenesetHierarchyItems(genesetScores, genesetPvalues, percentile, scoreThreshold, pvalueThreshold);
	}

	/**
	 * Finds and retrieves the gene set p-values of the profile linked to the given scoresGeneticProfileId.
	 * 
	 * @param scoresGeneticProfileId: genetic profile containing gene set scores
	 * @param sampleIds: samples to use for the representative score calculation
	 * @return
	 * @throws MolecularProfileNotFoundException
	 */
	private List<GenesetMolecularData> getGenesetPvalues(String scoresGeneticProfileId, List<String> sampleIds) throws MolecularProfileNotFoundException {

		List<MolecularProfile> pvaluesGeneticProfiles = geneticProfileService.getMolecularProfilesReferringTo(scoresGeneticProfileId);
		//validate : 
		if (pvaluesGeneticProfiles == null || pvaluesGeneticProfiles.size() != 1) {
			//unexpected, but could happen if dataset validation is skipped
			throw new RuntimeException("The given gene set profile [" + scoresGeneticProfileId + "] should have (at least) 1 and only 1 linked p-values profile in DB");
		}
		//validate if profile is of pvalue type:
		if (!pvaluesGeneticProfiles.get(0).getDatatype().equals("P-VALUE")) {
			throw new IllegalArgumentException("Genetic profile should be of DATA_TYPE = P-VALUE");
		}

		List<GenesetMolecularData> genesetPvalues = genesetDataService.fetchGenesetData(pvaluesGeneticProfiles.get(0).getStableId(), sampleIds, null);  
		return genesetPvalues;
	}

	/**
	 * Return the hierarchy nodes and leafs (gene sets) based on data found in genesetScores.
	 * 
	 * @param genesetScores: gene set score data found for the given geneticProfileId and sampleIds
	 * @param percentile: percentile to use for the representative score calculation
	 * @param scoreThreshold: filter criterion
	 * @param pvalueThreshold: filter criterion
	 * 
	 * @return
	 * @throws MolecularProfileNotFoundException
	 */
	private List<GenesetHierarchyInfo> getGenesetHierarchyItems(List<GenesetMolecularData> genesetScores,
			List<GenesetMolecularData> genesetPvalues,
    		Integer percentile, Double scoreThreshold, Double pvalueThreshold) throws MolecularProfileNotFoundException {

		List<String> genesetIds = new ArrayList<String>(genesetScores.stream().map(o -> o.getGenesetId()).collect( Collectors.toSet() ));

		//add all hierarchy nodes that have no leafs, but are intermediate/super nodes:
		List<GenesetHierarchyInfo> hierarchySuperNodes = genesetHierarchyRepository.getGenesetHierarchySuperNodes(genesetIds);
		
		//index genesetData : 
		Map<String, List<GenesetMolecularData>> genesetScoresMap =
				genesetScores.stream().collect(Collectors.groupingBy(GenesetMolecularData::getGenesetId));
		Map<String, List<GenesetMolecularData>> genesetPvaluesMap =
				genesetPvalues.stream().collect(Collectors.groupingBy(GenesetMolecularData::getGenesetId));
		
		
		//get the nodes that have gene sets as child/leafs:
		List<GenesetHierarchyInfo> hierarchyGenesetParents = genesetHierarchyRepository.getGenesetHierarchyParents(genesetIds);//maybe rename to pre-leafItems?
		if (genesetIds != null) {
			//complement the result with the gene sets info:
			for (GenesetHierarchyInfo hierarchyItem : hierarchyGenesetParents) {
				List<Geneset> genesets = genesetHierarchyRepository.getGenesetHierarchyGenesets(hierarchyItem.getNodeId());
				//get only  the ones that have data, filtering out the other ones (probably not needed, but just to be sure):
				genesets = getFilteredGenesets(genesets, genesetScoresMap);
				//for each gene set, calculate representative score:
				fillRepresentativeScoresAndPvalues(genesets, genesetScoresMap, genesetPvaluesMap, percentile);
				//filter out the ones that don't satisfy thresholds:
				genesets = getFilteredGenesets(genesets, scoreThreshold, pvalueThreshold);
				hierarchyItem.setGenesets(genesets);
				//if genesets turns out to be empty, still consider it as a possible super node (will be filtered below if it is not):
				if (genesets.size() == 0) {
					hierarchySuperNodes.add(hierarchyItem);
				}
			}
		}
		//remove the hierarchyGenesetParents that have no gene set leaf nodes:
		hierarchyGenesetParents = getFilteredHierarchyGenesetParents(hierarchyGenesetParents);
		//remove nodes from hierarchySuperNodes that do not lead to a leaf node:
		hierarchySuperNodes = getFilteredHierarchySuperNodes(hierarchySuperNodes, hierarchyGenesetParents);
		//TODO could probably simplify some of this code above by merging hierarchySuperNodes and hierarchyGenesetParents at the start
		//and filtering the tree in one recursive function... 
		
		//join both lists:
		hierarchySuperNodes.addAll(hierarchyGenesetParents);
		
		return hierarchySuperNodes;
	}

	
	private List<Geneset> getFilteredGenesets(List<Geneset> genesets, Double scoreThreshold, Double pvalueThreshold) {

		return genesets.stream().filter(
					g -> Math.abs(g.getRepresentativeScore()) >= scoreThreshold &&
					     g.getRepresentativePvalue() <= pvalueThreshold
				).collect(Collectors.toList());
	}

	private List<GenesetHierarchyInfo> getFilteredHierarchySuperNodes(List<GenesetHierarchyInfo> hierarchySuperNodes,
			List<GenesetHierarchyInfo> referringNodes) {
		
		//index referred node ids:
		Set<Integer> referredNodesSet =
				referringNodes.stream().map(GenesetHierarchyInfo::getParentId).collect(Collectors.toSet());

		//also make index for super nodes as they could refer to each other
		List<GenesetHierarchyInfo> referredNodes = new ArrayList<GenesetHierarchyInfo>();
		for (GenesetHierarchyInfo hierarchySuperNode: hierarchySuperNodes) {
			if (referredNodesSet.contains(hierarchySuperNode.getNodeId())) {
				//there are child nodes, so add this one:
				if (!referredNodes.contains(hierarchySuperNode)) {
					referredNodes.add(hierarchySuperNode);
				}
			}
		}
		if (referredNodes.size() > 0) {
			//recursion: also add the parents of the referredNodes
			List<GenesetHierarchyInfo> parents = getFilteredHierarchySuperNodes(hierarchySuperNodes, referredNodes);
			for (GenesetHierarchyInfo parent: parents) {
				if(!referredNodes.contains(parent))
					referredNodes.add(parent);
			}
		}
		return referredNodes;
	}

	private List<GenesetHierarchyInfo> getFilteredHierarchyGenesetParents(
			List<GenesetHierarchyInfo> hierarchyGenesetParents) {
		
		List<GenesetHierarchyInfo> result = new ArrayList<GenesetHierarchyInfo>();
		for (GenesetHierarchyInfo hierarchyGenesetParent: hierarchyGenesetParents) {
			if (hierarchyGenesetParent.getGenesets().size() > 0) {
				//there are leaf nodes, so add this one:
				result.add(hierarchyGenesetParent);
			}
		}
		return result;
	}

	private List<Geneset> getFilteredGenesets(List<Geneset> genesets, Map<String, List<GenesetMolecularData>> genesetDataMap) {
		
		List<Geneset> result = new ArrayList<Geneset>();
		for (Geneset geneset: genesets) {
			if (genesetDataMap.get(geneset.getGenesetId()) != null) {
				//there is data, so add this one:
				result.add(geneset);
			}
		}
		return result;
	}

	/**
	 * This will set the representativeScore attribute for each gene set, based on the data (the gene set 
	 * scores per sample).
	 * @param sampleIds 
	 * 
	 * @param genesets: list of gene sets for which to calculate and record the representativeScore
	 * @param genesetDataMap: the set of GSVA(like) scores per sample for each gene set 
	 * @param percentile: (optional) which percentile to use when determining the representativeScore. If not 
	 *            set, max of absolute score is returned.
	 * @throws MolecularProfileNotFoundException 
	 */
	private void fillRepresentativeScoresAndPvalues(List<Geneset> genesets,
                                                    Map<String, List<GenesetMolecularData>> genesetScoresMap, Map<String, List<GenesetMolecularData>> genesetPvaluesMap,
                                                    Integer percentile) throws MolecularProfileNotFoundException {
		
		genesets.stream().forEach(g -> calculateAndSetRepresentativeScoreAndPvalue(g, genesetScoresMap, genesetPvaluesMap, percentile));
	}

	private void calculateAndSetRepresentativeScoreAndPvalue(Geneset geneset, 
			Map<String, List<GenesetMolecularData>> genesetScoresMap, 
			Map<String, List<GenesetMolecularData>> genesetPvaluesMap, 
			Integer percentile) {
		
		List<GenesetMolecularData> genesetScoreData = genesetScoresMap.get(geneset.getGenesetId());
		List<GenesetMolecularData> genesetPvalueData = genesetPvaluesMap.get(geneset.getGenesetId());
		
		//lists to hold the score and p-value pairs:
		List<ImmutablePair<Double,Double>> positiveScoresAndPvalues = new ArrayList<ImmutablePair<Double,Double>>();
		List<ImmutablePair<Double,Double>> negativeScoresAndPvalues = new ArrayList<ImmutablePair<Double,Double>>();
		
		//return the maximum absolute value found:
		double max = 0;
		double pvalueOfMax = 1;
		for (int i = 0; i < genesetScoreData.size(); i++) {
			String scoreString = genesetScoreData.get(i).getValue();
			String pvalueString = genesetPvalueData.get(i).getValue();
			
			if (!NumberUtils.isNumber(scoreString)) 
				continue;

			double score = Double.parseDouble(scoreString);
			double pvalue = 1.0;
			if (NumberUtils.isNumber(pvalueString))
				pvalue = Double.parseDouble(pvalueString);
			if (score >= 0) {
				positiveScoresAndPvalues.add(new ImmutablePair<Double,Double>(score, pvalue));
			} else {
				negativeScoresAndPvalues.add(new ImmutablePair<Double,Double>(score, pvalue));
			}
			
			//keep track of max, in case percentile is null
			if (Math.abs(score) > Math.abs(max)) {
				max = score; //here no abs, since we want to get the raw score (could be negative)
				pvalueOfMax = pvalue;
			}
		}
		
		if (percentile == null) {
			geneset.setRepresentativeScore(max);
			geneset.setRepresentativePvalue(pvalueOfMax);
		} else {
			//sort scores (NB: .getLeft() returns the score, .getRight() returns the pvalue of each pair):
			positiveScoresAndPvalues.sort((ImmutablePair<Double,Double> o1, ImmutablePair<Double,Double> o2)-> Double.compare(o1.getLeft(), o2.getLeft()));
			//negative scores descending:
			negativeScoresAndPvalues.sort((ImmutablePair<Double,Double> o1, ImmutablePair<Double,Double> o2)-> Double.compare(o2.getLeft(), o1.getLeft()));
			
			//use percentile:
			ImmutablePair<Double,Double> representativePositiveScoreAndPvalue = new ImmutablePair<Double,Double>(0.0, 1.0);
			ImmutablePair<Double,Double> representativeNegativeScoreAndPvalue = new ImmutablePair<Double,Double>(0.0, 1.0);
			if (positiveScoresAndPvalues.size() > 0) {
				int idxPositiveScores = (int)Math.round(percentile * positiveScoresAndPvalues.size() / 100.0);
				if (idxPositiveScores == 0) {  //(can happen when positiveScoresAndPvalues.size() is small)
				    idxPositiveScores = 1;
				}
				representativePositiveScoreAndPvalue = positiveScoresAndPvalues.get(idxPositiveScores-1);
			}
			if (negativeScoresAndPvalues.size() > 0) {
				int idxNegativeScores = (int)Math.round(percentile * negativeScoresAndPvalues.size() / 100.0);
				if (idxNegativeScores == 0) {  //(can happen when positiveScoresAndPvalues.size() is small)
				    idxNegativeScores = 1;
                }
				representativeNegativeScoreAndPvalue = negativeScoresAndPvalues.get(idxNegativeScores-1);
			}
			
			//set best one:
			if (Math.abs(representativePositiveScoreAndPvalue.getLeft()) > Math.abs(representativeNegativeScoreAndPvalue.getLeft())) {
				geneset.setRepresentativeScore(representativePositiveScoreAndPvalue.getLeft());
				geneset.setRepresentativePvalue(representativePositiveScoreAndPvalue.getRight());
			} else {
				geneset.setRepresentativeScore(representativeNegativeScoreAndPvalue.getLeft());
				geneset.setRepresentativePvalue(representativeNegativeScoreAndPvalue.getRight());
			}
		}
	}
}
