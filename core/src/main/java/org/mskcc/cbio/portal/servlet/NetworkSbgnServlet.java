package org.mskcc.cbio.portal.servlet;

import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.dao.DaoGeneOptimized;
import org.mskcc.cbio.cgds.model.CanonicalGene;

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
import java.util.ArrayList;
import java.util.List;

/**
 * Servlet class to request SBGN directly from cpath web service.
 */
public class NetworkSbgnServlet extends HttpServlet
{
	public final static String HGNC_GENE_PREFIX = "urn:biopax:RelationshipXref:HGNC_";
	public final static String ENTREZ_GENE_PREFIX = "urn:biopax:RelationshipXref:NCBI+GENE_";
	// TODO do not use awabi! use the proper cpath webservice instead
	public final static String CPATH_SERVICE = "http://awabi.cbio.mskcc.org/cpath2/graph";
	//public final static String separator = "\t";
	public final static String NA = "NA";
	public final static String PATHSFROMTO = "PATHSFROMTO";

	private static String makePC2Request(String sourceGenes,
			String targetGenes,
			String method,
			String format,
			String direction,
			Integer limit)
			throws IOException
	{
		StringBuilder urlBuilder = new StringBuilder();

		urlBuilder.append(CPATH_SERVICE);
		urlBuilder.append("?source=").append(sourceGenes);
		urlBuilder.append("&kind=").append(method);
		urlBuilder.append("&format=").append(format);
		urlBuilder.append("&limit=").append(limit);

		if (!direction.equalsIgnoreCase(NA))
		{
			urlBuilder.append("&direction=").append(direction);
		}

		if (method.equals(PATHSFROMTO))
		{
			urlBuilder.append("&target=").append(targetGenes);
		}

		String url = urlBuilder.toString();

		URL pc2 = new URL(url);
		URLConnection pc2cxn = pc2.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(pc2cxn.getInputStream()));

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

	private static ArrayList<String> convert(String[] geneList)
	{
		ArrayList<String> convertedList = new ArrayList<String>();
		DaoGeneOptimized daoGeneOptimized;

		try
		{
			daoGeneOptimized = DaoGeneOptimized.getInstance();

			for(String gene: geneList)
			{
				CanonicalGene cGene = daoGeneOptimized.getGene(gene);
				//convertedList.add(HGNC_GENE_PREFIX + HGNCUtil.getID(gene).replace(":", "%253A"));
				convertedList.add(ENTREZ_GENE_PREFIX.replace("+", "%2B") + cGene.getEntrezGeneId());
			}
		}
		catch (DaoException e)
		{
			 e.printStackTrace();
		}

		return convertedList;
	}

	private static String joinStrings(List<String> strings, String delimiter) {
		String finalString = "";

		for(String s: strings)
			finalString += s + delimiter;

		return finalString.substring(0, finalString.length() - delimiter.length());
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

		String sourceSymbols = httpServletRequest.getParameter(QueryBuilder.GENE_LIST);

		// TODO temporary test values, if there is only one gene pathsbetween returns empty...
		String targetSymbols = "";
		String method = "pathsbetween";
		String format = "sbgn";
		String direction = "NA";
		String limit = "1";

		String[] sourceGeneSet = sourceSymbols.split("\\s");
		String[] targetGeneSet = targetSymbols.split("\\s");

		String sourceGenes = joinStrings(convert(sourceGeneSet), ",");
		String targetGenes = null;

		if (targetSymbols.length() > 0)
		{
			targetGenes = joinStrings(convert(targetGeneSet), ",");
		}

		String xml = makePC2Request(sourceGenes,
		                            targetGenes,
		                            method,
		                            format,
		                            direction,
		                            Integer.parseInt(limit));
		httpServletResponse.setContentType("text/xml;charset=UTF-8");
		out.write(xml);
	}
}
