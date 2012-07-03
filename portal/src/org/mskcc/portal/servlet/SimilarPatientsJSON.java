
package org.mskcc.portal.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.mskcc.cgds.dao.*;

/**
 *
 * @author jj
 */
public class SimilarPatientsJSON extends HttpServlet {
    private static Logger logger = Logger.getLogger(SimilarPatientsJSON.class);
    
    public static final String MUTATIONS = "mutations";
    
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

        String strMutations = request.getParameter(MUTATIONS);
        String patient = request.getParameter(PatientView.PATIENT_ID);
        
        try {
            Map<String, Set<Long>> similarMutations = DaoMutationEvent.getCasesWithMutations(parseMutationEventIds(strMutations));
            similarMutations.remove(patient);
            export(table, similarMutations);
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
    
    private Set<Long> parseMutationEventIds(String strMutations) {
        String[] parts = strMutations.split(" ");
        Set<Long> ret = new HashSet<Long>(parts.length);
        for (String strMut : parts) {
            try {
                ret.add(Long.valueOf(strMut));
            } catch (java.lang.NumberFormatException ex) {
                logger.info(ex.getMessage());
            }
        }
        return ret;
    }
    
    private void export(JSONArray table, Map<String, Set<Long>> similarMutations) 
            throws DaoException {
        
        for (Map.Entry<String, Set<Long>> entry : similarMutations.entrySet()) {
            JSONArray row = new JSONArray();
            String simPatient = entry.getKey();
            row.add(simPatient);
            String cancerStudy = DaoCancerStudy.getCancerStudyByInternalId(
                    DaoCase.getCase(simPatient).getCancerStudyId()).getName();
            row.add(cancerStudy);
            Set<Long> mutations = entry.getValue();
            row.add(formatMutation(mutations));
            row.add(mutations.size());
            table.add(row);
        }
    }
    
    private String formatMutation(Set<Long> mutations) {
        return ""+mutations.size();
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
