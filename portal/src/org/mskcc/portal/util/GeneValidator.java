package org.mskcc.portal.util;

import org.mskcc.cgds.dao.DaoGeneOptimized;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.portal.oncoPrintSpecLanguage.ParserOutput;

import java.util.ArrayList;

/**
 * Validates a String of Genes Specified by the User.
 *
 * @author Ethan Cerami.
 */
public class GeneValidator {
    private ArrayList<String> invalidGeneList = new ArrayList<String>();
    private ArrayList<CanonicalGene> validGeneList = new ArrayList<CanonicalGene>();

    /**
     * Constructor.
     *
     * @param geneListString List of Genes Input by the User.
     * @throws DaoException Database Error.
     */
    public GeneValidator (String geneListString) throws DaoException {
        ParserOutput theOncoPrintSpecParserOutput =
                OncoPrintSpecificationDriver.callOncoPrintSpecParserDriver(geneListString);

        //  Use the OncoPrint Parser to Easily Extract the Genes
        ArrayList<String> geneList = new ArrayList<String>();
        geneList.addAll(theOncoPrintSpecParserOutput.getTheOncoPrintSpecification().listOfGenes());

        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
        for (String currentGene:  geneList) {
            if (currentGene.trim().length() > 0) {
                CanonicalGene dbGene = daoGeneOptimized.getGene(currentGene.toUpperCase());
                if (dbGene == null) {
                    invalidGeneList.add(currentGene);
                } else {
                    validGeneList.add(dbGene);
                }
            }
        }
    }

    /**
     * Gets the Error List.
     * @return ArrayList of Error Messages.
     */
    public ArrayList<String> getInvalidGeneList() {
        return invalidGeneList;
    }

    /**
     * Gets the Valid Gene List.
     * @return ArrayList of Canonical Genes.
     */
    public ArrayList<CanonicalGene> getValidGeneList() {
        return validGeneList;
    }
}
