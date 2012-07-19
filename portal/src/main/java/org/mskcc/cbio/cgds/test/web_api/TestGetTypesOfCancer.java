package org.mskcc.cgds.test.web_api;

import junit.framework.TestCase;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoCancerStudy;
import org.mskcc.cgds.scripts.ImportTypesOfCancers;
import org.mskcc.cgds.scripts.ResetDatabase;
import org.mskcc.cgds.util.ProgressMonitor;
import org.mskcc.cgds.web_api.GetTypesOfCancer;
import org.mskcc.cgds.web_api.ProtocolException;
import org.mskcc.cgds.model.CancerStudy;

import java.io.File;
import java.io.IOException;

/**
 * JUnit Tests for GetTypes of Cancer.
 *
 * @author Ethan Cerami, Arthur Goldberg.
 */
public class TestGetTypesOfCancer extends TestCase {

    /**
     * Tests Get Types of Cancer.
     * @throws DaoException Database Error.
     * @throws IOException IO Error.
     * @throws ProtocolException ProtocolError.
     */
    public void testGetTypesOfCancer() throws DaoException, IOException, ProtocolException {
        ResetDatabase.resetDatabase();

        // First, verify that protocol exception is thrown when there are no cancer types
        try {
            String output = GetTypesOfCancer.getTypesOfCancer();
            fail ("ProtocolException should have been thrown.");
        } catch (ProtocolException e) {
            assertEquals(e.getMsg(), "No Types of Cancer Available.");
        }

        // then, load cancers
        ImportTypesOfCancers.load(new ProgressMonitor(), new File("test_data/cancers.txt"));

        //  Verify a few of the data lines
        String output = GetTypesOfCancer.getTypesOfCancer();
        assertTrue(output.contains("GBM\tGlioblastoma multiforme"));
        assertTrue(output.contains("PRAD\tProstate adenocarcinoma"));

        //  Verify header
        String lines[] = output.split("\n");
        assertEquals ("type_of_cancer_id\tname", lines[0].trim());
    }

    /**
     * Tests Get Cancer Studies.
     * @throws DaoException Database Error.
     * @throws IOException IO Error.
     * @throws ProtocolException ProtocolError.
     */
    public void testGetCancerStudies() throws DaoException, IOException, ProtocolException {
        ResetDatabase.resetDatabase();

        // First, verify that protocol exception is thrown when there are no cancer studies
        try {
            String output = GetTypesOfCancer.getCancerStudies();
            fail ("ProtocolException should have been thrown.");
        } catch (ProtocolException e) {
            assertEquals(e.getMsg(), "No Cancer Studies Available.");
        }

        // then, load one sample cancer study
        ImportTypesOfCancers.load(new ProgressMonitor(), new File("test_data/cancers.txt"));
        CancerStudy tcgaGbm = new CancerStudy("TCGA GBM", "TCGA GBM Project", "tcga_gbm" ,
                "GBM", true);

        DaoCancerStudy.addCancerStudy(tcgaGbm);
        String output = GetTypesOfCancer.getCancerStudies();
        String lines[] = output.split("\n");

        //  Verify we get exactly two lines
        assertEquals (2, lines.length);

        //  Verify header
        assertEquals ("cancer_study_id\tname\tdescription", lines[0].trim());

        //  Verify data
        assertEquals ("tcga_gbm\tTCGA GBM\tTCGA GBM Project", lines[1].trim());
    }
}
