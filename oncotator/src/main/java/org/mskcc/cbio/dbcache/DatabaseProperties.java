package org.mskcc.cbio.dbcache;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Stores db props (name, id, pw, host) and makes them accessible.
 */
public class DatabaseProperties
{
    private String dbHost;
    private String dbUser;
    private String dbPassword;
    private String dbName;

    public DatabaseProperties(String configFilename)
    {
    	try
    	{
            File configFile = new File(configFilename);
            InputStream in = (configFile.exists()) ? new FileInputStream(configFile) : this.getClass().getClassLoader().getResourceAsStream(configFilename);
            if (in == null) {
                System.err.println( "Properties file '" + configFilename + "' could not be found by getResourceAsStream(). Check the CLASSPATH or class loader.\n" +
                                    "See http://download.oracle.com/javase/1.5.0/docs/api/java/lang/Class.html#getResourceAsStream%28java.lang.String%29 re proper location of properties file.");
                System.exit(1);
            }
        	Properties props = new Properties();
			props.load(in);

			this.setDbHost(props.getProperty("db.host"));
	    	this.setDbName(props.getProperty("db.name"));
	    	this.setDbUser(props.getProperty("db.user"));
	    	this.setDbPassword(props.getProperty("db.password"));
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
}
