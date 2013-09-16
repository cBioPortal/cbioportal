
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
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.CnaEvent;
import org.mskcc.cbio.portal.model.CopyNumberSegment;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.model.Gistic;
import org.mskcc.cbio.portal.util.GlobalProperties;

/**
 *
 * @author jj
 */
public class CnaJSON extends HttpServlet {
    private static Logger logger = Logger.getLogger(CnaJSON.class);
    
    public static final String CMD = "cmd";
    public static final String GET_DRUG_CMD = "get_drug";
    public static final String GET_SEGMENT_CMD = "get_segment";
    public static final String GET_CNA_FRACTION_CMD = "get_cna_fraction";
    public static final String CNA_EVENT_ID = "cna_id";
    
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
            if (cmd.equalsIgnoreCase(GET_SEGMENT_CMD)) {
                processGetSegmentsRequest(request, response);
                return;
            }
            
            if (cmd.equalsIgnoreCase(GET_CNA_FRACTION_CMD)) {
                processCnaFractionsRequest(request, response);
                return;
            }
        }
            
        processGetCnaRequest(request, response);
    }
    
    private void processGetCnaRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String[] caseIds = request.getParameter(PatientView.CASE_ID).split(" +");
        String cnaProfileId = request.getParameter(PatientView.CNA_PROFILE);
        String mrnaProfileId = request.getParameter(PatientView.MRNA_PROFILE);
        String drugType = request.getParameter(PatientView.DRUG_TYPE);
        boolean fdaOnly = false;
        boolean cancerDrug = true;
        if (drugType!=null && drugType.equalsIgnoreCase(PatientView.DRUG_TYPE_FDA_ONLY)) {
            fdaOnly = true;
            cancerDrug = false;
        }
                
        GeneticProfile cnaProfile;
        CancerStudy cancerStudy = null;
        List<CnaEvent> cnaEvents = Collections.emptyList();
        Map<Long, Set<String>> drugs = Collections.emptyMap();
        Map<Long, Integer>  contextMap = Collections.emptyMap();
        Map<Long, Map<String,Object>> mrnaContext = Collections.emptyMap();

        try {
            cnaProfile = DaoGeneticProfile.getGeneticProfileByStableId(cnaProfileId);
            cancerStudy = DaoCancerStudy.getCancerStudyByInternalId(cnaProfile.getCancerStudyId());
            cnaEvents = DaoCnaEvent.getCnaEvents(caseIds, cnaProfile.getGeneticProfileId());
            String concatEventIds = getConcatEventIds(cnaEvents);
            int profileId = cnaProfile.getGeneticProfileId();
            drugs = getDrugs(cnaEvents, fdaOnly, cancerDrug);
            contextMap = DaoCnaEvent.countSamplesWithCnaEvents(concatEventIds, profileId);
            if (mrnaProfileId!=null && caseIds.length==1) {
                mrnaContext = getMrnaContext(caseIds[0], cnaEvents, mrnaProfileId);
            }
        } catch (DaoException ex) {
            throw new ServletException(ex);
        }
        
        Map<String,List> data = initMap();
        Map<Long, Integer> mapEventIndex = new HashMap<Long, Integer>();
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
        for (CnaEvent cnaEvent : cnaEvents) {
            Set<String> drug = Collections.emptySet();
            try {
                drug = drugs.get(cnaEvent.getEntrezGeneId());
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
            exportCnaEvent(data, mapEventIndex, cnaEvent, cancerStudy, drug,
                    contextMap.get(cnaEvent.getEventId()),
                    mrnaContext.get(cnaEvent.getEntrezGeneId()),
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
    
    private void processGetSegmentsRequest(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String[] caseIds = request.getParameter(PatientView.CASE_ID).split(" +");
        String cancerStudyId = request.getParameter(QueryBuilder.CANCER_STUDY_ID);
        
        List<CopyNumberSegment> segs = Collections.emptyList();
        
        try {
            int studyId = DaoCancerStudy.getCancerStudyByStableId(cancerStudyId).getInternalId();
            segs = DaoCopyNumberSegment.getSegmentForCases(Arrays.asList(caseIds), studyId);
        } catch (DaoException ex) {
            throw new ServletException(ex);
        }
        
        Map<String,List> map = new HashMap<String,List>();
        for (String caseId : caseIds) {
            map.put(caseId, new ArrayList());
        }
        
        for (CopyNumberSegment seg : segs) {
            exportCopyNumberSegment(map.get(seg.getCaseId()), seg);
        }

        response.setContentType("application/json");
        
        PrintWriter out = response.getWriter();
        ObjectMapper mapper = new ObjectMapper();
        try {
            out.write(mapper.writeValueAsString(map));
        } finally {            
            out.close();
        }
    }
    
    private void processCnaFractionsRequest(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        String strCaseIds = request.getParameter(QueryBuilder.CASE_IDS);
        List<String> caseIds = strCaseIds==null ? null : Arrays.asList(strCaseIds.split("[ ,]+"));
        String cancerStudyId = request.getParameter(QueryBuilder.CANCER_STUDY_ID);
        
        Map<String, Double> fraction = Collections.emptyMap();
        
        try {
            int studyId = DaoCancerStudy.getCancerStudyByStableId(cancerStudyId).getInternalId();
            fraction = DaoCopyNumberSegment.getCopyNumberActeredFraction(caseIds, studyId,
                    GlobalProperties.getPatientViewGenomicOverviewCnaCutoff()[0]);
        } catch (DaoException ex) {
            throw new ServletException(ex);
        }

        response.setContentType("application/json");
        
        PrintWriter out = response.getWriter();
        ObjectMapper mapper = new ObjectMapper();
        try {
            out.write(mapper.writeValueAsString(fraction));
        } finally {            
            out.close();
        }
    }
    
    private String getConcatEventIds(List<CnaEvent> cnaEvents) {
        if (cnaEvents.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        for (CnaEvent cna : cnaEvents) {
            sb.append(cna.getEventId()).append(',');
        }
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }
    
    private Map<Long, Set<String>> getDrugs(List<CnaEvent> cnaEvents, boolean fdaOnly, boolean cancerDrug)
            throws DaoException {
        DaoDrugInteraction daoDrugInteraction = DaoDrugInteraction.getInstance();
        Set<Long> genes = new HashSet<Long>();
        
        // Temporary way of handling cases such as akt inhibitor for pten loss
        Map<Long,Set<Long>> mapTargetToEventGenes = new HashMap<Long,Set<Long>>();
        // end Temporary way of handling cases such as akt inhibitor for pten loss
        
        for (CnaEvent cnaEvent : cnaEvents) {
            long gene = cnaEvent.getEntrezGeneId();
            if (cnaEvent.getAlteration()==CnaEvent.CNA.AMP
                    ||cnaEvent.getAlteration()==CnaEvent.CNA.GAIN) { // since drugs are usually intibiting
                genes.add(gene);
            }
            
            // Temporary way of handling cases such as akt inhibitor for pten loss
            Set<Long> targets = daoDrugInteraction.getMoreTargets(gene, cnaEvent.getAlteration().name());
            genes.addAll(targets);
            for (Long target : targets) {
                Set<Long> eventGenes = mapTargetToEventGenes.get(target);
                if (eventGenes==null) {
                    eventGenes = new HashSet<Long>();
                    mapTargetToEventGenes.put(target, eventGenes);
                }
                eventGenes.add(gene);
            }
            // end Temporary way of handling cases such as akt inhibitor for pten loss
        }
        
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
    
    private Map<Long, Map<String,Object>> getMrnaContext(String caseId, List<CnaEvent> cnaEvents,
            String mrnaProfileId) throws DaoException {
        Map<Long, Map<String,Object>> mapGenePercentile = new HashMap<Long, Map<String,Object>>();
        DaoGeneticAlteration daoGeneticAlteration = DaoGeneticAlteration.getInstance();
        for (CnaEvent cnaEvent : cnaEvents) {
            long gene = cnaEvent.getEntrezGeneId();
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
    
    private Map<String,List> initMap() {
        Map<String,List> map = new HashMap<String,List>();
        map.put("id", new ArrayList());
        map.put("caseIds", new ArrayList());
        map.put("entrez", new ArrayList());
        map.put("gene", new ArrayList());
        map.put("cytoband", new ArrayList());
        map.put("alter", new ArrayList());
        map.put("mrna", new ArrayList());
        map.put("gistic", new ArrayList());
        map.put("sanger", new ArrayList());
        map.put("cancer-gene", new ArrayList());
        map.put("drug", new ArrayList());
        map.put("altrate", new ArrayList());
        return map;
    }
    
    private void exportCnaEvent(Map<String,List> data, Map<Long, Integer> mapMutationEventIndex,
            CnaEvent cnaEvent, CancerStudy cancerStudy, Set<String> drugs, Integer context,
            Map<String,Object> mrna, DaoGeneOptimized daoGeneOptimized) 
            throws ServletException {
        Long eventId = cnaEvent.getEventId();
        Integer ix = mapMutationEventIndex.get(eventId);
        if (ix!=null) { // multiple samples
            List.class.cast(data.get("caseIds").get(ix)).add(cnaEvent.getCaseId());
            return;
        }
        
        mapMutationEventIndex.put(eventId, data.get("id").size());
        
        List<String> samples = new ArrayList<String>();
        samples.add(cnaEvent.getCaseId());
        data.get("caseIds").add(samples);
        
        data.get("id").add(cnaEvent.getEventId());
        CanonicalGene gene = daoGeneOptimized.getGene(cnaEvent.getEntrezGeneId());
        String symbol = gene.getHugoGeneSymbolAllCaps();
        data.get("gene").add(symbol);
        data.get("cytoband").add(gene.getCytoband());
        data.get("entrez").add(cnaEvent.getEntrezGeneId());
        data.get("alter").add(cnaEvent.getAlteration().getCode());
        data.get("mrna").add(mrna);
        
        // TODO: GISTIC
        List gistic;
        try {
            gistic = getGistic(cancerStudy.getInternalId(),
                    cnaEvent.getGeneSymbol(), cnaEvent.getAlteration());
        } catch (DaoException ex) {
            throw new ServletException(ex);
        }
        data.get("gistic").add(gistic);
        
        data.get("altrate").add(context);
        
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
    }
    
    private void exportCopyNumberSegment(List list, CopyNumberSegment seg) 
            throws ServletException {
        List row = new ArrayList();
        row.add(seg.getCaseId());
        row.add(seg.getChr());
        row.add(seg.getStart());
        row.add(seg.getEnd());
        row.add(seg.getNumProbes());
        row.add(seg.getSegMean());
        list.add(row);
    }
    
    private static final Map<Integer,Map<String,Map<CnaEvent.CNA,List>>> gisticMap // map from cancer study id
            = new HashMap<Integer,Map<String,Map<CnaEvent.CNA,List>>>();     // to map from gene to a list of params
    
    private static List getGistic(int cancerStudyId, String gene, CnaEvent.CNA cna) throws DaoException {
        Map<String,Map<CnaEvent.CNA,List>> mapGeneGistic;
        synchronized(gisticMap) {
            mapGeneGistic = gisticMap.get(cancerStudyId);
            if (mapGeneGistic == null) {
                mapGeneGistic = new HashMap<String,Map<CnaEvent.CNA,List>>();
                gisticMap.put(cancerStudyId, mapGeneGistic);
                List<Gistic> gistics = DaoGistic.getAllGisticByCancerStudyId(cancerStudyId);
                for (Gistic g : gistics) {
                    List<String> genes = new ArrayList<String>(g.getGenes_in_ROI().size());
                    for (CanonicalGene cg : g.getGenes_in_ROI()) {
                        genes.add(cg.getHugoGeneSymbolAllCaps());
                    }
                    List l = new ArrayList();
                    l.add(g.getqValue());
                    l.add(genes.size());
                    for (String hugo : genes) {
                        Map<CnaEvent.CNA,List> mapCC = mapGeneGistic.get(hugo);
                        if (mapCC==null) {
                            mapCC = new EnumMap<CnaEvent.CNA,List>(CnaEvent.CNA.class);
                            mapGeneGistic.put(hugo, mapCC);
                        }
                        mapCC.put(cna,l);
                    }
                }
            }
        }
        
        Map<CnaEvent.CNA,List> m = mapGeneGistic.get(gene);
        return m==null ? null : m.get(cna);
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
