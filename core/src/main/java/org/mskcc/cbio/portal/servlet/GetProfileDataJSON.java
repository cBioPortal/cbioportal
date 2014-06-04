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

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.map.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.mskcc.cbio.portal.dao.*;

import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.CaseList;
import org.mskcc.cbio.portal.model.Gene;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.util.CaseSetUtil;
import org.mskcc.cbio.portal.util.XssRequestWrapper;

/**
 * Retrieves genomic profile data for one or more genes.
 * developed based on web api "GetGeneticProfiles"
 *
 * @param genetic_profile_id
 * @param case_set_id
 * @param gene_list
 *
 * @return JSON objects of genetic profile data
 */
public class GetProfileDataJSON extends HttpServlet  {

    /**
     * Handles HTTP GET Request.
     *
     * @param httpServletRequest  HttpServletRequest
     * @param httpServletResponse HttpServletResponse
     * @throws ServletException
     */
    protected void doGet(HttpServletRequest httpServletRequest,
                         HttpServletResponse httpServletResponse)
            throws ServletException, IOException {
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
                          HttpServletResponse httpServletResponse)
            throws ServletException, IOException {

        //Get URL Parameters
        String caseSetId = httpServletRequest.getParameter("case_set_id");
        String caseIdsKey = httpServletRequest.getParameter("case_ids_key");
        String rawGeneIdList;
        if (httpServletRequest instanceof XssRequestWrapper) {
            rawGeneIdList = ((XssRequestWrapper)httpServletRequest).getRawParameter("gene_list");
        } else {
            rawGeneIdList = httpServletRequest.getParameter("gene_list");
        }
        
        String[] geneIdList = rawGeneIdList.split("\\s+");
        String[] geneticProfileIds = httpServletRequest.getParameter("genetic_profile_id").split("\\s+");

        //Final result JSON
        ObjectMapper mapper = new ObjectMapper();
        JsonNode result = mapper.createObjectNode();

        try {

            //Get Case case ID list
            DaoCaseList daoCaseList = new DaoCaseList();
            CaseList caseList;
            ArrayList<String> caseIdList = new ArrayList<String>();
            if (caseSetId.equals("-1")) {
                String strCaseIds = CaseSetUtil.getCaseIds(caseIdsKey);
                String[] caseArray = strCaseIds.split("\\s+");
                for (String item : caseArray) {
                    caseIdList.add(item);
                }
            } else {
                caseList = daoCaseList.getCaseListByStableId(caseSetId);
                caseIdList = caseList.getCaseList();
            }

            //Get profile data
            for (String geneId: geneIdList) {

                DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
                Gene gene = daoGene.getGene(geneId);

                JsonNode tmpGeneObj = mapper.createObjectNode();

                HashMap<String, JsonNode> tmpObjMap =
                        new LinkedHashMap<String, JsonNode>(); //<"case_id", "profile_data_collection_json"
                for (String caseId: caseIdList) {
                    JsonNode tmp = mapper.createObjectNode();
                    tmpObjMap.put(caseId, tmp);
                }

                //Get raw data (plain text) for each profile
                for (String geneticProfileId: geneticProfileIds) {
                    try {
                        ArrayList<String> tmpProfileDataArr = GeneticAlterationUtil.getGeneticAlterationDataRow(
                                gene,
                                caseIdList,
                                DaoGeneticProfile.getGeneticProfileByStableId(geneticProfileId));
                        //Mapping case Id and profile data
                        HashMap<String,String> tmpResultMap =
                                new HashMap<String,String>();  //<"case_id", "profile_data">
                        for (int i = 0; i < caseIdList.size(); i++) {
                            tmpResultMap.put(caseIdList.get(i), tmpProfileDataArr.get(i));
                        }

                        for (String caseId: caseIdList) {
                            ((ObjectNode)(tmpObjMap.get(caseId))).put(geneticProfileId, tmpResultMap.get(caseId));
                        }
                    } catch(NullPointerException e) {
                        //TODO: handle empty dataset
                        continue;
                    }
                }

                for (String caseId: caseIdList) {
                    ((ObjectNode)tmpGeneObj).put(caseId, tmpObjMap.get(caseId));
                }

                ((ObjectNode)result).put(geneId, tmpGeneObj);

            }
        } catch (DaoException e) {
            System.out.println("Caught DaoException: " + e.getMessage());
        }

        httpServletResponse.setContentType("application/json");
        PrintWriter out = httpServletResponse.getWriter();
        mapper.writeValue(out, result);

    }
}