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

import org.mskcc.cbio.portal.model.DownloadLink;
import org.mskcc.cbio.portal.util.XDebug;
import org.owasp.validator.html.PolicyException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Shows Content Already Retrieved from the CGDS Server.
 */
public class ShowData extends HttpServlet {
    public static final String INDEX = "index";

    /**
     * Handles HTTP GET Request.
     *
     * @param request  Http Servlet Request Object.
     * @param response Http Servlet Response Object.
     * @throws javax.servlet.ServletException Servlet Error.
     * @throws java.io.IOException            IO Error.
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        XDebug xdebug = new XDebug( request );
        showData(getServletContext(), request, response, xdebug);
    }

    private static void showData(ServletContext servletContext, HttpServletRequest request,
                                 HttpServletResponse response, XDebug xdebug)
        throws ServletException, IOException {
        String index = request.getParameter(INDEX);
        if (index == null || index.trim().length() == 0) {
            forwardToErrorPage(servletContext, request, response, xdebug);
        } else {
            try {
                int i = Integer.parseInt(index);
                showDataAtSpecifiedIndex(servletContext, request, response, i, xdebug);
            } catch (NumberFormatException e) {
                forwardToErrorPage(servletContext, request, response, xdebug);
            }
        }
    }

    /**
     * Shows data at specified index value.
     *
     * @param servletContext ServletContext Object.
     * @param request        Servlet Request.
     * @param response       Servlet Response.
     * @param i              Index i.
     * @throws IOException      IO Error.
     * @throws ServletException Servlet Error.
     */
    public static void showDataAtSpecifiedIndex(ServletContext servletContext,
                                                HttpServletRequest request, HttpServletResponse response, int i,
                                                XDebug xdebug)
        throws IOException, ServletException {
        ArrayList<DownloadLink> downloadLinkList = (ArrayList<DownloadLink>)
            request.getSession().getAttribute(QueryBuilder.DOWNLOAD_LINKS);
        if (downloadLinkList == null || downloadLinkList.size() == 0) {
            xdebug.logMsg(xdebug, "Download link list is null or empty");
            forwardToErrorPage(servletContext, request, response, xdebug);
        } else {
            try {
                DownloadLink downloadLink = downloadLinkList.get(i);
                PrintWriter writer = response.getWriter();
                response.setContentType("text/plain");
                response.setHeader("Content-Disposition", "attachment;filename=cBioPortal_data.txt");
                String transposeStr = request.getParameter(
                    QueryBuilder.CLIENT_TRANSPOSE_MATRIX);
                boolean transpose = false;
                if (transposeStr != null) {
                    transpose = true;
                }
                outputData(downloadLink.getContent(), writer, transpose, xdebug);
                writer.flush();
            } catch (ArrayIndexOutOfBoundsException e) {
                xdebug.logMsg(xdebug, "Array Index out of bounds:  " + e.getMessage());
                forwardToErrorPage(servletContext, request, response, xdebug);
            }
        }
    }

    private static void outputData(String content, PrintWriter writer, boolean transpose,
                                   XDebug xdebug) {
        String lines[] = content.split("\n");

        int numRows = 0;
        int numCols = 0;
        ArrayList<String> dataLines = new ArrayList<String>();
        for (String line : lines) {
            if (line.startsWith("#")) {
                writer.write(line + "\n");
            } else {
                dataLines.add(line.trim());
                numRows++;
                if (numRows == 1) {
                    String parts[] = line.split("\\s");
                    numCols = parts.length;
                }
            }
        }
        xdebug.logMsg(xdebug, "Data matrix is size:  " + numRows + " x " + numCols + ".");
        String matrix[][] = new String[numRows][numCols];
        for (int i = 0; i < dataLines.size(); i++) {
            String line = dataLines.get(i);
            String parts[] = line.split("\t");
            for (int j = 0; j < parts.length; j++) {
                matrix[i][j] = parts[j];
            }
        }

        if (!transpose) {
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numCols; j++) {
                    writer.write(matrix[i][j] + "\t");
                }
                writer.write("\n");
            }
        } else {
            for (int i = 0; i < numCols; i++) {
                for (int j = 0; j < numRows; j++) {
                    writer.write(matrix[j][i] + "\t");
                }
                writer.write("\n");
            }
        }

    }

    private static void forwardToErrorPage(ServletContext servletContext,
                                           HttpServletRequest request, HttpServletResponse response, XDebug xdebug)
        throws ServletException, IOException {
        request.setAttribute("xdebug_object", xdebug);
        RequestDispatcher dispatcher =
            servletContext.getRequestDispatcher("/WEB-INF/jsp/error.jsp");
        dispatcher.forward(request, response);
    }
}