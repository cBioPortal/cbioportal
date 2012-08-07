package org.mskcc.cbio.portal.html;

import org.mskcc.cbio.portal.html.special_gene.SpecialGeneFactory;
import org.mskcc.cbio.portal.html.special_gene.SpecialGene;
import org.mskcc.cbio.portal.util.SequenceCenterUtil;
import org.mskcc.cbio.cgds.model.ExtendedMutation;

import java.util.ArrayList;

/**
 * Utility Class for Creating the Mutation Table.
 *
 * @author Ethan Cerami
 */
public class MutationTableUtil {
    private ArrayList<String> headerList = new ArrayList<String>();
    private SpecialGene specialGene;
    private static final String VALID = "valid";
    private static final String SOMATIC = "somatic";
    private static final String GERMLINE = "germline";

    //  CSS Style Sheet Classes
    private static final String CSS_VALID = "valid";
    private static final String CSS_SOMATIC = "somatic";
    private static final String CSS_GERMLINE = "germline";

    public MutationTableUtil(String geneSymbol) {
        specialGene = SpecialGeneFactory.getInstance(geneSymbol);
        initHeaders();
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
        return HtmlUtil.createTableHeaderRow(headerList);
    }

    public ArrayList<String> getDataFields(ExtendedMutation mutation) {
        ArrayList <String> dataFieldList = new ArrayList<String>();

        //  Case ID.
        dataFieldList.add(HtmlUtil.getSafeWebValue(mutation.getCaseId()));

        //  Basic Mutation Info.
        dataFieldList.add(HtmlUtil.getSafeWebValue(getMutationStatus(mutation)));
        dataFieldList.add(HtmlUtil.getSafeWebValue(mutation.getMutationType()));
        dataFieldList.add(HtmlUtil.getSafeWebValue(getValidationStatus(mutation)));
        dataFieldList.add(HtmlUtil.getSafeWebValue(getSequencingCenter(mutation)));
        dataFieldList.add(HtmlUtil.getSafeWebValue(mutation.getAminoAcidChange()));

        //  OMA Links
        MutationAssessorHtmlUtil omaUtil = new MutationAssessorHtmlUtil(mutation);
        dataFieldList.add(omaUtil.getFunctionalImpactLink());
        dataFieldList.add(omaUtil.getMultipleSequenceAlignmentLink());
        dataFieldList.add(omaUtil.getPdbStructureLink());

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

    private String getValidationStatus(ExtendedMutation mutation) {
        String validationStatus = mutation.getValidationStatus();
        if (validationStatus != null) {
            if (mutation.getValidationStatus().equalsIgnoreCase(VALID)) {
                return HtmlUtil.createTextWithinSpan(validationStatus, CSS_VALID);
            } else {
                return validationStatus;
            }
        } else {
            return null;
        }
    }

    private String getMutationStatus(ExtendedMutation mutation) {
        String mutationStatus = mutation.getMutationStatus();
        if (mutation.getMutationStatus() != null) {
            if (mutation.getMutationStatus().equalsIgnoreCase(SOMATIC)) {
                return HtmlUtil.createTextWithinSpan(mutationStatus, CSS_SOMATIC);
            } else if (mutation.getMutationStatus().equalsIgnoreCase(GERMLINE)) {
                return HtmlUtil.createTextWithinSpan(mutationStatus, CSS_GERMLINE);
            } else {
                return mutationStatus;
            }
        } else {
            return null;
        }
    }

    private void initHeaders() {
        headerList.add("Case ID");
        headerList.add("Mutation Status");
        headerList.add("Mutation Type");
        headerList.add("Validation Status");
        headerList.add("Sequencing Center");
        headerList.add("AA Change");
        headerList.add("Predicted Impact**");
        headerList.add("Alignment");
        headerList.add("Structure");

        //  Add Any Gene-Specfic Headers
        if (specialGene != null) {
            headerList.addAll(specialGene.getDataFieldHeaders());
        }
    }
}