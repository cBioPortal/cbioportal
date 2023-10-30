package org.mskcc.cbio.portal.dao;

import org.apache.commons.dbcp2.BasicDataSource;
import org.mskcc.cbio.portal.util.GlobalProperties;
import org.springframework.util.Assert;


public class ClickhouseJdbcDataSource extends BasicDataSource {
    
    public ClickhouseJdbcDataSource() {
        String connectionString = GlobalProperties.getProperty("db.clickhouse.connection_string");
        String user = GlobalProperties.getProperty("db.clickhouse.user");
        String password = GlobalProperties.getProperty("db.clickhouse.password");

        Assert.notNull(connectionString, "Please set db.clickhouse.connection_string in portal.properties");
        Assert.notNull(user, "Please set db.clickhouse.user in portal.properties");
        Assert.notNull(password, "Please set db.clickhouse.password in portal.properties");
    
        this.setDriverClassName("com.clickhouse.jdbc.ClickHouseDriver");
        this.setUsername(user);
        this.setPassword(password);
        this.setUrl(connectionString);
        this.setJmxName("org.cbioportal:DataSource=clickhouse");
    }

}
