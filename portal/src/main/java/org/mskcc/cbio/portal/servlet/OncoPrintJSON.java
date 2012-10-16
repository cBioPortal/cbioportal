package org.mskcc.cbio.portal.servlet;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.mskcc.cbio.cgds.dao.DaoCancerStudy;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.model.CancerStudy;
import org.mskcc.cbio.cgds.model.GeneticProfile;
import org.mskcc.cbio.portal.remote.GetGeneticProfiles;
import org.owasp.validator.html.PolicyException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;

public class OncoPrintJSON extends HttpServlet {
    private ServletXssUtil servletXssUtil;
    public static final String SELECTED_CANCER_STUDY = "selected_cancer_type";
    private static Log log = LogFactory.getLog(GisticJSON.class);

    /*
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
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String cancer_study_id = servletXssUtil
                .getCleanInput(request.getParameter("SELECTED_CANCER_STUDY"));

        try {
            CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancer_study_id);

            if (log.isDebugEnabled()) {
                log.debug("cancerStudyId: " + cancerStudy.getInternalId()) ;
            }

            JSONArray gisticJSONArray = new JSONArray();

            ArrayList<GeneticProfile> profileList = GetGeneticProfiles.getGeneticProfiles(cancer_study_id);

//                Map map =

            response.setContentType("application/json");
            PrintWriter out = response.getWriter();

            try {
//                JSONValue.writeJSONString(, out);
                JSONArray.writeJSONString(profileList, out);
            } finally {
                out.close();
            }
        } catch (DaoException e) {
            throw new ServletException(e);
        }
    }

    /**
     * Just in case the request changes from GET to POST
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        doGet(request, response);
    }
}
