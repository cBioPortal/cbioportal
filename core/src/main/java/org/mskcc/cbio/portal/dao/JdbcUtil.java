/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center 
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center 
 * has been advised of the possibility of such damage.
*/

package org.mskcc.cbio.portal.dao;

import org.mskcc.cbio.portal.util.*;

import org.apache.commons.logging.*;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.beans.factory.BeanCreationException;

import java.sql.*;
import java.util.*;
import javax.sql.DataSource;
import javax.naming.InitialContext;

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
        // this method should be syncronized
        // but may slow the speed?
        
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

    private static DataSource initDataSource()
    {
        DataSource ds = null;
        ApplicationContext ctx = null;
        try {
            ctx = getContext("jndi");
            ds = (DataSource)ctx.getBean("businessDataSource");
        }
        catch (Exception e) {
            logMessage("Problem creating jndi datasource, opening dbcp datasource.");
            ctx = getContext("dbcp");
            ds = (DataSource)ctx.getBean("businessDataSource");
        }

        activeConnectionCount = new HashMap<String,Integer>();

        return ds;
    }

    private static ApplicationContext getContext(String profile)
    {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:applicationContext-business.xml");
        ctx.getEnvironment().setActiveProfiles(profile);
        ctx.refresh();
        return ctx; 
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
                    if (count<0) {
                        // since adding connection is not synchronized, the count may not be the real one
                        count = 0;
                    }
                    
                    activeConnectionCount.put(requester, count);
                }
            }
        } catch (Exception e) {
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

    private static void logMessage(String message) {
        if (LOG.isInfoEnabled()) {
            LOG.info(message);
        }
        System.err.println(message);
    }
    
    // is it good to put the two methods below here?
    static Integer readIntegerFromResultSet(ResultSet rs, String column) throws SQLException {
        int i = rs.getInt(column);
        return rs.wasNull() ? null : i;
    }
    
    static Long readLongFromResultSet(ResultSet rs, String column) throws SQLException {
        long l = rs.getInt(column);
        return rs.wasNull() ? null : l;
    }
    
    static Double readDoubleFromResultSet(ResultSet rs, String column) throws SQLException {
        double d = rs.getDouble(column);
        return rs.wasNull() ? null : d;
    }
}
