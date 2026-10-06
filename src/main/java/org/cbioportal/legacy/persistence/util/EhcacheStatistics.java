/*
 * Copyright (c) 2019 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cbioportal.legacy.persistence.util;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import org.cbioportal.legacy.utils.config.annotation.ConditionalOnProperty;
import org.ehcache.config.ResourceType;
import org.ehcache.core.statistics.*;
import org.ehcache.impl.internal.statistics.DefaultStatisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
    name = "persistence.cache_type",
    havingValue = {"ehcache-heap", "ehcache-disk", "ehcache-hybrid"})
public class EhcacheStatistics {

  private static final Logger log = LoggerFactory.getLogger(EhcacheStatistics.class);

  private static String TIER_NOT_IN_USE = "Tier not in use";

  private static double BYTES_IN_MB = 1048576.0;
  private static double BYTES_IN_GB = 1073741824.0;

  @Autowired private CustomEhcachingProvider customEhcachingProvider;

  private javax.cache.CacheManager cacheManager;
  private DefaultStatisticsService statisticsService;

  @PostConstruct
  public void initializeStatisticsService() {
    try {
      cacheManager = customEhcachingProvider.getCacheManager();
      statisticsService = new DefaultStatisticsService();
      for (String cacheName : cacheManager.getCacheNames()) {
        javax.cache.Cache cache = cacheManager.getCache(cacheName);
        org.ehcache.Cache ehcache = (org.ehcache.Cache) cache.unwrap(org.ehcache.Cache.class);
        statisticsService.cacheAdded(cacheName, ehcache);
      }
    } catch (Exception e) {
      log.error("Error initializing Ehcache statistics service", e);
    }
  }

  public String getCacheStatistics() {
    StringBuilder builder = new StringBuilder();
    builder.append("\n\nCACHE_STATISTICS START\n\n");
    for (String cacheName : cacheManager.getCacheNames()) {
      builder.append("Cache: " + cacheName + "\n");
      builder.append(
          "Allocated (heap): " + getAllocatedBytes(cacheName, ResourceType.Core.HEAP) + "\n");
      builder.append(
          "Occupied (heap): "
              + getOccupiedBytes(cacheName, "OnHeap", ResourceType.Core.HEAP)
              + "\n");
      builder.append(
          "Allocated (disk): " + getAllocatedBytes(cacheName, ResourceType.Core.DISK) + "\n");
      builder.append(
          "Occupied (disk): " + getOccupiedBytes(cacheName, "Disk", ResourceType.Core.DISK) + "\n");
      builder.append("\n");
    }
    builder.append("CACHE_STATISTICS END\n");
    return builder.toString();
  }

  private String getAllocatedBytes(String cacheName, ResourceType.Core resourceType) {
    try {
      org.ehcache.Cache ehcache = getEhcache(cacheName);
      return (getAllocatedBytes(ehcache, resourceType) + getAllocatedUnit(ehcache, resourceType));
    } catch (NullPointerException e) {
      return TIER_NOT_IN_USE;
    }
  }

  private String getOccupiedBytes(String cacheName, String tier, ResourceType.Core resourceType) {
    try {
      CacheStatistics cacheStatistics = statisticsService.getCacheStatistics(cacheName);
      Map<String, TierStatistics> tierStatistics = cacheStatistics.getTierStatistics();
      long occupiedBytes = tierStatistics.get(tier).getOccupiedByteSize();
      return scaleOccupiedBytes(cacheName, occupiedBytes, resourceType);
    } catch (NullPointerException e) {
      return TIER_NOT_IN_USE;
    }
  }

  private org.ehcache.Cache getEhcache(String cacheName) {
    javax.cache.Cache cache = cacheManager.getCache(cacheName);
    return (org.ehcache.Cache) cache.unwrap(org.ehcache.Cache.class);
  }

  private String getAllocatedBytes(org.ehcache.Cache ehcache, ResourceType.Core resourceType)
      throws NullPointerException {
    return String.valueOf(
        ehcache
            .getRuntimeConfiguration()
            .getResourcePools()
            .getPoolForResource(resourceType)
            .getSize());
  }

  private String getAllocatedUnit(org.ehcache.Cache ehcache, ResourceType.Core resourceType)
      throws NullPointerException {
    return ehcache
        .getRuntimeConfiguration()
        .getResourcePools()
        .getPoolForResource(resourceType)
        .getUnit()
        .toString();
  }

  private String scaleOccupiedBytes(
      String cacheName, long occupiedBytes, ResourceType.Core resourceType)
      throws NullPointerException {
    org.ehcache.Cache ehcache = getEhcache(cacheName);
    String allocatedUnit = getAllocatedUnit(ehcache, resourceType);
    if (allocatedUnit.equals("MB")) {
      return String.valueOf(
          String.format("%.01f", occupiedBytes / BYTES_IN_MB) + "MB (" + occupiedBytes + " bytes)");
    } else if (allocatedUnit.equals("GB")) {
      return String.valueOf(
          String.format("%.01f", occupiedBytes / BYTES_IN_GB) + "GB (" + occupiedBytes + " bytes)");
    } else {
      return (String.valueOf(occupiedBytes) + " bytes");
    }
  }
}
