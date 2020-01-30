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

import java.io.*;
import java.util.ArrayList;
import javax.servlet.*;
import javax.servlet.http.*;
import org.json.simple.*;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.*;

/**
 * Author: yichaoS
 * Date: 7/23/13
 *
 * Retrieves protein and/or phosphoprotein levels measured by reverse-phase protein arrays (RPPA).
 */
public class GetProteinArrayDataJSON extends HttpServlet {
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
    protected void doGet(
        HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse
    )
        throws ServletException, IOException {
        doPost(httpServletRequest, httpServletResponse);
    }

    /**
     * Handles the HTTP POST Request.
     *
     * @param httpServletRequest  HttpServletRequest
     * @param httpServletResponse HttpServletResponse
     * @throws ServletException
     */
    protected void doPost(
        HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse
    )
        throws ServletException, IOException {
        String cancerStudyIdentifier = httpServletRequest.getParameter(
            "cancer_study_id"
        );
        String sampleSetId = httpServletRequest.getParameter("case_set_id");
        String sampleIdsKey = httpServletRequest.getParameter("case_ids_key");
        String proteinArrayId = httpServletRequest.getParameter(
            "protein_array_id"
        );
        CancerStudy cancerStudy = null;
        try {
            if (cancerStudyIdentifier != null) {
                cancerStudy =
                    DaoCancerStudy.getCancerStudyByStableId(
                        cancerStudyIdentifier
                    );
                if (
                    cancerStudy == null ||
                    accessControl
                        .isAccessibleCancerStudy(
                            cancerStudy.getCancerStudyStableId()
                        )
                        .size() ==
                    0
                ) {
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

            //Get Protein Array Data and return JSON
            JSONObject result = new JSONObject();
            DaoProteinArrayData daoPAD = DaoProteinArrayData.getInstance();
            // NOTE - as of 12/12/14, patient lists contain sample ids
            for (ProteinArrayData pad : daoPAD.getProteinArrayData(
                cancerStudyId,
                proteinArrayId,
                InternalIdUtil.getInternalNonNormalSampleIds(
                    cancerStudyId,
                    sampleIdList
                )
            )) {
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
