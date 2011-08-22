package org.mskcc.cgds.web_api;

import org.mskcc.cgds.dao.*;
import org.mskcc.cgds.model.*;
import org.mskcc.cgds.servlet.WebService;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Web Service to Get Profile Data.
 */
public class GetProfileData {
    public static int ID_ENTREZ_GENE = 1;
    public static int GENE_SYMBOL = 0;

    public static String getProfileData(ArrayList<String> geneticProfileIdList,
										ArrayList<String> targetGeneList, 
										ArrayList<String> targetCaseList,
										Boolean suppressMondrianHeader) throws DaoException {

        StringBuffer buf = new StringBuffer();

        //  Get the Genetic Profile
        DaoGeneticProfile daoGeneticProfile = new DaoGeneticProfile();
        for (String geneticProfileId:  geneticProfileIdList) {
            GeneticProfile geneticProfile =
                    daoGeneticProfile.getGeneticProfileByStableId(geneticProfileId);
            if (geneticProfile == null) {
                buf.append ("No genetic profile available for " + WebService.GENETIC_PROFILE_ID + ":  "
                        + geneticProfileId +".\n");
                return buf.toString();
            }
        }

        //  Figure out Case List
        String geneticProfileId = geneticProfileIdList.get(0);
        GeneticProfile geneticProfile =
                daoGeneticProfile.getGeneticProfileByStableId(geneticProfileId);

        if (geneticProfileIdList.size() == 1) {
            geneticProfileId = geneticProfileIdList.get(0);
            geneticProfile = daoGeneticProfile.getGeneticProfileByStableId(geneticProfileId);

            //  Get the Gene List
            ArrayList<Gene> geneList = WebApiUtil.getGeneList(targetGeneList,
                    geneticProfile.getGeneticAlterationType(), buf);

            //  Output DATA_TYPE and COLOR_GRADIENT_SETTINGS
            if (!suppressMondrianHeader) {
                buf.append ("# DATA_TYPE\t " + geneticProfile.getProfileName() +"\n");
                buf.append ("# COLOR_GRADIENT_SETTINGS\t "
                            + geneticProfile.getGeneticAlterationType() + "\n");
            }

            //  Ouput Column Headings
            buf.append ("GENE_ID\tCOMMON");
            for (String caseId:  targetCaseList) {
                buf.append ("\t" + caseId);
            }
            buf.append ("\n");

            //  Iterate through all validated genes, and extract profile data.
            for (Gene gene: geneList) {
                outputDataRow(gene, targetCaseList, geneticProfile, buf);
            }
        } else {
            //  Get the Gene List
            ArrayList<Gene> geneList = WebApiUtil.getGeneList(targetGeneList,
                    geneticProfile.getGeneticAlterationType(), buf);

            //  Ouput Column Headings
            buf.append ("GENETIC_PROFILE_ID\tALTERATION_TYPE\tGENE_ID\tCOMMON");
            for (String caseId:  targetCaseList) {
                buf.append ("\t" + caseId);
            }
            buf.append ("\n");

            //  Iterate through all genetic profiles
            for (String gId:  geneticProfileIdList) {
                geneticProfile = daoGeneticProfile.getGeneticProfileByStableId(gId);

                if (geneList.size() > 0) {
                    Gene gene = geneList.get(0);
                    buf.append (geneticProfile.getStableId() + "\t"
                            + geneticProfile.getGeneticAlterationType() + "\t");
                    outputDataRow(gene, targetCaseList, geneticProfile, buf);
                }
            }
                
        }
        return buf.toString();
    }

    private static void outputDataRow(Gene gene, ArrayList<String> targetCaseList,
            GeneticProfile geneticProfile, StringBuffer buf) 
            throws DaoException {
        DaoGeneticAlteration daoGeneticAlteration = DaoGeneticAlteration.getInstance();
        DaoMicroRnaAlteration daoMicroRnaAlteration = DaoMicroRnaAlteration.getInstance();
        if (gene instanceof CanonicalGene) {
            CanonicalGene canonicalGene = (CanonicalGene) gene;
            buf.append (canonicalGene.getEntrezGeneId() + "\t");
            buf.append (canonicalGene.getHugoGeneSymbol());

            HashMap<String, String> caseMap;

            //  Handle Mutations one way
            if (geneticProfile.getGeneticAlterationType() ==
                    GeneticAlterationType.MUTATION_EXTENDED) {
                caseMap = getMutationMap (targetCaseList, geneticProfile.getGeneticProfileId(),
                        canonicalGene.getEntrezGeneId());
            } else if (geneticProfile.getGeneticAlterationType() ==
                    GeneticAlterationType.PROTEIN_ARRAY_PROTEIN_LEVEL) {
                caseMap = getProteinArrayDataMap (targetCaseList, canonicalGene.getEntrezGeneId(), "protein_level");
            } else if (geneticProfile.getGeneticAlterationType() ==
                    GeneticAlterationType.PROTEIN_ARRAY_PHOSPHORYLATION) {
                caseMap = getProteinArrayDataMap (targetCaseList, canonicalGene.getEntrezGeneId(), "phosphorylation");
            } else {
                //  Handle All Other Data Types another way
                caseMap = daoGeneticAlteration.getGeneticAlterationMap
                        (geneticProfile.getGeneticProfileId(), canonicalGene.getEntrezGeneId());
            }

            //  Iterate through all cases in the profile
            for (String caseId:  targetCaseList) {
                String value = caseMap.get(caseId);
                if (value == null) {
                    buf.append ("\tNaN");
                } else {
                    buf.append ("\t" + value);
                }
            }
            buf.append ("\n");
        } else if (gene instanceof MicroRna) {
            MicroRna microRna = (MicroRna) gene;
            buf.append ("-999999" + "\t");
            buf.append (microRna.getMicroRnaId());

            HashMap<String, String> caseMap = daoMicroRnaAlteration.getMicroRnaAlterationMap
                    (geneticProfile.getGeneticProfileId(), microRna.getMicroRnaId());

            //  Iterate through all cases in the profile
            for (String caseId:  targetCaseList) {
                String value = caseMap.get(caseId);
                if (value == null) {
                    buf.append ("\tNaN");
                } else {
                    buf.append ("\t" + value);
                }
            }
            buf.append ("\n");
        }
    }

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

    private static HashMap <String, String> getProteinArrayDataMap (ArrayList<String> targetCaseList,
            long entrezGeneId, String type) throws DaoException {
        DaoProteinArrayInfo daoPAI = DaoProteinArrayInfo.getInstance();
        DaoProteinArrayTarget daoPAT = DaoProteinArrayTarget.getInstance();
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