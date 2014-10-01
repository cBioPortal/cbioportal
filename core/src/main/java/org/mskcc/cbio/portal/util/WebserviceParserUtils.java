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


package org.mskcc.cbio.portal.util;

import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.servlet.WebService;
import org.mskcc.cbio.portal.web_api.ProtocolException;

import java.util.*;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author jgao
 */
public final class WebserviceParserUtils {
    
    private WebserviceParserUtils() {}

    /**
     * Grabs the appropriate stuff from a request and returns a list of case_ids
     * @param request
     * @return
     * @throws ProtocolException
     * @throws DaoException
     */
    public static ArrayList<String> getPatientList(HttpServletRequest request) throws ProtocolException,
            DaoException {
        String patients = request.getParameter(WebService.CASE_LIST);
        String patientSetId = request.getParameter(WebService.CASE_SET_ID);
        String patientIdsKey = request.getParameter(WebService.CASE_IDS_KEY);
        String samples = request.getParameter("samples");

        if (patients == null &&
        	patientIdsKey != null)
        {
        	patients = PatientSetUtil.getPatientIds(patientIdsKey);
        }

        ArrayList<String> patientList = new ArrayList<String>();
        if (patientSetId != null) {
            DaoPatientList dao = new DaoPatientList();
            PatientList selectedPatientList = dao.getPatientListByStableId(patientSetId);
            if (selectedPatientList == null) {
                throw new ProtocolException("Invalid " + WebService.CASE_SET_ID + ":  " + patientSetId + ".");
            }
            patientList = selectedPatientList.getPatientList();
        }
        else if (patients != null) {
            for (String _patient : patients.split("[\\s,]+")) {
                _patient = _patient.trim();
                if (_patient.length() == 0) continue;
                patientList.add(_patient);
            }
        }
        else if (samples != null) {     // todo: this is a hack, samples is just another word for patients
            return new ArrayList(Arrays.asList(samples.split(" ")));
        }
        else {
            throw new ProtocolException(WebService.CASE_SET_ID + " or " + WebService.CASE_LIST + " must be specified.");
        }
        return patientList;
    }

    /**
     * Given an HttpServletRequest, determine all cancer_study_ids associated with it.
     * cancer study identifiers can be inferred from profile_ids, case_list_ids, or case_ids.
     * this returns the set of ALL POSSIBLE cancer study identifiers
     *
     * @param request
     * @return the cancer_study_ids associated with the request, which will be empty
     *         if none can be determined; or empty set if a problem arises.
     * @throws DaoException
     * @throws ProtocolException
     */
    public static HashSet<String> getCancerStudyIDs(HttpServletRequest request)
            throws DaoException, ProtocolException {

        HashSet<String> cancerStudies = new HashSet<String>();

        // a CANCER_STUDY_ID is explicitly provided, as in getGeneticProfiles, getCaseLists, etc.
        // make sure the cancer_study_id provided in the request points to a real study
        String studyIDstring = getCancerStudyId(request);
        if (studyIDstring != null) {
            if (DaoCancerStudy.doesCancerStudyExistByStableId(studyIDstring)) {
                cancerStudies.add(studyIDstring);
            }
            
            return cancerStudies;
        }

        // a genetic_profile_id is explicitly provided, as in getProfileData
        if (null != request.getParameter(WebService.GENETIC_PROFILE_ID)) {
            ArrayList<String> geneticProfileIds = getGeneticProfileId(request);
            for (String geneticProfileId : geneticProfileIds) {

                // that's the point of this code??
//                if (geneticProfileId == null) {
//                    return cancerStudies;
//                }

                GeneticProfile aGeneticProfile = DaoGeneticProfile.getGeneticProfileByStableId(geneticProfileId);
                if (aGeneticProfile != null &&
                        DaoCancerStudy.doesCancerStudyExistByInternalId(aGeneticProfile.getCancerStudyId())) {
                    cancerStudies.add(DaoCancerStudy.getCancerStudyByInternalId
                            (aGeneticProfile.getCancerStudyId()).getCancerStudyStableId());
                }
            }
            
            return cancerStudies;
        }

        // a patient_set_id is explicitly provided, as in getProfileData, getMutationData, getClinicalData, etc.
        String patientSetId = request.getParameter(WebService.CASE_SET_ID);
        if (patientSetId != null) {
            DaoPatientList aDaoPatientList = new DaoPatientList();
            PatientList aPatientList = aDaoPatientList.getPatientListByStableId(patientSetId);
            
            if (aPatientList != null && DaoCancerStudy.doesCancerStudyExistByInternalId(aPatientList.getCancerStudyId())) {
                cancerStudies.add(DaoCancerStudy.getCancerStudyByInternalId
                        (aPatientList.getCancerStudyId()).getCancerStudyStableId());
            } 
            
            return cancerStudies;
        }
        
        return cancerStudies;
    }

    // TODO: rename TO getGeneticProfileId, as the return value is PLURAL
    public static ArrayList<String> getGeneticProfileId(HttpServletRequest request) throws ProtocolException {
        String geneticProfileIdStr = request.getParameter(WebService.GENETIC_PROFILE_ID);
        //  Split on white space or commas
        Pattern p = Pattern.compile("[,\\s]+");
        String geneticProfileIds[] = p.split(geneticProfileIdStr);
        ArrayList<String> geneticProfileIdList = new ArrayList<String>();
        for (String geneticProfileId : geneticProfileIds) {
            geneticProfileId = geneticProfileId.trim();
            geneticProfileIdList.add(geneticProfileId);
        }
        return geneticProfileIdList;
    }
    

    /**
     * Get Cancer Study ID in a backward compatible fashion.
     */
    public static String getCancerStudyId(HttpServletRequest request) {
        String cancerStudyId = request.getParameter(WebService.CANCER_STUDY_ID);
        if (cancerStudyId == null || cancerStudyId.length() == 0) {
            cancerStudyId = request.getParameter(WebService.CANCER_TYPE_ID);
        }
        return cancerStudyId;
    }

    /**
     * Get the return format requested
     */

    public static String getFormat(HttpServletRequest request) {
        String format = request.getParameter(WebService.FORMAT);

        return format == null ? format : format.toLowerCase();
    }

    public static String getCaseSetId(HttpServletRequest request) {
        return request.getParameter(WebService.CASE_SET_ID);
    }
}
