package org.mskcc.cbio.portal.model;

/**
 * Encapsulates a set of genes.
 */
public class GeneSet {
    private String name;
    private String geneList;

    /**
     * Gets the name of the gene set.
     *
     * @return gene set name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the alpha-numeric ID of the gene set.
     *
     * @return alpha-numeric ID of gene set.
     */
    public String getId() {
        String id = name.replaceAll(" ", "-");
        id = id.replaceAll("_", "-");
        return id.toLowerCase();
    }

    /**
     * Sets the name of the gene set.
     *
     * @param name gene set name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the List of Genes in the Set.
     *
     * @return whitespace delimited list of gene symbols.
     */
    public String getGeneList() {
        return geneList;
    }

    /**
     * Sets the List of Genes in the Set.
     *
     * @param geneList whitespace delimited list of gene symbols.
     */
    public void setGeneList(String geneList) {
        this.geneList = geneList;
    }
}
