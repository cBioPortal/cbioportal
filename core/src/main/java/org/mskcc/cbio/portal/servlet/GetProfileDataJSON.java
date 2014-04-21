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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.*;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.map.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.http.*;


/**
 * Retrieves genomic profile data for one or more genes.
 * developed based on web api "GetGeneticProfiles"
 *
 * @param genetic_profile_id
 * @param patient_set_id
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
        String[] geneticProfileIds = httpServletRequest.getParameter("genetic_profile_id").split("\\s+");

        //Final result JSON
        ObjectMapper mapper = new ObjectMapper();
        JsonNode result = mapper.createObjectNode();

        try {

            CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyIdentifier);
            if (cancerStudy == null) {
                throw new DaoException("Unknown cancer study id: " + cancerStudyIdentifier);
            }

            //Get patient ID list
            DaoPatientList daoPatientList = new DaoPatientList();
            PatientList patientList = null;
            ArrayList<String> patientIdList = new ArrayList<String>();
            if (patientSetId.equals("-1")) {
                String strPatientIds = PatientSetUtil.getPatientIds(patientIdsKey);
                String[] patientArray = strPatientIds.split("\\s+");
                for (String item : patientArray) {
                    patientIdList.add(item);
                }
            } else {
                patientList = daoPatientList.getPatientListByStableId(patientSetId);
                patientIdList = patientList.getPatientList();
            }
            List<Integer> internalSampleIds = InternalIdUtil.getInternalSampleIdsFromPatientIds(cancerStudy.getInternalId(), patientIdList);
            List<String> stableSampleIds = InternalIdUtil.getStableSampleIds(internalSampleIds);

            //Get profile data
            for (String geneId: geneIdList) {

                DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
                Gene gene = daoGene.getGene(geneId);

                JsonNode tmpGeneObj = mapper.createObjectNode();

                HashMap<String, JsonNode> tmpObjMap =
                        new LinkedHashMap<String, JsonNode>(); //<"sample_id", "profile_data_collection_json"
                for (String stableSampleId : stableSampleIds) {
                    JsonNode tmp = mapper.createObjectNode();
                    tmpObjMap.put(stableSampleId, tmp);
                }

                //Get raw data (plain text) for each profile
                for (String geneticProfileId: geneticProfileIds) {
                    try {
                        ArrayList<String> tmpProfileDataArr = GeneticAlterationUtil.getGeneticAlterationDataRow(
                                gene,
                                internalSampleIds,
                                DaoGeneticProfile.getGeneticProfileByStableId(geneticProfileId));
                        //Mapping sample Id and profile data
                        HashMap<String,String> tmpResultMap =
                                new HashMap<String,String>();  //<"sample_id", "profile_data">
                        for (int i = 0; i < stableSampleIds.size(); i++) {
                            tmpResultMap.put(stableSampleIds.get(i), tmpProfileDataArr.get(i));
                        }

                        for (String stableSampleId : stableSampleIds) {
                            ((ObjectNode)(tmpObjMap.get(stableSampleId))).put(geneticProfileId, tmpResultMap.get(stableSampleId));
                        }
                    } catch(NullPointerException e) {
                        //TODO: handle empty dataset
                        continue;
                    }
                }

                for (String stableSampleId : stableSampleIds) {
                    ((ObjectNode)tmpGeneObj).put(stableSampleId, tmpObjMap.get(stableSampleId));
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