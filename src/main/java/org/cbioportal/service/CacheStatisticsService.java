package org.cbioportal.service;

import java.util.*;
import org.cbioportal.service.exception.CacheNotFoundException;

public interface CacheStatisticsService {

    List<String> getKeyCountsPerClass(String cacheName) throws CacheNotFoundException;
    
    List<String> getKeysInCache(String cacheName) throws CacheNotFoundException;
    
    String getCacheStatistics();
}
