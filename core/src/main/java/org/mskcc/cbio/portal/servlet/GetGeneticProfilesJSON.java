/*
 * Copyright (c) 2015 - 2016 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.servlet;

import java.io.*;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.*;

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

	// class which process access control to cancer studies
    private AccessControl accessControl;
    
    /**
     * Initializes the servlet.
     */
    public void init() throws ServletException {
        super.init();
        accessControl = SpringUtil.getAccessControl();
    }
    
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
        String sampleSetId = httpServletRequest.getParameter("case_set_id");
        String sampleIdsKey = httpServletRequest.getParameter("case_ids_key");
        String geneListStr = httpServletRequest.getParameter("gene_list");
        if (httpServletRequest instanceof XssRequestWrapper) {
            geneListStr = ((XssRequestWrapper)httpServletRequest).getRawParameter("gene_list");
        }

		CancerStudy cancerStudy = null;
		try {
			if (cancerStudyIdentifier != null) {
				cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyIdentifier);
				if (cancerStudy == null
						|| accessControl.isAccessibleCancerStudy(cancerStudy.getCancerStudyStableId()).size() == 0) {
					return;
				}
			} else {
				return;
			}
		} catch (DaoException e) {
			System.out.println("DaoException Caught:" + e.getMessage());
			return;
		}
		
        if (cancerStudy != null) {

            int cancerStudyId = cancerStudy.getInternalId();

            JSONObject result = new JSONObject();
            ArrayList<GeneticProfile> list =
                    DaoGeneticProfile.getAllGeneticProfiles(cancerStudyId);

            if (list.size() > 0) {
                //Retrieve all the profiles available for this cancer study
                if (sampleSetId == null && geneListStr == null) {
                    for (GeneticProfile geneticProfile : list) {
                        JSONObject tmpProfileObj = new JSONObject();
                        tmpProfileObj.put("STABLE_ID", geneticProfile.getStableId());
                        tmpProfileObj.put("NAME", geneticProfile.getProfileName());
                        tmpProfileObj.put("DESCRIPTION", geneticProfile.getProfileDescription());
                        tmpProfileObj.put("GENETIC_ALTERATION_TYPE", geneticProfile.getGeneticAlterationType().name());
                        tmpProfileObj.put("CANCER_STUDY_ID", geneticProfile.getCancerStudyId());
                        tmpProfileObj.put("SHOW_PROFILE_IN_ANALYSIS_TAB", geneticProfile.showProfileInAnalysisTab());
                        // added datatype to be able to make distinction between log data and non-log data
                        tmpProfileObj.put("DATATYPE", geneticProfile.getDatatype());
                        result.put(geneticProfile.getStableId(), tmpProfileObj);
                    }
                    httpServletResponse.setContentType("application/json");
                    PrintWriter out = httpServletResponse.getWriter();
                    JSONValue.writeJSONString(result, out);
                } else if (geneListStr != null && sampleSetId != null && sampleIdsKey != null) { //Only return data available profiles for each queried gene
                    String[] geneList = geneListStr.split("\\s+");
                    try {
                        //Get patient ID list
                        DaoSampleList daoSampleList = new DaoSampleList();
                        SampleList sampleList;
                        ArrayList<String> sampleIdList = new ArrayList<String>();
                        if (sampleSetId.equals("-1") && sampleIdsKey.length() != 0) {
                            String strSampleIds = SampleSetUtil.getSampleIds(sampleIdsKey);
                            String[] sampleArray = strSampleIds.split("\\s+");
                            for (String item : sampleArray) {
                                sampleIdList.add(item);
                            }
                        } else {
                            sampleList = daoSampleList.getSampleListByStableId(sampleSetId);
                            sampleIdList = sampleList.getSampleList();
                        }
                        // NOTE - as of 12/12/14, patient lists contain sample ids
                        List<Integer> internalSampleIds = InternalIdUtil.getInternalNonNormalSampleIds(cancerStudyId, sampleIdList);

                        for (String geneId : geneList) {
                            //Get gene
                            DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
                            Gene gene = daoGene.getGene(geneId);

                            JSONObject tmpResult = new JSONObject();
                            for (GeneticProfile geneticProfile : list) {
                                ArrayList<String> tmpProfileDataArr = GeneticAlterationUtil.getGeneticAlterationDataRow(
                                        gene,
                                        internalSampleIds,
                                        DaoGeneticProfile.getGeneticProfileByStableId(geneticProfile.getStableId()));
                                if (isDataAvailable(tmpProfileDataArr)) {
                                    JSONObject tmpProfileObj = new JSONObject();
                                    tmpProfileObj.put("STABLE_ID", geneticProfile.getStableId());
                                    tmpProfileObj.put("NAME", geneticProfile.getProfileName());
                                    tmpProfileObj.put("DESCRIPTION", geneticProfile.getProfileDescription());
                                    tmpProfileObj.put("GENETIC_ALTERATION_TYPE", geneticProfile.getGeneticAlterationType().name());
                                    tmpProfileObj.put("CANCER_STUDY_ID", geneticProfile.getCancerStudyId());
                                    tmpProfileObj.put("SHOW_PROFILE_IN_ANALYSIS_TAB", geneticProfile.showProfileInAnalysisTab());
                                    // added datatype to be able to make distinction between log data and non-log data
                                    tmpProfileObj.put("DATATYPE", geneticProfile.getDatatype());
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
