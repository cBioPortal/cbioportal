package org.mskcc.cbio.importer.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.log4j.Logger;
import org.mskcc.cbio.portal.util.GlobalProperties;

/** Copyright (c) 2013 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center 
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center 
 * has been advised of the possibility of such damage.
*/

public class PropertiesLoader {

    private static final String HOME_DIR = "PORTAL_HOME";
    private static final String PORTAL_PROPERTIES_FILENAME = "importer.properties";
    private static Properties properties = new Properties();
    static final Logger logger = Logger.getLogger(PropertiesLoader.class);
    
    private static final String DMP_SERVER_NAME = "dmp.server_name";
    private static final String DMP_CBIO_USERNAME = "dmp.user_name";
    private static final String DMP_CBIO_PASSWORD = "dmp.password";
    private static final String DMP_CREATE_SESSION = "dmp.tokens.create_session";
    private static final String DMP_CBIO_RETRIEVE_VARIANTS = "dmp.tokens.retrieve_variants";
    private static final String DMP_CBIO_RETRIEVE_SEGMENT_DATA = "dmp.tokens.retrieve_segment_data";
    private static final String DMP_CBIO_CONSUME_SAMPLE = "dmp.tokens.consume_sample";

    private static final String IMPORTER_BASE_DIRECTORY = "importer.base.directory";
    
    private static PropertiesLoader instance = null;
   
    protected PropertiesLoader() {
        properties = loadProperties(getResourcesStream());
    }
    
    public static PropertiesLoader getInstance() {
        if(instance == null) {
            instance = new PropertiesLoader();
        }
        return instance;
    }
    
    private InputStream getResourcesStream() {
        
        String resourceFilename = null;
        InputStream resourceFIS = null;

        try {
            String home = System.getenv(HOME_DIR);
            if (home != null) {
                 resourceFilename =
                    home + File.separator + PORTAL_PROPERTIES_FILENAME;
                logger.info("Attempting to read properties file: " + resourceFilename);
                resourceFIS = new FileInputStream(resourceFilename);
                logger.info("Successfully read properties file");
            }
        }
        catch (FileNotFoundException e) {
            logger.info("Failed to find properties file: " + e.getMessage());
        }

        if (resourceFIS == null) {
            logger.info("Attempting to read properties file from classpath");
            resourceFIS = GlobalProperties.class.getClassLoader().
                getResourceAsStream(PORTAL_PROPERTIES_FILENAME);
            logger.info("Successfully read properties file");
        }
        
        return resourceFIS;
    }
    
    private static Properties loadProperties(InputStream resourceInputStream) {

        Properties _properties = new Properties();
        try {
            _properties.load(resourceInputStream);
            resourceInputStream.close();
        }
        catch (IOException e) {
            logger.error("Error loading properties file: " + e.getMessage());
        }

        return _properties;
    }
    
    public String getDmpServerName() {
        return properties.getProperty(DMP_SERVER_NAME);
    }

    public String getDmpUsername() {
        return properties.getProperty(DMP_CBIO_USERNAME);
    }

    public String getDmpPassword() {
        return properties.getProperty(DMP_CBIO_PASSWORD);
    }

    public String getDmpTokenCreateSession() {
        return properties.getProperty(DMP_CREATE_SESSION);
    }

    public String getDmpTokenRetrieveVariants() {
        return properties.getProperty(DMP_CBIO_RETRIEVE_VARIANTS);
    }

    public String getDmpTokenRetrieveSegmentData() {
        return properties.getProperty(DMP_CBIO_RETRIEVE_SEGMENT_DATA);
    }

    public String getDmpTokenConsumerSample() {
        return properties.getProperty(DMP_CBIO_CONSUME_SAMPLE);
    }

    public String getImporterBaseDirectory() { return properties.getProperty(IMPORTER_BASE_DIRECTORY);}

    public Path getImporterBasePath() {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(this.getImporterBaseDirectory()),
                "The base directory for staging files is undefined");
         return Paths.get(this.getImporterBaseDirectory());
    }



    public String getImporterPropertyByName(String propertyName){
        Preconditions.checkArgument(!Strings.isNullOrEmpty(propertyName),"A property name is required");
       Preconditions.checkArgument( properties.containsKey(propertyName),
               "Property " +propertyName +" is invalid");
        return properties.getProperty(propertyName);
    }

}