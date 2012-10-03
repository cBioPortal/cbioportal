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

package org.mskcc.cbio.portal.html;

import java.util.ArrayList;
import java.util.HashMap;
import org.mskcc.cbio.cgds.model.ExtendedMutation;
import org.mskcc.cbio.portal.html.special_gene.SpecialGene;
import org.mskcc.cbio.portal.html.special_gene.SpecialGeneFactory;
import org.mskcc.cbio.portal.util.ExtendedMutationUtil;
import org.mskcc.cbio.portal.util.SequenceCenterUtil;
import org.mskcc.cbio.portal.util.SkinUtil;

/**
 * Utility Class for Creating the Mutation Table.
 *
 * @author Ethan Cerami
 * @author Selcuk Onur Sumer
 */
public class MutationTableUtil
{
    private ArrayList<String> headerList = new ArrayList<String>();
	private SpecialGene specialGene;

	// Mappings between data values and html values
	private HashMap<String, String[]> mutationStatusMap;
	private HashMap<String, String[]> validationStatusMap;
	private HashMap<String, String[]> mutationTypeMap;

    // Validation Status Constants
    private static final String DISPLAY_VALID = "V";
	private static final String CSS_VALID = "valid";
	private static final String VALID = "valid";
	private static final String DISPLAY_UNKNOWN = "U";
	private static final String CSS_UNKNOWN = "unknown";
	private static final String UNKNOWN = "unknown";
	private static final String DISPLAY_WILDTYPE = "W";
	private static final String CSS_WILDTYPE = "wildtype";
	private static final String WILDTYPE = "wildtype";


	// Mutation Status Constants
    private static final String CSS_SOMATIC = "somatic";
    private static final String CSS_GERMLINE = "germline";
	private static final String SOMATIC = "somatic";
	private static final String GERMLINE = "germline";
	private static final String DISPLAY_SOMATIC = "S";
	private static final String DISPLAY_GERMLINE = "G";

	// Mutation Type Constants
	private static final String DISPLAY_MISSENSE = "Missense";
	private static final String DISPLAY_NONSENSE = "Nonsense";
	private static final String DISPLAY_FS_DEL = "FS del";
	private static final String DISPLAY_FS_INS = "FS ins";
	private static final String DISPLAY_IF_DEL = "IF ins";
	private static final String DISPLAY_IF_INS = "IF del";
	private static final String DISPLAY_SPLICE = "Splice";
	private static final String MISSENSE = "missense mutation";
	private static final String NONSENSE = "nonsense mutation";
	private static final String FS_DEL = "frame shift del";
	private static final String FS_INS = "frame shift ins";
	private static final String IF_DEL = "in frame ins";
	private static final String IF_INS = "in frame del";
	private static final String SPLICE = "splice site";
	private static final String CSS_MISSENSE = "missense_mutation";
	private static final String CSS_OTHER_MUT = "other_mutation";

    public MutationTableUtil(String geneSymbol)
    {
        specialGene = SpecialGeneFactory.getInstance(geneSymbol);
        this.mutationStatusMap = this.initMutationStatusMap();
	    this.validationStatusMap = this.initValidationStatusMap();
	    this.mutationTypeMap = this.initMutationTypeMap();
	    this.initHeaders();
    }

    public ArrayList<String> getTableHeaders() {
        return headerList;
    }

    public String getTableFooterMessage() {
        if (specialGene != null) {
            return specialGene.getFooter();
        } else {
            return HtmlUtil.EMPTY_STRING;
        }
    }

    public String getTableHeaderHtml() {
        return HtmlUtil.createTableHeaderRowWithTooltip(headerList);
    }

    public ArrayList<String> getDataFields(ExtendedMutation mutation)
    {
        ArrayList <String> dataFieldList = new ArrayList<String>();
	    MutationAssessorHtmlUtil omaUtil = new MutationAssessorHtmlUtil(mutation);

        //  Case ID.
        String caseId = HtmlUtil.getSafeWebValue(mutation.getCaseId());
        String htmlCaseId = "<a href='"
                + SkinUtil.getLinkToPatientView(caseId)
                + "'>" + caseId + "</a>";
        dataFieldList.add(htmlCaseId);

        //  Basic Mutation Info.
	    dataFieldList.add(HtmlUtil.getSafeWebValue(getProteinChange(mutation)));
	    dataFieldList.add(HtmlUtil.getSafeWebValue(getMutationType(mutation)));
	    dataFieldList.add(HtmlUtil.getSafeWebValue(getCosmicCount(mutation)));
	    dataFieldList.add(omaUtil.getFunctionalImpactScore());
	    dataFieldList.add(omaUtil.getPdbStructureLink());
	    dataFieldList.add(HtmlUtil.getSafeWebValue(getMutationStatus(mutation)));
	    dataFieldList.add(HtmlUtil.getSafeWebValue(getValidationStatus(mutation)));
        dataFieldList.add(HtmlUtil.getSafeWebValue(getSequencingCenter(mutation)));
	    dataFieldList.add(HtmlUtil.getSafeWebValue(getNcbiBuild(mutation)));
	    dataFieldList.add(HtmlUtil.getSafeWebValue(getChrPosition(mutation)));
	    dataFieldList.add(HtmlUtil.getSafeWebValue(mutation.getReferenceAllele()));
	    dataFieldList.add(HtmlUtil.getSafeWebValue(getVariantAllele(mutation)));
	    //TODO Norm. Freq. & Var. Freq.

        //  Fields for "Special" Genes
        if (specialGene != null) {
            dataFieldList.addAll(specialGene.getDataFields(mutation));
        }
        return dataFieldList;
    }

    public String getDataRowHtml(ExtendedMutation mutation) {
        return HtmlUtil.createTableRow(getDataFields(mutation));
    }

    private String getSequencingCenter(ExtendedMutation mutation) {
        return SequenceCenterUtil.getSequencingCenterAbbrev
                (mutation.getSequencingCenter());
    }

	/**
	 * Extracts the protein (amino acid) change information for the given
	 * mutation, and generates the html element for that protein change.
	 *
	 * @param mutation  mutation instance
	 * @return          returns html representation for the protein change
	 */
	private String getProteinChange(ExtendedMutation mutation)
	{
		if (mutation.getProteinChange() != null)
		{
			return HtmlUtil.createTextWithinSpan(mutation.getProteinChange(),
				"protein_change");
		}
		else
		{
			return mutation.getProteinChange();
		}
	}


	/**
	 * Extracts the validation status information for the given mutation,
	 * and generates the html element for that validation status
	 *
	 * @param mutation  mutation instance
	 * @return          returns html representation for the validation status
	 */
    private String getValidationStatus(ExtendedMutation mutation)
    {
	    String[] values = null;

	    if (mutation.getValidationStatus() != null)
	    {
		    values = this.validationStatusMap.get(
				mutation.getValidationStatus().toLowerCase());
	    }

	    // if there is a value pair (display, css) for the current validation status,
	    // use those values
	    if (values != null)
	    {
		    return HtmlUtil.createTextWithinSpan(
				"<label>" + values[0] + "</label>",
				values[1],
				mutation.getValidationStatus());
	    }
	    // else, directly use the validation status value itself
	    else
	    {
		    return mutation.getValidationStatus();
	    }
    }

	/**
	 * Extracts the mutation status information for the given mutation,
	 * and generates the html element for that mutation status
	 *
	 * @param mutation  mutation instance
	 * @return          returns html representation for the mutation status
	 */
    private String getMutationStatus(ExtendedMutation mutation)
    {
	    String[] values = null;

	    if (mutation.getMutationStatus() != null)
	    {
	        values = this.mutationStatusMap.get(
			    mutation.getMutationStatus().toLowerCase());
	    }

	    // if there is a value pair (display, css) for the current mutation status,
	    // use those values
        if (values != null)
        {
	        return HtmlUtil.createTextWithinSpan(
				"<label>" + values[0] + "</label>",
				values[1],
				mutation.getMutationStatus());
        }
        // else, directly use the mutation status value itself
        else
        {
            return mutation.getMutationStatus();
        }
    }

	/**
	 * Extracts the mutation type information for the given mutation,
	 * and generates the html element for that mutation type
	 *
	 * @param mutation  mutation instance
	 * @return          returns html representation for the mutation type
	 */
	private String getMutationType(ExtendedMutation mutation)
	{
		String[] values = null;

		if (mutation.getMutationType() != null)
		{
			// make it lowercase and remove any under dashes to match a possible key
			String type = mutation.getMutationType().toLowerCase().replaceAll("_", " ");
			values = this.mutationTypeMap.get(type);
		}

		// if there is a value pair (display, css) for the current mutation type,
		// use those values
		if (values != null)
		{
			return HtmlUtil.createTextWithinSpan(
				"<label>" + values[0] + "</label>", values[1]);
		}
		// else, directly use the mutation type value itself
		else
		{
			return mutation.getMutationType();
		}
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
	private String getCosmicCount(ExtendedMutation mutation)
	{
		if (mutation.getOncotatorCosmicOverlapping() == null ||
		    mutation.getOncotatorCosmicOverlapping().equals("NA"))
		{
			return null;
		}

		// calculate total cosmic count
		Integer total = ExtendedMutationUtil.calculateCosmicCount(mutation);

		if (total > 0)
		{
			return "<label class='mutation_table_cosmic' " +
				"alt='" + mutation.getOncotatorCosmicOverlapping() + "'>" +
				total.toString() + "</label>";
		}
		else
		{
			return null;
		}
	}

	/**
	 * Returns one of the tumor sequence alleles which is different from
	 * the reference allele.
	 *
	 * @param mutation  mutation instance
	 * @return          tumor sequence allele different from the reference allele
	 */
	private String getVariantAllele(ExtendedMutation mutation)
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
	private String getNcbiBuild(ExtendedMutation mutation)
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

	private String getChrPosition(ExtendedMutation mutation)
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

    private void initHeaders()
    {
		headerList.add("Case ID\tCase ID");
	    headerList.add("Protein Change\tAA Change");
	    headerList.add("Mutation Type\tType");
	    headerList.add("Overlapping mutations in COSMIC\tCOSMIC");
	    headerList.add("Predicted Functional Impact Score (via Mutation Assessor) " +
	                   "for missense mutations\tFIS");
	    headerList.add("3-D Structure\t3-D");
	    headerList.add("Mutation Status\tMS");
		headerList.add("Validation Status\tVS");
		headerList.add("Sequencing Center\tCenter");
		headerList.add("NCBI Build Number\tBuild");
		headerList.add("Position\tPosition");
		headerList.add("Reference Allele\tRef");
		headerList.add("Variant Allele\tVar");
		//TODO headerList.add("Variant Frequency\tVar Freq");
		//TODO headerList.add("Normal Frequency\tNorm Freq");

        //  Add Any Gene-Specfic Headers
        if (specialGene != null) {
            headerList.addAll(specialGene.getDataFieldHeaders());
        }
    }

	/**
	 * Creates a mapping between the mutation status (data) values and
	 * view values. The first element of an array corresponding to a
	 * data value is the display text (html), and the second one
	 * is style (css).
	 *
	 * @return  a mapping for possible values of mutation status
	 */
	private HashMap<String, String[]> initMutationStatusMap()
	{
		HashMap<String, String[]> map = new HashMap<String, String[]>();

		String[] somatic = {DISPLAY_SOMATIC, CSS_SOMATIC};
		String[] germline = {DISPLAY_GERMLINE, CSS_GERMLINE};
		String[] unknown = {DISPLAY_UNKNOWN, CSS_UNKNOWN};

		map.put(SOMATIC, somatic);
		map.put(GERMLINE, germline);
		map.put(UNKNOWN, unknown);

		return map;
	}

	/**
	 * Creates a mapping between the validation status (data) values and
	 * view values. The first element of an array corresponding to a
	 * data value is the display text (html), and the second one
	 * is style (css).
	 *
	 * @return  a mapping for possible values of validation status
	 */
	private HashMap<String, String[]> initValidationStatusMap()
	{
		HashMap<String, String[]> map = new HashMap<String, String[]>();

		String[] valid = {DISPLAY_VALID, CSS_VALID};
		String[] unknown = {DISPLAY_UNKNOWN, CSS_UNKNOWN};
		String[] wildtype = {DISPLAY_WILDTYPE, CSS_WILDTYPE};

		map.put(VALID, valid);
		map.put(UNKNOWN, unknown);
		map.put(WILDTYPE, wildtype);

		return map;
	}

	/**
	 * Creates a mapping between the mutation type (data) values and
	 * view values. The first element of an array corresponding to a
	 * data value is the display text (html), and the second one
	 * is style (css).
	 *
	 * @return  a mapping for possible values of validation status
	 */
	private HashMap<String, String[]> initMutationTypeMap()
	{
		HashMap<String, String[]> map = new HashMap<String, String[]>();

		String[] missense = {DISPLAY_MISSENSE, CSS_MISSENSE};
		String[] nonsense = {DISPLAY_NONSENSE, CSS_OTHER_MUT};
		String[] fsDel = {DISPLAY_FS_DEL, CSS_OTHER_MUT};
		String[] fsIns = {DISPLAY_FS_INS, CSS_OTHER_MUT};
		String[] ifDel = {DISPLAY_IF_DEL, CSS_OTHER_MUT};
		String[] ifIns = {DISPLAY_IF_INS, CSS_OTHER_MUT};
		String[] splice = {DISPLAY_SPLICE, CSS_OTHER_MUT};

		map.put(MISSENSE, missense);
		map.put(NONSENSE, nonsense);
		map.put(FS_DEL, fsDel);
		map.put(FS_INS, fsIns);
		map.put(IF_DEL, ifDel);
		map.put(IF_INS, ifIns);
		map.put(SPLICE, splice);

		return map;
	}
}
