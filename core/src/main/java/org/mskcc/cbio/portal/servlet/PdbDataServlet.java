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
import java.util.*;

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

					// Positions are not continuous, so exclude gaps, create segments
					// chain.segments -> [{start: x1, end:y1}, ...]
					JSONArray segments = this.segmentArray(
						DaoPdbUniprotResidueMapping.getAllPositions(
							uniprotId, pdbId, chainId));

					JSONObject chain = new JSONObject();

					chain.put("chainId", chainId);
					chain.put("segments", segments);
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

	/**
	 * Creates a JSONArray of segment objects for the given positions.
	 * Each segment object has two fields: {start, end}.
	 *
	 * If positions list is not empty, this function will return a JSONArray
	 * containing at least one segment.
	 *
	 * @param positions a list of uniprot positions
	 * @return  an array of segment objects
	 * @throws DaoException
	 */
	protected JSONArray segmentArray(List<Integer> positions) throws DaoException
	{
		JSONArray segments = new JSONArray();

		Integer start = null;
		Integer end = null;
		HashMap<String, Integer> segment;

		// iterate all positions to determine segment start and end
		for (Integer pos : positions)
		{
			if (start == null)
			{
				// init start & end
				start = pos;
				end = pos;
			}
			// a distance greater than 1 means a gap
			else if (pos - end > 1)
			{
				// add a new segment
				segment = new HashMap<String, Integer>();
				segment.put("start", start);
				segment.put("end", end);
				segments.add(segment);

				// reset start & end
				start = pos;
				end = pos;
			}
			else
			{
				// update end position
				end = pos;
			}
		}

		// add the last segment
		if (start != null)
		{
			segment = new HashMap<String, Integer>();
			segment.put("start", start);
			segment.put("end", end);
			segments.add(segment);
		}

		return segments;
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
