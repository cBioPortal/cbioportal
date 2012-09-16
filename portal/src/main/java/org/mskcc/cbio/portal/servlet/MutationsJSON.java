
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
import org.mskcc.cbio.cgds.model.*;

/**
 *
 * @author jj
 */
public class MutationsJSON extends HttpServlet {
    private static Logger logger = Logger.getLogger(MutationsJSON.class);
    
    public static final String MUT_SIG_QVALUE = "mut_sig_qvalue";
    private static final double DEFAULT_MUT_SIG_QVALUE_THRESHOLD = 0.05;
    
    public static final String COSMIC_THRESHOLD = "cosmic_threshold";
    private static final int DEFAULT_COSMIC_THRESHOLD = 5;
    
    public static final String CMD = "cmd";
    public static final String GET_CONTEXT_CMD = "get_context";
    public static final String GET_DRUG_CMD = "get_drug";
    public static final String COUNT_MUTATIONS_CMD = "count_mutations";
    public static final String MUTATION_EVENT_ID = "mutation_id";
    public static final String GENE_CONTEXT = "gene_context";
    public static final String KEYWORD_CONTEXT = "keyword_context";
    public static final String MUTATION_CONTEXT = "mutation_context";
    
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
        String cmd = request.getParameter(CMD);
        if (cmd!=null) {
            if (cmd.equalsIgnoreCase(GET_CONTEXT_CMD)) {
                processGetMutationContextRequest(request, response);
                return;
            }
            
            if (cmd.equalsIgnoreCase(GET_DRUG_CMD)) {
                processGetDrugsRequest(request, response);
                return;
            }
            
            if (cmd.equalsIgnoreCase(COUNT_MUTATIONS_CMD)) {
                processCountMutationsRequest(request, response);
                return;
            }
        }
            
        processGetMutationsRequest(request, response);
    }
    
    private void processGetMutationsRequest(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        JSONArray table = new JSONArray();

        String patient = request.getParameter(PatientView.PATIENT_ID);
        String mutationProfileId = request.getParameter(PatientView.MUTATION_PROFILE);
        
        String mutSigQvalue = request.getParameter(MUT_SIG_QVALUE);
        double qvalueThrehold;
        try {
            qvalueThrehold = Double.parseDouble(mutSigQvalue);
        } catch (Exception e) {
            qvalueThrehold = DEFAULT_MUT_SIG_QVALUE_THRESHOLD;
        }
        
        String strCosmicThreshold = request.getParameter(COSMIC_THRESHOLD);
        int cosmicThreshold;
        try {
            cosmicThreshold = Integer.parseInt(strCosmicThreshold);
        } catch (Exception e) {
            cosmicThreshold = DEFAULT_COSMIC_THRESHOLD;
        }
        
        GeneticProfile mutationProfile;
        Case _case;
        List<ExtendedMutation> mutations = Collections.emptyList();
        CancerStudy cancerStudy = null;
        Map<Long, Map<String,Integer>> cosmic = Collections.emptyMap();
        Map<String, List<String>> drugs = Collections.emptyMap();
        
        try {
            _case = DaoCase.getCase(patient);
            mutationProfile = daoGeneticProfile.getGeneticProfileByStableId(mutationProfileId);
            if (_case!=null && mutationProfile!=null) {
                cancerStudy = DaoCancerStudy.getCancerStudyByInternalId(_case.getCancerStudyId());
                mutations = DaoMutationEvent.getMutationEvents(patient,
                        mutationProfile.getGeneticProfileId());
                cosmic = getCosmic(mutations);
                drugs = getDrugs(mutations, mutationProfile.getGeneticProfileId());
            }
        } catch (DaoException ex) {
            throw new ServletException(ex);
        }
        
        Map<String,List> data = initMap();
        for (ExtendedMutation mutation : mutations) {
            exportMutation(data, mutation, cancerStudy, qvalueThrehold, drugs.get(mutation.getGeneSymbol()),
                    cosmic.get(mutation.getMutationEventId()),cosmicThreshold);
        }

        response.setContentType("application/json");
        
        PrintWriter out = response.getWriter();
        try {
            JSONValue.writeJSONString(data, out);
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
        Map<String, Integer> keywordContextMap = Collections.emptyMap();
//        Map<Long, Integer> mutationContextMap = Collections.emptyMap();
        
        try {
            mutationProfile = daoGeneticProfile.getGeneticProfileByStableId(mutationProfileId);
            if (mutationProfile!=null) {
                geneContextMap = getGeneContextMap(eventIds, mutationProfile.getGeneticProfileId());
                keywordContextMap = getKeywordContextMap(eventIds, mutationProfile.getGeneticProfileId());
//                mutationContextMap = DaoMutationEvent.countSamplesWithMutationEvents(
//                        eventIds, mutationProfile.getGeneticProfileId());
            }
        } catch (DaoException ex) {
            throw new ServletException(ex);
        }
        
        Map<String, Map<?, Integer>> map = new HashMap<String, Map<?, Integer>>();
        map.put(GENE_CONTEXT, geneContextMap);
        map.put(KEYWORD_CONTEXT, keywordContextMap);
//        map.put(MUTATION_CONTEXT, mutationContextMap);

        response.setContentType("application/json");
        
        PrintWriter out = response.getWriter();
        try {
            JSONValue.writeJSONString(map, out);
        } finally {            
            out.close();
        }
    }
    
    private void processGetDrugsRequest(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        String mutationProfileId = request.getParameter(PatientView.MUTATION_PROFILE);
        String eventIds = request.getParameter(MUTATION_EVENT_ID);
        
        GeneticProfile mutationProfile;
        Map<String, List<String>> drugs = Collections.emptyMap();
        
        try {
            mutationProfile = daoGeneticProfile.getGeneticProfileByStableId(mutationProfileId);
            if (mutationProfile!=null) {
                drugs = getDrugs(eventIds, mutationProfile.getGeneticProfileId());
            }
        } catch (DaoException ex) {
            throw new ServletException(ex);
        }

        response.setContentType("application/json");
        
        PrintWriter out = response.getWriter();
        try {
            JSONValue.writeJSONString(drugs, out);
        } finally {            
            out.close();
        }
    }
    
    private void processCountMutationsRequest(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        String mutationProfileId = request.getParameter(PatientView.MUTATION_PROFILE);
        String strCaseIds = request.getParameter(QueryBuilder.CASE_IDS);
        List<String> caseIds = strCaseIds==null ? null : Arrays.asList(strCaseIds.split("[ ,]+"));
        
        GeneticProfile mutationProfile;
        Map<String, Integer> count = Collections.emptyMap();
        
        try {
            mutationProfile = daoGeneticProfile.getGeneticProfileByStableId(mutationProfileId);
            if (mutationProfile!=null) {
                count = DaoMutationEvent.countMutationEvents(mutationProfile.getGeneticProfileId(),caseIds);
            }
        } catch (DaoException ex) {
            throw new ServletException(ex);
        }

        response.setContentType("application/json");
        
        PrintWriter out = response.getWriter();
        try {
            JSONValue.writeJSONString(count, out);
        } finally {            
            out.close();
        }
    }
    
    private Map<String, List<String>> getDrugs(List<ExtendedMutation> mutations, int profileId)
            throws DaoException {
        if (mutations.isEmpty()) {
            return Collections.emptyMap();
        }
        StringBuilder sb = new StringBuilder();
        for (ExtendedMutation mut : mutations) {
            sb.append(mut.getMutationEventId()).append(',');
        }
        sb.deleteCharAt(sb.length()-1);
        return getDrugs(sb.toString(), profileId);
    }
    
    private Map<String, List<String>> getDrugs(String eventIds, int profileId)
            throws DaoException {
        Set<Long> genes = DaoMutationEvent.getGenesOfMutations(eventIds, profileId);
        Map<Long, List<String>> map = DaoDrugInteraction.getInstance().getDrugs(genes,true,false);
        Map<String, List<String>> ret = new HashMap<String, List<String>>(map.size());
        for (Map.Entry<Long, List<String>> entry : map.entrySet()) {
            String symbol = DaoGeneOptimized.getInstance().getGene(entry.getKey())
                    .getHugoGeneSymbolAllCaps();
            ret.put(symbol, entry.getValue());
        }
        return ret;
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
    
    private Map<String, Integer> getKeywordContextMap(String eventIds, int profileId)
            throws DaoException {
        Set<String> genes = DaoMutationEvent.getKeywordsOfMutations(eventIds, profileId);
        return DaoMutationEvent.countSamplesWithKeywords(genes, profileId);
    }
    
    private Map<String,List> initMap() {
        Map<String,List> map = new HashMap<String,List>();
        map.put("id", new ArrayList());
        map.put("key", new ArrayList());
        map.put("chr", new ArrayList());
        map.put("start", new ArrayList());
        map.put("end", new ArrayList());
        map.put("gene", new ArrayList());
        map.put("aa", new ArrayList());
        map.put("type", new ArrayList());
        map.put("status", new ArrayList());
        map.put("cosmic", new ArrayList());
        map.put("mutsig", new ArrayList());
        map.put("sanger", new ArrayList());
        map.put("drug", new ArrayList());
        map.put("ma", new ArrayList());
        map.put("overview", new ArrayList());
        return map;
    }
    
    private void exportMutation(Map<String,List> data, ExtendedMutation mutation, CancerStudy 
            cancerStudy, double qvalueThreshold, List<String> drugs, Map<String,Integer> cosmic, int cosmicThreshold) 
            throws ServletException {
        data.get("id").add(mutation.getMutationEventId());
        data.get("key").add(mutation.getKeyword());
        data.get("chr").add(mutation.getChr());
        data.get("start").add(mutation.getStartPosition());
        data.get("end").add(mutation.getEndPosition());
        String symbol = mutation.getGeneSymbol();
        data.get("gene").add(symbol);
        data.get("aa").add(mutation.getProteinChange());
        data.get("type").add(mutation.getMutationType());
        data.get("status").add(mutation.getMutationStatus());
        
        // cosmic
        data.get("cosmic").add(cosmic);
        
        // mut sig
        double mutSigQvalue;
        try {
            mutSigQvalue = getMutSigQValue(cancerStudy.getInternalId(),
                    mutation.getGeneSymbol(), qvalueThreshold);
        } catch (DaoException ex) {
            throw new ServletException(ex);
        }
        data.get("mutsig").add(mutSigQvalue);
        
        // sanger
        boolean isSangerGene = false;
        try {
            isSangerGene = DaoSangerCensus.getInstance().getCancerGeneSet().containsKey(symbol);
        } catch (DaoException ex) {
            throw new ServletException(ex);
        }
        data.get("sanger").add(isSangerGene);
        
        // drug
        data.get("drug").add(drugs);
        
        // mutation assessor
        Map<String,String> ma = new HashMap<String,String>();
        ma.put("score", mutation.getFunctionalImpactScore());
        ma.put("xvia", mutation.getLinkXVar());
        ma.put("pdb", mutation.getLinkPdb());
        ma.put("msa", mutation.getLinkMsa());
        data.get("ma").add(ma);
        
        // show in summary table
        boolean includeInSummary = isSangerGene
                 || !Double.isNaN(mutSigQvalue)
                 || passCosmicThreshold(cosmic,cosmicThreshold);
                 //|| (drugs!=null && !drugs.isEmpty());
        data.get("overview").add(includeInSummary);
    }
    
    private boolean passCosmicThreshold(Map<String,Integer> cosmic, int cosmicThreshold) {
        if (cosmic==null) {
            return false;
        }
        int n = 0;
        for (int count : cosmic.values()) {
            n += count;
            if (n >= cosmicThreshold) {
                return true;
            }
        }
        return false;
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
    
    /**
     * 
     * @param mutations
     * @return Map of event id to map of aa change to count
     * @throws DaoException 
     */
    private Map<Long, Map<String,Integer>> getCosmic(
            List<ExtendedMutation> mutations) throws DaoException {
        Set<Long> mutIds = new HashSet<Long>(mutations.size());
        for (ExtendedMutation mut : mutations) {
            mutIds.add(mut.getMutationEventId());
        }
        
        Map<Long, List<CosmicMutationFrequency>> map = 
                DaoMutationEvent.getCosmicMutationFrequency(mutIds);
        Map<Long, Map<String,Integer>> ret
                = new HashMap<Long, Map<String,Integer>>(map.size());
        for (Map.Entry<Long, List<CosmicMutationFrequency>> entry : map.entrySet()) {
            Long id = entry.getKey();
            Map<String,Integer> mapSI = new HashMap<String,Integer>();
            for (CosmicMutationFrequency cmf : entry.getValue()) {
                mapSI.put(cmf.getAminoAcidChange(), cmf.getFrequency());
            }
            ret.put(id, mapSI);
        }
        return ret;
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
