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

package org.mskcc.cbio.cgds.web_api;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import junit.framework.TestCase;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.model.CancerStudy;
import org.mskcc.cbio.cgds.scripts.ImportClinicalData;
import org.mskcc.cbio.cgds.scripts.ResetDatabase;
import org.mskcc.cbio.cgds.util.ProgressMonitor;

/**
 * Tests Get Clinical Data.
 *
 * @author Ethan Cerami.
 */
public class TestGetClinicalData extends TestCase {

    /**
     * Tests Get Clinical Data.
     *
     * @throws DaoException Database Access Error.
     * @throws IOException IO Error.
     */
    public void testGetClinicalData() throws DaoException, IOException {

        ResetDatabase.resetDatabase();
        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(false);
		// TBD: change this to use getResourceAsStream()
        File file = new File("target/test-classes/clinical_test.txt");
        CancerStudy cancerStudy = new CancerStudy("test","test","test","test",true);
        ImportClinicalData importClinicalData = new ImportClinicalData(cancerStudy, file, pMonitor);
		importClinicalData.importData();
		
        HashSet<String> caseSet = new HashSet<String>();
        caseSet.add("TCGA-04-1331");
        caseSet.add("TCGA-24-2030");
        caseSet.add("TCGA-24-2261");
        String clinicalDataOut = GetClinicalData.getClinicalData(1,caseSet, false);
//        String lines[] = clinicalDataOut.split("\n");
//
//        assertTrue(lines[2].startsWith("TCGA-24-2030\tNA\tNA\t21.18\tRecurred/Progressed\tNA"));
//        assertTrue(lines[3].startsWith("TCGA-24-2261\t0.79\tDECEASED\tNA" +
//                "\tRecurred/Progressed\t76.43"));
    }
}
