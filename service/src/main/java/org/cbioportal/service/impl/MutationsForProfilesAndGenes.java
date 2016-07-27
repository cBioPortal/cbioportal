/*
 * Copyright (c) 2016 Memorial Sloan-Kettering Cancer Center.
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

package org.cbioportal.service.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import org.apache.log4j.Logger;
import org.mskcc.cbio.portal.dao.DaoSampleList;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoTextCache;
import org.mskcc.cbio.portal.model.SampleList;
import org.mskcc.cbio.portal.util.MutationDataUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

/**
 * Implementation of getMutationsForProfilesAndGenes, which supplies mutations for Web API endpoint /api/mutationsforprofilesandgenes.
 */

@Service
public class MutationsForProfilesAndGenes {
    private static final Logger logger = Logger.getLogger(MutationsForProfilesAndGenes.class);

    private DaoTextCache daoTextCache = new DaoTextCache();
    @Autowired
    private MutationDataUtils mutationDataUtils;

    /**
     * entrypoint to service.
     * Constructs an optional list of samples (based on either sampleSetIds, SampleIdsKeys, or providedSampleIds),
     * then uses the core module's mutationDataUtils.getMutationData to fetch mutations.
     */
    public List<?> get(List<String> geneticProfileStableIds, List<String> hugoGeneSymbols, List<String> providedSampleIds, List<String> sampleSetIds, List<String> sampleIdsKeys) {
        ArrayList result = new ArrayList<Object>();
        try {
            // generate list by processing possible valid sample list parameters
            List<String> targetSampleList = this.getSampleList(providedSampleIds, sampleSetIds, sampleIdsKeys);
            for (String profileId : geneticProfileStableIds) {
                // add mutation data for each genetic profile
                result.addAll(mutationDataUtils.getMutationData(profileId, hugoGeneSymbols, targetSampleList));
            }
        } catch (DaoException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private List<String> getSampleList(List<String> providedSampleIds, List<String> sampleSetIds, List<String> sampleIdsKeys) throws DaoException {
        DaoSampleList daoSampleList = new DaoSampleList();
        ArrayList<String> sampleList = new ArrayList<String>();
        // first check if sampleSetId param provided
        if (sampleSetIds != null && sampleSetIds.size() != 0 && !(sampleSetIds.size() == 1 && sampleSetIds.get(0).equals("-1"))) {
            for (String id : sampleSetIds) {
                SampleList list = daoSampleList.getSampleListByStableId(id);
                if (list != null) {
                    sampleList.addAll(list.getSampleList());
                }
            }
        }
        // if there is no sampleSetId, then check for sampleIdsKey param
        else if(sampleIdsKeys != null && sampleIdsKeys.size() != 0) {
            for (String sampleIdKey : sampleIdsKeys) {
                String sampleIdListString = daoTextCache.getText(sampleIdKey);
                if (sampleIdListString != null && sampleIdListString.length() > 0) {
                    String[] samples = sampleIdListString.split("[\\s,]+");
                    //return new ArrayList<String>(Arrays.asList(parts));
                    Collections.addAll(sampleList,samples);
                } 
            }
        } else if (providedSampleIds != null) {
            sampleList.addAll(providedSampleIds);
        }
        return sampleList;
    }
}
