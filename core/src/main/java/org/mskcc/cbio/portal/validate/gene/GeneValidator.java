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

package org.mskcc.cbio.portal.validate.gene;

import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.ParserOutput;
import org.mskcc.cbio.portal.util.OncoPrintSpecificationDriver;

import java.util.ArrayList;

/**
 * Validates a String of Genes Specified by the Client/User.
 *
 * @author Ethan Cerami.
 */
public class GeneValidator {
    public static final int DEFAULT_MAX_NUM_GENES = 100;
    private ArrayList<String> invalidGeneList = new ArrayList<String>();
    private ArrayList<CanonicalGene> validGeneList = new ArrayList<CanonicalGene>();
    private int maxNumGenes = DEFAULT_MAX_NUM_GENES;

    /**
     * Constructor.
     *
     * @param clientGeneList String of Genes, possibly encoded as OQL, Input by the Client.
     * @throws DaoException            Database Error.
     * @throws GeneValidationException Gene Validation Error.
     */
    public GeneValidator(String clientGeneList) throws DaoException, GeneValidationException {
        performAllChecks(clientGeneList);
    }

    public GeneValidator(String clientGeneList, int maxNumGenes) throws DaoException,
            GeneValidationException {
        this.maxNumGenes = maxNumGenes;
        performAllChecks(clientGeneList);
    }

    /**
     * Gets the List of Gene Symbols which did validate.
     *
     * @return ArrayList of Canonical Genes.
     */
    public ArrayList<CanonicalGene> getValidGeneList() {
        return validGeneList;
    }

    private void performAllChecks(String clientGeneList) throws GeneValidationException,
            DaoException {
        checkNullGenes(clientGeneList);
        checkInvalidGenes(clientGeneList);
        checkTooManyGenes();
    }

    private void checkInvalidGenes(String clientGeneList) throws DaoException,
            GeneValidationException {
        validateGenes(clientGeneList);
        if (invalidGeneList.size() > 0) {
            throw new InvalidGeneSymbolException(invalidGeneList);
        }
    }

    private void checkTooManyGenes() throws GeneValidationException {
        int numGenes = validGeneList.size();
        if (numGenes > maxNumGenes) {
            throw new TooManyGenesException(numGenes, maxNumGenes);
        }
    }

    private void checkNullGenes(String clientGeneList) throws GeneValidationException {
        if (clientGeneList == null || clientGeneList.trim().length() == 0) {
            throw new GeneValidationException("No genes specified.  Please specify at " +
                    "least one gene symbol.");
        }
    }

    private void validateGenes(String clientGeneList) throws DaoException {
        ArrayList<String> geneList = extractGeneList(clientGeneList);
        for (String currentGene : geneList) {
            if (currentGene.trim().length() > 0) {
                validateGene(currentGene);
            }
        }
    }

    /**
     * Validates a Gene Symbol.
     * <p/>
     * Code currently makes the following assumptions:
     * <p/>
     * - we ignore case
     * - we assume the gene is a protein-coding gene, and not a microRNA.
     */
    private void validateGene(String currentGeneSymbol) throws DaoException {
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
        CanonicalGene dbGene = daoGeneOptimized.getGene(currentGeneSymbol);
        if (dbGene == null) {
            invalidGeneList.add(currentGeneSymbol);
        } else {
            validGeneList.add(dbGene);
        }
    }

    private ArrayList<String> extractGeneList(String clientGeneList) {
        ParserOutput theOncoPrintSpecParserOutput =
                OncoPrintSpecificationDriver.callOncoPrintSpecParserDriver(clientGeneList);

        //  Use the OQL Parser to Extract the Gene Symbols
        ArrayList<String> geneList = new ArrayList<String>();
        geneList.addAll(theOncoPrintSpecParserOutput.getTheOncoPrintSpecification().listOfGenes());
        return geneList;
    }
}
