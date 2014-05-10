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

package org.mskcc.cbio.portal.servlet;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoCaseList;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.CaseList;
import org.mskcc.cbio.portal.model.Patient;
import org.mskcc.cbio.portal.web_api.GetClinicalData;
import org.mskcc.cbio.portal.util.CaseSetUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashSet;

/**
 * Author: yichaoS
 * Date: 8/13
 *
 * Retrieves survival information from clinical data.
 * @param:  case set id / case ids key, cancer study id
 * @return: Set of case id & overall survival months/status & diease free months/status in JSON format
 */
public class GetSurvivalDataJSON extends HttpServlet {

    /**
     * Handles HTTP GET Request.
     *
     * @param httpServletRequest  HttpServletRequest
     * @param httpServletResponse HttpServletResponse
     * @throws javax.servlet.ServletException
     */
    protected void doGet(HttpServletRequest httpServletRequest,
                         HttpServletResponse httpServletResponse) throws ServletException, IOException {
        doPost(httpServletRequest, httpServletResponse);
    }

    /**
     * Handles the HTTP POST Request.
     *
     * @param httpServletRequest  HttpServletRequest
     * @param httpServletResponse HttpServletResponse
     * @throws ServletException
     */
    protected void doPost(HttpServletRequest httpServletRequest,
                          HttpServletResponse httpServletResponse) throws ServletException, IOException {

        String cancerStudyIdentifier = httpServletRequest.getParameter("cancer_study_id");
        String caseSetId = httpServletRequest.getParameter("case_set_id");
        String caseIdsKey = httpServletRequest.getParameter("case_ids_key");
        //So far only accept single data type
        String dataType = httpServletRequest.getParameter("data_type");

        try {

            //Get Cancer Study ID (int)
            CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyIdentifier);
            int cancerStudyId = cancerStudy.getInternalId();

            //Get Case case ID list
            DaoCaseList daoCaseList = new DaoCaseList();
            CaseList caseList;
            ArrayList<String> caseIdList = new ArrayList<String>();
            if (caseSetId.equals("-1") && caseIdsKey.length() != 0) {
                String strCaseIds = CaseSetUtil.getCaseIds(caseIdsKey);
                String[] caseArray = strCaseIds.split("\\s+");
                for (String item : caseArray) {
                    caseIdList.add(item);
                }
            } else {
                caseList = daoCaseList.getCaseListByStableId(caseSetId);
                caseIdList = caseList.getCaseList();
            }

            //Get Clinical Data List
            HashSet<String> caseIdListHashSet = new HashSet<String>(caseIdList);
            List<Patient> clinicalDataList =
                    GetClinicalData.getClinicalData(cancerStudyId, caseIdListHashSet);

            //Assemble JSON object (key <-- case id)
            JSONObject results = new JSONObject();
            for (int i = 0; i < clinicalDataList.size(); i++){
                Patient clinicalData = clinicalDataList.get(i);
                JSONObject _result = new JSONObject();

                _result.put("case_id", clinicalData.getCaseId());
                if (dataType.equalsIgnoreCase("os")) {
                    if (clinicalData.getOverallSurvivalMonths() == null) {
                        _result.put("months", "NA");
                    } else {
                        _result.put("months", clinicalData.getOverallSurvivalMonths());
                    }
                    String osStatus = clinicalData.getOverallSurvivalStatus();
                    if(osStatus == null || osStatus.length() == 0) {
                        _result.put("status", "NA");
                    } else if (osStatus.equalsIgnoreCase("DECEASED")) {
                        _result.put("status", "1");
                    } else if(osStatus.equalsIgnoreCase("LIVING")) {
                        _result.put("status", "0");
                    }   
                } else if (dataType.equalsIgnoreCase("dfs")) {
                    if (clinicalData.getDiseaseFreeSurvivalMonths() == null) {
                        _result.put("months", "NA");
                    } else {
                        _result.put("months", clinicalData.getDiseaseFreeSurvivalMonths());
                    }
                    String dfsStatus = clinicalData.getDiseaseFreeSurvivalStatus();
                    if(dfsStatus == null || dfsStatus.length() == 0) {
                        _result.put("status", "NA");
                    }else if (dfsStatus.equalsIgnoreCase("Recurred/Progressed") || dfsStatus.equalsIgnoreCase("Recurred")) {
                        _result.put("status", "1");
                    } else if(dfsStatus.equalsIgnoreCase("DiseaseFree")) {
                        _result.put("status", "0");
                    }                     
                }
                results.put(clinicalData.getCaseId(), _result);
            }

            httpServletResponse.setContentType("application/json");
            PrintWriter out = httpServletResponse.getWriter();
            JSONValue.writeJSONString(results, out);

        } catch (DaoException e) {
            httpServletResponse.setContentType("application/text");
            PrintWriter out = httpServletResponse.getWriter();
            out.print("DaoException: " + e.getMessage());
            out.flush();
        }

    }

}

