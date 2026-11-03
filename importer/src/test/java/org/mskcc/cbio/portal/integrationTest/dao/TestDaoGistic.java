/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.integrationTest.dao;

import java.sql.SQLException;
import java.util.*;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.dao.DaoGistic;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.Gistic;
import org.mskcc.cbio.portal.validate.validationException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.mskcc.cbio.portal.integrationTest.IntegrationTestBase;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
public class TestDaoGistic extends IntegrationTestBase {

	@Test
    public void testDaoGistic() throws SQLException, DaoException, validationException {

        // initialize dummy parameters
		CanonicalGene brca1 = DaoGeneOptimized.getInstance().getGene("BRCA1");
		CanonicalGene brca2 = DaoGeneOptimized.getInstance().getGene("BRCA2");
		CanonicalGene kras = DaoGeneOptimized.getInstance().getGene("KRAS");
				
        ArrayList<CanonicalGene> geneList =
			new ArrayList<CanonicalGene>(Arrays.asList(brca1, brca2, kras));

        Gistic gisticIn1;
        Gistic gisticIn2;
        gisticIn1 = new Gistic(1, 1, "1q11.1", 1, 2, 0.01f, geneList, Gistic.AMPLIFIED);
        gisticIn2 = new Gistic(1, 2, "2q22.2", 1, 2, 0.01f, geneList, Gistic.AMPLIFIED);

        // end initialize

        assertEquals(Gistic.NO_SUCH_GISTIC, gisticIn1.getInternalId());
        // -- put stuff in --
        DaoGistic.addGistic(gisticIn1);
        // InternalId is auto-incremented by the db, starting at 1
        //assertEquals(1, gisticIn1.getInternalId());
        DaoGistic.addGistic(gisticIn2);
        //assertEquals(2, gisticIn2.getInternalId());
        DaoGistic.deleteGistic(2);
        //assertEquals(1, gisticIn1.getInternalId());
        DaoGistic.addGistic(gisticIn2);

        // -- get stuff back --

//        DaoGistic.getGisticByROI("1q11", 1,2);  Perhaps this is a new project of some sort?
// ROIs across various cancers.

        ArrayList<Gistic> gisticOut = DaoGistic.getAllGisticByCancerStudyId(1);
        assertTrue(gisticOut != null);
        //assertEquals(2, gisticOut.size());
    }
}
