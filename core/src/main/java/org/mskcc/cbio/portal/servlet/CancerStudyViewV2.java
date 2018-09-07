package org.mskcc.cbio.portal.servlet;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mskcc.cbio.portal.util.XDebug;

/**
 *
 * @author Karthik Kalletla
 */
public class CancerStudyViewV2 extends HttpServlet {
	private static Log LOG = LogFactory.getLog(CancerStudyView.class);
	public static final String ID = "id";

	/**
	 * Initializes the servlet.
	 *
	 * @throws ServletException
	 *             Serlvet Init Error.
	 */
	@Override
	public void init() throws ServletException {
		super.init();
	}

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
	protected void processRequest(HttpServletRequest  request,
			                      HttpServletResponse response) throws ServletException, IOException {

		XDebug xdebug = new XDebug(request);
		request.setAttribute(QueryBuilder.XDEBUG_OBJECT, xdebug);
        request.setAttribute(QueryBuilder.HTML_TITLE, "Study Summary");

        RequestDispatcher dispatcher = getServletContext()
                .getRequestDispatcher("/WEB-INF/jsp/dashboard/study_view.jsp");
        dispatcher.forward(request, response);
	}
	
    /**
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest  request,
    		                 HttpServletResponse response)
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
    protected void doPost(HttpServletRequest  request,
    		                  HttpServletResponse response)
        throws ServletException, IOException {
        processRequest(request, response);
    }
}