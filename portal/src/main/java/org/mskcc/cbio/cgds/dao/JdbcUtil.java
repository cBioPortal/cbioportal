package org.mskcc.cbio.cgds.dao;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbcp.DelegatingPreparedStatement;
import org.mskcc.cbio.cgds.util.DatabaseProperties;

import java.sql.*;

/**
 * Connection Utility for JDBC.
 *
 * @author Ethan Cerami
 */
public class JdbcUtil {
    private static BasicDataSource ds;

    /**
     * Gets Connection to the CPath Database.
     *
     * @return Live Connection to Database.
     * @throws java.sql.SQLException Error Connecting to Database.
     */
    public static Connection getDbConnection() throws SQLException {
        if (ds == null) {
            initDataSource();
        }
        Connection con = ds.getConnection();
        return con;
    }

    /**
     * Initializes Data Source.
     */
    private static void initDataSource() {
        DatabaseProperties dbProperties = DatabaseProperties.getInstance();
        String host = dbProperties.getDbHost();
        String userName = dbProperties.getDbUser();
        String password = dbProperties.getDbPassword();
        String database = dbProperties.getDbName();

        String url =
                new String("jdbc:mysql://" + host + "/" + database
                        + "?user=" + userName + "&password=" + password
                        + "&zeroDateTimeBehavior=convertToNull");

        //  Set up poolable data source
        ds = new BasicDataSource();
        ds.setDriverClassName("com.mysql.jdbc.Driver");
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
    private static void closeConnection(Connection con) throws SQLException {
        if (con != null && !con.isClosed()) {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Frees PreparedStatement and ResultSet.
     *
     * @param ps  Prepared Statement Object.
     * @param rs  ResultSet Object.
     */
    public static void closeAll(PreparedStatement ps, ResultSet rs) {
		JdbcUtil.closeAll(null, ps, rs);
	}

    /**
     * Frees Database Connection.
     *
     * @param con Connection Object.
     * @param ps  Prepared Statement Object.
     * @param rs  ResultSet Object.
     */
    public static void closeAll(Connection con, PreparedStatement ps,
            ResultSet rs) {
        try {
			if (con != null) {
				closeConnection(con);
			}
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //  Don't close PreparedStatements, as we have configured DBCP to pool/reuse
        //  PreparedStatements.
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

    /**
     * Gets the SQL string statement associated with a PreparedStatement.
     * <p/>
     * This method compensates for a bug in the DBCP Code.  DBCP wraps an
     * original PreparedStatement object, but when you call toString() on the
     * wrapper, it returns a generic String representation that does not include
     * the actual SQL code which gets executed.  To get around this bug, this
     * method checks to see if we have a DBCP wrapper.  If we do, we get the
     * original delegate, and properly call its toString() method.  This
     * results in the actual SQL statement sent to the database.
     *
     * @param pstmt PreparedStatement Object.
     * @return toString value.
     */
    public static String getSqlQuery(PreparedStatement pstmt) {
        if (pstmt instanceof DelegatingPreparedStatement) {
            DelegatingPreparedStatement dp =
                    (DelegatingPreparedStatement) pstmt;
            Statement delegate = dp.getDelegate();
            return delegate.toString();
        } else {
            return pstmt.toString();
        }
    }
}
