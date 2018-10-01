package org.mskcc.cbio.portal.dao;

import org.apache.commons.dbcp2.BasicDataSource;
import org.mskcc.cbio.portal.util.DatabaseProperties;

/**
 * Data source that self-initializes based on cBioPortal configuration.
 */
public class JdbcDataSource extends BasicDataSource {
    public JdbcDataSource () {
        DatabaseProperties dbProperties = DatabaseProperties.getInstance();
        String host = dbProperties.getDbHost();
        String userName = dbProperties.getDbUser();
        String password = dbProperties.getDbPassword();
        String database = dbProperties.getDbName();
        String url ="jdbc:mysql://" + host + "/" + database +
                        "?user=" + userName + "&password=" + password +
                        "&zeroDateTimeBehavior=convertToNull";
        //  Set up poolable data source
        this.setDriverClassName("com.mysql.jdbc.Driver");
        this.setUsername(userName);
        this.setPassword(password);
        this.setUrl(url);
        //  By pooling/reusing PreparedStatements, we get a major performance gain
        this.setPoolPreparedStatements(true);
        this.setMaxTotal(100);
    }
}
