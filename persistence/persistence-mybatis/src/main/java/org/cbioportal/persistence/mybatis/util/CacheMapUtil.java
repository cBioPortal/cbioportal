/*
 * Copyright (c) 2018 - 2019 Memorial Sloan-Kettering Cancer Center.
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

package org.cbioportal.persistence.mybatis.util;

import java.util.*;
import javax.annotation.PostConstruct;
import org.cbioportal.model.*;
import org.cbioportal.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CacheMapUtil {

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private MolecularProfileRepository molecularProfileRepository;

    @Autowired
    private SampleListRepository sampleListRepository;

    @Autowired
    private GenericAssayRepository genericAssayRepository;

    @Value("${authenticate:false}")
    private String authenticate;

    private Boolean cacheEnabled;
    private static final Logger LOG = LoggerFactory.getLogger(CacheMapUtil.class);

    private static final int REPOSITORY_RESULT_LIMIT = Integer.MAX_VALUE; // retrieve all entries (no limit to return size)
    private static final int REPOSITORY_RESULT_OFFSET = 0; // retrieve all entries (do not skip any)

    // maps used to cache required relationships - in all maps stable ids are key
    private Map<String, MolecularProfile> molecularProfileCache;
    private Map<String, SampleList> sampleListCache;
    private Map<String, CancerStudy> cancerStudyCache;
    private Map<String, String> genericAssayStableIdToMolecularProfileIdCache;

    public Map<String, MolecularProfile> getMolecularProfileMap() {
        return molecularProfileCache;
    }

    public Map<String, SampleList> getSampleListMap() {
        return sampleListCache;
    }

    public Map<String, CancerStudy> getCancerStudyMap() {
        return cancerStudyCache;
    }

    public Map<String, String> getGenericAssayStableIdToMolecularProfileIdMap() {
        return genericAssayStableIdToMolecularProfileIdCache;
    }

    @PostConstruct
    private void initializeCacheMemory() {
        // CHANGES TO THIS LIST MUST BE PROPAGATED TO 'GlobalProperties'
        this.cacheEnabled = (!authenticate.isEmpty() 
                && !authenticate.equals("false") 
                && !authenticate.contains("social_auth"));
        if (cacheEnabled) {
            LOG.debug("creating cache maps for authorization");
            populateMolecularProfileMap();
            populateSampleListMap();
            populateCancerStudyMap();
            populateGenericAssayStableIdToMolecularProfileIdMap();
        }
    }

    private void populateMolecularProfileMap() {
        if (molecularProfileCache == null) {
            molecularProfileCache = new HashMap<String, MolecularProfile>();
        }
        for (MolecularProfile mp : molecularProfileRepository.getAllMolecularProfiles(
                "SUMMARY",
                REPOSITORY_RESULT_LIMIT,
                REPOSITORY_RESULT_OFFSET,
                null,
                "ASC")) {
            molecularProfileCache.put(mp.getStableId(), mp);
        }
        LOG.debug("  molecular profile map size: " + molecularProfileCache.size());
    }

    private void populateSampleListMap() {
        if (sampleListCache == null) {
            sampleListCache = new HashMap<String, SampleList>();
        }
        for (SampleList sl : sampleListRepository.getAllSampleLists(
                "SUMMARY",
                REPOSITORY_RESULT_LIMIT,
                REPOSITORY_RESULT_OFFSET,
                null,
                "ASC")) {
            sampleListCache.put(sl.getStableId(), sl);
        }
        LOG.debug("  sample list map size: " + sampleListCache.size());
    }

    private void populateCancerStudyMap() {
        if (cancerStudyCache == null) {
            cancerStudyCache = new HashMap<String, CancerStudy>();
        }
        for (CancerStudy cs : studyRepository.getAllStudies(
                null,
                "SUMMARY",
                REPOSITORY_RESULT_LIMIT,
                REPOSITORY_RESULT_OFFSET,
                null,
                "ASC")) {
            cancerStudyCache.put(cs.getCancerStudyIdentifier(), cs);
        }
        LOG.debug("  cancer study map size: " + cancerStudyCache.size());
    }

    private void populateGenericAssayStableIdToMolecularProfileIdMap() {
        if (genericAssayStableIdToMolecularProfileIdCache == null) {
            genericAssayStableIdToMolecularProfileIdCache = new HashMap<String, String>();
        }
        for (MolecularProfile mp : molecularProfileRepository.getAllMolecularProfiles(
            "SUMMARY",
            REPOSITORY_RESULT_LIMIT,
            REPOSITORY_RESULT_OFFSET,
            null,
            "ASC")) {
            // Only select GENERIC_ASSAY profiles
            if (mp.getMolecularAlterationType().toString() == EntityType.GENERIC_ASSAY.toString()) {
                List<String> molecularId = new ArrayList<String>();
                molecularId.add(mp.getStableId());
                List<String> stableIds = genericAssayRepository.getGenericAssayStableIdsByMolecularIds(molecularId);
                for (String stableId : stableIds) {
                    genericAssayStableIdToMolecularProfileIdCache.put(stableId, mp.getStableId());
                }
            }
        }
        LOG.debug(" generic assay stableId to molecularProfileId map size: " + genericAssayStableIdToMolecularProfileIdCache.size());
    }

    /**
     * @return the cacheEnabled
     */
    public Boolean hasCacheEnabled() {
        return cacheEnabled;
    }

    /**
     * @param cacheEnabled the cacheEnabled to set
     */
    public void setCacheEnabled(Boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }

}
