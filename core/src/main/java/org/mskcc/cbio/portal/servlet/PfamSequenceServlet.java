package org.mskcc.cbio.portal.servlet;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.dao.DaoPfamGraphics;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.mut_diagram.IdMappingService;
import org.mskcc.cbio.portal.mut_diagram.impl.CgdsIdMappingService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Servlet designed to return JSON object of pfam protein sequence features.
 * This servlet does NOT query the actual pfam service, it uses an internal
 * prebuilt cache.
 *
 * @author Selcuk Onur Sumer
 */
public class PfamSequenceServlet extends HttpServlet
{
	private IdMappingService idMappingService;

	public PfamSequenceServlet()
	{
		this.idMappingService = new CgdsIdMappingService(
				DaoGeneOptimized.getInstance());
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

		List<String> uniProtIds = this.idMappingService.getUniProtIds(hugoGeneSymbol);

		// final json string to return
		String jsonString = "";

		// if no uniprot mapping, then try to get only the sequence length
		if (uniProtIds.isEmpty())
		{
			// try to create a dummy sequence data with only length info
			JSONArray dummyData = this.generateDummyData(hugoGeneSymbol);

			// write dummy (or empty) output
			if (dummyData != null)
			{
				this.writeOutput(response, dummyData);
			}
			else
			{
				// last resort: send empty data
				this.writeOutput(response, JSONValue.parse(jsonString));
			}

			return;
		}

		// TODO retrieve data for all uniprot ids, instead of the first one?
		String uniprotId = uniProtIds.get(0);

		DaoPfamGraphics dao = new DaoPfamGraphics();

		try
		{
			jsonString = dao.getPfamGraphics(uniprotId);
		}
		catch (DaoException e)
		{
			e.printStackTrace();
		}

		this.writeOutput(response, JSONValue.parse(jsonString));
	}

	protected JSONArray generateDummyData(String hugoGeneSymbol)
	{
		CanonicalGene gene = DaoGeneOptimized.getInstance().getGene(hugoGeneSymbol);
		JSONArray data = new JSONArray();

		if (gene != null)
		{
			int length = gene.getLength() / 3;

			JSONObject dummy = new JSONObject();
			dummy.put("markups", new JSONArray());
			dummy.put("length", length);
			dummy.put("regions", new JSONArray());
			dummy.put("motifs", new JSONArray());
			dummy.put("metadata", new JSONObject());

			data.add(dummy);
		}

		return data;
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
