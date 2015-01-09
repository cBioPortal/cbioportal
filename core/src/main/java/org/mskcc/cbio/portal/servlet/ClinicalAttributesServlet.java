/*
 * Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.mskcc.cbio.portal.dao.DaoClinicalAttribute;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.model.ClinicalAttribute;
import org.mskcc.cbio.portal.util.WebserviceParserUtils;
import org.mskcc.cbio.portal.web_api.ProtocolException;
import org.owasp.validator.html.PolicyException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;

public class ClinicalAttributesServlet extends HttpServlet {
    private static Log log = LogFactory.getLog(ClinicalAttributesServlet.class);

    /**
     * Initializes the servlet.
     *
     * @throws ServletException
     */
    public void init() throws ServletException {
        super.init();
    }

    /**
     * Takes any sort of patient list parameter in the request
     *
     * Returns a list of clinical attributes in json format
     *
     * @param request
     * @param response
     * @throws ServletException
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        try {
            JSONArray toWrite = new JSONArray();
            response.setContentType("text/json");

            int cancerStudyId = DaoCancerStudy.getCancerStudyByStableId(WebserviceParserUtils.getCancerStudyId(request)).getInternalId();
            List<ClinicalAttribute> clinicalAttributes = DaoClinicalAttribute.getDataByStudy(cancerStudyId);

            for (ClinicalAttribute attr : clinicalAttributes) {
                toWrite.add(ClinicalJSON.reflectToMap(attr));
            }
            PrintWriter out = response.getWriter();
            JSONArray.writeJSONString(toWrite, out);
        } catch (DaoException e) {
            throw new ServletException(e);
        } catch (IOException e) {
            throw new ServletException(e);
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        doGet(request, response);
    }
}