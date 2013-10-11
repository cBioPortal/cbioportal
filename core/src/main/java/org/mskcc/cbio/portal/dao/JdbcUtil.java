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

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbcp.DelegatingPreparedStatement;
import org.mskcc.cbio.portal.util.DatabaseProperties;

/**
 * Connection Utility for JDBC.
 *
 * @author Ethan Cerami
 */
public class JdbcUtil {
    private static BasicDataSource ds;
    private static int MAX_JDBC_CONNECTIONS = 100;
    private static Map<String,Integer> activeConnectionCount; // keep track of the number of active connection per class/requester

    /**
     * Gets Connection to the Database.
     * 
     * @param requester class
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
    public static Connection getDbConnection(String requester) throws SQLException {
        if (ds == null) {
            initDataSource();
        } else if (ds.getNumActive()>=MAX_JDBC_CONNECTIONS) {
            ds.close();
            initDataSource();
            System.err.println("Reach the maximum number of database connections: "+MAX_JDBC_CONNECTIONS
                    + "\n" + activeConnectionCount.toString());
        }
        
        Connection con = ds.getConnection();
        
        if (requester!=null) {
            Integer count = activeConnectionCount.get(requester);
            activeConnectionCount.put(requester, count==null ? 1 : (count+1));
        }
        
        if (ds.getNumActive() >= MAX_JDBC_CONNECTIONS/2) {
            System.err.println("Opened a MySQL connection. Active connections: "+ds.getNumActive()
                        + "\n" + activeConnectionCount.toString());
        }
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
        ds.setMaxActive(MAX_JDBC_CONNECTIONS);
        
        activeConnectionCount = new HashMap<String,Integer>();
    }

    /**
     * Frees Database Connection.
     *
     * @param con Connection Object.
     */
    public static void closeConnection(Class clazz, Connection con) {
        closeConnection(clazz.getName(), con);
    }
    
    public static void closeConnection(String requester, Connection con) {
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
                
                if (ds.getNumActive() >= MAX_JDBC_CONNECTIONS/2) {
                    System.err.println("Closed a MySQL connection. Active connections: "+ds.getNumActive()
                        + "\n" + activeConnectionCount.toString());
                }
            }
        } catch (SQLException e) {
            System.err.println("Problem Closed a MySQL connection from "+requester+".\nActive connections: "+ds.getNumActive()
                        + "\n" + activeConnectionCount.toString());
            e.printStackTrace();
        }
    }

    /**
     * Frees PreparedStatement and ResultSet.
     *
     * @param ps  Prepared Statement Object.
     * @param rs  ResultSet Object.
     */
    public static void closeAll(PreparedStatement ps, ResultSet rs) {
                JdbcUtil.closeAll((String)null, null, ps, rs);
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
        closeAll(clazz.getName(), con, ps, rs);
    }

    /**
     * Frees Database Connection.
     *
     * @param con Connection Object.
     * @param ps  Prepared Statement Object.
     * @param rs  ResultSet Object.
     */
    public static void closeAll(String requester, Connection con, PreparedStatement ps,
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
}
