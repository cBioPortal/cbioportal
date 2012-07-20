package org.mskcc.cbio.cgds.scripts;

import junit.framework.TestCase;

import org.mskcc.cbio.cgds.scripts.ResetDatabase;
import org.mskcc.cbio.cgds.scripts.GenerateMutationData;
import org.mskcc.cbio.cgds.util.ProgressMonitor;

import java.io.File;

/**
 * JUnit test for GenerateMutationData class.
 */
public class TestGenerateMutationData extends TestCase {

    public void testGenerateMutationData() throws Exception {
        ResetDatabase.resetDatabase();

        File allCasesFile = new File ("test_data/all_cases.txt");
        File sequencedGeneFile = new File ("test_data/sequenced_genes.txt");
        File sequencedCasesFile = new File ("test_data/sequenced_cases.txt");
        File mutationDataFile = new File ("test_data/mutations.txt");
        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(false);
        GenerateMutationData util = new GenerateMutationData (allCasesFile, sequencedGeneFile,
                sequencedCasesFile, mutationDataFile, pMonitor);
        String out = util.execute();
        String lines[] = out.split("\n");
        boolean line1Pass = false;
        boolean line2Pass = false;
        boolean line3Pass = false;
        for (String line:  lines) {
            if (line.startsWith("1277\tTCGA-02-0083")) {
                assertEquals ("1277\tTCGA-02-0083\tP343S,P774S", line.trim());
                line1Pass = true;
            } else if (line.startsWith("7157\tTCGA-06-0241")) {
                assertEquals ("7157\tTCGA-06-0241\tR248Q", line);
                line2Pass = true;
            } else if (line.startsWith("1956\tTCGA-02-0001")) {
                assertEquals ("1956\tTCGA-02-0001\t0", line);
                line3Pass = true;
            }
        }
        assertTrue (line1Pass);
        assertTrue (line2Pass);
    }
}