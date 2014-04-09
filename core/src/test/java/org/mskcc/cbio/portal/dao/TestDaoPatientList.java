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

package org.mskcc.cbio.portal.dao;

import java.util.ArrayList;
import junit.framework.TestCase;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.scripts.ResetDatabase;

/**
 * JUnit test for DaoCase List.
 */
public class TestDaoPatientList extends TestCase {

    public void testDaoPatientList() throws DaoException {
        createSmallDbms();
        DaoPatientList daoPatientList = new DaoPatientList();
        PatientList patientList = new PatientList();
        patientList.setName("Name0");
        patientList.setDescription("Description0");
        patientList.setStableId("stable_0");
        patientList.setCancerStudyId(CancerStudy.NO_SUCH_STUDY);
        patientList.setPatientListCategory(PatientListCategory.ALL_CASES_WITH_CNA_DATA);
        ArrayList<String> patients = new ArrayList<String>();
        patients.add("TCGA-1");
        patients.add("TCGA-2");
        patientList.setPatientList(patients);
        daoPatientList.addPatientList(patientList);
        
        PatientList patientListFromDb = daoPatientList.getPatientListByStableId("stable_0");
        assertEquals("Name0", patientListFromDb.getName());
        assertEquals("Description0", patientListFromDb.getDescription());
        assertEquals(PatientListCategory.ALL_CASES_WITH_CNA_DATA, patientListFromDb.getPatientListCategory());
        assertEquals("stable_0", patientListFromDb.getStableId());
        assertEquals(2, patientListFromDb.getPatientList().size());
    }

    private void createSmallDbms() throws DaoException {
        ResetDatabase.resetDatabase();
        CancerStudy study = new CancerStudy("study", "description", "id", "brca", true);
        Patient p = new Patient(study, "TCGA-1");
        int pId = DaoPatient.addPatient(p);
        p = new Patient(study, "TCGA-2");
        pId = DaoPatient.addPatient(p);
    }
}
