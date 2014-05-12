package org.mskcc.cbio.portal.servlet;

import org.mskcc.cbio.portal.util.XDebug;
import org.mskcc.cbio.portal.util.XssRequestWrapper;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Enables downloading of client-side generated text files.
 * Simply reflects back whatever received as a downloadable text file.
 *
 * @author Selcuk Onur Sumer
 */
public class TextFileDownload extends HttpServlet
{
	private static String DEFAULT_FILENAME = "result.txt";

	/**
	 * Handles HTTP GET Request.
	 *
	 * @param httpServletRequest  HttpServletRequest
	 * @param httpServletResponse HttpServletResponse
	 * @throws IOException
	 */
	protected void doGet(HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse)
			throws IOException
	{
		doPost(httpServletRequest, httpServletResponse);
	}

	/**
	 * Handles the HTTP POST Request.
	 *
	 * @param httpServletRequest  HttpServletRequest
	 * @param httpServletResponse HttpServletResponse
	 * @throws IOException
	 */
	protected void doPost(HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse)
			throws IOException
	{
		// setup a debug object
		XDebug xdebug = new XDebug( httpServletRequest );
		xdebug.logMsg(this, "Attempting to parse request parameters.");

		//String format = httpServletRequest.getParameter("filetype");
		String content = httpServletRequest.getParameter("fileContent");
		String filename = httpServletRequest.getParameter("filename");

		// TODO - update antisamy.xml to support svg-xml
		if (httpServletRequest instanceof XssRequestWrapper)
		{
			content = ((XssRequestWrapper) httpServletRequest).getRawParameter("fileContent");
		}

		if (filename == null || filename.length() == 0) {
			filename = DEFAULT_FILENAME;
		}

		httpServletResponse.setContentType("application/octet-stream");
		httpServletResponse.setContentType("application/force-download");
		httpServletResponse.setHeader(
				"content-disposition", "inline; filename='" + filename + "'");
		PrintWriter out = httpServletResponse.getWriter();
		out.write(content);
	}
}
