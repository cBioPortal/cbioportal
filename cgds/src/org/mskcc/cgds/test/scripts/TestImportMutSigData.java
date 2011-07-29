package org.mskcc.cgds.test.scripts;

import junit.framework.TestCase;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoGeneOptimized;
import org.mskcc.cgds.dao.DaoMutSig;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.model.MutSig;
import org.mskcc.cgds.scripts.ImportGeneData;
import org.mskcc.cgds.scripts.ImportMutSigData;
import org.mskcc.cgds.scripts.ResetDatabase;
import org.mskcc.cgds.util.ProgressMonitor;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: lennartbastian
 * Date: 27/07/2011
 * Time: 10:48
 * To change this template use File | Settings | File Templates.
 */

public class TestImportMutSigData extends TestCase {

    /**
     * Tests DaoGene and DaoGeneOptimized.
     *
     * @throws org.mskcc.cgds.dao.DaoException
     *          Database Error.
     */

    public void testImportMutSigData() throws Exception {
        //Test needs access to Gene Database; resetting whole Database will mess up test.
        //ResetDatabase.resetDatabase();
        DaoMutSig daoMutSig = DaoMutSig.getInstance();
        daoMutSig.deleteAllRecords();

        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(false);
        File file = new File("testData/test_mut_sig_data.txt");
        File properties = new File("testData/testCancerStudy.txt");
        ImportMutSigData parser = new ImportMutSigData(file, properties, pMonitor);
        parser.importData();
        parser.loadProps(properties);
        //Test if getMutSig works with a HugoGeneSymbol
        MutSig mutSig = DaoMutSig.getMutSig("EGFR");
        CanonicalGene gene = mutSig.getCanonicalGene();
        assertEquals("EGFR", gene.getHugoGeneSymbol());
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        CanonicalGene gene2 = daoGene.getGene("DDR2");
        //test if getMutSig also works by passing an EntrezGeneID
        mutSig = DaoMutSig.getMutSig(gene2.getEntrezGeneId());
        assertEquals("0.0014", mutSig.getpValue());

        daoMutSig.getAllMutSig();
    }


}
