/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
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

import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.CategorizedGeneticProfileSet;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.util.AccessControl;
import org.mskcc.cbio.portal.util.XssRequestWrapper;
import org.mskcc.cbio.portal.web_api.GetGeneticProfiles;
import org.mskcc.cbio.portal.validate.gene.GeneValidator;
import org.mskcc.cbio.portal.validate.gene.GeneValidationException;
import org.mskcc.cbio.portal.util.XDebug;
import org.mskcc.cbio.portal.web_api.ProtocolException;
import org.owasp.validator.html.PolicyException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Central Servlet for performing Cross-Cancer Study Queries.
 *
 * @author Ethan Cerami.
 */
public class CrossCancerStudyServlet extends HttpServlet {
	// class which process access control to cancer studies
	private AccessControl accessControl;

    /**
     * Initializes the servlet.
     */
    public void init() throws ServletException {
        super.init();

		ApplicationContext context =
			new ClassPathXmlApplicationContext("classpath:applicationContext-security.xml");
		accessControl = (AccessControl)context.getBean("accessControl");
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
            String geneList = httpServletRequest.getParameter(QueryBuilder.GENE_LIST);

	        // we need the raw gene list
	        if (httpServletRequest instanceof XssRequestWrapper)
	        {
		        geneList = ((XssRequestWrapper)httpServletRequest).getRawParameter(
				        QueryBuilder.GENE_LIST);
	        }

            ArrayList<CancerStudy> cancerStudyList = getCancerStudiesWithData();

            if (httpServletRequest.getRequestURL() != null) {
                httpServletRequest.setAttribute(QueryBuilder.ATTRIBUTE_URL_BEFORE_FORWARDING,
                        httpServletRequest.getRequestURL().toString());
            }

            httpServletRequest.setAttribute(QueryBuilder.CANCER_STUDY_ID,
                    AccessControl.ALL_CANCER_STUDIES_ID);
            httpServletRequest.setAttribute(QueryBuilder.CANCER_TYPES_INTERNAL, cancerStudyList);
            httpServletRequest.setAttribute(QueryBuilder.XDEBUG_OBJECT, xdebug);

            String action = httpServletRequest.getParameter(QueryBuilder.ACTION_NAME);

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
        } catch (ProtocolException e) {
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

    private ArrayList<CancerStudy> getCancerStudiesWithData() throws DaoException, ProtocolException {
		List<CancerStudy> candidateCancerStudyList = accessControl.getCancerStudies();
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