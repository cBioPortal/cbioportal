package org.cbioportal.web.config;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.*;
import java.util.Properties;

/**
 * Created by Hongxin Zhang on 11/15/19.
 */
public class WebServletContextListener implements ServletContextListener, InitializingBean {
    private Boolean showOncokb;
    
    private String oncokbToken;
    
    private Properties properties;

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        System.out.println("ServletContextListener destroyed");
    }

    //Run this before web application is started
    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        this.properties = loadProperties(getResourceStream("portal.properties"));
        
        this.showOncokb = Boolean.parseBoolean(getProperty("show.oncokb", "true"));
        this.oncokbToken = getProperty("oncokb.token", "");

        if (this.showOncokb) {
            checkOncokbInfo();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    private void checkOncokbInfo() {
        if(StringUtils.isEmpty(this.oncokbToken)) {
            System.out.println("----------------------------------------------------------------------------------------------------------------");
            // oncokb.org is deprecated, www.oncokb.org should be used
            System.out.println("-- OncoKB requires a token to access the latest data.");
            System.out.println("-- You will not be able to use OncoKB service within your instance without an access token.");
            System.out.println("-- Please review OncoKB terms(https://www.oncokb.org/terms) and register an account accordingly.");
            System.out.println("-- Thank you.");
            System.out.println("----------------------------------------------------------------------------------------------------------------");
        }
    }
    
    private String getProperty(String key, String defaultValue) {
        String propertyValue = this.properties.getProperty(key, defaultValue);
        return System.getProperty(key, propertyValue);
    }

    private InputStream getResourceStream(String propertiesFileName)
    {
        String resourceFilename = null;
        InputStream resourceFIS = null;

        try {
            String home = System.getenv("PORTAL_HOME");
            if (home != null) {
                resourceFilename =
                    home + File.separator + propertiesFileName;
                resourceFIS = new FileInputStream(resourceFilename);
            }
        } catch (FileNotFoundException e) {
        }

        if (resourceFIS == null) {
            resourceFIS = this.getClass().getClassLoader().
                getResourceAsStream(propertiesFileName);
        }

        return resourceFIS;
    }
    private Properties loadProperties(InputStream resourceInputStream)
    {
        Properties properties = new Properties();

        try {
            properties.load(resourceInputStream);
            resourceInputStream.close();
        }
        catch (IOException e) {
            System.out.println("Error loading properties file: " + e.getMessage());
        }

        return properties;
    }
}
