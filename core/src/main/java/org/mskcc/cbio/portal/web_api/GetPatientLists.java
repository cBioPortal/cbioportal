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

package org.mskcc.cbio.portal.web_api;

import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;

import java.util.ArrayList;

/**
 * Web API for Getting Patient Lists.
 *
 * @author Ethan Cerami.
 */
public class GetPatientLists {

    /**
     * Gets all Patient Sets Associated with a specific Cancer Study.
     *
     * @param cancerStudyId Cancer Study ID.
     * @return ArrayList of PatientSet Objects.
     * @throws DaoException Database Error.
     */
    public static ArrayList<PatientList> getPatientLists(String cancerStudyId)
            throws DaoException {
        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyId);
        if (cancerStudy != null) {
            DaoPatientList daoPatientList = new DaoPatientList();
            ArrayList<PatientList> patientList = daoPatientList.getAllPatientLists(cancerStudy.getInternalId());
            return patientList;
        } else {
            ArrayList<PatientList> patientList = new ArrayList<PatientList>();
            return patientList;
        }
    }

    /**
     * Get Patient List for Specified Stable Cancer Study ID.
     *
     * @param cancerStudyStableId Stable Cancer Study ID.
     * @return Table output.
     * @throws DaoException Database Error.
     */
    public static String getPatientListsAsTable(String cancerStudyStableId) throws DaoException {
        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyStableId);
        StringBuilder buf = new StringBuilder();
        if (cancerStudy != null) {
            int cancerStudyInternalId = cancerStudy.getInternalId();
            DaoPatientList daoPatientList = new DaoPatientList();
            ArrayList<PatientList> list = daoPatientList.getAllPatientLists(cancerStudyInternalId);
            if (list.size() > 0) {
                buf.append("case_list_id\tcase_list_name\tcase_list_description\t"
                        + "cancer_study_id\t" + "case_ids\n");
                for (PatientList patientList : list) {
                    buf.append(patientList.getStableId()).append("\t");
                    buf.append(patientList.getName()).append("\t");
                    buf.append(patientList.getDescription()).append("\t");
                    buf.append(patientList.getCancerStudyId()).append("\t");
                    for (String aPatient : patientList.getPatientList()) {
                        buf.append(aPatient).append(" ");
                    }
                    buf.append("\n");
                }
            } else {
                buf.append("Error:  No case lists available for:  ").append(cancerStudyStableId).append(".\n");
                return buf.toString();
            }
        }
        return buf.toString();
    }
}

