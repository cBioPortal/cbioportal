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

import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.util.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.web_api.ProtocolException;
import org.mskcc.cbio.portal.util.GlobalProperties;

import org.json.simple.JSONValue;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @author jgao
 */
public class TumorMapServlet extends HttpServlet {
    // ref to our access control object
    private static AccessControl accessControl = null;
    
    /** 
     * Initializes the AccessControl member.
     */
    private static synchronized AccessControl getaccessControl() {
        if (accessControl==null) { 
            accessControl = SpringUtil.getAccessControl();
        }
            
        return accessControl;
    }
    
    public static final String CMD = "cmd";
    public static final String GET_STUDY_STATISTICS_CMD = "statistics";
    public static final String GET_STUDY_STATISTICS_TYPE = "type";
    

    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String cmd = request.getParameter(CMD);
        if (null!=cmd) {
            if (cmd.equalsIgnoreCase(GET_STUDY_STATISTICS_CMD)) {
                String type = request.getParameter(GET_STUDY_STATISTICS_TYPE);
                processRequestStatistics(request, response, type);
                return;
            }
        }
        
        processRequestDefault(request, response);
    }
    
    private void processRequestDefault(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute(QueryBuilder.HTML_TITLE, GlobalProperties.getTitle());
        
        RequestDispatcher dispatcher =
                getServletContext().getRequestDispatcher("/WEB-INF/jsp/tumormap/tumormap.jsp");
        dispatcher.forward(request, response);
    }
    
    private void processRequestStatistics(HttpServletRequest request, HttpServletResponse response, String type)
            throws ServletException, IOException {
        String cancerStudyIds = request.getParameter(QueryBuilder.CANCER_STUDY_ID);
        
         // Get the db version
         String dbPortalVersion = GlobalProperties.getDbVersion();
         String dbVersion = DaoInfo.getVersion();
         if (!dbPortalVersion.equals(dbVersion))
         {
             request.setAttribute(QueryBuilder.DB_ERROR, "Current DB Version: " + dbVersion + "<br/>" + "Portal DB Version: " + dbPortalVersion);
         }
        
        try {
            boolean includeMut = "mut".equalsIgnoreCase(type);
            boolean includeCna = "cna".equalsIgnoreCase(type);
            
            Map<String,Map<String,Object>> data = new HashMap<String,Map<String,Object>>();
            // get list of cancer studies
            AccessControl accessControl = getaccessControl();
            List<CancerStudy> cancerStudies;
            if (cancerStudyIds==null || cancerStudyIds.isEmpty()) {
                cancerStudies = accessControl.getCancerStudies();
            } else {
                cancerStudies = new ArrayList<CancerStudy>();
                for (String studyId : cancerStudyIds.split("[ ,]+")) {
                    CancerStudy study  = DaoCancerStudy.getCancerStudyByStableId(studyId);
                    if (study!=null && !accessControl.isAccessibleCancerStudy(studyId).isEmpty()) {
                        cancerStudies.add(study);
                    }
                }
            }
            for (CancerStudy cancerStudy : cancerStudies) {
                if (cancerStudy.getCancerStudyStableId().equalsIgnoreCase("all")) {
                    continue;
                }
                Map<String,Object> row = new HashMap<String,Object>();
                data.put(cancerStudy.getCancerStudyStableId(),row);
                
                if (!includeMut&&!includeMut) {
                    row.put("name",cancerStudy.getName());
                    String pmid = cancerStudy.getPmid();
                    if (pmid!=null) {
                        row.put("pmid",pmid);
                    }
                    String citation = cancerStudy.getCitation();
                    if (citation!=null) {
                        row.put("citation", citation);
                    }
                    row.put("cases", DaoPatient.getPatientsByCancerStudyId(cancerStudy.getInternalId()).size());
                }
                
                if (includeMut) {
                    GeneticProfile mutProfile = cancerStudy.getMutationProfile();
                    if (mutProfile==null) {
                        row.put("mut",0);
                    } else {
                        int mutEvents = DaoMutation.countMutationEvents(mutProfile.getGeneticProfileId());
                        int samplesWithMut = DaoSampleProfile.countSamplesInProfile(mutProfile.getGeneticProfileId());
                        row.put("mut",1.0*mutEvents/samplesWithMut);
                    }
                }
                
                if (includeCna) {
                    GeneticProfile cnaProfile = cancerStudy.getCopyNumberAlterationProfile(false);
                    if (cnaProfile==null) {
                        row.put("cna",0);
                    } else {
                        List<Integer> samples = DaoSampleProfile.getAllSampleIdsInProfile(cnaProfile.getGeneticProfileId());
                        Map<Integer,Double> fracs = DaoCopyNumberSegment.getCopyNumberActeredFraction(samples,
                                cnaProfile.getCancerStudyId(),GlobalProperties.getPatientViewGenomicOverviewCnaCutoff()[0]);
                        double aveFrac = 0;
                        for (double frac : fracs.values()) {
                            aveFrac += frac;
                        }
                        aveFrac /= samples.size();
                        row.put("cna",aveFrac);
                    }
                }
            }
            
            response.setContentType("application/json");

            PrintWriter out = response.getWriter();
            try {
                JSONValue.writeJSONString(data, out);
            } finally {            
                out.close();
            }
        } catch (DaoException ex) {
            throw new ServletException(ex);
        } catch (ProtocolException ex) {
            throw new ServletException(ex);
        }
        
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
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
     * Handles the HTTP
     * <code>POST</code> method.
     *
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
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
