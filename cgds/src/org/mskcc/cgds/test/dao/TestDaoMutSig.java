package org.mskcc.cgds.test.dao;

import junit.framework.TestCase;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.MySQLbulkLoader;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.model.MutSig;
import org.mskcc.cgds.scripts.ResetDatabase;
import org.mskcc.cgds.dao.DaoMutSig;

/**
 * Created by IntelliJ IDEA.
 * User: lennartbastian
 * Date: 25/07/2011
 * Time: 15:23
 * To change this template use File | Settings | File Templates.
 */


public class TestDaoMutSig extends TestCase {

    /**
     * Tests DaoGene and DaoGeneOptimized.
     *
     * @throws org.mskcc.cgds.dao.DaoException
     *          Database Error.
     */

    public void testDaoMutSig() throws DaoException {
        ResetDatabase.resetDatabase();

        // Add Gene TP53 Gene to DB
        MySQLbulkLoader.bulkLoadOff();
        CanonicalGene gene1 = new CanonicalGene(10298321, "TP53");
        MutSig tp53 = new MutSig(1, gene1, 1, 145177, 48, 48, 0, 18, 17, 10, 3, "<1E-11", "<1E-8");
        DaoMutSig.addMutSig(tp53);
        CanonicalGene gene2 = new CanonicalGene(10298321, "PTEN");
        MutSig pten = new MutSig(1,gene2, 2, 156252, 34, 29, 5, 6, 9, 7, 12, "<1E-11", "<1E-8");
        DaoMutSig.addMutSig(pten);
        validateTP53(tp53);
        validatePTEN(pten);
        CanonicalGene gene3 = new CanonicalGene(1232401, "ERBB2");
        MutSig erbb2 = new MutSig(1,gene3, 3, 283387, 12, 10, 2, 0, 0, 11, 1, "<1E-11", "<1E-8");
        DaoMutSig.addMutSig(erbb2);
        MutSig erbb2Test = DaoMutSig.getMutSig("ERBB2");
        validateGetMutSig(erbb2Test);
    }

    /**
     * Validates TP53.
     *
     * @param mutSig MutSig Object.
     */
    private void validateTP53(MutSig mutSig) {
        CanonicalGene gene = mutSig.getCanonicalGene();
        assertEquals("TP53", gene.getHugoGeneSymbol());
        assertEquals(145177, mutSig.getN());
    }

    /**
     * Validates PTEN.
     *
     * @param mutSig MutSig Object.
     */
    private void validatePTEN(MutSig mutSig) {
        CanonicalGene gene = mutSig.getCanonicalGene();
        assertEquals("PTEN", gene.getHugoGeneSymbol());
        assertEquals("<1E-11", mutSig.getpValue());
    }

     /**
     * Validates getMutSig.
     *
     * @param mutSig MutSig Object.
     */

    private void validateGetMutSig(MutSig mutSig) {
        CanonicalGene gene = mutSig.getCanonicalGene();
        assertEquals("ERBB2", gene.getHugoGeneSymbol());
        assertEquals("<1E-11", mutSig.getpValue());
    }

}


