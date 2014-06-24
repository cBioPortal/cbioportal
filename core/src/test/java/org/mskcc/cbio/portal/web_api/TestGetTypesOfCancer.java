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

package org.mskcc.cbio.portal.web_api;

import junit.framework.TestCase;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.scripts.ImportTypesOfCancers;
import org.mskcc.cbio.portal.scripts.ResetDatabase;
import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.mskcc.cbio.portal.model.CancerStudy;

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
		// TBD: change this to use getResourceAsStream()
        ImportTypesOfCancers.load(new ProgressMonitor(), new File("target/test-classes/cancers.txt"));

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
		// TBD: change this to use getResourceAsStream()
        ImportTypesOfCancers.load(new ProgressMonitor(), new File("target/test-classes/cancers.txt"));
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
