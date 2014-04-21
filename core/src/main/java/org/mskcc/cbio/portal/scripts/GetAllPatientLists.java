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

import org.mskcc.cbio.portal.dao.DaoPatientList;
import org.mskcc.cbio.portal.model.PatientList;
import org.mskcc.cbio.portal.util.ProgressMonitor;

import java.util.ArrayList;

/**
 * Command Line Tool to Export All Patient Lists to the Console.
 */
public class GetAllPatientLists {

    public static void main(String[] args) throws Exception {
        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(true);
        DaoPatientList daoPatientList = new DaoPatientList();
        ArrayList <PatientList> patientListMaster = daoPatientList.getAllPatientLists();
        for (PatientList patientList:  patientListMaster) {
            System.out.println (patientList.getPatientListId() + ": "
                    + patientList.getStableId() + ": " + patientList.getName());
        }
    }
}