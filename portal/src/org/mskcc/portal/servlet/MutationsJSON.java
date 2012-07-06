
package org.mskcc.portal.servlet;

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
    
    public static final String CMD = "cmd";
    public static final String GET_CONTEXT_CMD = "get_context";
    public static final String MUTATION_EVENT_ID = "mutation_id";
    public static final String GENE_CONTEXT = "gene_context";
    public static final String MUTATION_CONTEXT = "mutation_context";
    
    private static final DaoGeneticProfile daoGeneticProfile = new DaoGeneticProfile();
    private static final DaoDrug daoDrug = new DaoDrug();
    
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String cmd = request.getParameter(CMD);
        if (cmd!=null && cmd.equalsIgnoreCase(GET_CONTEXT_CMD)) {
            processGetMutationContextRequest(request, response);
        } else {
            processGetMutationsRequest(request, response);
        }
    }
    
    private void processGetMutationsRequest(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        JSONArray table = new JSONArray();

        String patient = request.getParameter(PatientView.PATIENT_ID);
        String mutationProfileId = request.getParameter(PatientView.MUTATION_PROFILE);
        
        String mutSigQvalue = request.getParameter(MUT_SIG_QVALUE);
        double qvalue;
        try {
            qvalue = Double.parseDouble(mutSigQvalue);
        } catch (Exception e) {
            qvalue = DEFAULT_MUT_SIG_QVALUE_THRESHOLD;
        }
        
        
        GeneticProfile mutationProfile;
        Case _case;
        List<ExtendedMutation> mutations = Collections.emptyList();
        CancerStudy cancerStudy = null;
        
        try {
            _case = DaoCase.getCase(patient);
            mutationProfile = daoGeneticProfile.getGeneticProfileByStableId(mutationProfileId);
            if (_case!=null && mutationProfile!=null) {
                cancerStudy = DaoCancerStudy.getCancerStudyByInternalId(_case.getCancerStudyId());
                mutations = DaoMutationEvent.getMutationEvents(patient,
                        mutationProfile.getGeneticProfileId());
            }
        } catch (DaoException ex) {
            throw new ServletException(ex);
        }
        
        for (ExtendedMutation mutation : mutations) {
            export(table, mutation, cancerStudy, qvalue);
        }

        response.setContentType("application/json");
        
        PrintWriter out = response.getWriter();
        try {
            JSONValue.writeJSONString(table, out);
        } finally {            
            out.close();
        }
    }
    
    private void processGetMutationContextRequest(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        String mutationProfileId = request.getParameter(PatientView.MUTATION_PROFILE);
        String eventIds = request.getParameter(MUTATION_EVENT_ID);
        
        GeneticProfile mutationProfile;
        Map<String, Integer> geneContextMap = Collections.emptyMap();
        Map<Long, Integer> mutationContextMap = Collections.emptyMap();
        
        try {
            mutationProfile = daoGeneticProfile.getGeneticProfileByStableId(mutationProfileId);
            if (mutationProfile!=null) {
                geneContextMap = getGeneContextMap(eventIds, mutationProfile.getGeneticProfileId());
                mutationContextMap = DaoMutationEvent.countSamplesWithMutationEvents(
                        eventIds, mutationProfile.getGeneticProfileId());
            }
        } catch (DaoException ex) {
            throw new ServletException(ex);
        }
        
        Map<String, Map<?, Integer>> map = new HashMap<String, Map<?, Integer>>();
        map.put(GENE_CONTEXT, geneContextMap);
        map.put(MUTATION_CONTEXT, mutationContextMap);

        response.setContentType("application/json");
        
        PrintWriter out = response.getWriter();
        try {
            JSONValue.writeJSONString(map, out);
        } finally {            
            out.close();
        }
    }
    
    private Map<String, Integer> getGeneContextMap(String eventIds, int profileId)
            throws DaoException {
        Set<Long> genes = DaoMutationEvent.getGenesOfMutations(eventIds, profileId);
        Map<Long, Integer> map = DaoMutationEvent.countSamplesWithMutatedGenes(
                        genes, profileId);
        Map<String, Integer> ret = new HashMap<String, Integer>(map.size());
        for (Map.Entry<Long, Integer> entry : map.entrySet()) {
            ret.put(DaoGeneOptimized.getInstance().getGene(entry.getKey())
                    .getHugoGeneSymbolAllCaps(), entry.getValue());
        }
        return ret;
    }
    
    private void export(JSONArray table, ExtendedMutation mutation, CancerStudy 
            cancerStudy, double qvalueThreshold) 
            throws ServletException {
        JSONArray row = new JSONArray();
        row.add(mutation.getMutationEventId());
        String symbol = mutation.getGeneSymbol();
        row.add(symbol);
        row.add(mutation.getAminoAcidChange());
        row.add(mutation.getMutationType());
        row.add(mutation.getMutationStatus());
        // TODO: clinical trial
        List<Drug> drugs = null;
        try {
            drugs = daoDrug.getDrugs(mutation.getEntrezGeneId());
        } catch (DaoException ex) {
            throw new ServletException(ex);
        }
        row.add(getDrugInfo(drugs));
        // TODO: annotation
        row.add("pending");
        
        // mut sig
        double mutSigQvalue;
        try {
            mutSigQvalue = getMutSigQValue(cancerStudy.getInternalId(),
                    mutation.getGeneSymbol(), qvalueThreshold);
        } catch (DaoException ex) {
            throw new ServletException(ex);
        }
        row.add(mutSigQvalue);
        
        // show in summary table
        boolean isSangerGene = false;
        try {
            isSangerGene = DaoSangerCensus.getInstance().getCancerGeneSet().containsKey(symbol);
        } catch (DaoException ex) {
            throw new ServletException(ex);
        }
        row.add(isSangerGene || !Double.isNaN(mutSigQvalue));
        
        table.add(row);
    }
    
    private String getDrugInfo(List<Drug> drugs) {
        StringBuilder sb = new StringBuilder();
        for (Drug drug : drugs) {
            sb.append(drug.getdrugId()).append(", ");
        }
        sb.delete(sb.length()-2, sb.length());
        return sb.toString();
    }
    
    private static Map<Integer,Map<String,Double>> mutSigMap // map from cancer study id
            = new HashMap<Integer,Map<String,Double>>();     // to map from gene to Q-value
    
    private static double getMutSigQValue(int cancerStudyId, String gene,
            double qvalueThreshold) throws DaoException {
        Map<String,Double> mapGeneQvalue;
        synchronized(mutSigMap) {
            mapGeneQvalue = mutSigMap.get(cancerStudyId);
            if (mapGeneQvalue == null) {
                mapGeneQvalue = new HashMap<String,Double>();
                for (MutSig ms : DaoMutSig.getInstance().getAllMutSig(cancerStudyId,
                        qvalueThreshold)) {
                    double qvalue = ms.getqValue();
                    mapGeneQvalue.put(ms.getCanonicalGene().getHugoGeneSymbolAllCaps(),
                            qvalue);
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
