/*
 * Copyright (c) 2020 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cbioportal.legacy.persistence.util;

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

  @Value("${redis.health_check_interval_ms:30000}")
  private Long redisHealthCheckIntervalMs;

  public RedissonClient getRedissonClient() {
    if (leaderAddress == null || "".equals(leaderAddress)) {
      return null;
    }

    try {
      Config config = new Config();
      LOG.debug("leaderAddress: " + leaderAddress);
      LOG.debug("followerAddress: " + followerAddress);
      config
          .useMasterSlaveServers()
          .setMasterAddress(leaderAddress)
          .addSlaveAddress(followerAddress)
          .setDatabase(database)
          .setPassword(password);

      RedissonClient redissonClient = Redisson.create(config);
      LOG.debug("Created Redisson Client: " + redissonClient);
      return redissonClient;
    } catch (Exception e) {
      LOG.warn(
          "Failed to connect to Redis: {}. Application will start without Redis caching and fallback to database queries.",
          e.getMessage());
      LOG.debug("Redis connection error details:", e);
      return null;
    }
  }

  public CacheManager getCacheManager(RedissonClient redissonClient) {
    if (redissonClient == null) {
      LOG.warn("Redis client is null. Creating no-op cache manager.");
      return new NoOpCacheManager();
    }

    CustomRedisCacheManager manager =
        new CustomRedisCacheManager(redissonClient, expiryMins, redisHealthCheckIntervalMs);

    if (clearOnStartup) {
      try {
        Cache generalCache = manager.getCache(redisName + "GeneralRepositoryCache");
        if (generalCache != null) {
          generalCache.clear();
        }

        Cache staticRepositoryCache = manager.getCache(redisName + "StaticRepositoryCacheOne");
        if (staticRepositoryCache != null) {
          staticRepositoryCache.clear();
        }
      } catch (Exception e) {
        LOG.warn(
            "Failed to clear Redis cache on startup: {}. Continuing without cache clearing.",
            e.getMessage());
        LOG.debug("Cache clearing error details:", e);
      }
    }
    return manager;
  }
}
