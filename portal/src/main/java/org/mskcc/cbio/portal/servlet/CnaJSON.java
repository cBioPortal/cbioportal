
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
import org.mskcc.cbio.cgds.model.CanonicalGene;
import org.mskcc.cbio.cgds.model.Case;
import org.mskcc.cbio.cgds.model.CnaEvent;
import org.mskcc.cbio.cgds.model.CopyNumberSegment;
import org.mskcc.cbio.cgds.model.GeneticProfile;
import org.mskcc.cbio.cgds.model.Gistic;
import org.mskcc.cbio.portal.util.SkinUtil;

/**
 *
 * @author jj
 */
public class CnaJSON extends HttpServlet {
    private static Logger logger = Logger.getLogger(CnaJSON.class);
    private static final DaoGeneticProfile daoGeneticProfile = new DaoGeneticProfile();
    
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
        String patient = request.getParameter(PatientView.PATIENT_ID);
        String cnaProfileId = request.getParameter(PatientView.CNA_PROFILE);
                
        GeneticProfile cnaProfile;
        CancerStudy cancerStudy = null;
        Case _case;
        List<CnaEvent> cnaEvents = Collections.emptyList();
        Map<String, List<String>> drugs = Collections.emptyMap();
        Map<Long, Integer>  contextMap = Collections.emptyMap();

        try {
            _case = DaoCase.getCase(patient);
            cnaProfile = daoGeneticProfile.getGeneticProfileByStableId(cnaProfileId);
            cancerStudy = DaoCancerStudy.getCancerStudyByInternalId(_case.getCancerStudyId());
            if (_case!=null && cnaProfile!=null) {
                cnaEvents = DaoCnaEvent.getCnaEvents(patient, cnaProfile.getGeneticProfileId());
                String concatEventIds = getConcatEventIds(cnaEvents);
                int profileId = cnaProfile.getGeneticProfileId();
                Set<Long> genes = DaoCnaEvent.getAlteredGenes(concatEventIds, profileId);
                drugs = getDrugs(genes, profileId);
                contextMap = DaoCnaEvent.countSamplesWithCnaEvents(concatEventIds, profileId);
            }
        } catch (DaoException ex) {
            throw new ServletException(ex);
        }
        
        Map<String,List> data = initMap();
        for (CnaEvent cnaEvent : cnaEvents) {
            List<String> drug = 
                    (cnaEvent.getAlteration()==CnaEvent.CNA.AMP||cnaEvent.getAlteration()==CnaEvent.CNA.GAIN)
                    ? drugs.get(cnaEvent.getGeneSymbol()) : null;
            exportCnaEvent(data, cnaEvent, cancerStudy, drug, contextMap.get(cnaEvent.getEventId()));
        }

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        try {
            JSONValue.writeJSONString(data, out);
        } finally {            
            out.close();
        }
    }
    
    private void processGetSegmentsRequest(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        JSONArray table = new JSONArray();

        String patients = request.getParameter(PatientView.PATIENT_ID);
        
        List<CopyNumberSegment> segs = Collections.emptyList();
        
        try {
            segs = DaoCopyNumberSegment.getSegmentForCases(Arrays.asList(patients.split("[, ]+")));
        } catch (DaoException ex) {
            throw new ServletException(ex);
        }
        
        for (CopyNumberSegment seg : segs) {
            exportCopyNumberSegment(table, seg);
        }

        response.setContentType("application/json");
        
        PrintWriter out = response.getWriter();
        try {
            JSONValue.writeJSONString(table, out);
        } finally {            
            out.close();
        }
    }
    
    private void processCnaFractionsRequest(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        String strCaseIds = request.getParameter(QueryBuilder.CASE_IDS);
        List<String> caseIds = strCaseIds==null ? null : Arrays.asList(strCaseIds.split("[ ,]+"));
        
        Map<String, Double> fraction = Collections.emptyMap();
        
        try {
            fraction = DaoCopyNumberSegment.getCopyNumberActeredFraction(caseIds,
                    SkinUtil.getPatientViewGenomicOverviewCnaCutoff()[0]);
        } catch (DaoException ex) {
            throw new ServletException(ex);
        }

        response.setContentType("application/json");
        
        PrintWriter out = response.getWriter();
        try {
            JSONValue.writeJSONString(fraction, out);
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
    
    private Map<String, List<String>> getDrugs(Set<Long> genes, int profileId)
            throws DaoException {
        Map<Long, List<String>> map = DaoDrugInteraction.getInstance().getDrugs(genes,true,true);
        Map<String, List<String>> ret = new HashMap<String, List<String>>(map.size());
        for (Map.Entry<Long, List<String>> entry : map.entrySet()) {
            String symbol = DaoGeneOptimized.getInstance().getGene(entry.getKey())
                    .getHugoGeneSymbolAllCaps();
            ret.put(symbol, entry.getValue());
        }
        return ret;
    }
    
    private Map<String,List> initMap() {
        Map<String,List> map = new HashMap<String,List>();
        map.put("id", new ArrayList());
        map.put("entrez", new ArrayList());
        map.put("gene", new ArrayList());
        map.put("alter", new ArrayList());
        map.put("gistic", new ArrayList());
        map.put("sanger", new ArrayList());
        map.put("impact", new ArrayList());
        map.put("drug", new ArrayList());
        map.put("altrate", new ArrayList());
        return map;
    }
    
    private void exportCnaEvent(Map<String,List> data, CnaEvent cnaEvent,
            CancerStudy cancerStudy, List<String> drugs, Integer context) 
            throws ServletException {
        data.get("id").add(cnaEvent.getEventId());
        String symbol = null;
        try {
            symbol = DaoGeneOptimized.getInstance().getGene(cnaEvent.getEntrezGeneId())
                    .getHugoGeneSymbolAllCaps();
        } catch (DaoException ex) {
            throw new ServletException(ex);
        }
        data.get("gene").add(symbol);
        data.get("entrez").add(cnaEvent.getEntrezGeneId());
        data.get("alter").add(cnaEvent.getAlteration().getCode());
        
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
        boolean isIMPACTGene = false;
        try {
            isSangerGene = DaoSangerCensus.getInstance().getCancerGeneSet().containsKey(symbol);
            isIMPACTGene = DaoGeneOptimized.getInstance().isIMPACTGene(symbol);
        } catch (DaoException ex) {
            throw new ServletException(ex);
        }
        data.get("sanger").add(isSangerGene);
        data.get("impact").add(isIMPACTGene);
        
        // drug
        data.get("drug").add(drugs);
    }
    
    private void exportCopyNumberSegment(JSONArray table, CopyNumberSegment seg) 
            throws ServletException {
        JSONArray row = new JSONArray();
        row.add(seg.getCaseId());
        row.add(seg.getChr());
        row.add(seg.getStart());
        row.add(seg.getEnd());
        row.add(seg.getNumProbes());
        row.add(seg.getSegMean());
        table.add(row);
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
