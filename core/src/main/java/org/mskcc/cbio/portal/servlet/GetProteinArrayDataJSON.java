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

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoCaseList;
import org.mskcc.cbio.portal.dao.DaoProteinArrayData;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.CaseList;
import org.mskcc.cbio.portal.model.ProteinArrayData;
import org.mskcc.cbio.portal.util.CaseSetUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
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
        String caseSetId = httpServletRequest.getParameter("case_set_id");
        String caseIdsKey = httpServletRequest.getParameter("case_ids_key");
        String proteinArrayId = httpServletRequest.getParameter("protein_array_id");

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

            //Get Protein Array Data and return JSON
            JSONObject result = new JSONObject();
            DaoProteinArrayData daoPAD = DaoProteinArrayData.getInstance();
            for (ProteinArrayData pad : daoPAD.getProteinArrayData(cancerStudyId, proteinArrayId, caseIdList)) {
                result.put(pad.getCaseId(), pad.getAbundance());
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

