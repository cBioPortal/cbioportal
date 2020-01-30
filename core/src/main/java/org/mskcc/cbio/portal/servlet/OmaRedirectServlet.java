/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mskcc.cbio.portal.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.mskcc.cbio.portal.util.OmaLinkUtil;

/**
 * Redirects Client to the Online Mutation Assessor (OMA).
 *
 * @author Ethan Cerami.
 */
public class OmaRedirectServlet extends HttpServlet {
    private static Logger logger = Logger.getLogger(OmaRedirectServlet.class);

    protected void doGet(
        HttpServletRequest request,
        HttpServletResponse response
    )
        throws ServletException, IOException {
        response.setContentType("text/html");
        String omaQueryString = request.getQueryString();
        PrintWriter writer = response.getWriter();
        try {
            String omaUrl = OmaLinkUtil.createOmaLink(omaQueryString);
            sendHtmlRedirect(writer, omaUrl);
        } catch (MalformedURLException e) {
            logError(omaQueryString, e);
            sendErrorPage(writer, e);
        }
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
        writer.write(
            "<meta http-equiv=\"refresh\" content=\"0;url=" + omaUrl + "\"/>"
        );
        writer.write("</head>");
        writer.write("</html>");
    }

    private void logError(String urlString, MalformedURLException e) {
        logger.warn(
            "a request to OmaRedirectServlet contained a malformed URL:" +
            urlString
        );
        logger.warn("   exception message: " + e.getMessage());
    }

    private void sendErrorPage(PrintWriter writer, MalformedURLException e) {
        writer.write("<html>");
        writer.write("<head>");
        writer.write("<title>Error during redirect</title>");
        writer.write("</head>");
        writer.write("<body>");
        writer.write(
            "<p>An error occurred during the navigation to the requested link."
        );
        writer.write("&nbsp;<br>&nbsp;");
        writer.write(
            "<p>This error has been recorded in the webserver logs. We appologize for this problem, and we will try to correct it."
        );
        writer.write("</body>");
        writer.write("</html>");
    }
}
