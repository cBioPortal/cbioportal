package org.cbioportal.persistence;

import org.springframework.beans.factory.annotation.Value;
import java.util.ArrayList;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheEnabledConfig {

    private static final Logger LOG = LoggerFactory.getLogger(CacheEnabledConfig.class);

    private boolean enabled;
    
    public static final String EHCACHE_DISK = "ehcache-disk";
    public static final String EHCACHE_HEAP = "ehcache-heap";
    public static final String EHCACHE_HYBRID = "ehcache-hybrid";
    public static final String REDIS = "redis";

    public static ArrayList<String> validCacheTypes = new ArrayList<String>(Arrays.asList(EHCACHE_DISK, EHCACHE_HEAP, EHCACHE_HYBRID, REDIS));

    public CacheEnabledConfig(String cacheType) {
        this.enabled = enableCache(cacheType);
        LOG.info("Cache is enabled: " + this.enabled);
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

}
