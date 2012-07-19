package org.mskcc.portal.model;

import org.mskcc.portal.oncoPrintSpecLanguage.GeneWithSpec;

/**
 * Encapsulates a Gene with a Score.
 * Score is used to rank genes.
 */
public class GeneWithScore {
    private String gene;
    private int entrezGeneId;
    private double score;
    // apg: was: private GeneCounterPrefs counterPrefs;
    private GeneWithSpec aGeneWithSpec;

    /**
     * Constructor.
     */
    public GeneWithScore() {
       // apg: was: counterPrefs = new GeneCounterPrefs();
       // TODO: decide if this constructor is needed: aGeneWithSpec = new GeneWithSpec();
    }

    /**
     * Gets the Gene Symbol.
     *
     * @return gene symbol.
     */
    public String getGene() {
        return gene;
    }

    /**
     * Sets the Gene Symbol.
     *
     * @param gene gene symbol.
     */
    public void setGene(String gene) {
        this.gene = gene;
    }

    /**
     * Gets Entrez Gene Id.
     *
     * @return Entrez Gene Id.
     */
    public int getEntrezGeneId() {
        return entrezGeneId;
    }

    /**
     * Sets Entrez Gene Id.
     *
     * @param entrezGeneId Entrez Gene Id;
     */
    public void setEntrezGeneId(int entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }

    /**
     * Gets the score.
     *
     * @return gene score.
     */
    public double getScore() {
        return score;
    }

    /**
     * Sets the gene score.
     *
     * @param score gene score.
     */
    public void setScore(double score) {
        this.score = score;
    }

    /**
     * Gets the Counter Prefs.
     * @return Counter Prefs.
     */
    /* apg: was
    public GeneCounterPrefs getCounterPrefs() {
        return counterPrefs;
    }
     */
   public GeneWithSpec getaGeneWithSpec() {
      return aGeneWithSpec;
   }


    /**
     * Sets the Counter Prefs;
     * @param counterPrefs Counter Prefs.
     */
    /* apg: was
    public void setCounterPrefs(GeneCounterPrefs counterPrefs) {
        this.counterPrefs = counterPrefs;
    }
    */
   public void setaGeneWithSpec(GeneWithSpec aGeneWithSpec) {
      this.aGeneWithSpec = aGeneWithSpec;
   }
    
}