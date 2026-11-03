/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.integrationTest.dao;

import java.util.*;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.mskcc.cbio.portal.dao.ClickHouseBulkLoader;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.mskcc.cbio.portal.integrationTest.IntegrationTestBase;
import static org.junit.Assert.assertEquals;

/**
 * JUnit Tests for DaoGene and DaoGeneOptimized.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
public class TestDaoGene extends IntegrationTestBase {

    /**
     * Tests DaoGene and DaoGeneOptimized.
     * @throws DaoException Database Error.
     */
	@Test
    public void testAddExistingGene() throws DaoException {

		// save bulkload setting before turning off
		boolean isBulkLoad = ClickHouseBulkLoader.isBulkLoad();
		ClickHouseBulkLoader.bulkLoadOff();

        //  Add BRCA1 and BRCA2 Genes
        CanonicalGene gene = new CanonicalGene(672, "BRCA1",
                new HashSet<String>(Arrays.asList("BRCAI|BRCC1|BROVCA1|IRIS|PNCA4|PSCP|RNF53".split("\\|"))));
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
        int num = daoGeneOptimized.addGene(gene);
        assertEquals(5, num);

		// restore bulk setting
		if (isBulkLoad) {
			ClickHouseBulkLoader.bulkLoadOn();
		}
    }

    /**
     * Tests DaoGene and DaoGeneOptimized.
     * @throws DaoException Database Error.
     */
	@Test
    public void testAddNewGene() throws DaoException {

		// save bulkload setting before turning off
		boolean isBulkLoad = ClickHouseBulkLoader.isBulkLoad();
		ClickHouseBulkLoader.bulkLoadOff();

        //  Add BRCA1 and BRCA2 Genes
        CanonicalGene gene = new CanonicalGene(1956, "EGFR",
                new HashSet<String>(Arrays.asList("ERBB1|ERBB|HER1".split("\\|"))));
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
        int num = daoGeneOptimized.addGene(gene);
        assertEquals(4, num);

		// restore bulk setting
		if (isBulkLoad) {
			ClickHouseBulkLoader.bulkLoadOn();
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
