package org.cbioportal.legacy.service;

import java.util.*;
import org.cbioportal.legacy.service.exception.CacheNotFoundException;

public interface CacheStatisticsService {

    List<String> getKeyCountsPerClass(String cacheName) throws CacheNotFoundException;
    
    List<String> getKeysInCache(String cacheName) throws CacheNotFoundException;
    
    String getCacheStatistics();
}
