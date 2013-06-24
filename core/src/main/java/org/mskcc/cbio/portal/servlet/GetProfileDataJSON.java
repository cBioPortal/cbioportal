package org.mskcc.cbio.portal.servlet;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.map.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.*;
import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

import org.mskcc.cbio.cgds.dao.*;

import org.mskcc.cbio.cgds.model.CancerStudy;
import org.mskcc.cbio.cgds.model.CaseList;
import org.mskcc.cbio.cgds.model.Gene;
import org.mskcc.cbio.cgds.model.GeneticProfile;

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
        String[] geneIdList = httpServletRequest.getParameter("gene_list").split("\\s+");
        String[] geneticProfileIds = httpServletRequest.getParameter("genetic_profile_id").split("\\s+");

        //Final result JSON
        ObjectMapper mapper = new ObjectMapper();
        JsonNode result = mapper.createObjectNode();

        try {

            //Get Case case ID list
            DaoCaseList daoCaseList = new DaoCaseList();
            CaseList caseList = daoCaseList.getCaseListByStableId(caseSetId);
            ArrayList<String> caseIdList = caseList.getCaseList();

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
