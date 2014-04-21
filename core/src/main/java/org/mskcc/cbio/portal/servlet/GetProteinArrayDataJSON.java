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

import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.*;

import org.json.simple.*;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.ArrayList;

/**
 * Author: yichaoS
 * Date: 7/23/13
 *
 * Retrieves protein and/or phosphoprotein levels measured by reverse-phase protein arrays (RPPA).
 */
public class GetProteinArrayDataJSON extends HttpServlet {

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
        String patientSetId = httpServletRequest.getParameter("case_set_id");
        String patientIdsKey = httpServletRequest.getParameter("case_ids_key");
        String proteinArrayId = httpServletRequest.getParameter("protein_array_id");

        try {

            //Get Cancer Study ID (int)
            CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyIdentifier);
            int cancerStudyId = cancerStudy.getInternalId();

            //Get patient ID list
            DaoPatientList daoPatientList = new DaoPatientList();
            PatientList patientList;
            ArrayList<String> patientIdList = new ArrayList<String>();
            if (patientSetId.equals("-1") && patientIdsKey.length() != 0) {
                String strPatientIds = PatientSetUtil.getPatientIds(patientIdsKey);
                String[] patientArray = strPatientIds.split("\\s+");
                for (String item : patientArray) {
                    patientIdList.add(item);
                }
            } else {
                patientList = daoPatientList.getPatientListByStableId(patientSetId);
                patientIdList = patientList.getPatientList();
            }

            //Get Protein Array Data and return JSON
            JSONObject result = new JSONObject();
            DaoProteinArrayData daoPAD = DaoProteinArrayData.getInstance();
            for (ProteinArrayData pad : daoPAD.getProteinArrayData(cancerStudyId, proteinArrayId,
                                                                    InternalIdUtil.getInternalSampleIdsFromPatientIds(cancerStudyId, patientIdList))) {
                Sample s = DaoSample.getSampleById(pad.getSampleId());
                result.put(s.getStableId(), pad.getAbundance());
            }

            httpServletResponse.setContentType("application/json");
            PrintWriter out = httpServletResponse.getWriter();
            JSONValue.writeJSONString(result, out);

        } catch (DaoException e) {
                httpServletResponse.setContentType("application/text");
                PrintWriter out = httpServletResponse.getWriter();
                out.print("DaoException: " + e.getMessage());
                out.flush();
        }

    }
}

