/*
 * Copyright (c) 2019 Memorial Sloan-Kettering Cancer Center.
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

package org.cbioportal.persistence.util;

import java.util.*;
import javax.cache.CacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.Configuration;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.core.config.DefaultConfiguration;
import org.ehcache.jsr107.EhcacheCachingProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

public class CustomEhCachingProvider extends EhcacheCachingProvider {

    private static final Logger LOG = LoggerFactory.getLogger(CustomEhCachingProvider.class);

    @Value("${ehcache.xml_configuration:/ehcache.xml}")
    private String xmlConfiguration;

    @Value("${ehcache.cache_enabled}")
    private Boolean cacheEnabled;

    @Override
    public CacheManager getCacheManager() {

        CacheManager toReturn = null;
         try {
            if (cacheEnabled) {
                LOG.info("Caching is enabled, using '" + xmlConfiguration + "' for configuration");
                toReturn = this.getCacheManager(getClass().getResource(xmlConfiguration).toURI(),
                                            getClass().getClassLoader());
            } else {
                LOG.info("Caching is disabled");
                // we can not really disable caching,
                // we can not make a cache of 0 objects, 
                // and we can not make a heap of memory size 0, so make a tiny heap
                CacheConfiguration<Object, Object> generalRepositoryCacheConfiguration = CacheConfigurationBuilder.newCacheConfigurationBuilder(Object.class,
                    Object.class,
                    ResourcePoolsBuilder.newResourcePoolsBuilder().heap(1, MemoryUnit.B)).build();
                CacheConfiguration<Object, Object> staticRepositoryCacheOneConfiguration = CacheConfigurationBuilder.newCacheConfigurationBuilder(Object.class,
                    Object.class,
                    ResourcePoolsBuilder.newResourcePoolsBuilder().heap(1, MemoryUnit.B)).build();
                
                Map<String, CacheConfiguration<?, ?>> caches = new HashMap<>();
                caches.put("GeneralRepositoryCache", generalRepositoryCacheConfiguration);
                caches.put("StaticRepositoryCacheOne", staticRepositoryCacheOneConfiguration);

                Configuration configuration = new DefaultConfiguration(caches, this.getDefaultClassLoader());

                toReturn = this.getCacheManager(this.getDefaultURI(), configuration);
            }
        }
        catch (Exception e) {
            LOG.error(e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return toReturn;
    }
}
 
