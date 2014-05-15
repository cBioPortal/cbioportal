
package org.mskcc.cbio.portal.servlet;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.log4j.Logger;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.*;
import org.owasp.validator.html.PolicyException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 * @author jj
 */
public class CancerStudyView extends HttpServlet {
    private static Logger logger = Logger.getLogger(CancerStudyView.class);
    public static final String ERROR = "error";
    public static final String CANCER_STUDY = "cancer_study";
    public static final String MUTATION_PROFILE = "mutation_profile";
    public static final String CNA_PROFILE = "cna_profile";
    
    private static final DaoPatientList daoPatientList = new DaoPatientList();

    // class which process access control to cancer studies
    private AccessControl accessControl;

    /**
     * Initializes the servlet.
     *
     * @throws ServletException Serlvet Init Error.
     */
    @Override
    public void init() throws ServletException {
        super.init();

		ApplicationContext context =
			new ClassPathXmlApplicationContext("classpath:applicationContext-security.xml");
		accessControl = (AccessControl)context.getBean("accessControl");
    }
    
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        XDebug xdebug = new XDebug( request );
        request.setAttribute(QueryBuilder.XDEBUG_OBJECT, xdebug);

        try {
            if (validate(request)) {
                setGeneticProfiles(request);
            }
            
            if (request.getAttribute(ERROR)!=null) {
                forwardToErrorPage(request, response, (String)request.getAttribute(ERROR), xdebug);
            } else {
                RequestDispatcher dispatcher =
                        getServletContext().getRequestDispatcher("/WEB-INF/jsp/study_view/cancer_study_view.jsp");
                dispatcher.forward(request, response);
            }
        
        } catch (DaoException e) {
            xdebug.logMsg(this, "Got Database Exception:  " + e.getMessage());
            forwardToErrorPage(request, response,
                               "An error occurred while trying to connect to the database.", xdebug);
        } 
    }
    
    private boolean validate(HttpServletRequest request) throws DaoException {
        String cancerStudyID = request.getParameter(QueryBuilder.CANCER_STUDY_ID);
        
        CancerStudy cancerStudy = DaoCancerStudy
                .getCancerStudyByStableId(cancerStudyID);
        if (cancerStudy==null) {
            try {
                cancerStudy = DaoCancerStudy.getCancerStudyByInternalId(
                        Integer.parseInt(cancerStudyID));
            } catch(NumberFormatException ex) {}
                
        }
        if (cancerStudy==null) {
            request.setAttribute(ERROR, "No such cancer study");
            return false;
        }
        String cancerStudyIdentifier = cancerStudy.getCancerStudyStableId();
        
        if (accessControl.isAccessibleCancerStudy(cancerStudyIdentifier).size() != 1) {
            request.setAttribute(ERROR,
                    "You are not authorized to view the cancer study with id: '" +
                    cancerStudyIdentifier + "'. ");
            return false;
        }
        
        String patientListId = (String)request.getAttribute(QueryBuilder.CASE_SET_ID);
        if (patientListId==null) {
            patientListId = cancerStudy.getCancerStudyStableId()+"_all";
            request.setAttribute(QueryBuilder.CASE_SET_ID, patientListId);
        }
        
        PatientList patientList = daoPatientList.getPatientListByStableId(patientListId);
        if (patientList==null) {
            request.setAttribute(ERROR,
                    "Could not find patient list of '" + patientListId + "'. ");
            return false;
        }
        
        request.setAttribute(QueryBuilder.CASE_IDS, patientList.getPatientList());
        
        request.setAttribute(CANCER_STUDY, cancerStudy);
        request.setAttribute(QueryBuilder.HTML_TITLE, cancerStudy.getName());
        return true;
    }
    
    private void setGeneticProfiles(HttpServletRequest request) throws DaoException {
        CancerStudy cancerStudy = (CancerStudy)request.getAttribute(CANCER_STUDY);
        GeneticProfile mutProfile = cancerStudy.getMutationProfile();
        if (mutProfile!=null) {
            request.setAttribute(MUTATION_PROFILE, mutProfile);
        }
        
        GeneticProfile cnaProfile = cancerStudy.getCopyNumberAlterationProfile(true);
        if (cnaProfile!=null) {
            request.setAttribute(CNA_PROFILE, cnaProfile);
        }
    }
    
    private void forwardToErrorPage(HttpServletRequest request, HttpServletResponse response,
                                    String userMessage, XDebug xdebug)
            throws ServletException, IOException {
        request.setAttribute("xdebug_object", xdebug);
        request.setAttribute(QueryBuilder.USER_ERROR_MESSAGE, userMessage);
        RequestDispatcher dispatcher =
                getServletContext().getRequestDispatcher("/WEB-INF/jsp/error.jsp");
        dispatcher.forward(request, response);
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
