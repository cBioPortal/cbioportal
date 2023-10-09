package org.mskcc.cbio.portal.dao;

import org.apache.commons.dbcp2.BasicDataSource;
import org.mskcc.cbio.portal.util.DatabaseProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data source that self-initializes based on cBioPortal configuration.
 */
public class JdbcDataSource extends BasicDataSource {
    
    private static final Logger LOG = LoggerFactory.getLogger(JdbcDataSource.class);

    public JdbcDataSource () {
        DatabaseProperties dbProperties = DatabaseProperties.getInstance();

        String host = dbProperties.getDbHost();
        String userName = dbProperties.getDbUser();
        String password = dbProperties.getDbPassword();
        String mysqlDriverClassName = dbProperties.getDbDriverClassName();
        String database = dbProperties.getDbName();
        String enablePooling = (!StringUtils.isBlank(dbProperties.getDbEnablePooling())) ? dbProperties.getDbEnablePooling(): "false";
        String connectionURL = dbProperties.getConnectionURL();

        if (defined(host) || defined(database) || defined(dbProperties.getDbUseSSL())) {
		LOG.warn("\n----------------------------------------------------------------------------------------------------------------" +
			"-- Connection error:\n" +
			"-- You try to connect to the database using the deprecated 'db.host', 'db.portal_db_name' and 'db.use_ssl' properties.\n" +
			"-- Please remove these properties and use the 'db.connection_string' property instead. See https://docs.cbioportal.org/deployment/customization/portal.properties-reference/\n" +
			"-- for assistance on building a valid connection string.\n" +
			"----------------------------------------------------------------------------------------------------------------\n"
		);
	}

        Assert.hasText(userName, errorMessage("username", "db.user"));
        Assert.hasText(password, errorMessage("password", "db.password"));
        Assert.hasText(mysqlDriverClassName, errorMessage("driver class name", "db.driver"));

        this.setUrl(connectionURL);

        //  Set up poolable data source
        this.setDriverClassName(mysqlDriverClassName);
        this.setUsername(userName);
        this.setPassword(password);
        // Disable this to avoid caching statements
        this.setPoolPreparedStatements(Boolean.valueOf(enablePooling));
        // these are the values cbioportal has been using in their production
        // context.xml files when using jndi
        this.setMaxTotal(500);
        this.setMaxIdle(30);
        this.setMaxWaitMillis(10000);
        this.setMinEvictableIdleTimeMillis(30000);
        this.setTestOnBorrow(true);
        this.setValidationQuery("SELECT 1");
        this.setJmxName("org.cbioportal:DataSource=" + database);
    }

    private String errorMessage(String displayName, String propertyName) {
        return String.format("No %s provided for database connection. Please set '%s' in portal.properties.", displayName, propertyName);
    }

    private boolean defined(String property) {
        return property != null && !property.isEmpty();
    }
}
