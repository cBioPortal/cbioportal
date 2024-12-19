package org.cbioportal.persistence;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;

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

    public static ArrayList<String> validCacheTypes = new ArrayList<String>(Arrays.asList(EHCACHE_DISK, EHCACHE_HEAP, EHCACHE_HYBRID, REDIS));

    @PostConstruct
    public void init() {
        this.enabled = enableCache(cacheType);
        LOG.info("Cache is enabled: " + this.enabled);
        this.enabledClickhouse = enableCache(cacheTypeClickhouse);
        LOG.info("Cache is enabled for clickhouse: " + this.enabledClickhouse);
    }

    public static boolean enableCache(String cacheType) {
        for (String validCacheType : validCacheTypes) {
            if (validCacheType.equalsIgnoreCase(cacheType)) {
                return true;
            }
        }
        return false;
    }

    public String getEnabled() {
        if (enabled) {
            return "true";
        } else {
            return "false";
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getEnabledClickhouse() {
        if (enabledClickhouse) {
            return "true";
        } else {
            return "false";
        }
    }

    public boolean isEnabledClickhouse() {
        return enabledClickhouse;
    }

}
