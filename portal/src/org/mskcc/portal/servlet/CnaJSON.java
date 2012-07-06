
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
import org.mskcc.cgds.model.Case;
import org.mskcc.cgds.model.CnaEvent;
import org.mskcc.cgds.model.GeneticProfile;

/**
 *
 * @author jj
 */
public class CnaJSON extends HttpServlet {
    private static Logger logger = Logger.getLogger(CnaJSON.class);
    private static final DaoGeneticProfile daoGeneticProfile = new DaoGeneticProfile();
    
    public static final String CMD = "cmd";
    public static final String GET_CONTEXT_CMD = "get_context";
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
        if (cmd!=null && cmd.equalsIgnoreCase(GET_CONTEXT_CMD)) {
            processGetCnaContextRequest(request, response);
        } else {
            processGetCnaRequest(request, response);
        }
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
            export(table, cnaEvent);
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
    
    private void export(JSONArray table, CnaEvent cnaEvent) 
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
        // TODO: clinical trial
        row.add("pending");
        // TODO: annotation
        row.add("pending");
        
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
