
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

/**
 *
 * @author jj
 */
public class SimilarPatientsJSON extends HttpServlet {
    private static Logger logger = Logger.getLogger(SimilarPatientsJSON.class);
    
    public static final String MUTATION = "mutation";
    public static final String CNA = "cna";
    
    private static final DaoGeneticProfile daoGeneticProfile = new DaoGeneticProfile();
    
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
        
        try {
            Map<String, Set<Long>> similarMutations;
            if (strMutations==null||strMutations.isEmpty()) {
                similarMutations = Collections.emptyMap();
            } else {
                similarMutations = DaoMutationEvent.getCasesWithMutations(strMutations);
                similarMutations.remove(patient);
            }
            Map<String, Set<Long>> similarCnas;
            if (strCna==null||strCna.isEmpty()) {
                similarCnas = Collections.emptyMap();
            } else {
                similarCnas = DaoCnaEvent.getCasesWithAlterations(strCna);
                similarCnas.remove(patient);
            }
            
            export(table, similarMutations, similarCnas);
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
    
    private void export(JSONArray table, Map<String, Set<Long>> similarMutations, Map<String, Set<Long>> similarCnas) 
            throws DaoException {
        Set<String> patients = new HashSet<String>();
        patients.addAll(similarMutations.keySet());
        patients.addAll(similarCnas.keySet());
        for (String patient : patients) {
            JSONArray row = new JSONArray();
            row.add(patient);
            
            String cancerStudy = "unknown";
            try {
                cancerStudy = DaoCancerStudy.getCancerStudyByInternalId(
                    DaoCase.getCase(patient).getCancerStudyId()).getName();
            } catch (Exception e) {System.out.println(e);}
            
            row.add(cancerStudy);
            int nEvents = 0;
            Map<String,Set<Long>> events = new HashMap<String,Set<Long>>(2);
            
            Set<Long> mutations = similarMutations.get(patient);
            if (mutations != null) {
                nEvents += mutations.size();
                events.put(MUTATION, mutations);
            }
            
            Set<Long> cna = similarCnas.get(patient);
            if (cna != null) {
                nEvents += cna.size();
                events.put(CNA, cna);
            }
            
            row.add(events);
            row.add(nEvents);
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
