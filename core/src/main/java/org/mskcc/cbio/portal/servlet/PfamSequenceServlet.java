/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.servlet;

import org.biojava.nbio.core.sequence.compound.AminoAcidCompound;
import org.biojava.nbio.core.sequence.compound.AminoAcidCompoundSet;
import org.biojava.nbio.core.sequence.loader.UniprotProxySequenceReader;
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
		// TODO sanitize geneSymbol & uniprot id if necessary
		String hugoGeneSymbol = request.getParameter("geneSymbol");
		String uniprotAcc = request.getParameter("uniprotAcc");

		// final json string to return
		String jsonString = this.getPfamGraphics(uniprotAcc);

		// TODO retrieve data for all uniprotAccs, instead of only one?

		// provided uniprotAcc doesn't work, try the gene symbol
		if (hugoGeneSymbol != null &&
		    jsonString == null)
		{
			uniprotAcc = getUniprotAcc(hugoGeneSymbol, response);

			if (uniprotAcc != null)
			{
				jsonString = this.getPfamGraphics(uniprotAcc);
			}
		}

		if (jsonString != null)
		{
			this.writeOutput(response, JSONValue.parse(jsonString));
		}
		else {
		    // else: no PFAM data available for this gene/uniprotAcc, 
		    // so try to create a dummy sequence data with only length info
            JSONArray dummyData = this.generateDummyData(hugoGeneSymbol);

            // write dummy (or empty) output
            if (dummyData != null)
            {
                this.writeOutput(response, dummyData);
            }
            else
            {
                // last resort: send empty data - should not occur...but can if gene length is empty
                // TODO - throw exception instead?
                this.writeOutput(response, JSONValue.parse(""));
            }
		}
	}

	protected String getUniprotAcc(String hugoGeneSymbol,
			HttpServletResponse response) throws IOException
	{
		String uniprotAcc = null;

		List<String> uniProtAccs =
				this.idMappingService.mapFromHugoToUniprotAccessions(hugoGeneSymbol);

		// if no uniprot mapping, then try to get only the sequence length
		if (uniProtAccs.isEmpty())
		{
			return null;
		}
		else
		{
			// TODO longest sequence is not always the desired one.
			// (ex: BRCA1 returns E9PFC7_HUMAN instead of BRCA1_HUMAN)

			uniprotAcc = this.longestAcc(uniProtAccs);
		}

		return uniprotAcc;
	}

	protected String getPfamGraphics(String uniprotAcc)
	{
		String jsonString = null;

		if (uniprotAcc != null &&
		    uniprotAcc.length() > 0)
		{
			DaoPfamGraphics dao = new DaoPfamGraphics();

			try
			{
				jsonString = dao.getPfamGraphics(uniprotAcc);
			}
			catch (DaoException e)
			{
				e.printStackTrace();
			}
		}

		return jsonString;
	}

	/**
	 * Generate dummy pfam data with "length" attribute being the 
	 * gene length/3. If gene length is not set, then this method returns null.
	 * 
	 * @param hugoGeneSymbol : the gene symbol to look for (and get its length).
	 * 
	 * @return returns null if gene length is not filled in DB record.
	 */
	protected JSONArray generateDummyData(String hugoGeneSymbol)
	{
		CanonicalGene gene = DaoGeneOptimized.getInstance().getGene(hugoGeneSymbol);
		JSONArray data = new JSONArray();

		if (gene != null && gene.getLength() > 0)
		{
			int length = gene.getLength() / 3;

			JSONObject dummy = new JSONObject();
			dummy.put("markups", new JSONArray());
			dummy.put("length", length);
			dummy.put("regions", new JSONArray());
			dummy.put("motifs", new JSONArray());
			dummy.put("metadata", new JSONObject());

			data.add(dummy);
	        return data;
		}
		else {
		    return null;
		}

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

	/**
	 * Finds the uniprot accession, within the given list,
	 * corresponding to the longest uniprot sequence.
	 *
	 * @param uniProtAccs   list of uniprot accessions
	 * @return  uniprot accession corresponding to the longest sequence
	 */
	protected String longestAcc(List<String> uniProtAccs)
	{
		String longest = "";
		int max = -1;

		if (uniProtAccs.size() == 1) {
			return uniProtAccs.get(0);
		} else {
			try {
				for (String accession : uniProtAccs) {
					UniprotProxySequenceReader<AminoAcidCompound> uniprotSequence
							= new UniprotProxySequenceReader<AminoAcidCompound>(
							accession,
							AminoAcidCompoundSet.getAminoAcidCompoundSet());

					if (uniprotSequence.getLength() > max) {
						max = uniprotSequence.getLength();
						longest = accession;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			return longest;
		}
	}
}
