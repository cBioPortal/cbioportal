/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** Memorial Sloan-Kettering Cancer Center
 ** has no obligations to provide maintenance, support,
 ** updates, enhancements or modifications.  In no event shall
 ** Memorial Sloan-Kettering Cancer Center
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** Memorial Sloan-Kettering Cancer Center
 ** has been advised of the possibility of such damage.  See
 ** the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **/

// package
package org.mskcc.portal.servlet;

// imports
import org.mskcc.portal.util.XDebug;
import org.mskcc.portal.util.FileUploadRequestWrapper;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.owasp.validator.html.PolicyException;

import java.io.PrintWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Servlet responsible for converting OncoPrint into SVG.
 */
public class OncoPrintConverter extends HttpServlet {

	private Pattern svgXPosPattern;
    private ServletXssUtil servletXssUtil;

    /**
     * Initializes the servlet.
     *
     * @throws ServletException
     */
    public void init() throws ServletException {

        super.init();
        try {
            servletXssUtil = ServletXssUtil.getInstance();
			svgXPosPattern = Pattern.compile("( x=\"(\\d+)\")");
        }
		catch (PolicyException e) {
            throw new ServletException (e);
        }
    }

    /**
     * Handles HTTP GET Request.
     *
     * @param httpServletRequest  HttpServletRequest
     * @param httpServletResponse HttpServletResponse
     * @throws ServletException
     */
    protected void doGet(HttpServletRequest httpServletRequest,
                         HttpServletResponse httpServletResponse) throws ServletException,
            IOException {

        doPost(httpServletRequest, httpServletResponse);
    }

    /**
     * Handles the HTTP POST Request.
     *
     * @param httpServletRequest  HttpServletRequest
     * @param httpServletResponse HttpServletResponse
     * @throws ServletException
     */
    protected void doPost(HttpServletRequest httpServletRequest,
                          HttpServletResponse httpServletResponse) throws ServletException, IOException {

		// setup a debug object 
        XDebug xdebug = new XDebug( httpServletRequest );
		xdebug.logMsg(this, "Attempting to parse request parameters.");

		String xml = "";
		String format = "";
		if (httpServletRequest instanceof FileUploadRequestWrapper) {

			// get instance of our request wrapper
			FileUploadRequestWrapper wrapper = (FileUploadRequestWrapper)httpServletRequest;

			// get format parameter
			format = wrapper.getParameter("format");

			// get xml parameter
			xml = wrapper.getParameter("xml");
		}
		else {
			format = servletXssUtil.getCleanInput(httpServletRequest, "format");
			// TODO - update antisamy.xml to support svg-xml
			xml = httpServletRequest.getParameter("xml");
		}

		// sanity check
		if (!format.equals("svg")) {
			forwardToErrorPage(getServletContext(), httpServletRequest, httpServletResponse, xdebug);
		}

		// outta here
		convertToSVG(httpServletResponse, xml);
	}

	/**
	 * Reflect given svg xml back to browser for saving.
	 *
	 * @param response HttpServletResponse
	 * @param xml String
	 */
	private void convertToSVG(HttpServletResponse response, String xml) throws ServletException {

		try {
			response.setContentType("application/svg+xml");
			PrintWriter writer = response.getWriter();
			try {
				writer.write(xml);
			}
			finally {
				writer.flush();
				writer.close();
			}
		}
		catch (IOException e) {
			throw new ServletException(e);
		}
	}

	/**
	 * Method called when exception occurs.
	 * 
	 * @param servletContext ServletContext
	 * @param request HttpServletRequest
	 * @param response HttpServletResponse
	 * @param xdebug XDebug
	 */
    private static void forwardToErrorPage(ServletContext servletContext,
                                           HttpServletRequest request,
										   HttpServletResponse response,
										   XDebug xdebug) throws ServletException, IOException {

       request.setAttribute("xdebug_object", xdebug);
	   RequestDispatcher dispatcher = servletContext.getRequestDispatcher("/WEB-INF/jsp/error.jsp");
	   dispatcher.forward(request, response);
    }
}