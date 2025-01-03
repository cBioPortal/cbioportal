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

import java.util.Map;

import javax.cache.CacheManager;

import org.cbioportal.persistence.CacheEnabledConfig;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.jsr107.EhcacheCachingProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

// Extends EhcacheCachingProvider to add custom cache configuration and validation logic.
public class CustomEhcachingProvider extends EhcacheCachingProvider {

    // Logger for reporting errors and debugging information.
    private static final Logger LOG = LoggerFactory.getLogger(CustomEhcachingProvider.class);

    // Properties configured through application settings (e.g., application.properties or environment variables).
    @Value("${ehcache.xml_configuration:/ehcache.xml}") // Path to the Ehcache XML configuration file.
    private String xmlConfigurationFile;

    @Value("${persistence.cache_type:no-cache}") // Cache type (e.g., "ehcache-disk", "no-cache").
    private String cacheType;

    @Value("${ehcache.general_repository_cache.max_mega_bytes_heap:1024}") // Max heap size for the general cache in MB.
    private Integer generalRepositoryCacheMaxMegaBytes;

    @Value("${ehcache.static_repository_cache_one.max_mega_bytes_heap:30}") // Max heap size for the static cache in MB.
    private Integer staticRepositoryCacheOneMaxMegaBytes;

    @Value("${ehcache.persistence_path:/tmp/}") // Path for disk-based persistence.
    private String persistencePath;

    @Value("${ehcache.general_repository_cache.max_mega_bytes_local_disk:4096}") // Max disk size for the general cache in MB.
    private Integer generalRepositoryCacheMaxMegaBytesLocalDisk;

    @Value("${ehcache.static_repository_cache_one.max_mega_bytes_local_disk:32}") // Max disk size for the static cache in MB.
    private Integer staticRepositoryCacheOneMaxMegaBytesLocalDisk;

    @Autowired
    private CacheEnabledConfig cacheEnabledConfig; // Configuration to enable/disable caching.

    /**
     * Overrides the default `getCacheManager` method to initialize and return a customized CacheManager.
     *
     * @return CacheManager A cache manager based on the specified cache type and configurations.
     */
    @Override
    public CacheManager getCacheManager() {
        try {
            // Validate the cache configuration before initializing the CacheManager.
            CacheConfigValidator.validate(
                cacheType,
                xmlConfigurationFile,
                generalRepositoryCacheMaxMegaBytes,
                generalRepositoryCacheMaxMegaBytesLocalDisk,
                persistencePath
            );

            // Create resource pools for the general repository cache based on the cache type (heap or disk).
            ResourcePoolsBuilder generalRepositoryPools = CacheConfigurationBuilderUtil.createResourcePools(
                !cacheType.equalsIgnoreCase("ehcache-disk"), // Enable heap if cache type is not disk-based.
                !cacheType.equalsIgnoreCase("ehcache-heap"), // Enable disk if cache type is not heap-based.
                generalRepositoryCacheMaxMegaBytes, // Heap size in MB.
                generalRepositoryCacheMaxMegaBytesLocalDisk // Disk size in MB.
            );

            // Build the general repository cache configuration using the resource pools.
            CacheConfiguration<Object, Object> generalRepoCache = CacheConfigurationBuilderUtil.createCacheConfiguration(
                "RepositoryCacheTemplate",
                generalRepositoryPools
            );

            // Define the caches to be managed, in this case, a single cache named "GeneralRepositoryCache".
            Map<String, CacheConfiguration<?, ?>> caches = Map.of("GeneralRepositoryCache", generalRepoCache);

            // Create and return the CacheManager using the specified type and configurations.
            return CacheManagerFactory.createCacheManager(cacheType, caches, persistencePath);

        } catch (Exception e) {
            // Log and rethrow any exceptions encountered during cache manager initialization.
            LOG.error("Error initializing CacheManager: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Detects and logs errors in the cache configuration without throwing exceptions.
     * Useful for pre-validation during application startup.
     */
    public void detectCacheConfigurationErrorsAndLog() {
        // Prefix for error messages.
        String MESSAGE_PREFIX = "Errors detected during configuration of Ehcache:";
        StringBuffer messages = new StringBuffer(MESSAGE_PREFIX);

        // Validate the cache type (e.g., "ehcache-heap", "ehcache-disk").
        CacheValidationUtils.validateCacheType(this.cacheType, messages);

        // Perform additional validations based on the cache type.
        if (cacheType.equalsIgnoreCase("ehcache-heap") || cacheType.equalsIgnoreCase("ehcache-disk") || cacheType.equalsIgnoreCase("ehcache-hybrid")) {
            // Validate the Ehcache XML configuration file.
            CacheValidationUtils.validateXmlConfiguration(xmlConfigurationFile, messages);

            // Validate disk-based configuration if the cache type supports disk.
            if (cacheType.equalsIgnoreCase("ehcache-disk") || cacheType.equalsIgnoreCase("ehcache-hybrid")) {
                CacheValidationUtils.validateDiskConfiguration(generalRepositoryCacheMaxMegaBytesLocalDisk, persistencePath, messages);
            }

            // Validate heap-based configuration if the cache type supports heap.
            if (cacheType.equalsIgnoreCase("ehcache-heap") || cacheType.equalsIgnoreCase("ehcache-hybrid")) {
                CacheValidationUtils.validateHeapConfiguration(generalRepositoryCacheMaxMegaBytes, messages);
            }
        }

        // Log the validation errors if any issues are detected.
        if (messages.length() > MESSAGE_PREFIX.length()) {
            LOG.error(messages.toString());
        }
    }
}
