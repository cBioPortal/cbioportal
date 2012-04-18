package org.mskcc.cgds.test.scripts;

import junit.framework.TestCase;
import org.mskcc.cgds.dao.DaoMutSig;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.model.MutSig;
import org.mskcc.cgds.scripts.ImportMutSigData;
import org.mskcc.cgds.util.ProgressMonitor;

import java.io.File;

/*
* Tests DaoGene and DaoGeneOptimized.
*
* @throws org.mskcc.cgds.dao.DaoException
*          Database Error.
*/

public class TestImportMutSigData extends TestCase {

    public void testImportMutSigData() throws Exception {
//        ResetDatabase.resetDatabase();

        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(false);

//        ImportTypesOfCancers.load(new ProgressMonitor(), new File("test_data/cancers.txt"));
//        // changed "GBM_portal" to "tcga_gbm"
//        CancerStudy cancerStudy = new CancerStudy("Glioblastoma TCGA", "GBM Description", "tcga_gbm", "GBM", false);
//        DaoCancerStudy.addCancerStudy(cancerStudy);
//        assertEquals(1, cancerStudy.getInternalId());
//
//        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
//        CanonicalGene gene = new CanonicalGene(1956, "EGFR");
//        CanonicalGene gene2 = new CanonicalGene(4921, "DDR2");
//        daoGeneOptimized.addGene(gene);
//        daoGeneOptimized.addGene(gene2);
//        assertEquals("EGFR", gene.getHugoGeneSymbolAllCaps());
//        assertEquals(4921, gene2.getEntrezGeneId());

        // load testData file and properties file
        File file = new File("test_data/test_mut_sig_data.txt");
        File properties = new File("test_data/testCancerStudy.txt");
        ImportMutSigData parser = new ImportMutSigData(file, properties, pMonitor);

        parser.importData();

        // Test if getMutSig works with a HugoGeneSymbol
        MutSig mutSig = DaoMutSig.getMutSig("EGFR", 1);
        CanonicalGene testGene = mutSig.getCanonicalGene();
        assertEquals("EGFR", testGene.getHugoGeneSymbolAllCaps());
        assertEquals(mutSig.getNumMutations(), 4);
        assertEquals(mutSig.getNumBasesCovered(), 1978314);
        assertEquals(mutSig.getpValue(), "0.085");
        assertEquals(mutSig.getqValue(), "'0.82");
//
//        // test if getMutSig also works by passing an EntrezGeneID
//        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
//        CanonicalGene testGene2 = daoGene.getGene("DDR2");
//
//        MutSig mutSig2 = DaoMutSig.getMutSig(testGene2.getEntrezGeneId(), 1);
//        assertEquals("0.0014", mutSig2.getpValue());
//        assertEquals(273743 , mutSig2.getNumBasesCovered());

//        DaoMutSig daoMutSig = null;
//        daoMutSig.getAllMutSig();
    }
}
