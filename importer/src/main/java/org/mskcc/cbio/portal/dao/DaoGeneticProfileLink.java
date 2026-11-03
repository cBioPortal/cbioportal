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
import org.mskcc.cbio.portal.model.GeneticProfileLink;

public class DaoGeneticProfileLink {
	
	private DaoGeneticProfileLink() {
	}
	
	/**
     * Set genetic profile link in `genetic_profile_link` table in database.
     * @throws DaoException 
     */
    public static void addGeneticProfileLink(GeneticProfileLink geneticProfileLink) throws DaoException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
        	// Open connection to database
            connection = JdbcUtil.getDbConnection(DaoGeneticProfileLink.class);
	        
	        // Prepare SQL statement
            preparedStatement = connection.prepareStatement("INSERT INTO genetic_profile_link " 
	                + "(referring_genetic_profile_id, referred_genetic_profile_id, reference_type) VALUES(?,?,?)");
            
            // Fill in statement
            preparedStatement.setInt(1, geneticProfileLink.getReferringGeneticProfileId());
            preparedStatement.setInt(2, geneticProfileLink.getReferredGeneticProfileId());
            preparedStatement.setString(3, geneticProfileLink.getReferenceType());
            
            // Execute statement
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGeneticProfileLink.class, connection, preparedStatement, resultSet);
        }
    }
}
