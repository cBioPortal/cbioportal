/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.integrationTest.dao;

import java.io.*;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.dao.DaoMutSig;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.MutSig;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.mskcc.cbio.portal.integrationTest.IntegrationTestBase;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Lennart Bastian
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
public class TestDaoMutSig extends IntegrationTestBase {

    /**
     * Tests DaoGene and DaoGeneOptimized.
     *
     * @throws org.mskcc.cbio.portal.dao.DaoException
     *          Database Error.
     */

	@Test
    public void testDaoMutSig() throws DaoException, IOException {
        
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
