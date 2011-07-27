package org.mskcc.cgds.web_api;

import org.mskcc.cgds.dao.DaoCancerStudy;
import org.mskcc.cgds.dao.DaoCaseList;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.model.CancerStudy;
import org.mskcc.cgds.model.CaseList;

import java.util.ArrayList;

/**
 * Web API for Getting Case Lists.
 *
 * @author Ethan Cerami.
 */
public class GetCaseLists {

    /**
     * Get Case List for Specified Stable Cancer Study ID.
     *
     * @param cancerStudyStableId Stable Cancer Study ID.
     * @return Table output.
     * @throws DaoException Database Error.
     */
    public static String getCaseLists(String cancerStudyStableId) throws DaoException {
        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyStableId);
        StringBuffer buf = new StringBuffer();
        if (cancerStudy != null) {
            int cancerStudyInternalId = cancerStudy.getStudyId();
            DaoCaseList daoCaseList = new DaoCaseList();
            ArrayList<CaseList> list = daoCaseList.getAllCaseLists(cancerStudyInternalId);
            if (list.size() > 0) {
                buf.append("case_list_id\tcase_list_name\tcase_list_description\t"
                        + "cancer_study_id\t" + "case_ids\n");
                for (CaseList caseList : list) {
                    buf.append(caseList.getStableId() + "\t");
                    buf.append(caseList.getName() + "\t");
                    buf.append(caseList.getDescription() + "\t");
                    buf.append(caseList.getCancerStudyId() + "\t");
                    for (String _case : caseList.getCaseList()) {
                        buf.append(_case + " ");
                    }
                    buf.append("\n");
                }
            } else {
                buf.append("Error:  No case lists available for:  " + cancerStudyStableId + ".\n");
                return buf.toString();
            }
        }
        return buf.toString();
    }
}

