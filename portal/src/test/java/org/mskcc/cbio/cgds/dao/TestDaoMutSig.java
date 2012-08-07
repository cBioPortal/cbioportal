package org.mskcc.cbio.cgds.dao;

import junit.framework.TestCase;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.dao.DaoGeneOptimized;
import org.mskcc.cbio.cgds.dao.DaoMutSig;
import org.mskcc.cbio.cgds.model.CanonicalGene;
import org.mskcc.cbio.cgds.model.MutSig;
import org.mskcc.cbio.cgds.scripts.ResetDatabase;

import java.io.IOException;

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

    public void testDaoMutSig() throws DaoException, IOException {
        ResetDatabase.resetDatabase();
        
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();

        // Add Gene TP53 to both gene table and mut_sig table
        CanonicalGene gene1 = new CanonicalGene(10298321, "TP53");
        daoGeneOptimized.addGene(gene1);
        MutSig tp53 = new MutSig(1, gene1, 1, 145177, 48, 1E-11f, 1E-8f);

        // Add Gene PTEN to both gene table and mut_sig table
        CanonicalGene gene2 = new CanonicalGene(10298321, "PTEN");
        daoGeneOptimized.addGene(gene2);
        MutSig pten = new MutSig(1, gene2, 2, 156252, 34, 1E-11f, 1E-8f);
        DaoMutSig.addMutSig(pten);

        //get tp53 from mutsig table using hugoGeneSymbol
        MutSig mutSig = DaoMutSig.getMutSig("TP53", 1);
        CanonicalGene testGene = mutSig.getCanonicalGene();
        assertTrue("TP53".equals(testGene.getHugoGeneSymbolAllCaps()));
        assertEquals(1, mutSig.getCancerType());
        
        //get pten from mutsig table using entrez ID
        long foo = 10298321;
        MutSig mutSig2 = DaoMutSig.getMutSig(foo, 1);
        CanonicalGene testGene2 = mutSig2.getCanonicalGene();
        assertEquals(10298321, testGene2.getEntrezGeneId());
        assertEquals(1, mutSig2.getCancerType());
    }
}
