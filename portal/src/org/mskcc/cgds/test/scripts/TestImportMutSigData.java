package org.mskcc.cgds.test.scripts;

import junit.framework.TestCase;
import org.mskcc.cgds.dao.*;
import org.mskcc.cgds.model.CancerStudy;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.model.MutSig;
import org.mskcc.cgds.scripts.ImportMutSigData;
import org.mskcc.cgds.scripts.ImportTypesOfCancers;
import org.mskcc.cgds.scripts.ResetDatabase;
import org.mskcc.cgds.util.ProgressMonitor;

import java.io.File;

public class TestImportMutSigData extends TestCase {

    /**
     * Tests DaoGene and DaoGeneOptimized.
     *
     * @throws org.mskcc.cgds.dao.DaoException
     *          Database Error.
     */

    public void testImportMutSigData() throws Exception {
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
        assertEquals("EGFR", gene.getHugoGeneSymbolAllCaps());
        assertEquals(4921, gene2.getEntrezGeneId());
        //load testData file and properties file
        File file = new File("test_data/test_mut_sig_data.txt");
        File properties = new File("test_data/testCancerStudy.txt");
        ImportMutSigData parser = new ImportMutSigData(file, properties, pMonitor);
        //import data and properties: see ImportMutSigData Class
        parser.importData();

        //Test if getMutSig works with a HugoGeneSymbol
        MutSig mutSig = DaoMutSig.getMutSig("EGFR", 1);
        CanonicalGene testGene = mutSig.getCanonicalGene();
        assertEquals("EGFR", testGene.getHugoGeneSymbolAllCaps());
        assertEquals(19, mutSig.getnVal());
        assertEquals(1E-8,mutSig.getAdjustedQValue());
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        CanonicalGene testGene2 = daoGene.getGene("DDR2");

        //test if getMutSig also works by passing an EntrezGeneID
        MutSig mutSig2 = DaoMutSig.getMutSig(testGene2.getEntrezGeneId(), 1);
        assertEquals("0.0014", mutSig2.getpValue());
        assertEquals(273743 , mutSig2.getN());

        //daoMutSig.getAllMutSig();
    }
}
