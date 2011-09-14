package org.mskcc.portal.servlet;

import org.mskcc.portal.util.OmaLinkUtil;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Redirects Client to the Online Mutation Assessor (OMA).
 *
 * @author Ethan Cerami.
 */
public class OmaRedirectServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html");
        String omaQueryString = request.getQueryString();
        String omaUrl = OmaLinkUtil.createOmaLink(omaQueryString);
        PrintWriter writer = response.getWriter();
        sendHtmlRedirect(writer, omaUrl);
        writer.close();
    }

    /**
     * We use an HTML meta Refresh Redirect, because this strips out the HTTP Referer
     * header, which can cause problems with the OMA web site.
     *
     */
    private void sendHtmlRedirect(PrintWriter writer, String omaUrl) {
        writer.write("<html>");
        writer.write("<head>");
        writer.write("<meta http-equiv=\"refresh\" content=\"0;url=" + omaUrl + "\"/>");
        writer.write("</head>");
        writer.write("</html>");
    }
}