/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
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

package org.mskcc.cbio.dbcache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.Properties;

/**
 * Stores db props (name, id, pw, host) and makes them accessible.
 */
public class DatabaseProperties
{
    private static final String HOME_DIR = "PORTAL_HOME";
    private static final String PORTAL_PROPERTIES_FILENAME = "portal.properties";
    private static Log LOG = LogFactory.getLog(DatabaseProperties.class);

    private String dbHost;
    private String dbUser;
    private String dbPassword;
    private String dbName;

    public DatabaseProperties(String configFilename)
    {
    	try
    	{
            InputStream in = getResourcesStream(configFilename);
            if (in == null) {
	            throw new RuntimeException("Properties file '" + configFilename + "' could not be found.");
            }
        	Properties props = new Properties();
			props.load(in);

			this.setDbHost(props.getProperty("annotate.db.host"));
	    	this.setDbName(props.getProperty("annotate.db.name"));
	    	this.setDbUser(props.getProperty("annotate.db.user"));
	    	this.setDbPassword(props.getProperty("annotate.db.password"));
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    public String getDbHost() {
        return dbHost;
    }

    public void setDbHost(String dbHost) {
        this.dbHost = dbHost;
    }

    public String getDbUser() {
        return dbUser;
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    private InputStream getResourcesStream(String configFilename)
    {
        String resourceFilename = null;
        InputStream resourceFIS = null;

        try {
            logMessage("Attempting to read properties file: " + configFilename);
            resourceFIS = new FileInputStream(configFilename);
        }
        catch (FileNotFoundException e) {
            logMessage("Failed to read properties file: " + configFilename);
        }

        if (resourceFIS == null) {
            try {
                String home = System.getenv(HOME_DIR);
                if (home != null) {
                    resourceFilename = home + File.separator + PORTAL_PROPERTIES_FILENAME;
                    logMessage("Attempting to read properties file: " + resourceFilename);
                    resourceFIS = new FileInputStream(resourceFilename);
                    logMessage("Successfully read properties file");
                }
            }
            catch (FileNotFoundException e) {
                logMessage("Failed to read properties file: " + resourceFilename);
                logMessage("Attempting to read properties file from classpath: " + PORTAL_PROPERTIES_FILENAME);
                resourceFIS = this.getClass().getClassLoader().getResourceAsStream(PORTAL_PROPERTIES_FILENAME);
            }
        }

        return resourceFIS;
    }

    private static void logMessage(String message)
    {
        if (LOG.isInfoEnabled()) {
            LOG.info(message);
        }
        System.err.println(message);
    }
}
