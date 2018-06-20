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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mskcc.cbio.portal.model.virtualstudy.VirtualStudy;
import org.mskcc.cbio.portal.model.virtualstudy.VirtualStudyData;
import org.mskcc.cbio.portal.model.virtualstudy.VirtualStudySamples;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoSampleList;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.SampleList;
import org.mskcc.cbio.portal.util.AccessControl;
import org.mskcc.cbio.portal.util.SessionServiceUtil;
import org.mskcc.cbio.portal.util.SpringUtil;
import org.mskcc.cbio.portal.util.XDebug;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.social.NotAuthorizedException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @author jj
 */
public class CancerStudyView extends HttpServlet {
    private static Log LOG = LogFactory.getLog(CancerStudyView.class);
    public static final String ID = "id";
    public static final String ERROR = "error";
    public static final String CANCER_STUDY = "cancer_study";
    public static final String MUTATION_PROFILE = "mutation_profile";
    public static final String CNA_PROFILE = "cna_profile";
    public static final String STUDY_SAMPLE_MAP = "study_sample_map";

    private static final DaoSampleList daoSampleList = new DaoSampleList();

    // class which process access control to cancer studies
    private AccessControl accessControl;

    /**
     * Initializes the servlet.
     *
     * @throws ServletException Serlvet Init Error.
     */
    @Override
    public void init() throws ServletException {
        super.init();
        accessControl = SpringUtil.getAccessControl();
    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        XDebug xdebug = new XDebug( request );
        request.setAttribute(QueryBuilder.XDEBUG_OBJECT, xdebug);

        try {
            if (buildResponse(request)) {
                RequestDispatcher dispatcher =
                    getServletContext().getRequestDispatcher("/WEB-INF/jsp/dashboard/dashboard.jsp");
                dispatcher.forward(request, response);
            } else {
            		forwardToErrorPage(request, response, (String)request.getAttribute(ERROR), xdebug);
            }

        } catch (DaoException e) {
            xdebug.logMsg(this, "Got Database Exception:  " + e.getMessage());
            forwardToErrorPage(request, response,
                "An error occurred while trying to connect to the database.", xdebug);
        }
        catch (Exception e) {
            xdebug.logMsg(this, "Error while processing data:  " + e.getMessage());
            forwardToErrorPage(request, response,
                "Error while processing data", xdebug);
        }
    }

    /**
     * Gets CancerStudy object for the given study ID.
     * If no match found then It would return null
     * @param cancerStudyId
     * @return cancerStudy
     */
    private CancerStudy getCancerStudyDetails(String cancerStudyId) throws DaoException {
        CancerStudy cancerStudy = null;
        try {
            cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyId);
            if (cancerStudy == null) {
                cancerStudy = DaoCancerStudy.getCancerStudyByInternalId(Integer.parseInt(cancerStudyId));
            }
        } catch (NumberFormatException numberFormatException) {
            LOG.warn("CancerStudyView.getCancerStudyDetails(): NumberFormatException = '"
                + numberFormatException.getMessage() + "'");
        }
        return cancerStudy;
    }

    /**
     * This method builds the response for the servlet request.
     * @param request
     * @return boolean value( whether success or not)
     * @throws DaoException
     * @throws JsonProcessingException
     * @throws IOException
     */
    private boolean buildResponse(HttpServletRequest request)
        throws DaoException, JsonProcessingException, IOException {

        Map<String, HashSet<String>> inputStudyMap = getStudyIds(request);
        Set<VirtualStudy> studies = getProcessedStudyMap(inputStudyMap);
        Map<String, Set<String>> studySampleMap = new HashMap<String, Set<String>>();
        UserDetails ud = accessControl.getUserDetails();
        if (ud != null) {
            LOG.info("CancerStudyView.validate: Query initiated by user: " + ud.getUsername() + " : Study(s): "
                + inputStudyMap.keySet());
        }
        
        Set<String> knowIds = studies
                .stream()
                .map(obj -> obj.getId())
                .collect(Collectors.toSet());

        //add unknow input ids
        Set<String> unKnownIds = inputStudyMap
                .keySet()
                .stream()
                .filter(id -> !knowIds.contains(id))
                .collect(Collectors.toSet());
        
        //add unauthorized ids
        unKnownIds.addAll(studies
                .stream()
                .filter(obj -> {
                    if(inputStudyMap.containsKey(obj.getId())) {
                        for (VirtualStudySamples _study : obj.getData().getStudies()) {
                            try {
                                if (accessControl.isAccessibleCancerStudy(_study.getId()).size() != 1)
                                    return true;
                            } catch (Exception e) {
                                return true;
                            }
                        }
                        return false;
                    } else {
                        return true;
                    }
                })
                .map(obj -> obj.getId())
                .collect(Collectors.toSet()));
        
        if(unKnownIds.size() > 0) {
        		request.setAttribute(ERROR, "Unknown/Unauthorized studies in: "
                + StringUtils.join(unKnownIds, ",") + ".");
        		return false;
        }
        
        //prepare sample response map
        studies.stream().forEach(data -> {
	        	for(VirtualStudySamples _study : data.getData().getStudies()) {
	        		
	        		Set<String> sampleIdsToAdd = _study.getSamples();
	            if (sampleIdsToAdd == null) {
	                SampleList sampleList;
					try {
						sampleList = daoSampleList.getSampleListByStableId(_study.getId() + "_all");
					} catch (DaoException e) {
						throw new RuntimeException(e);
					}
	                sampleIdsToAdd = new HashSet<String>(sampleList.getSampleList());
	            }
	            Set<String> updatedSampleList  = studySampleMap.getOrDefault(_study.getId(), new HashSet<>());
	            updatedSampleList.addAll(sampleIdsToAdd);
	            
	            studySampleMap.put(_study.getId(), updatedSampleList);
	        	}
	    });
        
        ObjectMapper mapper = new ObjectMapper();
        String studySampleMapString = mapper.writeValueAsString(studySampleMap);
        request.setAttribute(STUDY_SAMPLE_MAP, studySampleMapString);
        request.setAttribute(ID, inputStudyMap.keySet());
        return true;
    }
    /**
     * Loop through all the input studies and add them to a map
     * Steps 1 : check if it is predefined study and user has access to it
     * 			a. if yes, add that to the map
     * Step 2 : if it not a predefined study or user doesn't have access to it, check if it is a virtual study
     * 			a. if yes, add that to the map
     *
     * @param inputStudyMap
     * @return Set<VirtualStudy> studiesMap
     * @throws DaoException
     */
    private Set<VirtualStudy> getProcessedStudyMap(Map<String, HashSet<String>> inputStudyMap) throws DaoException {
    		Set<VirtualStudy> studiesMap = new HashSet<>();
        SessionServiceUtil sessionServiceUtil = new SessionServiceUtil();
        for (String studyId : inputStudyMap.keySet()) {
            try {
                CancerStudy cancerStudy = getCancerStudyDetails(studyId);
                if (cancerStudy == null) {
                	VirtualStudy virtualStudy = sessionServiceUtil.getVirtualStudyData(studyId);
                    if (virtualStudy != null) {
                    	studiesMap.add(virtualStudy);
                    }
                } else {
                		VirtualStudy study = new VirtualStudy();
                		VirtualStudySamples studySamples = new VirtualStudySamples();
                		studySamples.setId(studyId);
                		studySamples.setSamples(inputStudyMap.get(studyId));
                		VirtualStudyData studyData = new VirtualStudyData();
                		studyData.setStudies(Collections.singleton(studySamples));
                		study.setId(cancerStudy.getCancerStudyStableId());
                		study.setData(studyData);
                		studiesMap.add(study);
                }
            } catch (NotAuthorizedException notAuthorizedException) {
                LOG.warn("notAuthorizedException" + notAuthorizedException);
            }
        }
        return studiesMap;
    }

    /**
     * Get study Ids from httprequest
     * Request format could be one of the following
     * 1. Predefined study or a study list(Ex : study=acc_tcga or study=acc_tcga,brca_tcga)
     * 2. User defined virtual study (Ex : study=57e293a9d4c6c0c4ed706f2)
     * 3. Any combination of 1 and 2
     * 4. Encoded URI of study sample map string(Ex : {"acc_tcga":["TCGA-PK-A5H9-01","TCGA-OR-A5J3-01","TCGA-OR-A5JJ-01"]})
     * 5. Existing 
     * @param request
     * @return Map<String, HashSet<String>> studyIds
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    private Map<String, HashSet<String>> getStudyIds(HttpServletRequest request)
        throws JsonParseException, JsonMappingException, IOException {
        String studyIds = request.getParameter(ID);

        // TODO: this block is temporarily to support cancer_study_id. Once
        // everything changes we could get rid of this
        if (studyIds == null) {
        		studyIds = request.getParameter("cancer_study_id");
        }

        Map<String, HashSet<String>> inputStudyMap = new HashMap<String, HashSet<String>>();
        if (studyIds == null) {
            // if study is null check for study_sample_map request parameter
            String studySampleMapString = request.getParameter(STUDY_SAMPLE_MAP);
            if (studySampleMapString == null) {
                request.setAttribute(ERROR, "No such cancer study");
            } else {
                // Decode study_sample_map string
                studySampleMapString = java.net.URLDecoder.decode(studySampleMapString, "UTF-8");
                ObjectMapper mapper = new ObjectMapper();
                inputStudyMap = mapper.readValue(studySampleMapString,
                    new TypeReference<Map<String, HashSet<String>>>() {
                    });
                if (inputStudyMap.keySet().size() == 0) {
                    request.setAttribute(ERROR, "No such cancer study");
                }
            }
        } else {
            for (String studyId : studyIds.split(",")) {
            		inputStudyMap.put(studyId, null);
            }
        }
        return inputStudyMap;
    }

    private void forwardToErrorPage(HttpServletRequest request, HttpServletResponse response,
                                    String userMessage, XDebug xdebug)
        throws ServletException, IOException {
        request.setAttribute("xdebug_object", xdebug);
        request.setAttribute(QueryBuilder.USER_ERROR_MESSAGE, userMessage);
        RequestDispatcher dispatcher =
            getServletContext().getRequestDispatcher("/WEB-INF/jsp/error.jsp");
        dispatcher.forward(request, response);
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}