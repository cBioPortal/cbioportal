package org.mskcc.cbio.portal.servlet;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

import org.json.simple.JSONValue;

/**
 * Servlet class to request information from Mutation Aligner web service.
 *
 * @author Selcuk Onur Sumer
 */
public class MutationAlignerServlet extends HttpServlet {
    public static final String MUTATION_ALIGNER_API =
        "http://mutationaligner.org/api/domains/";
    public static final String MUTATION_ALIGNER_API_SUFFIX = "?metadata=true";
    public static final String MUTATION_ALIGNER_BASE_LINK =
        "http://mutationaligner.org/domains/";

    private static String makeRequest(String pfamAccession) throws IOException {
        StringBuilder urlBuilder = new StringBuilder();

        urlBuilder.append(MUTATION_ALIGNER_API);
        urlBuilder.append(pfamAccession);
        urlBuilder.append(MUTATION_ALIGNER_API_SUFFIX);

        String url = urlBuilder.toString();

        URL aligner = new URL(url);
        URLConnection alignerCxn = aligner.openConnection();

        StringBuilder sb = new StringBuilder();

        // not found!
        if (
            ((HttpURLConnection) alignerCxn).getResponseCode() !=
            HttpURLConnection.HTTP_OK
        ) {
            sb.append("");
        } else {
            BufferedReader in = new BufferedReader(
                new InputStreamReader(alignerCxn.getInputStream())
            );
            String line;

            // Read all
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }

            in.close();
        }

        return sb.toString();
    }

    protected void doGet(
        HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse
    )
        throws ServletException, IOException {
        doPost(httpServletRequest, httpServletResponse);
    }

    protected void doPost(
        HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse
    )
        throws ServletException, IOException {
        String pfamAccession = httpServletRequest.getParameter("pfamAccession");
        String response = makeRequest(pfamAccession);

        httpServletResponse.setContentType("application/json");

        Map<String, Object> data = new HashMap<String, Object>();

        if (response != null && response.length() > 0) {
            data.put(
                "linkToMutationAligner",
                MUTATION_ALIGNER_BASE_LINK + pfamAccession
            );
        }

        PrintWriter out = httpServletResponse.getWriter();
        String jsonText = JSONValue.toJSONString(data);
        out.write(jsonText);
    }
}
