package org.mskcc.cbio.cgds.web_api;


import junit.framework.TestCase;
import org.mskcc.cbio.cgds.dao.DaoCancerStudy;
import org.mskcc.cbio.cgds.dao.DaoGeneOptimized;
import org.mskcc.cbio.cgds.dao.DaoMutSig;
import org.mskcc.cbio.cgds.model.CancerStudy;
import org.mskcc.cbio.cgds.model.CanonicalGene;
import org.mskcc.cbio.cgds.model.MutSig;
import org.mskcc.cbio.cgds.scripts.ImportTypesOfCancers;
import org.mskcc.cbio.cgds.scripts.ResetDatabase;
import org.mskcc.cbio.cgds.util.ProgressMonitor;
import org.mskcc.cbio.cgds.web_api.GetMutSig;
import org.mskcc.cbio.cgds.dao.DaoException;

import java.io.*;

/**
 * @author Lennart Bastian
 */

public class TestGetMutSig extends TestCase {

    public void testGetMutSig() throws DaoException, IOException {

        ResetDatabase.resetDatabase();

        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(false);

        ImportTypesOfCancers.load(new ProgressMonitor(), new File("/cancers.txt"));
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

