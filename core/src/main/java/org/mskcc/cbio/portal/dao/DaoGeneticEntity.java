package org.mskcc.cbio.portal.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.cbioportal.model.EntityType;
import org.cbioportal.model.GeneticEntity;

public class DaoGeneticEntity {

    /**
     * Private Constructor to enforce Singleton Pattern.
     */
    private DaoGeneticEntity() {
    }

    private enum SqlAction {
        INSERT, UPDATE, SELECT, DELETE
    }

    // TODO: consider renaming this class to something something similar to
    // DaoEntity
    // to reflect the fact that there are also entities that are "not genetic", like
    // for example the new entity of type "TREATMENT".
    // see: https://github.com/cBioPortal/cbioportal/pull/5460#discussion_r250148672

    /**
     * Adds a new genetic entity Record to the Database and returns the auto
     * generated id value.
     *
     * @param entityType : one of ...
     * @return : auto generated genetic entity id value
     * @throws DaoException Database Error.
     */
    public static int addNewGeneticEntity(EntityType entityType) throws DaoException {

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGene.class);
        	pstmt = con.prepareStatement
                    ("INSERT INTO genetic_entity (`ENTITY_TYPE`) "
                            + "VALUES (?)", Statement.RETURN_GENERATED_KEYS);
        	pstmt.setString(1, entityType.name());
            pstmt.executeUpdate();
            //get the auto generated key:
            rs = pstmt.getGeneratedKeys();
            rs.next();
            int newId = rs.getInt(1);
            return newId;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGene.class, con, pstmt, rs);
        }
    }

    public static GeneticEntity addNewGeneticEntity(GeneticEntity geneticEntity) throws DaoException {

        DbContainer container = executeSQLstatment(
            SqlAction.INSERT,
            "INSERT INTO genetic_entity (`ENTITY_TYPE`, `STABLE_ID`) "
            + "VALUES(?,?)",
            geneticEntity.getEntityType(),
            geneticEntity.getStableId()
        );

        geneticEntity.setId(container.getId());

        return geneticEntity;
    }

    /**
     * Given an external id, returns a GeneticEntity record.
     * @param stableId
     * @return GeneticEntity record
     * @throws DaoException 
     */
    public static GeneticEntity getGeneticEntityByStableId(String stableId) throws DaoException {
        DbContainer container = executeSQLstatment(SqlAction.SELECT, "SELECT * FROM genetic_entity WHERE `STABLE_ID` = ?", stableId);
        return container.getGeneticEntity();
    }
    
    /**
     * Get GeneticEntity record.
     * @param id genetic_entity id
     */
    public static GeneticEntity getGeneticEntityById(int id) throws DaoException {
        DbContainer container = executeSQLstatment(SqlAction.SELECT, "SELECT * FROM genetic_entity WHERE ID = ?", String.valueOf(id));
        return container.getGeneticEntity();
    }

    /**
     * Update GeneticEntity record.
     * @param geneticEntity GeneticEntity
     */
    public static void updateGeneticEntity(GeneticEntity geneticEntity) throws DaoException {

        executeSQLstatment(
            SqlAction.UPDATE,
            "UPDATE genetic_entity SET `ENTITY_TYPE` = ? WHERE `STABLE_ID` = ?", 
            geneticEntity.getEntityType(),
            geneticEntity.getStableId()
        );

    }

    /**
     * Extracts Geneset record from ResultSet.
     * @param rs
     * @return Geneset record
     * @throws SQLException
     * @throws DaoException 
     */
    private static GeneticEntity extractGeneticEntity(ResultSet rs) throws SQLException, DaoException {

        Integer id = rs.getInt("ID");
        String stableId = rs.getString("STABLE_ID");
        String entityType = rs.getString("ENTITY_TYPE");
        
        GeneticEntity geneticEntity = new GeneticEntity(id, entityType, stableId);

        return geneticEntity;
    }
    
    /**
     * Helper method for retrieval of a geneticEntity record from the database
     * @param action type of MySQL operation
     * @param statement MySQL statement
     * @param keys Series of values used in the statement (order is important)
     * @return Object return data from 
     * @throws DaoException 
     */
    private static DbContainer executeSQLstatment(SqlAction action, String statement, String ... keys) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;        
        try {
            con = JdbcUtil.getDbConnection(DaoGeneticEntity.class);

            // for insert statements the number of affected records is returned
            // this requires the RETURN_GENERATED_KEYS to be set for insert statements.
            int switchGetGeneratedKeys = PreparedStatement.NO_GENERATED_KEYS;
            if (action == SqlAction.INSERT)
                switchGetGeneratedKeys = PreparedStatement.RETURN_GENERATED_KEYS;

            pstmt = con.prepareStatement(statement, switchGetGeneratedKeys);

            int cnt = 1;
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
                        return new DbContainer(extractGeneticEntity(rs));
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
            JdbcUtil.closeAll(DaoGeneticEntity.class, con, pstmt, rs);
        }
    }

    final static class DbContainer {
        private int id;
        private GeneticEntity geneticEntity;

        public DbContainer(){
        }

        public DbContainer(int id){
            this.id = id;
        }
        
        public DbContainer(GeneticEntity geneticEntity) {
            this.geneticEntity = geneticEntity;
        }
        
        /**
         * @return the geneticEntity
         */
        public GeneticEntity getGeneticEntity() {
            return geneticEntity;
        }

        /**
         * @return the id
         */
        public int getId() {
            return id;
        }
        
    }
}
