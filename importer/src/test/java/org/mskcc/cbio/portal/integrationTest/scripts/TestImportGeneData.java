/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.integrationTest.scripts;

import java.io.*;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.scripts.ImportGeneData;
import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.mskcc.cbio.portal.integrationTest.IntegrationTestBase;
import static org.junit.Assert.assertEquals;

/**
 * JUnit tests for ImportGeneData class.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
public class TestImportGeneData extends IntegrationTestBase {

    @Test
    /*
     * Checks that ImportGeneData works by calculating the length from three genes 
     * in genes_test.txt. The file genes_test.txt contains real data.
     */
    public void testImportGeneData() throws Exception {
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        ProgressMonitor.setConsoleMode(false);
        
        File file = new File("src/test/resources/genes_test.txt");
        ImportGeneData.importData(file, "GRCh37");

        CanonicalGene gene = daoGene.getGene(10);
        assertEquals("NAT2", gene.getHugoGeneSymbolAllCaps());
        gene = daoGene.getGene(15);
        assertEquals("AANAT", gene.getHugoGeneSymbolAllCaps());

        gene = daoGene.getGene("ABCA3");
        assertEquals(21, gene.getEntrezGeneId());
    }
}
