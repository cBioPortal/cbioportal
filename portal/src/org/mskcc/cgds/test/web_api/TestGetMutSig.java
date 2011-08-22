package org.mskcc.cgds.test.web_api;


import junit.framework.TestCase;
import org.mskcc.cgds.dao.DaoCancerStudy;
import org.mskcc.cgds.dao.DaoGeneOptimized;
import org.mskcc.cgds.dao.DaoMutSig;
import org.mskcc.cgds.model.CancerStudy;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.model.MutSig;
import org.mskcc.cgds.scripts.ImportTypesOfCancers;
import org.mskcc.cgds.scripts.ResetDatabase;
import org.mskcc.cgds.util.ProgressMonitor;
import org.mskcc.cgds.web_api.GetMutSig;
import org.mskcc.cgds.dao.DaoException;

import java.io.*;

/**
 * @author Lennart Bastian
 */

public class TestGetMutSig extends TestCase {

    public void testGetMutSig() throws DaoException, IOException {

        ResetDatabase.resetDatabase();

        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(false);

        ImportTypesOfCancers.load(new ProgressMonitor(), new File("test_data/cancers.txt"));
        CancerStudy cancerStudy = new CancerStudy("Glioblastoma TCGA", "GBM Description", "GBM_portal", "GBM", false);
        DaoCancerStudy.addCancerStudy(cancerStudy);
        assertEquals(1, cancerStudy.getInternalId());

        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
        CanonicalGene gene = new CanonicalGene(1956, "EGFR");
        CanonicalGene gene2 = new CanonicalGene(4921, "DDR2");
        daoGeneOptimized.addGene(gene);
        daoGeneOptimized.addGene(gene2);
        assertEquals("EGFR", gene.getHugoGeneSymbol());
        assertEquals(4921, gene2.getEntrezGeneId());

        MutSig mutSig = new MutSig(1, gene, 1,502500, 20, 19, 1, 4, 13, 3, 0, "<1E-11", "<1E-8", 1E-8);
        MutSig mutSig2 = new MutSig(1, gene2, 15, 273743, 3, 3, 0, 1, 2, 0, 0, "0.0014", "0.13",0.13);
        assertEquals("<1E-11",mutSig.getpValue());
        assertEquals("0.13", mutSig2.getqValue());

        DaoMutSig.addMutSig(mutSig);
        DaoMutSig.addMutSig(mutSig2);

        StringBuffer stringBuffer = GetMutSig.GetAMutSig(1);
        //System.out.println(stringBuffer);

        //getMutSig("1", "","");
        //getMutSig("1", "","DDR2");
        //getMutSig("1", "","EGFR DDR2");
        //getMutSig("1", ".1","");

    }
    /*
     * this is taken directly from the WebService class, and minimally changed to function without
     * a writer, and HttpServletRequest, as to better suit it for a Test Class.
     */
    private void getMutSig(String cancerStudyID, String q_value_threshold, String gene_list)
            throws DaoException {
        int cancerID = Integer.parseInt(cancerStudyID);
        if ((q_value_threshold == null || q_value_threshold.length() == 0)
                && (gene_list == null || gene_list.length() == 0)) {
            StringBuffer output = GetMutSig.GetAMutSig(cancerID);
            System.err.println(output);
            System.err.println("exit code 0\n");
            //if client enters a q_value_threshold
        } else if ((q_value_threshold != null || q_value_threshold.length() != 0)
                && (gene_list == null || gene_list.length() == 0)) {
            StringBuffer output = GetMutSig.GetAMutSig(cancerID, q_value_threshold, true);
            System.err.println(output);
            System.err.println("exit code 1\n");
            //if client enters a gene_list
        } else if ((q_value_threshold == null || q_value_threshold.length() == 0)
                && (gene_list != null || gene_list.length() != 0)) {
            StringBuffer output = GetMutSig.GetAMutSig(cancerID, gene_list, false);
            System.err.println(output);
            System.err.println("exit code 2\n");
        } else {
            System.err.println("Invalid command. Please input a valid Q-Value Threshold, or Gene List. (Not Both)!");
        }
    }

}