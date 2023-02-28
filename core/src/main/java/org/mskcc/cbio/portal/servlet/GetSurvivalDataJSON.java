/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.web_api.GetClinicalData;
import org.mskcc.cbio.portal.util.*;

import org.json.simple.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Author: yichaoS
 * Date: 8/13
 *
 * Retrieves survival information from clinical data.
 * @param:  case set id / case ids key, cancer study id
 * @return: Set of case id & overall survival months/status & diease free months/status in JSON format
 */
public class GetSurvivalDataJSON extends HttpServlet {

	// class which process access control to cancer studies
    private AccessControl accessControl;
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        accessControl = SpringUtil.getAccessControl();
    }
    
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
        String sampleSetId = httpServletRequest.getParameter("case_set_id");
        String sampleIdsKey = httpServletRequest.getParameter("case_ids_key");
        //So far only accept single data type
        String dataType = httpServletRequest.getParameter("data_type");
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
            int cancerStudyId = cancerStudy.getInternalId();

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

            //Get Clinical Data List - NOTE - as of 12/12/14, patient lists contain sample ids
            HashSet<String> patientIdListHashSet = new HashSet<String>(InternalIdUtil.getStablePatientIdsFromSampleIds(cancerStudyId, sampleIdList));
            List<Patient> clinicalDataList =
                    GetClinicalData.getClinicalData(cancerStudyId, patientIdListHashSet);

            //Assemble JSON object (key <-- patient id)
            JSONObject results = new JSONObject();
            for (int i = 0; i < clinicalDataList.size(); i++){
                Patient clinicalData = clinicalDataList.get(i);
                JSONObject _result = new JSONObject();

                _result.put("case_id", clinicalData.getStableId());
                if (dataType.equalsIgnoreCase("os")) {
                    if (clinicalData.getOverallSurvivalMonths() == null) {
                        _result.put("months", "NA");
                    } else {
                        _result.put("months", clinicalData.getOverallSurvivalMonths());
                    }
                    String osStatus = clinicalData.getOverallSurvivalStatus();
                    if(osStatus == null || osStatus.length() == 0) {
                        _result.put("status", "NA");
                    } else if (osStatus.equalsIgnoreCase("1:DECEASED")) {
                        _result.put("status", "1");
                    } else if(osStatus.equalsIgnoreCase("0:LIVING")) {
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
                    }else if (dfsStatus.equalsIgnoreCase("1:Recurred/Progressed") || dfsStatus.equalsIgnoreCase("1:Recurred")) {
                        _result.put("status", "1");
                    } else if(dfsStatus.equalsIgnoreCase("0:DiseaseFree")) {
                        _result.put("status", "0");
                    }                   
                }
                results.put(clinicalData.getStableId(), _result);
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

