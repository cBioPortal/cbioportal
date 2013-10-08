/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

package org.mskcc.cbio.portal.scripts;

import junit.framework.TestCase;
import org.mskcc.cbio.portal.util.ProgressMonitor;

import java.io.File;

/**
 * JUnit test for CutInvalidCases class.
 */
public class TestCutInvalidCases extends TestCase {

    public void testCutInvalidCases() throws Exception {
		// TBD: change this to use getResourceAsStream()
        File casesExcludedFile = new File("target/test-classes/cases_excluded_test.txt");
        File dataFile = new File("target/test-classes/cna_test.txt");
        ProgressMonitor pMonitor = new ProgressMonitor();
        CutInvalidCases parser = new CutInvalidCases(casesExcludedFile,
                dataFile, pMonitor);
        String out = parser.process();

        String lines[] = out.split("\n");
        String headerLine = lines[0];
        String parts[] = headerLine.split("\t");
        for (String header : parts) {
            if (header.trim().equals("TCGA-06-0142")) {
                fail("TCGA-06-0142 should have been stripped out.");
            } else if (header.trim().equals("TCGA-06-0151")) {
                fail("TCGA-06-0142 should have been stripped out.");
            } else if (header.trim().equals("TCGA-06-0159")) {
                fail("TCGA-06-0159 should have been stripped out.");
            }
        }
        int numHeaders = parts.length;
        parts = lines[3].split("\t");

        //  Should go from 95 to 93 columns.
        assertEquals (93, numHeaders);
        assertEquals (93, parts.length);
    }
}
