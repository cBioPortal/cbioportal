/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.servlet;

import java.io.*;
import java.util.*;
import java.lang.Math;

import org.json.simple.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.json.simple.JSONValue;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.mskcc.cbio.portal.util.AccessControl;
import org.mskcc.cbio.portal.util.CoExpUtil;
import org.mskcc.cbio.portal.util.SpringUtil;
import org.mskcc.cbio.portal.util.XssRequestWrapper;

/**
 * Get the top co-expressed genes for queried genes
 *
 * @param : cancer study id
 * @param : queried genes
 * @return : JSON objects of co-expression under the same cancer_study
 * (but always the mrna genetic profile)
 */
public class GetCoExpressionJSON extends HttpServlet {

    private double coExpScoreThreshold = 0.3;
    //private int resultLength = 250;
    
    // class which process access control to cancer studies
    private AccessControl accessControl;
    
    /**
     * Initializes the servlet.
     */
    public void init() throws ServletException {
        super.init();
        accessControl = SpringUtil.getAccessControl();
    }

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
        CancerStudy cancerStudy = null;
        ArrayList<JsonNode> fullResultJson = new ArrayList<JsonNode>();
        ObjectMapper mapper = new ObjectMapper();
        httpServletResponse.setContentType("application/json");
        PrintWriter out = httpServletResponse.getWriter();
        try{
        	if(cancerStudyIdentifier != null) {
        		cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyIdentifier);
                if (cancerStudy == null || accessControl.isAccessibleCancerStudy(cancerStudy.getCancerStudyStableId()).size() == 0) {
                	 mapper.writeValue(out, fullResultJson);
                	 return;
                }
        	} else {
        		mapper.writeValue(out, fullResultJson);
           	 return;
        	}
        } catch (DaoException e) {
            System.out.println(e.getMessage());
            return;
        }
        
        String geneSymbol = httpServletRequest.getParameter("gene");
        if (httpServletRequest instanceof XssRequestWrapper) {
            geneSymbol = ((XssRequestWrapper) httpServletRequest).getRawParameter("gene");
        }
        String profileId = httpServletRequest.getParameter("profile_id");
        String caseSetId = httpServletRequest.getParameter("case_set_id");
        String caseIdsKey = httpServletRequest.getParameter("case_ids_key");
        boolean isFullResult = Boolean.parseBoolean(httpServletRequest.getParameter("is_full_result"));

        PearsonsCorrelation pearsonsCorrelation = new PearsonsCorrelation();
        SpearmansCorrelation spearmansCorrelation = new SpearmansCorrelation();
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();

        CanonicalGene geneObj = daoGeneOptimized.getGene(geneSymbol);
        Long queryGeneId = geneObj.getEntrezGeneId();

        if (!isFullResult) {
            GeneticProfile final_gp = DaoGeneticProfile.getGeneticProfileByStableId(profileId);
            if (final_gp != null) {
                try {
                    Map<Long, double[]> map = CoExpUtil.getExpressionMap(final_gp.getGeneticProfileId(), caseSetId, caseIdsKey);
                    int mapSize = map.size();
                    List<Long> genes = new ArrayList<Long>(map.keySet());
                    for (int i = 0; i < mapSize; i++) {
                        double[] query_gene_exp = map.get(queryGeneId);
                        long compared_gene_id = genes.get(i);
                        double[] compared_gene_exp = map.get(compared_gene_id);
                        if (compared_gene_exp != null && query_gene_exp != null) {
                            //Filter out cases with empty value on either side
                            int min_length = query_gene_exp.length < compared_gene_exp.length ? query_gene_exp.length : compared_gene_exp.length;
                            ArrayList<Double> new_query_gene_exp_arrlist = new ArrayList<Double>();
                            ArrayList<Double> new_compared_gene_exp_arrlist = new ArrayList<Double>();
                            for (int k = 0; k < min_length; k++) {
                                if (!Double.isNaN(query_gene_exp[k]) && !Double.isNaN(compared_gene_exp[k])) {
                                    new_query_gene_exp_arrlist.add(query_gene_exp[k]);
                                    new_compared_gene_exp_arrlist.add(compared_gene_exp[k]);
                                }
                            }
                            Double[] _new_query_gene_exp = new_query_gene_exp_arrlist.toArray(new Double[0]);
                            Double[] _new_compared_gene_exp = new_compared_gene_exp_arrlist.toArray(new Double[0]);
                            //convert double object to primitive data
                            double[] new_query_gene_exp = new double[_new_query_gene_exp.length];
                            double[] new_compared_gene_exp = new double[_new_compared_gene_exp.length];
                            for (int m = 0; m < _new_query_gene_exp.length; m++) {
                                new_query_gene_exp[m] = _new_query_gene_exp[m].doubleValue();
                                new_compared_gene_exp[m] = _new_compared_gene_exp[m].doubleValue();
                            }

                            if (new_query_gene_exp.length != 0 && new_compared_gene_exp.length != 0) {
                                double pearson = pearsonsCorrelation.correlation(new_query_gene_exp, new_compared_gene_exp);
                                if ((pearson >= coExpScoreThreshold ||
                                    pearson <= (-1) * coExpScoreThreshold) &&
                                    (compared_gene_id != queryGeneId)) {
                                    //Only calculate spearman with high scored pearson gene pairs.
                                    double spearman = spearmansCorrelation.correlation(new_query_gene_exp, new_compared_gene_exp);
                                    if ((spearman >= coExpScoreThreshold || spearman <= (-1) * coExpScoreThreshold) &&
                                        ((spearman > 0 && pearson > 0) || (spearman < 0 && pearson < 0))) {
                                        CanonicalGene comparedGene = daoGeneOptimized.getGene(compared_gene_id);
                                        ObjectNode _scores = mapper.createObjectNode();
                                        _scores.put("gene", comparedGene.getHugoGeneSymbolAllCaps());
                                        _scores.put("pearson", pearson);
                                        _scores.put("spearman", spearman);
                                        fullResultJson.add(_scores);
                                    }
                                }
                            }
                        }
                    }
                    mapper.writeValue(out, fullResultJson);
                } catch (DaoException e) {
                    System.out.println(e.getMessage());
                    mapper.writeValue(out, new JSONObject());
                }
            } else {
            	 mapper.writeValue(out, new JSONObject());
            }
        } else {
            StringBuilder fullResutlStr = new StringBuilder();
            fullResutlStr.append("Gene Symbol\tCytoband\tPearson Score\tSpearman Score\n");
            GeneticProfile final_gp = DaoGeneticProfile.getGeneticProfileByStableId(profileId);
            if (final_gp != null) {
                try {
                    Map<Long, double[]> map = CoExpUtil.getExpressionMap(final_gp.getGeneticProfileId(), caseSetId, caseIdsKey);
                    int mapSize = map.size();
                    List<Long> genes = new ArrayList<Long>(map.keySet());
                    for (int i = 0; i < mapSize; i++) {
                        double[] query_gene_exp = map.get(queryGeneId);
                        long compared_gene_id = genes.get(i);
                        double[] compared_gene_exp = map.get(compared_gene_id);
                        if (compared_gene_exp != null && query_gene_exp != null) {
                            //Filter out cases with empty value on either side
                            int min_length = (query_gene_exp.length < compared_gene_exp.length) ? query_gene_exp.length : compared_gene_exp.length;
                            ArrayList<Double> new_query_gene_exp_arrlist = new ArrayList<Double>();
                            ArrayList<Double> new_compared_gene_exp_arrlist = new ArrayList<Double>();
                            for (int k = 0; k < min_length; k++) {
                                if (!Double.isNaN(query_gene_exp[k]) && !Double.isNaN(compared_gene_exp[k])) {
                                    new_query_gene_exp_arrlist.add(query_gene_exp[k]);
                                    new_compared_gene_exp_arrlist.add(compared_gene_exp[k]);
                                }
                            }
                            Double[] _new_query_gene_exp = new_query_gene_exp_arrlist.toArray(new Double[0]);
                            Double[] _new_compared_gene_exp = new_compared_gene_exp_arrlist.toArray(new Double[0]);
                            //convert double object to primitive data
                            double[] new_query_gene_exp = new double[_new_query_gene_exp.length];
                            double[] new_compared_gene_exp = new double[_new_compared_gene_exp.length];
                            for (int m = 0; m < _new_query_gene_exp.length; m++) {
                                new_query_gene_exp[m] = _new_query_gene_exp[m].doubleValue();
                                new_compared_gene_exp[m] = _new_compared_gene_exp[m].doubleValue();
                            }
                            if (new_query_gene_exp.length != 0 && new_compared_gene_exp.length != 0 &&
                                compared_gene_id != queryGeneId) {
                                double pearson = pearsonsCorrelation.correlation(new_query_gene_exp, new_compared_gene_exp);
                                double spearman = spearmansCorrelation.correlation(new_query_gene_exp, new_compared_gene_exp);
                                CanonicalGene comparedGene = daoGeneOptimized.getGene(compared_gene_id);
                                fullResutlStr.append(
                                    comparedGene.getHugoGeneSymbolAllCaps() + "\t" +
                                    (double) Math.round(pearson * 100) / 100 + "\t" +
                                    (double) Math.round(spearman * 100) / 100 + "\n"
                                );
                            }
                        }
                    }
                    //construct file name
                    String fileName = "coexpression_" + geneSymbol + "_" +
                        final_gp.getProfileName().replaceAll("\\s+", "_") + "_" +
                        cancerStudyIdentifier.replaceAll("\\s+", "_") + ".txt";

                    httpServletResponse.setContentType("text/html");
                    httpServletResponse.setContentType("application/force-download");
                    httpServletResponse.setHeader("content-disposition", "inline; filename='" + fileName + "'");
                    out = httpServletResponse.getWriter();
                    JSONValue.writeJSONString(fullResutlStr, out);
                } catch (DaoException e) {
                    System.out.println(e.getMessage());
                    JSONValue.writeJSONString(new JSONObject(), out);
                }
            } else {
                JSONValue.writeJSONString(new JSONObject(), out);
            }
        }

    }
}



