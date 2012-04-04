package org.mskcc.cgds.dao;

import org.mskcc.cgds.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.apache.commons.math.stat.correlation.PearsonsCorrelation;

/**
 * Utility Class for Retrieving Genetic Alteration Data.
 *
 * This class is a wrapper for multiple DAO Classes, and enables you to retrieve different types
 * of genomic data, and based on different types of genes, e.g. canonical (protein-coding) genes
 * and microRNAs.
 *
 * @author Ethan Cerami.
 */
public class GeneticAlterationUtil {
    private static final String NAN = "NaN";

    /**
     * Gets a Row of data corresponding to:  target gene, within the target genetic profile
     * and only within the target cases.
     *
     * @param targetGene                Target Gene.
     * @param targetCaseList            Target Case List.
     * @param targetGeneticProfile      Target Genetic Profile.
     * @return Array List of String values.
     * @throws DaoException Database Error.
     */
    public static ArrayList<String> getGeneticAlterationDataRow  (Gene targetGene,
            ArrayList<String> targetCaseList, GeneticProfile targetGeneticProfile)
            throws DaoException {
        ArrayList<String> dataRow = new ArrayList<String>();
        DaoGeneticAlteration daoGeneticAlteration = DaoGeneticAlteration.getInstance();
        DaoMicroRnaAlteration daoMicroRnaAlteration = DaoMicroRnaAlteration.getInstance();

        //  First branch:  are we dealing with a canonical (protein-coding) gene or a microRNA?
        if (targetGene instanceof CanonicalGene) {
            CanonicalGene canonicalGene = (CanonicalGene) targetGene;
            Map<String, String> caseMap;

            //  Handle Mutations one way
            if (targetGeneticProfile.getGeneticAlterationType() ==
                    GeneticAlterationType.MUTATION_EXTENDED) {
                caseMap = getMutationMap (targetCaseList, targetGeneticProfile.getGeneticProfileId(),
                        canonicalGene.getEntrezGeneId());
            } else if (targetGeneticProfile.getGeneticAlterationType() ==
                    GeneticAlterationType.PROTEIN_ARRAY_PROTEIN_LEVEL) {
                String type = canonicalGene.isPhosphoProtein() ? 
                        GeneticAlterationType.PHOSPHORYLATION.toString():GeneticAlterationType.PROTEIN_LEVEL.toString();
                caseMap = getProteinArrayDataMap (targetGeneticProfile.getCancerStudyId(),
                        targetCaseList, canonicalGene, type, null)[0];
            } else if (targetGeneticProfile.getGeneticAlterationType() ==
                    GeneticAlterationType.PROTEIN_ARRAY_PHOSPHORYLATION) {
                caseMap = getProteinArrayDataMap (targetGeneticProfile.getCancerStudyId(),
                        targetCaseList, canonicalGene,
                        GeneticAlterationType.PHOSPHORYLATION.toString(),null)[0];
            } else {
                //  Handle All Other Data Types another way
                caseMap = daoGeneticAlteration.getGeneticAlterationMap
                        (targetGeneticProfile.getGeneticProfileId(),
                                canonicalGene.getEntrezGeneId());
            }

            //  Iterate through all cases in the profile
            for (String caseId:  targetCaseList) {
                String value = caseMap.get(caseId);
                if (value == null) {
                    dataRow.add (NAN);
                } else {
                    dataRow.add (value);
                }
            }
        } else if (targetGene instanceof MicroRna) {
            MicroRna microRna = (MicroRna) targetGene;
            HashMap<String, String> caseMap = daoMicroRnaAlteration.getMicroRnaAlterationMap
                    (targetGeneticProfile.getGeneticProfileId(), microRna.getMicroRnaId());

            //  Iterate through all cases in the profile
            for (String caseId:  targetCaseList) {
                String value = caseMap.get(caseId);
                if (value == null) {
                    dataRow.add (NAN);
                } else {
                    dataRow.add (value);
                }
            }
        }
        return dataRow;
    }
    
    public static ArrayList<String> getBestCorrelatedProteinArrayDataRow(
            int cancerStudyId, CanonicalGene targetGene,
            ArrayList<String> targetCaseList, ArrayList<String> correlatedToData)
            throws DaoException {
        ArrayList<String> dataRow = new ArrayList<String>();
        
        String type = targetGene.isPhosphoProtein() ? 
                GeneticAlterationType.PHOSPHORYLATION.toString():GeneticAlterationType.PROTEIN_LEVEL.toString();
        
        Map<String, String>[] caseMaps = getProteinArrayDataMap(cancerStudyId, targetCaseList, targetGene,
                        type,null);
        
        Map<String, String> caseMap = getBestCorrelatedCaseMap(caseMaps, targetCaseList, correlatedToData);
        //  Iterate through all cases in the profile
        for (String caseId:  targetCaseList) {
            String value = caseMap.get(caseId);
            if (value == null) {
                dataRow.add (NAN);
            } else {
                dataRow.add (value);
            }
        }
        return dataRow;
    }

    /**
     * Gets a Map of Mutation Data.
     */
    private static HashMap <String, String> getMutationMap (ArrayList<String> targetCaseList,
            int geneticProfileId, long entrezGeneId) throws DaoException {
        DaoMutation daoMutation = DaoMutation.getInstance();

        HashMap <String, String> mutationMap = new HashMap <String, String>();
        ArrayList <ExtendedMutation> mutationList =
                    daoMutation.getMutations(geneticProfileId, targetCaseList, entrezGeneId);
        
        for (ExtendedMutation mutation : mutationList) {
            String caseId = mutation.getCaseId();
            //  Handle the possibility of multiple mutations in the same gene / patient
            //  Handles issue:  165
            if (mutationMap.containsKey(caseId)) {
                String existingStr = mutationMap.get(caseId);
                mutationMap.put(caseId, existingStr + "," + mutation.getAminoAcidChange());
            } else {
                mutationMap.put(caseId, mutation.getAminoAcidChange());
            }
        }
        return mutationMap;
    }

    /**
     * Gets a Map of Protein Array Data.
     */
    private static Map <String, String>[] getProteinArrayDataMap
            (int cancerStudyId, ArrayList<String> targetCaseList, CanonicalGene canonicalGene, String type,
            ArrayList<String> correlatedToData) throws DaoException {
        DaoProteinArrayInfo daoPAI = DaoProteinArrayInfo.getInstance();
        DaoProteinArrayData daoPAD = DaoProteinArrayData.getInstance();
        
        Map <String, String>[] ret;

        List<String> arrayIds = new ArrayList<String>();
        if (canonicalGene.isPhosphoProtein()) {
            //TODO: this is somewhat hacky way--rppa array ids have to be aliases of the phosphoprotein
            for (String arrayId : canonicalGene.getAliases()) {
                ProteinArrayInfo pai = daoPAI.getProteinArrayInfo(arrayId);
                if (pai!=null && pai.getCancerStudies().contains(cancerStudyId)) {
                    arrayIds.add(arrayId);
                }
            }
        } else {
            for (ProteinArrayInfo pai : daoPAI.getProteinArrayInfoForEntrezId(
                    cancerStudyId, canonicalGene.getEntrezGeneId(), Collections.singleton(type))) {
                arrayIds.add(pai.getId());
            }
        }
        
        if (arrayIds.isEmpty()) {
            ret = new Map[1];
            Map <String, String> map = Collections.emptyMap();
            ret[0] = map;
            return ret;
        }

        int n = correlatedToData==null ? 1:arrayIds.size();
        ret = new Map[n];
        
        for (int i=0; i<n; i++) {
            String arrayId = arrayIds.get(0);

            if (arrayId == null) {
                continue;
            }

            ret[i] = new HashMap<String,String>();
            List<ProteinArrayData> pads = daoPAD.getProteinArrayData(arrayId, targetCaseList);
            for (ProteinArrayData pad : pads) {
                ret[i].put(pad.getCaseId(), Double.toString(pad.getAbundance()));
            }
        }
        return ret;
    }
    
    private static Map<String,String> getBestCorrelatedCaseMap(Map<String, String>[] caseMaps,
            ArrayList<String> targetCaseList, ArrayList<String> correlatedToData) {
        if (caseMaps.length==1 || correlatedToData==null || correlatedToData.size()!= targetCaseList.size()) {
            return caseMaps[0];
        }
        
        Map<String, String> ret = null;
        double maxCorr = Double.NEGATIVE_INFINITY;
        for (Map<String, String> map : caseMaps) {
            double corr = calcCorrCoef(map, targetCaseList, correlatedToData);
            if (corr > maxCorr) {
                maxCorr = corr;
                ret = map;
            }
        }
        
        return ret;
    }
    
    private static double calcCorrCoef(Map<String, String> caseMap,
            ArrayList<String> targetCaseList, ArrayList<String> correlatedToData) {
        try {
            List<Double> l1 = new ArrayList<Double>();
            List<Double> l2 = new ArrayList<Double>();
            for (int i=0; i<targetCaseList.size(); i++) {
                String targetCase = targetCaseList.get(i);
                if (correlatedToData.get(i).equals("NAN")) {
                    continue;
                }

                String abun = caseMap.get(targetCase);
                if (abun==null) {
                    continue;
                }

                l1.add(Double.valueOf(abun));
                l2.add(Double.valueOf(correlatedToData.get(i)));
            }

            int n = l1.size();
            double[] d1 = new double[n];
            double[] d2 = new double[n];
            for (int i=0; i<n; i++) {
                d1[i] = l1.get(i);
                d2[i] = l2.get(i);
            }

            PearsonsCorrelation pc = new PearsonsCorrelation();
            return pc.correlation(d1, d2);
        } catch (Exception e) {
            return -2.0;
        }
    }
}
