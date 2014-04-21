/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center 
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center 
 * has been advised of the possibility of such damage.
*/

package org.mskcc.cbio.portal.stats;

import org.mskcc.cbio.portal.model.ProfileDataSummary;

import java.util.ArrayList;

public class OddsRatioTemp {
    private int a = 0, b = 0, c = 0, d = 0;
    private ArrayList<String> caseIdList;
    private StringBuffer buf = new StringBuffer();
    private double oddsRatio;
    private double pValue;
    private double lowerConfidenceInterval;
    private double upperConfidenceInterval;

    public OddsRatioTemp(ProfileDataSummary pDataSummary) {
        caseIdList = pDataSummary.getProfileData().getCaseIdList();
        for (String caseId : caseIdList) {
            boolean brca1Value = pDataSummary.isGeneAltered("BRCA1", caseId);
            boolean brca2Value = pDataSummary.isGeneAltered("BRCA2", caseId);
            boolean valueA = false;
            if (brca1Value || brca2Value) {
                valueA = true;
            }

            boolean valueB = pDataSummary.isGeneAltered("C11orf30", caseId);
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
                "fisher.test (m, alternative=\"greater\")<BR>";
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