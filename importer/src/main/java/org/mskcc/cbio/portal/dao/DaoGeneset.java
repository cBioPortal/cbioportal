/*
 * Copyright (c) 2016 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

/*
 * @author ochoaa
 * @author Sander Tan
*/

package org.mskcc.cbio.portal.dao;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.Geneset;
import org.mskcc.cbio.portal.model.shared.EntityType;
import org.mskcc.cbio.portal.model.shared.GeneticEntity;
import org.mskcc.cbio.portal.scripts.ImportGenesetData;
import org.mskcc.cbio.portal.util.ProgressMonitor;

public class DaoGeneset {

    private static final String GENESET_SEQUENCE = "seq_geneset";

	private DaoGeneset() {
	}

    /**
     * Adds a new Geneset record to the database.
     * @param geneset
     * @return number of records successfully added
     * @throws DaoException 
     */
    public static Geneset addGeneset(Geneset geneset) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            // new geneset so add genetic entity first
            GeneticEntity geneticEntity = DaoGeneticEntity.addNewGeneticEntity(new GeneticEntity(EntityType.GENESET.name()));
            int geneticEntityId = geneticEntity.getId();
            geneset.setGeneticEntityId(geneticEntityId);
            
            con = JdbcUtil.getDbConnection(DaoGeneset.class);
            long genesetId = ClickHouseAutoIncrement.nextId(GENESET_SEQUENCE);
            pstmt = con.prepareStatement("INSERT INTO geneset "
                    + "(`id`, `genetic_entity_id`, `external_id`, `name`, `description`, `ref_link`) "
                    + "VALUES(?,?,?,?,?,?)");
            pstmt.setLong(1, genesetId);
            pstmt.setInt(2, geneset.getGeneticEntityId());
            pstmt.setString(3, geneset.getExternalId());
            pstmt.setString(4, geneset.getName());
            pstmt.setString(5, geneset.getDescription());
            pstmt.setString(6, geneset.getRefLink());
            pstmt.executeUpdate();
            geneset.setId((int) genesetId);
            
            return geneset;
        }
        catch (SQLException e) {
            throw new DaoException(e);
        } 
        finally {
            JdbcUtil.closeAll(DaoGeneset.class, con, pstmt, rs);
        }        
    }
    
    /**
     * Prepares a list of Gene records from Geneset object to be added to database via ClickHouseBulkLoader.
     * @param geneset
     * @return number of records where entrez gene id is found in db
     */
    public static int addGenesetGenesToBulkLoader(Geneset geneset) {
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
        
        Set<Long> entrezGeneIds = geneset.getGenesetGeneIds();
        int rows = 0;
        for (Long entrezGeneId : entrezGeneIds) {
            //validate:
            if (daoGeneOptimized.getGene(entrezGeneId.intValue()) == null) {
                //throw error with clear message:
                ProgressMonitor.logWarning(geneset.getExternalId() + " contains Entrez gene ID not found in local gene table: " + entrezGeneId);
                ImportGenesetData.skippedGenes++;
                continue;
            }
            // use this code if bulk loading
            // write to the temp file maintained by the ClickHouseBulkLoader
            ClickHouseBulkLoader.getClickHouseBulkLoader("geneset_gene").insertRecord(
                    Integer.toString(geneset.getId()),
                    Long.toString(entrezGeneId));
            rows ++;
        }
        return rows;
    }
    
    /**
     * Given a Geneset record, returns list of CanonicalGene records.
     * @param geneset
     * @return list of geneset genes
     * @throws DaoException 
     */
    public static List<CanonicalGene> getGenesetGenes(Geneset geneset) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
    	DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();

        try {
            con = JdbcUtil.getDbConnection(DaoGeneset.class);
            pstmt = con.prepareStatement("SELECT * FROM geneset_gene WHERE geneset_id = ?");
            pstmt.setInt(1, geneset.getId());
            rs = pstmt.executeQuery();
            
            // get list of entrez gene ids for geneset record
            Set<Long> entrezGeneIds = new HashSet<Long>();
            while (rs.next()) {
                entrezGeneIds.add(rs.getLong("entrez_gene_id"));
            }
            
            // get list of genes by entrez gene ids
            List<CanonicalGene> genes = new ArrayList<CanonicalGene>();
            for (Long entrezGeneId : entrezGeneIds) {
                CanonicalGene gene = daoGeneOptimized.getGene(entrezGeneId);
                genes.add(gene);
            }
            
            return genes;
        }
        catch (SQLException e) {
            throw new DaoException(e);
        } 
        finally {
            JdbcUtil.closeAll(DaoGeneset.class, con, pstmt, rs);
        }
    }
    
    /**
     * Given an external id, returns a Geneset record.
     * @param externalId
     * @return Geneset record
     * @throws DaoException 
     */
    public static Geneset getGenesetByExternalId(String externalId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;        
        try {
            con = JdbcUtil.getDbConnection(DaoGeneset.class);
            pstmt = con.prepareStatement("SELECT * FROM geneset WHERE `external_id` = ?");
            pstmt.setString(1, externalId);
            rs = pstmt.executeQuery();
            
            // return null if result set is empty
            if (rs.next()) {
                return extractGeneset(rs);
            }
            else {
                return null;
            }
        }
        catch (SQLException e) {
            throw new DaoException(e);
        } 
        finally {
            JdbcUtil.closeAll(DaoGeneset.class, con, pstmt, rs);
        }
    }
    
    /**
     * Get list of all genesets
     */
    public static List<Geneset> getAllGenesets() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Geneset> result = new ArrayList<>();
        try {
            con = JdbcUtil.getDbConnection(DaoGeneset.class);
            pstmt = con.prepareStatement("SELECT * FROM geneset");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                result.add(extractGeneset(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGeneset.class, con, pstmt, rs);
        }
    }
    
    /**
     * Extracts Geneset record from ResultSet.
     * @param rs
     * @return Geneset record
     * @throws SQLException
     * @throws DaoException 
     */
    private static Geneset extractGeneset(ResultSet rs) throws SQLException, DaoException {
        Integer id = rs.getInt("id");
        Integer geneticEntityId = rs.getInt("genetic_entity_id");
        String externalId = rs.getString("external_id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        String refLink = rs.getString("ref_link");
        
        Geneset geneset = new Geneset();
        geneset.setId(id);
        geneset.setGeneticEntityId(geneticEntityId);
        geneset.setExternalId(externalId);
        geneset.setName(name);
        geneset.setDescription(description);
        geneset.setRefLink(refLink);
        List<CanonicalGene> genesetGenes = getGenesetGenes(geneset);
        if (genesetGenes != null && genesetGenes.size() > 0) {
            geneset.setGenesetGenes(genesetGenes.stream().map(g -> g.getEntrezGeneId()).collect(Collectors.toSet()));
        }     
        return geneset;
    }
    
    public static Set<Long> getGenesetGeneticEntityIds() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGeneset.class);
            pstmt = con.prepareStatement("SELECT id FROM genetic_entity WHERE entity_type = 'geneset'");
            rs = pstmt.executeQuery();
            
            Set<Long> geneticEntities = new HashSet<Long>();
            while (rs.next()) {
            	geneticEntities.add(rs.getLong("id"));
            }
            return geneticEntities;
            
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGeneset.class, con, pstmt, rs);
        }
    }  
    
    /**
     * Checks the usage of a geneset by genetic entity id.
     * @param geneticEntityId
     * @return boolean indicating whether geneset is in use by other studies
     * @throws DaoException 
     */
    public static boolean checkUsage(Integer geneticEntityId) throws DaoException {
        String SQL = "SELECT count(DISTINCT `cancer_study_id`) FROM genetic_profile " +
                "WHERE `genetic_profile_id` IN (SELECT `genetic_profile_id` FROM genetic_alteration WHERE `genetic_entity_id` = ?)";
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGeneset.class);
            pstmt = con.prepareStatement(SQL);
            pstmt.setInt(1, geneticEntityId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1)>0;
            }
            return false;
        } 
        catch (SQLException e) {
            throw new DaoException(e);
        } 
        finally {
            JdbcUtil.closeAll(DaoGeneset.class, con, pstmt, rs);
        }
    }
    
    public static void updateGeneset(Geneset geneset, boolean updateGenesetGenes) throws DaoException {
        String SQL = "UPDATE geneset SET " + 
                "`name` = ?, `description` = ?, `ref_link` = ?" +
                "WHERE `id` = ?";
                
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGeneset.class);
            pstmt = con.prepareStatement(SQL);
            pstmt.setString(1, geneset.getName());
            pstmt.setString(2, geneset.getDescription());
            pstmt.setString(3, geneset.getRefLink());
            pstmt.setInt(4, geneset.getId());
            pstmt.executeUpdate();

            // We decided that when updating genesets, it's not a good idea to update genes it contains, because
            // in that case, data could be still from old version of the geneset.
            // A solution would be to update geneset database to new version, and include new genes in that.
        } 
        catch (SQLException e) {
            throw new DaoException(e);
        } 
        finally {
            JdbcUtil.closeAll(DaoGeneset.class, con, pstmt, rs);
        }
    }
    
    /**
     * Delete Geneset genetic entity records.
     */
    private static void deleteGenesetGeneticEntityRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGeneset.class);
            pstmt = con.prepareStatement("DELETE FROM genetic_entity WHERE entity_type = 'geneset'");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGeneset.class, con, pstmt, rs);
        }
    }   
    
    /**
     * Deletes Geneset data such as GSVA Scores and Pvalues by genetic entity id
     * @param id
     * @throws DaoException 
     */
    private static void deleteGenesetGeneticProfiles() throws DaoException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
        	connection = JdbcUtil.getDbConnection(DaoGeneset.class);
        	
        	// Prepare statement
        	preparedStatement = connection.prepareStatement("DELETE FROM genetic_profile WHERE genetic_alteration_type = 'geneset_score'");

            // Execute statement
            preparedStatement.executeUpdate();
        } 
        catch (SQLException e) {
            throw new DaoException(e);
        } 
        finally {
            JdbcUtil.closeAll(DaoGeneset.class, connection, preparedStatement, resultSet);
        }
    }
    
    /**
     * Deletes genetic_profile_link records which are pointing to a profile of type to GENESET_SCORE
     * @throws DaoException 
     */
	private static void deleteGenesetGeneticProfileLinks() throws DaoException {
		Connection connection = null;
        try {
        	connection = JdbcUtil.getDbConnection(DaoGeneset.class);
            List<Integer> genesetProfiles = collectGenesetProfileIds(connection);
            if (genesetProfiles.isEmpty()) {
                return;
            }
            ClickHouseBulkDeleter.getBulkDeleter("genetic_profile_link", "referred_genetic_profile_id").addIds(genesetProfiles);
            ClickHouseBulkDeleter.flushAll();
        }
        catch (SQLException e) {
            throw new DaoException(e);
        }
        finally {
            JdbcUtil.closeAll(DaoGeneset.class, connection, null, null);
        }
	}

    private static List<Integer> collectGenesetProfileIds(Connection connection) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        try {
            preparedStatement = connection.prepareStatement(
                "SELECT genetic_profile_id FROM genetic_profile WHERE genetic_alteration_type = 'GENESET_SCORE'");
            rs = preparedStatement.executeQuery();
            while (rs.next()) {
                ids.add(rs.getInt(1));
            }
        } finally {
            JdbcUtil.closeAll(DaoGeneset.class, null, preparedStatement, rs);
        }
        return ids;
    }

    /**
     * Deletes all records from 'geneset' table in database and records in related tables.
     * @throws DaoException 
     */
    private static void deleteAllGenesetRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGeneset.class);

            pstmt = con.prepareStatement("TRUNCATE TABLE geneset");
            pstmt.executeUpdate();
        }
        catch (SQLException e) {
            throw new DaoException(e);
        } 
        finally {
            JdbcUtil.closeAll(DaoGeneset.class, con, pstmt, rs);
        }
    }
    
    /**
     * Deletes all records from 'geneset' table in database and records in related tables.
     * @throws DaoException 
     */
    public static void deleteAllRecords() throws DaoException {
    	deleteAllGenesetRecords();
    	deleteGenesetGeneticProfileLinks();
    	deleteGenesetGeneticProfiles();
    	deleteGenesetGeneticEntityRecords();
    	DaoGenesetHierarchyNode.deleteAllGenesetHierarchyRecords();
    }

}
