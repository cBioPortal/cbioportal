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

package org.mskcc.cbio.portal.servlet;

import java.io.*;
import java.util.*;

import org.json.simple.JSONObject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONValue;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

/**
 * Get the top co-expressed genes for queried genes
 *
 * @param : cancer study id
 * @param : queried genes
 * @return : JSON objects of co-expression under the same cancer_study
 * (but always the mrna genetic profile)
 */
public class GetCoExpressionJSON extends HttpServlet  {

    private double coExpScoreThreshold = 0.3;
    private Collection<Long> queryGenes = new ArrayList<Long>();  //queried genes' Id



    /**
     * Handles HTTP GET Request.
     *
     * @param httpServletRequest  HttpServletRequest
     * @param httpServletResponse HttpServletResponse
     * @throws ServletException
     */
    protected void doGet(HttpServletRequest httpServletRequest,
                         HttpServletResponse httpServletResponse) throws ServletException, IOException {
        doPost(httpServletRequest, httpServletResponse);
    }

    /**
     * Handles the HTTP POST Request.
     *
     * @param httpServletRequest  HttpServletRequest
     * @param httpServletResponse HttpServletResponse
     * @throws ServletException
     */
    protected void doPost(HttpServletRequest httpServletRequest,
                          HttpServletResponse httpServletResponse) throws ServletException, IOException {

        String cancerStudyIdentifier = httpServletRequest.getParameter("cancer_study_id");
        String geneListStr = httpServletRequest.getParameter("gene_list");

        PearsonsCorrelation pearsonsCorrelation = new PearsonsCorrelation();
        //SpearmansCorrelation spearmansCorrelation = new SpearmansCorrelation();
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
        ArrayList<JSONObject> result = new ArrayList<JSONObject>();

        //Convert gene symbol (string) to gene ID (int)
        String[] geneList = geneListStr.split("\\s+");
        for (String gene_item : geneList) {
            CanonicalGene geneObj = daoGeneOptimized.getGene(gene_item);
            queryGenes.add(geneObj.getEntrezGeneId());
        }

        GeneticProfile final_gp = getPreferedGeneticProfile(cancerStudyIdentifier);
        if (final_gp != null) {
            try {
                Map<Long,double[]> map = getExpressionMap(final_gp.getGeneticProfileId());
                int mapSize = map.size();
                List<Long> genes = new ArrayList<Long>(map.keySet());


                for (Long i_queryGene : queryGenes) {
                    CanonicalGene queryGene = daoGeneOptimized.getGene(i_queryGene);
                    String queryGeneSymbol = queryGene.getHugoGeneSymbolAllCaps();
                    for (int i = 0; i < mapSize; i++) {
                        double[] query_gene_exp = map.get(i_queryGene);
                        long compared_gene_id = genes.get(i);
                        double[] compared_gene_exp = map.get(compared_gene_id);

                        if (compared_gene_exp != null && query_gene_exp != null) {
                            double pearson = pearsonsCorrelation.correlation(query_gene_exp, compared_gene_exp);
                            // double spearman = spearmansCorrelation.correlation(query_gene_exp, compared_gene_exp);

                            if ((pearson > coExpScoreThreshold || pearson < (-1) * coExpScoreThreshold ) &&
                                //(spearman > coExpScoreThreshold || spearman < (-1) * coExpScoreThreshold) &&
                               (compared_gene_id != i_queryGene)){

                                //Configure result datum
                                JSONObject _scores = new JSONObject();
                                //translate gene id to gene symbol
                                CanonicalGene comparedGene = daoGeneOptimized.getGene(compared_gene_id);
                                _scores.put("gene1", queryGeneSymbol);
                                _scores.put("gene2", comparedGene.getHugoGeneSymbolAllCaps());
                                _scores.put("pearson", pearson);
                                //_scores.put("spearman", spearman);

                                result.add(_scores);
                            }
                        }
                    }
                }

                httpServletResponse.setContentType("application/json");
                PrintWriter out = httpServletResponse.getWriter();
                JSONValue.writeJSONString(result, out);
            } catch (DaoException e) {
                System.out.println(e.getMessage());
            }
        } else {
            httpServletResponse.setContentType("text/html");
            PrintWriter out = httpServletResponse.getWriter();
            out.print("Error:  No genetic profiles available for: " + cancerStudyIdentifier);
            out.flush();
        }

    }

    private GeneticProfile getPreferedGeneticProfile(String cancerStudyIdentifier) {
        CancerStudy cs = DaoCancerStudy.getCancerStudyByStableId(cancerStudyIdentifier);
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
        return final_gp;
    }

    private Map<Long,double[]> getExpressionMap(int profileId) throws DaoException {
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