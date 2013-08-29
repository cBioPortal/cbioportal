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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.dao.DaoPdbUniprotResidueMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * PDB Data Servlet designed to provide PDB data for a specific uniprot id.
 *
 * @author Selcuk Onur Sumer
 */
public class PdbDataServlet extends HttpServlet
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
		// final array to be send as JSON
		JSONArray jsonArray = new JSONArray();

		// TODO sanitize id if necessary... and, allow more than one uniprot id?
		String uniprotId = request.getParameter("uniprotId");
		Set<Integer> positions = this.parsePositions(request.getParameter("positions"));

		try
		{
			Map<String, Set<String>> pdbChainMap =
					DaoPdbUniprotResidueMapping.mapToPdbChains(uniprotId);

			for (String pdbId : pdbChainMap.keySet())
			{
				JSONObject pdb = new JSONObject();
				JSONArray chainArray = new JSONArray();

				pdb.put("pdbId", pdbId);

				for (String chainId : pdbChainMap.get(pdbId))
				{
					// get the pdb positions corresponding to the given uniprot positions
					Map<Integer, Integer> positionMap = DaoPdbUniprotResidueMapping.mapToPdbChains(
							uniprotId, positions, pdbId, chainId);

					Integer[] endPositions = DaoPdbUniprotResidueMapping.getEndPositions(
							uniprotId, pdbId, chainId);

					JSONObject chain = new JSONObject();

					chain.put("chainId", chainId);
					chain.put("start", endPositions[0]);
					chain.put("end", endPositions[1]);
					chain.put("positionMap", positionMap);
					chainArray.add(chain);
				}

				pdb.put("chains", chainArray);
				jsonArray.add(pdb);
			}
		}
		catch (DaoException e)
		{
			e.printStackTrace();
		}

		this.writeOutput(response, jsonArray);
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
