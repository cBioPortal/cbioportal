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
     * @param caseIds  Space-delimited list of case IDs.
     * @return ProfileData Object.
     * @throws java.rmi.RemoteException Remote / Network IO Error.
     */
    public ArrayList <ExtendedMutation> getMutationData(GeneticProfile profile,
                ArrayList<String> geneList, String caseIds, XDebug xdebug) throws DaoException {
        try {
        ArrayList <ExtendedMutation> mutationList = new ArrayList <ExtendedMutation>();
        ArrayList<Long> entrezIDList= new ArrayList<Long>();
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
        DaoMutation daoMutation = DaoMutation.getInstance();
        int GeneticProfile = profile.getGeneticProfileId();
        //convert HUGOGENE List to ENTREZIDGENE List
        for (String gene : geneList) {
            CanonicalGene canonicalGene = daoGeneOptimized.getGene(gene);
            Long EntrezGeneID = canonicalGene.getEntrezGeneId();
            entrezIDList.add((EntrezGeneID));
        }
        //parse each Mutation List retrieved from DaoMutation and add to Main Mutation List
        for (Long entrezID : entrezIDList){
            ArrayList<ExtendedMutation> tempMutationList = daoMutation.getMutations(GeneticProfile, entrezID);
            for (ExtendedMutation mutation : tempMutationList){
                mutationList.add(mutation);
            }
        }
          return mutationList;
        } catch (DaoException e) {
          System.err.println("Database Error: " + e.getMessage());
        }
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