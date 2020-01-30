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
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONValue;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoSampleProfile;
import org.mskcc.cbio.portal.util.AccessControl;
import org.mskcc.cbio.portal.util.SpringUtil;
import org.mskcc.cbio.portal.web_api.ProtocolException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * This servlet returns portal meta data on demand.
 *
 * @author Selcuk Onur Sumer
 */
public class PortalMetaData extends HttpServlet {
    // class which process access control to cancer studies
    private AccessControl accessControl;

    /**
     * Initializes the servlet.
     *
     * @throws javax.servlet.ServletException Serlvet Init Error.
     */
    public void init() throws ServletException {
        super.init();

        ApplicationContext context = new ClassPathXmlApplicationContext(
            "classpath:applicationContext-security.xml"
        );

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
    protected void doGet(
        HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse
    )
        throws ServletException, IOException {
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
    protected void doPost(
        HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse
    )
        throws ServletException, IOException {
        String cancerStudyMetadata = httpServletRequest.getParameter(
            "cancerStudyMetaData"
        );
        String cancerStudyName = httpServletRequest.getParameter(
            "cancerStudyName"
        );

        Map<String, Object> metaData = new HashMap<String, Object>();

        try {
            if (
                cancerStudyMetadata != null &&
                !cancerStudyMetadata.equalsIgnoreCase("false")
            ) {
                metaData.put(
                    "cancerStudyMetaData",
                    DaoSampleProfile.metaData(accessControl.getCancerStudies())
                );
            }

            if (
                cancerStudyName != null &&
                !cancerStudyName.equalsIgnoreCase("false")
            ) {
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
