/*
 * Copyright (c) 2020 Memorial Sloan-Kettering Cancer Center.
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

import java.io.*;
import java.util.*;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.jcache.configuration.RedissonConfiguration;
import org.redisson.jcache.JCachingProvider;
import org.redisson.spring.cache.CacheConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

public class CustomRedisCachingProvider {

    private static final Logger LOG = LoggerFactory.getLogger(CustomRedisCachingProvider.class);

    @Value("${persistence.cache_type:no-cache}")
    private String cacheType;

    @Value("${app.name:cbioportal}")
    private String appName;

    public RedissonClient getRedissionClient() {
        Config config = new Config();
        config.useMasterSlaveServers()
                .setMasterAddress("redis://100.96.35.44:6379")
                .addSlaveAddress("redis://100.96.39.22:6379")
                .addSlaveAddress("redis://100.96.13.106:6379")
                .setPassword("PASSWORD_GOES_HERE");
        RedissonClient redissonClient = Redisson.create(config);
        LOG.error("Created Redisson Client: " + redissonClient);
        return redissonClient;
    }

    public CacheManager getCacheManager(RedissonClient redissonClient) {

        //RedissonSpringCacheManager toReturn = null;
        //Map<String, CacheConfig> caches = new HashMap<String, CacheConfig>();
        /*
            create cache with ttl and maxIdleTime
            ttl - - time to live for key\value entry in milliseconds. If 0 then time to live doesn't affect entry expiration.
            maxIdleTime - - max idle time for key\value entry in milliseconds.
            if maxIdleTime and ttl params are equal to 0 then entry stores infinitely.
            we rely on the server having maxmemory set and maxmemory-policy set to allkeys-lru or allkeys-lfu
            NOTE: for some reason having ttl and maxIdleTime set to 0 caused the portal to not start up even
            with allkeys-lfu eviction policy, so added maxIdleTime back.
        */
        //caches.put(appName + "GeneralRepositoryCache", new CacheConfig(0, 12*60*60*1000));
        //caches.put(appName + "StaticRepositoryCacheOne", new CacheConfig(0, 12*60*60*1000));
        //return new RedissonSpringCacheManager(redissonClient, config);

        MutableConfiguration<String, String> jcacheConfig = new MutableConfiguration<>();
        Configuration<String, String> config = RedissonConfiguration.fromInstance(redissonClient, jcacheConfig);
        CachingProvider redisCachingProvider = null;
        for (CachingProvider cachingProvider : Caching.getCachingProviders()) {
            LOG.debug("CachingProvider: " + cachingProvider);
            if (cachingProvider instanceof JCachingProvider) {
                redisCachingProvider = cachingProvider;
                break;
            }
        }
        if (redisCachingProvider == null) {
            // this should never happen, no one should try to create this bean (calling this method)
            // unless we have loaded the Redis libraries
            LOG.error("Failed to find a Redis caching provider");
            return null;
        }
        CacheManager manager = redisCachingProvider.getCacheManager();
        manager.createCache("TEST" + appName + "GeneralRepositoryCache", config);
        manager.createCache("TEST" + appName + "StaticRepositoryCacheOne", config);
        return manager;
    }

}
