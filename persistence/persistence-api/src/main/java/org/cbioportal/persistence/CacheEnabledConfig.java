package org.cbioportal.persistence;

import org.springframework.beans.factory.annotation.Value;

public class CacheEnabledConfig {

    private boolean enabled;

    public CacheEnabledConfig(String cacheEnabled) {
        this.enabled = Boolean.parseBoolean(cacheEnabled);
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
