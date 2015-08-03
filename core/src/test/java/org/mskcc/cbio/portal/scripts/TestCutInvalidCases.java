/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.scripts;

import org.junit.Ignore;
import org.junit.Test;
import org.mskcc.cbio.portal.util.ProgressMonitor;

import static org.junit.Assert.*;

import java.io.File;

/**
 * JUnit test for CutInvalidCases class.
 */
public class TestCutInvalidCases {

	// TODO Fix this test following the change in data file
	@Ignore
	@Test
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