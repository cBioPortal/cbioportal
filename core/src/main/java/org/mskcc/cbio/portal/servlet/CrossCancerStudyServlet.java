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

import org.mskcc.cbio.portal.util.*;
import org.owasp.validator.html.PolicyException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Central Servlet for performing Cross-Cancer Study Queries.
 *
 * @author Ethan Cerami.
 */
public class CrossCancerStudyServlet extends HttpServlet {

	// Set variables for gene list and error if gene list is empty.
    public static final String GENE_LIST = "gene_list";
    public static final String STEP4_ERROR_MSG = "step4_error_msg";
    private ServletXssUtil servletXssUtil;

    // Instanciate servletXssUtil
    public void init() throws ServletException {
        super.init();
        try {
            servletXssUtil = ServletXssUtil.getInstance();
        } catch (PolicyException e) {
            throw new ServletException (e);
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
            if (httpServletRequest.getRequestURL() != null) {
                httpServletRequest.setAttribute(QueryBuilder.ATTRIBUTE_URL_BEFORE_FORWARDING,
                    httpServletRequest.getRequestURL().toString());
            }

            httpServletRequest.setAttribute(QueryBuilder.CANCER_STUDY_ID,
                AccessControl.ALL_CANCER_STUDIES_ID);
            httpServletRequest.setAttribute(QueryBuilder.XDEBUG_OBJECT, xdebug);

            String action = httpServletRequest.getParameter(QueryBuilder.ACTION_NAME);

            // Validate if gene box is filled
            boolean errorsExist = false;

            //  Get User Defined Gene List
    	    String geneList = ((XssRequestWrapper)httpServletRequest).getRawParameter(GENE_LIST);
            geneList = servletXssUtil.getCleanInput(geneList);
            httpServletRequest.setAttribute(GENE_LIST, geneList);
            
            ArrayList<String> geneListArray = new ArrayList<>(Arrays.asList(geneList.split("( )|(\\n)")));
            
            if (geneListArray.size() == 1 && geneListArray.get(0).equals("")) {
            	httpServletRequest.setAttribute(STEP4_ERROR_MSG, "Please make a selection to query.");
        		errorsExist = true;
            }
                        
            if (action != null && action.equals(QueryBuilder.ACTION_SUBMIT) && (!errorsExist)) {
                dispatchToResultsJSP(httpServletRequest, httpServletResponse);
            } else {
            	if (errorsExist) {
                    httpServletRequest.setAttribute(QueryBuilder.USER_ERROR_MESSAGE, "Please fix the errors below.");
            	}
                dispatchToIndexJSP(httpServletRequest, httpServletResponse);
            }
        } catch (Exception e) {
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

}