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

package org.mskcc.cbio.cgds.dao;

import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.dao.DaoClinicalData;
import org.mskcc.cbio.cgds.scripts.ResetDatabase;
import org.mskcc.cbio.cgds.model.ClinicalData;

import java.util.ArrayList;
import java.util.HashSet;

import junit.framework.TestCase;

/**
 * Tests the DaoClinicalData Class.
 *
 * @author Ethan Cerami
 */
public class TestDaoClinical extends TestCase {

    /**
     * Basic Unit Tests.
     * @throws DaoException Database Access Error.
     */
    public void testDaoCase() throws DaoException {
        ResetDatabase.resetDatabase();
        DaoClinicalData daoClinical = new DaoClinicalData();

        daoClinical.addCase("TCGA-12-1234", new Double(0.5), "ALIVE", null, null, null);
        daoClinical.addCase("TCGA-12-1235", new Double(0.7), "ALIVE", new Double(0.9),
                "RECURRED", new Double(65));

        HashSet<String> caseSet = new HashSet<String>();
        caseSet.add("TCGA-12-1234");
        caseSet.add("TCGA-12-1235");
        ArrayList <ClinicalData> caseList = daoClinical.getCases(caseSet);

        assertEquals (2, caseList.size());
        ClinicalData caseSurvival = caseList.get(0);
        assertEquals ("TCGA-12-1234", caseSurvival.getCaseId());
        assertEquals (new Double(0.5), caseSurvival.getOverallSurvivalMonths());
        assertEquals ("ALIVE", caseSurvival.getOverallSurvivalStatus());
        assertNull (caseSurvival.getDiseaseFreeSurvivalMonths());
        assertNull (caseSurvival.getDiseaseFreeSurvivalStatus());
        assertNull (caseSurvival.getAgeAtDiagnosis());

        caseSurvival = caseList.get(1);
        assertEquals ("TCGA-12-1235", caseSurvival.getCaseId());
        assertEquals (new Double(0.7), caseSurvival.getOverallSurvivalMonths());
        assertEquals ("ALIVE", caseSurvival.getOverallSurvivalStatus());
        assertEquals (new Double(0.9), caseSurvival.getDiseaseFreeSurvivalMonths());
        assertEquals ("RECURRED", caseSurvival.getDiseaseFreeSurvivalStatus());
        assertEquals (new Double(65), caseSurvival.getAgeAtDiagnosis());
    }
}
