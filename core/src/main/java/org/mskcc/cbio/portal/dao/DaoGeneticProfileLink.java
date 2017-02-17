package org.mskcc.cbio.portal.dao;

import java.sql.*;
import org.mskcc.cbio.portal.model.*;

public class DaoGeneticProfileLink {
	
	// Keep Constructor empty
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
            connection = JdbcUtil.getDbConnection(DaoGenesetHierarchy.class);
	        
	        // Prepare SQL statement
            preparedStatement = connection.prepareStatement("INSERT INTO genetic_profile_link " 
	                + "(REFERRING_GENETIC_PROFILE_ID, REFERRED_GENETIC_PROFILE_ID, REFERENCE_TYPE) VALUES(?,?,?)");	        
            
            // Fill in statement
            preparedStatement.setInt(1, geneticProfileLink.getReferringGeneticProfileId());
            preparedStatement.setInt(2, geneticProfileLink.getReferredGeneticProfileId());
            preparedStatement.setString(3, geneticProfileLink.getReferenceType());
            
            // Execute statement
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGenesetInfo.class, connection, preparedStatement, resultSet);
        }
    }
}
