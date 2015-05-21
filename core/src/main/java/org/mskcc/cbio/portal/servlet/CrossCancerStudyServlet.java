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

import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.CategorizedGeneticProfileSet;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.util.*;
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
import java.util.HashMap;
import java.util.List;

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
		accessControl = SpringUtil.getAccessControl();
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
	    /*String[] cancerStudyIdList;
	    String cancerStudyIdListString = httpServletRequest.getParameter(QueryBuilder.CANCER_STUDY_LIST);*/

	        // we need the raw gene list
	        if (httpServletRequest instanceof XssRequestWrapper)
	        {
		        geneList = ((XssRequestWrapper)httpServletRequest).getRawParameter(
				        QueryBuilder.GENE_LIST);
			/*cancerStudyIdListString = ((XssRequestWrapper)httpServletRequest).getRawParameter(
					QueryBuilder.CANCER_STUDY_LIST);*/
	        }
		/*
		if (cancerStudyIdListString != null) {
			cancerStudyIdList = cancerStudyIdListString.split(",");
		} else {
			List<CancerStudy> cancerStudies = accessControl.getCancerStudies();
			cancerStudyIdList = new String[cancerStudies.size()];
			int i = 0;
			for (CancerStudy cs: cancerStudies) {
				cancerStudyIdList[i] = cs.getCancerStudyStableId();
				i += 1;
			}
		}

            ArrayList<CancerStudy> cancerStudyList = getCancerStudiesWithData(cancerStudyIdList);
	    //ArrayList<CancerStudy> cancerStudyList = getCancerStudiesWithData();
*/
            if (httpServletRequest.getRequestURL() != null) {
                httpServletRequest.setAttribute(QueryBuilder.ATTRIBUTE_URL_BEFORE_FORWARDING,
                        httpServletRequest.getRequestURL().toString());
            }
	    
            httpServletRequest.setAttribute(QueryBuilder.CANCER_STUDY_ID,
                    AccessControl.ALL_CANCER_STUDIES_ID);
	//	      AccessControl.MULTIPLE_CANCER_STUDIES_ID);
	    /*httpServletRequest.setAttribute(QueryBuilder.CANCER_TYPES_INTERNAL, cancerStudyList);*/
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
        }/* catch (ProtocolException e) {
            throw new ServletException(e);
		}*/
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
    /*
    private ArrayList<CancerStudy> getCancerStudiesWithData(String[] ids) throws DaoException, ProtocolException {
	    HashMap<String, Boolean> studyMap = new HashMap<>();
		for (String id : ids) {
			studyMap.put(id, Boolean.TRUE);
		}
		List<CancerStudy> candidateCancerStudyList = accessControl.getCancerStudies();
        ArrayList<CancerStudy> finalCancerStudyList = new ArrayList<CancerStudy>();

        //  Only include cancer studies that have default CNA and/or default mutation
        for (CancerStudy currentCancerStudy : candidateCancerStudyList) {
            if (hasDefaultCnaOrMutationProfiles(currentCancerStudy) && studyMap.containsKey(currentCancerStudy.getCancerStudyStableId())) {
	    //if (hasDefaultCnaOrMutationProfiles(currentCancerStudy)) {
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
    }*/
}