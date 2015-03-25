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
import java.util.List;
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
import org.mskcc.cbio.portal.dao.DaoSample;
import org.mskcc.cbio.portal.dao.DaoSampleProfile;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.model.Sample;
import org.mskcc.cbio.portal.or_analysis.ORAnalysisDiscretizedDataProxy;

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
            String _alteredCaseList = httpServletRequest.getParameter("altered_case_id_list");
            String[] alteredCaseList = _alteredCaseList.split("\\s+");
            String _unalteredCaseList = httpServletRequest.getParameter("unaltered_case_id_list");
            String[] unalteredCaseList = _unalteredCaseList.split("\\s+");
            String profileId = httpServletRequest.getParameter("profile_id");
            
            //Get Gene ID (int)
//            DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
//            CanonicalGene geneObj = daoGeneOptimized.getGene(geneSymbol);
//            Long queryGeneId = geneObj.getEntrezGeneId();
            
            //Get genetic profile ID (int) & Type
            GeneticProfile gp = DaoGeneticProfile.getGeneticProfileByStableId(profileId);
            int gpId = gp.getGeneticProfileId();
            String profileType = gp.getGeneticAlterationType().toString();
            
            //Get cancer study internal id (int)
            CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyId);
            int cancerStudyInternalId = cancerStudy.getInternalId();
            
            //Get Internal Sample Ids (int)
            List<Integer> alteredSampleIds = new ArrayList<Integer>();
            List<Integer> unalteredSampleIds = new ArrayList<Integer>();
            for(String alteredSampleId : alteredCaseList) {
                Sample sample = DaoSample.getSampleByCancerStudyAndSampleId(cancerStudyInternalId, alteredSampleId);   
                alteredSampleIds.add(sample.getInternalId()); 
            }   
            alteredSampleIds.retainAll(DaoSampleProfile.getAllSampleIdsInProfile(gpId));
            for(String unalteredSampleId : unalteredCaseList) {
                Sample sample = DaoSample.getSampleByCancerStudyAndSampleId(cancerStudyInternalId, unalteredSampleId);   
                unalteredSampleIds.add(sample.getInternalId()); 
            }   
            unalteredSampleIds.retainAll(DaoSampleProfile.getAllSampleIdsInProfile(gpId));
            
            //The actual calculation
            ORAnalysisDiscretizedDataProxy dataProxy = new ORAnalysisDiscretizedDataProxy(cancerStudyInternalId, gpId, profileType, alteredSampleIds, unalteredSampleIds);
            
//                Map<Long, String[]> map = OverRepresentationAnalysisUtil.getMutationMap(cancerStudyInternalId, gpId, caseSetId, caseIdsKey);
//                List<Long> genes = new ArrayList<Long>(map.keySet());
//                for (int i = 0; i < map.size(); i++) {
//                    long _gene = genes.get(i);
//                    String[] _rotate_gene_mut_arr = map.get(_gene);
//                    String[] _queried_gene_mut_arr = map.get(queryGeneId);
//                    
//                    //Mutation: fisher exacte test (mutated vs. non-mutated)
//                    double pValue = ORAnalysisDiscretizedDataProxy.calcMut(_rotate_gene_mut_arr, _queried_gene_mut_arr);
//                    String _rotate_gene_name = daoGeneOptimized.getGene(_gene).getHugoGeneSymbolAllCaps();
//                    result_json_str.append(_rotate_gene_name);
//                    result_json_str.append(":");
//                    result_json_str.append(pValue);
//                    result_json_str.append("|");
//                }
            
            httpServletResponse.setContentType("text/html");
            PrintWriter out = httpServletResponse.getWriter();
            JSONValue.writeJSONString(dataProxy.getResult(), out);

        } catch (DaoException ex) {
            Logger.getLogger(OverRepresentationAnalysisJSON.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
}



