package org.mskcc.cgds.test.dao;

import junit.framework.TestCase;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoGeneOptimized;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.scripts.ResetDatabase;

/**
 * JUnit Tests for DaoGene and DaoGeneOptimized.
 */
public class TestDaoGene extends TestCase {

    /**
     * Tests DaoGene and DaoGeneOptimized.
     * @throws DaoException Database Error.
     */
    public void testDaoGene() throws DaoException {
        ResetDatabase.resetDatabase();

        //  Add BRCA1 and BRCA2 Genes
        CanonicalGene gene = new CanonicalGene(672, "BRCA1");
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
        int num = daoGeneOptimized.addGene(gene);
        assertEquals(1, num);

        gene = new CanonicalGene(675, "BRCA2");
        num = daoGeneOptimized.addGene(gene);
        assertEquals(1, num);

        gene = daoGeneOptimized.getGene(675);
        validateBrca2(gene);
        gene = daoGeneOptimized.getGene("BRCA2");
        validateBrca2(gene);
        gene = daoGeneOptimized.getGene(672);
        validateBrca1(gene);
    }

    /**
     * Validates BRCA1.
     * @param gene Gene Object.
     */
    private void validateBrca1(CanonicalGene gene) {
        assertEquals("BRCA1", gene.getHugoGeneSymbolAllCaps());
        assertEquals(672, gene.getEntrezGeneId());
    }

    /**
     * Validates BRCA2.
     * @param gene Gene Object.
     */
    private void validateBrca2(CanonicalGene gene) {
        assertEquals("BRCA2", gene.getHugoGeneSymbolAllCaps());
        assertEquals(675, gene.getEntrezGeneId());
    }

}
