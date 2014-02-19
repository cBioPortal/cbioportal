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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/**
 * PDB Data Servlet designed to provide PDB data.
 *
 * This servlet is designed to be queried with 3 different set of parameters,
 * and for each set the servlet returns different information:
 * 1) {uniprotAcc} : returns a list of pdb alignments for the given uniprot accession (JSON array)
 * 2) {positions, alignments} : returns a uniprotPos->pdbPos mapping (JSON object)
 * 3) {pdbIds} : returns basic information about the given PDB ids (JSON object)
 *
 * @author Selcuk Onur Sumer
 */
public class PdbDataServlet extends HttpServlet
{
	public static String PDB_DATA_SERVICE = "http://www.rcsb.org/pdb/files/";

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
		// TODO sanitize acc if necessary... and, allow more than one uniprot accession?
		String uniprotAcc = request.getParameter("uniprotAcc");
		String type = request.getParameter("type");

		Set<Integer> positions = this.parseIntValues(request.getParameter("positions"));
		Set<Integer> alignments = this.parseIntValues(request.getParameter("alignments"));
		Set<String> pdbIds = this.parseStringValues(request.getParameter("pdbIds"));

		try
		{
			if (positions != null &&
			    alignments != null)
			{
				JSONObject positionData = this.getPositionMap(alignments, positions);
				this.writeOutput(response, positionData);
			}
			else if (pdbIds != null)
			{
				JSONObject pdbInfo = this.getPdbInfo(pdbIds);
				this.writeOutput(response, pdbInfo);
			}
			else if (type != null &&
			         type.equals("summary"))
			{
				JSONObject summary = this.getAlignmentSummary(uniprotAcc);
				this.writeOutput(response, summary);
			}
			else
			{
				// write back array of alignments for this uniprot accession
				JSONArray alignmentData = this.getAlignmentArray(uniprotAcc);
				this.writeOutput(response, alignmentData);
			}
		}
		catch (DaoException e)
		{
			e.printStackTrace();
			this.writeOutput(response, null);
		}
	}

	/**
	 * Retrieves PDB info data from the service
	 * for each pdb id in the given set.
	 *
	 * @param pdbIds    a set of PDB ids
	 * @return  a map of info string keyed on pdb id
	 */
	protected JSONObject getPdbInfo(Set<String> pdbIds)
	{
		JSONObject infoMap = new JSONObject();

		for (String pdbId: pdbIds)
		{
			try
			{
				String info = this.makeRequest(pdbId);
				infoMap.put(pdbId, this.parsePdbInfo(info));
			}
			catch (IOException e)
			{
				// unable to retrieve pdb info
				infoMap.put(pdbId, null);
			}
		}

		return infoMap;
	}

	/**
	 * Parses the raw PDB info returned by the service, and creates
	 * a human readable info string.
	 *
	 * @param rawInfo   raw data retrieved from the service
	 * @return  a human readable info string
	 */
	protected String parsePdbInfo(String rawInfo)
	{
		String[] lines = rawInfo.split("\n");
		StringBuilder sb = new StringBuilder();

		// count for the number of "TITLE" lines
		Integer count = 0;

		for (String line: lines)
		{
			count++;

			String str = line;

			// if there is more than one "TITLE" lines,
			// than the line starts with the line number
			// we should get rid of the line number as well
			if (count > 1)
			{
				str = str.replace(count.toString(), "");
			}

			// get rid of the "TITLE"
			str = str.toLowerCase().replaceAll("title", "").trim();
			sb.append(str);

			// whether to add a space at the end or not
			if (!str.endsWith("-"))
			{
				sb.append(" ");
			}
		}

		return sb.toString().trim();
	}

	/**
	 * Makes a request to the PDB info service.
	 *
	 * @param pdbId a specific PDB id
	 * @return      pdb info retrieved from PDB service
	 * @throws IOException
	 */
	protected String makeRequest(String pdbId) throws IOException
	{
		StringBuilder urlBuilder = new StringBuilder();

		urlBuilder.append(PDB_DATA_SERVICE);
		urlBuilder.append(pdbId.toUpperCase()).append(".pdb");
		urlBuilder.append("?headerOnly=").append("YES");

		String url = urlBuilder.toString();

		URL pdbService = new URL(url);
		URLConnection pdbCxn = pdbService.openConnection();
		BufferedReader in = new BufferedReader(
				new InputStreamReader(pdbCxn.getInputStream()));

		String line;
		StringBuilder sb = new StringBuilder();

		// TODO break the loop after "TITLE" lines?
		while((line = in.readLine()) != null)
		{
			// only append "TITLE" lines
			if (line.toLowerCase().startsWith("title"))
			{
				sb.append(line).append("\n");
			}
		}

		// Read all
//		while((line = in.readLine()) != null)
//		{
//			sb.append(line);
//		}

		in.close();

		return sb.toString();
	}

	protected JSONArray getAlignmentArray(String uniprotAcc) throws DaoException
	{
		JSONArray alignmentArray = new JSONArray();

		List<PdbUniprotAlignment> alignments =
				DaoPdbUniprotResidueMapping.getAlignments(uniprotAcc);

		for (PdbUniprotAlignment alignment : alignments)
		{
			JSONObject alignmentJson = new JSONObject();
			Integer alignmentId = alignment.getAlignmentId();

			alignmentJson.put("alignmentId", alignmentId);
			alignmentJson.put("pdbId", alignment.getPdbId());
			alignmentJson.put("chain", alignment.getChain());
			alignmentJson.put("uniprotAcc", alignment.getUniprotAcc());
			alignmentJson.put("pdbFrom", alignment.getPdbFrom());
			alignmentJson.put("pdbTo", alignment.getPdbTo());
			alignmentJson.put("uniprotFrom", alignment.getUniprotFrom());
			alignmentJson.put("uniprotTo", alignment.getUniprotTo());
			alignmentJson.put("eValue", alignment.getEValue());
			alignmentJson.put("identityPerc", alignment.getIdentityPerc());
			alignmentJson.put("alignmentString", this.alignmentString(alignment));

			alignmentArray.add(alignmentJson);
		}

		return alignmentArray;
	}

	protected JSONObject getAlignmentSummary(String uniprotAcc) throws DaoException
	{
		Integer count = DaoPdbUniprotResidueMapping.getAlignmentCount(uniprotAcc);

		JSONObject summary = new JSONObject();
		summary.put("alignmentCount", count);

		return summary;
	}

	protected JSONObject getPositionMap(Set<Integer> alignments,
			Set<Integer> positions) throws DaoException
	{
		Map<Integer, PdbUniprotResidueMapping> positionMap =
				new HashMap<Integer, PdbUniprotResidueMapping>();

		for (Integer alignmentId : alignments)
		{
			// get the pdb positions corresponding to the given uniprot positions
			positionMap.putAll(DaoPdbUniprotResidueMapping.mapToPdbResidues(alignmentId, positions));
		}

		// create a json object for each PdbUniprotResidueMapping in the positionMap
		JSONObject positionJson = new JSONObject();
		positionJson.put("positionMap", this.positionMap(positionMap));

		return positionJson;

		// get all positions corresponding to the current alignment
		//List<PdbUniprotResidueMapping> mappingList =
		//		DaoPdbUniprotResidueMapping.getResidueMappings(alignmentId);

		// create a json object for segments with special "match" values
		//alignmentJson.put("segments", this.segmentArray(mappingList));
	}

	protected String alignmentString(PdbUniprotAlignment alignment)
	{
		StringBuilder sb = new StringBuilder();

		// process 3 alignment strings and create a visualization string
		String midline = alignment.getMidlineAlign();
		String uniprot = alignment.getUniprotAlign();
		String pdb = alignment.getPdbAlign();

		if (midline.length() == uniprot.length() &&
		    midline.length() == pdb.length())
		{
			for (int i = 0; i < midline.length(); i++)
			{
				// do not append anything if there is a gap in uniprot alignment
				if (uniprot.charAt(i) != '-')
				{
					if (pdb.charAt(i) == '-')
					{
						sb.append('-');
					}
					else
					{
						sb.append(midline.charAt(i));
					}
				}
			}
		}
		else
		{
			// the execution should never reach here,
			// if everything is OK with the data...
			sb.append("NA");
		}

		return sb.toString();
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

	// TODO it might be better to extract parse functions to a utility class

	protected Set<String> parseStringValues(String values)
	{
		Set<String> set = new HashSet<String>();

		// return an empty set if no positions
		if (values == null ||
		    values.trim().length() == 0)
		{
			return null;
		}

		// parse and add each position into the set of integers
		for (String value: values.trim().split("[\\s,]+"))
		{
			if (value != null &&
			    value.length() > 0)
			{
				set.add(value);
			}
		}

		return set;
	}


	protected Set<Integer> parseIntValues(String values)
	{
		Set<Integer> set = new HashSet<Integer>();

		// return an empty set if no positions
		if (values == null ||
		    values.trim().length() == 0)
		{
			return null;
		}

		// parse and add each position into the set of integers
		for (String value: values.trim().split("[\\s,]+"))
		{
			Integer val = null;

			try {
				val = Integer.parseInt(value);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}

			// TODO do not exclude -1 values?
			if (val != null &&
			    !val.equals(-1))
			{
				set.add(val);
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
