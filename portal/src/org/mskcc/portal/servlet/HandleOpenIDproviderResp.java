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

            if (verified != null) {
                request.setAttribute(QueryBuilder.USER_ERROR_MESSAGE, "You are logged in with id: " +
                        UserInfo.getEmailId(request));
                RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/index.do");
                dispatcher.forward(request, response);

            } else {
                // TODO: Later: ACCESS CONTROL: MAKE THIS GO TO login.jsp
                request.setAttribute(QueryBuilder.USER_ERROR_MESSAGE, "Login failed, please try again.");
                RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/index.do");
                dispatcher.forward(request, response);

            }

        } catch (RuntimeException e) {
            out.println("<br>OpenIDreturnURL.doGet: RuntimeException: " + e.getMessage());
            e.printStackTrace(out);
            out.close();
        }
    }
}