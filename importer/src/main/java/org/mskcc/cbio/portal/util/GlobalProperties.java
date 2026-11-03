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
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for getting / setting global properties.
 */
public class GlobalProperties {
    public static final String HOME_DIR = "PORTAL_HOME";
    private static final String PORTAL_PROPERTIES_FILE_NAME = "application.properties";
    private static final String POM_RESOURCE_PATH = "META-INF/maven/org.mskcc.cbio/core/pom.xml";
    private static final String DEFAULT_DB_VERSION = "0";
    private static final Pattern DB_VERSION_PATTERN = Pattern.compile("<db\\.version>\\s*([^<]+)\\s*</db\\.version>");
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

    private static InputStream getPomResourceStream() {
        InputStream pomStream = GlobalProperties.class.getClassLoader().getResourceAsStream(POM_RESOURCE_PATH);
        if (pomStream != null) {
            return pomStream;
        }
        return null;
    }

    private static String readDbVersionFromPom() {
        try (InputStream pomStream = getPomResourceStream()) {
            if (pomStream == null) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Unable to locate pom.xml for db.version lookup. " +
                            "Checked classpath resource " + POM_RESOURCE_PATH);
                }
                return null;
            }
            String pomContents = new String(pomStream.readAllBytes(), StandardCharsets.UTF_8);
            Matcher matcher = DB_VERSION_PATTERN.matcher(pomContents);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
            if (LOG.isWarnEnabled()) {
                LOG.warn("db.version was not found in pom.xml.");
            }
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Error reading pom.xml for db.version: " + e.getMessage());
            }
        }
        return null;
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
                    String version = readDbVersionFromPom();
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
