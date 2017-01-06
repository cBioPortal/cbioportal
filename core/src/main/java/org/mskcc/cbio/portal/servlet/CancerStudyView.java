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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoSampleList;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.Cohort;
import org.mskcc.cbio.portal.model.CohortStudyCasesMap;
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
    public static final String COHORTS = "cohorts";
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
        	buildResponse(request);
            
            if (request.getAttribute(ERROR)!=null) {
                forwardToErrorPage(request, response, (String)request.getAttribute(ERROR), xdebug);
            } else {
                RequestDispatcher dispatcher =
                        getServletContext().getRequestDispatcher("/WEB-INF/jsp/dashboard/dashboard.jsp");
                dispatcher.forward(request, response);
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
	private CancerStudy getCancerStudyDetails(String cancerStudyId) throws NotAuthorizedException, DaoException {
		CancerStudy cancerStudy = null;
		try {
			cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyId);
			if (cancerStudy == null) {
				cancerStudy = DaoCancerStudy.getCancerStudyByInternalId(Integer.parseInt(cancerStudyId));
			} else {
				if (accessControl.isAccessibleCancerStudy(cancerStudy.getCancerStudyStableId()).size() != 1) {
					throw new NotAuthorizedException(cancerStudy.getCancerStudyStableId(), "unAuthorized");
				}
			}
		} catch (NumberFormatException numberFormatException) {
			LOG.warn("CancerStudyView.getCancerStudyDetails(): NumberFormatException = '"
					+ numberFormatException.getMessage() + "'");
		}
		return cancerStudy;
	}
    
    /**
     * This method builds the response for the servlet request.
     * Request format could be one of the following
     * 1. Predefined cohort or a cohort list(Ex : cohorts=acc_tcga or cohorts=acc_tcga,brca_tcga)
     * 2. User defined virtual cohort (Ex : cohorts=57e293a9d4c6c0c4ed706f2)
     * 3. Any combination of 1 and 2
     * 4. Encoded URI of study sample map string(Ex : {"acc_tcga":["TCGA-PK-A5H9-01","TCGA-OR-A5J3-01","TCGA-OR-A5JJ-01"]})
     * @param request
     * @return boolean value( whether success or not)
     * @throws DaoException
     * @throws JsonProcessingException
     * @throws IOException
     */
	private boolean buildResponse(HttpServletRequest request)
			throws DaoException, JsonProcessingException, IOException {

		Set<String> unknownStudyIds = new HashSet<>();
		Set<String> unAuthorizedStudyIds = new HashSet<>();
		Map<String, HashSet<String>> inputCohortMap = getCohortIds(request);
		Map<String, Cohort> cohortMap = getProcessedCohortMap(inputCohortMap);
		Map<String, Set<String>> studySampleMap = new HashMap<String, Set<String>>();
		UserDetails ud = accessControl.getUserDetails();
		if (ud != null) {
			LOG.info("CancerStudyView.validate: Query initiated by user: " + ud.getUsername() + " : Study(s): "
					+ inputCohortMap.keySet());
		}

		// Find unauthorized and unknown studies
		for (String cohortId : inputCohortMap.keySet()) {
			if (cohortMap.get(cohortId) == null) {
				try {
					CancerStudy cancerStudy = getCancerStudyDetails(cohortId);
					if (cancerStudy == null) {
						unknownStudyIds.add(cohortId);
					}
				} catch (NotAuthorizedException notAuthorizedException) {
					LOG.info("User: " + accessControl.getUserDetails().getUsername()
							+ ", not authorized to query Study(s): " + cohortId);
					unAuthorizedStudyIds.add(cohortId);
				}
			}
		}

		// Loop though all the process map and and prepare final response map
		for (String cohortId : cohortMap.keySet()) {
			Cohort cohort = cohortMap.get(cohortId);
			// if it is a virutal cohort loop through all the studies and check
			// whether user has access to those studies and if yes add them to
			// the response map
			if (cohort.isVirtualCohort()) {
				for (CohortStudyCasesMap cohortStudyCasesMap : cohort.getCohortStudyCasesMap()) {
					try {
						CancerStudy cancerStudy = getCancerStudyDetails(cohortStudyCasesMap.getStudyID());
						if (cancerStudy != null) {
							addCohortToMap(studySampleMap, cohortStudyCasesMap.getStudyID(),
									cohortStudyCasesMap.getSamples());
						} else {
							unknownStudyIds.add(cohortStudyCasesMap.getStudyID());
						}
					} catch (NotAuthorizedException notAuthorizedException) {
						unAuthorizedStudyIds.add(cohortStudyCasesMap.getStudyID());
					}
				}
			} else {
				addCohortToMap(studySampleMap, cohort.getId(), inputCohortMap.get(cohort.getId()));
			}
		}
		// check if there are any studies in the response map, if no then check
		if (studySampleMap.size() == 0) {
			if (unknownStudyIds.size() > 0 && unAuthorizedStudyIds.size() > 0) {
				request.setAttribute(ERROR,
						"No such cohort(s): " + StringUtils.join(unknownStudyIds, ",")
								+ "' and unauthorized to view with id(s):" + StringUtils.join(unAuthorizedStudyIds, ",")
								+ "'. ");
			} else if (unknownStudyIds.size() > 0) {
				request.setAttribute(ERROR, "No such cohort(s): " + StringUtils.join(unknownStudyIds, ",") + "'. ");
			} else {
				request.setAttribute(ERROR, "You are not authorized to view the cancer study with id(s): "
						+ StringUtils.join(unAuthorizedStudyIds, ",") + "'. ");
			}
			return false;
		}
		ObjectMapper mapper = new ObjectMapper();
		String studySampleMapString = mapper.writeValueAsString(studySampleMap);
		request.setAttribute(STUDY_SAMPLE_MAP, studySampleMapString);
		request.setAttribute(COHORTS, request.getParameter(COHORTS));
		return true;
	}
    /**
     * Loop through all the input cohorts and add them to a map
     * Steps 1 : check if it is predefined study and user has access to it
     * 			a. if yes, add that to the map
     * Step 2 : if it not a predefined study or user doesn't have access to it, check if it is a virtual cohort
     * 			a. if yes, add that to the map
     * 
     * @param inputCohortMap
     * @return Map<String, Cohort> cohortMap
     * @throws DaoException
     */
	private Map<String, Cohort> getProcessedCohortMap(Map<String, HashSet<String>> inputCohortMap) throws DaoException {
		Map<String, Cohort> cohortMap = new HashMap<String, Cohort>();
		SessionServiceUtil sessionServiceUtil = new SessionServiceUtil();
		for (String cohortId : inputCohortMap.keySet()) {
			try {
				CancerStudy cancerStudy = getCancerStudyDetails(cohortId);
				if (cancerStudy == null) {
					Cohort virtualCohort = sessionServiceUtil.getVirtualCohortData(cohortId);
					if (virtualCohort != null) {
						cohortMap.put(virtualCohort.getId(), virtualCohort);
					}
				} else {
					Cohort cohort = new Cohort();
					cohort.setId(cancerStudy.getCancerStudyStableId());
					cohort.setVirtualCohort(false);
					cohortMap.put(cohort.getId(), cohort);
				}
			} catch (NotAuthorizedException notAuthorizedException) {
				LOG.warn("notAuthorizedException" + notAuthorizedException);
			}
		}
		return cohortMap;
	}

	/**
	 * Get cohort Ids from httprequest
	 * @param request
	 * @return Map<String, HashSet<String>> cohortIds
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	private Map<String, HashSet<String>> getCohortIds(HttpServletRequest request) throws JsonParseException, JsonMappingException, IOException {
		String cohortIds = request.getParameter(COHORTS);
		Map<String, HashSet<String>> inputCohortMap = new HashMap<String, HashSet<String>>();
		if (cohortIds == null) {
			// if cohorts is null check for study_sample_map request parameter
			String studySampleMapString = request.getParameter(STUDY_SAMPLE_MAP);
			if (studySampleMapString == null) {
				request.setAttribute(ERROR, "No such cancer study");
			} else {
				// Decode study_sample_map string
				studySampleMapString = java.net.URLDecoder.decode(studySampleMapString, "UTF-8");
				ObjectMapper mapper = new ObjectMapper();
				inputCohortMap = mapper.readValue(studySampleMapString,
						new TypeReference<Map<String, HashSet<String>>>() {
						});
				if (inputCohortMap.keySet().size() == 0) {
					request.setAttribute(ERROR, "No such cancer study");
				}
			}
		} else {
			String[] cohortIdsList = cohortIds.split(",");
			for (String cohortId : cohortIdsList) {
				inputCohortMap.put(cohortId, null);
			}
		}
		return inputCohortMap;
	}

	/**
     * This method adds a study and its related samples to the response map
     * @param studySampleMap
     * @param cancerStudyId
     * @param sampleIds
     * @throws DaoException
     */
	private void addCohortToMap(Map<String, Set<String>> studySampleMap, String cancerStudyId, Set<String> sampleIds)
			throws DaoException {
		// check if the the study already present in the response map
		// and if yes, find the intersection of the samples in the response map
		// and the samples that needs to be added into the map
		if (studySampleMap.containsKey(cancerStudyId)) {
			Set<String> sampleIdsTemp = studySampleMap.get(cancerStudyId);
			if (sampleIds == null || sampleIds.size() == 0) {
				SampleList sampleList = daoSampleList.getSampleListByStableId(cancerStudyId + "_all");
				sampleIds = new HashSet<String>(sampleList.getSampleList());
			}
			sampleIdsTemp.retainAll(sampleIds);
			studySampleMap.put(cancerStudyId, sampleIdsTemp);
		} else {
			if (sampleIds == null || sampleIds.size() == 0) {
				SampleList sampleList = daoSampleList.getSampleListByStableId(cancerStudyId + "_all");
				sampleIds = new HashSet<String>(sampleList.getSampleList());
			}
			studySampleMap.put(cancerStudyId, sampleIds);
		}

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
