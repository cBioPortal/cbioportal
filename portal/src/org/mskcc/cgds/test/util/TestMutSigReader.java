package org.mskcc.cgds.test.util;

import junit.framework.TestCase;
import org.mskcc.cgds.dao.DaoCancerStudy;
import org.mskcc.cgds.dao.DaoGeneOptimized;
import org.mskcc.cgds.dao.DaoMutSig;
import org.mskcc.cgds.model.CancerStudy;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.model.MutSig;
import org.mskcc.cgds.scripts.ImportTypesOfCancers;
import org.mskcc.cgds.scripts.ResetDatabase;
import org.mskcc.cgds.util.MutSigReader;
import org.mskcc.cgds.util.ProgressMonitor;

import java.io.File;

public class TestMutSigReader extends TestCase {

    File properties = new File("test_data/testCancerStudy.txt");
    File mutSigFile = new File("test_data/test_mut_sig_data.txt");
    
    ProgressMonitor pm = new ProgressMonitor();
    File cancers =  new File("test_data/cancers.txt");


    public void testgetInternalId() throws Exception {

        // since tcga_gbm has InternalID = 1 in the Sample Data (./loadSampleData.sh)
        assertEquals(1, MutSigReader.getInternalId(properties));
    }
    
    public void testloadMutSig() throws Exception {

        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(false);

        // Add cancers to a fresh database
        // Add a cancer study whose standardId is "tcga_gbm"
        // In accordance with test_data/testCancerStudy.txt
        ResetDatabase.resetDatabase();
        ImportTypesOfCancers.load(new ProgressMonitor(), new File("test_data/cancers.txt"));
        CancerStudy cancerStudy = new CancerStudy("Glioblastoma TCGA", "GBM Description", "tcga_gbm", "GBM", false);
        DaoCancerStudy.addCancerStudy(cancerStudy);
        assertEquals(1, cancerStudy.getInternalId());

        // Add some genes to the fresh database
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
        CanonicalGene gene = new CanonicalGene(1956, "EGFR");
        CanonicalGene gene2 = new CanonicalGene(4921, "DDR2");
        daoGeneOptimized.addGene(gene);
        daoGeneOptimized.addGene(gene2);

        MutSigReader.loadMutSig(MutSigReader.getInternalId(properties), mutSigFile, pMonitor);


        // Test if getMutSig works with a HugoGeneSymbol
        MutSig mutSig = DaoMutSig.getMutSig("EGFR", 1);
        CanonicalGene testGene = mutSig.getCanonicalGene();

        assertTrue("EGFR".equals(testGene.getHugoGeneSymbolAllCaps()));
        assertEquals(mutSig.getNumMutations(), 20);
        assertEquals(mutSig.getNumBasesCovered(), 502500);
        assertTrue("<1E-11".equals(mutSig.getpValue()));
        assertTrue("<1E-8".equals(mutSig.getqValue()));

        // test if getMutSig also works by passing an EntrezGeneID
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        CanonicalGene testGene2 = daoGene.getGene("DDR2");

        MutSig mutSig2 = DaoMutSig.getMutSig(testGene2.getEntrezGeneId(), 1);
        assertEquals("0.0014", mutSig2.getpValue());
        assertEquals(273743 , mutSig2.getNumBasesCovered());

    }
}
