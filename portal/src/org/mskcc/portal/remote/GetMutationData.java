package org.mskcc.portal.remote;

import org.apache.commons.httpclient.URI;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoGeneOptimized;
import org.mskcc.cgds.dao.DaoMutation;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.model.ExtendedMutation;
import org.mskcc.portal.util.XDebug;
import org.mskcc.cgds.model.GeneticProfile;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Gets Mutation Data for all specified genes in a specific genetic profile.
 */
public class GetMutationData {
    private ArrayList<String> warningList = new ArrayList<String>();
    private URI uri;
    private String content;

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
        DaoMutation daoMutation = DaoMutation.getInstance();

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
                            daoMutation.getMutations(GeneticProfile, entrezID);
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
}