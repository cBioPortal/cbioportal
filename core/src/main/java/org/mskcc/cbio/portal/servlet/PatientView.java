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


import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.mskcc.cbio.portal.util.*;

import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.servlet.*;

/**
 *
 * @author jj
 */
public class PatientView extends HttpServlet {
    public static final String SAMPLE_ID = "sample_id";
    public static final String PATIENT_ID = "case_id";
    public static final String HAS_ALLELE_FREQUENCY_DATA = "has_allele_frequency_data";
    public static final String MUTATION_PROFILE = "mutation_profile";
    public static final String CNA_PROFILE = "cna_profile";
    public static final String MRNA_PROFILE = "mrna_profile";

    public static final String DRUG_TYPE = "drug_type";
    public static final String DRUG_TYPE_CANCER_DRUG = "cancer_drug";
    public static final String DRUG_TYPE_FDA_ONLY = "fda_approved";

    private static Logger logger = Logger.getLogger(PatientView.class);

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this,
                config.getServletContext());
    }
    
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        XDebug xdebug = new XDebug( request );
        request.setAttribute(QueryBuilder.XDEBUG_OBJECT, xdebug);

        // Convert old URL parameters cancer_study_id and case_id/sample_id to new frontend
        // hash style URL paramters #studyId=x&caseId=y
        String cancerStudyId = (String) request.getParameter("cancer_study_id");
        if (cancerStudyId != null) {
            String hashUrl = "#/patient?studyId=" + cancerStudyId;
            String caseId = (String) request.getParameter("case_id");
            String sampleId = (String) request.getParameter("sample_id");
            if (caseId != null) {
                hashUrl += "&caseId=" + caseId;
            } else if (sampleId != null) {
                hashUrl += "&sampleId=" + sampleId;
            } else {
               response.sendError(HttpServletResponse.SC_NOT_FOUND); 
               return;
            }
            
            response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
            String oldUrl = ((HttpServletRequest)request).getRequestURL().toString();
            response.setHeader("Location", oldUrl + hashUrl);
        } else {
            request.setAttribute(QueryBuilder.HTML_TITLE, "Patient");

            RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/WEB-INF/jsp/patient_view/patient_view.jsp");
            dispatcher.forward(request, response);
        }
    }

    private static String getFullURL(HttpServletRequest request) {
        StringBuffer requestURL = request.getRequestURL();
        String queryString = request.getQueryString();
    
        if (queryString == null) {
                return requestURL.toString();
        } else {
                return requestURL.append('?').append(queryString).toString();
        }
    }


    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
