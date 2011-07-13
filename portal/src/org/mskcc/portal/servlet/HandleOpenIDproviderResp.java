package org.mskcc.portal.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mskcc.portal.openIDlogin.PortalAccessControl;
import org.mskcc.portal.openIDlogin.SampleConsumer;
import org.openid4java.discovery.Identifier;

/**
 * Handle an OpenID authentication response. 
 * Listens on a URL 
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 *
 */
public class HandleOpenIDproviderResp extends HttpServlet {

   public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

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

               if( k.equals("emailFromFetch")){
                  request.getSession().setAttribute( PortalAccessControl.EMAIL, value);
               }
            }
            
            /* 
             * STRANGE: CAUSES NullPointerException in mySampleConsumer.verifyResponse() above!
             * email = (String)mySampleConsumer.authRequestData.get( "emailFromFetch" );
             */
         }

         if (null != verified) {
            request.setAttribute(QueryBuilder.USER_ERROR_MESSAGE, "You're logged in with email id " + 
                     request.getSession().getAttribute( PortalAccessControl.EMAIL ) );
            RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/index.do");
            dispatcher.forward(request, response);

         } else {
            // TODO: Later: ACCESS CONTROL: MAKE THIS GO TO login.jsp
            request.setAttribute(QueryBuilder.USER_ERROR_MESSAGE, "Login failed, please try again.");
            RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/index.do");
            dispatcher.forward(request, response);

         }

      } catch (RuntimeException e) {
         out.println("<br>OpenIDreturnURL.doGet: RuntimeException: " + e.getMessage() );
         e.printStackTrace(out);
         out.close();
      }
   }

   private void goodToGo(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      request.setAttribute(QueryBuilder.USER_ERROR_MESSAGE, "You are now logged in.");
      RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/index.do");
      dispatcher.forward(request, response);
   }
   
}