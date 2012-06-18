package org.mskcc.cgds.test.dao;

import junit.framework.TestCase;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoGistic;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.model.Gistic;
import org.mskcc.cgds.scripts.ResetDatabase;

import java.sql.SQLException;
import java.util.ArrayList;

public class TestDaoGistic extends TestCase {

    public void testDaoGistic() throws SQLException, DaoException {
        
        ResetDatabase.resetDatabase();
        DaoGistic.deleteAllRecords();

        // initialize dummy parameters
        ArrayList<CanonicalGene> geneList = new ArrayList<CanonicalGene>();
        geneList.add(new CanonicalGene((long) 1, "AAA"));
        geneList.add(new CanonicalGene((long) 2, "BBB"));
        geneList.add(new CanonicalGene((long) 3, "CCC"));

        Gistic gisticIn1;
        Gistic gisticIn2;
        gisticIn1 = new Gistic(1, "1q11", 1, 2, "0.01", "0.02", geneList, Gistic.AMPLIFIED);
        gisticIn2 = new Gistic(1, "2q22", 1, 2, "0.01", "0.02", geneList, Gistic.AMPLIFIED);
        // end initialize

        assertEquals(Gistic.NO_SUCH_GISTIC, gisticIn1.getInternalId());
        // -- put stuff in --
        DaoGistic.addGistic(gisticIn1);
        // InternalId is auto-incremented by the db, starting at 1
        assertEquals(1, gisticIn1.getInternalId());
        DaoGistic.addGistic(gisticIn2);
        assertEquals(2, gisticIn2.getInternalId());
        DaoGistic.deleteGistic(2);
        assertEquals(1, gisticIn1.getInternalId());
        DaoGistic.addGistic(gisticIn2);

        // -- get stuff back --

//        DaoGistic.getGisticByROI("1q11", 1,2);  Perhaps this is a new project of some sort?
// ROIs across various cancers.

        ArrayList<Gistic> gisticOut = DaoGistic.getAllGisticByCancerStudyId(1);
        assertTrue(gisticOut != null);
        assertEquals(2, gisticOut.size());

        DaoGistic.deleteAllRecords();
    }
}
