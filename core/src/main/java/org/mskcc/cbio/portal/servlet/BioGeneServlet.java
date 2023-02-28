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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;

/**
 * Servlet class to request BioGene information from BioGene web service.
 *
 * @author Selcuk Onur Sumer
 */
public class BioGeneServlet extends HttpServlet
{
	public final static String BIO_GENE_SERVICE = "https://biogene.herokuapp.com/retrieve.do";
	public final static String NA = "NA";

	private static String makeRequest(String query,
			String org,
			String format) throws IOException
	{
		StringBuilder urlBuilder = new StringBuilder();

		urlBuilder.append(BIO_GENE_SERVICE);
		urlBuilder.append("?query=").append(query);
		urlBuilder.append("&org=").append(org);
		urlBuilder.append("&format=").append(format);

		String url = urlBuilder.toString();

		URL bioGene = new URL(url);
		URLConnection bioGeneCxn = bioGene.openConnection();
		BufferedReader in = new BufferedReader(
				new InputStreamReader(bioGeneCxn.getInputStream()));

		String line;
		StringBuilder sb = new StringBuilder();

		// Read all
		while((line = in.readLine()) != null)
		{
			sb.append(line);
		}

		in.close();

		return sb.toString();
	}


	protected void doGet(HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse)throws ServletException, IOException
	{
		doPost(httpServletRequest, httpServletResponse);
	}

	protected void doPost(HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse)throws ServletException, IOException
	{
		PrintWriter out = httpServletResponse.getWriter();

		String query = httpServletRequest.getParameter("query");
		String org = httpServletRequest.getParameter("org");
		String format = httpServletRequest.getParameter("format");

		String xml = makeRequest(query, org, format);

		if (format.equalsIgnoreCase("json"))
		{
			httpServletResponse.setContentType("application/json");
		}
		else
		{
			httpServletResponse.setContentType("text/xml;charset=UTF-8");
		}

		out.write(xml);
	}
}
