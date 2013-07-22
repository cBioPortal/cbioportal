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

package org.mskcc.cbio.dbcache;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Stores db props (name, id, pw, host) and makes them accessible.
 */
public class DatabaseProperties
{
    private String dbHost;
    private String dbUser;
    private String dbPassword;
    private String dbName;

    public DatabaseProperties(String configFilename)
    {
    	try
    	{
            File configFile = new File(configFilename);
            InputStream in = (configFile.exists()) ? new FileInputStream(configFile) : this.getClass().getClassLoader().getResourceAsStream(configFilename);
            if (in == null) {
//                System.err.println( "Properties file '" + configFilename + "' could not be found by getResourceAsStream(). Check the CLASSPATH or class loader.\n" +
//                                    "See http://download.oracle.com/javase/1.5.0/docs/api/java/lang/Class.html#getResourceAsStream%28java.lang.String%29 re proper location of properties file.");
//                System.exit(1);
	            throw new RuntimeException("Properties file '" + configFilename + "' could not be found by getResourceAsStream(). Check the CLASSPATH or class loader.\n" +
                                    "See http://download.oracle.com/javase/1.5.0/docs/api/java/lang/Class.html#getResourceAsStream%28java.lang.String%29 re proper location of properties file.");
            }
        	Properties props = new Properties();
			props.load(in);

			this.setDbHost(props.getProperty("db.host"));
	    	this.setDbName(props.getProperty("db.name"));
	    	this.setDbUser(props.getProperty("db.user"));
	    	this.setDbPassword(props.getProperty("db.password"));
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    public String getDbHost() {
        return dbHost;
    }

    public void setDbHost(String dbHost) {
        this.dbHost = dbHost;
    }

    public String getDbUser() {
        return dbUser;
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }
}
