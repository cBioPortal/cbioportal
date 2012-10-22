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
import org.mskcc.cbio.importer.DatabaseUtils;
import org.mskcc.cbio.importer.io.internal.DataSourceFactoryBean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DataAccessException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.beans.factory.annotation.Value;

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

	// the follow db properties are set here for convenient access by our clients

	// db user 
	private String databaseUser;
	@Value("${database_user}")
    public void setDatabaseUser(final String databaseUser) { this.databaseUser = databaseUser; }
	@Override
    public String getDatabaseUser() { return this.databaseUser; }

	// db password
	private String databasePassword;
	@Value("${database_password}")
	public void setDatabasePassword(final String databasePassword) { this.databasePassword = databasePassword; }
	@Override
    public String getDatabasePassword() { return this.databasePassword; }

	// db connection
	private String databaseConnectionString;
	@Value("${database_connection_string}")
	public void setDatabaseConnectionString(final String databaseConnectionString) { this.databaseConnectionString = databaseConnectionString; }
	@Override
    public String getDatabaseConnectionString() { return this.databaseConnectionString; }

	// importer database name
	private String importerDatabaseName;
	@Value("${importer_database_name}")
	public void setImporterDatabaseName(final String importerDatabaseName) { this.importerDatabaseName = importerDatabaseName; }
	@Override
    public String getImporterDatabaseName() { return this.importerDatabaseName; }

	// portal database name
	private String portalDatabaseName;
	@Value("${portal_database_name}")
	public void setPortalDatabaseName(final String portalDatabaseName) { this.portalDatabaseName = portalDatabaseName; }
	@Override
    public String getPortalDatabaseName() { return this.portalDatabaseName; }

	// gene information database name
	private String geneInformationDatabaseName;
	@Value("${gene_information_database_name}")
	public void setGeneInformationDatabaseName(final String geneInformationDatabaseName) { this.geneInformationDatabaseName = geneInformationDatabaseName; }
	@Override
    public String getGeneInformationDatabaseName() { return this.geneInformationDatabaseName; }

    /**
	 * Creates a database and optional schema.
	 * 
	 * @param databaseName String
	 * @param createSchema boolean
	 */
	@Override
	public void createDatabase(final String databaseName, final boolean createSchema) {

		if (LOG.isInfoEnabled()) {
			LOG.info("createDatabase(): " + databaseName);
		}

		// we want to get a reference to the database util factory bean interface
		// so we can set a datasource which matches the bean DatabaseUtil bean name
		ApplicationContext context = new ClassPathXmlApplicationContext(importerContextFile);
		DataSourceFactoryBean dataSourceFactoryBean =
			(DataSourceFactoryBean)context.getBean("&dataSourceFactory");

		// create the database - drop if it exists
		createDatabase(dataSourceFactoryBean, databaseName, true);

		if (createSchema) {
			// create a datasource to this database name - important to set the map key
			// to be equal to the bean name within the createSchema context file
			dataSourceFactoryBean.createDataSourceMapping("createSchema", databaseName);

			// load the context that auto-creates tables
			context = new ClassPathXmlApplicationContext(createSchemaContextFile);
		}
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
