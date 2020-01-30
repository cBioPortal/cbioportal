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

package org.mskcc.cbio.portal.util;

import java.util.*;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.servlet.WebService;
import org.mskcc.cbio.portal.web_api.ProtocolException;

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
    public static ArrayList<String> getSampleIds(HttpServletRequest request)
        throws ProtocolException, DaoException {
        String samples = request.getParameter(WebService.CASE_LIST);
        String sampleSetId = request.getParameter(WebService.CASE_SET_ID);
        String sampleIdsKey = request.getParameter(WebService.CASE_IDS_KEY);

        if (sampleIdsKey != null) {
            samples = SampleSetUtil.getSampleIds(sampleIdsKey);
        }

        ArrayList<String> sampleList = new ArrayList<String>();
        if (sampleSetId != null && !(sampleSetId.equals("-1"))) {
            DaoSampleList dao = new DaoSampleList();
            SampleList selectedSampleList = dao.getSampleListByStableId(
                sampleSetId
            );
            if (selectedSampleList == null) {
                throw new ProtocolException(
                    "Invalid " +
                    WebService.CASE_SET_ID +
                    ":  " +
                    sampleSetId +
                    "."
                );
            }
            sampleList = selectedSampleList.getSampleList();
        } else if (samples != null) {
            for (String _sample : samples.split("[\\s,]+")) {
                _sample = _sample.trim();
                if (_sample.length() == 0) continue;
                sampleList.add(_sample);
            }
        } else if (samples != null) { // todo: this is a hack, samples is just another word for patients
            return new ArrayList(Arrays.asList(samples.split(" ")));
        } else {
            throw new ProtocolException(
                WebService.CASE_SET_ID +
                " or " +
                WebService.CASE_LIST +
                " must be specified."
            );
        }
        return sampleList;
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

                GeneticProfile aGeneticProfile = DaoGeneticProfile.getGeneticProfileByStableId(
                    geneticProfileId
                );
                if (
                    aGeneticProfile != null &&
                    DaoCancerStudy.doesCancerStudyExistByInternalId(
                        aGeneticProfile.getCancerStudyId()
                    )
                ) {
                    cancerStudies.add(
                        DaoCancerStudy
                            .getCancerStudyByInternalId(
                                aGeneticProfile.getCancerStudyId()
                            )
                            .getCancerStudyStableId()
                    );
                }
            }

            return cancerStudies;
        }

        // a patient_set_id is explicitly provided, as in getProfileData, getMutationData, getClinicalData, etc.
        String sampleSetId = request.getParameter(WebService.CASE_SET_ID);
        if (sampleSetId != null) {
            DaoSampleList aDaoSampleList = new DaoSampleList();
            SampleList aSampleList = aDaoSampleList.getSampleListByStableId(
                sampleSetId
            );

            if (
                aSampleList != null &&
                DaoCancerStudy.doesCancerStudyExistByInternalId(
                    aSampleList.getCancerStudyId()
                )
            ) {
                cancerStudies.add(
                    DaoCancerStudy
                        .getCancerStudyByInternalId(
                            aSampleList.getCancerStudyId()
                        )
                        .getCancerStudyStableId()
                );
            }

            return cancerStudies;
        }

        return cancerStudies;
    }

    // TODO: rename TO getGeneticProfileId, as the return value is PLURAL
    public static ArrayList<String> getGeneticProfileId(
        HttpServletRequest request
    )
        throws ProtocolException {
        String geneticProfileIdStr = request.getParameter(
            WebService.GENETIC_PROFILE_ID
        );
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
