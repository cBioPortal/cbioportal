package org.mskcc.portal.tool;

import org.mskcc.portal.model.ExtendedMutation;
import org.mskcc.portal.model.GeneticAlterationType;
import org.mskcc.portal.model.ProfileData;
import org.mskcc.portal.tool.bundle.Bundle;
import org.mskcc.portal.tool.bundle.BundleHelper;
import org.mskcc.portal.tool.bundle.GeneRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class FingerPrint {
    private static int CELL_WIDTH = 8;
    private static int CELL_HEIGHT = 18;

    private static StringBuffer svg = new StringBuffer();

    public FingerPrint(Bundle bundle, ArrayList<BinnedCase> binnedCaseList,
                       HashMap<String, ArrayList<ExtendedMutation>> mutationMap,
                       HashMap<String, ProfileData> cnaMap,
                       HashMap<String, ProfileData> binaryMethylationMap) throws IOException {
        int windowWidth = 300 + (CELL_WIDTH * binnedCaseList.size());
        int windowHeight = 100 + (CELL_HEIGHT * bundle.getGeneRequestList().size());
        svg.append("<?xml version=\"1.0\"?>\n" +
                "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \n" +
                "    \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n" +
                "<svg onload=\"init(evt)\" xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" \n" +
                "    width=\"" + windowWidth + "\" height=\"" + windowHeight + "\">\n");

        //  Output One Case per Row
        int x = 0;
        int y = 50;
        int block_height = CELL_HEIGHT - 2;
        svg.append("<g font-family=\"Verdana\">");

        x = 120;

        y = 70;
        ArrayList<String> geneList = new ArrayList<String>();


        for (GeneRequest geneRequest : bundle.getGeneRequestList()) {
            if (!geneList.contains(geneRequest.getGeneSymbol())) {
                geneList.add(geneRequest.getGeneSymbol());
            }
        }

        for (String gene : geneList) {
            x = 120;
            svg.append("<text x=\"30\" y = \"" + (y + 15) + "\" fill = \"black\" font-size = \"16\">\n"
                    + gene.toUpperCase() + "</text>");
            for (BinnedCase binnedCase : binnedCaseList) {
                String style = getCopyNumberStyle(gene, binnedCase.getCaseId(),
                        cnaMap, mutationMap);
                boolean isEpigeneticallySilenced = BundleHelper.isCaseEpigeneticallySilenced(binnedCase.getCaseId(),
                        gene, binaryMethylationMap);
                String epigenticStyle = getEpigeneticStyle(isEpigeneticallySilenced);
                svg.append("  <rect x=\"" + x + "\" y=\"" + y
                        + "\" width=\"5\" stroke='" + epigenticStyle + "' height=\""
                        + block_height + "\" fill=\"" + style + "\"\n" +
                        " fill-opacity=\"1.0\"/>\n");

                boolean isGermlineMutated = BundleHelper.isCaseGermlineMutated(binnedCase.getCaseId(),
                        gene, mutationMap);
                boolean isSomaticallyMutated = BundleHelper.isCaseSomaticallyMutated(binnedCase.getCaseId(),
                        gene, mutationMap);

                if (isGermlineMutated) {
                    String mutationStyle = getMutationStyle(1);
                    svg.append(" <rect x='" + x + "' y='" + (y + 2)
                            + "' fill='" + mutationStyle + "' width='5' height='6'/>");
                }
                if (isSomaticallyMutated) {
                    String mutationStyle = getMutationStyle(2);
                    svg.append(" <rect x='" + x + "' y='" + (y + 7)
                            + "' fill='" + mutationStyle + "' width='5' height='6'/>");
                }
                x += CELL_WIDTH;
            }
            y += CELL_HEIGHT;
        }
        svg.append("<text x='10' font-size='11' y='" + (y + 20) + "' id='mouseover'></text>");
        svg.append("</g>");
        svg.append("</svg>");
    }

    public String getSvg() {
        return svg.toString();
    }

    private static String getCopyNumberStyle(String gene,
                                             String caseId, HashMap<String, ProfileData> cnaMap,
                                             HashMap<String, ArrayList<ExtendedMutation>> mutationMap) {
        ProfileData pData = cnaMap.get(gene);
        String style = "lightgray";

        if (pData != null) {
            String cna = pData.getValue(gene, caseId);
            if (gene.equalsIgnoreCase("BRCA1") || gene.equalsIgnoreCase("BRCA2")) {
                if (BundleHelper.isCaseSomaticallyMutated(caseId, gene, mutationMap)
                        || BundleHelper.isCaseGermlineMutated(caseId, gene, mutationMap)) {
                    if (cna.equalsIgnoreCase(GeneticAlterationType.HEMIZYGOUS_DELETION)) {
                        style = "#FFCC99";
                    }
                }
            } else {
                if (cna.equals(GeneticAlterationType.HOMOZYGOUS_DELETION)) {
                    style = "blue";
                } else if (cna.equals(GeneticAlterationType.AMPLIFICATION)) {
                    style = "red";
                }
            }
        }
        return style;
    }

    private static String getEpigeneticStyle(boolean silenced) {
        String style = "white";
        if (silenced) {
            style = "blue";
        } else {
            style = "white";
        }
        return style;
    }

    private static String getMutationStyle(int mutationStatus) {
        String style = "white";
        // somatic
        if (mutationStatus == 1) {
            // germline
            style = "purple";
        } else if (mutationStatus == 2) {
            // somatic
            style = "green";
        }
        return style;
    }
}
