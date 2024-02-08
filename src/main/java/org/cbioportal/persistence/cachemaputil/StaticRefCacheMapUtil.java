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

package org.cbioportal.persistence.cachemaputil;

import jakarta.annotation.PostConstruct;
import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.SampleList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
// Instantiate when user authorization is active and spring-managed implementation is not needed
@ConditionalOnExpression("{'oauth2','saml','saml_plus_basic'}.contains('${authenticate}') or ('optional_oauth2' eq '${authenticate}' and 'true' eq '${security.method_authorization_enabled}')")
@ConditionalOnProperty(value = "cache.cache-map-utils.spring-managed", havingValue = "false", matchIfMissing = true)
public class StaticRefCacheMapUtil implements CacheMapUtil {

    private static final Logger LOG = LoggerFactory.getLogger(StaticRefCacheMapUtil.class);

    @Autowired
    private CacheMapBuilder cacheMapBuilder;

    // This implementation of the CacheMapUtils keeps a locally cached/referenced HashMap and does
    // not defer to any Spring managed caching solution.

    // maps used to cache required relationships - in all maps stable ids are key
    // Fields are static because the proxying mechanism of the CancerStudyPermissionEvaluator
    // appears to perturb the Singleton scope of the CacheMapUtils bean. When debugging
    // two version appeared to exist in context. A mechanism with bean injection did not work here.
    static Map<String, MolecularProfile> molecularProfileCache;
    static Map<String, SampleList> sampleListCache;
    static Map<String, CancerStudy> cancerStudyCache;

    @PostConstruct
    private void init() {
        initializeCacheMemory();
    }

    public synchronized void initializeCacheMemory() {
        LOG.debug("creating cache maps for authorization");
        molecularProfileCache = cacheMapBuilder.buildMolecularProfileMap();
        sampleListCache = cacheMapBuilder.buildSampleListMap();
        cancerStudyCache = cacheMapBuilder.buildCancerStudyMap();
    }

    @Override
    public Map<String, MolecularProfile> getMolecularProfileMap() {
        return molecularProfileCache;
    }

    @Override
    public Map<String, SampleList> getSampleListMap() {
        return sampleListCache;
    }

    @Override
    public Map<String, CancerStudy> getCancerStudyMap() {
        return cancerStudyCache;
    }
    
    @Override
    public boolean hasCacheEnabled() {
        return true;
    }

}
