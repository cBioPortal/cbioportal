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
package org.mskcc.cbio.importer.model;

// imports
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Class which contains database metadata.
 */
public final class DatabaseMetadata {

	// bean properties
    private String user;
    private String password;
    private String driver;
    private String connectionString;
    private String importerDatabase;
    private String portalDatabase;
    private String geneInformationDatabase;

    /**
     * Create a PortalMetadata instance with specified properties.
     *
     * @param user String
     * @param password String
     * @param driver String
     * @param connectionString String
	 * @param importerDatabase String
	 * @param portalDatabase String
	 * @param geneInformationDatabase String
     */
    public DatabaseMetadata(final String user, final String password,
							final String driver, final String connectionString,
							final String importerDatabase, final String portalDatabase,
							final String geneInformationDatabase) {

		if (user == null) {
            throw new IllegalArgumentException("user must not be null");
		}
		this.user = user;

		if (password == null) {
            throw new IllegalArgumentException("password must not be null");
		}
		this.password = password;

		if (driver == null) {
            throw new IllegalArgumentException("driver must not be null");
		}
		this.driver = driver;

		if (connectionString == null) {
            throw new IllegalArgumentException("connectionString must not be null");
		}
		this.connectionString = connectionString;

		if (importerDatabase == null) {
            throw new IllegalArgumentException("importerDatabase must not be null");
		}
		this.importerDatabase = importerDatabase;

		if (portalDatabase == null) {
            throw new IllegalArgumentException("portalDatabase must not be null");
		}
		this.portalDatabase = portalDatabase;

		if (geneInformationDatabase == null) {
            throw new IllegalArgumentException("geneInformationDatabase must not be null");
		}
		this.geneInformationDatabase = geneInformationDatabase;
	}

	public String getUser() { return user; }
	public String getPassword() { return password; }
	public String getDriver() { return driver; }
	public String getConnectionString() { return connectionString; }
	public String getImporterDatabase() { return importerDatabase; }
	public String getPortalDatabase() { return portalDatabase; }
	public String getGeneInformationDatabase() { return geneInformationDatabase; }
}
