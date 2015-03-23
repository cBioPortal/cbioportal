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

package org.mskcc.cbio.portal.servlet;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONValue;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.dao.DaoGeneticProfile;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.ExtendedMutation;
import org.mskcc.cbio.portal.model.GeneticAlterationType;
import static org.mskcc.cbio.portal.model.GeneticAlterationType.COPY_NUMBER_ALTERATION;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.or_analysis.ORAnalysisDiscretizedDataProxy;
import org.mskcc.cbio.portal.or_analysis.OverRepresentationAnalysisUtil;

/**
 * Calculate over representation scores 
 * 
 */
public class OverRepresentationAnalysisJSON extends HttpServlet  {

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
        
        try {
            //Extract parameters
            String cancerStudyId = httpServletRequest.getParameter("cancer_study_id");
            String geneSymbol = httpServletRequest.getParameter("gene");
            String caseSetId = httpServletRequest.getParameter("case_set_id");
            String caseIdsKey = httpServletRequest.getParameter("case_ids_key");
            String profileId = httpServletRequest.getParameter("profile_id");
            
            //Get Gene ID (int)
            DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
            CanonicalGene geneObj = daoGeneOptimized.getGene(geneSymbol);
            Long queryGeneId = geneObj.getEntrezGeneId();
            
            //Get genetic profile ID (int)
            GeneticProfile gp = DaoGeneticProfile.getGeneticProfileByStableId(profileId);
            int gpId = gp.getGeneticProfileId();
            GeneticAlterationType gp_type = gp.getGeneticAlterationType();
            
            //Get cancer study internal id (int)
            CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyId);
            int cancerStudyInternalId = cancerStudy.getInternalId();
            
            StringBuilder result_json_str = new StringBuilder();
            
            String gp_type_str = gp_type.toString();
            if(gp_type_str.equals(GeneticAlterationType.COPY_NUMBER_ALTERATION.toString())) {
                Map<Long,double[]> map = OverRepresentationAnalysisUtil.getExpressionMap(cancerStudyInternalId, gpId, caseSetId, caseIdsKey);
                List<Long> genes = new ArrayList<Long>(map.keySet());
                for (int i = 0; i < map.size(); i++) {
                    long _gene = genes.get(i);
                    double[] _rotate_gene_exp = map.get(_gene);
                    double[] _queried_gene_exp = map.get(queryGeneId);

                    //copy number: fisher exact test (high level +/-2)
                    double pValue = ORAnalysisDiscretizedDataProxy.calcCNA(_rotate_gene_exp, _queried_gene_exp);
                    String _rotate_gene_name = daoGeneOptimized.getGene(_gene).getHugoGeneSymbolAllCaps();
                    result_json_str.append(_rotate_gene_name);
                    result_json_str.append(":");
                    result_json_str.append(pValue);
                    result_json_str.append("|");

                }
            } else if (gp_type_str.equals(GeneticAlterationType.MUTATION_EXTENDED.toString())) {
                Map<Long, String[]> map = OverRepresentationAnalysisUtil.getMutationMap(cancerStudyInternalId, gpId, caseSetId, caseIdsKey);
                List<Long> genes = new ArrayList<Long>(map.keySet());
                for (int i = 0; i < map.size(); i++) {
                    long _gene = genes.get(i);
                    String[] _rotate_gene_mut_arr = map.get(_gene);
                    String[] _queried_gene_mut_arr = map.get(queryGeneId);
                    
                    //Mutation: fisher exacte test (mutated vs. non-mutated)
                    double pValue = ORAnalysisDiscretizedDataProxy.calcMut(_rotate_gene_mut_arr, _queried_gene_mut_arr);
                    String _rotate_gene_name = daoGeneOptimized.getGene(_gene).getHugoGeneSymbolAllCaps();
                    result_json_str.append(_rotate_gene_name);
                    result_json_str.append(":");
                    result_json_str.append(pValue);
                    result_json_str.append("|");
                }
            }
            
            httpServletResponse.setContentType("text/html");
            PrintWriter out = httpServletResponse.getWriter();
            JSONValue.writeJSONString(result_json_str.deleteCharAt(result_json_str.length() - 1).toString(), out);


        } catch (DaoException ex) {
            Logger.getLogger(OverRepresentationAnalysisJSON.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
}



