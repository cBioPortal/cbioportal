package org.mskcc.portal.util;

import org.mskcc.portal.model.GeneticEventImpl.CNA;
import org.mskcc.portal.model.GeneticEventImpl.MRNA;
import org.mskcc.portal.model.GeneticEventImpl.RPPA;
import org.mskcc.portal.model.*;
import org.mskcc.portal.util.GlobalProperties;
import org.mskcc.portal.oncoPrintSpecLanguage.*;
import org.mskcc.cgds.model.CaseList;
import org.mskcc.cgds.model.GeneticProfile;
import org.mskcc.cgds.model.ExtendedMutation;
import org.mskcc.portal.model.ExtendedMutationMap;

import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.*;

/**
 * Generates the Genomic OncoPrint.
 *
 * @author Ethan Cerami, Arthur Goldberg.
 */
public class MakeOncoPrint {
	public static int CELL_HEIGHT = 21; // if this changes, ALTERATION_HEIGHT in raphaeljs-oncoprint.js should change
	private static String PERCENT_ALTERED_COLUMN_HEADING = "Total\\naltered";
	private static String COPY_NUMBER_ALTERATION_FOOTNOTE = "Copy number alterations are putative.";

    /**
     * Generate the OncoPrint in HTML or SVG.
     * @param cancerTypeID              Cancer Study ID.
     * @param geneList                  List of Genes.
     * @param mergedProfile             Merged Data Profile.
	 * @param mutationList              List of Mutations
     * @param caseSets                  All Case Sets for this Cancer Study.
     * @param caseSetId                 Selected Case Set ID.
     * @param zScoreThreshold           Z-Score Threshhold
     * @param geneticProfileIdSet       IDs for all Genomic Profiles.
     * @param profileList               List of all Genomic Profiles.
	 * @param forSummaryTab             If we are providing content for the Summary Tab (otherwise assume Cross Cancer Study Page)
     * @throws IOException IO Error.
     */
    public static String makeOncoPrint(String cancerTypeID,
									   String geneList,
									   ProfileData mergedProfile,
									   ArrayList<ExtendedMutation> mutationList,
									   ArrayList<CaseList> caseSets,
									   String caseSetId,
									   double zScoreThreshold,
                                                                           double rppaScoreThreshold,
									   HashSet<String> geneticProfileIdSet,
									   ArrayList<GeneticProfile> profileList,
									   boolean forSummaryTab) throws IOException {

        StringBuffer out = new StringBuffer();

        ParserOutput theOncoPrintSpecParserOutput =
                OncoPrintSpecificationDriver.callOncoPrintSpecParserDriver(geneList,
                geneticProfileIdSet, profileList, zScoreThreshold, rppaScoreThreshold);

        ArrayList<String> listOfGenes =
                theOncoPrintSpecParserOutput.getTheOncoPrintSpecification().listOfGenes();
        String[] listOfGeneNames = new String[listOfGenes.size()];
        listOfGeneNames = listOfGenes.toArray(listOfGeneNames);

		ExtendedMutationMap mutationMap =
			(mutationList == null) ? null : new ExtendedMutationMap(mutationList, mergedProfile.getCaseIdList());

        ProfileDataSummary dataSummary = new ProfileDataSummary(mergedProfile,
                theOncoPrintSpecParserOutput.getTheOncoPrintSpecification(), zScoreThreshold, rppaScoreThreshold);

        ArrayList<GeneWithScore> geneWithScoreList = dataSummary.getGeneFrequencyList();

        // TODO: make the gene sort order a user param, then call a method in ProfileDataSummary to sort
        GeneticEvent sortedMatrix[][] = ConvertProfileDataToGeneticEvents.convert
			(dataSummary, listOfGeneNames,
			 theOncoPrintSpecParserOutput.getTheOncoPrintSpecification(), zScoreThreshold, rppaScoreThreshold);
        GeneticEvent unsortedMatrix[][] = ConvertProfileDataToGeneticEvents.convert
			(dataSummary, listOfGeneNames,
			 theOncoPrintSpecParserOutput.getTheOncoPrintSpecification(), zScoreThreshold, rppaScoreThreshold);

        //  Sort Columns via Cascade Sorter
        ArrayList<EnumSet<CNA>> CNAsortOrder = new ArrayList<EnumSet<CNA>>();
        CNAsortOrder.add(EnumSet.of(CNA.AMPLIFIED));
        CNAsortOrder.add(EnumSet.of(CNA.HOMODELETED));
        CNAsortOrder.add(EnumSet.of(CNA.GAINED));
        CNAsortOrder.add(EnumSet.of(CNA.HEMIZYGOUSLYDELETED));
        // combined because these are represented by the same color in the OncoPring
        CNAsortOrder.add(EnumSet.of(CNA.DIPLOID, CNA.NONE));

        ArrayList<EnumSet<MRNA>> MRNAsortOrder = new ArrayList<EnumSet<MRNA>>();
        MRNAsortOrder.add(EnumSet.of(MRNA.UPREGULATED));
        MRNAsortOrder.add(EnumSet.of(MRNA.DOWNREGULATED));

        // combined because these are represented by the same color in the OncoPrint
        MRNAsortOrder.add(EnumSet.of(MRNA.NORMAL, MRNA.NOTSHOWN));

        ArrayList<EnumSet<RPPA>> RPPAsortOrder = new ArrayList<EnumSet<RPPA>>();
        RPPAsortOrder.add(EnumSet.of(RPPA.UPREGULATED));
        RPPAsortOrder.add(EnumSet.of(RPPA.DOWNREGULATED));

        // combined because these are represented by the same color in the OncoPrint
        RPPAsortOrder.add(EnumSet.of(RPPA.NORMAL, RPPA.NOTSHOWN));

        GeneticEventComparator comparator = new GeneticEventComparator(
                CNAsortOrder,
                MRNAsortOrder,
                RPPAsortOrder,
                GeneticEventComparator.defaultMutationsSortOrder());

        CascadeSortOfMatrix sorter = new CascadeSortOfMatrix(comparator);
        for (GeneticEvent[] row : sortedMatrix) {
            for (GeneticEvent element : row) {
                element.setGeneticEventComparator(comparator);
            }
        }
        sortedMatrix = (GeneticEvent[][]) sorter.sort(sortedMatrix);

		writeOncoPrint(out, cancerTypeID, unsortedMatrix, sortedMatrix,
					   dataSummary, mutationMap, caseSets, caseSetId,
					   theOncoPrintSpecParserOutput.getTheOncoPrintSpecification(),
					   forSummaryTab);

		// outta here
        return out.toString();
    }

    /**
     * Generates an OncoPrint in HTML.
	 *
	 * @param out StringBuffer
	 * @param cancerTypeID String
	 * @param unsortedMatrix GeneticEvent[][]
	 * @param sortedMatrix GeneticEvent[][]
	 * @param dataSummary ProfileDataSummary
	 * @param mutationMap ExtendedMutationMap
     * @param caseSets List<CaseList>
     * @param caseSetId String
     * @param theOncoPrintSpecification OncoPrintSpecification
	 * @param forSummaryTab boolean
     */
    static void writeOncoPrint(StringBuffer out,
							   String cancerTypeID,
							   GeneticEvent unsortedMatrix[][],
							   GeneticEvent sortedMatrix[][],
							   ProfileDataSummary dataSummary,
							   ExtendedMutationMap mutationMap,
							   List<CaseList> caseSets, String caseSetId,
							   OncoPrintSpecification theOncoPrintSpecification,
							   boolean forSummaryTab) {
		//
		// the follow vars are values of various HTML elements and javascript vars that get generated below
		//

		// the overall div for the oncoprint
		String oncoprintSection = "oncoprint_section_" + cancerTypeID;
		// each oncoprint is composed of a header, body & footer
		String oncoprintHeaderDivName = "oncoprint_header_" + cancerTypeID;
		String oncoprintBodyDivName = "oncoprint_body_" + cancerTypeID;
		String oncoprintLegendDivName = "oncoprint_legend_" + cancerTypeID;
		// the name of the unsort checkbox and its label (span).
		// these are not hardcoded as the name is shared between routines below
		String unsortSamplesCheckboxName = "unsortSamples";
		String unsortSamplesLabelName = "unsortSamplesLabel";
		// names of various javascript variables used by the raphaeljs-oncoprint.js
		String headerVariablesVarName = "HEADER_VARIABLES_" + cancerTypeID;
		String longestLabelVarName = "LONGEST_LABEL_" + cancerTypeID;
		String sortedGeneticAlterationsVarName = "GENETIC_ALTERATIONS_SORTED_" + cancerTypeID;
		String unsortedGeneticAlterationsVarName = "GENETIC_ALTERATIONS_UNSORTED_" + cancerTypeID;
		String geneticAlterationsLegendVarName = "GENETIC_ALTERATIONS_LEGEND_" + cancerTypeID;
		String legendFootnoteVarName = "LEGEND_FOOTNOTE_" + cancerTypeID;
		String oncoprintReferenceVarName = "ONCOPRINT_" + cancerTypeID;

		// oncoprint header
		if (forSummaryTab) {
			out.append("<div id=\"" + oncoprintSection + "\" class=\"oncoprint_section\">\n");
			out.append("<p><h4>OncoPrint&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<small>(<a href=\"faq.jsp#what-are-oncoprints\">What are OncoPrints?</a>)</small></h4>\n");
			out.append("<p></p>\n");
			out.append("<div style=\"width:800px;\">\n");
			out.append("<div id=\"" + oncoprintSection + "\" class=\"oncoprint\">\n");
		}

		// include some javascript libs
		out.append("<script type=\"text/javascript\" src=\"js/raphael/raphael.js\"></script>\n");
		out.append("<script type=\"text/javascript\" src=\"js/raphaeljs-oncoprint.js\"></script>\n");
		out.append("<script type=\"text/javascript\">\n");
		// output oncoprint variables
		out.append(writeOncoPrintHeaderVariables(sortedMatrix, dataSummary, caseSets, caseSetId, headerVariablesVarName));
		// output longest label variable
		out.append(writeJavascriptConstVariable(longestLabelVarName, getLongestLabel(sortedMatrix, dataSummary)));
		// output sorted genetic alteration variable for oncoprint body
		out.append(writeOncoPrintGeneticAlterationVariable(unsortedMatrix, dataSummary, mutationMap, unsortedGeneticAlterationsVarName));
		out.append(writeOncoPrintGeneticAlterationVariable(sortedMatrix, dataSummary, mutationMap, sortedGeneticAlterationsVarName));
		// output lengend footnote
		String legendFootnote = getLegendFootnote(theOncoPrintSpecification.getUnionOfPossibleLevels());
		out.append(writeJavascriptConstVariable(legendFootnoteVarName, legendFootnote));
		// output genetic alteration variable for oncoprint legend
		out.append(writeOncoPrintLegendGeneticAlterationVariable(geneticAlterationsLegendVarName,
																 theOncoPrintSpecification.getUnionOfPossibleLevels()));
		// on document ready, draw oncoprint header, oncoprint, oncoprint legend
		out.append(writeOncoPrintDocumentReadyJavascript(oncoprintSection, oncoprintReferenceVarName,
														 oncoprintHeaderDivName, oncoprintBodyDivName, oncoprintLegendDivName,
														 longestLabelVarName, headerVariablesVarName,
														 sortedGeneticAlterationsVarName, geneticAlterationsLegendVarName,
														 legendFootnoteVarName, unsortSamplesLabelName, forSummaryTab));
		out.append("</script>\n");
		if (forSummaryTab) {
			out.append(writeHTMLControls(oncoprintReferenceVarName, longestLabelVarName, headerVariablesVarName,unsortSamplesCheckboxName,
										 unsortSamplesLabelName, sortedGeneticAlterationsVarName, unsortedGeneticAlterationsVarName, forSummaryTab));
		}
		out.append("<div id=\"" + oncoprintHeaderDivName + "\" class=\"oncoprint\"></div>\n");
		out.append("<div id=\"" + oncoprintBodyDivName + "\" class=\"oncoprint\"></div>\n");
		out.append("<br>\n");
		out.append("<div id=\"" + oncoprintLegendDivName + "\" class=\"oncoprint\"></div>\n");

		// oncoprint footer
		if (forSummaryTab) {
			out.append("</div>\n");
			out.append("</div>\n");
			out.append("<p>\n");
			out.append("</div>\n");
		}
	}

	/**
	 * Creates javascript variable for header variables.
	 *
	 * @param matrix[][] GeneticEvent
     * @param caseSets List<CaseList>
     * @param caseSetId String
	 * @param dataSummary ProfileDataSummary
	 * @param varName String
	 *
	 * @return String
	 */
	static String writeOncoPrintHeaderVariables(GeneticEvent matrix[][],
												ProfileDataSummary dataSummary,
												List<CaseList> caseSets,
												String caseSetId,
												String varName) {
		// output case set description
		String caseSetDescription = getCaseSetDescription(caseSetId, caseSets);
		String alteredStats = ("Altered in " + dataSummary.getNumCasesAffected() + " (" +
							   alterationValueToString(dataSummary.getPercentCasesAffected())
							   + ") of cases.");
		String percentAlteredColumnHeading = PERCENT_ALTERED_COLUMN_HEADING;
		String allSamplesColumnHeading = "All " + pluralize(matrix[0].length, " case") + " -->";
		String alteredSamplesColumnHeading = (pluralize(dataSummary.getNumCasesAffected(), " case")
											  + " with altered genes, out of " + pluralize(matrix[0].length, " total case") + " -->");
															
		StringBuilder builder = new StringBuilder("\tvar " + varName + " = (function() {\n");
		builder.append("\t\tvar private = {\n");
		builder.append("\t\t\t'CASE_SET_DESCRIPTION' : \"" + caseSetDescription + "\",\n");
		builder.append("\t\t\t'ALTERED_STATS' : \"" + alteredStats + "\",\n");
		builder.append("\t\t\t'PERCENT_ALTERED_COLUMN_HEADING' : \"" + percentAlteredColumnHeading + "\",\n");
		builder.append("\t\t\t'ALL_SAMPLES_COLUMN_HEADING' : \"" + allSamplesColumnHeading + "\",\n");
		builder.append("\t\t\t'ALTERED_SAMPLES_COLUMN_HEADING' : \"" + alteredSamplesColumnHeading + "\",\n");
		// zap off last ',\n'
		builder.delete(builder.length()-2, builder.length());
		builder.append("\t\t};\n");
		builder.append("\t\treturn {\n");
		builder.append("\t\t\tget : function(name) { return private[name]; }\n");
		builder.append("\t\t};\n");
		builder.append("\t})();\n");

		// outta here
		return builder.toString();
	}

	/**
	 * Creates javascript that represents genetic alteration matrix used for OncoPrint rendering.
	 *
	 * @param matrix[][] GeneticEvent
	 * @param dataSummary ProfileDataSummary
	 * @param mutationMap ExtendedMutationMap
	 * @param varName String
	 *
	 * @return String
	 */
	static String writeOncoPrintGeneticAlterationVariable(GeneticEvent matrix[][],
                ProfileDataSummary dataSummary, ExtendedMutationMap mutationMap, String varName) {

            StringBuilder builder = new StringBuilder("\tvar " + varName + " = (function() {\n");
            builder.append("\t\tvar private = {\n");
            builder.append("\t\t\t'" + varName + "' : [\n");

            for (int i = 0; i < matrix.length; i++) {
                GeneticEvent rowEvent = matrix[i][0];
                String gene = rowEvent.getGene().toUpperCase();
                String alterationValue =
                        alterationValueToString(dataSummary.getPercentCasesWhereGeneIsAltered(rowEvent.getGene()));
                builder.append("\t\t\t\t{\n\t\t\t\t 'hugoGeneSymbol' : \"" + gene + "\",\n");
                builder.append("\t\t\t\t 'percentAltered' : \"" + alterationValue  + "\",\n");
                builder.append("\t\t\t\t 'alterations' : [\n");
                for (int j = 0; j < matrix[0].length; j++) {
                    GeneticEvent event = matrix[i][j];
                    // get level of each datatype; concatenate to make image name
                    // color could later could be in configuration file
                    String cnaName = "CNA_" + event.getCnaValue().name().toUpperCase();
                    String mrnaName = "MRNA_" + event.getMrnaValue().name().toUpperCase();
                    String rppaName = "RPPA_" + event.getRPPAValue().name().toUpperCase();
                    String mutationName = (event.isMutated()) ? "MUTATED" : "NORMAL";
                    String alterationSettings = cnaName + " | " + mrnaName + " | " + mutationName + " | " + rppaName;
                    StringBuilder mutationDetails = new StringBuilder();
                    if (event.isMutated() && mutationMap != null) {
                        mutationDetails.append(", 'mutation' : [");
                        List<ExtendedMutation> mutations = mutationMap.getExtendedMutations(gene, event.caseCaseId());
                        for (ExtendedMutation mutation : mutations) {
                            mutationDetails.append("\"" + mutation.getAminoAcidChange() + "\", ");
                        }
                        // zap off last ', '
                        mutationDetails.delete(mutationDetails.length()-2, mutationDetails.length());
                        mutationDetails.append("]");
                    }
                    builder.append("\t\t\t\t\t{ 'sample' : \"" + event.caseCaseId() + "\", " +
                                                "'alteration' : " + alterationSettings +
                                                mutationDetails.toString()  + "},\n");
                }
                
                // zap off last ',\n'
                builder.delete(builder.length()-2, builder.length());
                builder.append("]\n");
                builder.append("\t\t\t\t},\n");
            }
            
            // zap off last ',\n'
            builder.delete(builder.length()-2, builder.length());
            builder.append("\n\t\t\t]\n");
            builder.append("\t\t};\n");
            builder.append("\t\treturn {\n");
            builder.append("\t\t\tget : function(name) { return private[name]; }\n");
            builder.append("\t\t};\n");
            builder.append("\t})();\n");

            // outta here
            return builder.toString();
	}

	/**
	 * Creates javascript that represents genetic alteration matrix used for legend rendering.
	 *
	 * @param varName String
	 * @param allPossibleAlterations OncoPrintGeneDisplaySpec
	 *
	 * @return String
	 */
	static String writeOncoPrintLegendGeneticAlterationVariable(String varName,
																OncoPrintGeneDisplaySpec allPossibleAlterations) {

		StringBuilder builder = new StringBuilder("\tvar " + varName + " = (function() {\n");

		builder.append("\t\tvar private = {\n");
		builder.append("\t\t\t'" + varName + "' : [\n");
        if (allPossibleAlterations.satisfy(GeneticDataTypes.CopyNumberAlteration, GeneticTypeLevel.Amplified)) {
			builder.append("\t\t\t\t{\n\t\t\t\t 'label' : \"Amplification\",\n");
			builder.append("\t\t\t\t 'alteration' : CNA_AMPLIFIED | MRNA_NOTSHOWN | NORMAL | RPPA_NOTSHOWN\n\t\t\t\t},\n");
		}
        if (allPossibleAlterations.satisfy(GeneticDataTypes.CopyNumberAlteration, GeneticTypeLevel.HomozygouslyDeleted)) {
			builder.append("\t\t\t\t{\n\t\t\t\t 'label' : \"Homozygous Deletion\",\n");
			builder.append("\t\t\t\t 'alteration' : CNA_HOMODELETED | MRNA_NOTSHOWN | NORMAL | RPPA_NOTSHOWN\n\t\t\t\t},\n");
		}
        if (allPossibleAlterations.satisfy(GeneticDataTypes.CopyNumberAlteration, GeneticTypeLevel.Gained)) {
			builder.append("\t\t\t\t{\n\t\t\t\t 'label' : \"Gain\",\n");
			builder.append("\t\t\t\t 'alteration' : CNA_GAINED | MRNA_NOTSHOWN | NORMAL | RPPA_NOTSHOWN\n\t\t\t\t},\n");
		}
        if (allPossibleAlterations.satisfy(GeneticDataTypes.CopyNumberAlteration, GeneticTypeLevel.HemizygouslyDeleted)) {
			builder.append("\t\t\t\t{\n\t\t\t\t 'label' : \"Hemizygous Deletion\",\n");
			builder.append("\t\t\t\t 'alteration' : CNA_HEMIZYGOUSLYDELETED | MRNA_NOTSHOWN | NORMAL | RPPA_NOTSHOWN\n\t\t\t\t},\n");
		}
        
        // gene expression
        ResultDataTypeSpec theResultDataTypeSpec = allPossibleAlterations.getResultDataTypeSpec(GeneticDataTypes.Expression);
        if (theResultDataTypeSpec != null) {
			if (theResultDataTypeSpec.getCombinedGreaterContinuousDataTypeSpec() != null) {
				builder.append("\t\t\t\t{\n\t\t\t\t 'label' : \"Up-regulation (RNA)\",\n");
				builder.append("\t\t\t\t 'alteration' : CNA_DIPLOID | MRNA_UPREGULATED | NORMAL | RPPA_NOTSHOWN\n\t\t\t\t},\n");
			}
			if (theResultDataTypeSpec.getCombinedLesserContinuousDataTypeSpec() != null) {
				builder.append("\t\t\t\t{\n\t\t\t\t 'label' : \"Down-regulation (RNA)\",\n");
				builder.append("\t\t\t\t 'alteration' : CNA_DIPLOID | MRNA_DOWNREGULATED | NORMAL | RPPA_NOTSHOWN\n\t\t\t\t},\n");
			}
		}
        
        // RPPA
        theResultDataTypeSpec = allPossibleAlterations.getResultDataTypeSpec(GeneticDataTypes.RPPA);
        if (theResultDataTypeSpec != null) {
			if (theResultDataTypeSpec.getCombinedGreaterContinuousDataTypeSpec() != null) {
				builder.append("\t\t\t\t{\n\t\t\t\t 'label' : \"Up-regulation (RPPA)\",\n");
				builder.append("\t\t\t\t 'alteration' : CNA_DIPLOID | MRNA_NOTSHOWN | NORMAL | RPPA_UPREGULATED\n\t\t\t\t},\n");
			}
			if (theResultDataTypeSpec.getCombinedLesserContinuousDataTypeSpec() != null) {
				builder.append("\t\t\t\t{\n\t\t\t\t 'label' : \"Down-regulation (RPPA)\",\n");
				builder.append("\t\t\t\t 'alteration' : CNA_DIPLOID | MRNA_NOTSHOWN | NORMAL | RPPA_DOWNREGULATED\n\t\t\t\t},\n");
			}
		}
        
        if (allPossibleAlterations.satisfy(GeneticDataTypes.Mutation, GeneticTypeLevel.Mutated)) {
			builder.append("\t\t\t\t{\n\t\t\t\t 'label' : \"Mutation\",\n");
			builder.append("\t\t\t\t 'alteration' : CNA_DIPLOID | MRNA_NOTSHOWN | MUTATED | RPPA_NOTSHOWN\n\t\t\t\t},\n");
		}

		// zap off last ',\n'
		builder.delete(builder.length()-2, builder.length());
		builder.append("\n\t\t\t]\n");
		builder.append("\t\t};\n");
		builder.append("\t\treturn {\n");
		builder.append("\t\t\tget : function(name) { return private[name]; }\n");
		builder.append("\t\t};\n");
		builder.append("\t})();\n");

		// outta here
		return builder.toString();
	}

	/**
	 * Creates javascript which invokes (via jquery) OncoPrint drawing 
	 * when document is ready.
	 *
	 * @param oncoprintSectionVarName String
	 * @param oncoprintReferenceVarName String
	 * @param headerElement String
	 * @param bodyElement String
	 * @param legendElement String
	 * @param logestLabelVarName String
	 * @param headerVariablesVarName String
	 * @param geneticAlterationsVarName String
	 * @param geneticAlterationsLegendVarName String
	 * @param legendFootnoteVarName String
	 * @param unsortSamplesLabelName String
	 * @param forSummaryTab String
	 *
	 * @return String
	 */
	static String writeOncoPrintDocumentReadyJavascript(String oncoprintSectionVarName,
														String oncoprintReferenceVarName,
														String headerElement,
														String bodyElement,
														String legendElement,
														String longestLabelVarName,
														String headerVariablesVarName,
														String geneticAlterationsVarName,
														String geneticAlterationsLegendVarName,
														String legendFootnoteVarName,
														String unsortSamplesLabelName,
														boolean forSummaryTab) {

		StringBuilder builder = new StringBuilder();

		// declare the oncoprint ref outside .ready so it is accessilble by page widgets
		builder.append("\tvar " + oncoprintReferenceVarName + " = null;\n");
		// jquery on document ready
		builder.append("\t$(document).ready(function() {\n");
		// setup default properties
		builder.append("\t\t" + oncoprintReferenceVarName + " = OncoPrintInit(" +
					   headerElement + ", " + bodyElement + ", " + legendElement + ");\n");
		// oncoprint header
		builder.append("\t\tDrawOncoPrintHeader(" + oncoprintReferenceVarName + ", " +
					   longestLabelVarName + ".get('" + longestLabelVarName + "'), " + 
					   headerVariablesVarName + ", " + forSummaryTab  + ");\n");
		// draw oncoprint
		builder.append("\t\tDrawOncoPrintBody(" + oncoprintReferenceVarName + ", " +
					   longestLabelVarName + ".get('" + longestLabelVarName + "'), " + 
					   geneticAlterationsVarName  + ".get('" + geneticAlterationsVarName + "'), " +
					   forSummaryTab + ");\n");
		if (forSummaryTab) {
			// draw legend
			builder.append("\t\tDrawOncoPrintLegend(" + oncoprintReferenceVarName + ", " +
						   longestLabelVarName + ".get('" + longestLabelVarName + "'), " + 
						   geneticAlterationsLegendVarName  + ".get('" + geneticAlterationsLegendVarName + "'), " +
						   legendFootnoteVarName  + ".get('" + legendFootnoteVarName + "'));\n");
			// handle tooltip drawing when page is first loaded
			builder.append("\t\tvar currentLocation = window.location.pathname;\n");
			builder.append("\t\tif (currentLocation.indexOf(\"index.do\") != -1) { \n");
			builder.append("\t\t\tDrawOncoPrintTooltipRegion(" + oncoprintReferenceVarName +
						   ", document.getElementById(\"" + oncoprintSectionVarName +
						   "\"), document.getElementById(\"" + unsortSamplesLabelName + "\"));\n");
			builder.append("\t\t}\n");
			// handle tooltip drawing when other tabs are clicked
			builder.append("\t\t$(\"a\").click(function(event) {\n");
			builder.append("\t\t\t\tvar tab = $(this).attr(\"href\");\n");
			builder.append("\t\t\tif (tab == \"#summary\") {\n");
			builder.append("\t\t\t\tDrawOncoPrintTooltipRegion(" + oncoprintReferenceVarName +
						   ", document.getElementById(\"" + oncoprintSectionVarName +
						   "\"), document.getElementById(\"" + unsortSamplesLabelName + "\"));\n");
			builder.append("\t\t\t}\n");
			builder.append("\t\t\t// we only clear if one of the inner index.do tabs are clicked\n"); 
			builder.append("\t\t\t// otherwise we get a noticable tooltip clear before the page is reloaded\n");
			builder.append("\t\t\telse if (tab.indexOf(\".jsp\") == -1) {\n");
			builder.append("\t\t\t\tClearOncoPrintTooltipRegion(" + oncoprintReferenceVarName + ");\n");
			builder.append("\t\t\t}\n");
			builder.append("\t\t});\n");
			// handle tooltip drawing when browser is resized
			builder.append("\t\t$(window).resize(function() {\n");
			builder.append("\t\t\tClearOncoPrintTooltipRegion(" + oncoprintReferenceVarName + ");\n");
			builder.append("\t\t\tDrawOncoPrintTooltipRegion(" + oncoprintReferenceVarName +
						   ", document.getElementById(\"" + oncoprintSectionVarName +
						   "\"), document.getElementById(\"" + unsortSamplesLabelName + "\"));\n");
			builder.append("\t\t});\n");
		}
		// end on document ready
		builder.append("\t});\n");

		// outta here
		return builder.toString();
	}

	/**
	 * Creates OncoPrint Control (checkboxes, submit button, etc).
	 *
	 * @param oncoprintReferenceVarName String
	 * @param longestLabelVarName String
	 * @param headerVariablesVarName String
	 * @param unsortSamplesCheckboxName String
	 * @param unsortSamplesLabelName String
	 * @param sortedGeneticAlterationsVarName String
	 * @param unsortedGeneticAlterationsVarName String
	 * @param forSummaryTab String
	 *
	 * @return String
	 */
	static String writeHTMLControls(String oncoprintReferenceVarName,
									String longestLabelVarName,
									String headerVariablesVarName,
									String unsortSamplesCheckboxName,
									String unsortSamplesLabelName,
									String sortedGeneticAlterationsVarName,
									String unsortedGeneticAlterationsVarName,
									boolean forSummaryTab) {

		String formID = "oncoprintForm";
		StringBuilder builder = new StringBuilder();

		// form start
		builder.append("<form id=\"" + formID + "\" action=\"oncoprint_converter.svg\" method=\"POST\"" +
					   "onsubmit=\"this.elements['longest_label_length'].value=GetLongestLabelLength(" + oncoprintReferenceVarName + "); "+ 
					   "this.elements['xml'].value=GetOncoPrintBodyXML(" + oncoprintReferenceVarName + "); return true;\"" +
					   " target=\"_blank\">\n");
		// add some hidden elements
		builder.append("<input type=hidden name=\"xml\">\n");
		builder.append("<input type=hidden name=\"longest_label_length\">\n");
		builder.append("<input type=hidden name=\"format\" value=\"svg\">\n");

		// export SVG button
		builder.append("<P>Get OncoPrint:&nbsp;&nbsp<input type=\"submit\" value=\"SVG\">\n");
		
		// show altered checkbox
		builder.append("&nbsp;&nbsp<input type=\"checkbox\" id= \"showAlteredColumns\" name=\"showAlteredColumns\" value=\"false\" " +
					   "onClick=\"ShowAlteredSamples(" + oncoprintReferenceVarName + ", this.checked); " +
					   "DrawOncoPrintHeader(" + oncoprintReferenceVarName + ", " +
					   longestLabelVarName + ".get('" + longestLabelVarName + "'), " + 
					   headerVariablesVarName + ", true); " +
					   "if (document.getElementById('" + unsortSamplesCheckboxName + "').checked) { DrawOncoPrintBody(" + oncoprintReferenceVarName + ", " +
					   longestLabelVarName + ".get('" + longestLabelVarName + "'), " +
					   unsortedGeneticAlterationsVarName  + ".get('" + unsortedGeneticAlterationsVarName + "'), " + forSummaryTab  + "); } else { " +
					   "DrawOncoPrintBody(" + oncoprintReferenceVarName + ", " +
					   longestLabelVarName + ".get('" + longestLabelVarName + "'), " +
					   sortedGeneticAlterationsVarName  + ".get('" + sortedGeneticAlterationsVarName + "'), " + forSummaryTab  + "); } return true;\"" +
					   "><span id=\"showAlteredCasesLabel\">Only show altered cases.</span>\n");

		// sort/unsort altered checkbox
		builder.append("&nbsp;&nbsp<input type=\"checkbox\" id=\"" + unsortSamplesCheckboxName + "\" name=\"" + unsortSamplesCheckboxName + "\" value=\"false\" " +
					   "onClick=\"if (this.checked) { DrawOncoPrintBody(" + oncoprintReferenceVarName + ", " +
					   longestLabelVarName + ".get('" + longestLabelVarName + "'), " +
					   unsortedGeneticAlterationsVarName  + ".get('" + unsortedGeneticAlterationsVarName + "'), " + forSummaryTab + "); } else { " +
					   "DrawOncoPrintBody(" + oncoprintReferenceVarName + ", " +
					   longestLabelVarName + ".get('" + longestLabelVarName + "'), " +
					   sortedGeneticAlterationsVarName  + ".get('" + sortedGeneticAlterationsVarName + "'), " + forSummaryTab + "); } return true;\"" +
					   "><span id=\"" + unsortSamplesLabelName + "\">Unsort Samples.</span>\n");

		// form end
		builder.append("</form>\n");
	
		// outta here
		return builder.toString();
	}

	/**
	 * Creates javascript var for given var name/value.
	 *
	 * @param varName String
	 * @param varValue Sting
	 *
	 * @return String
	 */
	static String writeJavascriptConstVariable(String varName, String varValue) {

		StringBuilder builder = new StringBuilder("\tvar " + varName + " = (function() {\n");

		builder.append("\t\tvar private = {\n");
		builder.append("\t\t\t'" + varName + "' : \"" + varValue + "\"\n");
		builder.append("\t\t};\n");
		builder.append("\t\treturn {\n");
		builder.append("\t\t\tget : function(name) { return private[name]; }\n");
		builder.append("\t\t};\n");
		builder.append("\t})();\n");

		// outta here
		return builder.toString();
	}

    /**
	 * pluralize a count + name; dumb, because doesn't consider adding 'es' to pluralize
	 *
	 * @param num  The count.
	 * @param s The string to pluralize.
	 *
	 * @return String
	 */
    static String pluralize(int num, String s) {
        if (num == 1) {
            return new String(num + s);
        } else {
            return new String(num + s + "s");
        }
    }

    /**
     * Format percentage.
	 *
     * <p/>
     * if value == 0 return "--"
     * case value
     * 0: return "--"
     * 0<value<=0.01: return "<1%"
     * 1<value: return "<value>%"
     *
     * @param value double
	 *
     * @return String
     */
    static String alterationValueToString(double value) {

        // in oncoPrint show 0 percent as 0%, not --
        if (0.0 < value && value <= 0.01) {
            return "<1%";
        }

        // if( 1.0 < value ){
        Formatter f = new Formatter();
        f.format("%.0f", value * 100.0);
        return f.out().toString() + "%";
    }

	/**
	 * Gets the legend footnote (if any).
	 *
	 * @param allPossibleAlterations OncoPrintGeneDisplaySpec
	 *
	 * @return String
	 */
	static String getLegendFootnote(OncoPrintGeneDisplaySpec allPossibleAlterations) {

        return (allPossibleAlterations.satisfy(GeneticDataTypes.CopyNumberAlteration)) ?
			COPY_NUMBER_ALTERATION_FOOTNOTE  : "";
	}

	/**
	 * Constructs the OncoPrint case set description.
	 *
     * @param caseSetId String
	 * @param caseSets List<CaseList>
	 *
	 * @return String
	 */
	static String getCaseSetDescription(String caseSetId, List<CaseList> caseSets) {

		StringBuilder builder = new StringBuilder();
		for (CaseList caseSet : caseSets) {
			if (caseSetId.equals(caseSet.getStableId())) {
				builder.append("Case Set: " + caseSet.getName() +
							   ":  " + caseSet.getDescription());
			}
		}

		// outta here
		return builder.toString();
	}

	/**
	 * Computes the longest (in pixels) gene, % altered string pairing.
	 *
	 * @param matrix[][] GeneticEvent
	 * @param dataSummary ProfileDataSummary
	 *
	 * @return String
	 */
	private static String getLongestLabel(GeneticEvent matrix[][], ProfileDataSummary dataSummary) {

		// this font/size corresponds to .oncoprint td specified in global_portal.css
		java.awt.image.BufferedImage image =
			new java.awt.image.BufferedImage(100, 100, java.awt.image.BufferedImage.TYPE_INT_RGB);
		java.awt.Font portalFont = new java.awt.Font("verdana", java.awt.Font.PLAIN, 12);
		java.awt.FontMetrics fontMetrics = image.getGraphics().getFontMetrics(portalFont);
                
		int maxWidth = 0;
		String longestLabel = null;
        for (int lc = 0; lc < matrix.length; lc++) {
            GeneticEvent rowEvent = matrix[lc][0];
			String gene = rowEvent.getGene().toUpperCase();
			String alterationValue =
				alterationValueToString(dataSummary.getPercentCasesWhereGeneIsAltered(rowEvent.getGene()));
			String label = gene + alterationValue;
			int width = fontMetrics.stringWidth(label);
			if (width > maxWidth) {
				maxWidth = width;
				longestLabel = label;
			}
		}
		
		// outta here
		return longestLabel;
	}
}
