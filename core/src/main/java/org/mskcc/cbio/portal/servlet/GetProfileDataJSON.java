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

import javax.servlet.ServletException;
import javax.servlet.http.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
        String rawGeneIdList;
        if (httpServletRequest instanceof XssRequestWrapper) {
            rawGeneIdList = ((XssRequestWrapper)httpServletRequest).getRawParameter("gene_list");
        } else {
            rawGeneIdList = httpServletRequest.getParameter("gene_list");
        }
        String[] geneIdList = rawGeneIdList.split("\\s+");
        String[] geneticProfileIds = httpServletRequest.getParameter("genetic_profile_id").split("\\s+");
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
                    ((ObjectNode)tmpGeneObj).set(stableSampleId, tmpObjMap.get(stableSampleId));
                }

                ((ObjectNode)result).set(geneId, tmpGeneObj);

            }
        } catch (DaoException e) {
            System.out.println("Caught DaoException: " + e.getMessage());
        }

        if (forceDownload == null) {
            httpServletResponse.setContentType("application/json");
            PrintWriter out = httpServletResponse.getWriter();
            mapper.writeValue(out, result);
        } else {
            String result_str = "";
            if (format.equals("tab")) {
                String sampleId_str = "GENE_ID" + "\t" + "COMMON" + "\t";
                Iterator<String> sampleIds = result.get(geneIdList[0]).fieldNames();
                while (sampleIds.hasNext()) {
                    sampleId_str += sampleIds.next() + "\t";
                }
                sampleId_str += "\n";

                String val_str = "";
                DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
                Iterator<String> geneSymbols = result.fieldNames();
                while (geneSymbols.hasNext()) {
                    String geneSymbol = geneSymbols.next();
                    CanonicalGene gene = daoGene.getGene(geneSymbol);
                    long entrezGeneId = gene.getEntrezGeneId();
                    val_str += geneSymbol + "\t" + entrezGeneId + "\t";
                    JsonNode dataObj = result.get(geneSymbol);
                    Iterator<String> sampleIds_val = dataObj.fieldNames();
                    while (sampleIds_val.hasNext()) {
                        String sampleId = sampleIds_val.next();
                        String _val = dataObj.get(sampleId).get(geneticProfileIds[0]).toString();
                        _val = _val.replaceAll("\"", "");
                        val_str +=  _val + "\t";                        
                    }
                    val_str += "\n";
                }
                result_str += sampleId_str + val_str;
            } else if (format.equals("matrix")) {

                String  gene_str = "",
                        val_str = "";

                gene_str += "GENE_ID" + "\t";
                Iterator<String> geneSymbols = result.fieldNames();
                DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
                while (geneSymbols.hasNext()) {
                    String geneSymbol = geneSymbols.next();
                    CanonicalGene gene = daoGene.getGene(geneSymbol);
                    gene_str += gene.getEntrezGeneId() + "\t";
                }
                gene_str += "\n" + "COMMON" + "\t";
                for(String geneId : geneIdList) {
                    gene_str += geneId + "\t";
                }
                gene_str += "\n";

                Iterator<String> sampleIds = result.get(geneIdList[0]).fieldNames();
                while (sampleIds.hasNext()) {
                    String sampleId = sampleIds.next();
                    val_str += sampleId + "\t";
                    for (String geneId : geneIdList) {
                       String _val = result.get(geneId).get(sampleId).get(geneticProfileIds[0]).toString();
                        _val = _val.replaceAll("\"", "");
                        val_str += _val + "\t";
                    }
                    val_str += "\n";
                }
                result_str += gene_str + val_str;
            }

            httpServletResponse.setContentType("application/octet-stream");
            httpServletResponse.setHeader("content-disposition", "attachment; filename='" + fileName + "'");
            PrintWriter out = httpServletResponse.getWriter();
            out.write(result_str);
        }

    }
}
