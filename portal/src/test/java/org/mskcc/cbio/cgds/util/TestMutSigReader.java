package org.mskcc.cbio.cgds.test.util;

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
    
    public void testloadMutSig() throws Exception {

        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(false);

        ResetDatabase.resetDatabase();
        ImportTypesOfCancers.load(new ProgressMonitor(), cancers);

        // Add cancers to a fresh database
        // Add a cancer study whose standardId is "tcga_gbm"
        // In accordance with test_data/testCancerStudy.txt
        CancerStudy cancerStudy = new CancerStudy("Glioblastoma TCGA", "GBM Description", "tcga_gbm", "GBM", false);
        DaoCancerStudy.addCancerStudy(cancerStudy);
        assertEquals(1, cancerStudy.getInternalId());

        // Add some genes to the fresh database
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
        CanonicalGene gene = new CanonicalGene(1956, "EGFR");
        CanonicalGene gene2 = new CanonicalGene(4921, "DDR2");
        daoGeneOptimized.addGene(gene);
        daoGeneOptimized.addGene(gene2);

        int cancerStudyId = MutSigReader.getInternalId(properties);
        assertTrue(CancerStudy.NO_SUCH_STUDY != cancerStudyId);
        MutSigReader.loadMutSig(cancerStudyId, mutSigFile, pMonitor);
        
        // Is the data in the database?
        MutSig mutSig = DaoMutSig.getMutSig("EGFR", 1);
        assertTrue(mutSig != null);
        CanonicalGene testGene = mutSig.getCanonicalGene();
        assertTrue(testGene != null);

        assertTrue("EGFR".equals(testGene.getHugoGeneSymbolAllCaps()));
        assertEquals(mutSig.getNumMutations(), 20);
        assertEquals(mutSig.getNumBasesCovered(), 502500);
        assertTrue(1E-11f == mutSig.getpValue());
        assertTrue(1E-8f == mutSig.getqValue());
    }
}
