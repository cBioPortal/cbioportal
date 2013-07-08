/*
 * Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center
 * has been advised of the possibility of such damage.  See
 * the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */

package org.mskcc.cbio.portal.servlet;

import org.apache.log4j.Logger;
import org.json.simple.JSONValue;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.model.CancerStudy;
import org.mskcc.cbio.cgds.util.AccessControl;
import org.mskcc.cbio.cgds.web_api.ProtocolException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class CrossCancerJSON extends HttpServlet {
    private static Logger logger = Logger.getLogger(CrossCancerJSON.class);

    // class which process access control to cancer studies
    private AccessControl accessControl;

    /**
     * Initializes the servlet.
     *
     * @throws ServletException Serlvet Init Error.
     */
    public void init() throws ServletException {
        super.init();
        ApplicationContext context =
                new ClassPathXmlApplicationContext("classpath:applicationContext-security.xml");
        accessControl = (AccessControl)context.getBean("accessControl");
    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws javax.servlet.ServletException if a servlet-specific error occurs
     * @throws java.io.IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException
    {
        response.setContentType("application/json");
        PrintWriter writer = response.getWriter();

        try {
            List resultsList = new LinkedList();

            //  Cancer All Cancer Studies
            List<CancerStudy> cancerStudiesList = accessControl.getCancerStudies();
            for (CancerStudy cancerStudy : cancerStudiesList) {
                Map cancerMap = new LinkedHashMap();
                resultsList.add(cancerMap);
                cancerMap.put("studyId", cancerStudy.getCancerStudyStableId());

                // TODO: 1) Decide on the caseSetId
                // ...
                // TODO: 2) Calculate alteration statistics
                Map alterations = new LinkedHashMap();
                cancerMap.put("alterations", alterations);
                alterations.put("mutation", 0);
                alterations.put("cna", 0);
                alterations.put("other", 0);
            }

            JSONValue.writeJSONString(resultsList, writer);
        } catch (DaoException e) {
            throw new ServletException(e);
        } catch (ProtocolException e) {
            throw new ServletException(e);
        } finally {
            writer.close();
        }
    }
}
