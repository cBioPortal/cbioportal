package org.mskcc.cbio.oncotator;

import org.apache.commons.dbcp.BasicDataSource;

import java.sql.*;

/**
 * Connection Utility for oncotator JDBC.
 */
public class DatabaseUtil
{
	private static BasicDataSource ds;

    /**
     * Gets DB connection to oncotator database
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
