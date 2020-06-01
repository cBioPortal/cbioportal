/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.dao;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.dao.MySQLbulkLoader;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

import java.util.HashSet;

/**
 * JUnit Tests for DaoGene and DaoGeneOptimized.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@Rollback
@Transactional
public class TestDaoGene {

    /**
     * Tests DaoGene and DaoGeneOptimized.
     * @throws DaoException Database Error.
     */
	@Test
    public void testAddExistingGene() throws DaoException {

		// save bulkload setting before turning off
		boolean isBulkLoad = MySQLbulkLoader.isBulkLoad();
		MySQLbulkLoader.bulkLoadOff();

        //  Add BRCA1 and BRCA2 Genes
        CanonicalGene gene = new CanonicalGene(672, "BRCA1",
                new HashSet<String>(Arrays.asList("BRCAI|BRCC1|BROVCA1|IRIS|PNCA4|PSCP|RNF53".split("\\|"))));
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
        int num = daoGeneOptimized.addGene(gene);
        assertEquals(5, num);

		// restore bulk setting
		if (isBulkLoad) {
			MySQLbulkLoader.bulkLoadOn();
		}
    }

    /**
     * Tests DaoGene and DaoGeneOptimized.
     * @throws DaoException Database Error.
     */
	@Test
    public void testAddNewGene() throws DaoException {

		// save bulkload setting before turning off
		boolean isBulkLoad = MySQLbulkLoader.isBulkLoad();
		MySQLbulkLoader.bulkLoadOff();

        //  Add BRCA1 and BRCA2 Genes
        CanonicalGene gene = new CanonicalGene(1956, "EGFR",
                new HashSet<String>(Arrays.asList("ERBB1|ERBB|HER1".split("\\|"))));
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
        int num = daoGeneOptimized.addGene(gene);
        assertEquals(4, num);

		// restore bulk setting
		if (isBulkLoad) {
			MySQLbulkLoader.bulkLoadOn();
		}
    }

    /**
     * Validates BRCA1.
     */
    @Test
    public void testBRCA1ById() {
        CanonicalGene gene = DaoGeneOptimized.getInstance().getGene(672);
        assertEquals("BRCA1", gene.getHugoGeneSymbolAllCaps());
        assertEquals(672, gene.getEntrezGeneId());
    }

    /**
     * Validates BRCA2.
     */
    @Test
    public void testBRCA2ById() {
        CanonicalGene gene = DaoGeneOptimized.getInstance().getGene(675);
        assertEquals("BRCA2", gene.getHugoGeneSymbolAllCaps());
        assertEquals(675, gene.getEntrezGeneId());
    }

    /**
     * Validates BRCA2.
     */
    @Test
    public void testBRCA2ByName() {
        CanonicalGene gene = DaoGeneOptimized.getInstance().getGene("BRCA2");
        assertEquals("BRCA2", gene.getHugoGeneSymbolAllCaps());
        assertEquals(675, gene.getEntrezGeneId());
    }

}
