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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONValue;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.dao.DaoGeneticProfile;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.util.OverRepresentationAnalysisUtil;

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
            String cancerStudyIdentifier = httpServletRequest.getParameter("cancer_study_id");
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
            
            String _tmp = "something";
            
            Map<Long,double[]> map = OverRepresentationAnalysisUtil.getExpressionMap(gpId, caseSetId, caseIdsKey);
            List<Long> genes = new ArrayList<Long>(map.keySet());
            for (int i = 0; i < map.size(); i++) {
                long _gene = genes.get(i);
                double[] _gene_exp = map.get(_gene);
                _tmp += _gene + "||";
                for (int j = 0; j < _gene_exp.length; j++) {
                    _tmp += _gene_exp[j] + "   ";
                }
                _tmp += "\n";
            }
            System.out.println(_tmp);
            
            httpServletResponse.setContentType("text/html");
            PrintWriter out = httpServletResponse.getWriter();
            JSONValue.writeJSONString(_tmp, out);
            
        } catch (DaoException ex) {
            Logger.getLogger(OverRepresentationAnalysisJSON.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
}



