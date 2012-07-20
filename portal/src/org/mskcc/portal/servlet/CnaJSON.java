
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
import org.mskcc.cgds.model.Case;
import org.mskcc.cgds.model.CnaEvent;
import org.mskcc.cgds.model.CopyNumberSegment;
import org.mskcc.cgds.model.GeneticProfile;
import org.mskcc.portal.util.SkinUtil;

/**
 *
 * @author jj
 */
public class CnaJSON extends HttpServlet {
    private static Logger logger = Logger.getLogger(CnaJSON.class);
    private static final DaoGeneticProfile daoGeneticProfile = new DaoGeneticProfile();
    
    public static final String CMD = "cmd";
    public static final String GET_CONTEXT_CMD = "get_context";
    public static final String GET_DRUG_CMD = "get_drug";
    public static final String GET_SEGMENT_CMD = "get_segment";
    public static final String GET_CNA_FRACTION_CMD = "get_cna_fraction";
    public static final String CNA_EVENT_ID = "cna_id";
    public static final String CNA_CONTEXT = "cna_context";
    
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
                processGetCnaContextRequest(request, response);
                return;
            }
            
            if (cmd.equalsIgnoreCase(GET_DRUG_CMD)) {
                processGetDrugsRequest(request, response);
                return;
            }
            
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
        JSONArray table = new JSONArray();

        String patient = request.getParameter(PatientView.PATIENT_ID);
        String cnaProfileId = request.getParameter(PatientView.CNA_PROFILE);
                
        GeneticProfile cnaProfile;
        Case _case;
        List<CnaEvent> cnaEvents = Collections.emptyList();

        try {
            _case = DaoCase.getCase(patient);
            cnaProfile = daoGeneticProfile.getGeneticProfileByStableId(cnaProfileId);
            if (_case!=null && cnaProfile!=null) {
                cnaEvents = DaoCnaEvent.getCnaEvents(patient, cnaProfile.getGeneticProfileId());
            }
        } catch (DaoException ex) {
            throw new ServletException(ex);
        }
        
        for (CnaEvent cnaEvent : cnaEvents) {
            exportCnaEvent(table, cnaEvent);
        }

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        try {
            JSONValue.writeJSONString(table, out);
        } finally {            
            out.close();
        }
    }
    
    private void processGetCnaContextRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String cnaProfileId = request.getParameter(PatientView.CNA_PROFILE);
        String eventIds = request.getParameter(CNA_EVENT_ID);
        
        GeneticProfile cnaProfile;
        Map<Long, Integer> contextMap = Collections.emptyMap();
        
        try {
            cnaProfile = daoGeneticProfile.getGeneticProfileByStableId(cnaProfileId);
            if (cnaProfile!=null) {
                contextMap = DaoCnaEvent.countSamplesWithCnaEvents(
                        eventIds, cnaProfile.getGeneticProfileId());
            }
        } catch (DaoException ex) {
            throw new ServletException(ex);
        }

        response.setContentType("application/json");
        
        PrintWriter out = response.getWriter();
        try {
            JSONValue.writeJSONString(contextMap, out);
        } finally {            
            out.close();
        }
    }
    
    private void processGetDrugsRequest(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        String cnaProfileId = request.getParameter(PatientView.CNA_PROFILE);
        String eventIds = request.getParameter(CNA_EVENT_ID);
        
        GeneticProfile cnaProfile;
        Map<String, List<String>> drugs = Collections.emptyMap();
        
        try {
            cnaProfile = daoGeneticProfile.getGeneticProfileByStableId(cnaProfileId);
            if (cnaProfile!=null) {
                drugs = getDrugs(eventIds, cnaProfile.getGeneticProfileId());
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
    
    private Map<String, List<String>> getDrugs(String eventIds, int profileId)
            throws DaoException {
        Set<Long> genes = DaoCnaEvent.getAlteredGenes(eventIds, profileId);
        Map<Long, List<String>> map = DaoDrugInteraction.getInstance().getDrugs(genes);
        Map<String, List<String>> ret = new HashMap<String, List<String>>(map.size());
        for (Map.Entry<Long, List<String>> entry : map.entrySet()) {
            String symbol = DaoGeneOptimized.getInstance().getGene(entry.getKey())
                    .getHugoGeneSymbolAllCaps();
            ret.put(symbol, entry.getValue());
        }
        return ret;
    }
    
    private void exportCnaEvent(JSONArray table, CnaEvent cnaEvent) 
            throws ServletException {
        JSONArray row = new JSONArray();
        row.add(cnaEvent.getEventId());
        String symbol = null;
        try {
            symbol = DaoGeneOptimized.getInstance().getGene(cnaEvent.getEntrezGeneId())
                    .getHugoGeneSymbolAllCaps();
        } catch (DaoException ex) {
            throw new ServletException(ex);
        }
        row.add(symbol);
        row.add(cnaEvent.getAlteration().getDescription());
        
        // TODO: GISTIC
        double gistic = Math.random();
        row.add(gistic);
        
        // show in summary table
        boolean isSangerGene = false;
        try {
            isSangerGene = DaoSangerCensus.getInstance().getCancerGeneSet().containsKey(symbol);
        } catch (DaoException ex) {
            throw new ServletException(ex);
        }
        boolean includeInSummary = isSangerGene;
        row.add(includeInSummary);
        
        table.add(row);
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
