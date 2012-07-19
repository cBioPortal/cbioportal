package org.mskcc.cbio.cgds.test.dao;

import junit.framework.TestCase;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.dao.DaoMutationFrequency;
import org.mskcc.cbio.cgds.dao.DaoGeneOptimized;
import org.mskcc.cbio.cgds.scripts.ResetDatabase;
import org.mskcc.cbio.cgds.model.CanonicalGene;

import java.util.ArrayList;

/**
 * JUnit test for DaoMutationFrequency class.
 */
public class TestDaoMutationFrequency extends TestCase {

    public void testDaoMutationFrequency() throws DaoException {
        ResetDatabase.resetDatabase();
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        daoGene.addGene(new CanonicalGene(672, "BRCA1"));
        daoGene.addGene(new CanonicalGene(675, "BRCA2"));

        DaoMutationFrequency daoMutationFrequency = new DaoMutationFrequency();
        daoMutationFrequency.addGene(672, 0.06, 2);
        daoMutationFrequency.addGene(675, 0.10, 2);

        ArrayList <CanonicalGene> list = daoMutationFrequency.getTop100SomaticMutatedGenes(2);
        assertEquals (2, list.size());
        CanonicalGene gene0 = list.get(0);
        assertEquals (675, gene0.getEntrezGeneId());
        assertEquals ("BRCA2", gene0.getHugoGeneSymbolAllCaps());
        assertEquals (0.10, gene0.getSomaticMutationFrequency(), 0.0001);

        CanonicalGene gene1 = list.get(1);
        assertEquals (672, gene1.getEntrezGeneId());
        assertEquals ("BRCA1", gene1.getHugoGeneSymbolAllCaps());
        assertEquals (0.06, gene1.getSomaticMutationFrequency(), 0.0001);
        
        daoMutationFrequency.deleteAllRecords();
        assertEquals ( null, daoMutationFrequency.getSomaticMutationFrequency( 672 ) );
    }
}