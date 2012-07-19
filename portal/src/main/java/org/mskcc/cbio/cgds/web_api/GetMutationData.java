package org.mskcc.cbio.cgds.web_api;

import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.dao.DaoGeneticProfile;
import org.mskcc.cbio.cgds.dao.DaoMutation;
import org.mskcc.cbio.cgds.model.CanonicalGene;
import org.mskcc.cbio.cgds.model.ExtendedMutation;
import org.mskcc.cbio.cgds.model.Gene;
import org.mskcc.cbio.cgds.model.GeneticProfile;
import org.mskcc.cbio.cgds.servlet.WebService;

import java.util.ArrayList;

/**
 * Class to get mutation data
 */
public class GetMutationData {
    public static final int ID_ENTREZ_GENE = 1;
    public static final int GENE_SYMBOL = 0;
    private static final String TAB = "\t";

    public static String getProfileData(String geneticProfileId,
                                        ArrayList<String> targetGeneList,
                                        ArrayList<String> targetCaseList) throws DaoException {

        StringBuffer buf = new StringBuffer();

        //  Get the Genetic Profile
        DaoGeneticProfile daoGeneticProfile = new DaoGeneticProfile();
        GeneticProfile geneticProfile = daoGeneticProfile.getGeneticProfileByStableId
                (geneticProfileId);
        if (geneticProfile == null) {
            buf.append("No genetic profile available for ").append(WebService.GENETIC_PROFILE_ID)
                    .append(":  ").append(geneticProfileId).append(".\n");
            return buf.toString();
        }

        //  Output Actual Data
        ArrayList<Gene> geneList = WebApiUtil.getGeneList(targetGeneList,
                geneticProfile.getGeneticAlterationType(),
                buf, new ArrayList<String>());

        //  Output DATA_TYPE
        buf.append("# DATA_TYPE\t ").append(geneticProfile.getProfileName()).append("\n");

        //  Ouput Column Headings
        buf.append("entrez_gene_id\tgene_symbol\tcase_id\tsequencing_center\t");
        buf.append("mutation_status\tmutation_type\tvalidation_status\t");
        buf.append("amino_acid_change\t");
        buf.append("functional_impact_score\t");
        buf.append("xvar_link\t");
        buf.append("xvar_link_pdb\t");
        buf.append("xvar_link_msa\t");
        buf.append("chr\t");
        buf.append("start_position\t");
        buf.append("end_position");
        buf.append("\n");

        //  Iterate through all validated genes, and extract mutation data.
        DaoMutation daoMutation = DaoMutation.getInstance();
        for (Gene gene : geneList) {
            CanonicalGene canonicalGene = (CanonicalGene) gene;
            ArrayList<ExtendedMutation> mutationList =
                    daoMutation.getMutations(geneticProfile.getGeneticProfileId(),
                            canonicalGene.getEntrezGeneId());
            for (ExtendedMutation mutation:  mutationList) {
                String caseId = mutation.getCaseId();
                if (targetCaseList.contains(caseId)) {
                    buf.append(canonicalGene.getEntrezGeneId()).append(TAB);
                    buf.append(canonicalGene.getHugoGeneSymbolAllCaps()).append(TAB);
                    buf.append(caseId).append(TAB);
                    buf.append(mutation.getSequencingCenter()).append(TAB);
                    buf.append(mutation.getMutationStatus()).append(TAB);
                    buf.append(mutation.getMutationType()).append(TAB);
                    buf.append(mutation.getValidationStatus()).append(TAB);
                    buf.append(mutation.getAminoAcidChange()).append(TAB);
                    buf.append(mutation.getFunctionalImpactScore()).append(TAB);
                    buf.append(mutation.getLinkXVar()).append(TAB);
                    buf.append(mutation.getLinkPdb()).append(TAB);
                    buf.append(mutation.getLinkMsa()).append(TAB);
                    buf.append(mutation.getChr()).append(TAB);
                    buf.append(mutation.getStartPosition()).append(TAB);
                    buf.append(mutation.getEndPosition());
                    buf.append("\n");
                }
            }
        }
        return buf.toString();
    }
}