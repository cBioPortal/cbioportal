// package
package org.mskcc.cbio.importer.util.internal;

// imports
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.Map;
import java.util.HashMap;
import javax.sql.DataSource;

/**
 * Class which can create database/database schema dynamically.
 */
public class DataSourceFactoryBean implements BeanNameAware, FactoryBean<DataSource> {

	// our logger
	private static final Log LOG = LogFactory.getLog(DataSourceFactoryBean.class);

	// this map associates dynamic datasources with keys whose value takes on the bean id
	private static ThreadLocal<Map<String, DataSource>> beansByName =
        new ThreadLocal<Map<String, DataSource>>() {
            @Override
            protected Map<String, DataSource> initialValue() {
                return new HashMap<String, DataSource>(1);
			}
        };

	// the reference which holds our bean name (BeanNameAware interface)
	private String beanName;

	// the following are vars set from importer.properties 

	// db user
	private String databaseUser;
	@Value("${database_user}")
    public void setDatabaseUser(final String databaseUser) { this.databaseUser = databaseUser; }

	// db password
	private String databasePassword;
	@Value("${database_password}")
	public void setDbPassword(final String databasePassword) { this.databasePassword = databasePassword; }

	// db driver
	private String databaseDriver;
	@Value("${database_driver}")
	public void setDbDriver(final String databaseDriver) { this.databaseDriver = databaseDriver; }

	// db connection
	private String databaseConnectionString;
	@Value("${database_connection_string}")
	public void setDbConnection(final String databaseConnectionString) { this.databaseConnectionString = databaseConnectionString; }

	/**
	 * Our implementation of BeanNameAware.
	 */
	@Override
	public void setBeanName(final String beanName) {
		this.beanName = beanName;
	}

	/**
	 * Our implementation of FactoryBean
	 */
	@Override
    public DataSource getObject() {
        return getDataSource(beanName);
    }

	/**
	 * Our implementation of FactoryBean
	 */
    @Override
    public Class<?> getObjectType() {
        return getObject().getClass();
    }

	/**
	 * Our implementation of FactoryBean
	 */
    @Override
    public boolean isSingleton() {
        return true;
    }

	/**
	 * Method used to create a DataSource with the given key/database name.
	 *
	 * @param key String
	 * @param databaseName String
	 */
	public void createDataSourceMapping(final String key, final String databaseName) {

		getDataSourceMap().put(key, getDataSource(databaseName));
	}

	/**
	 * Method used to get a datasource for the given database.
	 *
	 * @param databaseName String
	 * @return DataSource
	 */
	public DataSource getDataSource(final String databaseName) {

        DataSource dataSource = getDataSourceMap().get(databaseName);
        if (dataSource == null) {
			// the args to the following call are properties set during bean instanciation
			dataSource = getDataSource(databaseUser, databasePassword,
									   databaseDriver, databaseConnectionString + databaseName);
        }

		// outta here
        return dataSource;
	}

	/**
	 * Method to return map of bean ids to datasources
	 */
	private static Map<String, DataSource> getDataSourceMap() {
        return beansByName.get();
    }

	/**
	 * Method used to create a Datasource.
	 *
	 * @param databaseUser String
	 * @param databasePassword String
	 * @param databaseDriver String
	 * @param databaseConnectionString String
	 * @return DataSource
	 */
	private static DataSource getDataSource(String databaseUser, String databasePassword,
											String databaseDriver, String databaseConnectionString) {

		// create the datasource and set its properties
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName(databaseDriver);
		dataSource.setUrl(databaseConnectionString + "?max_allowed_packet=256M");
		dataSource.setUsername(databaseUser);
		dataSource.setPassword(databasePassword);

		// outta here
		return dataSource;
	}
}