
package org.mskcc.cbio.portal.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;

/**
 *
 * @author jj
 */
public class MutationsJSON extends HttpServlet {
    private static Logger logger = Logger.getLogger(MutationsJSON.class);
    
    public static final String CMD = "cmd";
    public static final String GET_CONTEXT_CMD = "get_context";
    public static final String GET_DRUG_CMD = "get_drug";
    public static final String COUNT_MUTATIONS_CMD = "count_mutations";
    public static final String GET_SMG_CMD = "get_smg";
    public static final String MUTATION_EVENT_ID = "mutation_id";
    public static final String GENE_CONTEXT = "gene_context";
    public static final String KEYWORD_CONTEXT = "keyword_context";
    public static final String MUTATION_CONTEXT = "mutation_context";
    
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
            
            if (cmd.equalsIgnoreCase(COUNT_MUTATIONS_CMD)) {
                processCountMutationsRequest(request, response);
                return;
            }
            
            if (cmd.equalsIgnoreCase(GET_SMG_CMD)) {
                processGetSmgRequest(request, response);
                return;
            }
        }
            
        processGetMutationsRequest(request, response);
    }
    
    private static int DEFAULT_THERSHOLD_NUM_SMGS = 100;
    private void processGetSmgRequest(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        String mutationProfileId = request.getParameter(PatientView.MUTATION_PROFILE);
        GeneticProfile mutationProfile;
        Map<Long, Double> mutsig = Collections.emptyMap();
        Map<Long, Integer> smgs = Collections.emptyMap();
        try {
            mutationProfile = DaoGeneticProfile.getGeneticProfileByStableId(mutationProfileId);
            if (mutationProfile!=null) {
                int profileId = mutationProfile.getGeneticProfileId();
                
                // get all recurrently mutation genes
                smgs = DaoMutation.getSMGs(profileId, null, 2, DEFAULT_THERSHOLD_NUM_SMGS);
                
                mutsig = getMutSig(mutationProfile.getCancerStudyId());
                if (!mutsig.isEmpty()) {
                    Set<Long> mutsigGenes = new HashSet<Long>(mutsig.keySet());
                    mutsigGenes.removeAll(smgs.keySet());
                    if (!mutsigGenes.isEmpty()) {
                        // append mutsig genes
                        smgs.putAll(DaoMutation.getSMGs(profileId, mutsigGenes, -1, -1));
                    }
                }
            }
        } catch (DaoException ex) {
            throw new ServletException(ex);
        }
        
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
        List<Map<String,Object>> data = new ArrayList<Map<String,Object>>();
        for (Map.Entry<Long, Integer> entry : smgs.entrySet()) {
            Map<String,Object> map = new HashMap<String,Object>();
            
            Long entrez = entry.getKey();
            CanonicalGene gene = daoGeneOptimized.getGene(entrez);
            
            String hugo = gene.getHugoGeneSymbolAllCaps();
            map.put("gene_symbol", hugo);
            
            String cytoband = gene.getCytoband();
            map.put("cytoband", cytoband);
            
            int length = gene.getLength();
            if (length>0) {
                map.put("length", length);
            }
            
            Integer count = entry.getValue();
            map.put("num_muts", count);
            
            Double qvalue = mutsig.get(entrez);
            if (qvalue!=null) {
                map.put("qval", qvalue);
            }
            
            data.add(map);
        }
        
        response.setContentType("application/json");
        
        PrintWriter out = response.getWriter();
        ObjectMapper mapper = new ObjectMapper();
        try {
            out.write(mapper.writeValueAsString(data));
        } finally {            
            out.close();
        }
    }
    
    private void processGetMutationsRequest(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        String[] patients = request.getParameter(PatientView.CASE_ID).split(" +");
        String mutationProfileId = request.getParameter(PatientView.MUTATION_PROFILE);
        String mrnaProfileId = request.getParameter(PatientView.MRNA_PROFILE);
        String drugType = request.getParameter(PatientView.DRUG_TYPE);
        boolean fdaOnly = false;
        boolean cancerDrug = true;
        if (drugType!=null && drugType.equalsIgnoreCase(PatientView.DRUG_TYPE_FDA_ONLY)) {
            fdaOnly = true;
            cancerDrug = false;
        }
        
        GeneticProfile mutationProfile;
        List<ExtendedMutation> mutations = Collections.emptyList();
        CancerStudy cancerStudy = null;
        Map<Long, Set<CosmicMutationFrequency>> cosmic = Collections.emptyMap();
        Map<Long, Set<String>> drugs = Collections.emptyMap();
        Map<String, Integer> geneContextMap = Collections.emptyMap();
        Map<String, Integer> keywordContextMap = Collections.emptyMap();
        DaoGeneOptimized daoGeneOptimized = null;
        Map<Long, Map<String,Object>> mrnaContext = Collections.emptyMap();
        
        try {
            mutationProfile = DaoGeneticProfile.getGeneticProfileByStableId(mutationProfileId);
            if (mutationProfile!=null) {
                cancerStudy = DaoCancerStudy.getCancerStudyByInternalId(mutationProfile.getCancerStudyId());
                mutations = DaoMutation.getMutations(mutationProfile.getGeneticProfileId(),patients);
                cosmic = DaoCosmicData.getCosmicForMutationEvents(mutations);
                String concatEventIds = getConcatEventIds(mutations);
                int profileId = mutationProfile.getGeneticProfileId();
                daoGeneOptimized = DaoGeneOptimized.getInstance();
                drugs = getDrugs(concatEventIds, profileId, fdaOnly, cancerDrug);
                geneContextMap = getGeneContextMap(concatEventIds, profileId, daoGeneOptimized);
                keywordContextMap = getKeywordContextMap(concatEventIds, profileId);
                if (mrnaProfileId!=null && patients.length==1) { // only if there is only one tumor
                    mrnaContext = getMrnaContext(patients[0], mutations, mrnaProfileId);
                }
            }
        } catch (DaoException ex) {
            throw new ServletException(ex);
        }
        
        Map<String,List> data = initMap();
        Map<Long, Integer> mapMutationEventIndex = new HashMap<Long, Integer>();
        for (ExtendedMutation mutation : mutations) {
            exportMutation(data, mapMutationEventIndex, mutation, cancerStudy,
                    drugs.get(mutation.getEntrezGeneId()), geneContextMap.get(mutation.getGeneSymbol()),
                    keywordContextMap.get(mutation.getKeyword()),
                    cosmic.get(mutation.getMutationEventId()),
                    mrnaContext.get(mutation.getEntrezGeneId()),
                    daoGeneOptimized);
        }

        response.setContentType("application/json");
        
        PrintWriter out = response.getWriter();
        ObjectMapper mapper = new ObjectMapper();
        try {
            out.write(mapper.writeValueAsString(data));
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
            mutationProfile = DaoGeneticProfile.getGeneticProfileByStableId(mutationProfileId);
            if (mutationProfile!=null) {
                DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
                geneContextMap = getGeneContextMap(eventIds, mutationProfile.getGeneticProfileId(), daoGeneOptimized);
                keywordContextMap = getKeywordContextMap(eventIds, mutationProfile.getGeneticProfileId());
//                mutationContextMap = DaoMutation.countSamplesWithMutationEvents(
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
        ObjectMapper mapper = new ObjectMapper();
        try {
            out.write(mapper.writeValueAsString(map));
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
            mutationProfile = DaoGeneticProfile.getGeneticProfileByStableId(mutationProfileId);
            if (mutationProfile!=null) {
                count = DaoMutation.countMutationEvents(mutationProfile.getGeneticProfileId(),caseIds);
            }
        } catch (DaoException ex) {
            throw new ServletException(ex);
        }

        response.setContentType("application/json");
        
        PrintWriter out = response.getWriter();
        ObjectMapper mapper = new ObjectMapper();
        try {
            out.write(mapper.writeValueAsString(count));
        } finally {            
            out.close();
        }
    }
    
    private String getConcatEventIds(List<ExtendedMutation> mutations) {
        if (mutations.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        for (ExtendedMutation mut : mutations) {
            sb.append(mut.getMutationEventId()).append(',');
        }
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }
    
    private Map<Long, Set<String>> getDrugs(String eventIds, int profileId, boolean fdaOnly,
            boolean cancerDrug)
            throws DaoException {
        DaoDrugInteraction daoDrugInteraction = DaoDrugInteraction.getInstance();
        Set<Long> genes = DaoMutation.getGenesOfMutations(eventIds, profileId);
        
        // Temporary way of handling cases such as akt inhibitor for pten loss
        Map<Long,Set<Long>> mapTargetToEventGenes = new HashMap<Long,Set<Long>>();
        Set<Long> moreTargets = new HashSet<Long>();
        for (long gene : genes) {
            Set<Long> targets = daoDrugInteraction.getMoreTargets(gene, "MUT");
            moreTargets.addAll(targets);
            for (Long target : targets) {
                Set<Long> eventGenes = mapTargetToEventGenes.get(target);
                if (eventGenes==null) {
                    eventGenes = new HashSet<Long>();
                    mapTargetToEventGenes.put(target, eventGenes);
                }
                eventGenes.add(gene);
            }
        }
        genes.addAll(moreTargets);
        // end Temporary way of handling cases such as akt inhibitor for pten loss
        
        Map<Long, List<String>> map = daoDrugInteraction.getDrugs(genes,fdaOnly,cancerDrug);
        Map<Long, Set<String>> ret = new HashMap<Long, Set<String>>(map.size());
        for (Map.Entry<Long, List<String>> entry : map.entrySet()) {
            ret.put(entry.getKey(), new HashSet<String>(entry.getValue()));
        }
        
        // Temporary way of handling cases such as akt inhibitor for pten loss
        for (Map.Entry<Long, List<String>> entry : map.entrySet()) {
            Set<Long> eventGenes = mapTargetToEventGenes.get(entry.getKey());
            if (eventGenes!=null) {
                for (long eventGene : eventGenes) {
                    Set<String> drugs = ret.get(eventGene);
                    if (drugs==null) {
                        drugs = new HashSet<String>();
                        ret.put(eventGene, drugs);
                    }
                    drugs.addAll(entry.getValue());
                }
            }
        }
        // end Temporary way of handling cases such as akt inhibitor for pten loss
        
        return ret;
    }
    
    private Map<Long, Map<String,Object>> getMrnaContext(String caseId, List<ExtendedMutation> mutations,
            String mrnaProfileId) throws DaoException {
        Map<Long, Map<String,Object>> mapGenePercentile = new HashMap<Long, Map<String,Object>>();
        DaoGeneticAlteration daoGeneticAlteration = DaoGeneticAlteration.getInstance();
        for (ExtendedMutation mutEvent : mutations) {
            long gene = mutEvent.getEntrezGeneId();
            if (mapGenePercentile.containsKey(gene)) {
                continue;
            }
            
            Map<String,String> mrnaMap = daoGeneticAlteration.getGeneticAlterationMap(
                    DaoGeneticProfile.getGeneticProfileByStableId(mrnaProfileId).getGeneticProfileId(),
                    gene);
            double mrnaCase = parseNumber(mrnaMap.get(caseId));
            if (Double.isNaN(mrnaCase)) {
                continue;
            }
            
            Map<String,Object> map = new HashMap<String,Object>();
            mapGenePercentile.put(gene, map);
            
            map.put("zscore", mrnaCase);
            
            int total = 0, below = 0;
            for (String strMrna : mrnaMap.values()) {
                double mrna = parseNumber(strMrna);
                if (Double.isNaN(mrna)) {
                    continue;
                }
                
                total++;
                if (mrna <= mrnaCase) {
                    below++;
                }
            }
            
            map.put("perc", 100*below/total);
        }
        
        return mapGenePercentile;
    }
    
    private double parseNumber(String mrna) {
        try {
            return Double.parseDouble(mrna);
        } catch (Exception e) {
            return Double.NaN;
        }
    }
    
    private Map<String, Integer> getGeneContextMap(String eventIds, int profileId, DaoGeneOptimized daoGeneOptimized)
            throws DaoException {
        Set<Long> genes = DaoMutation.getGenesOfMutations(eventIds, profileId);
        Map<Long, Integer> map = DaoMutation.countSamplesWithMutatedGenes(
                        genes, profileId);
        Map<String, Integer> ret = new HashMap<String, Integer>(map.size());
        for (Map.Entry<Long, Integer> entry : map.entrySet()) {
            ret.put(daoGeneOptimized.getGene(entry.getKey())
                    .getHugoGeneSymbolAllCaps(), entry.getValue());
        }
        return ret;
    }
    
    private Map<String, Integer> getKeywordContextMap(String eventIds, int profileId)
            throws DaoException {
        Set<String> genes = DaoMutation.getKeywordsOfMutations(eventIds, profileId);
        return DaoMutation.countSamplesWithKeywords(genes, profileId);
    }
    
    private Map<String,List> initMap() {
        Map<String,List> map = new HashMap<String,List>();
        map.put("id", new ArrayList());
        map.put("caseIds", new ArrayList());
        map.put("key", new ArrayList());
        map.put("chr", new ArrayList());
        map.put("start", new ArrayList());
        map.put("end", new ArrayList());
        map.put("entrez", new ArrayList());
        map.put("gene", new ArrayList());
        map.put("aa", new ArrayList());
        map.put("ref", new ArrayList());
        map.put("var", new ArrayList());
        map.put("type", new ArrayList());
        map.put("status", new ArrayList());
        map.put("cosmic", new ArrayList());
        map.put("mutsig", new ArrayList());
        map.put("genemutrate", new ArrayList());
        map.put("keymutrate", new ArrayList());
        map.put("mrna", new ArrayList());
        map.put("sanger", new ArrayList());
        map.put("cancer-gene", new ArrayList());
        map.put("drug", new ArrayList());
        map.put("ma", new ArrayList());
        map.put("alt-count", new ArrayList());
        map.put("ref-count", new ArrayList());
        map.put("normal-alt-count", new ArrayList());
        map.put("normal-ref-count", new ArrayList());
        map.put("validation", new ArrayList());
        
        return map;
    }
    
    private Map<String,Integer> addReadCountMap(Map<String,Integer> map, String caseId, int readCount) {
        if (readCount>=0) {
            map.put(caseId, readCount);
        }
        return map;
    }
    
    private void exportMutation(Map<String,List> data, Map<Long, Integer> mapMutationEventIndex,
            ExtendedMutation mutation, CancerStudy cancerStudy, Set<String> drugs,
            int geneContext, int keywordContext, Set<CosmicMutationFrequency> cosmic, Map<String,Object> mrna,
            DaoGeneOptimized daoGeneOptimized) throws ServletException {
        Long eventId = mutation.getMutationEventId();
        Integer ix = mapMutationEventIndex.get(eventId);
        if (ix!=null) { // multiple samples
            List.class.cast(data.get("caseIds").get(ix)).add(mutation.getCaseId());
            addReadCountMap(Map.class.cast(data.get("alt-count").get(ix)),mutation.getCaseId(), mutation.getTumorAltCount());
            addReadCountMap(Map.class.cast(data.get("ref-count").get(ix)),mutation.getCaseId(), mutation.getTumorRefCount());
            addReadCountMap(Map.class.cast(data.get("normal-alt-count").get(ix)),mutation.getCaseId(), mutation.getNormalAltCount());
            addReadCountMap(Map.class.cast(data.get("normal-ref-count").get(ix)),mutation.getCaseId(), mutation.getNormalRefCount());
            return;
        }
        
        mapMutationEventIndex.put(eventId, data.get("id").size());
        
        data.get("id").add(mutation.getMutationEventId());
        List<String> samples = new ArrayList<String>();
        samples.add(mutation.getCaseId());
        data.get("caseIds").add(samples);
        data.get("key").add(mutation.getKeyword());
        data.get("chr").add(mutation.getChr());
        data.get("start").add(mutation.getStartPosition());
        data.get("end").add(mutation.getEndPosition());
        String symbol = mutation.getGeneSymbol();
        data.get("entrez").add(mutation.getEntrezGeneId());
        data.get("gene").add(symbol);
        data.get("aa").add(mutation.getProteinChange());
        data.get("ref").add(mutation.getReferenceAllele());
        data.get("var").add(mutation.getTumorSeqAllele());
        data.get("type").add(mutation.getMutationType());
        data.get("status").add(mutation.getMutationStatus());
        data.get("alt-count").add(addReadCountMap(new HashMap<String,Integer>(),mutation.getCaseId(),mutation.getTumorAltCount()));
        data.get("ref-count").add(addReadCountMap(new HashMap<String,Integer>(),mutation.getCaseId(),mutation.getTumorRefCount()));
        data.get("normal-alt-count").add(addReadCountMap(new HashMap<String,Integer>(),mutation.getCaseId(),mutation.getNormalAltCount()));
        data.get("normal-ref-count").add(addReadCountMap(new HashMap<String,Integer>(),mutation.getCaseId(),mutation.getNormalRefCount()));
        data.get("validation").add(mutation.getValidationStatus());
        data.get("mrna").add(mrna);
        
        // cosmic
        data.get("cosmic").add(convertCosmicDataToMatrix(cosmic));
        
        // mut sig
        Double mutSigQvalue;
        try {
            mutSigQvalue = getMutSigQValue(cancerStudy.getInternalId(),
                    mutation.getEntrezGeneId());
        } catch (DaoException ex) {
            throw new ServletException(ex);
        }
        data.get("mutsig").add(mutSigQvalue);
        
        // context
        data.get("genemutrate").add(geneContext);
        data.get("keymutrate").add(keywordContext);
        
        // sanger & cbio cancer gene
        boolean isSangerGene = false;
        boolean isCbioCancerGene = false;
        try {
            isSangerGene = DaoSangerCensus.getInstance().getCancerGeneSet().containsKey(symbol);
            isCbioCancerGene = daoGeneOptimized.isCbioCancerGene(symbol);
        } catch (DaoException ex) {
            throw new ServletException(ex);
        }
        data.get("sanger").add(isSangerGene);
        data.get("cancer-gene").add(isCbioCancerGene);
        
        // drug
        data.get("drug").add(drugs);
        
        // mutation assessor
        Map<String,String> ma = new HashMap<String,String>();
        ma.put("score", mutation.getFunctionalImpactScore());
        ma.put("xvia", mutation.getLinkXVar());
        ma.put("pdb", mutation.getLinkPdb());
        ma.put("msa", mutation.getLinkMsa());
        data.get("ma").add(ma);
    }
    
    private List<List> convertCosmicDataToMatrix(Set<CosmicMutationFrequency> cosmic) {
        if (cosmic==null) {
            return null;
        }
        List<List> mat = new ArrayList(cosmic.size());
        for (CosmicMutationFrequency cmf : cosmic) {
            List l = new ArrayList(3);
            l.add(cmf.getId());
            l.add(cmf.getAminoAcidChange());
            l.add(cmf.getFrequency());
            mat.add(l);
        }
        return mat;
    }
    
    private static final Map<Integer,Map<Long,Double>> mutSigMap // map from cancer study id
            = new HashMap<Integer,Map<Long,Double>>();     // to map from gene to Q-value
    
    private static Double getMutSigQValue(int cancerStudyId, long entrez) throws DaoException {
        Map<Long,Double> mapGeneQvalue;
        synchronized(mutSigMap) {
            mapGeneQvalue = mutSigMap.get(cancerStudyId);
            if (mapGeneQvalue == null) {
                mapGeneQvalue = new HashMap<Long,Double>();
                mutSigMap.put(cancerStudyId, mapGeneQvalue);
                for (MutSig ms : DaoMutSig.getAllMutSig(cancerStudyId)) {
                    double qvalue = ms.getqValue();
                    mapGeneQvalue.put(ms.getCanonicalGene().getEntrezGeneId(), qvalue);
                }
            }
        }
        return mapGeneQvalue.get(entrez);
    }
    
    private static Map<Long,Double> getMutSig(int cancerStudyId) throws DaoException {
        Map<Long,Double> mapGeneQvalue;
        synchronized(mutSigMap) {
            mapGeneQvalue = mutSigMap.get(cancerStudyId);
            if (mapGeneQvalue == null) {
                mapGeneQvalue = new HashMap<Long,Double>();
                mutSigMap.put(cancerStudyId, mapGeneQvalue);
                for (MutSig ms : DaoMutSig.getAllMutSig(cancerStudyId)) {
                    double qvalue = ms.getqValue();
                    mapGeneQvalue.put(ms.getCanonicalGene().getEntrezGeneId(),
                            qvalue);
                }
            }
        }
        return mapGeneQvalue;
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
