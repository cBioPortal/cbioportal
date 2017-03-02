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

import org.codehaus.jackson.node.*;
import org.mskcc.cbio.portal.model.EntityType;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import javax.servlet.http.*;
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
        
        String entityX = httpServletRequest.getParameter("entity_x");
        String entityXProfileId = httpServletRequest.getParameter("entity_x_profile");
        String entityY = httpServletRequest.getParameter("entity_y");
        String entityYProfileId = httpServletRequest.getParameter("entity_y_profile");
        
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
            GeneticProfile entityXProfile = DaoGeneticProfile.getGeneticProfileByStableId(entityXProfileId);
            GeneticProfile entityYProfile = DaoGeneticProfile.getGeneticProfileByStableId(entityYProfileId);

            List<String> stableSampleIds = CoExpUtil.getSampleIds(sampleSetId, patientIdsKey);

            ObjectMapper mapper = new ObjectMapper();
            JsonNodeFactory factory = JsonNodeFactory.instance;
            JsonNode _result = mapper.createObjectNode();

            ArrayNode _sampleValues = getSampleValuesNodeForEntity(entityXProfile, entityX, stableSampleIds, mapper, factory);
            ((ObjectNode)_result).put(entityX, _sampleValues);
            _sampleValues = getSampleValuesNodeForEntity(entityYProfile, entityY, stableSampleIds, mapper, factory);
            ((ObjectNode)_result).put(entityY, _sampleValues);
            
            //duplicated names in output
            ((ObjectNode)_result).put("entity_x_profile_name", entityXProfile.getProfileName());
            ((ObjectNode)_result).put("entity_x_profile_description", entityXProfile.getProfileDescription());
            ((ObjectNode)_result).put("entity_y_profile_name", entityYProfile.getProfileName());
            ((ObjectNode)_result).put("entity_y_profile_description", entityYProfile.getProfileDescription());

            httpServletResponse.setContentType("application/json");
            PrintWriter out = httpServletResponse.getWriter();
            //JSONValue.writeJSONString(_result, out);
            mapper.writeValue(out, _result);
        } catch (DaoException e) {
            System.out.println(e.getMessage());
        }

    }

	private ArrayNode getSampleValuesNodeForEntity(GeneticProfile entityProfile, String entityId, List<String> stableSampleIds, 
			ObjectMapper mapper, JsonNodeFactory factory) throws DaoException {
		ArrayNode result = new ArrayNode(factory);
		//use new API which supports geneset query:
		EntityType entityType = null;//entityProfile
		String entityStableId = null; 
		
		if (entityProfile.getGeneticAlterationType().equals(GeneticAlterationType.GENESET_SCORE)) {
			entityType = EntityType.GENESET;
			entityStableId = entityId;
		} else {
			entityType = EntityType.GENE;
			//for gene, stableId should be entrez:
			DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
			entityStableId = daoGeneOptimized.getGene(entityId).getEntrezGeneId()+ "";
		}
			
        ArrayList<String> tmpProfileDataArr = GeneticAlterationUtil.getGeneticDataRow(
    				entityStableId, 
    				stableSampleIds, 
    				entityType, 
    				entityProfile);
        
        for (int i = 0; i < stableSampleIds.size(); i++) {
            if (tmpProfileDataArr.get(i) != null &&
                !tmpProfileDataArr.get(i).equals("NA") && 
                !tmpProfileDataArr.get(i).equals("NaN") &&
                !tmpProfileDataArr.get(i).equals("")) {
                ObjectNode _datum = mapper.createObjectNode();
                _datum.put("caseId", stableSampleIds.get(i));
                _datum.put("value", Float.parseFloat(tmpProfileDataArr.get(i)));
                result.add(_datum);                        
            }
        }
        return result;
	}
}
