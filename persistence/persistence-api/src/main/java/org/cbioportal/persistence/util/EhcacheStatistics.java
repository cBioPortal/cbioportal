/*
 * Copyright (c) 2019 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.cbioportal.persistence.util;

import org.ehcache.core.statistics.*;
import org.ehcache.config.ResourceType;
import org.ehcache.impl.internal.statistics.DefaultStatisticsService;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;
import javax.annotation.PostConstruct;

@Component
@Profile({"ehcache-heap", "ehcache-disk", "ehcache-hybrid"})
public class EhcacheStatistics {

    private static String TIER_NOT_IN_USE = "Tier not in use";

    private static double BYTES_IN_MB = 1048576.0;
    private static double BYTES_IN_GB = 1073741824.0;

    private javax.cache.CacheManager cacheManager;
    private DefaultStatisticsService statisticsService;

    public EhcacheStatistics(javax.cache.CacheManager cacheManager) {
        if (cacheManager == null) {
            throw new RuntimeException("A CacheManager needs to be set before calling this method.");
        }
        this.cacheManager = cacheManager;
    }

    @PostConstruct
    public void initializeStatisticsService () {
        try {
            statisticsService = new DefaultStatisticsService();
            for (String cacheName : cacheManager.getCacheNames()) {
                javax.cache.Cache cache = cacheManager.getCache(cacheName);
                org.ehcache.Cache ehcache = (org.ehcache.Cache)cache.unwrap(org.ehcache.Cache.class);
                statisticsService.cacheAdded(cacheName, ehcache);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getCacheStatistics() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n\nCACHE_STATISTICS START\n\n");
        for (String cacheName : cacheManager.getCacheNames()) {
            builder.append("Cache: " + cacheName + "\n");
            builder.append("Allocated (heap): " + getAllocatedBytes(cacheName, ResourceType.Core.HEAP) + "\n");
            builder.append("Occupied (heap): " + getOccupiedBytes(cacheName, "OnHeap", ResourceType.Core.HEAP) + "\n");
            builder.append("Allocated (disk): " + getAllocatedBytes(cacheName, ResourceType.Core.DISK) + "\n");
            builder.append("Occupied (disk): " + getOccupiedBytes(cacheName, "Disk", ResourceType.Core.DISK) + "\n");
            builder.append("\n");
        }
        builder.append("CACHE_STATISTICS END\n");
        return builder.toString();
    }

    private String getAllocatedBytes(String cacheName, ResourceType.Core resourceType)
    {
        try {
            org.ehcache.Cache ehcache = getEhcache(cacheName);
            return (getAllocatedBytes(ehcache, resourceType) + getAllocatedUnit(ehcache, resourceType));
        }
        catch (NullPointerException e) {
            return TIER_NOT_IN_USE;
        }
    }

    private String getOccupiedBytes(String cacheName, String tier, ResourceType.Core resourceType)
    {
        try {
            CacheStatistics cacheStatistics = statisticsService.getCacheStatistics(cacheName);
            Map<String, TierStatistics> tierStatistics = cacheStatistics.getTierStatistics();
            long occupiedBytes = tierStatistics.get(tier).getOccupiedByteSize();
            return scaleOccupiedBytes(cacheName, occupiedBytes, resourceType);
        }
        catch (NullPointerException e) {
            return TIER_NOT_IN_USE;
        }
    }

    private org.ehcache.Cache getEhcache(String cacheName)
    {
        javax.cache.Cache cache = cacheManager.getCache(cacheName);
        return (org.ehcache.Cache)cache.unwrap(org.ehcache.Cache.class);
    }

    private String getAllocatedBytes(org.ehcache.Cache ehcache, ResourceType.Core resourceType) throws NullPointerException
    {
        return String.valueOf(ehcache.getRuntimeConfiguration().getResourcePools().getPoolForResource(resourceType).getSize());
    }

    private String getAllocatedUnit(org.ehcache.Cache ehcache, ResourceType.Core resourceType) throws NullPointerException
    {
        return ehcache.getRuntimeConfiguration().getResourcePools().getPoolForResource(resourceType).getUnit().toString();
    }

    private String scaleOccupiedBytes(String cacheName, long occupiedBytes, ResourceType.Core resourceType) throws NullPointerException
    {
        org.ehcache.Cache ehcache = getEhcache(cacheName);
        String allocatedUnit = getAllocatedUnit(ehcache, resourceType);
        if (allocatedUnit.equals("MB")) {
            return String.valueOf(String.format("%.01f", occupiedBytes / BYTES_IN_MB) + "MB (" + occupiedBytes + " bytes)");
        }
        else if (allocatedUnit.equals("GB")) {
            return String.valueOf(String.format("%.01f", occupiedBytes / BYTES_IN_GB) + "GB (" + occupiedBytes + " bytes)");
        }
        else {
            return (String.valueOf(occupiedBytes) + " bytes");
        }

    }
}
