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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoClinicalData;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.ClinicalAttribute;
import org.mskcc.cbio.portal.model.ClinicalData;
import org.mskcc.cbio.portal.model.ClinicalData;
import org.mskcc.cbio.portal.util.AccessControl;
import org.mskcc.cbio.portal.util.SpringUtil;
import org.owasp.validator.html.PolicyException;

public class ClinicalJSON extends HttpServlet {
    // our logger
    private static Log LOG = LogFactory.getLog(ClinicalJSON.class);

    // class which process access control to cancer studies
    private AccessControl accessControl;

    public static final String SAMPLES_DELIMITER = " ";
    public static final String ALL = "all";

    /**
     * Initializes the servlet.
     *
     * @throws ServletException
     */
    public void init() throws ServletException {
        super.init();
        accessControl = SpringUtil.getAccessControl();
    }

    /**
     * take a clinical object and convert it to a map
     *
     * @param clinical
     * @return
     */
    public JSONObject reflectToMap(ClinicalData clinical) {
        JSONObject object = new JSONObject();
        object.put("attr_id", clinical.getAttrId());
        object.put("attr_val", clinical.getAttrVal());
        //TODO: at some point we may want to incorporate the cancer_study_id
        //        object.put("cancer_study_id", Integer.toString(clinical.getCancerStudyId()));
        object.put("case_id", clinical.getStableId());
        return object;
    }

    public static Map<String, String> reflectToMap(
        ClinicalAttribute clinicalAttribute
    ) {
        JSONObject object = new JSONObject();
        object.put("attr_id", clinicalAttribute.getAttrId());
        object.put("datatype", clinicalAttribute.getDatatype());
        object.put("description", clinicalAttribute.getDescription());
        object.put("display_name", clinicalAttribute.getDisplayName());
        return object;
    }

    public JSONArray clinicals2JSONArray(List<ClinicalData> clincials) {
        JSONArray toReturn = new JSONArray();
        for (ClinicalData c : clincials) {
            toReturn.add(reflectToMap(c));
        }
        return toReturn;
    }

    /**
     * Using post request to increase URL length, actually this should not be a POST request
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    public void doPost(
        HttpServletRequest request,
        HttpServletResponse response
    )
        throws ServletException, IOException {
        String query = request.getParameter("q");
        String type = request.getParameter("t");

        String samples = request.getParameter("samples");
        String cancerStudyId = request.getParameter("cancer_study_id");

        List<ClinicalData> clinicals;
        JSONArray maps = null;

        try {
            if (cancerStudyId != null) {
                CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(
                    cancerStudyId
                );
                if (
                    cancerStudy != null &&
                    accessControl
                        .isAccessibleCancerStudy(
                            cancerStudy.getCancerStudyStableId()
                        )
                        .size() ==
                    1
                ) {
                    if (samples == null || samples.equals(ALL)) {
                        clinicals = DaoClinicalData.getData(cancerStudyId);
                        maps = clinicals2JSONArray(clinicals);
                    } else {
                        clinicals =
                            DaoClinicalData.getData(
                                cancerStudyId,
                                Arrays.asList(
                                    samples.trim().split(SAMPLES_DELIMITER)
                                )
                            );
                        maps = clinicals2JSONArray(clinicals);
                    }
                    for (ClinicalData c : clinicals) {
                        maps.add(reflectToMap(c));
                    }
                }
            }
        } catch (DaoException e) {
            throw new ServletException(e);
        }

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JSONArray.writeJSONString(maps, out);
    }

    /**
     * reroute get to post
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        doPost(request, response);
    }
}
