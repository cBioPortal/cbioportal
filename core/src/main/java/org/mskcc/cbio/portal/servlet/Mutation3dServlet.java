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
import java.util.HashSet;
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
		String chainId = null;

		// TODO sanitize id if necessary... and, allow more than one uniprot id?
		String uniprotId = request.getParameter("uniprotId");
		Set<Integer> positions = this.parsePositions(request.getParameter("positions"));
		Map<Integer, Integer> chainMap = null;

		try
		{
			Map<String, Set<String>> pdbChainMap =
				DaoPdbUniprotResidueMapping.mapToPdbChains(uniprotId);

			// TODO getting only the first pdb id & the first chain
			if (pdbChainMap.keySet().iterator().hasNext())
			{
				pdbId = pdbChainMap.keySet().iterator().next();

				if (pdbChainMap.get(pdbId).iterator().hasNext())
				{
					chainId = pdbChainMap.get(pdbId).iterator().next();

					chainMap = DaoPdbUniprotResidueMapping.mapToPdbChains(
						uniprotId, positions, pdbId, chainId);
				}
			}
		}
		catch (DaoException e)
		{
			e.printStackTrace();
		}

		// TODO using mutation locations also get locations on the chain
		// see DaoPdbUniprotResidueMapping.mapToPdbChains() -> need to implement this first

		jsonObject.put("pdbId", pdbId);
		jsonObject.put("chainId", chainId);
		jsonObject.put("chainMap", chainMap);

		this.writeOutput(response, jsonObject);
	}

	protected Set<Integer> parsePositions(String positions)
	{
		Set<Integer> set = new HashSet<Integer>();

		// return an empty set if no positions
		if (positions == null ||
		    positions.trim().length() == 0)
		{
			return set;
		}

		// parse and add each position into the set of integers
		for (String position: positions.trim().split("[\\s,]+"))
		{
			Integer pos = null;

			try {
				pos = Integer.parseInt(position);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}

			if (pos != null && !pos.equals(-1))
			{
				set.add(pos);
			}
		}

		return set;
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
