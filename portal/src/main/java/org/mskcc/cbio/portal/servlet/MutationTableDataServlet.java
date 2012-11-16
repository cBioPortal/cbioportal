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
import org.mskcc.cbio.cgds.model.ExtendedMutation;
import org.mskcc.cbio.portal.html.special_gene.SpecialGene;
import org.mskcc.cbio.portal.html.special_gene.SpecialGeneFactory;
import org.mskcc.cbio.portal.util.ExtendedMutationUtil;
import org.mskcc.cbio.portal.util.SequenceCenterUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.codehaus.jackson.map.DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY;

/**
 *
 */
public class MutationTableDataServlet extends HttpServlet
{
	private static final Logger logger = Logger.getLogger(MutationTableDataServlet.class);

	//protected SpecialGene specialGene;
	//protected HashMap<String, Object> headerList;

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

			rowData.put("caseId", mutation.getCaseId());
			rowData.put("proteinChange", mutation.getProteinChange());
			rowData.put("mutationType", mutation.getMutationType());
			rowData.put("cosmic", mutation.getOncotatorCosmicOverlapping());
			rowData.put("cosmicCount", this.getCosmicCount(mutation));
			rowData.put("functionalImpactScore", "TODO (OMA)"); // TODO oma
			rowData.put("pdbLink", "TODO (OMA)"); // TODO oma
			rowData.put("mutationStatus", mutation.getMutationStatus());
			rowData.put("validationStatus", mutation.getValidationStatus());
			rowData.put("sequencingCenter", this.getSequencingCenter(mutation));
			rowData.put("ncbiBuildNo", this.getNcbiBuild(mutation));
			rowData.put("position", this.getChrPosition(mutation));
			rowData.put("referenceAllele", mutation.getReferenceAllele());
			rowData.put("variantAllele", this.getVariantAllele(mutation));

			//  fields for "Special" genes
			if (specialGene != null)
			{
				JSONArray specialGeneData = new JSONArray();

				for (String field : specialGene.getDataFields(mutation))
				{
					specialGeneData.add(field);
				}

				rowData.put("specialGeneData", specialGeneData);
			}


			rows.add(rowData);
		}

		jsonObject.put("header", headerList);
		jsonObject.put("mutations", rows);
		jsonObject.put("footerMsg", this.getTableFooterMessage(specialGene));
		jsonObject.put("hugoGeneSymbol", "geneSymbol");

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
			return -1;
		}

		// calculate total cosmic count
		Integer total = ExtendedMutationUtil.calculateCosmicCount(mutation);

		if (total > 0)
		{
			return total;
		}
		else
		{
			return -1;
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

	protected String getChrPosition(ExtendedMutation mutation)
	{
		if (mutation.getChr() == null)
		{
			return null;
		}
		else
		{
			return "chr" + mutation.getChr() + ":" + mutation.getStartPosition();
		}
	}

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
		headerList.put("position", "Position");
		headerList.put("referenceAllele", "Ref");
		headerList.put("variantAllele", "Var");
		//TODO headerList.add("Variant Frequency\tVar Freq");
		//TODO headerList.add("Normal Frequency\tNorm Freq");

		//  Add Any Gene-Specfic Headers
		if (specialGene != null)
		{
			JSONArray specialGeneHeaders = new JSONArray();

			for (String header : specialGene.getDataFieldHeaders())
			{
				specialGeneHeaders.add(header);
			}

			headerList.put("specialGeneHeaders", specialGeneHeaders);
		}

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
