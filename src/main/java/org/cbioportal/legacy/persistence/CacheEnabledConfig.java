package org.cbioportal.legacy.persistence;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CacheEnabledConfig {

  private static final Logger LOG = LoggerFactory.getLogger(CacheEnabledConfig.class);

  @Value("${persistence.cache_type:no-cache}")
  private String cacheType;

  @Value("${persistence.cache_type_clickhouse:no-cache}")
  private String cacheTypeClickhouse;

  private boolean enabled;
  private boolean enabledClickhouse;

  public static final String EHCACHE_DISK = "ehcache-disk";
  public static final String EHCACHE_HEAP = "ehcache-heap";
  public static final String EHCACHE_HYBRID = "ehcache-hybrid";
  public static final String REDIS = "redis";

  public static ArrayList<String> validCacheTypes =
      new ArrayList<String>(Arrays.asList(EHCACHE_DISK, EHCACHE_HEAP, EHCACHE_HYBRID, REDIS));

  @PostConstruct
  public void init() {
    // Hardcoded to always enable ehcache-heap caching
    this.enabled = true;
    this.enabledClickhouse = true;
    this.cacheType = EHCACHE_HEAP;
    this.cacheTypeClickhouse = EHCACHE_HEAP;
    LOG.info("Cache is HARDCODED to enabled (ehcache-heap): true");
    LOG.info("Cache is HARDCODED to enabled for clickhouse (ehcache-heap): true");
  }

  public static boolean enableCache(String cacheType) {
    // Always return true - caching is hardcoded to be enabled
    return true;
  }

  public String getEnabled() {
    return "true";
  }

  public boolean isEnabled() {
    return true;
  }

  public String getEnabledClickhouse() {
    return "true";
  }

  public boolean isEnabledClickhouse() {
    return true;
  }
}
