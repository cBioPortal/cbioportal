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

package org.mskcc.cbio.portal.servlet;

import org.mskcc.cbio.portal.util.OmaLinkUtil;

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