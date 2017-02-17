/*
 * Copyright (c) 2016 The Hyve B.V.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/*
 * @author ochoaa
 * @author Sander Tan
*/

package org.mskcc.cbio.portal.dao;

import java.sql.*;

import org.mskcc.cbio.portal.model.GenesetInfo;

public class DaoGenesetInfo {
	
	// Keep Constructor empty
	private DaoGenesetInfo() {
	}
	
	/**
     * Set gene set version in geneset_info table in database.
     * @throws DaoException 
     */
    public static void setGenesetInfo(GenesetInfo genesetInfo) throws DaoException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
        	// Open connection to database
            connection = JdbcUtil.getDbConnection(DaoGenesetHierarchy.class);
	        
	        // Prepare SQL statement
            preparedStatement = connection.prepareStatement("INSERT INTO geneset_info " + "(GENESET_VERSION) VALUES(?)");
            
            preparedStatement.setString(1, genesetInfo.getVersion());
            // Execute statement
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGenesetInfo.class, connection, preparedStatement, resultSet);
        }
    }	
    	
	/**
     * Set gene set version in geneset_info table in database.
     * @throws DaoException 
     */
    public static void updateGenesetInfo(GenesetInfo genesetInfo) throws DaoException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
        	// Open connection to database
            connection = JdbcUtil.getDbConnection(DaoGenesetHierarchy.class);
	        
	        // Prepare SQL statement
            preparedStatement = connection.prepareStatement("UPDATE geneset_info SET GENESET_VERSION=?");
            preparedStatement.setString(1, genesetInfo.getVersion());

            // Execute statement
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGenesetInfo.class, connection, preparedStatement, resultSet);
        }
    }	
    
	/**
     * Get gene set version from geneset_info table in database.
     * @throws DaoException 
     */
    public static GenesetInfo getGenesetInfo() throws DaoException {
    	
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
        	// Open connection to database
        	connection = JdbcUtil.getDbConnection(DaoGenesetInfo.class);
        	
	        // Prepare SQL statement
        	preparedStatement = connection.prepareStatement(
        			"SELECT * FROM geneset_info");
        	
            // Execute statement
        	resultSet = preparedStatement.executeQuery();
        	GenesetInfo genesetInfo = new GenesetInfo();

            // Extract version from result
            if (resultSet.next()) {
                genesetInfo.setVersion(resultSet.getString("GENESET_VERSION"));
            }    
        	return genesetInfo;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGenesetInfo.class, connection, preparedStatement, resultSet);
        }
    }

    /**
     * Deletes all records from 'geneset_info' table in database.
     * @throws DaoException 
     */
	public static void deleteAllRecords() throws DaoException {
		Connection con = null;
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;
	    try {
	        con = JdbcUtil.getDbConnection(DaoGeneset.class);
	
	        pstmt = con.prepareStatement("DELETE FROM geneset_info");
	        pstmt.executeUpdate();
	    }
	    catch (SQLException e) {
	        throw new DaoException(e);
	    } 
	    finally {
	        JdbcUtil.closeAll(DaoGeneset.class, con, pstmt, rs);
	    }
    }
}


