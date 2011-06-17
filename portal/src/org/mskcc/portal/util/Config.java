package org.mskcc.portal.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static Config config = null;
    private Properties properties;

    private Config() {
        try {
            InputStream in = this.getClass().getResourceAsStream("build.properties");
            properties = new Properties();
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
}
