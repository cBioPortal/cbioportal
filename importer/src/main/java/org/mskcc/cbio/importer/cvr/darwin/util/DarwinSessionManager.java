package org.mskcc.cbio.importer.cvr.darwin.util;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.log4j.Logger;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mskcc.cbio.importer.fetcher.internal.DmpDarwinFetcherImpl;
import org.mskcc.cbio.portal.util.GlobalProperties;

/**
 * Copyright (c) 2014 Memorial Sloan-Kettering Cancer Center.
 * <p/>
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
 * <p/>
 * Created by criscuof on 11/20/14.
 */
public enum DarwinSessionManager {
    

    /*
    A Singleton implemented as an enum to provide access to a Darwin SQL
    session object
     */
    INSTANCE;
    static final Logger logger = Logger.getLogger(DarwinSessionManager.class);
    private SqlSession session = Suppliers.memoize(new DarwinSessionSupplier()).get();
    
    private static final String HOME_DIR = "PORTAL_HOME";
    private static final String PORTAL_PROPERTIES_FILENAME = "importer.properties";
    private static final String MYBATIS_CONFIG_FILE = "darwin.mybatis_config_file";
    private static final Log LOG = LogFactory.getLog(DarwinSessionManager.class);
    
    public SqlSession getDarwinSession(){
        return this.session;
    }

    public void closeSession(){
        this.session.close();
        logger.info("The SQL session has been closed.");
    }

    private class DarwinSessionSupplier implements Supplier<SqlSession> {
        @Override
        public SqlSession get() {
            Properties properties = loadProperties(getResourcesStream());
            String configFileName = properties.getProperty(MYBATIS_CONFIG_FILE);
            InputStream inputStream =DarwinSessionSupplier.class.getResourceAsStream(configFileName);
            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
           return sqlSessionFactory.openSession();
        }
    }

    // main method for testing
    public static void main(String...args){
        SqlSession session = DarwinSessionManager.INSTANCE.getDarwinSession();
        try {
            logger.info("The session is open? " +!session.getConnection().isClosed());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        DarwinSessionManager.INSTANCE.closeSession();
    }
    
    private InputStream getResourcesStream() {
        String resourceFilename = null;
        InputStream resourceFIS = null;

        try {
            String home = System.getenv(HOME_DIR);
            if (home != null) {
                 resourceFilename =
                    home + File.separator + PORTAL_PROPERTIES_FILENAME;
                if (LOG.isInfoEnabled()) {
                    LOG.info("Attempting to read properties file: " + resourceFilename);
                }
                resourceFIS = new FileInputStream(resourceFilename);
                if (LOG.isInfoEnabled()) {
                    LOG.info("Successfully read properties file");
                }
            }
        }
        catch (FileNotFoundException e) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Failed to read properties file: " + resourceFilename);
            }
        }

        if (resourceFIS == null) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Attempting to read properties file from classpath");
            }
            resourceFIS = GlobalProperties.class.getClassLoader().
                getResourceAsStream(PORTAL_PROPERTIES_FILENAME);
            if (LOG.isInfoEnabled()) {
                LOG.info("Successfully read properties file");
            }
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
            if (LOG.isErrorEnabled()) {
                LOG.error("Error loading properties file: " + e.getMessage());
            }
        }

        return _properties;
    }
    


}
