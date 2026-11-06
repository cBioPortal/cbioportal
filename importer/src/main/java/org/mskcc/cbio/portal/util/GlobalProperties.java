/*
 * Copyright (c) 2015 - 2026 Memorial Sloan Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for getting / setting global properties.
 */
public class GlobalProperties {
    public static final String HOME_DIR = "PORTAL_HOME";
    private static final String PORTAL_PROPERTIES_FILE_NAME = "application.properties";
    // Written at build time from db.version in the root pom.xml (see importer/pom.xml)
    private static final String DB_VERSION_RESOURCE = "db-version.properties";
    private static final String DEFAULT_DB_VERSION = "0";
    public static final String DB_VERSION = "db.version";
    public static final String SPECIES = "species";
    public static final String DEFAULT_SPECIES = "human";
    public static final String UCSC_BUILD = "ucsc.build";
    public static final String DEFAULT_UCSC_BUILD = "hg19";
    private static Logger LOG = LoggerFactory.getLogger(GlobalProperties.class);
    private static ConfigPropertyResolver portalProperties = new ConfigPropertyResolver();
    private static volatile String cachedDbVersion;

    /**
     * Minimal portal property resolver that takes system property overrides.
     *
     * Provides properties from runtime or the baked-in
     * application.properties config file, but takes overrides from <code>-D</code>
     * system properties.
     */
    private static class ConfigPropertyResolver {
        private Properties configFileProperties;
        /**
         * Finds the config file for properties not overridden by system props.
         *
         * Either the runtime or buildtime application.properties file.
         */
        public ConfigPropertyResolver() {
            configFileProperties = initializeProperties(PORTAL_PROPERTIES_FILE_NAME);
        }
        /**
         * Finds the property with the specified key, or returns the default.
         */
        public String getProperty(String key, String defaultValue) {
            String propertyValue = configFileProperties.getProperty(key, defaultValue);
            return System.getProperty(key, propertyValue);
        }
        /**
         * Finds the property with the specified key, or returns null.
         */
        public String getProperty(String key) {
            return getProperty(key, null);
        }
        /**
        * Tests if a property has been specified for this key.
        *
        * @return true iff the property was specified, even if blank.
        */
        public boolean containsKey(String key) {
            return getProperty(key) != null;
        }
    }

    private static Properties initializeProperties(String propertiesFileName) {
        return loadProperties(getResourceStream(propertiesFileName));
    }

    private static InputStream getResourceStream(String propertiesFileName) {
        String resourceFilename = null;
        InputStream resourceFIS = null;
        try {
            String home = System.getenv(HOME_DIR);
            if (home != null) {
                 resourceFilename = home + File.separator + propertiesFileName;
                if (LOG.isInfoEnabled()) {
                    LOG.info("Attempting to read properties file: " + resourceFilename);
                }
                resourceFIS = new FileInputStream(resourceFilename);
                if (LOG.isInfoEnabled()) {
                    LOG.info("Successfully read properties file");
                }
            }
        } catch (FileNotFoundException e) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Failed to read properties file: " + resourceFilename);
            }
        }
        if (resourceFIS == null) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Attempting to read properties file from classpath");
            }
            resourceFIS = GlobalProperties.class.getClassLoader().getResourceAsStream(propertiesFileName);
            if (LOG.isInfoEnabled()) {
                LOG.info("Successfully read properties file");
            }
        }
        return resourceFIS;
    }

    private static Properties loadProperties(InputStream resourceInputStream) {
        Properties properties = new Properties();
        try {
            properties.load(resourceInputStream);
            resourceInputStream.close();
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Error loading properties file: " + e.getMessage());
            }
        }
        return properties;
    }

    /**
     * Reads the schema version this importer was built for. It used to be read from the importer's
     * own pom.xml inside the JAR, but since the importer became a module of the cbioportal build,
     * db.version is declared only in the root POM, which the JAR doesn't contain.
     */
    private static String readDbVersionFromBuild() {
        try (InputStream stream = GlobalProperties.class.getResourceAsStream(DB_VERSION_RESOURCE)) {
            if (stream == null) {
                LOG.error("{} not found on the classpath next to GlobalProperties", DB_VERSION_RESOURCE);
                return null;
            }
            Properties properties = new Properties();
            properties.load(stream);
            String version = properties.getProperty(DB_VERSION);
            if (version == null || version.isBlank() || version.startsWith("@")) {
                LOG.error("{} has no db.version filled in: {}", DB_VERSION_RESOURCE, version);
                return null;
            }
            return version.trim();
        } catch (IOException e) {
            LOG.error("Error reading {}: {}", DB_VERSION_RESOURCE, e.getMessage());
            return null;
        }
    }

    public static String getProperty(String property) {
        return (portalProperties.containsKey(property)) ? portalProperties.getProperty(property) : null;
    }

    public static String getSpecies() {
        String species = portalProperties.getProperty(SPECIES);
        return species == null ? DEFAULT_SPECIES : species;
    }

    public static String getDbVersion() {
        String override = System.getProperty(DB_VERSION);
        if (override != null) {
            return override;
        }
        if (cachedDbVersion == null) {
            synchronized (GlobalProperties.class) {
                if (cachedDbVersion == null) {
                    String version = readDbVersionFromBuild();
                    cachedDbVersion = version == null ? DEFAULT_DB_VERSION : version;
                }
            }
        }
        return cachedDbVersion;
    }

    public static String getReferenceGenomeName() {
        return portalProperties.getProperty(UCSC_BUILD, DEFAULT_UCSC_BUILD);
    }

    public static Integer parseIntegerProperty(String key, Integer defaultValue) {
        String valueString = getProperty(key);
        if (valueString == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(valueString);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static Integer parseIntegerProperty(String key) {
        return parseIntegerProperty(key, null);
    }

}
