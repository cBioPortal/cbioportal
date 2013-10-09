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
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoPdbUniprotResidueMapping;
import org.mskcc.cbio.portal.model.PdbUniprotAlignment;
import org.mskcc.cbio.portal.model.PdbUniprotResidueMapping;

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
			List<PdbUniprotAlignment> alignments =
					DaoPdbUniprotResidueMapping.getAlignments(uniprotId);

			for (PdbUniprotAlignment alignment : alignments)
			{
				JSONObject alignmentJson = new JSONObject();
				Integer alignmentId = alignment.getAlignmentId();

				alignmentJson.put("alignmentId", alignmentId);
				alignmentJson.put("pdbId", alignment.getPdbId());
				alignmentJson.put("chain", alignment.getChain());
				alignmentJson.put("uniprotId", alignment.getUniprotId());
				alignmentJson.put("pdbFrom", alignment.getPdbFrom());
				alignmentJson.put("pdbTo", alignment.getPdbTo());
				alignmentJson.put("uniprotFrom", alignment.getUniprotFrom());
				alignmentJson.put("uniprotTo", alignment.getUniprotTo());

				// get the pdb positions corresponding to the given uniprot positions
				Map<Integer, PdbUniprotResidueMapping> positionMap =
						DaoPdbUniprotResidueMapping.mapToPdbResidues(
								alignmentId, positions);

				// TODO postpone position mapping until clicking on the pdb chain (except default chain)
				// create a json object for each PdbUniprotResidueMapping in the positionMap
				alignmentJson.put("positionMap", this.positionMap(positionMap));

				// get all positions corresponding to the current alignment
				//List<PdbUniprotResidueMapping> mappingList =
				//		DaoPdbUniprotResidueMapping.getResidueMappings(alignmentId);

				// create a json object for segments with special "match" values
				//alignmentJson.put("segments", this.segmentArray(mappingList));

				alignmentJson.put("alignmentString", this.alignmentString(alignment));

				jsonArray.add(alignmentJson);
			}
		}
		catch (DaoException e)
		{
			e.printStackTrace();
		}

		this.writeOutput(response, jsonArray);
	}

	protected String alignmentString(PdbUniprotAlignment alignment)
	{
		// TODO process 3 alignment strings and create a visualization string
		return alignment.getMidlineAlign();
	}


	// TODO remove unused methods when done...

	protected JSONArray segmentArray(List<PdbUniprotResidueMapping> mappingList)
	{
		Stack<Integer> specialPositions = new Stack<Integer>();
		String lastMatch = "NA";

		JSONArray segments = new JSONArray();

		// assuming the mapping list is sorted by asc uniprot position
		for (PdbUniprotResidueMapping mapping : mappingList)
		{
			String match = mapping.getMatch();
			Integer pos = mapping.getUniprotPos();

			Integer lastPos = specialPositions.empty() ? -1 : specialPositions.peek();

			// check for special characters (empty string & plus sign)
			if (match.length() == 0 ||
			    match.equals("+"))
			{
				boolean adjacent = match.equals(lastMatch) &&
					(pos == lastPos + 1);

				// check for adjacency (which is very unlikely)
				if (!adjacent)
				{
					// create segment by using the stack
					segments.add(this.createSegment(specialPositions, lastMatch));

					// clear stack for the next segment
					specialPositions.clear();
				}

				specialPositions.push(pos);
				lastMatch = match;
			}
		}

		// create the last segment
		if (!specialPositions.empty())
		{
			segments.add(this.createSegment(specialPositions, lastMatch));
		}

		return segments;
	}

	protected JSONObject createSegment(Stack<Integer> positions, String match)
	{
		JSONObject segment = null;

		if (!positions.empty())
		{
			segment = new JSONObject();
			segment.put("end", positions.peek());
			segment.put("start", positions.firstElement());
			segment.put("match", match);
		}

		return segment;
	}

	protected Map<Integer, JSONObject> positionMap(
			Map<Integer, PdbUniprotResidueMapping> positionMap)
	{
		Map<Integer, JSONObject> map = new HashMap<Integer, JSONObject>();

		for (Integer position : positionMap.keySet())
		{
			PdbUniprotResidueMapping mapping = positionMap.get(position);
			JSONObject residueMappingJson = new JSONObject();

			residueMappingJson.put("pdbPos", mapping.getPdbPos());
			residueMappingJson.put("match", mapping.getMatch());

			map.put(position, residueMappingJson);
		}

		return map;
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
	protected JSONArray getSegmentArray(List<Integer> positions) throws DaoException
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
