package org.mskcc.cbio.portal.servlet;

import org.json.simple.JSONValue;
import org.mskcc.cbio.portal.dao.DaoSampleProfile;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.util.AccessControl;
import org.mskcc.cbio.portal.util.SpringUtil;
import org.mskcc.cbio.portal.web_api.ProtocolException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * This servlet returns portal meta data on demand.
 *
 * @author Selcuk Onur Sumer
 */
public class PortalMetaData extends HttpServlet
{
	// class which process access control to cancer studies
	private AccessControl accessControl;

	/**
	 * Initializes the servlet.
	 *
	 * @throws javax.servlet.ServletException Serlvet Init Error.
	 */
	public void init() throws ServletException
	{
		super.init();

		ApplicationContext context
				= new ClassPathXmlApplicationContext("classpath:applicationContext-security.xml");

		accessControl = SpringUtil.getAccessControl();
	}

	/**
	 * Handles HTTP GET Request.
	 *
	 * @param httpServletRequest Http Servlet Request Object.
	 * @param httpServletResponse Http Servlet Response Object.
	 * @throws javax.servlet.ServletException Servlet Error.
	 * @throws java.io.IOException IO Error.
	 */
	protected void doGet(HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse) throws ServletException, IOException
	{
		doPost(httpServletRequest, httpServletResponse);
	}

	/**
	 * Handles HTTP POST Request.
	 *
	 * @param httpServletRequest Http Servlet Request Object.
	 * @param httpServletResponse Http Servlet Response Object.
	 * @throws javax.servlet.ServletException Servlet Error.
	 * @throws java.io.IOException IO Error.
	 */
	protected void doPost(HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse) throws ServletException, IOException
	{
		String cancerStudyMetadata = httpServletRequest.getParameter("cancerStudyMetaData");
		String cancerStudyName = httpServletRequest.getParameter("cancerStudyName");

		Map<String, Object> metaData = new HashMap<String, Object>();

		try {
			if (cancerStudyMetadata != null &&
			    !cancerStudyMetadata.equalsIgnoreCase("false"))
			{
				metaData.put("cancerStudyMetaData", DaoSampleProfile.metaData(
					accessControl.getCancerStudies()));
			}

			if (cancerStudyName != null &&
			    !cancerStudyName.equalsIgnoreCase("false"))
			{
				// TODO cancerStudyName
			}
		} catch (DaoException e) {
			throw new ServletException(e);
		} catch (ProtocolException e) {
			throw new ServletException(e);
		}

		httpServletResponse.setContentType("application/json");
		String jsonText = JSONValue.toJSONString(metaData);
		PrintWriter writer = httpServletResponse.getWriter();
		writer.write(jsonText);
		writer.flush();
		writer.close();
	}
}
