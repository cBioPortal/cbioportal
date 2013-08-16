package org.mskcc.cbio.portal.servlet;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.dao.DaoGeneOptimized;
import org.mskcc.cbio.cgds.dao.DaoPdbUniprotResidueMapping;
import org.mskcc.cbio.portal.mut_diagram.IdMappingService;
import org.mskcc.cbio.portal.mut_diagram.impl.CgdsIdMappingService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class Mutation3dServlet extends HttpServlet
{
	@Override
	protected void doGet(final HttpServletRequest request,
			final HttpServletResponse response)
			throws ServletException, IOException
	{
		this.doPost(request, response);
	}

	@Override
	protected void doPost(final HttpServletRequest request,
			final HttpServletResponse response)
			throws ServletException, IOException
	{
		// final object to be send as JSON
		JSONObject jsonObject = new JSONObject();

		String pdbId = null;

		// TODO sanitize id if necessary... and, allow more than one uniprot id?
		String uniprotId = request.getParameter("uniprotId");

		try
		{
			Map<String, Set<String>> pdbChainMap =
				DaoPdbUniprotResidueMapping.mapToPdbChains(uniprotId);

			// TODO getting only the first pdb id
			if (pdbChainMap.keySet().iterator().hasNext())
			{
				pdbId = pdbChainMap.keySet().iterator().next();
			}
		}
		catch (DaoException e)
		{
			e.printStackTrace();
		}

		// TODO using mutation locations also get locations on the chain
		// see DaoPdbUniprotResidueMapping.mapToPdbChains() -> need to implement this first

		jsonObject.put("pdbId", pdbId);

		this.writeOutput(response, jsonObject);
	}

	protected void writeOutput(HttpServletResponse response,
			Object value) throws IOException
	{
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();

		try
		{
			JSONValue.writeJSONString(value, out);
		}
		finally
		{
			out.close();
		}
	}
}
