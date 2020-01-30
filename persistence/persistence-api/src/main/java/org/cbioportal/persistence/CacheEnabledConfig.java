package org.cbioportal.persistence;

import java.util.ArrayList;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;

public class CacheEnabledConfig {
    private boolean enabled;

    public static final String DISK = "disk";
    public static final String HEAP = "heap";
    public static final String HYBRID = "hybrid";

    public static ArrayList<String> validCacheTypes = new ArrayList<String>(
        Arrays.asList(DISK, HEAP, HYBRID)
    );

    public CacheEnabledConfig(String cacheType) {
        this.enabled = enableCache(cacheType);
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
