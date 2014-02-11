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
package org.mskcc.cbio.portal.scripts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.sql.Timestamp;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.Coexpression;
import org.mskcc.cbio.portal.model.GeneticAlterationType;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.util.ProgressMonitor;

/**
 * Calculate the co-expression score (Spearmans and Pearson)
 * between all possible gene pairs within a cancer study
 * filter -- only keep gene pairs with score > 0.3 or < -0.3
 * genetic profile used -- mrna profile, priority: rna_seq (no zscores), and then the rest
 *
 * @param:  cancer study stable id
 *
 * @author jgao, yichao
 * @time nov 2013
 */
public class CalculateCoexpression {

    public static void main(String[] args) throws Exception {
        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(true);

        if (args.length == 0) {
            System.out.println ("You must specify a cancer study stable id.");
            return;
        }
        String cancerStudyStableId = args[0];
        CancerStudy cs = DaoCancerStudy.getCancerStudyByStableId(cancerStudyStableId);
        if ( cs == null ) {
            System.out.println (cancerStudyStableId + " is not a valid cancer study stable id.");
            return;
        }

        ArrayList<GeneticProfile> gps = DaoGeneticProfile.getAllGeneticProfiles(cs.getInternalId());
        GeneticProfile final_gp = null;
        for (GeneticProfile gp : gps) {
            // TODO: support miRNA later
            if (gp.getGeneticAlterationType() == GeneticAlterationType.MRNA_EXPRESSION) {
                //rna seq profile (no z-scores applied) holds the highest priority)
                if (gp.getStableId().toLowerCase().contains("rna_seq") &&
                   !gp.getStableId().toLowerCase().contains("zscores")) {
                    final_gp = gp;
                    break;
                } else if (!gp.getStableId().toLowerCase().contains("zscores")) {
                    final_gp = gp;
                }
            }
        }
        if (final_gp == null) {
            System.out.println("------------------ Calculating Co-expression Score ------------------ ");
            System.out.println("Cancer Study Stable Id: " + cs.getCancerStudyStableId());
            System.out.println("No qualified genetic profile in this cancer study.");
        } else {
            System.out.println("------------------ Calculating Co-expression Score ------------------ ");
            System.out.println("Cancer Study Stable Id: " + cs.getCancerStudyStableId());
            System.out.println("Genetic Profile Chosen: " + final_gp.getStableId());
            calculate(final_gp, pMonitor);
        }
    }

    private static void calculate(GeneticProfile profile, ProgressMonitor pMonitor) throws DaoException {

        int genePairCounter = 0;
        double coExpScoreThreshold = 0;
        int[] scoreStatsPearson = new int[201];  //Score distribution
        int[] scoreStatsSpearman = new int[201];  //Score distribution

        MySQLbulkLoader.bulkLoadOn();

        PearsonsCorrelation pearsonsCorrelation = new PearsonsCorrelation();
        SpearmansCorrelation spearmansCorrelation = new SpearmansCorrelation();

        Date timeStart = new Date();
        System.out.println(new Timestamp(timeStart.getTime()));

        System.out.println("Loading genetic alteration data.....");
        Map<Long,double[]> map = getExpressionMap(profile.getGeneticProfileId());

        int n = map.size();
        //int n = 30;
        pMonitor.setMaxValue(n * (n - 1) / 2);

        System.out.println("Calculating scores of all possible gene pairs......");
        List<Long> genes = new ArrayList<Long>(map.keySet());
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                pMonitor.incrementCurValue();

                long gene1 = genes.get(i);
                double[] exp1 = map.get(gene1);

                long gene2 = genes.get(j);
                double[] exp2 = map.get(gene2);

                double pearson = pearsonsCorrelation.correlation(exp1, exp2);
                double spearman = spearmansCorrelation.correlation(exp1, exp2);

                if (pearson > coExpScoreThreshold || pearson < (-1) * coExpScoreThreshold ||
                        spearman > coExpScoreThreshold || spearman < (-1) * coExpScoreThreshold) {

                    //Coexpression coexpression = new Coexpression(gene1, gene2, profile.getGeneticProfileId(), pearson, spearman);
                    //DaoCoexpression.addCoexpression(coexpression);

                    genePairCounter += 1;

                    if ((int)Math.round(pearson * 100) < 0) {
                        scoreStatsPearson[100 - Math.abs((int)Math.round(pearson * 100))] += 1;
                    } else {
                        scoreStatsPearson[(int)Math.round(pearson * 100) + 100] += 1;
                    }
                    if ((int)Math.round(spearman * 100) < 0) {
                        scoreStatsSpearman[100 - Math.abs((int)Math.round(spearman * 100))] += 1;
                    } else {
                        scoreStatsSpearman[(int)Math.round(spearman * 100) + 100] += 1;
                    }
                }
                MySQLbulkLoader.flushAll();
            }
        }

        System.out.println("Done. ");
        Date timeEnd = new Date();
        System.out.println(new Timestamp(timeEnd.getTime()));
        System.out.println(genePairCounter + " gene pairs loaded.");
        System.out.println("Pearson Score Distribution ...............");
        for (int p_index = 0; p_index < 201; p_index++) {
            System.out.print(scoreStatsPearson[p_index] + ", ");
        }
        System.out.print("\n");
        System.out.println("Spearman Score Distribution ...............");
        for (int s_index = 0; s_index < 201; s_index++) {
            System.out.print(scoreStatsSpearman[s_index] + ", ");
        }
        System.out.print("\n");
    }

    private static Map<Long,double[]> getExpressionMap(int profileId) throws DaoException {
        ArrayList<String> orderedCaseList = DaoGeneticProfileCases.getOrderedCaseList(profileId);
        int nCases = orderedCaseList.size();
        DaoGeneticAlteration daoGeneticAlteration = DaoGeneticAlteration.getInstance();
        Map<Long, HashMap<String, String>> mapStr = daoGeneticAlteration.getGeneticAlterationMap(profileId, null);
        Map<Long, double[]> map = new HashMap<Long, double[]>(mapStr.size());
        for (Map.Entry<Long, HashMap<String, String>> entry : mapStr.entrySet()) {
            Long gene = entry.getKey();
            Map<String, String> mapCaseValueStr = entry.getValue();
            double[] values = new double[nCases];
            for (int i = 0; i < nCases; i++) {
                String caseId = orderedCaseList.get(i);
                String value = mapCaseValueStr.get(caseId);
                Double d;
                try {
                    d = Double.valueOf(value);
                } catch (Exception e) {
                    d = Double.NaN;
                }
                if (d!=null && !d.isNaN()) {
                    values[i]=d;
                }
            }
            map.put(gene, values);
        }
        return map;
    }
}
