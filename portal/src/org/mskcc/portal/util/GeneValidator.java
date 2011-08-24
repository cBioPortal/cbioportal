package org.mskcc.portal.util;

import org.mskcc.cgds.dao.DaoGeneOptimized;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.model.CanonicalGene;

import java.util.ArrayList;

/**
 * Validates a String of Genes Specified by the User.
 *
 * @author Ethan Cerami.
 */
public class GeneValidator {
    private ArrayList<String> invalidGeneList = new ArrayList<String>();
    private ArrayList<CanonicalGene> geneList = new ArrayList<CanonicalGene>();

    /**
     * Constructor.
     *
     * @param geneListString List of Genes Input by the User.
     * @throws DaoException Database Error.
     */
    public GeneValidator (String geneListString) throws DaoException {
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
        String genes[] = geneListString.split("\\s+");
        for (String currentGene:  genes) {
            String parts[] = currentGene.split(":");
            String geneId = parts[0];
            if (geneId.trim().length() > 0) {
                CanonicalGene gene = daoGeneOptimized.getGene(geneId);
                if (gene == null) {
                    invalidGeneList.add(geneId);
                } else {
                    geneList.add(gene);
                }
            }
        }
    }

    /**
     * Gets the Error List.
     * @return ArrayList of Error Messages.
     */
    public ArrayList getInvalidGeneList() {
        return invalidGeneList;
    }

    /**
     * Gets the Gene List.
     * @return ArrayList of Canonical Genes.
     */
    public ArrayList<CanonicalGene> getGeneList() {
        return geneList;
    }
}
