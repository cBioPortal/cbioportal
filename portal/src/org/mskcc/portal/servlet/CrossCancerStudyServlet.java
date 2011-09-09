package org.mskcc.portal.servlet;

import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.model.CancerStudy;
import org.mskcc.cgds.model.CategorizedGeneticProfileSet;
import org.mskcc.cgds.model.GeneticProfile;
import org.mskcc.portal.remote.GetCancerTypes;
import org.mskcc.portal.remote.GetGeneticProfiles;
import org.mskcc.portal.util.GeneValidator;
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

            httpServletRequest.setAttribute(QueryBuilder.CANCER_STUDY_ID,
                    GetCancerTypes.ALL_CANCER_STUDIES_ID);
            httpServletRequest.setAttribute(QueryBuilder.CANCER_TYPES_INTERNAL, cancerStudyList);
            httpServletRequest.setAttribute(QueryBuilder.XDEBUG_OBJECT, xdebug);

            String action = servletXssUtil.getCleanInput(httpServletRequest,
                    QueryBuilder.ACTION_NAME);
            if (action != null && action.equals(QueryBuilder.ACTION_SUBMIT)) {
                boolean errorsExist = validateGenes(geneList, httpServletRequest);
                if (errorsExist) {
                    dispatchToIndexJSP(httpServletRequest, httpServletResponse);
                } else {
                    dispatchToResultsJSP(httpServletRequest, httpServletResponse);
                }
            } else {
                dispatchToIndexJSP(httpServletRequest, httpServletResponse);
            }
        } catch (DaoException e) {
            throw new ServletException(e);
        }
    }

    private boolean validateGenes(String geneList, HttpServletRequest httpServletRequest)
            throws DaoException {
        boolean errorsExist = false;
        if (geneList == null || geneList.trim().length() == 0) {
            httpServletRequest.setAttribute(QueryBuilder.STEP4_ERROR_MSG,
                    "Please enter at least one gene symbol below. ");
            errorsExist = true;
        }
        if (geneList != null && geneList.trim().length() > 0) {
            GeneValidator geneValidator = new GeneValidator(geneList);
            int numGenes = geneValidator.getValidGeneList().size();
            if (numGenes > QueryBuilder.MAX_NUM_GENES) {
                httpServletRequest.setAttribute(QueryBuilder.STEP4_ERROR_MSG,
                        "Please restrict your request to " + QueryBuilder.MAX_NUM_GENES
                                + " genes or less.");
                errorsExist = true;
            }

            //  Validate the incoming gene list
            ArrayList<String> invalidGeneList = geneValidator.getInvalidGeneList();
            if (invalidGeneList.size() > 0) {
                String errorMessage = extractInvalidGenes(invalidGeneList);
                httpServletRequest.setAttribute(QueryBuilder.STEP4_ERROR_MSG, errorMessage);
                errorsExist = true;
            }
        }
        return errorsExist;
    }

    private String extractInvalidGenes(ArrayList<String> invalidGeneList) {
        StringBuffer errorMessage = new StringBuffer
                ("Invalid or unrecognized gene(s):  ");
        for (int i = 0; i < invalidGeneList.size(); i++) {
            String invalidGeneId = invalidGeneList.get(i);
            errorMessage.append(invalidGeneId);
            if (i < invalidGeneList.size() - 1) {
                errorMessage.append(", ");
            } else {
                errorMessage.append(".");
            }
        }
        return errorMessage.toString();
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