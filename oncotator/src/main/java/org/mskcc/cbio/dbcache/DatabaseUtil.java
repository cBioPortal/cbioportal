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

import org.apache.commons.dbcp.BasicDataSource;

import java.sql.*;

/**
 * Connection Utility for JDBC.
 */
public class DatabaseUtil
{
	private static BasicDataSource ds;

    /**
     * Gets DB connection to the database
     */
    public static Connection getDbConnection()
		    throws SQLException
    {
	    if (ds == null)
	    {
		    initDataSource();
	    }

	    Connection conn = ds.getConnection();
	    return conn;
    }

	/**
	 * Initializes DB via the BasicDataSource instance.
	 */
	public static void initDataSource()
	{
		DatabaseProperties dbProperties = new DatabaseProperties("db.properties");

		String host = dbProperties.getDbHost();
		String userName = dbProperties.getDbUser();
		String password = dbProperties.getDbPassword();
		String database = dbProperties.getDbName();
		String driver = "com.mysql.jdbc.Driver";

		String url = new String("jdbc:mysql://" + host + "/" + database
		                        + "?user=" + userName + "&password=" + password
		                        + "&zeroDateTimeBehavior=convertToNull");

		//  Set up poolable data source
		ds = new BasicDataSource();
		ds.setDriverClassName(driver);
		ds.setUsername(userName);
		ds.setPassword(password);
		ds.setUrl(url);

		//  By pooling/reusing PreparedStatements, we get a major performance gain
		ds.setPoolPreparedStatements(true);
		ds.setMaxActive(75);
	}

    /**
     * Frees Database Connection.
     *
     * @param con Connection Object.
     */
    private static void closeConnection(Connection con) throws SQLException
    {
        if (con != null && !con.isClosed()) {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Frees Database Connection.
     *
     * @param con Connection Object.
     * @param ps  Prepared Statement Object.
     * @param rs  ResultSet Object.
     */
    public static void closeAll(Connection con,
		    PreparedStatement ps,
            ResultSet rs)
    {
        try
        {
			if (con != null) {
				closeConnection(con);
			}
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

	    // pooling, so do not close prepared statement...
//        if (ps != null) {
//            try {
//                ps.close();
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        }

        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
