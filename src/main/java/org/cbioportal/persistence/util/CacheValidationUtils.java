package org.cbioportal.persistence.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Utility class for validating cache configurations.
public class CacheValidationUtils {

    // Logger for error messages and debug information.
    private static final Logger LOG = LoggerFactory.getLogger(CacheValidationUtils.class);

    /**
     * Validates the cache type to ensure it is a recognized value.
     *
     * @param cacheType The type of cache (e.g., "no-cache", "ehcache-heap").
     * @param messages  A buffer to collect validation error messages.
     */
    public static void validateCacheType(String cacheType, StringBuffer messages) {
        switch (cacheType.trim().toLowerCase()) {
            // Recognized cache types
            case "no-cache":
            case "ehcache-heap":
            case "ehcache-disk":
            case "ehcache-hybrid":
            case "redis":
                break; // Valid case
            default:
                // Append an error message for unrecognized cache types.
                messages.append("\n  property persistence.cache_type has value (")
                        .append(cacheType)
                        .append(") which is not a recognized value");
        }
    }

    /**
     * Validates the XML configuration file for Ehcache.
     *
     * @param xmlConfigurationFile The path to the XML configuration file.
     * @param messages             A buffer to collect validation error messages.
     */
    public static void validateXmlConfiguration(String xmlConfigurationFile, StringBuffer messages) {
        if (xmlConfigurationFile == null || xmlConfigurationFile.trim().isEmpty()) {
            // The XML configuration file path is required but unset.
            messages.append("\n  property ehcache.xml_configuration is required but is unset.");
        } else {
            // Attempt to load the XML configuration file as a resource.
            URL configFileURL = CacheValidationUtils.class.getResource(xmlConfigurationFile);
            if (configFileURL == null) {
                // Resource could not be found by the class loader.
                messages.append("\n  property ehcache.xml_configuration has value (")
                        .append(xmlConfigurationFile)
                        .append(") but this resource is not available to the classloader.");
            } else {
                try (InputStream configFileInputStream = configFileURL.openStream()) {
                    // Check if the file is readable by attempting to read from it.
                    configFileInputStream.read();
                } catch (IOException e) {
                    // Unable to read the XML configuration file.
                    messages.append("\n  property ehcache.xml_configuration has value (")
                            .append(xmlConfigurationFile)
                            .append(") but an attempt to read from this resource failed.");
                }
            }
        }
    }

    /**
     * Validates the disk-based configuration for Ehcache.
     *
     * @param diskSize        The size of the disk cache in MB.
     * @param persistencePath The directory path for storing disk-based cache data.
     * @param messages        A buffer to collect validation error messages.
     */
    public static void validateDiskConfiguration(Integer diskSize, String persistencePath, StringBuffer messages) {
        if (diskSize == null || diskSize <= 0) {
            // Disk size must be a positive integer.
            messages.append("\n  property disk size must be greater than zero but is not.");
        }
        if (persistencePath == null || persistencePath.trim().isEmpty()) {
            // Persistence path is required for disk-based caches but is unset.
            messages.append("\n  property ehcache.persistence_path is required but is unset.");
        } else {
            // Validate that the persistence path is a writable directory.
            File persistenceDirectory = new File(persistencePath);
            if (!persistenceDirectory.isDirectory() || !persistenceDirectory.canWrite()) {
                // The directory is either not valid or not writable.
                messages.append("\n  property ehcache.persistence_path is not a valid writable directory.");
            }
        }
    }

    /**
     * Validates the heap-based configuration for Ehcache.
     *
     * @param heapSize The size of the heap cache in MB.
     * @param messages A buffer to collect validation error messages.
     */
    public static void validateHeapConfiguration(Integer heapSize, StringBuffer messages) {
        if (heapSize == null || heapSize <= 0) {
            // Heap size must be a positive integer.
            messages.append("\n  property heap size must be greater than zero but is not.");
        }
    }
}
