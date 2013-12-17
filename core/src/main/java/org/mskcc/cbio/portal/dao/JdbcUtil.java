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

package org.mskcc.cbio.portal.dao;

import org.mskcc.cbio.portal.util.DatabaseProperties;
import org.mskcc.cbio.portal.util.GlobalProperties;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbcp.DelegatingPreparedStatement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import javax.naming.InitialContext;
import javax.sql.DataSource;

/**
 * Connection Utility for JDBC.
 *
 * @author Ethan Cerami
 */
public class JdbcUtil {
    private static DataSource ds;
    private static final int MAX_JDBC_CONNECTIONS = 100;
    private static Map<String,Integer> activeConnectionCount; // keep track of the number of active connection per class/requester
    private static final Log LOG = LogFactory.getLog(JdbcUtil.class);

    /**
     * Gets Connection to the Database.
     * 
     * @param clazz class
     * @return Live Connection to Database.
     * @throws java.sql.SQLException Error Connecting to Database.
     */
    public static Connection getDbConnection(Class clazz) throws SQLException {
        return getDbConnection(clazz.getName());
    }
    
    /**
     * Gets Connection to the Database.
     * 
     * @param requester name
     * @return Live Connection to Database.
     * @throws java.sql.SQLException Error Connecting to Database.
     */
    private static Connection getDbConnection(String requester) throws SQLException {

        if (ds == null) {
            ds = initDataSource();
        }

        Connection con;
        try {
            con = ds.getConnection();
        }
        catch (Exception e) {
            logMessage(e.getMessage());
            throw new SQLException(e);
        }

        if (requester!=null) {
            Integer count = activeConnectionCount.get(requester);
            activeConnectionCount.put(requester, count==null ? 1 : (count+1));
        }
        
        return con;
    }

    private static DataSource initDataSource() {

        DataSource ds = initDataSourceTomcat();

        try {
            ds.getConnection();
        }
        catch (Exception e) {
            ds = null;
        }

        if (ds == null) {
            ds = initDataSourceDirect();
        }

        activeConnectionCount = new HashMap<String,Integer>();

        return ds;
    }

    private static DataSource initDataSourceTomcat() {

        DataSource ds = null;
        activeConnectionCount = new HashMap<String,Integer>();
       
        try {
            InitialContext cxt = new InitialContext();
            if (cxt == null) {
                throw new Exception("Context for creating data source not found!");
            }
            ds = (DataSource)cxt.lookup( "java:/comp/env/jdbc/" + GlobalProperties.getProperty("db.portal_db_name") );
            if (ds == null) {
                throw new Exception("Data source not found!");
            }
        }
        catch (Exception e) {
            logMessage(e.getMessage());
        }

        return ds;
    }

    /**
     * Initializes Data Source.
     */
    private static DataSource initDataSourceDirect() {

        DatabaseProperties dbProperties = DatabaseProperties.getInstance();
        String host = dbProperties.getDbHost();
        String userName = dbProperties.getDbUser();
        String password = dbProperties.getDbPassword();
        String database = dbProperties.getDbName();
        String driverClassname = dbProperties.getDbDriverClassName();

        String url =
                "jdbc:mysql://" + host + "/" + database
                        + "?user=" + userName + "&password=" + password
                        + "&zeroDateTimeBehavior=convertToNull";
        
        //  Set up poolable data source
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(driverClassname);
        ds.setUsername(userName);
        ds.setPassword(password);
        ds.setUrl(url);

        //  By pooling/reusing PreparedStatements, we get a major performance gain
        ds.setPoolPreparedStatements(true);
        ds.setMaxActive(MAX_JDBC_CONNECTIONS);

        return ds;
    }

    /**
     * Frees Database Connection.
     *
     * @param con Connection Object.
     */
    public static void closeConnection(Class clazz, Connection con) {
        closeConnection(clazz.getName(), con);
    }
    
    private static void closeConnection(String requester, Connection con) {
        try {
            if (con != null && !con.isClosed()) {
                con.close();
                
                if (requester!=null) {
                    int count = activeConnectionCount.get(requester)-1;
                    if (count==0) {
                        activeConnectionCount.remove(requester);
                    } else {
                        activeConnectionCount.put(requester, count);
                    }
                }
            }
        } catch (SQLException e) {
            logMessage("Problem Closed a MySQL connection from " + requester + ": " + activeConnectionCount.toString());
            e.printStackTrace();
        }
    }

    /**
     * Frees PreparedStatement and ResultSet.
     *
     * @param rs  ResultSet Object.
     */
    public static void closeAll(ResultSet rs) {
                JdbcUtil.closeAll((String)null, null, rs);
        }

    /**
     * Frees Database Connection.
     *
     * @param con Connection Object.
     * @param ps  Prepared Statement Object.
     * @param rs  ResultSet Object.
     */
    public static void closeAll(Class clazz, Connection con, PreparedStatement ps,
            ResultSet rs) {
        closeAll(clazz.getName(), con, rs);
    }

    /**
     * Frees Database Connection.
     *
     * @param con Connection Object.
     * @param rs  ResultSet Object.
     */
    private static void closeAll(String requester, Connection con,
                                 ResultSet rs) {
        closeConnection(requester, con);
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Why does closeAll need a PreparedStatement?
     * This is a copy of closeAll without the PreparedStatement
     * @param clazz
     * @param con
     * @param rs
     */
    public static void closeAll(Class clazz, Connection con, ResultSet rs) {
        String requester = clazz.getName();
        closeConnection(requester, con);
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

    private static void logMessage(String message) {
        if (LOG.isInfoEnabled()) {
            LOG.info(message);
        }
        System.err.println(message);
    }
}
