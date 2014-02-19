package org.mskcc.cbio.portal.servlet;

import java.io.*;
import java.util.*;
import java.lang.Float;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.*;

/**
 * Author: yichao
 * Date: Dec 2013
 *
 * @param : Gene Symbol
 * @param : Cancer Study Id
 * @param : Case Set Id / Case Ids Key
 * @return : Array of JSON object [case id, alteration value]
 */

public class GetAlterationDataJSON extends HttpServlet {

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
        String[] geneIdList = httpServletRequest.getParameter("gene_list").split("\\s+");
        String caseSetId = httpServletRequest.getParameter("case_set_id");
        String caseIdsKey = httpServletRequest.getParameter("case_ids_key");

        try {

            GeneticProfile final_gp = CoExpUtil.getPreferedGeneticProfile(cancerStudyIdentifier);
            ArrayList<String> caseIds = CoExpUtil.getCaseIds(caseSetId, caseIdsKey);

            JSONObject _result = new JSONObject();
            DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();

            for (String geneId: geneIdList) {
                ArrayList<JSONObject> _geneArr = new ArrayList();
                ArrayList<String> tmpProfileDataArr = 
                            GeneticAlterationUtil.getGeneticAlterationDataRow(
                                daoGeneOptimized.getGene(geneId), 
                                caseIds, 
                                final_gp
                            );
                for (int i = 0; i < caseIds.size(); i++) {
                    if (!tmpProfileDataArr.get(i).equals("NA") && 
                        tmpProfileDataArr.get(i) != null &&
                        !tmpProfileDataArr.get(i).equals("NaN")) {
                        JSONObject _datum = new JSONObject();
                        _datum.put("caseId", caseIds.get(i));
                        _datum.put("value", Float.parseFloat(tmpProfileDataArr.get(i)));
                        _geneArr.add(_datum);                        
                    }
                }
                _result.put(geneId, _geneArr);
            }
            _result.put("profile_name", final_gp.getProfileName());
            _result.put("profile_description", final_gp.getProfileDescription());

            httpServletResponse.setContentType("application/json");
            PrintWriter out = httpServletResponse.getWriter();
            JSONValue.writeJSONString(_result, out);

        } catch (DaoException e) {
            System.out.println(e.getMessage());
        }

    }
}
