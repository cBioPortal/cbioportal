package org.mskcc.portal.util;

import org.mskcc.portal.model.*;
import org.mskcc.portal.model.GeneticEventImpl.CNA;
import org.mskcc.portal.model.GeneticEventImpl.MRNA;
import org.mskcc.portal.oncoPrintSpecLanguage.*;
import org.mskcc.portal.servlet.QueryBuilder;
import org.mskcc.portal.servlet.ServletXssUtil;
import org.owasp.validator.html.PolicyException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
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
     * Generates SVG for the Genomic Fingerprint.
     *
     * @param request  HTTP Request.
     * @param response HTTP Response.
     * @throws IOException IO Error.
     */
    public static void makeOncoPrint(HttpServletRequest request,
            HttpServletResponse response,
            OncoPrintType theOncoPrintType,
            boolean showAlteredColumns,
            HashSet<String> geneticProfileIdSet,
            ArrayList<GeneticProfile> profileList
    ) throws IOException {
        PrintWriter writer = response.getWriter();
        ProfileData mergedProfile = (ProfileData)
                request.getAttribute(QueryBuilder.MERGED_PROFILE_DATA_INTERNAL);
        double zScoreThreshold = ZScoreUtil.getZScore(geneticProfileIdSet, profileList, request);

        String geneList = null;
        try {
            ServletXssUtil xssUtil = ServletXssUtil.getInstance();
            geneList = xssUtil.getCleanInput(request, QueryBuilder.GENE_LIST);
        } catch (PolicyException e) {

        }

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
        // combined because these are represented by the same color in the OncoPring
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
                        theOncoPrintSpecParserOutput.getTheOncoPrintSpecification(),
                        writer, mergedCaseList, geneWithScoreList);
                writer.flush();
                writer.close();
                break;          // exit the switch

            case HTML:
                int spacing = 0;
                int padding = 1;

                // the images are 4x bigger than this, which is necessary to create the necessary
                // design; but width and height scale them down, so that the OncoPrint fits
                // TODO: move these constants elsewhere, or derive from images directly
                int width = 6;
                int height = 17;

                writeHTMLOncoPrint(request, matrix, numColumnsToShow, showAlteredColumns,
                        theOncoPrintSpecParserOutput.getTheOncoPrintSpecification(), dataSummary,
                        writer, spacing, padding, width, height);
                writer.flush();
                writer.close();

                break;          // exit the switch
        }
    }

    static void writeSVGOncoPrint(GeneticEvent matrix[][], int numColumnsToShow,
            OncoPrintSpecification theOncoPrintSpecification,
            PrintWriter writer,
            ArrayList<String> mergedCaseList,
            ArrayList<GeneWithScore> geneWithScoreList) {

        int windowWidth = 300 + (CELL_WIDTH * mergedCaseList.size());
        int windowHeight = 50 + (CELL_HEIGHT * geneWithScoreList.size());

        writer.write("<?xml version=\"1.0\"?>\n" +
                "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \n" +
                "    \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n" +
                "<svg onload=\"init(evt)\" xmlns=\"http://www.w3.org/2000/svg\" " +
                "version=\"1.1\" \n" +
                "    width=\"" + windowWidth + "\" height=\"" + windowHeight + "\">\n");

        //  Output One Gene per Row
        int x = 0;
        int y = 25;

        writer.write("<g font-family=\"Verdana\">");

        for (int i = 0; i < matrix.length; i++) {
            GeneticEvent rowEvent = matrix[i][0];
            x = 120;
            writer.write("<text x=\"30\" y = \"" + (y + 15) + "\" fill = \"black\" " +
                    "font-size = \"16\">\n"
                    + rowEvent.getGene().toUpperCase() + "</text>");
            for (int j = 0; j < numColumnsToShow; j++) {
                GeneticEvent event = matrix[i][j];
                String style = getCopyNumberStyle(event);
                String mRNAStyle = getMRNAStyle(event);

                boolean isMutated = event.isMutated();
                int block_height = CELL_HEIGHT - 2;
                writer.write("\n<rect x=\"" + x + "\" y=\"" + y
                        + "\" width=\"5\" stroke='" + mRNAStyle + "' height=\""
                        + block_height + "\" fill=\"" + style + "\"\n" +
                        " fill-opacity=\"1.0\"/>");
                if (isMutated) {
                    writer.write("\n<rect x='" + x + "' y='" + (y + 5)
                            + "' fill='green' width='5' height='6'/>");
                }

                x += CELL_WIDTH;
            }
            y += CELL_HEIGHT;
        }
        writer.write("</g>");
        writer.write("</svg>\n");
    }

    /**
     * Generate an OncoPrint in HTML
     *
     * @param matrix
     * @param numColumnsToShow
     * @param out
     * @param geneCounterPrefs
     * @param cellspacing
     * @param cellpadding
     * @param width            width of ticks
     * @param height           height of ticks
     */
    static void writeHTMLOncoPrint(HttpServletRequest request, GeneticEvent matrix[][],
            int numColumnsToShow, boolean showAlteredColumns,
            OncoPrintSpecification theOncoPrintSpecification,
            ProfileDataSummary dataSummary,
            PrintWriter out,
            int cellspacing, int cellpadding, int width, int height) {

        /* provenance; comment in generated HTML "cbio MSKCC cancer genomics data portal, <date>" */
        Date now = new Date();
        out.print("<title>cBio Cancer Genomics Pathway Portal::Results</title>\n");
        out.print("<! -- MSKCC cBio Cancer Genomics Pathway Portal; " + now + "-->\n");

        out.print("<link href=\"css/style.css\" type=\"text/css\" rel=\"stylesheet\" >\n");
        out.print("<div CLASS=\"oncoprint\">\n");

        // CASE SET AND ID
        ArrayList<CaseSet> caseSets = (ArrayList<CaseSet>)
                request.getAttribute(QueryBuilder.CASE_SETS_INTERNAL);
        String caseSetId = (String) request.getAttribute(QueryBuilder.CASE_SET_ID);

        for (CaseSet caseSet : caseSets) {
            if (caseSetId.equals(caseSet.getId())) {
                out.print(
                        "<P>Case Set: " + caseSet.getName()
                                + ":  " + caseSet.getDescription() + "</P>");
            }
        }

        // stats on pct alteration
        out.print("<P>Altered in " + dataSummary.getNumCasesAffected() + " (" +
                alterationValueToString(dataSummary.getPercentCasesAffected())
                + ") of cases." + "</P>");

        // output table header
        out.print(
                "\n<TABLE FRAME=\"void\" border=\"1\" rules=\"none\" cellspacing=" + cellspacing +
                        " cellpadding=" + cellpadding +
                        ">\n" +
                        "<THEAD>\n"
        );

        int columnWidthOfLegend = 80;
        //  heading that indicates columns are cases
        // span multiple columns like legend
        String caseHeading;
        int numCases = matrix[0].length;
        if (showAlteredColumns) {
            caseHeading = pluralize(dataSummary.getNumCasesAffected(), " case")
                    + " with altered genes, out of " + pluralize(numCases, " total case") + " -->";
        } else {
            caseHeading = "All " + pluralize(numCases, " case") + " -->";
        }

        out.print("<TR><TH><TH width=\"50\">Total altered<TH colspan="
                + columnWidthOfLegend + " align=left >" + caseHeading + "<TBODY>");

        for (int i = 0; i < matrix.length; i++) {
            GeneticEvent rowEvent = matrix[i][0];

            // new row
            out.println();
            out.print("<TR>");

            // output cell with gene name, CSS does left justified
            out.print("<TD>" + rowEvent.getGene().toUpperCase() + "</TD>");

            // output total % altered, right justified
            out.print("<TD style=\" text-align: right\">");
            out.print(alterationValueToString(dataSummary.getPercentCasesWhereGeneIsAltered
                    (rowEvent.getGene())));
            out.print("</TD>");

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

                out.println("<TD class=op_data_cell>"
                        + IMG(iconFileName.toString(), width, height, event.caseCaseId())
                        + "</TD>");

            }

            // TODO: learn how to fix: maybe Caitlin knows
            // ugly hack to make table wide enough to fit legend
            for (int c = numColumnsToShow; c < columnWidthOfLegend; c++) {
                out.print("<TD></TD>");
            }

        }
        out.println();

        // write table with legend
        out.println("<TR>");
        writeLegend(out, theOncoPrintSpecification.getUnionOfPossibleLevels(), 2,
                columnWidthOfLegend, width, height, cellspacing, cellpadding, width, 0.75f);

        out.println("</TABLE>");
        out.println("</DIV>");
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
        /*
        if( value == 0.0 ) {
            return "--";
        }
        */
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

    /**
     * write an IMG tag of the given dimensions, with an optional tooltip
     *
     * @param theImage
     * @param width
     * @param height
     * @param toolTip
     * @return
     */

    static String IMG(String theImage, int width, int height, String toolTip) {
        StringBuffer sb = new StringBuffer();
        sb.append("<IMG SRC='" + imageDirectory + theImage +
                "' ALT='" + theImage + // TODO: FOR PRODUCTION; real ALT, should be description of genetic alteration
                "' WIDTH=" + width + " HEIGHT=" + height);
        if (null != toolTip) {
            sb.append("class=\"Tips1\" title=\"" + toolTip + "\"");
        }
        return sb.append(">").toString();
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
    public static void writeLegend(PrintWriter out, OncoPrintGeneDisplaySpec allPossibleAlterations,
            int colsIndented, int colspan, int width, int height,
            int cellspacing, int cellpadding,
            int horizontalSpaceAfterDescription, float gap) {

        int rowHeight = (int) ((1.0 + gap) * height);

        // indent in enclosing table
        // for horiz alignment, skip colsIndented columns
        for (int i = 0; i < colsIndented; i++) {
            out.print("<TD height=" + rowHeight + ">");
        }

        // TODO: FIX; LOOKS BAD WHEN colspan ( == number of columns == cases) is small
        out.print("<TD colspan=" + colspan + " height=" + rowHeight + ">");

        // output table header
        out.print(
                "\n<TABLE FRAME=\"void\" border=\"1\" rules=\"none\" cellspacing=" + cellspacing +
                        " cellpadding=" + cellpadding + ">" +
                        "\n<TBODY>");

        out.print("\n<TR height=" + rowHeight + ">");
        //System.err.println( "allPossibleAlterations: " + allPossibleAlterations);         

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

        out.println();
        if (allPossibleAlterations.satisfy(GeneticDataTypes.CopyNumberAlteration)) {
            out.println("<TR height=" + rowHeight / 2 + ">");
            out.println("<TD height=" + rowHeight / 2 + " colspan=" + colspan / 4
                    + " style=\"vertical-align:bottom\" >"
                    + "<DIV class=\"tiny\"> Copy number alterations are putative.<br></DIV></TD>");
            out.println("</TR>");
        }
        out.println("</TBODY></TABLE>");
    }

    private static void outputLegendEntry(PrintWriter out, String imageName,
            String imageDescription, int rowHeight, int width, int height,
            int horizontalSpaceAfterDescription) {
        out.println("<TD height=" + rowHeight + " style=\"vertical-align:bottom\" >"
                + IMG(imageName + ".png", width, height));
        out.println("<TD height=" + rowHeight + " style=\"vertical-align:bottom\" >"
                + imageDescription);

        // add some room after description
        out.print("<TD WIDTH=" + horizontalSpaceAfterDescription + ">");

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

    /*
    enum ExpressionDisplayLevels {
        upRegulated, notShown, downRegulated
    };
    */

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

    private static String formatHTMLtag(boolean endTag, String tag, Object... attributeValuePairs) {
        StringBuffer sb = new StringBuffer();
        // error if tag contains whitespace
        if (tag.matches("\\s")) {
            throw new IllegalArgumentException("tag contains whitespace");
        }
        sb.append("<" + tag);

        // error if args aren't even length
        if (0 != attributeValuePairs.length % 2) {
            throw new IllegalArgumentException("Need pairs of attributes and values");
        }
        for (int i = 0; i < attributeValuePairs.length; i += 2) {

            // error if attribute name contains whitespace
            String name = (String) attributeValuePairs[i];
            if (name.matches("\\s")) {
                throw new IllegalArgumentException("attribute name contains whitespace");
            }
            sb.append(attributeValuePairs[i] + "=");
            sb.append("\"" + attributeValuePairs[i + 1] + "\"");
        }
        sb.append("<" + endTag + ">\n");
        return sb.append("\n").toString();
    }
}