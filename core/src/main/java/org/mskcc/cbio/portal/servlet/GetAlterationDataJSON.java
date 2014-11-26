package org.mskcc.cbio.portal.servlet;

import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.*;

import org.codehaus.jackson.node.*;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import javax.servlet.http.*;
import javax.servlet.ServletException;

import java.io.*;
import java.util.*;
import java.lang.Float;

/**
 * Author: yichao
 * Date: Dec 2013
 *
 * @param : Gene Symbol
 * @param : Cancer Study Id
 * @param : Case Set Id / Case Ids Key
 * @return : Array of JSON object [case id, alteration value]
 */

public class GetAlterationDataJSON extends HttpServlet {

    /**
     * Handles HTTP GET Request.
     *
     * @param httpServletRequest  HttpServletRequest
     * @param httpServletResponse HttpServletResponse
     * @throws javax.servlet.ServletException
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
        String patientSetId = httpServletRequest.getParameter("case_set_id");
        String patientIdsKey = httpServletRequest.getParameter("case_ids_key");
        
        String rawGeneIdList;
        if (httpServletRequest instanceof XssRequestWrapper) {
            rawGeneIdList = ((XssRequestWrapper)httpServletRequest).getRawParameter("gene_list");
        } else {
            rawGeneIdList = httpServletRequest.getParameter("gene_list");
        }
        
        String[] geneIdList = rawGeneIdList.split("\\s+");
        String profileId = httpServletRequest.getParameter("profile_id");


        try {

            GeneticProfile final_gp = DaoGeneticProfile.getGeneticProfileByStableId(profileId);
            List<Integer> sampleIds = InternalIdUtil.getInternalNonNormalSampleIdsFromPatientIds(final_gp.getCancerStudyId(),
                                                                                        CoExpUtil.getPatientIds(patientSetId, patientIdsKey));

            ObjectMapper mapper = new ObjectMapper();
            JsonNodeFactory factory = JsonNodeFactory.instance;
            JsonNode _result = mapper.createObjectNode();
            //JSONObject _result = new JSONObject();
            DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();

            for (String geneId: geneIdList) {
                //ArrayList<ObjectNode> _geneArr = new ArrayList();
                ArrayNode _geneArr = new ArrayNode(factory);
                ArrayList<String> tmpProfileDataArr = 
                            GeneticAlterationUtil.getGeneticAlterationDataRow(
                                daoGeneOptimized.getGene(geneId), 
                                sampleIds, 
                                final_gp
                            );
                for (int i = 0; i < sampleIds.size(); i++) {
                    if (!tmpProfileDataArr.get(i).equals("NA") && 
                        tmpProfileDataArr.get(i) != null &&
                        !tmpProfileDataArr.get(i).equals("NaN")) {
                        //JSONObject _datum = new JSONObject();
                        ObjectNode _datum = mapper.createObjectNode();
                        Sample sample = DaoSample.getSampleById(sampleIds.get(i));
                        _datum.put("caseId", sample.getStableId());
                        _datum.put("value", Float.parseFloat(tmpProfileDataArr.get(i)));
                        _geneArr.add(_datum);                        
                    }
                }
                ((ObjectNode)_result).put(geneId, _geneArr);
            }
            ((ObjectNode)_result).put("profile_name", final_gp.getProfileName());
            ((ObjectNode)_result).put("profile_description", final_gp.getProfileDescription());

            httpServletResponse.setContentType("application/json");
            PrintWriter out = httpServletResponse.getWriter();
            //JSONValue.writeJSONString(_result, out);
            mapper.writeValue(out, _result);
        } catch (DaoException e) {
            System.out.println(e.getMessage());
        }

    }
}
