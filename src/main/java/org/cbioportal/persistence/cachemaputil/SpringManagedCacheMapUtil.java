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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
// Instantiate when user authorization is active and spring-managed implementation is needed
@ConditionalOnExpression("{'oauth2','saml','saml_plus_basic'}.contains('${authenticate}') or ('optional_oauth2' eq '${authenticate}' and 'true' eq '${security.method_authorization_enabled}')")
@ConditionalOnProperty(value = "cache.cache-map-utils.spring-managed", havingValue = "true")
public class SpringManagedCacheMapUtil implements CacheMapUtil {

    private static final Logger LOG = LoggerFactory.getLogger(SpringManagedCacheMapUtil.class);

    @Value("${persistence.cache_type:no-cache}")
    private String cacheType;
    @Value("${persistence.cache_type_clickhouse:no-cache}")
    private String cacheTypeClickhouse;
    
    @Value("${cache.cache-map-utils.spring-managed}")
    private boolean springManagedCacheMapUtils;

    @Autowired
    private CacheMapBuilder cacheMapBuilder;
    
    @PostConstruct
    public void init() {
        // Make sure the user does not have a conflicting configuration. Explode if there is.
        if (cacheType.equals("no-cache") && cacheTypeClickhouse.equals("no-cache") && springManagedCacheMapUtils) {
            throw new RuntimeException("cache.cache-map-utils.spring-managed property is set to 'true' but the portal is not " +
                "configured with a cache-implementation (persistence.cache_type property is 'no-cache'). Please set to 'false'" +
                " or configure the cache.");
        }
    }
    
    // This implementation of the CacheMapUtils does not keep a locally cached/referenced HashMap
    // but retrieves the HashMaps from the active Spring caching solution.

    @Override
    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    public Map<String, MolecularProfile> getMolecularProfileMap() {
        LOG.debug("Building molecularProfileMap (cache miss)");
        return cacheMapBuilder.buildMolecularProfileMap();
    }

    @Override
    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    public Map<String, SampleList> getSampleListMap() {
        LOG.debug("Building sampleListMap (cache miss)");
        return cacheMapBuilder.buildSampleListMap();
    }

    @Override
    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    public Map<String, CancerStudy> getCancerStudyMap() {
        LOG.debug("Building cancerStudyMap (cache miss)");
        return cacheMapBuilder.buildCancerStudyMap();
    }

    //  bean is only instantiated when there is user authorization
    @Override
    public boolean hasCacheEnabled() {
        return true;
    }

}
