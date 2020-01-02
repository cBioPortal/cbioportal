package org.mskcc.cbio.portal.dao;

import org.apache.commons.dbcp2.BasicDataSource;
import org.mskcc.cbio.portal.util.DatabaseProperties;
import org.apache.commons.lang.StringUtils;

/**
 * Data source that self-initializes based on cBioPortal configuration.
 */
public class JdbcDataSource extends BasicDataSource {
    public JdbcDataSource () {
        DatabaseProperties dbProperties = DatabaseProperties.getInstance();
        String host = dbProperties.getDbHost();
        String userName = dbProperties.getDbUser();
        String password = dbProperties.getDbPassword();
        String mysqlDriverClassName = dbProperties.getDbDriverClassName();
        String database = dbProperties.getDbName();
        String useSSL = (!StringUtils.isBlank(dbProperties.getDbUseSSL())) ? dbProperties.getDbUseSSL() : "false";
        String enablePooling = (!StringUtils.isBlank(dbProperties.getDbEnablePooling())) ? dbProperties.getDbEnablePooling(): "false";
        String url ="jdbc:mysql://" + host + "/" + database +
                        "?user=" + userName + "&password=" + password +
                        "&zeroDateTimeBehavior=convertToNull&useSSL=" + useSSL;
        //  Set up poolable data source
        this.setDriverClassName(mysqlDriverClassName);
        this.setUsername(userName);
        this.setPassword(password);
        this.setUrl(url);
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
}
