package org.mskcc.cgds.util;

import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
    private static Config config = null;
    private Properties properties;

    private Config () {
        try {
           String props = "build.properties";
            InputStream in = this.getClass().getResourceAsStream( props );
            if( null == in ){
               System.err.println( "Properties file '" + props + "' could not be found by getResourceAsStream(). Check the CLASSPATH or class loader.\n" +
                     "See http://download.oracle.com/javase/1.5.0/docs/api/java/lang/Class.html#getResourceAsStream%28java.lang.String%29 re proper location of properties file.");
               System.exit(1);
            }
            properties = new Properties ();
            properties.load(in);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static Config getInstance() {
        if (config == null) {
            config = new Config();
        }
        return config;
    }

    /**
     * Determines if users must authenticate or not.
     * @return true or false.
     */
    public boolean usersMustAuthenticate() {
		return new Boolean(properties.getProperty("authenticate"));
    }
}
