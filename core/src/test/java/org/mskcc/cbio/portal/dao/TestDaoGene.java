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

package org.mskcc.cbio.portal.dao;

import java.util.Arrays;
import junit.framework.TestCase;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.dao.MySQLbulkLoader;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.scripts.ResetDatabase;

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

		// reset database
        ResetDatabase.resetDatabase();

		// save bulkload setting before turning off
		boolean isBulkLoad = MySQLbulkLoader.isBulkLoad();
		MySQLbulkLoader.bulkLoadOff();

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

		// restore bulk setting
		if (isBulkLoad) {
			MySQLbulkLoader.bulkLoadOn();
		}
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
