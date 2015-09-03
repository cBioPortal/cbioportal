package org.mskcc.cbio.portal.or_analysis;

import java.util.*;

import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.dao.*;


public class OverRepresentationAnalysisUtil {
    
    public static Map<Long, HashMap<Integer, String>> getValueMap(
            int cancerStudyId, int profileId, String profileType, List<Integer> alteredSampleIds, List<Integer> unalteredSampleIds, String geneSet, String proteinExpType) throws DaoException {

        DaoGeneticAlteration daoGeneticAlteration = DaoGeneticAlteration.getInstance();
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();

        Set<Long> entrezGeneIds = new HashSet<Long>();
        if (proteinExpType.equals("phospho")) {
            ArrayList<CanonicalGene> allGeneSet = daoGeneOptimized.getAllGenes();
            for (CanonicalGene gene: allGeneSet) {
                if (gene.isPhosphoProtein()) { //only get the phospho-protein genes
                    entrezGeneIds.add(gene.getEntrezGeneId());
                }
            }
        } else {
            if (geneSet.equals("cancer_genes")) {
                //get cancer genes
                Set<CanonicalGene> cancerGeneSet = daoGeneOptimized.getCbioCancerGenes();
                for (CanonicalGene cancerGene : cancerGeneSet) {
                    entrezGeneIds.add(cancerGene.getEntrezGeneId());
                }
            } else if (geneSet.equals("all_genes")) {
                ArrayList<CanonicalGene> allGeneSet = daoGeneOptimized.getAllGenes();
                for (CanonicalGene gene: allGeneSet) {
                    entrezGeneIds.add(gene.getEntrezGeneId());
                }
            }
        }

        //join two lists
        List<Integer> sampleIds = new ArrayList<Integer>(alteredSampleIds);
        sampleIds.addAll(unalteredSampleIds);
        
        //Map<GeneId, HashMap<CaseId, Value>>
        Map<Long, HashMap<Integer, String>> result = new HashMap<Long, HashMap<Integer,String>>();

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
            result = daoGeneticAlteration.getGeneticAlterationMap(profileId, entrezGeneIds);
        }
        return result;
    }

}
