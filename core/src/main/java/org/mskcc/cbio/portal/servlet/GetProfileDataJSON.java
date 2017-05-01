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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Iterator;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.*;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.map.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.mskcc.cbio.portal.model.EntityType;
import org.mskcc.cbio.portal.model.GeneticAlterationType;

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
        String sampleSetId = httpServletRequest.getParameter("case_set_id");
        String sampleIdsKey = httpServletRequest.getParameter("case_ids_key");
        String rawGeneticEntityIdList;
        if (httpServletRequest instanceof XssRequestWrapper) {
            rawGeneticEntityIdList = ((XssRequestWrapper)httpServletRequest).getRawParameter("genetic_entity_list");
        } else {
            rawGeneticEntityIdList = httpServletRequest.getParameter("genetic_entity_list");
        }

        String[] geneticEntityIdList = rawGeneticEntityIdList.split("\\s+");
        String geneticProfileId = httpServletRequest.getParameter("genetic_profile_id"); 
        //only one genetic profile id expected:
        String [] geneticProfileIds = geneticProfileId.split("\\s+");
        if (geneticProfileIds.length > 1) {
            throw new IllegalArgumentException ("Only one genetic profile id is expected");
        };
        
        String forceDownload = httpServletRequest.getParameter("force_download");
        String format = httpServletRequest.getParameter("format");
        String fileName = httpServletRequest.getParameter("file_name");

        //Final result JSON
        ObjectMapper mapper = new ObjectMapper();
        JsonNode result = mapper.createObjectNode();
        CancerStudy cancerStudy = null;
        try {
        	if (cancerStudyIdentifier != null) {
				cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyIdentifier);
				if (cancerStudy == null
						|| accessControl.isAccessibleCancerStudy(cancerStudy.getCancerStudyStableId()).size() == 0) {
					return;
				}
			} else {
				return;
			}
        } catch (DaoException e) {
            System.out.println("Caught DaoException: " + e.getMessage());
            return;
        }
        try {
            //Get patient ID list
            DaoSampleList daoSampleList = new DaoSampleList();
            SampleList sampleList = null;
            ArrayList<String> sampleIdList = new ArrayList<String>();
            if (sampleSetId.equals("-1")) {
                String strSampleIds = SampleSetUtil.getSampleIds(sampleIdsKey);
                String[] sampleArray = strSampleIds.split("\\s+");
                for (String item : sampleArray) {
                    sampleIdList.add(item);
                }
            } else {
                sampleList = daoSampleList.getSampleListByStableId(sampleSetId);
                sampleIdList = sampleList.getSampleList();
            }
            // NOTE - as of 12/12/14, patient lists contain sample ids
            List<Integer> internalSampleIds = InternalIdUtil.getInternalNonNormalSampleIds(cancerStudy.getInternalId(), sampleIdList);
            List<String> stableSampleIds = InternalIdUtil.getStableSampleIds(internalSampleIds);

            //Get profile data
            for (String geneticEntityId: geneticEntityIdList) {

                DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
                Gene gene = daoGene.getGene(geneticEntityId);

                JsonNode tmpGeneObj = mapper.createObjectNode();

                HashMap<String, JsonNode> tmpObjMap =
                        new LinkedHashMap<String, JsonNode>(); //<"sample_id", "profile_data_collection_json"
                for (String stableSampleId : stableSampleIds) {
                    JsonNode tmp = mapper.createObjectNode();
                    tmpObjMap.put(stableSampleId, tmp);
                }

                //Get raw data (plain text) for the profile
                ArrayList<String> tmpProfileDataArr;
                GeneticProfile geneticProfile = DaoGeneticProfile.getGeneticProfileByStableId(geneticProfileId);
                if (geneticProfile.getGeneticAlterationType().equals(GeneticAlterationType.GENESET_SCORE)) {
                //use new API which supports geneset query:
                tmpProfileDataArr = GeneticAlterationUtil.getGeneticDataRow(
                        geneticEntityId,
                        stableSampleIds,
                        EntityType.GENESET,
                        geneticProfile);
                } else {
                    tmpProfileDataArr = GeneticAlterationUtil.getGeneticAlterationDataRow(
                            gene,
                            internalSampleIds,
                            geneticProfile);
                }
                //Mapping sample Id and profile data
                HashMap<String,String> tmpResultMap =
                        new HashMap<String,String>();  //<"sample_id", "profile_data">
                for (int i = 0; i < stableSampleIds.size(); i++) {
                    tmpResultMap.put(stableSampleIds.get(i), tmpProfileDataArr.get(i));
                }
                for (String stableSampleId : stableSampleIds) {
                    ((ObjectNode)(tmpObjMap.get(stableSampleId))).put(geneticProfileId, tmpResultMap.get(stableSampleId));
                }

                for (String stableSampleId : stableSampleIds) {
                    ((ObjectNode)tmpGeneObj).put(stableSampleId, tmpObjMap.get(stableSampleId));
                }

                ((ObjectNode)result).put(geneticEntityId, tmpGeneObj);

            }
        } catch (DaoException e) {
            System.out.println("Caught DaoException: " + e.getMessage());
        }

        if (forceDownload == null) {
        	//write out in json format:
            httpServletResponse.setContentType("application/json");
            PrintWriter out = httpServletResponse.getWriter();
            mapper.writeValue(out, result);
        } else {
        	//tabular format response:
        	//validate:
        	GeneticProfile geneticProfile = DaoGeneticProfile.getGeneticProfileByStableId(geneticProfileId);
            String result_str = "";
            if (format.equals("tab")) {
                String sampleId_str = (geneticProfile.getGeneticAlterationType().equals(GeneticAlterationType.GENESET_SCORE) ? 
                		"GENESET_ID" : "GENE_ID" + "\t" + "COMMON") + "\t";
                Iterator<String> sampleIds = result.get(geneticEntityIdList[0]).getFieldNames();
                while (sampleIds.hasNext()) {
                    sampleId_str += sampleIds.next() + "\t";
                }
                sampleId_str += "\n";

                String val_str = "";
                Iterator<String> geneticEntityIds = result.getFieldNames();
                while (geneticEntityIds.hasNext()) {
                	String entityId = geneticEntityIds.next();
                	if (geneticProfile.getGeneticAlterationType().equals(GeneticAlterationType.GENESET_SCORE)) {
                		val_str += entityId + "\t";
                	} else {
                        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
                        CanonicalGene gene = daoGene.getGene(entityId);
	                    long entrezGeneId = gene.getEntrezGeneId();
	                    val_str += entityId + "\t" + entrezGeneId + "\t";
                	}
                    JsonNode dataObj = result.get(entityId);
                    Iterator<String> sampleIds_val = dataObj.getFieldNames();
                    while (sampleIds_val.hasNext()) {
                        String sampleId = sampleIds_val.next();
                        String _val = dataObj.get(sampleId).get(geneticProfileId).toString();
                        _val = _val.replaceAll("\"", "");
                        val_str +=  _val + "\t";                        
                    }
                    val_str += "\n";
                }
                result_str += sampleId_str + val_str;
            } else if (format.equals("matrix")) {

                String  entity_str = "",
                        val_str = "";
                entity_str += (geneticProfile.getGeneticAlterationType().equals(GeneticAlterationType.GENESET_SCORE) ? 
                		"GENESET_ID" : "GENE_ID") + "\t";
                //1st header row:
                for(String entityId : geneticEntityIdList) {
                	if (geneticProfile.getGeneticAlterationType().equals(GeneticAlterationType.GENESET_SCORE)) {
                		entity_str += entityId + "\t";
                	} else {
	                    DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
	                    CanonicalGene gene = daoGene.getGene(entityId);
	                    entity_str += gene.getEntrezGeneId() + "\t";
                	}
                }
                //2nd header row (in case of genes only):
                if (!geneticProfile.getGeneticAlterationType().equals(GeneticAlterationType.GENESET_SCORE)) { 
	                entity_str += "\n" + "COMMON" + "\t";
	                for(String entityId : geneticEntityIdList) {
	                    entity_str += entityId + "\t";
	                }
                }
                entity_str += "\n";

                Iterator<String> sampleIds = result.get(geneticEntityIdList[0]).getFieldNames();
                while (sampleIds.hasNext()) {
                    String sampleId = sampleIds.next();
                    val_str += sampleId + "\t";
                    for (String geneticEntityId : geneticEntityIdList) {
                       String _val = result.get(geneticEntityId).get(sampleId).get(geneticProfileId).toString();
                        _val = _val.replaceAll("\"", "");
                        val_str += _val + "\t";
                    }
                    val_str += "\n";
                }
                result_str += entity_str + val_str;
            }

            httpServletResponse.setContentType("application/octet-stream");
            httpServletResponse.setHeader("content-disposition", "attachment; filename='" + fileName + "'");
            PrintWriter out = httpServletResponse.getWriter();
            out.write(result_str);
        }

    }
}
