// package
package org.mskcc.cbio.importer;

// imports
import javax.sql.DataSource;

/**
 * Interface used to create database/database schema dynamically.
 */
public interface DatabaseUtils {

    /**
	 * Creates a database schema within the given database.
	 * 
	 * @param databaseName - the database for which we are creating a schema
	 */
	void createSchema(final String databaseName);
}
