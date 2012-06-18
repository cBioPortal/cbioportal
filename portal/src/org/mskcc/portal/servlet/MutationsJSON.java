
package org.mskcc.portal.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.mskcc.cgds.dao.*;
import org.mskcc.cgds.model.CancerStudy;
import org.mskcc.cgds.model.Case;
import org.mskcc.cgds.model.ExtendedMutation;
import org.mskcc.cgds.model.GeneticProfile;

/**
 *
 * @author jj
 */
public class MutationsJSON extends HttpServlet {
    private static Logger logger = Logger.getLogger(MutationsJSON.class);
    
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

        String patient = request.getParameter(PatientView.PATIENT_ID);
        String mutationProfileId = request.getParameter(PatientView.MUTATION_PROFILE);
        GeneticProfile mutationProfile;
        Case _case;
        List<ExtendedMutation> mutations = Collections.emptyList();
        CancerStudy cancerStudy = null;
        int numAllCases = 0;
        try {
            _case = DaoCase.getCase(patient);
            mutationProfile = daoGeneticProfile.getGeneticProfileByStableId(mutationProfileId);
            if (_case!=null && mutationProfile!=null) {
                cancerStudy = DaoCancerStudy.getCancerStudyByInternalId(_case.getCancerStudyId());
                mutations = DaoMutation.getInstance().getMutations(mutationProfile.getGeneticProfileId(),patient);
                numAllCases = DaoCase.countCases(cancerStudy.getInternalId());
            }
        } catch (DaoException ex) {
            throw new ServletException(ex);
        }
        
        PrintWriter out = response.getWriter();
        if (_case==null || mutations.isEmpty()) {
            try {
                out.print("No mutation data is available for "+patient);
            } finally {            
                out.close();
            }
            return;
        }
        
        HashMap<Long, Integer> mapGeneAlteredSamples = new HashMap<Long,Integer>();
        for (ExtendedMutation mutation : mutations) {
            export(table, mutation, cancerStudy, mutationProfile.getGeneticProfileId(),
                    mapGeneAlteredSamples, numAllCases);
        }

        response.setContentType("application/json");
        try {
            JSONValue.writeJSONString(table, out);
        } finally {            
            out.close();
        }
    }
    
    private void export(JSONArray table, ExtendedMutation mutation, CancerStudy cancerStudy,
            int profileId, HashMap<Long, Integer> mapGeneAlteredSamples, int numAllCases) 
            throws ServletException {
        JSONArray row = new JSONArray();
        row.add(mutation.getGeneSymbol());
        row.add(mutation.getAminoAcidChange());
        row.add(mutation.getMutationType());
        row.add(mutation.getMutationStatus());
        // TODO: clinical trial
        row.add("pending");
        // TODO: context
        String context = getContext(mutation, cancerStudy, profileId, mapGeneAlteredSamples, numAllCases);
        row.add(context);
        // TODO: annotation
        row.add("pending");
        table.add(row);
    }
    
    private String getContext(ExtendedMutation mutation, CancerStudy cancerStudy, int profileId, 
            HashMap<Long, Integer> mapGeneAlteredSamples, int numAllCases) throws ServletException {
        StringBuilder sb = new StringBuilder();
        int numGeneMutated = countMutatedSamples(mutation.getEntrezGeneId(), null,
                profileId, mapGeneAlteredSamples);
        String percGeneMutated = String.format("%.1f%%", 100.0*numGeneMutated/numAllCases);
        int numAAChange = countMutatedSamples(mutation.getEntrezGeneId(), mutation.getAminoAcidChange(),
                profileId, null);
        String percAAChange = String.format("%.1f%%", 100.0*numAAChange/numAllCases);
        sb.append("Out of ").append(numAllCases).append(" cases in ")
                .append(cancerStudy.getName()).append(", <br/>").append(numGeneMutated)
                .append(" (").append(percGeneMutated).append(") ").append(numGeneMutated>1?"are":"is")
                .append(" mutated in gene ").append(mutation.getGeneSymbol())
                .append(",<br/> and ").append(numAAChange).append(" (").append(percAAChange)
                .append(") ").append(numAAChange>1?"carry ":"carries the ")
                .append(mutation.getAminoAcidChange()).append(" mutation");
        return sb.toString();
    }
    
    private int countMutatedSamples(long entrez, String aminoAcidChange, int profileId,
            HashMap<Long, Integer> mapGeneAlteredSamples) throws ServletException {
        Integer numI;
        if (mapGeneAlteredSamples!=null && (numI=mapGeneAlteredSamples.get(entrez))!=null) {
            return numI;
        }
        
        int num;
        try {
            num = DaoMutation.getInstance()
                .countSamplesWithSpecificMutations(profileId, entrez, aminoAcidChange);
        } catch(DaoException ex) {
            throw new ServletException(ex);
        }
        
        if (mapGeneAlteredSamples!=null) {
            mapGeneAlteredSamples.put(entrez, num);
        }
        
        return num;
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
