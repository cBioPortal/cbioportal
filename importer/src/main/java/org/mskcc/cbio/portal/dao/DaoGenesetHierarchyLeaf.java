/*
 * Copyright (c) 2017 The Hyve B.V.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

/*
 * @author Sander Tan
*/

package org.mskcc.cbio.portal.dao;

import java.sql.*;
import java.util.*;
import org.mskcc.cbio.portal.model.GenesetHierarchyLeaf;

public class DaoGenesetHierarchyLeaf {

	private DaoGenesetHierarchyLeaf() {
	}
    
	/**
     * Add gene set hierarchy object to geneset_hierarchy_leaf table in database.
     * @throws DaoException 
     */	
	public static void addGenesetHierarchyLeaf(GenesetHierarchyLeaf genesetHierarchyLeaf) throws DaoException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
        	// Open connection to database
            connection = JdbcUtil.getDbConnection(DaoGenesetHierarchyLeaf.class);
	        
	        // Prepare SQL statement
            preparedStatement = connection.prepareStatement("INSERT INTO geneset_hierarchy_leaf " 
	                + "(`node_id`, `geneset_id`) VALUES(?,?)");
	        
            // Fill in statement
            preparedStatement.setInt(1, genesetHierarchyLeaf.getNodeId());
            preparedStatement.setInt(2, genesetHierarchyLeaf.getGenesetId());
            
            // Execute statement
            preparedStatement.executeUpdate();
            
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGenesetHierarchyLeaf.class, connection, preparedStatement, resultSet);
        }
	}
	
	public static List<GenesetHierarchyLeaf> getGenesetHierarchyLeafsByGenesetId(int genesetId) throws DaoException {
		Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
        	// Open connection to database
            connection = JdbcUtil.getDbConnection(DaoGenesetHierarchyLeaf.class);
	        
	        // Prepare SQL statement
            preparedStatement = connection.prepareStatement("SELECT * FROM geneset_hierarchy_leaf WHERE geneset_id = ?");
            preparedStatement.setInt(1, genesetId);

            // Execute statement
            resultSet = preparedStatement.executeQuery();
            
            List<GenesetHierarchyLeaf> genesetHierarchyLeafs = new ArrayList<GenesetHierarchyLeaf>();

            while (resultSet.next()) {
                GenesetHierarchyLeaf genesetHierarchyLeaf = new GenesetHierarchyLeaf();
            	genesetHierarchyLeaf.setNodeId(resultSet.getInt("node_id"));
                genesetHierarchyLeaf.setGenesetId(resultSet.getInt("geneset_id"));
                genesetHierarchyLeafs.add(genesetHierarchyLeaf);
            }
            
            return genesetHierarchyLeafs;

        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGenesetHierarchyLeaf.class, connection, preparedStatement, resultSet);
        }
	}
}
