/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONValue;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoCase;
import org.mskcc.cbio.portal.dao.DaoCaseProfile;
import org.mskcc.cbio.portal.dao.DaoCopyNumberSegment;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoMutation;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.util.AccessControl;
import org.mskcc.cbio.portal.web_api.ProtocolException;
import org.mskcc.cbio.portal.util.GlobalProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 * @author jgao
 */
public class TumorMapServlet extends HttpServlet {
    // ref to our access control object
    private static AccessControl accessControl = null;
    
    /** 
     * Initializes the AccessControl member.
     */
    private static synchronized AccessControl getaccessControl() {
        if (accessControl==null) {ApplicationContext context = 
                    new ClassPathXmlApplicationContext("classpath:applicationContext-security.xml");
            accessControl = (AccessControl)context.getBean("accessControl");
        }
            
        return accessControl;
    }
    
    public static final String CMD = "cmd";
    public static final String GET_STUDY_STATISTICS_CMD = "statistics";
    public static final String GET_STUDY_STATISTICS_TYPE = "type";
    

    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String cmd = request.getParameter(CMD);
        if (null!=cmd) {
            if (cmd.equalsIgnoreCase(GET_STUDY_STATISTICS_CMD)) {
                String type = request.getParameter(GET_STUDY_STATISTICS_TYPE);
                processRequestStatistics(request, response, type);
                return;
            }
        }
        
        processRequestDefault(request, response);
    }
    
    private void processRequestDefault(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute(QueryBuilder.HTML_TITLE, GlobalProperties.getTitle());
        
        RequestDispatcher dispatcher =
                getServletContext().getRequestDispatcher("/WEB-INF/jsp/tumormap/tumormap.jsp");
        dispatcher.forward(request, response);
    }
    
    private void processRequestStatistics(HttpServletRequest request, HttpServletResponse response, String type)
            throws ServletException, IOException {
        String cancerStudyIds = request.getParameter(QueryBuilder.CANCER_STUDY_ID);
        
        try {
            boolean includeMut = "mut".equalsIgnoreCase(type);
            boolean includeCna = "cna".equalsIgnoreCase(type);
            
            Map<String,Map<String,Object>> data = new HashMap<String,Map<String,Object>>();
            // get list of cancer studies
            AccessControl accessControl = getaccessControl();
            List<CancerStudy> cancerStudies;
            if (cancerStudyIds==null || cancerStudyIds.isEmpty()) {
                cancerStudies = accessControl.getCancerStudies();
            } else {
                cancerStudies = new ArrayList<CancerStudy>();
                for (String studyId : cancerStudyIds.split("[ ,]+")) {
                    CancerStudy study  = DaoCancerStudy.getCancerStudyByStableId(studyId);
                    if (study!=null && !accessControl.isAccessibleCancerStudy(studyId).isEmpty()) {
                        cancerStudies.add(study);
                    }
                }
            }
            for (CancerStudy cancerStudy : cancerStudies) {
                if (cancerStudy.getCancerStudyStableId().equalsIgnoreCase("all")) {
                    continue;
                }
                Map<String,Object> row = new HashMap<String,Object>();
                data.put(cancerStudy.getCancerStudyStableId(),row);
                
                if (!includeMut&&!includeMut) {
                    row.put("name",cancerStudy.getName());
                    String pmid = cancerStudy.getPmid();
                    if (pmid!=null) {
                        row.put("pmid",pmid);
                    }
                    String citation = cancerStudy.getCitation();
                    if (citation!=null) {
                        row.put("citation", citation);
                    }
                    row.put("cases",DaoCase.countCases(cancerStudy.getInternalId()));
                }
                
                if (includeMut) {
                    GeneticProfile mutProfile = cancerStudy.getMutationProfile();
                    if (mutProfile==null) {
                        row.put("mut",0);
                    } else {
                        int mutEvents = DaoMutation.countMutationEvents(mutProfile.getGeneticProfileId());
                        int samplesWithMut = DaoCaseProfile.countCasesInProfile(mutProfile.getGeneticProfileId());
                        row.put("mut",1.0*mutEvents/samplesWithMut);
                    }
                }
                
                if (includeCna) {
                    GeneticProfile cnaProfile = cancerStudy.getCopyNumberAlterationProfile(false);
                    if (cnaProfile==null) {
                        row.put("cna",0);
                    } else {
                        List<String> cases = DaoCaseProfile.getAllCaseIdsInProfile(cnaProfile.getGeneticProfileId());
                        Map<String,Double> fracs = DaoCopyNumberSegment.getCopyNumberActeredFraction(cases,
                                cnaProfile.getCancerStudyId(),GlobalProperties.getPatientViewGenomicOverviewCnaCutoff()[0]);
                        double aveFrac = 0;
                        for (double frac : fracs.values()) {
                            aveFrac += frac;
                        }
                        aveFrac /= cases.size();
                        row.put("cna",aveFrac);
                    }
                }
            }
            
            response.setContentType("application/json");

            PrintWriter out = response.getWriter();
            try {
                JSONValue.writeJSONString(data, out);
            } finally {            
                out.close();
            }
        } catch (DaoException ex) {
            throw new ServletException(ex);
        } catch (ProtocolException ex) {
            throw new ServletException(ex);
        }
        
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
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
     * Handles the HTTP
     * <code>POST</code> method.
     *
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
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
