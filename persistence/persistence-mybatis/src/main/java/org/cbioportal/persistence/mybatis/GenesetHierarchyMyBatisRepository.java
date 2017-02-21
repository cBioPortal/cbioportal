/*
 * Copyright (c) 2016 Memorial Sloan Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
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
package org.cbioportal.persistence.mybatis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang.math.NumberUtils;
import org.cbioportal.model.Geneset;
import org.cbioportal.model.GenesetAlteration;
import org.cbioportal.model.GenesetHierarchyInfo;
import org.cbioportal.model.GeneticProfile;
import org.cbioportal.model.GeneticProfile.DataType;
import org.cbioportal.persistence.GenesetHierarchyRepository;
import org.cbioportal.persistence.GeneticDataRepository;
import org.cbioportal.persistence.GeneticProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class GenesetHierarchyMyBatisRepository implements GenesetHierarchyRepository {

	@Autowired
    private GenesetHierarchyMapper genesetHierarchyMapper;
	@Autowired
    private GeneticDataRepository geneticDataRepository;
	@Autowired
    private GeneticProfileRepository geneticProfileRepository;
	
	@Override
    public List<GenesetHierarchyInfo> getGenesetHierarchyItems(String geneticProfileId, List<GenesetAlteration> genesetScores, 
    		Integer percentile, Double scoreThreshold, Double pvalueThreshold) {

		List<String> genesetIds = genesetScores.stream().map(o -> o.getGenesetId()).collect( Collectors.toList() );

		//add all hierarchy nodes that have no leafs, but are intermediate/super nodes:
		List<GenesetHierarchyInfo> hierarchySuperNodes = genesetHierarchyMapper.getGenesetHierarchySuperNodes(genesetIds);
		
		//index genesetData : 
		Map<String, GenesetAlteration> genesetScoresMap =
				genesetScores.stream().collect(Collectors.toMap(GenesetAlteration::getGenesetId,
			                                              Function.identity()));
		
		//get the nodes that have gene sets as child/leafs:
		List<GenesetHierarchyInfo> hierarchyGenesetParents = genesetHierarchyMapper.getGenesetHierarchyParents(genesetIds);//maybe rename to pre-leafItems?
		if (genesetIds != null) {
			//complement the result with the gene sets info:
			for (GenesetHierarchyInfo hierarchyItem : hierarchyGenesetParents) {
				List<Geneset> genesets = genesetHierarchyMapper.getGenesetHierarchyGenesets(hierarchyItem.getNodeId());
				//get only  the ones that have data, filtering out the other ones:
				genesets = getFilteredGenesets(genesets, genesetScoresMap);
				//for each gene set, calculate representative score:
				fillRepresentativeScoresAndPvalues(geneticProfileId, genesets, genesetScoresMap, percentile);
				//filter out the ones that don't satisfy thresholds:
				genesets = getFilteredGenesets(genesets, scoreThreshold, pvalueThreshold);
				hierarchyItem.setGenesets(genesets);
			}
		}
		//remove the hierarchyGenesetParents that have no gene set leaf nodes:
		hierarchyGenesetParents = getFilteredHierarchyGenesetParents(hierarchyGenesetParents);
		//remove nodes from hierarchySuperNodes that do not lead to a leaf node:
		hierarchySuperNodes = getFilteredHierarchySuperNodes(hierarchySuperNodes, hierarchyGenesetParents);
		
		//join both lists:
		hierarchySuperNodes.addAll(hierarchyGenesetParents);
		
		return hierarchySuperNodes;
	}

	
	private List<Geneset> getFilteredGenesets(List<Geneset> genesets, Double scoreThreshold, Double pvalueThreshold) {

		return genesets.stream().filter(
					g -> g.getRepresentativeScore() >= scoreThreshold &&
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
			referredNodes.addAll(parents);
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

	private List<Geneset> getFilteredGenesets(List<Geneset> genesets, Map<String, GenesetAlteration> genesetDataMap) {
		
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
	 * 
	 * @param genesets: list of gene sets for which to calculate and record the representativeScore
	 * @param genesetDataMap: the set of GSVA(like) scores per sample for each gene set 
	 * @param percentile: (optional) which percentile to use when determining the representativeScore. If not 
	 *            set, max of absolute score is returned.
	 */
	private void fillRepresentativeScoresAndPvalues(String scoresGeneticProfileId, List<Geneset> genesets, 
			Map<String, GenesetAlteration> genesetScoresMap, Integer percentile) {
		
		List<GeneticProfile> pvaluesGeneticProfiles = geneticProfileRepository.getGeneticProfilesReferringTo(scoresGeneticProfileId);
		//validate : 
		if (pvaluesGeneticProfiles == null || pvaluesGeneticProfiles.size() != 1) {
			//unexpected, but could happen if dataset validation is skipped
			throw new RuntimeException("The given gene set profile [" + scoresGeneticProfileId + "] should have (at least) 1 and only 1 linked p-values profile in DB");
		}
		//validate if profile is of pvalue type:
		if (!pvaluesGeneticProfiles.get(0).getDatatype().equals(DataType.P_VALUE)) {
			throw new IllegalArgumentException("Genetic profile should be of DATA_TYPE = " + DataType.P_VALUE);
		}

		List<GenesetAlteration> genesetPvalues = geneticDataRepository.getGenesetAlterations(pvaluesGeneticProfiles.get(0).getStableId(), null, "SUMMARY");
		//index genesetData : 
		Map<String, GenesetAlteration> genesetPvaluesMap =
						genesetPvalues.stream().collect(Collectors.toMap(GenesetAlteration::getGenesetId,
					                                              Function.identity()));
		
		genesets.stream().forEach(g -> setRepresentativeScoreAndPvalue(g, genesetScoresMap, genesetPvaluesMap, percentile));
	}

	private void setRepresentativeScoreAndPvalue(Geneset geneset, 
			Map<String, GenesetAlteration> genesetScoresMap, 
			Map<String, GenesetAlteration> genesetPvaluesMap, 
			Integer percentile) {
		
		GenesetAlteration genesetScoreData = genesetScoresMap.get(geneset.getGenesetId());
		GenesetAlteration genesetPvalueData = genesetPvaluesMap.get(geneset.getGenesetId());
		
		//return the maximum absolute value found:
		String[] values = genesetScoreData.getValues().split(",");
		String[] pvalues = genesetPvalueData.getValues().split(",");
		List<ScoreAndPvalue> positiveScoresAndPvalues = new ArrayList<ScoreAndPvalue>();
		List<ScoreAndPvalue> negativeScoresAndPvalues = new ArrayList<ScoreAndPvalue>();
		
		double max = 0;
		double pvalueOfMax = 1;
		for (int i = 0; i < values.length; i++) {
			String scoreString = values[i];
			String pvalueString = pvalues[i];
			
			if (!NumberUtils.isNumber(scoreString)) 
    			continue;
    		
			double score = Double.parseDouble(scoreString);
			double pvalue = 1.0;
			if (NumberUtils.isNumber(pvalueString))
				pvalue = Double.parseDouble(pvalueString);
			if (score >= 0) {
				positiveScoresAndPvalues.add(new ScoreAndPvalue(score, pvalue));
			} else {
				negativeScoresAndPvalues.add(new ScoreAndPvalue(score, pvalue));
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
			//sort scores:
			Collections.sort(positiveScoresAndPvalues, ScoreAndPvalue.comparator);
			Collections.sort(negativeScoresAndPvalues, ScoreAndPvalue.comparator);
			
			//use percentile:
			ScoreAndPvalue representativePositiveScoreAndPvalue = new ScoreAndPvalue(0, 1);
			ScoreAndPvalue representativeNegativeScoreAndPvalue = new ScoreAndPvalue(0, 1);
			if (positiveScoresAndPvalues.size() > 0) {
				int idxPositiveScores = (int)Math.round(percentile * positiveScoresAndPvalues.size() / 100.0); //or Math.floor ?
				representativePositiveScoreAndPvalue = positiveScoresAndPvalues.get(idxPositiveScores);
			}
			if (negativeScoresAndPvalues.size() > 0) {
				int idxNegativeScores = (int)Math.round(percentile * negativeScoresAndPvalues.size() / 100.0);
				representativeNegativeScoreAndPvalue = negativeScoresAndPvalues.get(idxNegativeScores);
			}
			
			//set best one:
			if (Math.abs(representativePositiveScoreAndPvalue.score) > Math.abs(representativeNegativeScoreAndPvalue.score)) {
				geneset.setRepresentativeScore(representativePositiveScoreAndPvalue.score);
				geneset.setRepresentativePvalue(representativePositiveScoreAndPvalue.pvalue);
			} else {
				geneset.setRepresentativeScore(representativeNegativeScoreAndPvalue.score);
				geneset.setRepresentativePvalue(representativeNegativeScoreAndPvalue.pvalue);
			}
		}
	}

	static class ScoreAndPvalue {

		double score;
		double pvalue;
		public ScoreAndPvalue(double score, double pvalue) {
			this.score = score;
			this.pvalue = pvalue;
		}
		
		static final Comparator<ScoreAndPvalue> comparator = new Comparator<ScoreAndPvalue>() {
            @Override
            public int compare(ScoreAndPvalue o1, ScoreAndPvalue o2) {
            	
        		//asc order
        		if (o1.score < o2.score)
        			return -1;
        		if (o1.score > o2.score)
        			return 1;
        		
        		return 0;
            }
        };

	}

}
