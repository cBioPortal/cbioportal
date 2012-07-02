
package org.mskcc.portal.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
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
import org.mskcc.cgds.model.*;

/**
 *
 * @author jj
 */
public class MutationsJSON extends HttpServlet {
    private static Logger logger = Logger.getLogger(MutationsJSON.class);
    
    public static final String MUT_SIG_QVALUE = "mut_sig_qvalue";
    private static final double DEFAULT_MUT_SIG_QVALUE_THRESHOLD = 0.05;
    
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
        
        String mutSigQvalue = request.getParameter(MUT_SIG_QVALUE);
        double qvalue = DEFAULT_MUT_SIG_QVALUE_THRESHOLD;
        try {
            qvalue = Double.parseDouble(mutSigQvalue);
        } catch (Exception e) {
            qvalue = DEFAULT_MUT_SIG_QVALUE_THRESHOLD;
        }
        
        
        GeneticProfile mutationProfile;
        Case _case;
        List<ExtendedMutation> mutations = Collections.emptyList();
        CancerStudy cancerStudy = null;
        
        String strNumAllCases = request.getParameter(PatientView.NUM_CASES_IN_SAME_STUDY);
        int numAllCases = strNumAllCases==null ? 0 : Integer.parseInt(strNumAllCases);
        try {
            _case = DaoCase.getCase(patient);
            mutationProfile = daoGeneticProfile.getGeneticProfileByStableId(mutationProfileId);
            if (_case!=null && mutationProfile!=null) {
                cancerStudy = DaoCancerStudy.getCancerStudyByInternalId(_case.getCancerStudyId());
                mutations = DaoMutation.getInstance().getMutations(mutationProfile.getGeneticProfileId(),patient);
                if (strNumAllCases==null) {
                    numAllCases = DaoCase.countCases(cancerStudy.getInternalId());
                }
            }
        } catch (DaoException ex) {
            throw new ServletException(ex);
        }
        
        HashMap<Long, Integer> mapGeneAlteredSamples = new HashMap<Long,Integer>();
        for (ExtendedMutation mutation : mutations) {
            export(table, mutation, cancerStudy, mutationProfile.getGeneticProfileId(),
                    mapGeneAlteredSamples, numAllCases, qvalue);
        }

        response.setContentType("application/json");
        
        PrintWriter out = response.getWriter();
        try {
            JSONValue.writeJSONString(table, out);
        } finally {            
            out.close();
        }
    }
    
    private void export(JSONArray table, ExtendedMutation mutation, CancerStudy cancerStudy,
            int profileId, HashMap<Long, Integer> mapGeneAlteredSamples, int numAllCases, double qvalueThreshold) 
            throws ServletException {
        JSONArray row = new JSONArray();
        row.add(mutation.getGeneSymbol());
        row.add(mutation.getAminoAcidChange());
        row.add(mutation.getMutationType());
        row.add(mutation.getMutationStatus());
        // TODO: context
        String context = getContext(mutation, cancerStudy, profileId, mapGeneAlteredSamples, numAllCases);
        row.add(context);
        // TODO: clinical trial
        row.add("pending");
        // TODO: annotation
        row.add("pending");
        
        // mut sig
        double mutSigQvalue;
        try {
            mutSigQvalue = getMutSigQValue(cancerStudy.getInternalId(), mutation.getGeneSymbol(), qvalueThreshold);
        } catch (DaoException ex) {
            throw new ServletException(ex);
        }
        row.add(mutSigQvalue);
        
        // show in summary table
        row.add(!Double.isNaN(mutSigQvalue));
        
        table.add(row);
    }
    
    private String getContext(ExtendedMutation mutation, CancerStudy cancerStudy, int profileId, 
            HashMap<Long, Integer> mapGeneAlteredSamples, int numAllCases) throws ServletException {
        StringBuilder sb = new StringBuilder();
        int numGeneMutated = countMutatedSamples(mutation.getEntrezGeneId(), null,
                profileId, mapGeneAlteredSamples);
        String percGeneMutated = String.format("<b>%.1f%%</b>", 100.0*numGeneMutated/numAllCases);
        int numAAChange = countMutatedSamples(mutation.getEntrezGeneId(), mutation.getAminoAcidChange(),
                profileId, null);
        String percAAChange = String.format("<b>%.1f%%</b>", 100.0*numAAChange/numAllCases);
        sb.append(mutation.getGeneSymbol()).append(": ").append(numGeneMutated)
                .append(" (").append(percGeneMutated).append(")<br/>")
                .append(mutation.getAminoAcidChange()).append(": ")
                .append(numAAChange).append(" (").append(percAAChange)
                .append(") ");
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
    
    private static Map<Integer,Map<String,Double>> mutSigMap            // map from cancer study id
            = mutSigMap = new HashMap<Integer,Map<String,Double>>();    // to map from gene to Q-value
    
    private static double getMutSigQValue(int cancerStudyId, String gene, double qvalueThreshold) throws DaoException {
        Map<String,Double> mapGeneQvalue;
        synchronized(mutSigMap) {
            mapGeneQvalue = mutSigMap.get(cancerStudyId);
            if (mapGeneQvalue == null) {
                mapGeneQvalue = new HashMap<String,Double>();
                for (MutSig ms : DaoMutSig.getInstance().getAllMutSig(cancerStudyId, qvalueThreshold)) {
                    double qvalue = ms.getqValue();
                    mapGeneQvalue.put(ms.getCanonicalGene().getHugoGeneSymbolAllCaps(), qvalue);
                }
            }
        }
        Double qvalue = mapGeneQvalue.get(gene);
        return qvalue!=null ? qvalue : Double.NaN;
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
