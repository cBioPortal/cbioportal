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
