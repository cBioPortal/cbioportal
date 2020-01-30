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
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.web_api.GetSampleLists;

/**
 * Utility class for the user-defined sample sets (samplet ID list).
 *
 * @author Selcuk Onur Sumer
 */
public class SampleSetUtil {

    /**
     * Checks whether the provided sample IDs are valid for a specific
     * cancer study. This method returns a list of invalid samples if
     * any, or returns an empty list if all the samples are valid.
     *
     * @param studyId			stable cancer study id
     * @param sampleIds		sample IDs as a single string
     * @return					list of invalid samples
     * @throws DaoException		if a DB error occurs
     */
    public static List<String> validateSampleSet(
        String studyId,
        String sampleIds
    )
        throws DaoException {
        ArrayList<String> invalidSample = new ArrayList<String>();

        // get cancer study for the given stable id
        CancerStudy study = DaoCancerStudy.getCancerStudyByStableId(studyId);
        int iStudyId = study.getInternalId();

        if (sampleIds != null) {
            // validate each sample ID
            for (String sampleId : sampleIds.trim().split("\\s+")) {
                Sample sample = DaoSample.getSampleByCancerStudyAndSampleId(
                    iStudyId,
                    sampleId
                );

                if (sample == null) {
                    invalidSample.add(sampleId);
                }
            }
        }

        return invalidSample;
    }

    /**
     * Shortens the (possibly long) user-defined sample id list by hashing.
     * Also adds the generated (key, text) pair to the database for
     * future reference.
     *
     * @param sampleIds	sample ID string to be shortened
     * @return			short (hashed) version of sample IDs
     * @throws DaoException
     */
    public static String shortenSampleIds(String sampleIds)
        throws DaoException {
        DaoTextCache dao = new DaoTextCache();

        // normalize sample IDs list to avoid redundant white spaces
        String normalizedIds = normalizeSampleIds(sampleIds);

        // generate hash key for the sample IDs string
        String sampleIdsKey = dao.generateKey(normalizedIds);

        // add new key and list pair to DB if record does not exist
        if (dao.getText(sampleIdsKey) == null) {
            dao.cacheText(sampleIdsKey, normalizedIds);
        }

        // return hash key
        return sampleIdsKey;
    }

    /**
     * Retrieves the sample ID list corresponding to the given key.
     *
     * @param sampleIdsKey	key for a specific sample id list
     * @return				sample id list corresponding to the given key
     * @throws DaoException
     */
    public static String getSampleIds(String sampleIdsKey) throws DaoException {
        DaoTextCache dao = new DaoTextCache();

        String sampleIds = dao.getText(sampleIdsKey);

        return sampleIds;
    }

    /**
     * Normalize the given sample list by trimming and replacing any white space
     * character with single space. This is to prevent same sample lists
     * to be interpreted as different lists just because of the different white
     * space characters.
     *
     * @param sampleIds	list of sample ids to be normalized
     * @return			normalized string with the same sample ids
     */
    public static String normalizeSampleIds(String sampleIds) {
        return sampleIds.trim().replaceAll("\\s", " ");
    }
}
