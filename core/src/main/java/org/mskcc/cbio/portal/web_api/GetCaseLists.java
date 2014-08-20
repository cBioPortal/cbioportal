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

package org.mskcc.cbio.portal.web_api;

import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoCaseList;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.CaseList;

import java.util.ArrayList;

/**
 * Web API for Getting Case Lists.
 *
 * @author Ethan Cerami.
 */
public class GetCaseLists {

    /**
     * Gets all Case Sets Associated with a specific Cancer Study.
     *
     * @param cancerStudyId Cancer Study ID.
     * @return ArrayList of CaseSet Objects.
     * @throws DaoException Database Error.
     */
    public static ArrayList<CaseList> getCaseLists(String cancerStudyId)
            throws DaoException {
        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyId);
        if (cancerStudy != null) {
            DaoCaseList daoCaseList = new DaoCaseList();
            ArrayList<CaseList> caseList = daoCaseList.getAllCaseLists(cancerStudy.getInternalId());
            return caseList;
        } else {
            ArrayList<CaseList> caseList = new ArrayList<CaseList>();
            return caseList;
        }
    }

    /**
     * Get Case List for Specified Stable Cancer Study ID.
     *
     * @param cancerStudyStableId Stable Cancer Study ID.
     * @return Table output.
     * @throws DaoException Database Error.
     */
    public static String getCaseListsAsTable(String cancerStudyStableId) throws DaoException {
        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyStableId);
        StringBuilder buf = new StringBuilder();
        if (cancerStudy != null) {
            int cancerStudyInternalId = cancerStudy.getInternalId();
            DaoCaseList daoCaseList = new DaoCaseList();
            ArrayList<CaseList> list = daoCaseList.getAllCaseLists(cancerStudyInternalId);
            if (list.size() > 0) {
                buf.append("case_list_id\tcase_list_name\tcase_list_description\t"
                        + "cancer_study_id\t" + "case_ids\n");
                for (CaseList caseList : list) {
                    buf.append(caseList.getStableId()).append("\t");
                    buf.append(caseList.getName()).append("\t");
                    buf.append(caseList.getDescription()).append("\t");
                    buf.append(caseList.getCancerStudyId()).append("\t");
                    for (String aCase : caseList.getCaseList()) {
                        buf.append(aCase).append(" ");
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

