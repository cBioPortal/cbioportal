/*
 * Copyright (c) 2019. The Hyve B.V.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
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
 * @author Pim van Nierop, pim@thehyve.nl
*/

package org.mskcc.cbio.portal.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.cbioportal.model.EntityType;
import org.mskcc.cbio.portal.model.Treatment;

public class DaoTreatment {

    private enum SqlAction {INSERT, UPDATE, SELECT, DELETE}

    /**
     * Adds a new Treatment record to the database.
     * @param treatment
     * @return number of records successfully added
     * @throws DaoException 
     */
    public static Treatment addTreatment(Treatment treatment) throws DaoException {

        // new treatment requires a genetic entity to be added first
        int geneticEntityId = DaoGeneticEntity.addNewGeneticEntity(EntityType.TREATMENT);
        treatment.setGeneticEntityId(geneticEntityId);

        DbContainer container = executeSQLstatment(
            SqlAction.INSERT,
            "INSERT INTO treatment (`GENETIC_ENTITY_ID`, `STABLE_ID`, `NAME`, `DESCRIPTION`, `LINKOUT_URL`) "
            + "VALUES(?,?,?,?,?)",
            treatment.getGeneticEntityId(),
            treatment.getStableId(),
            treatment.getName(),
            treatment.getDescription(),
            treatment.getRefLink()
        );

        treatment.setId(container.getId());

        return treatment;
    }

    /**
     * Given an external id, returns a Treatment record.
     * @param stableId
     * @return Treatment record
     * @throws DaoException 
     */
    public static Treatment getTreatmentByStableId(String stableId) throws DaoException {
        DbContainer container = executeSQLstatment(SqlAction.SELECT, "SELECT * FROM treatment WHERE `STABLE_ID` = ?", stableId);
        return container.getTreatment();
    }
    
    /**
     * Get Treatment record.
     * @param treatment id
     */
    public Treatment getTreatmentById(int id) throws DaoException {
        DbContainer container = executeSQLstatment(SqlAction.SELECT, "SELECT * FROM treatment WHERE ID = ?", String.valueOf(id));
        return container.getTreatment();
    }

    /**
     * Update Treatment record.
     * @param treatment id
     */
    public static void updateTreatment(Treatment treatment) throws DaoException {

        executeSQLstatment(
            SqlAction.UPDATE,
            "UPDATE treatment SET `NAME` = ?, `DESCRIPTION` = ?, `LINKOUT_URL` = ? WHERE `STABLE_ID` = ?", 
            treatment.getName(),
            treatment.getDescription(),
            treatment.getRefLink(),
            treatment.getStableId()
        );

    }
    
    /**
     * Delete Treatment genetic entity records.
     */
    private static void deleteTreatmentGeneticEntityRecords() throws DaoException {
        executeSQLstatment(
            SqlAction.DELETE,
            "DELETE FROM genetic_entity WHERE ENTITY_TYPE = 'TREATMENT'"
        );
    }   
    
    /**
     * Deletes Treatment data such as IC50, EC50, GI50... by genetic entity type
     * @param id
     * @throws DaoException 
     */
    private static void deleteTreatmentGeneticProfiles() throws DaoException {
        executeSQLstatment(
            SqlAction.DELETE,
            "DELETE FROM genetic_profile WHERE GENETIC_ALTERATION_TYPE = 'TREATMENT'"
        );
    }
    
    /**
     * Deletes genetic_profile_link records which are pointing to a profile of type to TREATMENT genetic_alteration_type
     * @throws DaoException 
     */
	private static void deleteTreatmentGeneticProfileLinks() throws DaoException {
		executeSQLstatment(
            SqlAction.DELETE,
            "DELETE FROM genetic_profile_link WHERE REFERRED_GENETIC_PROFILE_ID IN "
        	+ "(SELECT GENETIC_PROFILE_ID FROM genetic_profile WHERE GENETIC_ALTERATION_TYPE = 'TREATMENT')");
	}    

    /**
     * Deletes all records from 'treatment' table in database and records in related tables.
     * @throws DaoException 
     */
    private static void deleteAllTreatmentRecords() throws DaoException {
        executeSQLstatment(
            SqlAction.DELETE,
            "DELETE FROM treatment"
        );
    }
    
    /**
     * Deletes all records from 'treatment' table in database and records in related tables.
     * @throws DaoException 
     */
    public static void deleteAllRecords() throws DaoException {
    	deleteAllTreatmentRecords();
    	deleteTreatmentGeneticProfileLinks();
    	deleteTreatmentGeneticProfiles();
    	deleteTreatmentGeneticEntityRecords();
    }

    /**
     * Extracts Geneset record from ResultSet.
     * @param rs
     * @return Geneset record
     * @throws SQLException
     * @throws DaoException 
     */
    private static Treatment extractTreatment(ResultSet rs) throws SQLException, DaoException {

        Integer id = rs.getInt("ID");
        String stableId = rs.getString("STABLE_ID");
        String name = rs.getString("NAME");
        String description = rs.getString("DESCRIPTION");
        String refLink = rs.getString("LINKOUT_URL");
        Integer geneticEntityId = rs.getInt("GENETIC_ENTITY_ID");
        
        Treatment treatment = new Treatment(id, geneticEntityId, stableId, name, description, refLink);

        return treatment;
    }

    /**
     * Helper method for retrieval of a Treatment record from the database
     * @param action type of MySQL operation
     * @param statement MySQL statement
     * @param keys Series of values used in the statement (order is important)
     * @return Object return data from 
     * @throws DaoException 
     */
    private static DbContainer executeSQLstatment(SqlAction action, String statement, String ... keys) throws DaoException {
        return executeSQLstatment(action, statement, null, keys);
    }
    
    /**
     * Helper method for retrieval of a Treatment record from the database
     * @param action type of MySQL operation
     * @param statement MySQL statement
     * @param geneticEntityId Database identifier of the genetic entity entry linked to the Treatment
     * @param keys Series of values used in the statement (order is important)
     * @return Object return data from 
     * @throws DaoException 
     */
    private static DbContainer executeSQLstatment(SqlAction action, String statement, Integer geneticEntityId, String ... keys) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;        
        try {
            con = JdbcUtil.getDbConnection(DaoTreatment.class);

            // for insert statements the number of affected records is returned
            // this requires the RETURN_GENERATED_KEYS to be set for insert statements.
            int switchGetGeneratedKeys = PreparedStatement.NO_GENERATED_KEYS;
            if (action == SqlAction.INSERT)
                switchGetGeneratedKeys = PreparedStatement.RETURN_GENERATED_KEYS;

            pstmt = con.prepareStatement(statement, switchGetGeneratedKeys);

            int cnt = 1;
            if (geneticEntityId != null) {
                pstmt.setInt(cnt++, geneticEntityId);
            }
            for (int i = 0; i < keys.length; i++)
                pstmt.setString(cnt++, keys[i]);

            switch(action) {
                case INSERT:
                    pstmt.executeUpdate();
                    rs = pstmt.getGeneratedKeys();
                    rs.next();
                    return new DbContainer(rs.getInt(1));
                case SELECT:
                    rs = pstmt.executeQuery();
                    if (rs.next()) {
                        return new DbContainer(extractTreatment(rs));
                    }
                    return new DbContainer();
                default:
                    pstmt.executeUpdate();
                    return null;
            }
            
        }
        catch (SQLException e) {
            throw new DaoException(e);
        } 
        finally {
            JdbcUtil.closeAll(DaoTreatment.class, con, pstmt, rs);
        }
    }

    final static class DbContainer {
        private int id;
        private Treatment treatment;

        public DbContainer(){
        }

        public DbContainer(int id){
            this.id = id;
        }
        
        public DbContainer(Treatment treatment) {
            this.treatment = treatment;
        }
        
        /**
         * @return the treatment
         */
        public Treatment getTreatment() {
            return treatment;
        }

        /**
         * @return the id
         */
        public int getId() {
            return id;
        }
        
    }

}