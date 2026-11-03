/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.util;

/**
 * Stores db props (name, id, pw, host) from application.properties
 * and makes them accessible.
 */
public class DatabaseProperties {
    private String dbHost;
    private String dbUser;
    private String dbPassword;
    private String dbName;
    private String dbEncryptedKey;
    private String dbDriverClassName;
    private String dbUseSSL;
    private String dbEnablePooling;
    private String connectionURL;
    private String springDbUser;
    private String springDbPassword;
    private String springConnectionURL;
    private String springDbDriverClassName;

    // No production keys stored in filesystem or code: digest the key; put it in properties; load it into dbms on startup
    private static DatabaseProperties dbProperties;

    public static DatabaseProperties getInstance() {
        if (dbProperties == null) {
            dbProperties = new DatabaseProperties();
            //  Get DB Properties from application.properties.
            dbProperties.setDbHost(GlobalProperties.getProperty("db.host"));
            dbProperties.setDbName(GlobalProperties.getProperty("db.portal_db_name"));
            dbProperties.setDbUser(GlobalProperties.getProperty("db.user"));
            dbProperties.setDbPassword(GlobalProperties.getProperty("db.password"));
            dbProperties.setDbEncryptedKey(GlobalProperties.getProperty("db.encryptedKey"));
            dbProperties.setDbDriverClassName(GlobalProperties.getProperty("db.driver"));
            dbProperties.setDbUseSSL(GlobalProperties.getProperty("db.use_ssl"));
            dbProperties.setDbEnablePooling(GlobalProperties.getProperty("db.enable_pooling"));
            dbProperties.setConnectionURL(GlobalProperties.getProperty("db.connection_string"));
            dbProperties.setSpringDbUser(GlobalProperties.getProperty("spring.datasource.username"));
            dbProperties.setSpringDbPassword(GlobalProperties.getProperty("spring.datasource.password"));
            dbProperties.setSpringConnectionURL(GlobalProperties.getProperty("spring.datasource.url"));
            dbProperties.setSpringDbDriverClassName(GlobalProperties.getProperty("spring.datasource.driver-class-name"));

        }
        return dbProperties;
    }

    public String getDbEncryptedKey() {
      return dbEncryptedKey;
   }

   public void setDbEncryptedKey(String dbEncryptedKey) {
      this.dbEncryptedKey = dbEncryptedKey;
   }

   private DatabaseProperties() {
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

    public String getDbDriverClassName() {
        return dbDriverClassName;
    }

    public void setDbDriverClassName(String dbDriverClassName) {
        this.dbDriverClassName = dbDriverClassName;
    }
    
    public String getDbUseSSL() {
        return dbUseSSL;
    }

    public void setDbUseSSL(String dbUseSSL) {
        this.dbUseSSL = dbUseSSL;
    }

    public String getDbEnablePooling() {
        return dbEnablePooling;
    }

    public void setDbEnablePooling(String dbEnablePooling) {
        this.dbEnablePooling = dbEnablePooling;
    }

    public String getConnectionURL() {
        return connectionURL;
    }

    public void setConnectionURL(String connectionURL) {
        this.connectionURL = connectionURL;
    }

    public String getSpringConnectionURL() {
        return springConnectionURL;
    }

    public void setSpringConnectionURL(String springConnectionURL) {
        this.springConnectionURL = springConnectionURL;
    }

    public String getSpringDbUser() {
        return springDbUser;
    }

    public void setSpringDbUser(String springDbUser) {
        this.springDbUser = springDbUser;
    }

    public String getSpringDbPassword() {
        return springDbPassword;
    }

    public void setSpringDbPassword(String springDbPassword) {
        this.springDbPassword = springDbPassword;
    }

    public String getSpringDbDriverClassName() {
        return springDbDriverClassName;
    }

    public void setSpringDbDriverClassName(String springDbDriverClassName) {
        this.springDbDriverClassName = springDbDriverClassName;
    }
    
}
