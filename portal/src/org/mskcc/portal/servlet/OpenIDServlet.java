package org.mskcc.portal.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mskcc.portal.openIDlogin.PortalAccessControl;
import org.mskcc.portal.openIDlogin.SampleConsumer;
import org.mskcc.portal.util.UserInfo;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.discovery.DiscoveryException;

/**
 * This documents the OpenID login:
 * login.jsp presents the login GUI implemented by openid-jquery.js
 * it calls OpenIDServlet.doPost here.
 * doPost sets handleOpenIDproviderResp as a handler for the authentication response 
 * if the login succeeds, HandleOpenIDproviderResp.doGet stores the user's email
 * returned by the response as their EMAIL attribute in their HttpSession.
 * (see OpenID documentation at 
 * http://openid.net/specs/openid-authentication-2_0.html,
 * http://andrewjstevens.com/2008/02/openid-authentication-protocol/ and
 * http://news.cnet.com/8301-17939_109-10078467-2.html)
 * 
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 *
 */
public class OpenIDServlet extends HttpServlet {

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		String OpenID = request.getParameter("openid_identifier");
		if (OpenID == null || OpenID.length() == 0 ) {
			out.println("No OpenID entered.");
			// TODO later ACCESS: change this URL 
			out.println("<P><A HREF=../test/tryOpenID.html>Try another OpenID</A>");
			out.close();
		} else {
			out.println("<p>OpenID is: " + OpenID );
			try {
				SampleConsumer mySampleConsumer = SampleConsumer.INSTANCE;

				// grabs host, port, and path to servlet via request object
				String requestURL = request.getRequestURL().toString();
				String handlerURL = (requestURL.substring(0, requestURL.indexOf(request.getServletPath())) +
									 "/handleOpenIDproviderResp.do");
				mySampleConsumer.setHandlerURL(handlerURL);
				UserInfo.logoutUser(request);
			 
				mySampleConsumer.authRequest(OpenID, request, response, out);
				// out.println("<br>request.getRequestURI(): " + request.getRequestURI() );
				// out.println("<br>request.getQueryString(): " + request.getQueryString() );

			} catch (ConsumerException e) {
				out.println("<P>ConsumerException: " + e.getMessage());
				e.printStackTrace(out);
			} catch (DiscoveryException e) {
				out.println("<P>Debugging: DiscoveryException: " + e.getMessage());
				out.println("<P>Invalid OpenID: " + OpenID);
			}

			// TODO ACCESS: change this URL 
			out.println("<P><A HREF=../test/tryOpenID.html>Try another OpenID</A>");
			out.close();
		}
	}
}
