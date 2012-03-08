package org.mskcc.portal.util;

import org.mskcc.portal.model.GeneticEventImpl.CNA;
import org.mskcc.portal.model.GeneticEventImpl.MRNA;
import org.mskcc.portal.model.*;
import org.mskcc.portal.util.GlobalProperties;
import org.mskcc.portal.oncoPrintSpecLanguage.*;
import org.mskcc.cgds.model.CaseList;
import org.mskcc.cgds.model.GeneticProfile;

import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.*;

/**
 * Generates the Genomic OncoPrint.
 *
 * @author Ethan Cerami, Arthur Goldberg.
 */
public class MakeOncoPrint {
    public static int CELL_WIDTH = 8; 
    public static int CELL_HEIGHT = 18;  // also used in fingerprint.jsp
	private static String PERCENT_ALTERED_COLUMN_HEADING = "Total\\naltered";
	private static String COPY_NUMBER_ALTERATION_FOOTNOTE = "Copy number alterations are putative.";

    // support OncoPrints in both SVG and HTML; only HTML will have extra textual info,
    // such as legend, %alteration
    public enum OncoPrintType {
        SVG, HTML
    }

    /**
     * Generate the OncoPrint in HTML or SVG.
     * @param geneList                  List of Genes.
     * @param mergedProfile             Merged Data Profile.
     * @param caseSets                  All Case Sets for this Cancer Study.
     * @param caseSetId                 Selected Case Set ID.
     * @param zScoreThreshold           Z-Score Threshhold
     * @param theOncoPrintType          OncoPrint Type.
     * @param showAlteredColumns        Show only the altered columns.
     * @param geneticProfileIdSet       IDs for all Genomic Profiles.
     * @param profileList               List of all Genomic Profiles.
	 * @param includeCaseSetDescription Include case set description boolean.
	 * @param includeLegend             Include legend boolean.
     * @throws IOException IO Error.
     */
    public static String makeOncoPrint(String geneList, ProfileData mergedProfile,
									   ArrayList<CaseList> caseSets, String caseSetId, double zScoreThreshold,
									   OncoPrintType theOncoPrintType,
									   boolean showAlteredColumns,
									   HashSet<String> geneticProfileIdSet,
									   ArrayList<GeneticProfile> profileList,
									   boolean includeCaseSetDescription,
									   boolean includeLegend
    ) throws IOException {
        StringBuffer out = new StringBuffer();

        ParserOutput theOncoPrintSpecParserOutput =
                OncoPrintSpecificationDriver.callOncoPrintSpecParserDriver(geneList,
                geneticProfileIdSet, profileList, zScoreThreshold);

        ArrayList<String> listOfGenes =
                theOncoPrintSpecParserOutput.getTheOncoPrintSpecification().listOfGenes();
        String[] listOfGeneNames = new String[listOfGenes.size()];
        listOfGeneNames = listOfGenes.toArray(listOfGeneNames);

        ProfileDataSummary dataSummary = new ProfileDataSummary(mergedProfile,
                theOncoPrintSpecParserOutput.getTheOncoPrintSpecification(), zScoreThreshold);

        ArrayList<GeneWithScore> geneWithScoreList = dataSummary.getGeneFrequencyList();
        ArrayList<String> mergedCaseList = mergedProfile.getCaseIdList();

        // TODO: make the gene sort order a user param, then call a method in ProfileDataSummary to sort
        GeneticEvent matrix[][] = ConvertProfileDataToGeneticEvents.convert
                (dataSummary, listOfGeneNames,
                theOncoPrintSpecParserOutput.getTheOncoPrintSpecification(), zScoreThreshold);

        //  Sort Columns via Cascade Sorter
        ArrayList<EnumSet<CNA>> CNAsortOrder = new ArrayList<EnumSet<CNA>>();
        CNAsortOrder.add(EnumSet.of(CNA.amplified));
        CNAsortOrder.add(EnumSet.of(CNA.homoDeleted));
        CNAsortOrder.add(EnumSet.of(CNA.Gained));
        CNAsortOrder.add(EnumSet.of(CNA.HemizygouslyDeleted));
        // combined because these are represented by the same color in the OncoPring
        CNAsortOrder.add(EnumSet.of(CNA.diploid, CNA.None));

        ArrayList<EnumSet<MRNA>> MRNAsortOrder = new ArrayList<EnumSet<MRNA>>();
        MRNAsortOrder.add(EnumSet.of(MRNA.upRegulated));
        MRNAsortOrder.add(EnumSet.of(MRNA.downRegulated));

        // combined because these are represented by the same color in the OncoPrint
        MRNAsortOrder.add(EnumSet.of(MRNA.Normal, MRNA.notShown));

        GeneticEventComparator comparator = new GeneticEventComparator(
                CNAsortOrder,
                MRNAsortOrder,
                GeneticEventComparator.defaultMutationsSortOrder());

        CascadeSortOfMatrix sorter = new CascadeSortOfMatrix(comparator);
        for (GeneticEvent[] row : matrix) {
            for (GeneticEvent element : row) {
                element.setGeneticEventComparator(comparator);
            }
        }
        matrix = (GeneticEvent[][]) sorter.sort(matrix);

        // optionally, show only columns with alterations
        // depending on showAlteredColumns, find last column with alterations
        int numColumnsToShow = matrix[0].length;

        if (showAlteredColumns) {
            // identify last column with an alteration
            // make HTML OncoPrint: not called if there are no genes

            // have oncoPrint shows nothing when no cases are modified
            numColumnsToShow = 0;
            firstAlteration:
            {

                // iterate through cases from the end, stopping at first case with alterations
                // (the sort order could sort unaltered cases before altered ones)
                for (int j = matrix[0].length - 1; 0 <= j; j--) {

                    // check all genes to determine if a case is altered
                    for (int i = 0; i < matrix.length; i++) {
                        GeneticEvent event = matrix[i][j];
                        if (dataSummary.isGeneAltered(event.getGene(), event.caseCaseId())) {

                            numColumnsToShow = j + 1;
                            break firstAlteration;
                        }
                    }
                }
            }
        }

        // support both SVG and HTML oncoPrints
        switch (theOncoPrintType) {

            case SVG:
                writeSVGOncoPrint(matrix, numColumnsToShow,
                        out, mergedCaseList, geneWithScoreList);
                break;          // exit the switch

            case HTML:
                writeHTMLOncoPrint(out,
								   matrix,
								   dataSummary,
								   caseSets, caseSetId,
								   theOncoPrintSpecParserOutput.getTheOncoPrintSpecification());
                break;          // exit the switch
        }
        return out.toString();
    }

    static void writeSVGOncoPrint(GeneticEvent matrix[][], int numColumnsToShow,
            StringBuffer out, ArrayList<String> mergedCaseList,
            ArrayList<GeneWithScore> geneWithScoreList) {

        int windowWidth = 300 + (CELL_WIDTH * mergedCaseList.size());
        int windowHeight = 50 + (CELL_HEIGHT * geneWithScoreList.size());

        out.append("<?xml version=\"1.0\"?>\n" +
                "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \n" +
                "    \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n" +
                "<svg onload=\"init(evt)\" xmlns=\"http://www.w3.org/2000/svg\" " +
                "version=\"1.1\" \n" +
                "    width=\"" + windowWidth + "\" height=\"" + windowHeight + "\">\n");


        //  Output One Gene per Row
        int x = 0;
        int y = 25;

        out.append("<g font-family=\"Verdana\">");

        for (int i = 0; i < matrix.length; i++) {
            GeneticEvent rowEvent = matrix[i][0];
            x = 160;
            out.append("<text x=\"30\" y = \"" + (y + 15) + "\" fill = \"black\" " +
                    "font-size = \"16\">\n"
                    + rowEvent.getGene().toUpperCase() + "</text>");
            for (int j = 0; j < numColumnsToShow; j++) {
                GeneticEvent event = matrix[i][j];
                String style = getCopyNumberStyle(event);
                String mRNAStyle = getMRNAStyle(event);

                boolean isMutated = event.isMutated();
                int block_height = CELL_HEIGHT - 2;
                out.append("\n<rect x=\"" + x + "\" y=\"" + y
                        + "\" width=\"5\" stroke='" + mRNAStyle + "' height=\""
                        + block_height + "\" fill=\"" + style + "\"\n" +
                        " fill-opacity=\"1.0\"/>");
                if (isMutated) {
                    out.append("\n<rect x='" + x + "' y='" + (y + 5)
                            + "' fill='green' width='5' height='6'/>");
                }

                x += CELL_WIDTH;
            }
            y += CELL_HEIGHT;
        }
        out.append("</g>");
        out.append("</svg>\n");
    }

    /**
     * Gets the Correct Copy Number color for OncoPrint.
     */
    private static String getCopyNumberStyle(GeneticEvent event) {

        switch (event.getCnaValue()) {
            case amplified:
                return "red";
            case Gained:
                return "lightpink";
            case HemizygouslyDeleted:
                return "lightblue";
            case homoDeleted:
                return "blue";
            case diploid:
            case None:
                return "lightgray";
        }
        // TODO: throw exception
        return "shouldNotBeReached"; // never reached
    }

    /**
     * Gets the Correct mRNA color.
     * Displayed in the rectangle boundary.
     */
    private static String getMRNAStyle(GeneticEvent event) {

        switch (event.getMrnaValue()) {

            case upRegulated:
                // if the mRNA is UpRegulated, then pink boundary;
                // see colors at http://www.december.com/html/spec/colorsafecodes.html
                return "#FF9999";
            case notShown:
                // white is the default, not showing mRNA expression level
                return "white";
            case downRegulated:
                // downregulated, then blue boundary
                return "#6699CC";
        }
        // TODO: throw exception
        return "shouldNotBeReached"; // never reached
    }

    /**
     * Generates an OncoPrint in HTML.
	 *
	 * @param out StringBuffer
	 * @param matrix GeneticEvent[][]
	 * @param dataSummary ProfileDataSummary
     * @param caseSets List<CaseList>
     * @param caseSetId String
     * @param theOncoPrintSpecification OncoPrintSpecification
     */
    static void writeHTMLOncoPrint(StringBuffer out,
								   GeneticEvent matrix[][],
								   ProfileDataSummary dataSummary,
								   List<CaseList> caseSets, String caseSetId,
								   OncoPrintSpecification theOncoPrintSpecification) {

		// include some javascript libs
		out.append("<script type=\"text/javascript\" src=\"js/raphael/raphael.js\"></script>\n");
		out.append("<script type=\"text/javascript\" src=\"js/raphaeljs-oncoprint.js\"></script>\n");

		out.append("<script type=\"text/javascript\">\n");
		// output oncoprint variables
		out.append(writeOncoPrintHeaderVariables(matrix, dataSummary, caseSets, caseSetId, "HEADER_VARIABLES"));
		// output longest label variable
		out.append(writeJavascriptConstVariable("LONGEST_LABEL", getLongestLabel(matrix, dataSummary)));
		// output genetic alternation variable
		out.append(writeOncoPrintGeneticAlterationVariable(matrix, dataSummary, "GENETIC_ALTERATIONS_SORTED"));
		// on document ready, draw oncoprint
		out.append(writeOncoPrintDocumentReadyJavascript("oncoprint", "canvas", "GENETIC_ALTERATIONS_SORTED",
														 matrix.length, matrix[0].length, "properties", "LONGEST_LABEL",
														 "oncoprint_header", "HEADER_VARIABLES"));
		out.append("</script>\n");
		out.append("<div id=\"oncoprint_header\" class=\"oncoprint\"></div>\n");
		out.append("<div id=\"oncoprint\" class=\"oncoprint\"></div>\n");

		out.append("<script type=\"text/javascript\">\n");
		String legendFootnote = getLegendFootnote(theOncoPrintSpecification.getUnionOfPossibleLevels());
		out.append(writeJavascriptConstVariable("LEGEND_FOOTNOTE", legendFootnote));
		out.append(writeOncoPrintLegendGeneticAlterationVariable("GENETIC_ALTERATIONS_LEGEND",
																 theOncoPrintSpecification.getUnionOfPossibleLevels()));
		out.append(writeOncoPrintLegendDocumentReadyJavascript("oncoprint_legend", "GENETIC_ALTERATIONS_LEGEND", "LEGEND_FOOTNOTE"));
		out.append("</script>\n");
		out.append("<br>\n");
		out.append("<div id=\"oncoprint_legend\" class=\"oncoprint\"></div>\n");
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
	 * Creates javascript that represents genetic alteration matrix used for OncoPrint rendering.
	 *
	 * @param matrix[][] GeneticEvent
	 * @param dataSummary ProfileDataSummary
	 * @param varName String
	 *
	 * @return String
	 */
	static String writeOncoPrintGeneticAlterationVariable(GeneticEvent matrix[][],
														  ProfileDataSummary dataSummary,
														  String varName) {

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
			// used to track altered samples
			ArrayList<Integer> samplesAltered = new ArrayList<Integer>();
			for (int j = 0; j < matrix[0].length; j++) {
                GeneticEvent event = matrix[i][j];
				if (dataSummary.isGeneAltered(event.getGene(), event.caseCaseId())) {
					samplesAltered.add(j);
				}
                // get level of each datatype; concatenate to make image name
                // color could later could be in configuration file
                String cnaName = event.getCnaValue().name().toUpperCase();
                String mrnaName = event.getMrnaValue().name().toUpperCase();
				String mutationName = (event.isMutated()) ? "MUTATED" : "NORMAL";
				String alterationSettings = cnaName + " | " + mrnaName + " | " + mutationName;
				builder.append("\t\t\t\t\t{ 'sample' : " + j + ", " + "'alteration' : " + alterationSettings + "},\n");
            }
			// zap off last ',\n'
			builder.delete(builder.length()-2, builder.length());
			builder.append("],\n");
			// print list of altered samples
			builder.append("\t\t\t\t 'alteredSamples' : [\n\t\t\t\t\t");
			int sampleCt = 0;
			for (Integer sample : samplesAltered) {
				builder.append(sample + ", ");
				if (++sampleCt % 10 == 0) {
					builder.append("\n\t\t\t\t\t");
				}
			}
			// zap off last '\n\t\t\t\t\t' or ', '
			if (builder.lastIndexOf("\n\t\t\t\t\t") == builder.length()-6) {
				builder.delete(builder.length()-6, builder.length());
			}
			else {
				builder.delete(builder.length()-2, builder.length());
			}
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
			builder.append("\t\t\t\t 'alteration' : AMPLIFIED | NOTSHOWN | NORMAL\n\t\t\t\t},\n");
		}
        if (allPossibleAlterations.satisfy(GeneticDataTypes.CopyNumberAlteration, GeneticTypeLevel.HomozygouslyDeleted)) {
			builder.append("\t\t\t\t{\n\t\t\t\t 'label' : \"Homozygous Deletion\",\n");
			builder.append("\t\t\t\t 'alteration' : HOMODELETED | NOTSHOWN | NORMAL\n\t\t\t\t},\n");
		}
        if (allPossibleAlterations.satisfy(GeneticDataTypes.CopyNumberAlteration, GeneticTypeLevel.Gained)) {
			builder.append("\t\t\t\t{\n\t\t\t\t 'label' : \"Gain\",\n");
			builder.append("\t\t\t\t 'alteration' : GAINED | NOTSHOWN | NORMAL\n\t\t\t\t},\n");
		}
        if (allPossibleAlterations.satisfy(GeneticDataTypes.CopyNumberAlteration, GeneticTypeLevel.HemizygouslyDeleted)) {
			builder.append("\t\t\t\t{\n\t\t\t\t 'label' : \"Hemizygous Deletion\",\n");
			builder.append("\t\t\t\t 'alteration' : HEMIZYGOUSLYDELETED | NOTSHOWN | NORMAL\n\t\t\t\t},\n");
		}
        ResultDataTypeSpec theResultDataTypeSpec = allPossibleAlterations.getResultDataTypeSpec(GeneticDataTypes.Expression);
        if (theResultDataTypeSpec != null) {
			if (theResultDataTypeSpec.getCombinedGreaterContinuousDataTypeSpec() != null) {
				builder.append("\t\t\t\t{\n\t\t\t\t 'label' : \"Up-regulation\",\n");
				builder.append("\t\t\t\t 'alteration' : DIPLOID | UPREGULATED | NORMAL\n\t\t\t\t},\n");
			}
			if (theResultDataTypeSpec.getCombinedLesserContinuousDataTypeSpec() != null) {
				builder.append("\t\t\t\t{\n\t\t\t\t 'label' : \"Down-regulation\",\n");
				builder.append("\t\t\t\t 'alteration' : DIPLOID | DOWNREGULATED | NORMAL\n\t\t\t\t},\n");
			}
		}
        if (allPossibleAlterations.satisfy(GeneticDataTypes.Mutation, GeneticTypeLevel.Mutated)) {
			builder.append("\t\t\t\t{\n\t\t\t\t 'label' : \"Mutation\",\n");
			builder.append("\t\t\t\t 'alteration' : DIPLOID | NOTSHOWN | MUTATED\n\t\t\t\t},\n");
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
	 * @param oncoPrintCanvasParent String
	 * @param canvasID String
	 * @param geneticAlterationsVarName String
	 * @param numGenes int
	 * @param numSamples int
	 * @param propertiesVarName String
	 * @param logestLabelVarName String
	 * @param oncoPrintHeaderCanvasParent String
	 * @param oncoPrintHeaderVariablesVarName String
	 *
	 * @return String
	 */
	static String writeOncoPrintDocumentReadyJavascript(String oncoPrintCanvasParent, String canvasID,
														String geneticAlterationsVarName,
														int numGenes, int numSamples,
														String propertiesVarName,
														String longestLabelVarName,
														String oncoPrintHeaderCanvasParent,
														String oncoPrintHeaderVariablesVarName) {

		StringBuilder builder = new StringBuilder();

		// jquery on document ready
		builder.append("\t$(document).ready(function() {\n");
		// setup default properties
		builder.append("\t\tvar " + propertiesVarName + " = CreateProperties(true);\n");
		// set longest label length
		builder.append("\t\tSetLongestLabelLength(" + 
					   longestLabelVarName + ".get('" + longestLabelVarName + "'));\n");
		// oncoprint heading
		builder.append("\t\tDrawOncoPrintHeader(" + oncoPrintHeaderCanvasParent + ", " +
   					   oncoPrintHeaderVariablesVarName  + ", " + propertiesVarName + ");\n");
		// create canvas
		builder.append("\t\tvar " + canvasID + " = CreateCanvas(" + oncoPrintCanvasParent + ", " +
					   numGenes + ", " + numSamples + ", " + propertiesVarName + ");\n");
		// draw oncoprint
		builder.append("\t\tDrawOncoPrint(" + canvasID + ", " +
					   geneticAlterationsVarName  + ".get('" + geneticAlterationsVarName + "'), " +
					   propertiesVarName + ");\n");
		// end on document ready
		builder.append("\t});\n");

		// outta here
		return builder.toString();
	}

	/**
	 * Creates javascript which invokes (via jquery) OncoPrint Legend drawing 
	 * when document is ready.
	 *
	 * @param canvasParent String
	 * @param geneticAlterationsVarName String
	 * @param legendFootnoteVarName String
	 *
	 * @return String
	 */
	static String writeOncoPrintLegendDocumentReadyJavascript(String canvasParent,
															  String geneticAlterationsVarName,
															  String legendFootnoteVarName) {

		StringBuilder builder = new StringBuilder();

		// jquery on document ready
		builder.append("\t$(document).ready(function() {\n");
		// draw oncoprint legend
		builder.append("\t\tDrawOncoPrintLegend(" + canvasParent + ", " +
					   geneticAlterationsVarName  + ".get('" + geneticAlterationsVarName + "'), " +
   					   legendFootnoteVarName  + ".get('" + legendFootnoteVarName + "'));\n");
		// end on document ready
		builder.append("\t});\n");

		// outta here
		return builder.toString();
	}

	/**
	 * Creates javascript var which contains longest label.
	 *
	 * @param matrix[][] GeneticEvent
	 * @param dataSummary ProfileDataSummary
	 * @param longestLabelVarName String
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