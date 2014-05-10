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

package org.mskcc.cbio.portal.web_api;

import org.mskcc.cbio.portal.dao.DaoMutation;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneticProfile;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.ExtendedMutation;
import org.mskcc.cbio.portal.model.Gene;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.servlet.WebService;
import org.mskcc.cbio.portal.util.XDebug;

import org.apache.commons.httpclient.URI;

import java.util.HashSet;
import java.util.ArrayList;

/**
 * Class to get mutation data
 */
public class GetMutationData {

    public static final int GENE_SYMBOL = 0;
    public static final int ID_ENTREZ_GENE = 1;

    private static final String TAB = "\t";

    private URI uri;
    private String content;
    private ArrayList<String> warningList = new ArrayList<String>();

    /**
     * Gets MutationData Data for all specified genes in a specific genetic profile.
     *
     * @param profile  GeneticProfile Object.
     * @param geneList ArrayList of official gene symbols.
     * @param caseIdSet HashSet of Strings which are Case Ids.
     * @return ProfileData Object in an ArrayList.
     * @throws DaoException, as of August 2011 GetMutationData has direct access to DAO Objects.
     */
    public ArrayList<ExtendedMutation> getMutationData(GeneticProfile profile,
                                                       ArrayList<String> geneList, HashSet<String> caseIdSet, XDebug xdebug) throws DaoException {

        //initialize DAO objects and ArrayLists
        ArrayList<ExtendedMutation> mutationList = new ArrayList<ExtendedMutation>();
        ArrayList<Long> entrezIDList = new ArrayList<Long>();
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();

        //Get Genetic Profile ID from GeneticProfile Object
        int GeneticProfile = profile.getGeneticProfileId();
        if (geneList.size() > 0) {
            //convert HUGOGENE List to ENTREZIDGENE List
            for (String gene : geneList) {
                if (null != gene) {
                    CanonicalGene canonicalGene = daoGeneOptimized.getGene(gene);
                    if(null != canonicalGene){
                    Long EntrezGeneID = canonicalGene.getEntrezGeneId();
                    entrezIDList.add((EntrezGeneID));
                    }
                }
            }
            try {
                //parse each Mutation List retrieved from DaoMutation and add to Main Mutation List
                for (Long entrezID : entrezIDList) {
                    ArrayList<ExtendedMutation> tempmutationList =
                            DaoMutation.getMutations(GeneticProfile, entrezID);
                    for (ExtendedMutation mutation : tempmutationList){
                        // seperate out mutations for the given set of caseIDS.
                        if (caseIdSet.contains(mutation.getCaseId()))
                            mutationList.add(mutation);
                    }

                }
                return mutationList;
            } catch (DaoException e) {
                System.err.println("Database Error: " + e.getMessage());
            }
        }
        System.err.println("Invalid list of Genes entered");
        return null;
    }

    /**
     * Gets warnings (if triggered).
     *
     * @return ArrayList of Warning Strings.
     */
    public ArrayList<String> getWarnings() {
        return this.warningList;
    }

    /**
     * Gets URI for connecting to remote server.
     *
     * @return URI.
     */
    public URI getURI() {
        return this.uri;
    }

    /**
     * Gets Full Content retrieved from the CGDS.
     *
     * @return Full Content.
     */
    public String getContent() {
        return this.content;
    }

    public static String getProfileData(String geneticProfileId,
                                        ArrayList<String> targetGeneList,
                                        ArrayList<String> targetCaseList) throws DaoException {

        StringBuffer buf = new StringBuffer();

        //  Get the Genetic Profile
        GeneticProfile geneticProfile = DaoGeneticProfile.getGeneticProfileByStableId
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
        buf.append(getColumnHeaders()).append("\n");

	    // TODO get data directly from MutationDataUtils and iterate the returned data
        //  Iterate through all validated genes, and extract mutation data.
        for (Gene gene : geneList) {
            CanonicalGene canonicalGene = (CanonicalGene) gene;
            ArrayList<ExtendedMutation> mutationList =
                    DaoMutation.getMutations(geneticProfile.getGeneticProfileId(),
                            canonicalGene.getEntrezGeneId());
            for (ExtendedMutation mutation:  mutationList) {
                String caseId = mutation.getCaseId();
                if (targetCaseList==null || targetCaseList.contains(caseId)) {
                    buf.append(canonicalGene.getEntrezGeneId()).append(TAB);
                    buf.append(canonicalGene.getHugoGeneSymbolAllCaps()).append(TAB);
                    buf.append(caseId).append(TAB);
                    buf.append(mutation.getSequencingCenter()).append(TAB);
                    buf.append(mutation.getMutationStatus()).append(TAB);
                    buf.append(mutation.getMutationType()).append(TAB);
                    buf.append(mutation.getValidationStatus()).append(TAB);
                    buf.append(mutation.getProteinChange()).append(TAB);
                    buf.append(mutation.getFunctionalImpactScore()).append(TAB);
                    buf.append(mutation.getLinkXVar()).append(TAB);
                    buf.append(mutation.getLinkPdb()).append(TAB);
                    buf.append(mutation.getLinkMsa()).append(TAB);
                    buf.append(mutation.getChr()).append(TAB);
                    buf.append(mutation.getStartPosition()).append(TAB);
                    buf.append(mutation.getEndPosition()).append(TAB);
	                buf.append(mutation.getReferenceAllele()).append(TAB);
	                buf.append(mutation.getTumorSeqAllele()).append(TAB);
	                buf.append(getAlleleCount(mutation.getTumorRefCount())).append(TAB);
	                buf.append(getAlleleCount(mutation.getTumorAltCount())).append(TAB);
	                buf.append(getAlleleCount(mutation.getNormalRefCount())).append(TAB);
	                buf.append(getAlleleCount(mutation.getNormalAltCount())).append(TAB);
                    buf.append(geneticProfileId);
                    buf.append("\n");
                }
            }
        }
        return buf.toString();
    }

	private static String getAlleleCount(Integer count)
	{
		if (count < 0)
		{
			return "NA";
		}
		else
		{
			return count.toString();
		}
	}

	private static String getColumnHeaders()
	{
		StringBuffer buf = new StringBuffer();

		// TODO externalize headers to an array and pair with corresponding data fields
		buf.append("entrez_gene_id").append(TAB);
		buf.append("gene_symbol").append(TAB);
		buf.append("case_id").append(TAB);
		buf.append("sequencing_center").append(TAB);
		buf.append("mutation_status").append(TAB);
		buf.append("mutation_type").append(TAB);
		buf.append("validation_status").append(TAB);
		buf.append("amino_acid_change").append(TAB);
		buf.append("functional_impact_score").append(TAB);
		buf.append("xvar_link").append(TAB);
		buf.append("xvar_link_pdb").append(TAB);
		buf.append("xvar_link_msa").append(TAB);
		buf.append("chr").append(TAB);
		buf.append("start_position").append(TAB);
		buf.append("end_position").append(TAB);
		buf.append("reference_allele").append(TAB);
		buf.append("variant_allele").append(TAB);
		buf.append("reference_read_count_tumor").append(TAB);
		buf.append("variant_read_count_tumor").append(TAB);
		buf.append("reference_read_count_normal").append(TAB);
		buf.append("variant_read_count_normal").append(TAB);
		buf.append("genetic_profile_id");

		return buf.toString();
	}
}