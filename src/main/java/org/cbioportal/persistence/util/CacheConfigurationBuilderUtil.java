package org.cbioportal.persistence.util;

// Importing required Ehcache classes and configuration builders.
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;

// Utility class to help create Ehcache configurations.
public class CacheConfigurationBuilderUtil {

    /**
     * Creates a cache configuration using the specified resource pools.
     *
     * @param templateName The name of the template (not currently used in this method but could be useful for future extensions).
     * @param resourcePoolsBuilder The builder that defines how resources (heap, disk) are allocated for the cache.
     * @return A `CacheConfiguration` instance for the specified resource pools.
     */
    public static CacheConfiguration<Object, Object> createCacheConfiguration(
        String templateName,
        ResourcePoolsBuilder resourcePoolsBuilder
    ) {
        // Builds a cache configuration with Object types for keys and values, using the provided resource pools.
        return CacheConfigurationBuilder.newCacheConfigurationBuilder(
            Object.class, // The type of keys stored in the cache.
            Object.class, // The type of values stored in the cache.
            resourcePoolsBuilder // Resource allocation settings for the cache.
        ).build();
    }

    /**
     * Creates a resource pool builder based on heap and/or disk configuration.
     *
     * @param useHeap Whether to allocate memory on the heap for caching.
     * @param useDisk Whether to allocate disk space for caching.
     * @param heapSize The size of the heap memory (in MB) to allocate if `useHeap` is true.
     * @param diskSize The size of the disk memory (in MB) to allocate if `useDisk` is true.
     * @return A `ResourcePoolsBuilder` instance with the specified resource pool settings.
     */
    public static ResourcePoolsBuilder createResourcePools(
        boolean useHeap, // Indicates if heap memory should be used for caching.
        boolean useDisk, // Indicates if disk space should be used for caching.
        int heapSize,    // Amount of heap memory (in MB) to allocate.
        int diskSize     // Amount of disk memory (in MB) to allocate.
    ) {
        // Initialize a new resource pool builder.
        ResourcePoolsBuilder resourcePoolsBuilder = ResourcePoolsBuilder.newResourcePoolsBuilder();
        
        // If heap caching is enabled, configure the builder with the specified heap size.
        if (useHeap) {
            resourcePoolsBuilder = resourcePoolsBuilder.heap(heapSize, MemoryUnit.MB);
        }
        
        // If disk caching is enabled, configure the builder with the specified disk size.
        if (useDisk) {
            resourcePoolsBuilder = resourcePoolsBuilder.disk(diskSize, MemoryUnit.MB);
        }
        
        // Return the fully configured resource pool builder.
        return resourcePoolsBuilder;
    }
}
