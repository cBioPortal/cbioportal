// package
package org.mskcc.cbio.importer.io.internal;

// imports
import org.mskcc.cbio.importer.DatabaseUtils;
import org.mskcc.cbio.importer.io.internal.DataSourceFactoryBean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DataAccessException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.sql.DataSource;

/**
 * Class which can create database/database schema dynamically.
 */
public class DatabaseUtilsImpl implements DatabaseUtils {

	// our logger
	private static final Log LOG = LogFactory.getLog(DatabaseUtilsImpl.class);

	// some context files
	private static final String importerContextFile = "classpath:applicationContext-importer.xml";
	private static final String createSchemaContextFile = "classpath:applicationContext-createSchema.xml";

    /**
	 * Creates a database schema within the given database.
	 * 
	 * @param databaseName - the database for which we are creating a schema
	 */
	@Override
	public void createSchema(final String databaseName) {

		if (LOG.isInfoEnabled()) {
			LOG.info("createSchema(): " + databaseName);
		}

		// we want to get a reference to the database util factory bean interface
		// so we can set a datasource which matches the bean DatabaseUtil bean name
		ApplicationContext context = new ClassPathXmlApplicationContext(importerContextFile);
		DataSourceFactoryBean dataSourceFactoryBean =
			(DataSourceFactoryBean)context.getBean("&dataSourceFactory");

		// create the database - drop if it exists
		createDatabase(dataSourceFactoryBean, databaseName, true);

		// create a datasource to this database name - important to set the map key
		// to be equal to the bean name within the createSchema context file
		dataSourceFactoryBean.createDataSourceMapping("createSchema", databaseName);

		// load the context that auto-creates tables
		context = new ClassPathXmlApplicationContext(createSchemaContextFile);
	}

	/**
	 * Creates a database with the given name.
	 *
	 * @param dataSourceFactoryBean DataSourceFactoryBean
	 * @param databaseName String
	 * @param dropDatabase boolean
	 * @return boolean
	 */
	private boolean createDatabase(DataSourceFactoryBean dataSourceFactoryBean,
								   String databaseName, final boolean dropDatabase) {

		boolean toReturn = true;

		// create simple JdbcTemplate if necessary
		DataSource dataSource = dataSourceFactoryBean.getDataSource("");
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		try {
			// drop if desired
			if (dropDatabase) {
				jdbcTemplate.execute("DROP DATABASE IF EXISTS " + databaseName);
			}
			// create
			jdbcTemplate.execute("CREATE DATABASE " + databaseName);
			if (LOG.isInfoEnabled()) {
				LOG.info("createDatabase(): " + databaseName + " successfully created.");
			}
		}
		catch (DataAccessException e) {
			LOG.error(e);
			toReturn = false;
		}

		// outta here
		return toReturn;
	}
}
