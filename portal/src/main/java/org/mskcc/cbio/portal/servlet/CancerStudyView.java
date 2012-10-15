
package org.mskcc.cbio.portal.servlet;

import java.io.IOException;
import java.util.List;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.mskcc.cbio.cgds.dao.DaoCancerStudy;
import org.mskcc.cbio.cgds.dao.DaoCaseList;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.dao.DaoGeneticProfile;
import org.mskcc.cbio.cgds.model.CancerStudy;
import org.mskcc.cbio.cgds.model.CaseList;
import org.mskcc.cbio.cgds.model.GeneticAlterationType;
import org.mskcc.cbio.cgds.model.GeneticProfile;
import org.mskcc.cbio.cgds.util.AccessControl;
import org.mskcc.cbio.portal.util.XDebug;
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
    private ServletXssUtil servletXssUtil;
    
    private static final DaoGeneticProfile daoGeneticProfile = new DaoGeneticProfile();
    private static final DaoCaseList daoCaseList = new DaoCaseList();

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

        try {
            if (validate(request)) {
                setGeneticProfiles(request);
            }
            RequestDispatcher dispatcher =
                    getServletContext().getRequestDispatcher("/WEB-INF/jsp/tumormap/cancer_study_view/cancer_study_view.jsp");
            dispatcher.forward(request, response);
        
        } catch (DaoException e) {
            xdebug.logMsg(this, "Got Database Exception:  " + e.getMessage());
            forwardToErrorPage(request, response,
                               "An error occurred while trying to connect to the database.", xdebug);
        } 
    }
    
    private boolean validate(HttpServletRequest request) throws DaoException {
        String cancerStudyID = servletXssUtil.getCleanInput (request, QueryBuilder.CANCER_STUDY_ID);
        
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
        
        String caseListId = (String)request.getAttribute(QueryBuilder.CASE_SET_ID);
        if (caseListId==null) {
            caseListId = cancerStudy.getCancerStudyStableId()+"_all";
            request.setAttribute(QueryBuilder.CASE_SET_ID, caseListId);
        }
        
        CaseList caseList = daoCaseList.getCaseListByStableId(caseListId);
        if (caseList==null) {
            request.setAttribute(ERROR,
                    "Could not find case list of '" + caseListId + "'. ");
            return false;
        }
        
        request.setAttribute(QueryBuilder.CASE_IDS, caseList.getCaseList());
        
        request.setAttribute(CANCER_STUDY, cancerStudy);
        request.setAttribute(QueryBuilder.HTML_TITLE, cancerStudy.getName());
        return true;
    }
    
    private void setGeneticProfiles(HttpServletRequest request) throws DaoException {
        CancerStudy cancerStudy = (CancerStudy)request.getAttribute(CANCER_STUDY);
        List<GeneticProfile> profiles = daoGeneticProfile.getAllGeneticProfiles(
                cancerStudy.getInternalId());
        for (GeneticProfile profile : profiles) {
            // TODO: is it possible of multiple mutation or gistic profiles for one cancer study?
            if (profile.getGeneticAlterationType() == GeneticAlterationType.MUTATION_EXTENDED) {
                request.setAttribute(MUTATION_PROFILE, profile);
            } else if (profile.getGeneticAlterationType() == GeneticAlterationType
                    .COPY_NUMBER_ALTERATION && profile.showProfileInAnalysisTab()) {
                request.setAttribute(CNA_PROFILE, profile);
            }
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
