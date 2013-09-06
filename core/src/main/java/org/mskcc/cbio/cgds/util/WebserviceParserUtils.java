/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/


package org.mskcc.cbio.cgds.util;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import org.mskcc.cbio.cgds.dao.DaoCancerStudy;

import org.mskcc.cbio.cgds.dao.DaoCaseList;
import org.mskcc.cbio.cgds.dao.DaoCaseProfile;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.dao.DaoGeneticProfile;
import org.mskcc.cbio.cgds.model.CaseList;
import org.mskcc.cbio.cgds.model.GeneticProfile;
import org.mskcc.cbio.cgds.servlet.WebService;
import org.mskcc.cbio.cgds.web_api.ProtocolException;
import org.mskcc.cbio.portal.util.CaseSetUtil;

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
    public static ArrayList<String> getCaseList(HttpServletRequest request) throws ProtocolException,
            DaoException {
        String cases = request.getParameter(WebService.CASE_LIST);
        String caseSetId = request.getParameter(WebService.CASE_SET_ID);
        String caseIdsKey = request.getParameter(WebService.CASE_IDS_KEY);
        String samples = request.getParameter("samples");

        if (cases == null &&
        	caseIdsKey != null)
        {
        	cases = CaseSetUtil.getCaseIds(caseIdsKey);
        }

        ArrayList<String> caseList = new ArrayList<String>();
        if (caseSetId != null) {
            DaoCaseList dao = new DaoCaseList();
            CaseList selectedCaseList = dao.getCaseListByStableId(caseSetId);
            if (selectedCaseList == null) {
                throw new ProtocolException("Invalid " + WebService.CASE_SET_ID + ":  " + caseSetId + ".");
            }
            caseList = selectedCaseList.getCaseList();
        }
        else if (cases != null) {
            for (String _case : cases.split("[\\s,]+")) {
                _case = _case.trim();
                if (_case.length() == 0) continue;
                caseList.add(_case);
            }
        }
        else if (samples != null) {     // todo: this is a hack, samples is just another word for cases
            return new ArrayList(Arrays.asList(samples.split(" ")));
        }
        else {
            throw new ProtocolException(WebService.CASE_SET_ID + " or " + WebService.CASE_LIST + " must be specified.");
        }
        return caseList;
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

        // a case_set_id is explicitly provided, as in getProfileData, getMutationData, getClinicalData, etc.
        String caseSetId = request.getParameter(WebService.CASE_SET_ID);
        if (caseSetId != null) {
            DaoCaseList aDaoCaseList = new DaoCaseList();
            CaseList aCaseList = aDaoCaseList.getCaseListByStableId(caseSetId);
            
            if (aCaseList != null && DaoCancerStudy.doesCancerStudyExistByInternalId(aCaseList.getCancerStudyId())) {
                cancerStudies.add(DaoCancerStudy.getCancerStudyByInternalId
                        (aCaseList.getCancerStudyId()).getCancerStudyStableId());
            } 
            
            return cancerStudies;
        }
        
        // Cannot not this any more because case IDs are not necessary unique.
//        // a case_list is explicitly provided, as in getClinicalData, etc.
//        String caseList = request.getParameter(WebService.CASE_LIST);
//        String caseIdsKey = request.getParameter(WebService.CASE_IDS_KEY);
//        
//        // no case list provided, but case IDs key provided
//        if (caseList == null
//        	&& caseIdsKey != null)
//        {
//        	// try to get case list by using the key
//        	caseList = CaseSetUtil.getCaseIds(caseIdsKey);
//        }
//        
//        if (caseList != null) {
//            for (String aCase : caseList.split("[\\s,]+")) {
//                aCase = aCase.trim();
//                if (aCase.length() == 0) {
//                    continue;
//                }
//
//                int profileId = DaoCaseProfile.getProfileIdForCase(aCase);
//                if (DaoCaseProfile.NO_SUCH_PROFILE_ID == profileId) {
//                    return cancerStudies;
//                }
//
//                GeneticProfile aGeneticProfile = DaoGeneticProfile.getGeneticProfileById(profileId);
//                if (aGeneticProfile == null) {
//                    return cancerStudies;
//                }
//                if (DaoCancerStudy.doesCancerStudyExistByInternalId(aGeneticProfile.getCancerStudyId())) {
//                    cancerStudies.add(DaoCancerStudy.getCancerStudyByInternalId
//                            (aGeneticProfile.getCancerStudyId()).getCancerStudyStableId());
//                } else {
//                    return cancerStudies;
//                }
//            }
//        }
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
