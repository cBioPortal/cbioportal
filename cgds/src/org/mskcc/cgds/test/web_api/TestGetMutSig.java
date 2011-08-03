package org.mskcc.cgds.test.web_api;


import junit.framework.TestCase;
import org.mskcc.cgds.dao.DaoCancerStudy;
import org.mskcc.cgds.dao.DaoGeneOptimized;
import org.mskcc.cgds.model.CancerStudy;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.scripts.ImportMutSigData;
import org.mskcc.cgds.scripts.ImportTypesOfCancers;
import org.mskcc.cgds.scripts.ResetDatabase;
import org.mskcc.cgds.util.ProgressMonitor;
import org.mskcc.cgds.web_api.GetMutSig;
import org.mskcc.cgds.dao.DaoException;

import java.io.File;
import java.io.IOException;

/**
 * @author Lennart Bastian
 */

public class TestGetMutSig extends TestCase {

    public void testGetMutSig() throws DaoException, IOException {

    ResetDatabase.resetDatabase();

        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(false);

        ImportTypesOfCancers.load(new ProgressMonitor(), new File("testData/cancers.txt"));
        CancerStudy cancerStudy = new CancerStudy("Glioblastoma TCGA", "GBM Description", "GBM_portal", "GBM", false);
        DaoCancerStudy.addCancerStudy(cancerStudy);
        assertEquals(1, cancerStudy.getStudyId());

        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
        CanonicalGene gene = new CanonicalGene(1956, "EGFR");
        CanonicalGene gene2 = new CanonicalGene(4921, "DDR2");
        daoGeneOptimized.addGene(gene);
        daoGeneOptimized.addGene(gene2);
        assertEquals("EGFR", gene.getHugoGeneSymbol());
        assertEquals(4921, gene2.getEntrezGeneId());
        //load testData file and properties file
        File file = new File("testData/test_mut_sig_data.txt");
        File properties = new File("testData/testCancerStudy.txt");
        ImportMutSigData parser = new ImportMutSigData(file, properties, pMonitor);
        //import data and properties: see ImportMutSigData Class
        parser.importData();

        String getMutSig = GetMutSig.GetMutSig(1);


    }

}