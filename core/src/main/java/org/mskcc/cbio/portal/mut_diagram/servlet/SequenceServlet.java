/*
 * Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center
 * has been advised of the possibility of such damage.  See
 * the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */

package org.mskcc.cbio.portal.mut_diagram.servlet;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mskcc.cbio.cgds.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.mut_diagram.FeatureService;
import org.mskcc.cbio.portal.mut_diagram.IdMappingService;
import org.mskcc.cbio.portal.mut_diagram.Region;
import org.mskcc.cbio.portal.mut_diagram.Sequence;
import org.mskcc.cbio.portal.mut_diagram.impl.CacheFeatureService;
import org.mskcc.cbio.portal.mut_diagram.impl.CgdsIdMappingService;
import org.mskcc.cbio.portal.mut_diagram.impl.PfamGraphicsCacheLoader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.io.IOException;

import static org.codehaus.jackson.map.DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY;

/**
 * Servlet designed to return JSON object of protein sequence features.
 *
 * @author Selcuk Onur Sumer
 */
public class SequenceServlet extends HttpServlet
{
	private static final Logger logger = Logger.getLogger(SequenceServlet.class);

	private final ObjectMapper objectMapper;
	private final FeatureService featureService;
	private final IdMappingService idMappingService;

	public SequenceServlet()
	{
		objectMapper = new ObjectMapper();
		objectMapper.configure(ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

		PfamGraphicsCacheLoader cacheLoader = new PfamGraphicsCacheLoader(objectMapper);
		featureService = new CacheFeatureService(cacheLoader);

		idMappingService = new CgdsIdMappingService(DaoGeneOptimized.getInstance());
	}

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
		// TODO sanitize geneSymbol if necessary
		String hugoGeneSymbol = request.getParameter("geneSymbol");

		List<String> uniProtIds = idMappingService.getUniProtIds(hugoGeneSymbol);

		// final json object to return
		JSONObject jsonObject = new JSONObject();

		if (uniProtIds.isEmpty())
		{
			this.writeOutput(response, jsonObject);
			return;
		}

		String uniProtId = uniProtIds.get(0);
		List<Sequence> sequences = featureService.getFeatures(uniProtId);

		JSONArray regions = new JSONArray();
		Object sequenceLength = "";
		Object description = "";
		Object identifier = "";

		if (!sequences.isEmpty()) {
			Sequence sequence = sequences.get(0);
			sequenceLength = sequence.getLength();

			if (sequence.getMetadata() != null) {
				description = sequence.getMetadata().get("description");
				identifier = sequence.getMetadata().get("identifier");
			}

			for (Region region: sequence.getRegions())
			{
				JSONObject regionObject = new JSONObject();

				regionObject.put("color", region.getColour());
				regionObject.put("start", region.getStart());
				regionObject.put("end", region.getEnd());
				regionObject.put("text", region.getText());
				regionObject.put("identifier", region.getMetadata().get("identifier"));
				regionObject.put("type", region.getMetadata().get("type"));
				regionObject.put("description", region.getMetadata().get("description"));

				regions.add(regionObject);
			}
		}

		jsonObject.put("geneSymbol", hugoGeneSymbol);
		jsonObject.put("uniprotId", uniProtId);
		jsonObject.put("identifier", identifier);
		jsonObject.put("sequenceLength", sequenceLength);
		jsonObject.put("description", description);
		jsonObject.put("regions", regions);

		response.setContentType("application/json");

		this.writeOutput(response, jsonObject);

	}

	protected void writeOutput(HttpServletResponse response,
			JSONObject jsonObject) throws IOException
	{
		PrintWriter out = response.getWriter();

		try
		{
			JSONValue.writeJSONString(jsonObject, out);
		}
		finally
		{
			out.close();
		}
	}
}
