package org.mskcc.cbio.cgds.dao;

import junit.framework.TestCase;
import org.mskcc.cbio.cgds.dao.DaoCancerStudy;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.model.CancerStudy;
import org.mskcc.cbio.cgds.scripts.ImportTypesOfCancers;
import org.mskcc.cbio.cgds.scripts.ResetDatabase;
import org.mskcc.cbio.cgds.util.ProgressMonitor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * JUnit Tests for DaoCancer Study.
 *
 * @author Ethan Cerami, Arthur Goldberg.
 */
public class TestDaoCancerStudy extends TestCase {

    /**
     * Tests DaoCancer Study #1.
     * @throws DaoException Database Error.
     * @throws IOException IO Error.
     */
    public void testDaoCancerStudy() throws DaoException, IOException {
        ResetDatabase.resetDatabase();
        // load cancers
		// TBD: change this to use getResourceAsStream()
        ImportTypesOfCancers.load(new ProgressMonitor(), new File("target/test-classes/cancers.txt"));

        CancerStudy cancerStudy = new CancerStudy("GBM", "GBM Description", "gbm", "brca", false);
        DaoCancerStudy.addCancerStudy(cancerStudy);

        //   `CANCER_STUDY_ID` auto_increment counts from 1
        assertEquals(1, cancerStudy.getInternalId());

        cancerStudy.setName("Breast");
        cancerStudy.setCancerStudyStablId("breast");
        cancerStudy.setDescription("Breast Description");
        DaoCancerStudy.addCancerStudy(cancerStudy);
        assertEquals(2, cancerStudy.getInternalId()); //

        ArrayList<CancerStudy> list = DaoCancerStudy.getAllCancerStudies();
        assertEquals(2, list.size());

        cancerStudy = list.get(0);
        assertEquals("gbm", cancerStudy.getCancerStudyStableId());
        assertEquals("GBM", cancerStudy.getName());
        assertEquals("GBM Description", cancerStudy.getDescription());
        assertEquals(1, cancerStudy.getInternalId());

        cancerStudy = list.get(1);
        assertEquals(2, cancerStudy.getInternalId());
        assertEquals("Breast Description", cancerStudy.getDescription());
        assertEquals("Breast", cancerStudy.getName());

        cancerStudy = DaoCancerStudy.getCancerStudyByStableId("gbm");
        assertEquals("gbm", cancerStudy.getCancerStudyStableId());
        assertEquals("GBM", cancerStudy.getName());
        assertEquals("GBM Description", cancerStudy.getDescription());

        assertEquals(null, DaoCancerStudy.getCancerStudyByStableId("no such study"));
        assertTrue(DaoCancerStudy.doesCancerStudyExistByStableId
                (cancerStudy.getCancerStudyStableId()));
        assertFalse(DaoCancerStudy.doesCancerStudyExistByStableId("no such study"));

        assertTrue(DaoCancerStudy.doesCancerStudyExistByInternalId(cancerStudy.getInternalId()));
        assertFalse(DaoCancerStudy.doesCancerStudyExistByInternalId(-1));

        DaoCancerStudy.deleteCancerStudy(cancerStudy.getInternalId());

        list = DaoCancerStudy.getAllCancerStudies();
        assertEquals(1, list.size());
    }

    /**
     * Tests DaoCancer Study #2.
     * @throws DaoException Database Error.
     * @throws IOException IO Error.
     */
    public void testDaoCancerStudy2() throws DaoException, IOException {
        ResetDatabase.resetDatabase();
        // load cancers
		// TBD: change this to use getResourceAsStream()
        ImportTypesOfCancers.load(new ProgressMonitor(), new File("target/test-classes/cancers.txt"));

        CancerStudy cancerStudy1 = new CancerStudy("GBM public study x", "GBM Description",
                "tcga_gbm1", "brca", true);
        DaoCancerStudy.addCancerStudy(cancerStudy1);

        CancerStudy cancerStudy = new CancerStudy("GBM private study x", "GBM Description 2",
                "tcga_gbm2", "brca", false);
        DaoCancerStudy.addCancerStudy(cancerStudy);

        cancerStudy = new CancerStudy("Breast", "Breast Description",
                "tcga_gbm3", "brca", false);
        DaoCancerStudy.addCancerStudy(cancerStudy);

        ArrayList<CancerStudy> list = DaoCancerStudy.getAllCancerStudies();
        assertEquals(3, list.size());

        cancerStudy = list.get(0);
        assertEquals(1, cancerStudy.getInternalId());
        assertEquals("GBM Description", cancerStudy1.getDescription());
        assertEquals("GBM public study x", cancerStudy1.getName());
        assertEquals(true, cancerStudy1.isPublicStudy());

        cancerStudy1 = list.get(1);
        assertEquals(2, cancerStudy1.getInternalId());
        assertEquals("GBM private study x", cancerStudy1.getName());
        assertEquals("GBM Description 2", cancerStudy1.getDescription());
        assertEquals(false, cancerStudy1.isPublicStudy());

        cancerStudy1 = list.get(2);
        assertEquals(3, cancerStudy1.getInternalId());
        assertEquals("Breast", cancerStudy1.getName());
        assertEquals("Breast Description", cancerStudy1.getDescription());
        assertEquals(false, cancerStudy1.isPublicStudy());

        cancerStudy1 = DaoCancerStudy.getCancerStudyByInternalId(1);
        assertEquals(1, cancerStudy1.getInternalId());
        assertEquals("GBM Description", cancerStudy1.getDescription());
        assertEquals("GBM public study x", cancerStudy1.getName());
        assertEquals(true, cancerStudy1.isPublicStudy());

        assertEquals(3, DaoCancerStudy.getCount());
        DaoCancerStudy.deleteCancerStudy(1);
        assertEquals(2, DaoCancerStudy.getCount());
        DaoCancerStudy.deleteCancerStudy(1);
        assertEquals(2, DaoCancerStudy.getCount());
        DaoCancerStudy.deleteAllRecords();
        assertEquals(0, DaoCancerStudy.getCount());
        assertEquals(null, DaoCancerStudy.getCancerStudyByInternalId(CancerStudy.NO_SUCH_STUDY));
    }
}
