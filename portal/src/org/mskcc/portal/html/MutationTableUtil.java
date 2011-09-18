package org.mskcc.portal.html;

import java.util.ArrayList;

/**
 * Utility Class for Creating the Mutation Table.
 *
 * @author Ethan Cerami
 */
public class MutationTableUtil {
    private ArrayList<String> headerList = new ArrayList<String>();
    private String geneSymbol;
    private static final String BRCA1 = "BRCA1";
    private static final String BRCA2 = "BRCA2";

    public MutationTableUtil(String geneSymbol) {
        this.geneSymbol = geneSymbol;
        initHeaders();
    }

    public ArrayList<String> getTableHeaders() {
        return headerList;
    }

    public String getTableHeaderRow() {
        return HtmlUtil.createTableHeaderRow(headerList);
    }

    //  Gets the Table Footer Message (if any)
    public String getTableFooterMessage() {
        if (isBrca1()) {
            return getBrca1FooterMessage();
        } else if (isBrca2()) {
            return getBrca2FooterMessage();
        } else {
            return HtmlUtil.EMPTY_STRING;
        }
    }

    private boolean isBrca1() {
        return geneSymbol.equalsIgnoreCase(BRCA1);
    }

    private boolean isBrca2() {
        return geneSymbol.equalsIgnoreCase(BRCA2);
    }

    private String getBrca1FooterMessage() {
        return ("* Known BRCA1 185/187DelAG and 5382/5385 insC founder mutations " +
                "are shown in bold.");
    }

    private String getBrca2FooterMessage() {
        return ("* Known BRCA2 6174delT founder mutation are shown in bold.");
    }

    private void initHeaders() {
        headerList.add("Case ID");
        headerList.add("Mutation Status");
        headerList.add("Mutation Type");
        headerList.add("Validation Status");
        headerList.add("Sequencing Center");
        headerList.add("Amino Acid Change");
        headerList.add("Predicted Functional Impact**");
        headerList.add("Alignment");
        headerList.add("Structure");

        //  Special Handling for BRCA1/2
        if (isBrca1() || isBrca2()) {
            headerList.add("Nucleotide Position *");
        }
    }
}