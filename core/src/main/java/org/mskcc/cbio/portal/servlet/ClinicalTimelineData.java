/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.codehaus.jackson.map.ObjectMapper;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoDiagnostic;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoLabTest;
import org.mskcc.cbio.portal.dao.DaoTreatment;
import org.mskcc.cbio.portal.model.Diagnostic;
import org.mskcc.cbio.portal.model.LabTest;
import org.mskcc.cbio.portal.model.Treatment;

/**
 *
 * @author jgao
 */
public class ClinicalTimelineData extends HttpServlet {
    
    public final static String TREATMENT = "treatment";
    public final static String DIAGNOSTIC = "diagnostic";
    public final static String LAB_TEST = "lab_test";
    
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Set<String> types = new HashSet<String>(Arrays.asList(request.getParameter("type").split("[ ,]+")));
        int cancerStudyId = DaoCancerStudy.getCancerStudyByStableId(request.getParameter("cancer_study_id")).getInternalId();
        String patientId = request.getParameter("patient_id");
        
        HashMap result = new HashMap();
        
        try {
            if (types.contains(TREATMENT)) {
                List<Treatment> treatments = DaoTreatment.getTreatment(cancerStudyId, patientId);
                if (!treatments.isEmpty()) {
                    result.put(TREATMENT, treatments);
                }
            }

            if (types.contains(DIAGNOSTIC)) {
                List<Diagnostic> diagnostics = DaoDiagnostic.getDiagnostic(cancerStudyId, patientId);
                if (!diagnostics.isEmpty()) {
                    result.put(DIAGNOSTIC, diagnostics);
                }
            }

            if (types.contains(LAB_TEST)) {
                List<LabTest> labTests = DaoLabTest.getLabTest(cancerStudyId, patientId);
                if (!labTests.isEmpty()) {
                    result.put(LAB_TEST, labTests);
                }
            }
        } catch (DaoException ex) {
            throw new ServletException(ex);
        }
        
        
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(out, result);
        } finally {            
            out.close();
        }
        
    }
    
    private void processGetTreatmentDataRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int cancerStudyId = DaoCancerStudy.getCancerStudyByStableId(request.getParameter("cancer_study_id")).getInternalId();
        String patientId = request.getParameter("patient_id");
        
        List<Treatment> treatments = Collections.emptyList();
        try {
            treatments = DaoTreatment.getTreatment(cancerStudyId, patientId);
        } catch (DaoException ex) {
            throw new ServletException(ex);
        }
        
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(out, treatments);
        } finally {            
            out.close();
        }
    }
    
    private void processGetDiagnosticDataRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int cancerStudyId = DaoCancerStudy.getCancerStudyByStableId(request.getParameter("cancer_study_id")).getInternalId();
        String patientId = request.getParameter("patient_id");
        
        List<Diagnostic> diagnostics = Collections.emptyList();
        try {
            diagnostics = DaoDiagnostic.getDiagnostic(cancerStudyId, patientId);
        } catch (DaoException ex) {
            throw new ServletException(ex);
        }
        
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(out, diagnostics);
        } finally {            
            out.close();
        }
    }
    
    private void processGetLabTestDataRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int cancerStudyId = DaoCancerStudy.getCancerStudyByStableId(request.getParameter("cancer_study_id")).getInternalId();
        String patientId = request.getParameter("patient_id");
        
        List<LabTest> labTests = Collections.emptyList();
        try {
            labTests = DaoLabTest.getLabTest(cancerStudyId, patientId);
        } catch (DaoException ex) {
            throw new ServletException(ex);
        }
        
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(out, labTests);
        } finally {            
            out.close();
        }
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
