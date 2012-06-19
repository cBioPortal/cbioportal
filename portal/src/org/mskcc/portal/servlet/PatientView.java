
package org.mskcc.portal.servlet;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.mskcc.cgds.dao.DaoCancerStudy;
import org.mskcc.cgds.dao.DaoCase;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoGeneticProfile;
import org.mskcc.cgds.dao.DaoCaseProfile;
import org.mskcc.cgds.model.GeneticProfile;
import org.mskcc.cgds.model.CancerStudy;
import org.mskcc.cgds.model.Case;
import org.mskcc.cgds.util.AccessControl;
import org.mskcc.portal.util.XDebug;
import org.owasp.validator.html.PolicyException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.List;
import org.mskcc.cgds.model.GeneticAlterationType;

/**
 *
 * @author jj
 */
public class PatientView extends HttpServlet {
    private static Logger logger = Logger.getLogger(PatientView.class);
    public static final String ERROR = "error";
    public static final String PATIENT_ID = "patient";
    public static final String PATIENT_CASE_OBJ = "case_obj";
    public static final String CANCER_STUDY = "cancer_study";
    public static final String MUTATION_PROFILE = "mutation_profile";
    public static final String NUM_CASES_IN_SAME_STUDY = "num_cases";
    private ServletXssUtil servletXssUtil;
    
    private static final DaoGeneticProfile daoGeneticProfile = new DaoGeneticProfile();
    private static final DaoCaseProfile daoCaseProfile = new DaoCaseProfile();

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
        try {
            servletXssUtil = ServletXssUtil.getInstance();
			ApplicationContext context = 
				new ClassPathXmlApplicationContext("classpath:applicationContext-security.xml");
			accessControl = (AccessControl)context.getBean("accessControl");
        } catch (PolicyException e) {
            throw new ServletException (e);
        }
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
        
        //  Get patient ID
        String patientID = servletXssUtil.getCleanInput (request, PATIENT_ID);

        request.setAttribute(QueryBuilder.HTML_TITLE, "Patient "+patientID);
        request.setAttribute(PATIENT_ID, patientID);
        
        try {
            validate(request);
            setGeneticProfiles(request);
            setNumCases(request);
            RequestDispatcher dispatcher =
                    getServletContext().getRequestDispatcher("/WEB-INF/jsp/patient_view.jsp");
            dispatcher.forward(request, response);
        
        } catch (DaoException e) {
            xdebug.logMsg(this, "Got Database Exception:  " + e.getMessage());
            forwardToErrorPage(request, response,
                               "An error occurred while trying to connect to the database.", xdebug);
        } 
    }
    
    private boolean validate(HttpServletRequest request) throws DaoException {
        String caseId = (String) request.getAttribute(PATIENT_ID);
        Case _case = DaoCase.getCase(caseId);
        if (_case==null) {
            request.setAttribute(ERROR, "We have no information about patient "+caseId);
            return false;
        }
        
        CancerStudy cancerStudy = DaoCancerStudy
                .getCancerStudyByInternalId(_case.getCancerStudyId());
        String cancerStudyIdentifier = cancerStudy.getCancerStudyStableId();
        
        if (accessControl.isAccessibleCancerStudy(cancerStudyIdentifier).size() != 1) {
            request.setAttribute(ERROR,
                    "You are not authorized to view the cancer study with id: '" +
                    cancerStudyIdentifier + "'. ");
            return false;
        }
        
        request.setAttribute(PATIENT_CASE_OBJ, _case);
        request.setAttribute(CANCER_STUDY, cancerStudy);
        return true;
    }
    
    private void setGeneticProfiles(HttpServletRequest request) throws DaoException {
        Case _case = (Case)request.getAttribute(PATIENT_CASE_OBJ);
        CancerStudy cancerStudy = (CancerStudy)request.getAttribute(CANCER_STUDY);
        List<GeneticProfile> profiles = daoGeneticProfile.getAllGeneticProfiles(cancerStudy.getInternalId());
        for (GeneticProfile profile : profiles) {
            if (profile.getGeneticAlterationType() == GeneticAlterationType.MUTATION_EXTENDED) {
                if (daoCaseProfile.caseExistsInGeneticProfile(_case.getCaseId(), profile.getGeneticProfileId())) {
                    request.setAttribute(MUTATION_PROFILE, profile);
                }
            }
        }
    }
    
    private void setNumCases(HttpServletRequest request) throws DaoException {
        CancerStudy cancerStudy = (CancerStudy)request.getAttribute(CANCER_STUDY);
        request.setAttribute(NUM_CASES_IN_SAME_STUDY,DaoCase.countCases(cancerStudy.getInternalId()));
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
