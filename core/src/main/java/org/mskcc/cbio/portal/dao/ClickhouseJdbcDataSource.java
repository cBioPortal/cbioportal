package org.mskcc.cbio.portal.dao;

import org.apache.commons.dbcp2.BasicDataSource;
import org.mskcc.cbio.portal.util.GlobalProperties;

public class ClickhouseJdbcDataSource extends BasicDataSource {
    
    public ClickhouseJdbcDataSource() {
        String connectionString = GlobalProperties.getProperty("db.clickhouse.connection_string");
        String user = GlobalProperties.getProperty("db.user");
        String password = GlobalProperties.getProperty("db.password");
        this.setDriverClassName("com.clickhouse.jdbc.ClickHouseDriver");
        this.setUsername(user);
        this.setPassword(password);
        this.setUrl(connectionString);
        this.setJmxName("org.cbioportal:DataSource=clickhouse");
    }
}
