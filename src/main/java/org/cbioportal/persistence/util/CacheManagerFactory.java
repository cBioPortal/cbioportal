package org.cbioportal.persistence.util;

import java.io.File;
import java.util.Map;

import org.ehcache.config.Configuration;
import org.ehcache.core.config.DefaultConfiguration;
import org.ehcache.impl.config.persistence.DefaultPersistenceConfiguration;
import org.ehcache.jsr107.EhcacheCachingProvider;

// Utility class for creating Ehcache CacheManager instances.
public class CacheManagerFactory {

    /**
     * Creates and configures a CacheManager instance based on the provided cache type and configuration.
     *
     * @param cacheType       The type of cache to create (e.g., "ehcache-heap" or disk-based cache).
     * @param caches          A map of cache names and their corresponding configurations.
     * @param persistencePath The directory path for storing disk-based cache data (used for persistent caches).
     * @return A configured CacheManager instance for managing cache operations.
     */
    public static javax.cache.CacheManager createCacheManager(
        String cacheType,
        Map<String, org.ehcache.config.CacheConfiguration<?, ?>> caches,
        String persistencePath
    ) {
        // Declare a variable to hold the configuration object.
        Configuration configuration;

        // **Heap-based Cache Configuration**
        if (cacheType.equalsIgnoreCase("ehcache-heap")) {
            // Use the DefaultConfiguration for heap-based caching, with the class loader and provided cache configurations.
            configuration = new DefaultConfiguration(caches, CacheManagerFactory.class.getClassLoader());
        } else {
            // **Disk-based Cache Configuration**
            // Use DefaultConfiguration with a persistence configuration to store cache data on disk.
            configuration = new DefaultConfiguration(
                caches, // Map of cache configurations
                CacheManagerFactory.class.getClassLoader(), // Class loader to load configurations
                new DefaultPersistenceConfiguration(new File(persistencePath)) // Disk persistence settings
            );
        }

        // **Obtain an Ehcache Caching Provider**
        // Use the EhcacheCachingProvider, which implements the JSR-107 caching API.
        EhcacheCachingProvider cachingProvider = (EhcacheCachingProvider) javax.cache.Caching.getCachingProvider();

        // **Create and Return the CacheManager**
        // The CacheManager is initialized with the default URI and the custom configuration.
        return cachingProvider.getCacheManager(
            cachingProvider.getDefaultURI(), // Default URI for Ehcache
            configuration // Custom configuration (heap or disk)
        );
    }
}
