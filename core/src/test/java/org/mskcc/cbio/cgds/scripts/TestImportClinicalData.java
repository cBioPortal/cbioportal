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

package org.mskcc.cbio.cgds.scripts;

import junit.framework.TestCase;
import org.mskcc.cbio.cgds.dao.DaoClinicalData;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.util.ProgressMonitor;
import org.mskcc.cbio.cgds.model.ClinicalData;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.ArrayList;
import org.mskcc.cbio.cgds.model.CancerStudy;

/**
 * Tests Import of Clinical Data.
 *
 * @author Ethan Cerami.
 */
public class TestImportClinicalData extends TestCase {

    /**
     * Test importing of Clinical Data File.
     *
     * @throws DaoException Database Access Error.
     * @throws IOException  IO Error.
     */
    public void testImportClinicalData() throws DaoException, IOException {
        ResetDatabase.resetDatabase();
        ProgressMonitor pMonitor = new ProgressMonitor();
		// TBD: change this to use getResourceAsStream()
        File file = new File("target/test-classes/clinical_test.txt");
        CancerStudy cancerStudy = new CancerStudy("test","test","test","test",true);
        cancerStudy.setInternalId(1);
        ImportClinicalData importClinicalData = new ImportClinicalData(cancerStudy, file, pMonitor);
        importClinicalData.importData();

        DaoClinicalData dao = new DaoClinicalData();

        HashSet <String> caseSet = new HashSet<String>();
        caseSet.add("TCGA-04-1331");
        caseSet.add("TCGA-24-2030");
        caseSet.add("TCGA-24-2261");

        ArrayList<ClinicalData> clinicalCaseList = dao.getCases(1,caseSet);
        assertEquals (3, clinicalCaseList.size());

        ClinicalData clinical0 = clinicalCaseList.get(0);
        assertEquals (new Double(79.04), clinical0.getAgeAtDiagnosis());
        assertEquals ("DECEASED", clinical0.getOverallSurvivalStatus());
        assertEquals ("Recurred/Progressed", clinical0.getDiseaseFreeSurvivalStatus());
        assertEquals (new Double(43.8), clinical0.getOverallSurvivalMonths());
        assertEquals (new Double(15.05), clinical0.getDiseaseFreeSurvivalMonths());

        ClinicalData clinical1 = clinicalCaseList.get(1);
        assertEquals (null, clinical1.getAgeAtDiagnosis());
        assertEquals (null, clinical1.getOverallSurvivalStatus());
        assertEquals ("Recurred/Progressed", clinical1.getDiseaseFreeSurvivalStatus());
        assertEquals (null, clinical1.getOverallSurvivalMonths());
        assertEquals (new Double(21.18), clinical1.getDiseaseFreeSurvivalMonths());

        ClinicalData clinical2 = clinicalCaseList.get(2);
        assertEquals (null, clinical2.getDiseaseFreeSurvivalMonths());
    }
}