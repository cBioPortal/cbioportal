
package org.mskcc.portal.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.mskcc.cgds.dao.*;
import org.mskcc.cgds.model.ExtendedMutation;

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
            Map<String, List<ExtendedMutation>> similarMutations = getSimilarMutations(strMutations, patient);
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
    
    private Map<String, List<ExtendedMutation>> getSimilarMutations(String strMutations, String patient) throws DaoException {
        Map<String, List<ExtendedMutation>> mapSampleMutations = 
                new HashMap<String, List<ExtendedMutation>>();
        DaoMutation daoMutation = DaoMutation.getInstance();
        for (String strMutation : strMutations.split(",")) {
            String[] geneAA = strMutation.split(":");
            long entrez = DaoGeneOptimized.getInstance().getGene(geneAA[0]).getEntrezGeneId();
            List<ExtendedMutation> mutations;
            if (patient==null) {
                mutations = daoMutation.getMutations(entrez, geneAA[1]);
            } else {
                mutations = daoMutation.getSimilarMutations(entrez, geneAA[1], patient);
            }
            for (ExtendedMutation mutation : mutations) {
                String p = mutation.getCaseId();
                List<ExtendedMutation> list = mapSampleMutations.get(p);
                if (list == null) {
                    list = new ArrayList<ExtendedMutation>();
                    mapSampleMutations.put(p, list);
                }
                list.add(mutation);
            }
        }
        return mapSampleMutations;
    }
    
    private void export(JSONArray table, Map<String, List<ExtendedMutation>> similarMutations) 
            throws DaoException {
        
        for (Map.Entry<String, List<ExtendedMutation>> entry : similarMutations.entrySet()) {
            JSONArray row = new JSONArray();
            String simPatient = entry.getKey();
            row.add(simPatient);
            String cancerStudy = DaoCancerStudy.getCancerStudyByInternalId(
                    DaoCase.getCase(simPatient).getCancerStudyId()).getName();
            row.add(cancerStudy);
            List<ExtendedMutation> mutations = entry.getValue();
            row.add(formatMutation(mutations));
            row.add(mutations.size());
            table.add(row);
        }
    }
    
    private String formatMutation(List<ExtendedMutation> mutations) {
        StringBuilder sb = new StringBuilder();
        for (ExtendedMutation mut : mutations) {
            sb.append(mut.getGeneSymbol()).append(":").append(mut.getAminoAcidChange()).append(", ");
        }
        sb.delete(sb.length()-2, sb.length());
        return sb.toString();
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
