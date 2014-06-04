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
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGistic;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.Gistic;
import org.mskcc.cbio.portal.scripts.ResetDatabase;
import org.mskcc.cbio.portal.validate.validationException;

import java.sql.SQLException;
import java.util.ArrayList;

public class TestDaoGistic extends TestCase {

    public void testDaoGistic() throws SQLException, DaoException, validationException {

        // save the database table and
        // delete the database
        ResetDatabase.resetDatabase();

        // initialize dummy parameters
		CanonicalGene brca1 = new CanonicalGene(672, "BRCA1");
		CanonicalGene brca2 = new CanonicalGene(675, "BRCA2");
		CanonicalGene tp53 = new CanonicalGene(7157, "TP53");

        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        daoGene.addGene(brca1);
        daoGene.addGene(brca2);
        daoGene.addGene(tp53);
        ArrayList<CanonicalGene> geneList =
			new ArrayList<CanonicalGene>(Arrays.asList(brca1, brca2, tp53));

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
