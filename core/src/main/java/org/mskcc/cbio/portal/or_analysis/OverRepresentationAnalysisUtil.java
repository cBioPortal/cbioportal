/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center
 * has been advised of the possibility of such damage.
 */

package org.mskcc.cbio.portal.or_analysis;

import java.util.*;

import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.dao.*;


public class OverRepresentationAnalysisUtil {
    
    public static Map<Long, HashMap<Integer, String>> getValueMap(
            int profileId, String profileType, List<Integer> alteredSampleIds, List<Integer> unalteredSampleIds, String geneSet) throws DaoException {

        //Map<GeneId, HashMap<CaseId, Value>>
        Map<Long, HashMap<Integer, String>> result = new HashMap<Long, HashMap<Integer,String>>();

        DaoGeneticAlteration daoGeneticAlteration = DaoGeneticAlteration.getInstance();
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();

        Set<Long> entrezGeneIds = new HashSet<Long>();

        Set<Long> profileGeneIds = new HashSet<>();
        if (profileType.equals(GeneticAlterationType.MUTATION_EXTENDED.toString())) { //get only genes that has mutations -- performance concern
            Set<CanonicalGene> profileGeneSet = DaoMutation.getGenesInProfile(profileId);
            for (CanonicalGene profileGene : profileGeneSet) {
                profileGeneIds.add(profileGene.getEntrezGeneId());
            }
        }

        if (geneSet.equals("cancer_genes")) {
            //get cancer genes
            Set<CanonicalGene> cancerGeneSet = daoGeneOptimized.getCbioCancerGenes();
            for (CanonicalGene cancerGene : cancerGeneSet) {
                entrezGeneIds.add(cancerGene.getEntrezGeneId());
            }
            //overlap two gene sets for mutation profile
            if (profileType.equals(GeneticAlterationType.MUTATION_EXTENDED.toString())) {
                entrezGeneIds.retainAll(profileGeneIds);
            }
        } else if (geneSet.equals("all_genes")) {
            if (profileType.equals(GeneticAlterationType.MUTATION_EXTENDED.toString())) {
                entrezGeneIds.addAll(profileGeneIds);
            } else {
                ArrayList<CanonicalGene> allGeneSet = daoGeneOptimized.getAllGenes();
                for (CanonicalGene gene: allGeneSet) {
                    entrezGeneIds.add(gene.getEntrezGeneId());
                }
            }
        }

        //join two lists
        List<Integer> sampleIds = new ArrayList<Integer>(alteredSampleIds);
        sampleIds.addAll(unalteredSampleIds);

        if (profileType.equals(GeneticAlterationType.MUTATION_EXTENDED.toString())) {
            HashMap mutHm = DaoMutation.getSimplifiedMutations(profileId, sampleIds, entrezGeneIds);
            for (Long entrezGeneId : entrezGeneIds) {
                //Assign every sample (included non mutated ones) values -- mutated -> Mutation Type, non-mutated -> "Non"
                HashMap<Integer, String> singleGeneMutMap = new HashMap<Integer, String>();
                for (Integer sampleId : sampleIds) {
                    String mutationStatus = "Non";
                    String tmpStr = new StringBuilder().append(Integer.toString(sampleId)).append(Long.toString(entrezGeneId)).toString();
                    if(mutHm.containsKey(tmpStr)) mutationStatus = "Mutated";
                    singleGeneMutMap.put(sampleId, mutationStatus);
                }
                //add a new entry into the overall result map
                result.put(entrezGeneId, singleGeneMutMap);
            }
        } else {
            //result = daoGeneticAlteration.getGeneticAlterationMap(profileId, null);
            result = daoGeneticAlteration.getGeneticAlterationMap(profileId, entrezGeneIds);
        }

        return result;
    }

}
