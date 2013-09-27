/*
 * Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
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
 * has been advised of the possibility of such damage.  See
 * the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package org.mskcc.cbio.portal.servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mskcc.cbio.portal.dao.DaoClinicalData;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.model.ClinicalData;
import org.mskcc.cbio.portal.model.ClinicalAttribute;
import org.mskcc.cbio.portal.model.ClinicalData;
import org.owasp.validator.html.PolicyException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ClinicalJSON extends HttpServlet {
    private ServletXssUtil servletXssUtil;

    // our logger
    private static Log LOG = LogFactory.getLog(ClinicalJSON.class);

    public static final String SAMPLES_DELIMITER = " ";
    public static final String ALL = "all";

    /**
     * Initializes the servlet.
     *
     * @throws ServletException
     */
    public void init() throws ServletException {
        super.init();
        try {
            servletXssUtil = ServletXssUtil.getInstance();
        } catch (PolicyException e) {
            throw new ServletException(e);
        }
    }

    /**
     * take a clinical object and convert it to a map
     *
     * @param clinical
     * @return
     */
    public JSONObject reflectToMap(ClinicalData clinical) {
        JSONObject map = new JSONObject();

        map.put("attr_id", clinical.getAttrId());
        map.put("attr_val", clinical.getAttrVal());
        //TODO: at some point we may want to incorporate the cancer_study_id
//        map.put("cancer_study_id", Integer.toString(clinical.getCancerStudyId()));
        map.put("case_id", clinical.getCaseId());

        return map;
    }

    public static Map<String, String> reflectToMap(ClinicalAttribute clinicalAttribute) {
        JSONObject map = new JSONObject();

        map.put("attr_id", clinicalAttribute.getAttrId());
        map.put("datatype", clinicalAttribute.getDatatype());
        map.put("description", clinicalAttribute.getDescription());
        map.put("display_name", clinicalAttribute.getDisplayName());

        return map;
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
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String query = request.getParameter("q");
        String type = request.getParameter("t");

        String samples = request.getParameter("samples");
        String cancerStudyId = request.getParameter("cancer_study_id");

        List<ClinicalData> clinicals;
        JSONArray maps = null;

        try {
            if (samples == null || samples.equals(ALL) ) {
                clinicals = DaoClinicalData.getData(cancerStudyId);
                maps = clinicals2JSONArray(clinicals);
            } else {
                clinicals = DaoClinicalData.getData(cancerStudyId,
                        Arrays.asList(samples.trim().split(SAMPLES_DELIMITER)));
                maps = clinicals2JSONArray(clinicals);
            }
        } catch (DaoException e) {
            throw new ServletException(e);
        }

        for (ClinicalData c : clinicals) {
            maps.add(reflectToMap(c));
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
