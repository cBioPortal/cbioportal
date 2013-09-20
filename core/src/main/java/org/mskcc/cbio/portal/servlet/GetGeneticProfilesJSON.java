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

package org.mskcc.cbio.portal.servlet;

import java.io.*;
import java.util.ArrayList;

import org.json.simple.JSONObject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONValue;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.CaseList;
import org.mskcc.cbio.portal.model.Gene;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.util.CaseSetUtil;

/**
 * Get the genetic profiles for a cancer study
 *
 * same input and output as the original web API
 * getGeneticProfiles
 * except return JSON instead of plain text
 *
 * @param cancer_study_id
 * @param case_set_id, case_ids_key, gene_list (optional)
 * @return JSON objects of genetic profiles
 */
public class GetGeneticProfilesJSON extends HttpServlet  {

    /**
     * Handles HTTP GET Request.
     *
     * @param httpServletRequest  HttpServletRequest
     * @param httpServletResponse HttpServletResponse
     * @throws ServletException
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
        String geneListStr = httpServletRequest.getParameter("gene_list");

        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyIdentifier);
        if (cancerStudy != null) {

            int cancerStudyId = cancerStudy.getInternalId();

            JSONObject result = new JSONObject();
            ArrayList<GeneticProfile> list =
                    DaoGeneticProfile.getAllGeneticProfiles(cancerStudyId);

            if (list.size() > 0) {
                //Retrieve all the profiles available for this cancer study
                if (caseSetId == null && geneListStr == null) {
                    for (GeneticProfile geneticProfile : list) {
                        JSONObject tmpProfileObj = new JSONObject();
                        tmpProfileObj.put("STABLE_ID", geneticProfile.getStableId());
                        tmpProfileObj.put("NAME", geneticProfile.getProfileName());
                        tmpProfileObj.put("DESCRIPTION", geneticProfile.getProfileDescription());
                        tmpProfileObj.put("GENETIC_ALTERATION_TYPE", geneticProfile.getGeneticAlterationType().toString());
                        tmpProfileObj.put("CANCER_STUDY_ID", geneticProfile.getCancerStudyId());
                        tmpProfileObj.put("SHOW_PROFILE_IN_ANALYSIS_TAB", geneticProfile.showProfileInAnalysisTab());
                        result.put(geneticProfile.getStableId(), tmpProfileObj);
                    }
                    httpServletResponse.setContentType("application/json");
                    PrintWriter out = httpServletResponse.getWriter();
                    JSONValue.writeJSONString(result, out);
                } else if (geneListStr != null && caseSetId != null && caseIdsKey != null) { //Only return data available profiles for each queried gene
                    String[] geneList = geneListStr.split("\\s+");
                    try {
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

                        for (String geneId : geneList) {
                            //Get gene
                            DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
                            Gene gene = daoGene.getGene(geneId);

                            JSONObject tmpResult = new JSONObject();
                            for (GeneticProfile geneticProfile : list) {
                                ArrayList<String> tmpProfileDataArr = GeneticAlterationUtil.getGeneticAlterationDataRow(
                                        gene,
                                        caseIdList,
                                        DaoGeneticProfile.getGeneticProfileByStableId(geneticProfile.getStableId()));
                                if (isDataAvailable(tmpProfileDataArr)) {
                                    JSONObject tmpProfileObj = new JSONObject();
                                    tmpProfileObj.put("STABLE_ID", geneticProfile.getStableId());
                                    tmpProfileObj.put("NAME", geneticProfile.getProfileName());
                                    tmpProfileObj.put("DESCRIPTION", geneticProfile.getProfileDescription());
                                    tmpProfileObj.put("GENETIC_ALTERATION_TYPE", geneticProfile.getGeneticAlterationType().toString());
                                    tmpProfileObj.put("CANCER_STUDY_ID", geneticProfile.getCancerStudyId());
                                    tmpProfileObj.put("SHOW_PROFILE_IN_ANALYSIS_TAB", geneticProfile.showProfileInAnalysisTab());
                                    tmpResult.put(geneticProfile.getStableId(), tmpProfileObj);
                                }
                            }
                            result.put(geneId, tmpResult);
                        }
                    } catch (DaoException e) {
                        System.out.println("DaoException Caught:" + e.getMessage());
                    }
                    httpServletResponse.setContentType("application/json");
                    PrintWriter out = httpServletResponse.getWriter();
                    JSONValue.writeJSONString(result, out);
                } else {
                    httpServletResponse.setContentType("application/text");
                    PrintWriter out = httpServletResponse.getWriter();
                    out.print("Error: Please provide both GENE ID and CASE_SET_ID/CASE_IDS_KEY");
                    out.flush();
                }
            } else {
                httpServletResponse.setContentType("application/text");
                PrintWriter out = httpServletResponse.getWriter();
                out.print("Error:  No genetic profiles available for: " + cancerStudyId);
                out.flush();
            }

        }
    }

    private boolean isDataAvailable(ArrayList<String> inputArr) {
        if (inputArr.size() == 0) return false;
        for (String item : inputArr) {
            if (item != null && item != "NaN" && item != "NA") {
                return true;
            }
        }
        return false;
    }

}