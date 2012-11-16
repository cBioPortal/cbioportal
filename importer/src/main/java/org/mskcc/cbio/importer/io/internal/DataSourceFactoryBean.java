/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

// package
package org.mskcc.cbio.importer.io.internal;

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
	public void setDatabasePassword(final String databasePassword) { this.databasePassword = databasePassword; }

	// db driver
	private String databaseDriver;
	@Value("${database_driver}")
	public void setDatabaseDriver(final String databaseDriver) { this.databaseDriver = databaseDriver; }

	// db connection
	private String databaseConnectionString;
	@Value("${database_connection_string}")
	public void setDatabaseConnection(final String databaseConnectionString) { this.databaseConnectionString = databaseConnectionString; }

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