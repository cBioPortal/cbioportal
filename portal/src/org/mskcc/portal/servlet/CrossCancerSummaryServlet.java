package org.mskcc.portal.servlet;


import org.mskcc.portal.model.CancerType;
import org.mskcc.portal.model.GeneticProfile;
import org.mskcc.portal.remote.GetCancerTypes;
import org.mskcc.portal.remote.GetGeneticProfiles;
import org.mskcc.portal.util.GlobalProperties;
import org.mskcc.portal.util.XDebug;
import org.owasp.validator.html.PolicyException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Central Servlet for Summarizing One Cancer in a Cross-Cancer Summary.
 *
 * @author Ethan Cerami.
 */
public class CrossCancerSummaryServlet extends HttpServlet {

    private ServletXssUtil servletXssUtil;

    /**
     * Initializes the servlet.
     *
     * @throws javax.servlet.ServletException Serlvet Init Error.
     */
    public void init() throws ServletException {
        super.init();
        String cgdsUrl = getInitParameter(QueryBuilder.CGDS_URL_PARAM);
        GlobalProperties.setCgdsUrl(cgdsUrl);
		String pathwayCommonsUrl = getInitParameter(QueryBuilder.PATHWAY_COMMONS_URL_PARAM);
        GlobalProperties.setPathwayCommonsUrl(pathwayCommonsUrl);
        try {
            servletXssUtil = ServletXssUtil.getInstance();
        } catch (PolicyException e) {
            throw new ServletException(e);
        }
    }

    /**
     * Handles HTTP GET Request.
     *
     * @param httpServletRequest  Http Servlet Request Object.
     * @param httpServletResponse Http Servelt Response Object.
     * @throws javax.servlet.ServletException Servlet Error.
     * @throws java.io.IOException            IO Error.
     */
    protected void doGet(HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) throws ServletException,
            IOException {
        doPost(httpServletRequest, httpServletResponse);
    }

    /**
     * Handles HTTP POST Request.
     *
     * @param httpServletRequest  Http Servlet Request Object.
     * @param httpServletResponse Http Servelt Response Object.
     * @throws javax.servlet.ServletException Servlet Error.
     * @throws java.io.IOException            IO Error.
     */
    protected void doPost(HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) throws ServletException,
            IOException {
        XDebug xdebug = new XDebug();
        xdebug.startTimer();

        try{
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        // In order to process request, we must have a gene list, and a cancer type
        String geneList = servletXssUtil.getCleanInput(QueryBuilder.GENE_LIST);
        String cancerStudyId = httpServletRequest.getParameter(QueryBuilder.CANCER_STUDY_ID);

        ArrayList<GeneticProfile> geneticProfileList = GetGeneticProfiles.getGeneticProfiles(cancerStudyId, xdebug);
        httpServletRequest.setAttribute(QueryBuilder.PROFILE_LIST_INTERNAL, geneticProfileList);
        RequestDispatcher dispatcher =
                getServletContext().getRequestDispatcher("/WEB-INF/jsp/cross_cancer_summary.jsp");
        dispatcher.forward(httpServletRequest, httpServletResponse);
    }
}