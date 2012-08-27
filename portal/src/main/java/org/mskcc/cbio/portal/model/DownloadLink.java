package org.mskcc.cbio.portal.model;

import org.mskcc.cbio.cgds.model.GeneticProfile;

import java.util.ArrayList;

/**
 * Encapsulates a Download Link.
 */
public class DownloadLink {
    private GeneticProfile profile;
    private ArrayList<String> geneList;
    private String caseIds;
    private String content;

    /**
     * Constructor.
     *
     * @param profile  GeneticProfile Object.
     * @param geneList ArrayList of Gene Symbols.
     * @param caseIds  Whitespace-delimited list of case Ids.
     * @param content  Content from the CGDS Server.
     */
    public DownloadLink(GeneticProfile profile, ArrayList<String> geneList,
                        String caseIds, String content) {
        this.profile = profile;
        this.geneList = geneList;
        this.caseIds = caseIds;
        this.content = content;
    }

    /**
     * Gets the Genetic Profile.
     *
     * @return GeneticProfile Object.
     */
    public GeneticProfile getProfile() {
        return profile;
    }

    /**
     * Gets the Gene List.
     *
     * @return ArrayList of Gene Symbols.
     */
    public ArrayList<String> getGeneList() {
        return geneList;
    }

    /**
     * Gets the Case IDs.
     *
     * @return whitespace-delimited list of case IDs.
     */
    public String getCaseIds() {
        return caseIds;
    }

    /**
     * Gets the content returned by the CGDS Server.
     *
     * @return CGDS Content.
     */
    public String getContent() {
        return content;
    }
}
