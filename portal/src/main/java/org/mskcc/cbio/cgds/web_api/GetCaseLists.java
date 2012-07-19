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

