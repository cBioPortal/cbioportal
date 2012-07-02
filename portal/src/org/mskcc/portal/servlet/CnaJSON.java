
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
            }
        } catch (DaoException ex) {
            throw new ServletException(ex);
        }
        
        PrintWriter out = response.getWriter();
        if (_case==null || cnaEvents.isEmpty()) {
            try {
                out.print("No copy number alteration in "+patient);
            } finally {            
                out.close();
            }
            return;
        }
        
        HashMap<Long, String> mapGeneContent = new HashMap<Long,String>();
        for (CnaEvent cnaEvent : cnaEvents) {
            export(table, cnaEvent, mapGeneContent, numAllCases);
        }

        response.setContentType("application/json");
        try {
            JSONValue.writeJSONString(table, out);
        } finally {            
            out.close();
        }
    }
    
    private void export(JSONArray table, CnaEvent cnaEvent, 
            HashMap<Long, String> mapGeneContent, int numAllCases) 
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
        String context = getContext(cnaEvent, mapGeneContent, numAllCases);
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
    
    private String getContext(CnaEvent cnaEvent, HashMap<Long, String> mapGeneContent, 
            int numAllCases) throws ServletException {
        String ret = mapGeneContent.get(cnaEvent.getEntrezGeneId());
        if (ret!=null) {
            return ret;
        }
        
        StringBuilder sb = new StringBuilder();
        int altCount;
        try {
            altCount = DaoCnaEvent.countSamplesWithCnaEvents(cnaEvent.getEventId(), cnaEvent.getCnaProfileId());
        } catch (DaoException ex) {
            throw new ServletException(ex);
        }
        
        String perc = String.format("<b>%.1f%%</b>", 100.0*altCount/numAllCases);
        sb.append(altCount).append(" (").append(perc).append(")");
        
        ret = sb.toString();
        mapGeneContent.put(cnaEvent.getEntrezGeneId(), ret);
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
