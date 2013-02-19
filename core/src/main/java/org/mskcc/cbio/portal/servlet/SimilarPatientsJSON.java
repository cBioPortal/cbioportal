
package org.mskcc.cbio.portal.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.mskcc.cbio.cgds.dao.*;
import org.mskcc.cbio.cgds.model.CancerStudy;
import org.mskcc.cbio.cgds.model.Case;

/**
 *
 * @author jj
 */
public class SimilarPatientsJSON extends HttpServlet {
    private static Logger logger = Logger.getLogger(SimilarPatientsJSON.class);
    
    public static final String MUTATION = "mutation";
    public static final String CNA = "cna";
    
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        JSONArray table = new JSONArray();

        String strMutations = request.getParameter(MUTATION);
        String strCna = request.getParameter(CNA);
        String patient = request.getParameter(PatientView.PATIENT_ID);
        String cancerStudyId = request.getParameter(QueryBuilder.CANCER_STUDY_ID);
        
        try {
            CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyId);
            if (cancerStudy!=null) {
                Case _case = DaoCase.getCase(patient, cancerStudy.getInternalId());
                if (_case!=null) {
                    Map<Case, Set<Long>> similarMutations;
                    if (strMutations==null||strMutations.isEmpty()) {
                        similarMutations = Collections.emptyMap();
                    } else {
                        similarMutations = DaoMutationEvent.getSimilarCasesWithMutationsByKeywords(strMutations);
                        similarMutations.remove(_case);
                    }
                    Map<Case, Set<Long>> similarCnas;
                    if (strCna==null||strCna.isEmpty()) {
                        similarCnas = Collections.emptyMap();
                    } else {
                        similarCnas = DaoCnaEvent.getCasesWithAlterations(strCna);
                        similarCnas.remove(_case);
                    }

                    export(table, similarMutations, similarCnas);
                }
            }
        } catch (DaoException ex) {
            throw new ServletException(ex);
        }
        
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        try {
            JSONValue.writeJSONString(table, out);
        } finally {            
            out.close();
        }
    }
    
//    private Set<Long> parseEventIds(String str) {
//        String[] parts = str.split(" ");
//        Set<Long> ret = new HashSet<Long>(parts.length);
//        for (String strMut : parts) {
//            try {
//                ret.add(Long.valueOf(strMut));
//            } catch (java.lang.NumberFormatException ex) {
//                logger.info(ex.getMessage());
//            }
//        }
//        return ret;
//    }
    
    private void export(JSONArray table, Map<Case, Set<Long>> similarMutations, Map<Case, Set<Long>> similarCnas) 
            throws DaoException {
        Set<Case> patients = new HashSet<Case>();
        patients.addAll(similarMutations.keySet());
        patients.addAll(similarCnas.keySet());
        for (Case patient : patients) {
            JSONArray row = new JSONArray();
            row.add(patient.getCaseId());
            
            String[] cancerStudy = {"unknown","unknown"};
            try {
                CancerStudy study = DaoCancerStudy.getCancerStudyByInternalId(
                    patient.getCancerStudyId());
                cancerStudy[0] = study.getCancerStudyStableId();
                cancerStudy[1] = study.getName();
            } catch (Exception e) {
                logger.error(e.getStackTrace());
            }

            row.add(Arrays.asList(cancerStudy));
            Map<String,Set<Long>> events = new HashMap<String,Set<Long>>(2);
            
            Set<Long> mutations = similarMutations.get(patient);
            if (mutations != null) {
                events.put(MUTATION, mutations);
            }
            
            Set<Long> cna = similarCnas.get(patient);
            if (cna != null) {
                events.put(CNA, cna);
            }
            
            row.add(events);
            table.add(row);
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
