package org.mskcc.cgds.web_api;

import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoGeneticProfile;
import org.mskcc.cgds.dao.DaoMutation;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.model.ExtendedMutation;
import org.mskcc.cgds.model.Gene;
import org.mskcc.cgds.model.GeneticProfile;
import org.mskcc.cgds.servlet.WebService;

import java.util.ArrayList;

public class GetMutationData {
    public static int ID_ENTREZ_GENE = 1;
    public static int GENE_SYMBOL = 0;
    private static String TAB = "\t";

    public static String getProfileData(String geneticProfileId,
                                        ArrayList<String> targetGeneList,
                                        ArrayList<String> targetCaseList) throws DaoException {

        StringBuffer buf = new StringBuffer();

        //  Get the Genetic Profile
        DaoGeneticProfile daoGeneticProfile = new DaoGeneticProfile();
        GeneticProfile geneticProfile = daoGeneticProfile.getGeneticProfileByStableId
                (geneticProfileId);
        if (geneticProfile == null) {
            buf.append("No genetic profile available for " + WebService.GENETIC_PROFILE_ID + ":  "
                    + geneticProfileId + ".\n");
            return buf.toString();
        }

        //  Output Actual Data
        ArrayList<Gene> geneList = WebApiUtil.getGeneList(targetGeneList,
                geneticProfile.getGeneticAlterationType(),
                buf, new ArrayList<String>());

        //  Output DATA_TYPE
        buf.append("# DATA_TYPE\t " + geneticProfile.getProfileName() + "\n");

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
                    buf.append(canonicalGene.getEntrezGeneId() + TAB);
                    buf.append(canonicalGene.getHugoGeneSymbolAllCaps() + TAB);
                    buf.append(caseId + TAB);
                    buf.append(mutation.getSequencingCenter() + TAB);
                    buf.append(mutation.getMutationStatus() + TAB);
                    buf.append(mutation.getMutationType() + TAB);
                    buf.append(mutation.getValidationStatus() + TAB);
                    buf.append(mutation.getAminoAcidChange() + TAB);
                    buf.append(mutation.getFunctionalImpactScore() + TAB);
                    buf.append(mutation.getLinkXVar() + TAB);
                    buf.append(mutation.getLinkPdb() + TAB);
                    buf.append(mutation.getLinkMsa() + TAB);
                    buf.append(mutation.getChr() + TAB);
                    buf.append(mutation.getStartPosition() + TAB);
                    buf.append(mutation.getEndPosition());
                    buf.append("\n");
                }
            }
        }
        return buf.toString();
    }
}