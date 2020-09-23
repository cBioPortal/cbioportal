package org.cbioportal.service.impl;

import org.cbioportal.persistence.util.EhcacheStatistics;

import java.lang.String;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Primary // if there is another CacheStatisticsService bean this one should be the one selected
@Service
@Profile({"ehcache-heap", "ehcache-disk", "ehcache-hybrid"})
public class EhcacheStatisticsServiceImpl extends CacheStatisticsServiceImpl {

    @Autowired
    public EhcacheStatistics ehcacheStatistics;

    @Override
    public String getCacheStatistics() {
        super.checkIfCacheStatisticsEndpointEnabled();
        return ehcacheStatistics.getCacheStatistics();
    }
}
