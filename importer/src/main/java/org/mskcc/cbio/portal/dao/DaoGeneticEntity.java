package org.mskcc.cbio.portal.dao;

import org.mskcc.cbio.portal.model.shared.GeneticEntity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class DaoGeneticEntity {

    /**
     * Private Constructor to enforce Singleton Pattern.
     */
    private DaoGeneticEntity() {
    }

    private enum SqlAction {
        UPDATE, SELECT, DELETE
    }

    /**
     * Adds a new genetic entity Record to the Database and returns GeneticEntity
     *
     * @param GeneticEntity : GeneticEntity, can only contains the entity type
     * @return : GeneticEntity created
     * @throws DaoException Database Error.
     */

    public static GeneticEntity addNewGeneticEntity(GeneticEntity geneticEntity) throws DaoException {

        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGeneticEntity.class);
            long entityId = ClickHouseAutoIncrement.nextId("seq_genetic_entity");
            pstmt = con.prepareStatement("INSERT INTO genetic_entity (`id`, `entity_type`, `stable_id`) "
                    + "VALUES(?,?,?)");
            pstmt.setLong(1, entityId);
            pstmt.setString(2, geneticEntity.getEntityType());
            pstmt.setString(3, geneticEntity.getStableId());
            pstmt.executeUpdate();
            geneticEntity.setId((int) entityId);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGeneticEntity.class, con, pstmt, null);
        }

        return geneticEntity;
    }

    /**
     * Given an external id, returns a GeneticEntity record.
     * @param stableId
     * @return GeneticEntity record
     * @throws DaoException 
     */
    public static GeneticEntity getGeneticEntityByStableId(String stableId) throws DaoException {
        DbContainer container = executeSQLstatment(SqlAction.SELECT, "SELECT * FROM genetic_entity WHERE `stable_id` = ?", stableId);
        return container.getGeneticEntity();
    }
    
    /**
     * Get GeneticEntity record.
     * @param id genetic_entity id
     */
    public static GeneticEntity getGeneticEntityById(int id) throws DaoException {
        DbContainer container = executeSQLstatment(SqlAction.SELECT, "SELECT * FROM genetic_entity WHERE id = ?", String.valueOf(id));
        return container.getGeneticEntity();
    }

    /**
     * Get all GeneticEntity record.
     */
    public static List<GeneticEntity> getAllGeneticEntities() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGeneticEntity.class);
            pstmt = con.prepareStatement
                ("SELECT * FROM genetic_entity");
            rs = pstmt.executeQuery();
            List<GeneticEntity> geneticEntities = new ArrayList<>();
            while (rs.next()) {
                geneticEntities.add(extractGeneticEntity(rs));
            }
            return geneticEntities;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGeneticEntity.class, con, pstmt, rs);
        }
    }

    /**
     * Extracts Geneset record from ResultSet.
     * @param rs
     * @return Geneset record
     * @throws SQLException
     * @throws DaoException 
     */
    private static GeneticEntity extractGeneticEntity(ResultSet rs) throws SQLException, DaoException {

        Integer id = rs.getInt("id");
        String stableId = rs.getString("stable_id");
        String entityType = rs.getString("entity_type");
        
        GeneticEntity geneticEntity = new GeneticEntity(id, entityType, stableId);

        return geneticEntity;
    }
    
    /**
     * Helper method for retrieval of a geneticEntity record from the database
     * @param action type of SQL operation
     * @param statement SQL statement
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

            pstmt = con.prepareStatement(statement);

            int cnt = 1;
            for (int i = 0; i < keys.length; i++)
                pstmt.setString(cnt++, keys[i]);

            switch(action) {
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
