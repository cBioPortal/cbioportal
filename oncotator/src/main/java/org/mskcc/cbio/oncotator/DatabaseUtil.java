package org.mskcc.cbio.oncotator;

import java.sql.*;

/**
 * Connection Utility for oncotator JDBC.
 */
public class DatabaseUtil
{
    /**
     * Gets DB connection to oncotator database
     */
    public static Connection getDbConnection()
		    throws SQLException
    {
    	DatabaseProperties dbProperties = new DatabaseProperties("db.properties");
        
        String host = dbProperties.getDbHost();
        String userName = dbProperties.getDbUser();
        String password = dbProperties.getDbPassword();
        String database = dbProperties.getDbName();

        String url = new String("jdbc:mysql://" + host + "/" + database
                        + "?user=" + userName + "&password=" + password
                        + "&zeroDateTimeBehavior=convertToNull");

	    Connection conn = null;

	    try
	    {
		    Class.forName("com.mysql.jdbc.Driver").newInstance();
	    }
	    catch (Exception e)
	    {
		    e.printStackTrace();
	    }

	    conn = DriverManager.getConnection(url, userName, password);

	    return conn;
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

        if (ps != null) {
            try {
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
