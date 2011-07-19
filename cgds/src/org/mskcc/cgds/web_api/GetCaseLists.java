package org.mskcc.cgds.web_api;

import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoCaseList;
import org.mskcc.cgds.model.CaseList;
import org.mskcc.cgds.servlet.WebService;

import java.util.ArrayList;

public class GetCaseLists {

   public static String getCaseLists(int cancerStudyId) throws DaoException {
      DaoCaseList daoCaseList = new DaoCaseList();
      ArrayList<CaseList> list = daoCaseList.getAllCaseLists(cancerStudyId);
      StringBuffer buf = new StringBuffer();
      if (list.size() > 0) {

         buf.append("case_list_id\tcase_list_name\tcase_list_description\t" + "cancer_study_id\t" + "case_ids\n");
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
         return buf.toString();
      } else {

         buf.append("Error:  No case lists available for " + WebService.CANCER_STUDY_ID + ":  " + cancerStudyId + ".\n");
         return buf.toString();
      }
   }

}
