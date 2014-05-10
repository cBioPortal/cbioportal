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
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.dao.DaoMutSig;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.MutSig;
import org.mskcc.cbio.portal.scripts.ImportTypesOfCancers;
import org.mskcc.cbio.portal.scripts.ResetDatabase;
import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.mskcc.cbio.portal.dao.DaoException;

import java.io.*;

/**
 * @author Lennart Bastian
 */

public class TestGetMutSig extends TestCase {

    public void testGetMutSig() throws DaoException, IOException {

        ResetDatabase.resetDatabase();

        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(false);
		// TBD: change this to use getResourceAsStream()
        ImportTypesOfCancers.load(new ProgressMonitor(), new File("target/test-classes/cancers.txt"));
        // changed GBM_portal to tcga_gbm
        CancerStudy cancerStudy = new CancerStudy("Glioblastoma TCGA", "GBM Description", "tcga_gbm", "GBM", false);
        DaoCancerStudy.addCancerStudy(cancerStudy);
        assertEquals(1, cancerStudy.getInternalId());

        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
        CanonicalGene gene = new CanonicalGene(1956, "EGFR");
        CanonicalGene gene2 = new CanonicalGene(4921, "DDR2");
        daoGeneOptimized.addGene(gene);
        daoGeneOptimized.addGene(gene2);
        assertEquals("EGFR", gene.getHugoGeneSymbolAllCaps());
        assertEquals(4921, gene2.getEntrezGeneId());

        MutSig mutSig = new MutSig(1, gene, 1, 502500, 20, 1E-11f, 1E-8f);
        MutSig mutSig2 = new MutSig(1, gene2, 14, 273743, 3, 1E-11f, 1E-8f);

        assertTrue(1E-11f == mutSig.getpValue());
        assertTrue(1E-8f == mutSig2.getqValue());

        DaoMutSig.addMutSig(mutSig);
        DaoMutSig.addMutSig(mutSig2);

        StringBuffer stringBuffer = GetMutSig.getMutSig(1);
    }
    /*
     * this is taken directly from the WebService class, and minimally changed to function without
     * a writer, and HttpServletRequest, as to better suit it for a Test Class.
     */
    private void getMutSig(String cancerStudyID, String qValueThreshold, String geneList)
            throws DaoException {
        int cancerID = Integer.parseInt(cancerStudyID);
        if ((qValueThreshold == null || qValueThreshold.length() == 0)
                && (geneList == null || geneList.length() == 0)) {
            StringBuffer output = GetMutSig.getMutSig(cancerID);
            System.err.println(output);
            System.err.println("exit code 0\n");
            //if client enters a q_value_threshold
        } else if ((qValueThreshold != null || qValueThreshold.length() != 0)
                && (geneList == null || geneList.length() == 0)) {
            StringBuffer output = GetMutSig.getMutSig(cancerID, qValueThreshold, true);
            System.err.println(output);
            System.err.println("exit code 1\n");
            //if client enters a gene_list
        } else if ((qValueThreshold == null || qValueThreshold.length() == 0)
                && (geneList != null || geneList.length() != 0)) {
            StringBuffer output = GetMutSig.getMutSig(cancerID, geneList, false);
            System.err.println(output);
            System.err.println("exit code 2\n");
        } else {
            System.err.println("Invalid command. Please input a valid Q-Value Threshold, or Gene List. (Not Both)!");
        }
    }

}

