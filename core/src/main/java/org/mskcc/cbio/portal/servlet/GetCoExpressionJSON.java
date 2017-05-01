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

import org.json.simple.JSONValue;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.map.ObjectMapper;

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
        
        String queryGeneticEntity = ((XssRequestWrapper) httpServletRequest).getRawParameter("genetic_entity");
        String queryGeneticEntityType = httpServletRequest.getParameter("genetic_entity_type");
        String subjectProfileId = httpServletRequest.getParameter("genetic_entity_profile_id");
        String queryProfileId = httpServletRequest.getParameter("correlated_entities_profile_id");
        String correlated_entities_to_find = httpServletRequest.getParameter("correlated_entities_to_find");
        String caseSetId = httpServletRequest.getParameter("case_set_id");
        String caseIdsKey = httpServletRequest.getParameter("case_ids_key");

        PearsonsCorrelation pearsonsCorrelation = new PearsonsCorrelation();
        SpearmansCorrelation spearmansCorrelation = new SpearmansCorrelation();
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
        
        int queryGeneticEntityId;
        if (EntityType.GENESET.name().equals(queryGeneticEntityType)) {
        	try {
        		queryGeneticEntityId = DaoGeneset.getGenesetByExternalId(queryGeneticEntity).getGeneticEntityId();
        	} catch (DaoException e) {
        		throw new IOException("Error while querying DB: " + e.getMessage());
        	}
        } else if (EntityType.GENE.name().equals(queryGeneticEntityType)) {
        	queryGeneticEntityId = daoGeneOptimized.getGene(queryGeneticEntity).getGeneticEntityId();
        }
        else {
        	//raise error
        	throw new IllegalArgumentException("Not supported: " + queryGeneticEntityType);
        }        

    	//validation:
        GeneticProfile queryProfile = DaoGeneticProfile.getGeneticProfileByStableId(queryProfileId);
        GeneticProfile subjectProfile = DaoGeneticProfile.getGeneticProfileByStableId(subjectProfileId);
        	
        if (queryProfile != null) {
            try {
                Map<Integer, double[]> map = CoExpUtil.getExpressionMap(queryProfile.getGeneticProfileId(), caseSetId, caseIdsKey, null);
                int mapSize = map.size();
                List<Integer> genetic_entities = new ArrayList<Integer>(map.keySet());
                //expression of the query item. All other expression lists in the map are compared to this: 
                double[] subject_gene_entity_exp = CoExpUtil.getExpressionList(subjectProfile.getGeneticProfileId(), caseSetId, caseIdsKey, queryGeneticEntityId);
                //iterate over all the other items, comparing to the query_gene_entity_exp:
                for (int i = 0; i < mapSize; i++) {
                    Integer compared_gene_entity_id = genetic_entities.get(i);
                    double[] compared_gene_entity_exp = map.get(compared_gene_entity_id);
                    if (compared_gene_entity_exp != null && subject_gene_entity_exp != null) {
                        //Filter out cases with empty value on either side
                        int min_length = subject_gene_entity_exp.length < compared_gene_entity_exp.length ? subject_gene_entity_exp.length : compared_gene_entity_exp.length;
                        ArrayList<Double> subject_gene_entity_exp_arrlist = new ArrayList<Double>();
                        ArrayList<Double> new_compared_gene_entity_exp_arrlist = new ArrayList<Double>();
                        for (int k = 0; k < min_length; k++) {
                            if (!Double.isNaN(subject_gene_entity_exp[k]) && !Double.isNaN(compared_gene_entity_exp[k])) {
                                subject_gene_entity_exp_arrlist.add(subject_gene_entity_exp[k]);
                                new_compared_gene_entity_exp_arrlist.add(compared_gene_entity_exp[k]);
                            }
                        }
                        Double[] _subject_query_gene_entity_exp = subject_gene_entity_exp_arrlist.toArray(new Double[0]);
                        Double[] _new_compared_gene_entity_exp = new_compared_gene_entity_exp_arrlist.toArray(new Double[0]);
                        //convert double object to primitive data
                        double[] subject_query_gene_entity_exp = new double[_subject_query_gene_entity_exp.length];
                        double[] new_compared_gene_entity_exp = new double[_new_compared_gene_entity_exp.length];
                        for (int m = 0; m < _subject_query_gene_entity_exp.length; m++) {
                            subject_query_gene_entity_exp[m] = _subject_query_gene_entity_exp[m].doubleValue();
                            new_compared_gene_entity_exp[m] = _new_compared_gene_entity_exp[m].doubleValue();
                        }

                        if (subject_query_gene_entity_exp.length != 0 && new_compared_gene_entity_exp.length != 0) {
                            double pearson = pearsonsCorrelation.correlation(subject_query_gene_entity_exp, new_compared_gene_entity_exp);
                            if ((pearson >= coExpScoreThreshold ||
                                pearson <= (-1) * coExpScoreThreshold) &&
                                (compared_gene_entity_id != queryGeneticEntityId)) {
                                //Only calculate spearman with high scored pearson gene pairs.
                                double spearman = spearmansCorrelation.correlation(subject_query_gene_entity_exp, new_compared_gene_entity_exp);
                                if ((spearman >= coExpScoreThreshold || spearman <= (-1) * coExpScoreThreshold) &&
                                    ((spearman > 0 && pearson > 0) || (spearman < 0 && pearson < 0))) {
                                    ObjectNode _scores = mapper.createObjectNode();

                                    if ((EntityType.GENE.name().equals(correlated_entities_to_find))) {
                                        CanonicalGene comparedGene = daoGeneOptimized.getGeneByEntityId(compared_gene_entity_id);
                                        _scores.put("gene", comparedGene.getHugoGeneSymbolAllCaps());
                                        _scores.put("cytoband", comparedGene.getCytoband());
                                    }
                                    else if ((EntityType.GENESET.name().equals(correlated_entities_to_find))) {
                                        String entityStableId = DaoGeneset.getGenesetByEntityId(compared_gene_entity_id).getExternalId();
                                        _scores.put("gene", entityStableId);//TODO change "gene" to a more generic name
                                        _scores.put("cytoband", "-");
                                    }
                                    _scores.put("profileId", queryProfile.getStableId());
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
    }
}



