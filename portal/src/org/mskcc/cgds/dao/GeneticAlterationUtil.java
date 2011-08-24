package org.mskcc.cgds.dao;

import org.mskcc.cgds.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
            HashMap<String, String> caseMap;

            //  Handle Mutations one way
            if (targetGeneticProfile.getGeneticAlterationType() ==
                    GeneticAlterationType.MUTATION_EXTENDED) {
                caseMap = getMutationMap (targetCaseList, targetGeneticProfile.getGeneticProfileId(),
                        canonicalGene.getEntrezGeneId());
            } else if (targetGeneticProfile.getGeneticAlterationType() ==
                    GeneticAlterationType.PROTEIN_ARRAY_PROTEIN_LEVEL) {
                caseMap = getProteinArrayDataMap (targetCaseList, canonicalGene.getEntrezGeneId(),
                        GeneticAlterationType.PROTEIN_LEVEL.toString());
            } else if (targetGeneticProfile.getGeneticAlterationType() ==
                    GeneticAlterationType.PROTEIN_ARRAY_PHOSPHORYLATION) {
                caseMap = getProteinArrayDataMap (targetCaseList, canonicalGene.getEntrezGeneId(),
                        GeneticAlterationType.PHOSPHORYLATION.toString());
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

    /**
     * Gets a Map of Mutation Data.
     */
    private static HashMap <String, String> getMutationMap (ArrayList<String> targetCaseList,
            int geneticProfileId, long entrezGeneId) throws DaoException {
        DaoMutation daoMutation = DaoMutation.getInstance();

        HashMap <String, String> mutationMap = new HashMap <String, String>();
        for (String caseId:  targetCaseList) {
            ArrayList <ExtendedMutation> mutationList =
                    daoMutation.getMutations(geneticProfileId, caseId, entrezGeneId);
            if (mutationList.size() > 0) {
                ExtendedMutation mutation = mutationList.get(0);
                mutationMap.put(caseId, mutation.getAminoAcidChange());
            }
        }
        return mutationMap;
    }

    /**
     * Gets a Map of Protein Array Data.
     */
    private static HashMap <String, String> getProteinArrayDataMap
            (ArrayList<String> targetCaseList, long entrezGeneId, String type) throws DaoException {
        DaoProteinArrayInfo daoPAI = DaoProteinArrayInfo.getInstance();
        DaoProteinArrayData daoPAD = DaoProteinArrayData.getInstance();

        HashMap <String, String> arrayDataMap = new HashMap <String, String>();

        ArrayList<ProteinArrayInfo> pais = daoPAI.getProteinArrayInfoForEntrezId(entrezGeneId, type);
        if (pais.isEmpty())
            return arrayDataMap;

        ProteinArrayInfo pai = pais.get(0);
        String arrayId = pai.getId();

        if (arrayId == null)
            return arrayDataMap;

        List<ProteinArrayData> pads = daoPAD.getProteinArrayData(arrayId, targetCaseList);
        for (ProteinArrayData pad : pads) {
            arrayDataMap.put(pad.getCaseId(), Double.toString(pad.getAbundance()));
        }
        return arrayDataMap;
    }
}