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
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang.math.NumberUtils;
import org.cbioportal.model.Geneset;
import org.cbioportal.model.GenesetAlteration;
import org.cbioportal.model.GenesetHierarchyInfo;
import org.cbioportal.persistence.GenesetHierarchyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class GenesetHierarchyMyBatisRepository implements GenesetHierarchyRepository {

	@Autowired
    GenesetHierarchyMapper genesetHierarchyMapper;
    
	@Override
	public List<GenesetHierarchyInfo> getGenesetHierarchyItems(List<GenesetAlteration> genesetData) {

		List<String> genesetIds = genesetData.stream().map(o -> o.getGenesetId()).collect( Collectors.toList() );

		//add all hierarchy nodes that have no leafs, but are intermediate/super nodes:
		List<GenesetHierarchyInfo> hierarchySuperNodes = genesetHierarchyMapper.getGenesetHierarchySuperNodes(genesetIds);
		
		//index genesetData : 
		Map<String, GenesetAlteration> genesetDataMap =
				genesetData.stream().collect(Collectors.toMap(GenesetAlteration::getGenesetId,
			                                              Function.identity()));
		
		//get the nodes that have gene sets as child/leafs:
		List<GenesetHierarchyInfo> hierarchyGenesetParents = genesetHierarchyMapper.getGenesetHierarchyParents(genesetIds);//maybe rename to pre-leafItems?
		if (genesetIds != null) {
			//complement the result with the gene sets info:
			for (GenesetHierarchyInfo hierarchyItem : hierarchyGenesetParents) {
				List<Geneset> genesets = genesetHierarchyMapper.getGenesetHierarchyGenesets(hierarchyItem.getNodeId());
				//get only  the ones that have data, filtering out the other ones:
				genesets = getFilteredGenesets(genesets, genesetDataMap);
				//for each gene set, calculate representative score:
				fillRepresentativeScores(genesets, genesetDataMap);
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
	 */
	private void fillRepresentativeScores(List<Geneset> genesets, Map<String, GenesetAlteration> genesetDataMap) {
		
		genesets.stream().forEach(g -> g.setRepresentativeScore(calculateRepresentativeScore(g, genesetDataMap)));
	}

	private Double calculateRepresentativeScore(Geneset geneset, Map<String, GenesetAlteration> genesetDataMap) {
		
		GenesetAlteration genesetAlterationData = genesetDataMap.get(geneset.getGenesetId());
		
		//return the maximum absolute value found:
		String[] values = genesetAlterationData.getValues().split(",");
		
		double max = 0;
		for (String value : values) {
			if (!NumberUtils.isNumber(value)) 
    			continue;
    		
    		if (Math.abs(Double.parseDouble(value)) > Math.abs(max)) {
    			max = Double.parseDouble(value); //here no abs, since we want to get the raw score (could be negative)
    		}
		}
		return max;
	}

}
