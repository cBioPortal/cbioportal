package org.cbioportal.legacy.web.config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import java.io.*;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.ObjectUtils;

/** Created by Hongxin Zhang on 11/15/19. */
public class WebServletContextListener implements ServletContextListener, InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(WebServletContextListener.class);

  private Boolean showOncokb;

  private String oncokbToken;

  private String oncokbURL;

  private Properties properties;

  private static final String DEFAULT_ONCOKB_URL = "https://public.api.oncokb.org/api/v1";

  @Override
  public void contextDestroyed(ServletContextEvent arg0) {
    log.info("ServletContextListener destroyed");
  }

  // Run this before web application is started
  @Override
  public void contextInitialized(ServletContextEvent arg0) {
    this.properties = loadProperties(getResourceStream("application.properties"));

    this.showOncokb = Boolean.parseBoolean(getProperty("show.oncokb", "true"));
    this.oncokbToken = getProperty("oncokb.token", "");
    this.oncokbURL = getProperty("oncokb.public_api.url", "");

    if (this.showOncokb) {
      checkOncokbInfo();
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {}

  private void checkOncokbInfo() {
    if (ObjectUtils.isEmpty(this.oncokbToken)
        && (ObjectUtils.isEmpty(this.oncokbURL)
            || this.oncokbURL.equalsIgnoreCase(DEFAULT_ONCOKB_URL))) {
      log.warn(
          "----------------------------------------------------------------------------------------------------------------");
      // oncokb.org is deprecated, www.oncokb.org should be used
      log.warn(
          "-- You are connecting to the OncoKB public instance which does not include any therapeutic information.");
      log.warn(
          "-- Please consider obtaining a license to support future OncoKB development by following https://docs.cbioportal.org/2.4-integration-with-other-webservices/oncokb-data-access.");
      log.warn("-- Thank you.");
      log.warn(
          "----------------------------------------------------------------------------------------------------------------");
    }
  }

  private String getProperty(String key, String defaultValue) {
    String propertyValue = this.properties.getProperty(key, defaultValue);
    return System.getProperty(key, propertyValue);
  }

  private InputStream getResourceStream(String propertiesFileName) {
    String resourceFilename = null;
    InputStream resourceFIS = null;

    try {
      String home = System.getenv("PORTAL_HOME");
      if (home != null) {
        resourceFilename = home + File.separator + propertiesFileName;
        resourceFIS = new FileInputStream(resourceFilename);
      }
    } catch (FileNotFoundException e) {
    }

    if (resourceFIS == null) {
      resourceFIS = this.getClass().getClassLoader().getResourceAsStream(propertiesFileName);
    }

    return resourceFIS;
  }

  private Properties loadProperties(InputStream resourceInputStream) {
    Properties properties = new Properties();

    try {
      properties.load(resourceInputStream);
      resourceInputStream.close();
    } catch (IOException e) {
      log.error("Error loading properties file: {}", e.getMessage(), e);
    }

    return properties;
  }
}
