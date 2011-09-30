package org.mskcc.cgds.test.dao;

import java.util.Arrays;
import junit.framework.TestCase;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoGeneOptimized;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.scripts.ResetDatabase;

import java.util.HashSet;

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
        CanonicalGene gene = new CanonicalGene(672, "BRCA1",
                new HashSet<String>(Arrays.asList("BRCAI|BRCC1|BROVCA1|IRIS|PNCA4|PSCP|RNF53".split("\\|"))));
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
        int num = daoGeneOptimized.addGene(gene);
        assertEquals(8, num);

        gene = new CanonicalGene(675, "BRCA2",
                new HashSet<String>(Arrays.asList("BRCC2|BROVCA2|FACD|FAD|FAD1|FANCB|FANCD|FANCD1|GLM3|PNCA2".split("\\|"))));
        num = daoGeneOptimized.addGene(gene);
        assertEquals(11, num);

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
