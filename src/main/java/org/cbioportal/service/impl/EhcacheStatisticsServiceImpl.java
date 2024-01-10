package org.cbioportal.service.impl;

import org.cbioportal.persistence.util.EhcacheStatistics;
import org.cbioportal.utils.config.annotation.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Primary // if there is another CacheStatisticsService bean this one should be the one selected
@Service
@ConditionalOnProperty(name = "persistence.cache_type", havingValue = {"ehcache-heap", "ehcache-disk", "ehcache-hybrid"})
public class EhcacheStatisticsServiceImpl extends CacheStatisticsServiceImpl {

    @Autowired
    public EhcacheStatistics ehcacheStatistics;

    @Override
    public String getCacheStatistics() {
        super.checkIfCacheStatisticsEndpointEnabled();
        return ehcacheStatistics.getCacheStatistics();
    }
}
