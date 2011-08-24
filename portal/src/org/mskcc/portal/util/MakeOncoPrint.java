package org.mskcc.portal.util;

import org.mskcc.portal.model.GeneticEventImpl.CNA;
import org.mskcc.portal.model.GeneticEventImpl.MRNA;
import org.mskcc.portal.model.*;
import org.mskcc.portal.oncoPrintSpecLanguage.*;
import org.mskcc.cgds.model.CaseList;
import org.mskcc.cgds.model.GeneticProfile;

import java.io.IOException;
import java.util.*;

/**
 * Generates the Genomic OncoPrint.
 *
 * @author Ethan Cerami, Arthur Goldberg.
 */
public class MakeOncoPrint {
    public static int CELL_WIDTH = 8;
    public static int CELL_HEIGHT = 18;

    // support OncoPrints in both SVG and HTML; only HTML will have extra textual info,
    // such as legend, %alteration
    public enum OncoPrintType {
        SVG, HTML
    }

    /**
     * Generate the OncoPrint in HTML or SVG.
     * @param geneList              List of Genes.
     * @param mergedProfile         Merged Data Profile.
     * @param caseSets              All Case Sets for this Cancer Study.
     * @param caseSetId             Selected Case Set ID.
     * @param zScoreThreshold       Z-Score Threshhold
     * @param theOncoPrintType      OncoPrint Type.
     * @param showAlteredColumns    Show only the altered columns.
     * @param geneticProfileIdSet   IDs for all Genomic Profiles.
     * @param profileList           List of all Genomic Profiles.
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
                int spacing = 0;
                int padding = 1;

                // the images are 4x bigger than this, which is necessary to create the necessary
                // design; but width and height scale them down, so that the OncoPrint fits
                // TODO: move these constants elsewhere, or derive from images directly
                int width = 6;
                int height = 17;

                writeHTMLOncoPrint(caseSets, caseSetId, matrix, numColumnsToShow, showAlteredColumns,
                        theOncoPrintSpecParserOutput.getTheOncoPrintSpecification(), dataSummary,
                        out, spacing, padding, width, height, includeCaseSetDescription,
                        includeLegend);
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
            x = 120;
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
     * Generates an OncoPrint in HTML.
     * @param caseSets                  List of all Case Sets.
     * @param caseSetId                 Selected Case Set ID.
     * @param matrix                    Matrix of Genomic Events.
     * @param numColumnsToShow          Number of Columns to Show.
     * @param showAlteredColumns        Flag to show only altered columns.
     * @param theOncoPrintSpecification The OncoPrint Spec. Object.
     * @param dataSummary               Data Summary Object.
     * @param out                       HTML Out.
     * @param cellspacing               Cellspacing.
     * @param cellpadding               Cellpadding.
     * @param width                     Width.
     * @param height                    Height.
     */
    static void writeHTMLOncoPrint(ArrayList<CaseList> caseSets, String caseSetId,
            GeneticEvent matrix[][],
            int numColumnsToShow, boolean showAlteredColumns,
            OncoPrintSpecification theOncoPrintSpecification,
            ProfileDataSummary dataSummary,
            StringBuffer out,
            int cellspacing, int cellpadding, int width, int height,
            boolean includeCaseSetDescription,
            boolean includeLegend) {

        out.append("<script type=\"text/javascript\" src=\"js/jquery.min.js\"></script>\n" +
                "<script type=\"text/javascript\" src=\"js/jquery.tipTip.minified.js\"></script>") ;


        /*out.append("<script type=\"text/javascript\">\n"+
                    "$(document).ready(function(){  \n" +
                    "$(\".oncoprint_help\").tipTip({defaultPosition: \"right\", delay:\"100\", edgeOffset: 25});\n" +
                    "});\n" +
                    "</script>\n");
        */
        out.append("<div class=\"oncoprint\">\n");
        if (includeCaseSetDescription) {
            for (CaseList caseSet : caseSets) {
                if (caseSetId.equals(caseSet.getStableId())) {
                    out.append(
                            "<p>Case Set: " + caseSet.getName()
                                    + ":  " + caseSet.getDescription() + "</p>");
                }
            }
        }

        // stats on pct alteration
        out.append("<p>Altered in " + dataSummary.getNumCasesAffected() + " (" +
                alterationValueToString(dataSummary.getPercentCasesAffected())
                + ") of cases." + "</p>");

        // output table header
        out.append(
                "\n<table cellspacing='" + cellspacing +
                        "' cellpadding='" + cellpadding +
                        "'>\n" +
                        "<thead>\n"
        );

        int columnWidthOfLegend = 80;
        //  heading that indicates columns are cases
        // span multiple columns like legend
        String caseHeading;
        int numCases = matrix[0].length;
        String rightArrow =  " &rarr;";
        if (showAlteredColumns) {
            caseHeading = pluralize(dataSummary.getNumCasesAffected(), " case")
                    + " with altered genes, out of " + pluralize(numCases, " total case") + rightArrow;
        } else {
            caseHeading = "All " + pluralize(numCases, " case") + rightArrow;
        }

        out.append("\n<tr><th></th><th valign='bottom' width=\"50\">Total altered</th>\n<th colspan='"
                + columnWidthOfLegend + "' align='left'>" + caseHeading + "</th>\n</tr>");
        out.append("</thead>");

        for (int i = 0; i < matrix.length; i++) {
            GeneticEvent rowEvent = matrix[i][0];

            // new row
            out.append("<tr>");

            // output cell with gene name, CSS does left justified
            out.append("<td>" + rowEvent.getGene().toUpperCase() + "</td>\n");

            // output total % altered, right justified
            out.append("<td style=\" text-align: right\">");
            out.append(alterationValueToString(dataSummary.getPercentCasesWhereGeneIsAltered
                    (rowEvent.getGene())));
            out.append("</td>\n");

            // for each case
            for (int j = 0; j < numColumnsToShow; j++) {
                GeneticEvent event = matrix[i][j];

                // get level of each datatype; concatenate to make image name
                // color could later could be in configuration file
                GeneticEventImpl.CNA CNAlevel = event.getCnaValue();
                GeneticEventImpl.MRNA MRNAlevel = event.getMrnaValue();

                // construct filename of icon representing this gene's genetic alteration event
                StringBuffer iconFileName = new StringBuffer();

                // TODO: fix; IMHO this is wrong; diploid should be different from None
                String cnaName = CNAlevel.name();
                if (cnaName.equals("None")) {
                    cnaName = "diploid";
                }
                iconFileName.append(cnaName);
                iconFileName.append("-");

                iconFileName.append(MRNAlevel.name());
                iconFileName.append("-");

                if (event.isMutated()) {
                    iconFileName.append("mutated");
                } else {
                    iconFileName.append("normal");
                }
                iconFileName.append(".png");



                out.append("<td class='op_data_cell'>"

                        + IMG(iconFileName.toString(), width, height, event.caseCaseId())

                        // temporary tooltip = event.toString()+"\n"+event.caseCaseId()
                        //+ IMG(iconFileName.toString(), width, height, event.toString()+"&lt;br/&gt;"+event.caseCaseId())
                        + "</td>\n");

            }

            // TODO: learn how to fix: maybe Caitlin knows
            // ugly hack to make table wide enough to fit legend
            for (int c = numColumnsToShow; c < columnWidthOfLegend; c++) {
                out.append("<td></td>\n");
            }

        }
        out.append ("\n");

        // write table with legend
        out.append("</tr>");
        if (includeLegend) {
            out.append("<tr>");
            writeLegend(out, theOncoPrintSpecification.getUnionOfPossibleLevels(), 2,
                    columnWidthOfLegend, width, height, cellspacing, cellpadding, width, 0.75f);

            out.append("</table>");
            out.append("</div>");
        }
    }

    // pluralize a count + name; dumb, because doesn't consider adding 'es' to pluralize
    static String pluralize(int num, String s) {
        if (num == 1) {
            return new String(num + s);
        } else {
            return new String(num + s + "s");
        }
    }

    /**
     * format percentage
     * <p/>
     * if value == 0 return "--"
     * case value
     * 0: return "--"
     * 0<value<=0.01: return "<1%"
     * 1<value: return "<value>%"
     *
     * @param value
     * @return
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

    // directory containing images
    static String imageDirectory = "images/oncoPrint/";

    static String IMG(String theImage, int width, int height) {
        return IMG(theImage, width, height, null);
    }

    static String IMG(String theImage, int width, int height, String toolTip) {
        StringBuffer sb = new StringBuffer();
        sb.append("<img src='" + imageDirectory + theImage +
                "' alt='" + theImage + // TODO: FOR PRODUCTION; real ALT, should be description of genetic alteration
                "' width='" + width + "' height='" + height+"'");
        if (null != toolTip) {
            sb.append(" class=\"oncoprint_help\" title=\"" + toolTip + "\"");
        }
        return sb.append("/>").toString();
    }

    /**
     * write legend for HTML OncoPrint
     *
     * @param out         writer for the output
     * @param width       width of an icon
     * @param height      height of an icon
     * @param cellspacing TABLE attribute
     * @param cellpadding TABLE attribute
     * @param horizontalSpaceAfterDescription
     *                    blank space, in pixels, after each description
     */
    public static void writeLegend(StringBuffer out, OncoPrintGeneDisplaySpec allPossibleAlterations,
            int colsIndented, int colspan, int width, int height,
            int cellspacing, int cellpadding,
            int horizontalSpaceAfterDescription, float gap) {

        int rowHeight = (int) ((1.0 + gap) * height);

        // indent in enclosing table
        // for horiz alignment, skip colsIndented columns
        for (int i = 0; i < colsIndented; i++) {
            out.append("<td></td>");
        }

        // TODO: FIX; LOOKS BAD WHEN colspan ( == number of columns == cases) is small
        out.append("<td colspan='" + colspan + "'>");

        // output table header
        out.append(
                "\n<table cellspacing='" + cellspacing +
                        "' cellpadding='" + cellpadding + "'>" +
                        "\n<tbody>");

        out.append("\n<tr>");

        /*
        * TODO: make this data driven; use enumerations
        */
        // { "amplified-notShown-normal", "Amplification" }
        if (allPossibleAlterations.satisfy(GeneticDataTypes.CopyNumberAlteration,
                GeneticTypeLevel.Amplified)) {
            outputLegendEntry(out, "amplified-notShown-normal", "Amplification",
                    rowHeight, width, height,
                    horizontalSpaceAfterDescription);
        }
        // { "homoDeleted-notShown-normal", "Homozygous Deletion" },
        if (allPossibleAlterations.satisfy(GeneticDataTypes.CopyNumberAlteration,
                GeneticTypeLevel.HomozygouslyDeleted)) {
            outputLegendEntry(out, "homoDeleted-notShown-normal", "Homozygous Deletion",
                    rowHeight, width, height,
                    horizontalSpaceAfterDescription);
        }
        // { "Gained-notShown-normal", "Gain" },
        if (allPossibleAlterations.satisfy(GeneticDataTypes.CopyNumberAlteration,
                GeneticTypeLevel.Gained)) {
            outputLegendEntry(out, "Gained-notShown-normal", "Gain",
                    rowHeight, width, height,
                    horizontalSpaceAfterDescription);
        }

        // { "HemizygouslyDeleted-notShown-normal", "Hemizygous Deletion" },
        if (allPossibleAlterations.satisfy(GeneticDataTypes.CopyNumberAlteration,
                GeneticTypeLevel.HemizygouslyDeleted)) {
            outputLegendEntry(out, "HemizygouslyDeleted-notShown-normal", "Hemizygous Deletion",
                    rowHeight, width, height,
                    horizontalSpaceAfterDescription);
        }

        // { "diploid-upRegulated-normal", "Up-regulation" },
        ResultDataTypeSpec theResultDataTypeSpec = allPossibleAlterations
                .getResultDataTypeSpec(GeneticDataTypes.Expression);
        if (null != theResultDataTypeSpec &&
                (null != theResultDataTypeSpec.getCombinedGreaterContinuousDataTypeSpec())) {
            outputLegendEntry(out, "diploid-upRegulated-normal", "Up-regulation", rowHeight,
                    width, height,
                    horizontalSpaceAfterDescription);
        }

        // { "diploid-downRegulated-normal", "Down-regulation" },
        theResultDataTypeSpec = allPossibleAlterations.getResultDataTypeSpec
                (GeneticDataTypes.Expression);
        if (null != theResultDataTypeSpec &&
                (null != theResultDataTypeSpec.getCombinedLesserContinuousDataTypeSpec())) {
            outputLegendEntry(out, "diploid-downRegulated-normal", "Down-regulation",
                    rowHeight, width, height,
                    horizontalSpaceAfterDescription);
        }

        // { "diploid-notShown-mutated", "Mutation" },
        if (allPossibleAlterations.satisfy(GeneticDataTypes.Mutation, GeneticTypeLevel.Mutated)) {
            outputLegendEntry(out, "diploid-notShown-mutated", "Mutation", rowHeight, width, height,
                    horizontalSpaceAfterDescription);
        }

        out.append("</tr>\n");
        if (allPossibleAlterations.satisfy(GeneticDataTypes.CopyNumberAlteration)) {
            out.append("<tr>\n");
            out.append("<td colspan='" + colspan / 4
                    + "' style=\"vertical-align:bottom\" >"
                    + "<div class=\"tiny\"> Copy number alterations are putative.<br/></div></td>\n");
            out.append("</tr>");
        }
        out.append("</tbody></table></td></tr>");
    }

    private static void outputLegendEntry(StringBuffer out, String imageName,
            String imageDescription, int rowHeight, int width, int height,
            int horizontalSpaceAfterDescription) {
        out.append("<td height='" + rowHeight + "' style=\"vertical-align:bottom\" >"
                + IMG(imageName + ".png", width, height));
        out.append("</td>\n");
        out.append("<td height='" + rowHeight + "' style=\"vertical-align:bottom\" >"
                + imageDescription);

        // add some room after description
        out.append("</td>\n");
        out.append("<td width='" + horizontalSpaceAfterDescription + "'></td>\n");

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
}