
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
import org.mskcc.cgds.model.CancerStudy;
import org.mskcc.cgds.model.Case;
import org.mskcc.cgds.model.CnaEvent;
import org.mskcc.cgds.model.GeneticProfile;
import java.util.HashSet;

/**
 *
 * @author jj
 */
public class CnaJSON extends HttpServlet {
    private static Logger logger = Logger.getLogger(CnaJSON.class);
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
        String cnaProfileId = request.getParameter(PatientView.CNA_PROFILE);
                
        GeneticProfile cnaProfile;
        Case _case;
        List<CnaEvent> cnaEvents = Collections.emptyList();
        CancerStudy cancerStudy;
        Map<Long, String> contextMap = Collections.emptyMap();
        
        String strNumAllCases = request.getParameter(PatientView.NUM_CASES_IN_SAME_STUDY);
        int numAllCases = strNumAllCases==null ? 0 : Integer.parseInt(strNumAllCases);
        try {
            _case = DaoCase.getCase(patient);
            cnaProfile = daoGeneticProfile.getGeneticProfileByStableId(cnaProfileId);
            if (_case!=null && cnaProfile!=null) {
                cancerStudy = DaoCancerStudy.getCancerStudyByInternalId(_case.getCancerStudyId());
                cnaEvents = DaoCnaEvent.getCnaEvents(patient, cnaProfile.getGeneticProfileId());
                if (strNumAllCases==null) {
                    numAllCases = DaoCase.countCases(cancerStudy.getInternalId());
                }
                contextMap = getContextMap(cnaEvents, cnaProfile.getGeneticProfileId(), numAllCases);
            }
        } catch (DaoException ex) {
            throw new ServletException(ex);
        }
        
        for (CnaEvent cnaEvent : cnaEvents) {
            export(table, cnaEvent, contextMap);
        }

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        try {
            JSONValue.writeJSONString(table, out);
        } finally {            
            out.close();
        }
    }
    
    private Map<Long, String> getContextMap(List<CnaEvent> cnaEvents, int cnaProfileId,
            int numAllCases) throws DaoException {
        HashSet<Long> eventIds = new HashSet<Long>();
        for (CnaEvent ev : cnaEvents) {
            eventIds.add(ev.getEventId());;
        }
        
        Map<Long, Integer> countMap = DaoCnaEvent.countSamplesWithCnaEvents(eventIds, cnaProfileId);
        
        Map<Long, String> contextMap = new HashMap<Long, String>(countMap.size());
        for (Map.Entry<Long, Integer> entry : countMap.entrySet()) {
            int altCount = entry.getValue();
            String context = String.format("%d (<b>%.1f%%</b>) ", altCount, 100.0*altCount/numAllCases);
            contextMap.put(entry.getKey(), context);
        }
        
        return contextMap;
    }
    
    private void export(JSONArray table, CnaEvent cnaEvent, Map<Long, String> contextMap) 
            throws ServletException {
        JSONArray row = new JSONArray();
        try {
            row.add(DaoGeneOptimized.getInstance().getGene(cnaEvent.getEntrezGeneId())
                    .getHugoGeneSymbolAllCaps());
        } catch (DaoException ex) {
            throw new ServletException(ex);
        }
        row.add(cnaEvent.getAlteration().getDescription());
        // TODO: context
        String context = contextMap.get(cnaEvent.getEventId());
        row.add(context);
        // TODO: clinical trial
        row.add("pending");
        // TODO: annotation
        row.add("pending");
        
        // TODO: GISTIC
        double gistic = Math.random();
        row.add(gistic);
        
        // show in summary table
        boolean includeInSummary = gistic < 0.05;
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
