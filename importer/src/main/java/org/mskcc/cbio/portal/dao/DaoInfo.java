/*
 * Copyright (c) 2015 - 2016 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.mskcc.cbio.portal.util.GlobalProperties;

public class DaoInfo {
    private static String version;

	/**
	 * Set database schema version in `info` table in database.
	 * @throws DaoException 
	 */    
    public static synchronized void setVersion() {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        version = "-1";
        try {
            con = JdbcUtil.getDbConnection(DaoInfo.class);
            pstmt = con.prepareStatement
                    ("SELECT * FROM info");
            rs = pstmt.executeQuery();
            if (rs.next()) {
                version = rs.getString("db_schema_version");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            JdbcUtil.closeAll(DaoInfo.class, con, pstmt, rs);
        }
    }

	/**
	 * Get database schema version in `info` table in database.
	 * @throws DaoException 
	 */
    public static String getVersion() {
        return version;
    }

	/**
	 * Check database schema version in `info` table in database.
	 * @throws DaoException 
	 */
    public static boolean checkVersion(StringBuilder logMessageBuilder) {
        setVersion();
        String expectedVersion = GlobalProperties.getDbVersion();
        String foundVersion = getVersion();
        logMessageBuilder.append("Checked DB schema version: (expected: " + expectedVersion + ") (found: " + foundVersion +")");
        return foundVersion.equals(expectedVersion);
    }

	/**
	 * Set gene set version in `info` table in database.
	 * @throws DaoException 
	 */
	public static void setGenesetVersion(String genesetVersion) throws DaoException {
	    Connection connection = null;
	    PreparedStatement preparedStatement = null;
	    ResultSet resultSet = null;
	    try {
	    	// Open connection to database
	        connection = JdbcUtil.getDbConnection(DaoInfo.class);
	        
	        // Prepare SQL statement
	        preparedStatement = connection.prepareStatement("ALTER TABLE info UPDATE geneset_version = ? WHERE 1");
	        
	        preparedStatement.setString(1, genesetVersion);
	        // Execute statement
	        preparedStatement.executeUpdate();
	    } catch (SQLException e) {
	        throw new DaoException(e);
	    } finally {
	        JdbcUtil.closeAll(DaoInfo.class, connection, preparedStatement, resultSet);
	    }
	}

	/**
	 * Get gene set version from `info` table in database.
	 * @throws DaoException 
	 */
	public static String getGenesetVersion() throws DaoException {	
	    Connection connection = null;
	    PreparedStatement preparedStatement = null;
	    ResultSet resultSet = null;    
	    try {
	    	// Open connection to database
	    	connection = JdbcUtil.getDbConnection(DaoInfo.class);
	    	
	        // Prepare SQL statement
	    	preparedStatement = connection.prepareStatement(
	    			"SELECT * FROM info");
	    	
	        // Execute statement
	    	resultSet = preparedStatement.executeQuery();
	    	String genesetVersion = new String();
	
	        // Extract version from result
	        if (resultSet.next()) {
	            genesetVersion = resultSet.getString("geneset_version");
	        }    
	    	return genesetVersion;
	    } catch (SQLException e) {
	        throw new DaoException(e);
	    } finally {
	        JdbcUtil.closeAll(DaoInfo.class, connection, preparedStatement, resultSet);
	    }
	}

	/**
	 * Clears (resets to NULL) the gene set version in `info` table in database.
	 * @throws DaoException 
	 */
	public static void clearVersion() throws DaoException {
		Connection con = null;
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;
	    try {
	        con = JdbcUtil.getDbConnection(DaoInfo.class);
	
	        pstmt = con.prepareStatement("ALTER TABLE info UPDATE geneset_version = NULL WHERE 1");
	        pstmt.executeUpdate();
	    }
	    catch (SQLException e) {
	        throw new DaoException(e);
	    } 
	    finally {
	        JdbcUtil.closeAll(DaoInfo.class, con, pstmt, rs);
	    }
	}
}
