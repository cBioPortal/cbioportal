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

import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.*;

import javax.servlet.http.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.servlet.ServletConfig;
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

	// class which process access control to cancer studies
    private AccessControl accessControl;
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        accessControl = SpringUtil.getAccessControl();
    }
    
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
        String sampleSetId = httpServletRequest.getParameter("case_set_id");
        String patientIdsKey = httpServletRequest.getParameter("case_ids_key");
        
        String rawGeneIdList;
        if (httpServletRequest instanceof XssRequestWrapper) {
            rawGeneIdList = ((XssRequestWrapper)httpServletRequest).getRawParameter("gene_list");
        } else {
            rawGeneIdList = httpServletRequest.getParameter("gene_list");
        }
        
        String[] geneIdList = rawGeneIdList.split("\\s+");
        String profileId = httpServletRequest.getParameter("profile_id");
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
            GeneticProfile final_gp = DaoGeneticProfile.getGeneticProfileByStableId(profileId);
            if(final_gp.getCancerStudyId() != cancerStudy.getInternalId()) {
            	return;
            }
            List<String> stableSampleIds = CoExpUtil.getSampleIds(sampleSetId, patientIdsKey);
            List<Integer> sampleIds = InternalIdUtil.getInternalSampleIds(final_gp.getCancerStudyId(), stableSampleIds);

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
                        !tmpProfileDataArr.get(i).equals("NaN") &&
                        !tmpProfileDataArr.get(i).equals("")) {
                        //JSONObject _datum = new JSONObject();
                        ObjectNode _datum = mapper.createObjectNode();
                        Sample sample = DaoSample.getSampleById(sampleIds.get(i));
                        _datum.put("caseId", sample.getStableId());
                        _datum.put("value", Float.parseFloat(tmpProfileDataArr.get(i)));
                        _geneArr.add(_datum);                        
                    }
                }
                ((ObjectNode)_result).set(geneId, _geneArr);
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
