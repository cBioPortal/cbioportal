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

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

public class CustomRedisCachingProvider {

    private static final Logger LOG = LoggerFactory.getLogger(CustomRedisCachingProvider.class);

    @Value("${redis.name:cbioportal}")
    private String redisName;

    @Value("${redis.leader_address}")
    private String leaderAddress;

    @Value("${redis.follower_address}")
    private String followerAddress;

    @Value("${redis.database}")
    private Integer database;

    @Value("${redis.password}")
    private String password;
    
    @Value("${redis.ttl_mins:10000}")
    private Long expiryMins;

    @Value("${redis.clear_on_startup:true}")
    private boolean clearOnStartup;
    
    public RedissonClient getRedissonClient() {
        if (leaderAddress == null || "".equals(leaderAddress)) {
            return null;
        }
        
        Config config = new Config();
        LOG.debug("leaderAddress: " + leaderAddress);
        LOG.debug("followerAddress: " + followerAddress);
        config.useMasterSlaveServers()
                .setMasterAddress(leaderAddress)
                .addSlaveAddress(followerAddress)
                .setDatabase(database)
                .setPassword(password);
        
        RedissonClient redissonClient = Redisson.create(config);
        LOG.debug("Created Redisson Client: " + redissonClient);
        return redissonClient;
    }

    public CacheManager getCacheManager(RedissonClient redissonClient) {
        CustomRedisCacheManager manager = new CustomRedisCacheManager(redissonClient, expiryMins);
        
        if (clearOnStartup) {
        	Cache generalCache = manager.getCache(redisName + "GeneralRepositoryCache");
        	if(generalCache != null) {
        		generalCache.clear();
        	}
            
            Cache staticRepositoryCache = manager.getCache(redisName + "StaticRepositoryCacheOne");
            if(staticRepositoryCache != null) {
            	staticRepositoryCache.clear();
            }
        }
        return manager;
    }
}
