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
import org.mskcc.cbio.cgds.dao.DaoClinical;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.model.Clinical;
import org.mskcc.cbio.cgds.model.ClinicalAttribute;
import org.owasp.validator.html.PolicyException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClinicalJSON extends HttpServlet {
    private ServletXssUtil servletXssUtil;

    // our logger
    private static Log LOG = LogFactory.getLog(ClinicalJSON.class);

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
    public Map<String, String> reflectToMap(Clinical clinical) {
        Map<String, String> map = new HashMap<String, String>();

        map.put("attr_id", clinical.getAttrId());
        map.put("attr_val", clinical.getAttrVal());
        map.put("cancer_study_id", Integer.toString(clinical.getCancerStudyId()));
        map.put("case_id", clinical.getCaseId());

        return map;
    }

    public Map<String, String> reflectToMap(ClinicalAttribute clinicalAttribute) {
        Map<String, String> map = new HashMap<String, String>();

        map.put("attr_id", clinicalAttribute.getAttrId());
        map.put("datatype", clinicalAttribute.getDatatype());
        map.put("description", clinicalAttribute.getDescription());
        map.put("display_name", clinicalAttribute.getDisplayName());

        return map;
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

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        String clinical_attributes = request.getParameter("clinical_attributes");
        // list of clinical attributes separated by a space

        String samples = request.getParameter("samples");
        // list of samples separated by a space

        String cancer_study_id = request.getParameter("cancer_study_id");
        // stable id

        if (cancer_study_id != null) {
            // get by clinical id
            if (clinical_attributes == null) {
                // get all clinical attributes
            }
            else {
                // get by clinical attributes
            }
        }
        else {
            if (samples == null) {
                throw new ServletException("no samples requested");
            }
            String[] samplesList = samples.split(" ");

            try {
                DaoClinical.getBySampleId("");
            } catch (DaoException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

//        } catch (SQLException e) {
//            log.info("failed to extract result set into clinical: " + rs);
//            return null;
//        }
        }
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
