package org.mskcc.cgds.test.dao;

import junit.framework.TestCase;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoGeneOptimized;
import org.mskcc.cgds.dao.MySQLbulkLoader;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.model.MutSig;
import org.mskcc.cgds.scripts.ResetDatabase;
import org.mskcc.cgds.dao.DaoMutSig;

/**
 * @author Lennart Bastian
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

        // Add Genes TP53 and PTEN to both mut_sig table and gene table
        CanonicalGene gene1 = new CanonicalGene(10298321, "TP53");
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
        daoGeneOptimized.addGene(gene1);
        MutSig tp53 = new MutSig(1, gene1, 1, 145177, 48, 48, 0, 18, 17, 10, 3, "<1E-11", "<1E-8",1E-8);
        DaoMutSig.addMutSig(tp53);
        CanonicalGene gene2 = new CanonicalGene(10298321, "PTEN");
        daoGeneOptimized.addGene(gene2);
        MutSig pten = new MutSig(1, gene2, 2, 156252, 34, 29, 5, 6, 9, 7, 12, "<1E-11", "<1E-8", 1E-8);
        DaoMutSig.addMutSig(pten);

        //get tp53 from mutsig table using hugoGeneSymbol
        MutSig mutSig = DaoMutSig.getMutSig("TP53", 1);
        CanonicalGene testGene = mutSig.getCanonicalGene();
        assertEquals("TP53", testGene.getHugoGeneSymbol());
        assertEquals(1, mutSig.getCancerType());
        //get pten from mutsig table using entrez ID
        long foo = 10298321;
        MutSig mutSig2 = DaoMutSig.getMutSig(foo, 1);
        CanonicalGene testGene2 = mutSig2.getCanonicalGene();
        assertEquals(10298321, testGene2.getEntrezGeneId());
        assertEquals(1, mutSig2.getCancerType());

    }

}


