/*
 * Copyright (c) 2020 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.integrationTest.dao;

import java.util.*;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.dao.DaoGenePanel;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.GenePanel;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.mskcc.cbio.portal.integrationTest.IntegrationTestBase;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * JUnit Tests for DaoGenePanel.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
public class TestDaoGenePanel extends IntegrationTestBase {

    /**
     * Tests DaoGenePanel.addGenePanel().
     * @throws DaoException Database Error.
     */
	@Test
    public void testAddGenePanel() throws DaoException {

		CanonicalGene brca1 = DaoGeneOptimized.getInstance().getGene("BRCA1");
		CanonicalGene brca2 = DaoGeneOptimized.getInstance().getGene("BRCA2");
		CanonicalGene kras = DaoGeneOptimized.getInstance().getGene("KRAS");
        HashSet<CanonicalGene> canonicalGenes = new HashSet<CanonicalGene>();
        canonicalGenes.add(brca1);
        canonicalGenes.add(brca2);
        canonicalGenes.add(kras);

        DaoGenePanel.addGenePanel("testGenePanel", "Test gene panel description", canonicalGenes);

        GenePanel genePanel = DaoGenePanel.getGenePanelByStableId("testGenePanel");
        assertTrue(genePanel != null);
        assertTrue(genePanel.getStableId().equals("testGenePanel"));
        assertTrue(genePanel.getDescription().equals("Test gene panel description"));
        assertTrue(genePanel.getDescription().equals("Test gene panel description"));
        assertEquals(genePanel.getGenes().size(), 3);

        DaoGenePanel.deleteGenePanel(genePanel);
        genePanel = DaoGenePanel.getGenePanelByStableId("testGenePanel");
        assertTrue(genePanel == null);
    }
}
