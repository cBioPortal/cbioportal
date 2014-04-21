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

package org.mskcc.cbio.portal.scripts;

import junit.framework.TestCase;

import org.mskcc.cbio.portal.util.ProgressMonitor;

import java.io.File;

/**
 * JUnit test for GenerateMutationData class.
 */
public class TestGenerateMutationData extends TestCase {

    public void testGenerateMutationData() throws Exception {
        ResetDatabase.resetDatabase();

		// TBD: change this to use getResourceAsStream()
        File allCasesFile = new File ("target/test-classes/all_cases.txt");
        File sequencedGeneFile = new File ("target/test-classes/sequenced_genes.txt");
        File sequencedCasesFile = new File ("target/test-classes/sequenced_cases.txt");
        File mutationDataFile = new File ("target/test-classes/mutations.txt");
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