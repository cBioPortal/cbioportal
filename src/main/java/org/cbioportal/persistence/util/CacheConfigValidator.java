package org.cbioportal.persistence.util;

import java.io.File;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Utility class to validate Ehcache configuration settings.
public class CacheConfigValidator {

    // Logger instance for logging errors and messages.
    private static final Logger LOG = LoggerFactory.getLogger(CacheConfigValidator.class);

    /**
     * Validates the provided Ehcache configuration settings.
     *
     * @param cacheType         The type of cache (e.g., "ehcache-disk").
     * @param xmlConfigurationFile Path to the Ehcache XML configuration file.
     * @param heapSize          The size of the heap memory (in MB) to allocate.
     * @param diskSize          The size of the disk memory (in MB) to allocate.
     * @param persistencePath   The directory path for storing disk-based cache data.
     * @throws IllegalArgumentException if validation fails.
     */
    public static void validate(String cacheType, String xmlConfigurationFile, Integer heapSize, Integer diskSize, String persistencePath) {
        // Use a StringBuilder to collect error messages for logging and exception handling.
        StringBuilder errors = new StringBuilder("Errors detected during configuration of Ehcache:");
        boolean valid = true;

        // **Validation of XML Configuration File**
        if (xmlConfigurationFile == null || xmlConfigurationFile.trim().isEmpty()) {
            // Check if the XML configuration file path is provided.
            errors.append("\n  - The property 'ehcache.xml_configuration' is required but is unset.");
            valid = false;
        } else {
            // Check if the XML configuration file exists and is accessible.
            URL configFileURL = CacheConfigValidator.class.getResource(xmlConfigurationFile);
            if (configFileURL == null) {
                errors.append("\n  - The property 'ehcache.xml_configuration' points to an unavailable resource.");
                valid = false;
            }
        }

        // **Validation of Heap Size**
        if (heapSize != null && heapSize <= 0) {
            // Ensure the heap size is greater than zero.
            errors.append("\n  - Heap size must be greater than zero.");
            valid = false;
        }

        // **Validation of Disk Size**
        if (diskSize != null && diskSize <= 0) {
            // Ensure the disk size is greater than zero.
            errors.append("\n  - Disk size must be greater than zero.");
            valid = false;
        }

        // **Validation of Persistence Path**
        if (cacheType.equalsIgnoreCase("ehcache-disk") && (persistencePath == null || persistencePath.trim().isEmpty())) {
            // Disk-based caches require a persistence path.
            errors.append("\n  - The property 'ehcache.persistence_path' is required for disk-based caches but is unset.");
            valid = false;
        } else if (persistencePath != null) {
            // If a persistence path is provided, validate it as a writable directory.
            File persistenceDirectory = new File(persistencePath);
            if (!persistenceDirectory.isDirectory() || !persistenceDirectory.canWrite()) {
                errors.append("\n  - The persistence path is not a valid directory or is not writable.");
                valid = false;
            }
        }

        // **Error Handling**
        if (!valid) {
            // Log all errors and throw an exception if validation fails.
            LOG.error(errors.toString());
            throw new IllegalArgumentException(errors.toString());
        }
    }
}
