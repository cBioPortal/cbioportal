package org.mskcc.portal.servlet;

import org.mskcc.portal.openIDlogin.SampleConsumer;
import org.mskcc.portal.util.UserInfo;
import org.openid4java.discovery.Identifier;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Handle an OpenID authentication response.
 * Listens on a URL
 *
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 */
public class HandleOpenIDproviderResp extends HttpServlet {

    /**
     * Handles HTTP Get Request.
     *
     * @param request  HTTP Request.
     * @param response HTTP Response.
     * @throws ServletException Servlet Error.
     * @throws IOException      IO Error.
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        // out.println("<br>request.getRequestURI(): " + request.getRequestURI());
        // out.println("<br>request.getQueryString(): " + request.getQueryString());
        // out.println("<br>OpenIDreturnURL.doGet called.");

        // TODO: should be one for each user
        SampleConsumer mySampleConsumer = SampleConsumer.INSTANCE;
        Identifier verified;
        try {
            verified = mySampleConsumer.verifyResponse(request);

            if (null == mySampleConsumer.authRequestData) {
                out.println("<br>No data available.");
            } else {

                String value;
                for (String k : mySampleConsumer.authRequestData.keySet()) {
                    value = mySampleConsumer.authRequestData.get(k);

                    if (k.equals("emailFromFetch")) {
                        UserInfo.loginUser(value, request);
                    }
                }

                /*
                * STRANGE: CAUSES NullPointerException in mySampleConsumer.verifyResponse() above!
                * email = (String)mySampleConsumer.authRequestData.get( "emailFromFetch" );
                */
            }

			String requestURL = request.getRequestURL().toString();
			String redirectURL = requestURL.substring(0, requestURL.indexOf(request.getServletPath()));

            if (verified != null) {
				// changed from forward to redirect - forward is performed internally by servlet - browser
				// completely undaware so original URL remains intact.  redirect instructs browser to fetch
				// second url.
				// NOTE:
				// not sure what to do with request.setAttribute(QueryBuilder.USER_ERROR_MESSAGE, "You're logged in...");
				// its not reference anywhere, is it just for debugging?
				redirectURL += "/index.do";
				System.out.println("redirecting to: " + redirectURL);
				response.sendRedirect(redirectURL);

            } else {
				// TODO: Later: ACCESS CONTROL: MAKE THIS GO TO login.jsp

				// changed from forward to redirect - forward is performed internally by servlet - browser
				// completely undaware so original URL remains intact.  redirect instructs browser to fetch
				// second url.
				// NOTE:
				// not sure what to do with request.setAttribute(QueryBuilder.USER_ERROR_MESSAGE, "Login failed...");
				// its not reference anywhere, is it just for debugging?
				redirectURL += "/index.do";
				System.out.println("redirecting to: " + redirectURL);
				response.sendRedirect(redirectURL);
            }

        } catch (RuntimeException e) {
            out.println("<br>OpenIDreturnURL.doGet: RuntimeException: " + e.getMessage());
            e.printStackTrace(out);
            out.close();
        }
    }
}