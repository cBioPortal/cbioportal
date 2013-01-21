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

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.CollectionType;
import org.codehaus.jackson.map.type.TypeFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mskcc.cbio.cgds.dao.DaoCancerStudy;
import org.mskcc.cbio.cgds.dao.DaoGeneticProfile;
import org.mskcc.cbio.cgds.model.ExtendedMutation;
import org.mskcc.cbio.maf.MafRecord;
import org.mskcc.cbio.portal.html.special_gene.SpecialGene;
import org.mskcc.cbio.portal.html.special_gene.SpecialGeneFactory;
import org.mskcc.cbio.portal.util.ExtendedMutationUtil;
import org.mskcc.cbio.portal.util.OmaLinkUtil;
import org.mskcc.cbio.portal.util.SequenceCenterUtil;
import org.mskcc.cbio.portal.util.SkinUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.codehaus.jackson.map.DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY;

/**
 * A servlet designed to retrieve mutation data for a single mutation details table.
 * This servlet returns a JSON object.
 *
 * @author Selcuk Onur Sumer
 */
public class MutationTableDataServlet extends HttpServlet
{
	private static final Logger logger = Logger.getLogger(MutationTableDataServlet.class);
	private final ObjectMapper objectMapper;

	public MutationTableDataServlet()
	{
		this.objectMapper = new ObjectMapper();
		this.objectMapper.configure(ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{
		this.doPost(request, response);
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{
		String geneSymbol = request.getParameter("hugoGeneSymbol");

		// final object to be send as JSON
		JSONObject jsonObject = new JSONObject();
		JSONArray rows = new JSONArray();

		SpecialGene specialGene = SpecialGeneFactory.getInstance(geneSymbol);
		HashMap<String, Object> headerList = this.initHeaders(specialGene);

		List<ExtendedMutation> mutations = readMutations(request.getParameter("mutations"));

		for (ExtendedMutation mutation : mutations)
		{
			HashMap<String, Object> rowData = new HashMap<String, Object>();

			int cancerStudyId = DaoGeneticProfile.getGeneticProfileById(
					mutation.getGeneticProfileId()).getCancerStudyId();
			String cancerStudyStableId = DaoCancerStudy.getCancerStudyByInternalId(
					cancerStudyId).getCancerStudyStableId();
			String linkToPatientView = SkinUtil.getLinkToPatientView(mutation.getCaseId(),
					cancerStudyStableId);

			rowData.put("caseId", mutation.getCaseId());
			rowData.put("linkToPatientView", linkToPatientView);
			rowData.put("proteinChange", mutation.getProteinChange());
			rowData.put("mutationType", mutation.getMutationType());
			rowData.put("cosmic", mutation.getOncotatorCosmicOverlapping());
			rowData.put("cosmicCount", this.getCosmicCount(mutation));
			rowData.put("functionalImpactScore", mutation.getFunctionalImpactScore());
			rowData.put("msaLink", this.getMsaLink(mutation));
			rowData.put("xVarLink", this.getXVarLink(mutation));
			rowData.put("pdbLink", this.getPdbLink(mutation));
			rowData.put("mutationStatus", mutation.getMutationStatus());
			rowData.put("validationStatus", mutation.getValidationStatus());
			rowData.put("sequencingCenter", this.getSequencingCenter(mutation));
			rowData.put("ncbiBuildNo", this.getNcbiBuild(mutation));
			rowData.put("chr", this.getChromosome(mutation));
			rowData.put("startPos", mutation.getStartPosition());
			rowData.put("endPos", mutation.getEndPosition());
			rowData.put("referenceAllele", mutation.getReferenceAllele());
			rowData.put("variantAllele", this.getVariantAllele(mutation));
			rowData.put("tumorFreq", this.getTumorFreq(mutation));
			rowData.put("normalFreq", this.getNormalFreq(mutation));
			rowData.put("tumorRefCount", this.getTumorRefCount(mutation));
			rowData.put("tumorAltCount", this.getTumorAltCount(mutation));
			rowData.put("normalRefCount", this.getNormalRefCount(mutation));
			rowData.put("normalAltCount", this.getNormalAltCount(mutation));

			JSONArray specialGeneData = new JSONArray();

			//  fields for "Special" genes
			if (specialGene != null)
			{
				for (String field : specialGene.getDataFields(mutation))
				{
					specialGeneData.add(field);
				}
			}

			rowData.put("specialGeneData", specialGeneData);

			rows.add(rowData);
		}

		jsonObject.put("header", headerList);
		jsonObject.put("mutations", rows);
		jsonObject.put("footerMsg", this.getTableFooterMessage(specialGene));
		jsonObject.put("hugoGeneSymbol", geneSymbol);

		response.setContentType("application/json");
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

	/**
	 * Returns the MSA (alignment) link for the given mutation.
	 *
	 * @param mutation  mutation instance
	 * @return          corresponding MSA link
	 */
	protected String getMsaLink(ExtendedMutation mutation)
	{
		String urlMsa = "";

		if (linkIsValid(mutation.getLinkMsa()))
		{
			try
			{
				if(mutation.getLinkMsa().length() == 0 ||
				   mutation.getLinkMsa().equals("NA"))
				{
					urlMsa = "NA";
				}
				else
				{
					urlMsa = OmaLinkUtil.createOmaRedirectLink(mutation.getLinkMsa());
				}
			}
			catch (MalformedURLException e)
			{
				logger.error("Could not parse OMA URL:  " + e.getMessage());
			}
		}

		return urlMsa;
	}

	/**
	 * Returns the PDB (structure) link for the given mutation.
	 *
	 * @param mutation  mutation instance
	 * @return          corresponding PDB link
	 */
	protected String getPdbLink(ExtendedMutation mutation)
	{
		String urlPdb = "";

		if (linkIsValid(mutation.getLinkPdb()))
		{
			try
			{
				if(mutation.getLinkPdb().length() == 0 ||
				   mutation.getLinkPdb().equals("NA"))
				{
					urlPdb = "NA";
				}
				else
				{
					urlPdb = OmaLinkUtil.createOmaRedirectLink(mutation.getLinkPdb());
				}
			}
			catch (MalformedURLException e)
			{
				logger.error("Could not parse OMA URL:  " + e.getMessage());
			}
		}

		return urlPdb;
	}

	/**
	 * Returns the xVar (mutation assessor) link for the given mutation.
	 *
	 * @param mutation  mutation instance
	 * @return          corresponding xVar link
	 */
	protected String getXVarLink(ExtendedMutation mutation)
	{
		String xVarLink = "";

		if (linkIsValid(mutation.getLinkXVar()))
		{
			try
			{
				xVarLink = OmaLinkUtil.createOmaRedirectLink(mutation.getLinkXVar());
			}
			catch (MalformedURLException e)
			{
				logger.error("Could not parse OMA URL:  " + e.getMessage());
				//return HtmlUtil.createEmptySpacer();
			}
		}

		return xVarLink;
	}

	/**
	 * Checks the validity of the given link.
	 *
	 * @param link  string representation of a URL
	 * @return      true if valid, false otherwise
	 */
	protected boolean linkIsValid(String link)
	{
		return link != null &&
		   link.length() > 0 &&
		   !link.equalsIgnoreCase("NA");
	}

	protected String getSequencingCenter(ExtendedMutation mutation)
	{
		return SequenceCenterUtil.getSequencingCenterAbbrev(
				mutation.getSequencingCenter());
	}

	/**
	 * Creates an html "a" element for the cosmic overlapping value
	 * of the given mutation. The text of the element will be the sum
	 * of all cosmic values, and the id of the element will be the
	 * (non-parsed) cosmic overlapping string.
	 *
	 * @param mutation  mutation instance
	 * @return          string representing an "a" element for the cosmic value
	 */
	protected int getCosmicCount(ExtendedMutation mutation)
	{
		if (mutation.getOncotatorCosmicOverlapping() == null ||
		    mutation.getOncotatorCosmicOverlapping().equals("NA"))
		{
			return 0;
		}

		// calculate total cosmic count
		Integer total = ExtendedMutationUtil.calculateCosmicCount(mutation);

		if (total > 0)
		{
			return total;
		}
		else
		{
			return 0;
		}
	}

	/**
	 * Returns one of the tumor sequence alleles which is different from
	 * the reference allele.
	 *
	 * @param mutation  mutation instance
	 * @return          tumor sequence allele different from the reference allele
	 */
	protected String getVariantAllele(ExtendedMutation mutation)
	{
		String varAllele = mutation.getTumorSeqAllele1();

		if (mutation.getReferenceAllele() != null &&
		    mutation.getReferenceAllele().equals(mutation.getTumorSeqAllele1()))
		{
			varAllele = mutation.getTumorSeqAllele2();
		}

		return varAllele;
	}

	/**
	 * Returns the corresponding NCBI build number (hg18 or hg19).
	 *
	 * @param mutation  mutation instance
	 * @return          corresponding NCBI build number
	 */
	protected String getNcbiBuild(ExtendedMutation mutation)
	{
		String build = mutation.getNcbiBuild();

		if (build == null)
		{
			return build;
		}
		if (build.equals("36") ||
		    build.equals("36.1"))
		{
			return "hg18";
		}
		else if (build.equals("37") ||
		         build.equalsIgnoreCase("GRCh37"))
		{
			return "hg19";
		}
		else
		{
			return build;
		}
	}

	/**
	 * Returns the corresponding chromosome.
	 *
	 * @param mutation  mutation instance
	 * @return          chromosome number
	 */
	protected String getChromosome(ExtendedMutation mutation)
	{
		if (mutation.getChr() == null)
		{
			return null;
		}
		else if (mutation.getChr().equals("NA"))
		{
			return "NA";
		}
		else
		{
			return "chr" + mutation.getChr();
		}
	}

	private Integer getNormalAltCount(ExtendedMutation mutation)
	{
		// TODO consider other possible columns
		Integer count = mutation.getNormalAltCount();

		if (count == MafRecord.NA_INT)
		{
			count = null;
		}

		return count;
	}

	private Integer getNormalRefCount(ExtendedMutation mutation)
	{
		// TODO consider other possible columns
		Integer count = mutation.getNormalRefCount();

		if (count == MafRecord.NA_INT)
		{
			count = null;
		}

		return count;
	}

	private Integer getTumorAltCount(ExtendedMutation mutation)
	{
		// TODO consider other possible columns
		Integer count = mutation.getTumorAltCount();

		if (count == MafRecord.NA_INT)
		{
			count = null;
		}

		return count;
	}

	private Integer getTumorRefCount(ExtendedMutation mutation)
	{
		// TODO consider other possible columns
		Integer count = mutation.getTumorRefCount();

		if (count == MafRecord.NA_INT)
		{
			count = null;
		}

		return count;
	}

	private Double getNormalFreq(ExtendedMutation mutation)
	{
		Integer altCount = this.getNormalAltCount(mutation);
		Integer refCount = this.getNormalRefCount(mutation);

		return this.getFreq(altCount, refCount);
	}

	private Double getTumorFreq(ExtendedMutation mutation)
	{
		Integer altCount = this.getTumorAltCount(mutation);
		Integer refCount = this.getTumorRefCount(mutation);

		return this.getFreq(altCount, refCount);
	}

	private Double getFreq(Integer altCount, Integer refCount)
	{
		Double freq;

		if (altCount == null ||
		    refCount == null)
		{
			freq = null;
		}
		else
		{
			freq = altCount.doubleValue() /
			       (altCount.doubleValue() + refCount.doubleValue());
		}

		return freq;
	}

	/**
	 * Gets the footer message specific to the provided special gene.
	 *
	 * @param specialGene   a special gene
	 * @return              corresponding footer message
	 */
	public String getTableFooterMessage(SpecialGene specialGene)
	{
		if (specialGene != null)
		{
			return specialGene.getFooter();
		}
		else
		{
			return "";
		}
	}

	/**
	 * Initializes a map of data column headers.
	 *
	 * @param specialGene   a special gene
	 * @return              a map of variable name and column name pairs
	 */
	protected HashMap<String, Object> initHeaders(SpecialGene specialGene)
	{
		HashMap<String, Object> headerList = new HashMap<String, Object>();

		headerList.put("caseId", "Case ID");
		headerList.put("proteinChange", "AA Change");
		headerList.put("mutationType", "Type");
		headerList.put("cosmic", "COSMIC");
		headerList.put("functionalImpactScore", "FIS");
		headerList.put("pdbLink", "3D");
		headerList.put("mutationStatus", "MS");
		headerList.put("validationStatus", "VS");
		headerList.put("sequencingCenter", "Center");
		headerList.put("ncbiBuildNo", "Build");
		headerList.put("chr", "Chr");
		headerList.put("startPos", "Start Pos");
		headerList.put("endPos", "End Pos");
		headerList.put("referenceAllele", "Ref");
		headerList.put("variantAllele", "Var");
		headerList.put("tumorFreq", "Allele Freq (T)");
		headerList.put("normalFreq", "Allele Freq (N)");
		headerList.put("tumorRefCount", "Var Ref");
		headerList.put("tumorAltCount", "Var Alt");
		headerList.put("normalRefCount", "Norm Ref");
		headerList.put("normalAltCount", "Norm Alt");

		JSONArray specialGeneHeaders = new JSONArray();

		//  Add Any Gene-Specfic Headers
		if (specialGene != null)
		{
			for (String header : specialGene.getDataFieldHeaders())
			{
				specialGeneHeaders.add(header);
			}
		}

		headerList.put("specialGeneHeaders", specialGeneHeaders);

		return headerList;
	}

	/**
	 * Read and return a list of extended mutations from the specified value in JSON format
	 * or an empty list if the value cannot be read.
	 *
	 * @param value list of extended mutations in JSON format
	 * @return a list of extended mutations from the specified value in JSON format, or an
	 *    empty list if the value cannot be read
	 */
	List<ExtendedMutation> readMutations(final String value) {
		List<ExtendedMutation> mutations = Collections.emptyList();
		if (value != null) {
			try {
				TypeFactory typeFactory = objectMapper.getTypeFactory();
				CollectionType sequenceList = typeFactory.constructCollectionType(List.class, ExtendedMutation.class);
				mutations = objectMapper.readValue(value, sequenceList);
			}
			catch (JsonParseException e) {
				logger.warn("could not deserialize extended mutations", e);
			}
			catch (JsonMappingException e) {
				logger.warn("could not deserialize extended mutations", e);
			}
			catch (IOException e) {
				logger.warn("could not deserialize extended mutations", e);
			}
		}
		return mutations;
	}
}
