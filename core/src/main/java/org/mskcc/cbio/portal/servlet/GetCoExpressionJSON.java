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
import org.mskcc.cbio.portal.util.CoExpUtil;

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
    private int resultLength = 250;

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
        String geneSymbol = httpServletRequest.getParameter("gene");
	      String caseSetId = httpServletRequest.getParameter("case_set_id");
        String caseIdsKey = httpServletRequest.getParameter("case_ids_key");

        PearsonsCorrelation pearsonsCorrelation = new PearsonsCorrelation();
        SpearmansCorrelation spearmansCorrelation = new SpearmansCorrelation();
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
        ArrayList<JSONObject> fullResult = new ArrayList<JSONObject>();


        CanonicalGene geneObj = daoGeneOptimized.getGene(geneSymbol);
        Long queryGeneId = geneObj.getEntrezGeneId();

    	  GeneticProfile final_gp = CoExpUtil.getPreferedGeneticProfile(cancerStudyIdentifier);
	      if (final_gp != null) {
            try {
                Map<Long,double[]> map = CoExpUtil.getExpressionMap(final_gp.getGeneticProfileId(), caseSetId, caseIdsKey);
                int mapSize = map.size();
                List<Long> genes = new ArrayList<Long>(map.keySet());

                for (int i = 0; i < mapSize; i++) {
                    double[] query_gene_exp = map.get(queryGeneId);
                    long compared_gene_id = genes.get(i);
                    double[] compared_gene_exp = map.get(compared_gene_id);

                    if (compared_gene_exp != null && query_gene_exp != null) {
                        double pearson = pearsonsCorrelation.correlation(query_gene_exp, compared_gene_exp);
                        if ((pearson > coExpScoreThreshold || pearson < (-1) * coExpScoreThreshold ) &&
                           (compared_gene_id != queryGeneId)){
                            //Only calculate spearman with high scored pearson gene pairs.
                            double spearman = spearmansCorrelation.correlation(query_gene_exp, compared_gene_exp);
                            JSONObject _scores = new JSONObject();
                            CanonicalGene comparedGene = daoGeneOptimized.getGene(compared_gene_id);
                            _scores.put("gene", comparedGene.getHugoGeneSymbolAllCaps());
                            _scores.put("pearson", pearson);
                            _scores.put("spearman", spearman);
                            fullResult.add(_scores);
                        }
                    }
                }
                fullResult = CoExpUtil.sortJsonArr(fullResult, "pearson");
                ArrayList<JSONObject> result = new ArrayList<JSONObject>();
                int _len = (fullResult.size() > resultLength ? resultLength : fullResult.size());
                for (int i = 0; i < _len; i++) {
                    result.add(fullResult.get(i));
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

}



