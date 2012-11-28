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
 */
public class BioGeneServlet extends HttpServlet
{
	public final static String BIO_GENE_SERVICE = "http://cbio.mskcc.org/biogene/retrieve.do";
	//public final static String separator = "\t";
	public final static String NA = "NA";
	public final static String PATHSFROMTO = "PATHSFROMTO";

	private static String makeRequest(String query, String org) throws IOException
	{
		StringBuilder urlBuilder = new StringBuilder();

		urlBuilder.append(BIO_GENE_SERVICE);
		urlBuilder.append("?query=").append(query);
		urlBuilder.append("&org=").append(org);

		String url = urlBuilder.toString();

		URL bioGene = new URL(url);
		URLConnection bioGeneCxn = bioGene.openConnection();
		BufferedReader in = new BufferedReader(
				new InputStreamReader(bioGeneCxn.getInputStream()));

		String line;
		StringBuilder xml = new StringBuilder();

		// Read all
		while((line = in.readLine()) != null)
		{
			xml.append(line);
		}

		in.close();

		return xml.toString();
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
//		String sourceSymbols = httpServletRequest.getParameter("source");
//		String targetSymbols = httpServletRequest.getParameter("target");
//		String method = httpServletRequest.getParameter("kind");
//		String format = httpServletRequest.getParameter("format");
//		String direction = httpServletRequest.getParameter("direction");
//		String limit = httpServletRequest.getParameter("limit");

		String query = httpServletRequest.getParameter("query");
		String org = httpServletRequest.getParameter("org");

		String xml = makeRequest(query, org);
		httpServletResponse.setContentType("text/xml;charset=UTF-8");
		out.write(xml);
	}
}
