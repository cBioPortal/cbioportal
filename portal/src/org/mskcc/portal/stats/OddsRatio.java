package org.mskcc.portal.stats;

import org.mskcc.portal.model.ProfileDataSummary;

import java.util.ArrayList;

public class OddsRatio {
    private int a = 0, b = 0, c = 0, d = 0;
    private ArrayList<String> caseIdList;
    private StringBuffer buf = new StringBuffer();
    private double oddsRatio;
    private double pValue;
    private double lowerConfidenceInterval;
    private double upperConfidenceInterval;
    private StringBuffer x = new StringBuffer();
    private StringBuffer y = new StringBuffer();

    public OddsRatio(ProfileDataSummary pDataSummary, String geneA, String geneB) {
        caseIdList = pDataSummary.getProfileData().getCaseIdList();
        x.append ("x = c(");
        y.append ("y = c(");
        for (String caseId : caseIdList) {
            boolean valueA = pDataSummary.isGeneAltered(geneA, caseId);
            boolean valueB = pDataSummary.isGeneAltered(geneB, caseId);

            if (valueA) {
                x.append("1,");
            } else {
                x.append("0,");
            }
            if (valueB) {
                y.append("1,");
            } else {
                y.append("0,");
            }

            String valueAStr = pDataSummary.getProfileData().getValue(geneA, caseId);
            String valueBStr = pDataSummary.getProfileData().getValue(geneB, caseId);
            buf.append(valueAStr + ":  " + valueBStr + "<BR>");
            buf.append("--->  " + valueA + ": " + valueB + "<BR>");
            if (valueA == true && valueB == true) {
                d++;
            } else if (valueA == false && valueB == false) {
                a++;
            } else if (valueA == true && valueB == false) {
                c++;
            } else {
                b++;
            }
        }
        x = new StringBuffer(x.substring(0, x.length()-1));
        y = new StringBuffer(y.substring(0, y.length()-1));
        x.append (")");
        y.append (")");

        oddsRatio = ((double) (a * d)) / ((double) (b * c));
        FisherExact fisher = new FisherExact(a + b + c + d);
        pValue = fisher.getCumlativeP(a, b, c, d);
        lowerConfidenceInterval = Math.exp(Math.log(oddsRatio) - 1.96 * (Math.sqrt(1 / (double) a
                + 1 / (double) b + 1 / (double) c + 1 / (double) d)));
        upperConfidenceInterval = Math.exp(Math.log(oddsRatio) + 1.96 * (Math.sqrt(1 / (double) a
                + 1 / (double) b + 1 / (double) c + 1 / (double) d)));

    }

    public String getLog() {
        return buf.toString();
    }

    public double getOddsRatio() {
        return oddsRatio;
    }

    public double getCumulativeP() {
        return pValue;
    }

    public String getRCommand() {
        return "library(\"Kendall\")<BR>m <- matrix (c ("
                + a + ", " + c + "," + b + "," + d + "), nr =2)"
                + "<br>" + "fisher.test (m, alternative=\"less\")<br>" +
                "fisher.test (m, alternative=\"greater\")<BR>" +
                x.toString() + "<BR>" +
                y.toString() + "<BR>" +
                "Kendall(x,y)";
    }

    public int getA() {
        return a;
    }

    public int getB() {
        return b;
    }

    public int getC() {
        return c;
    }

    public int getD() {
        return d;
    }

    public int getNumCases() {
        return caseIdList.size();
    }

    public double getLowerConfidenceInterval() {
        return lowerConfidenceInterval;
    }

    public double getUpperConfidenceInterval() {
        return upperConfidenceInterval;
    }
}
