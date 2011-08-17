package org.mskcc.portal.servlet;

import org.mskcc.portal.remote.GetCancerTypes;
import org.mskcc.portal.util.XDebug;
import org.mskcc.cgds.model.CancerStudy;
import org.mskcc.cgds.dao.DaoException;
import org.owasp.validator.html.PolicyException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Central Servlet for performing Cross-Cancer Study Queries.
 *
 * @author Ethan Cerami.
 */
public class CrossCancerStudyServlet extends HttpServlet {

    private ServletXssUtil servletXssUtil;

    /**
     * Initializes the servlet.
     *
     * @throws javax.servlet.ServletException Servlet Init Error.
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
     * Handles HTTP GET Request.
     *
     * @param httpServletRequest  Http Servlet Request Object.
     * @param httpServletResponse Http Servlet Response Object.
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
        try {
            String geneList = servletXssUtil.getCleanInput(httpServletRequest, QueryBuilder.GENE_LIST);
            ArrayList<CancerStudy> cancerStudyList = GetCancerTypes.getCancerStudies();

            httpServletRequest.setAttribute(QueryBuilder.CANCER_STUDY_ID,
                    cancerStudyList.get(0).getCancerStudyStableId());
            httpServletRequest.setAttribute(QueryBuilder.CANCER_TYPES_INTERNAL, cancerStudyList);
            httpServletRequest.setAttribute(QueryBuilder.XDEBUG_OBJECT, xdebug);

            boolean errorsExist = false;

            String action = servletXssUtil.getCleanInput(httpServletRequest, QueryBuilder.ACTION);
            if (action != null && action.equals(QueryBuilder.ACTION_SUBMIT)) {
                if (geneList == null || geneList.trim().length() == 0) {
                    httpServletRequest.setAttribute(QueryBuilder.STEP4_ERROR_MSG,
                            "Please enter at least one gene symbol below. ");
                    errorsExist = true;
                }
                if (geneList != null && geneList.trim().length() > 0) {
                    String geneSymbols[] = geneList.split("\\s");
                    int numGenes = 0;
                    for (String gene : geneSymbols) {
                        if (gene.trim().length() > 0) {
                            numGenes++;
                        }
                    }
                    if (numGenes > QueryBuilder.MAX_NUM_GENES) {
                        httpServletRequest.setAttribute(QueryBuilder.STEP4_ERROR_MSG,
                                "Please restrict your request to " + QueryBuilder.MAX_NUM_GENES
                                        + " genes or less.");
                        errorsExist = true;
                    }
                }
                if (errorsExist) {
                    RequestDispatcher dispatcher =
                            getServletContext().getRequestDispatcher("/WEB-INF/jsp/index.jsp");
                    dispatcher.forward(httpServletRequest, httpServletResponse);
                } else {
                    RequestDispatcher dispatcher =
                            getServletContext().getRequestDispatcher("/WEB-INF/jsp/cross_cancer_results.jsp");
                    dispatcher.forward(httpServletRequest, httpServletResponse);
                }
            } else {
                RequestDispatcher dispatcher =
                        getServletContext().getRequestDispatcher("/WEB-INF/jsp/index.jsp");
                dispatcher.forward(httpServletRequest, httpServletResponse);
            }
        } catch (DaoException e) {
            throw new ServletException (e);
        }
    }
}