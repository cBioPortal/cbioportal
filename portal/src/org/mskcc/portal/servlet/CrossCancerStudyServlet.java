package org.mskcc.portal.servlet;

import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.model.CancerStudy;
import org.mskcc.cgds.model.CategorizedGeneticProfileSet;
import org.mskcc.cgds.model.GeneticProfile;
import org.mskcc.portal.remote.GetCancerTypes;
import org.mskcc.portal.remote.GetGeneticProfiles;
import org.mskcc.portal.validator.gene.GeneValidator;
import org.mskcc.portal.validator.gene.GeneValidationException;
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
 * Central Servlet for performing Cross-Cancer Study Queries.
 *
 * @author Ethan Cerami.
 */
public class CrossCancerStudyServlet extends HttpServlet {

    private ServletXssUtil servletXssUtil;

    /**
     * Initializes the servlet.
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
     */
    protected void doGet(HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) throws ServletException,
            IOException {
        doPost(httpServletRequest, httpServletResponse);
    }

    /**
     * Handles HTTP POST Request.
     */
    protected void doPost(HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) throws ServletException,
            IOException {
        XDebug xdebug = new XDebug();
        xdebug.startTimer();
        try {
            String geneList = servletXssUtil.getCleanInput(httpServletRequest,
                    QueryBuilder.GENE_LIST);
            ArrayList<CancerStudy> cancerStudyList = getCancerStudiesWithData();

            if (cancerStudyList.size() > 0) {
                httpServletRequest.setAttribute(QueryBuilder.CANCER_STUDY_ID,
                        cancerStudyList.get(0).getCancerStudyStableId());
            }
            httpServletRequest.setAttribute(QueryBuilder.CANCER_TYPES_INTERNAL, cancerStudyList);
            httpServletRequest.setAttribute(QueryBuilder.XDEBUG_OBJECT, xdebug);

            String action = servletXssUtil.getCleanInput(httpServletRequest,
                    QueryBuilder.ACTION_NAME);
            if (action != null && action.equals(QueryBuilder.ACTION_SUBMIT)) {
                GeneValidator geneValidator = new GeneValidator(geneList);
                dispatchToResultsJSP(httpServletRequest, httpServletResponse);
            } else {
                dispatchToIndexJSP(httpServletRequest, httpServletResponse);
            }
        } catch (GeneValidationException e) {
            httpServletRequest.setAttribute(QueryBuilder.STEP4_ERROR_MSG, e.getMessage());
            dispatchToIndexJSP(httpServletRequest, httpServletResponse);
        } catch (DaoException e) {
            throw new ServletException(e);
        }

    }

    private void dispatchToResultsJSP(HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) throws ServletException, IOException {
        RequestDispatcher dispatcher =
                getServletContext().getRequestDispatcher("/WEB-INF/jsp/cross_cancer_results.jsp");
        dispatcher.forward(httpServletRequest, httpServletResponse);
    }

    private void dispatchToIndexJSP(HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) throws ServletException, IOException {
        RequestDispatcher dispatcher =
                getServletContext().getRequestDispatcher("/WEB-INF/jsp/index.jsp");
        dispatcher.forward(httpServletRequest, httpServletResponse);
    }

    private ArrayList<CancerStudy> getCancerStudiesWithData() throws DaoException {
        ArrayList<CancerStudy> candidateCancerStudyList = GetCancerTypes.getCancerStudies();
        ArrayList<CancerStudy> finalCancerStudyList = new ArrayList<CancerStudy>();

        //  Only include cancer studies that have default CNA and/or default mutation
        for (CancerStudy currentCancerStudy : candidateCancerStudyList) {
            if (hasDefaultCnaOrMutationProfiles(currentCancerStudy)) {
                finalCancerStudyList.add(currentCancerStudy);
            }
        }
        return finalCancerStudyList;
    }

    private boolean hasDefaultCnaOrMutationProfiles(CancerStudy currentCancerStudy)
            throws DaoException {
        ArrayList<GeneticProfile> geneticProfileList = GetGeneticProfiles.getGeneticProfiles
                (currentCancerStudy.getCancerStudyStableId());
        CategorizedGeneticProfileSet categorizedSet =
                new CategorizedGeneticProfileSet(geneticProfileList);
        return categorizedSet.getNumDefaultMutationAndCopyNumberProfiles() > 0;
    }
}