package org.cbioportal.persistence.util;

import java.util.List;

public interface CacheUtils {
    List<String> getKeys(String cacheName);
    void evictByPattern(String cacheName, String pattern);
}
