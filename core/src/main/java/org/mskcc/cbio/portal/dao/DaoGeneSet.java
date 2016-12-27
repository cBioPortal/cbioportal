/*
 * Copyright (c) 2016 Memorial Sloan-Kettering Cancer Center.
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

package org.mskcc.cbio.portal.dao;

import org.cbioportal.model.GeneSet;
import org.mskcc.cbio.portal.model.CanonicalGene;

import java.sql.*;
import java.util.*;

public class DaoGeneSet {
    
    private static DaoGeneSet daoGeneSet = null;
    private static DaoGeneOptimized daoGene = null;
    
    private DaoGeneSet() {
        daoGene = DaoGeneOptimized.getInstance();
    }
    
    /**
     * Gets instance of DaoGeneSet (singleton pattern). 
     * @return DaoGeneSet
     * @throws DaoException
     */
    public static DaoGeneSet getInstance() throws DaoException {
        if (daoGeneSet == null) {
            daoGeneSet = new DaoGeneSet();
        }
        return daoGeneSet;
    }
    
    /**
     * Adds a new GeneSet record to the database.
     * @param geneSet
     * @return number of records successfully added
     * @throws DaoException 
     */
    public int addGeneSet(GeneSet geneSet) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            int rows = 0;
            
            // new geneset so add genetic entity first
            int geneticEntityId = DaoGeneticEntity.addNewGeneticEntity(DaoGeneticEntity.EntityTypes.GENE_SET);
            geneSet.setGeneticEntityId(geneticEntityId);
            
            con = JdbcUtil.getDbConnection(DaoGeneSet.class);
            pstmt = con.prepareStatement("INSERT INTO geneset " 
                    + "(`ID`, `GENETIC_ENTITY_ID`, `EXTERNAL_ID`, `NAME_SHORT`, `NAME`, `REF_LINK`, `VERSION`) "
                    + "VALUES(?,?,?,?,?,?,?)");
            pstmt.setInt(1, geneSet.getId());
            pstmt.setInt(2, geneSet.getGeneticEntityId());
            pstmt.setString(3, geneSet.getExternalId());
            pstmt.setString(4,geneSet.getNameShort());
            pstmt.setString(5,geneSet.getName());
            pstmt.setString(6, geneSet.getRefLink());
            pstmt.setString(7, geneSet.getVersion());
            rows += pstmt.executeUpdate();
            
            // add geneset genes
            rows += addGeneSetGenes(geneSet);
            
            return rows;
        }
        catch (SQLException e) {
            throw new DaoException(e);
        } 
        finally {
            JdbcUtil.closeAll(DaoGene.class, con, pstmt, rs);
        }        
    }
    
    /**
     * Adds list of Gene records from GeneSet object to database.
     * @param geneSet
     * @return number of records successfully added
     * @throws DaoException 
     */
    public int addGeneSetGenes(GeneSet geneSet) throws DaoException {
        if (MySQLbulkLoader.isBulkLoad()) {
            // write to temp file maintained by the MySQLbulkLoader
            List<Integer> entrezGeneIds = geneSet.getGenesetGenes();
            for (Integer entrezGeneId : entrezGeneIds) {
                MySQLbulkLoader.getMySQLbulkLoader("geneset_gene").insertRecord(
                        Integer.toString(geneSet.getId()),
                        Integer.toString(entrezGeneId)
                );
            }
            // return 1 because normal insert will return 1 if no error occurs
            return 1;            
        }
        
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGeneSet.class);
            List<Integer> entrezGeneIds = geneSet.getGenesetGenes();
            int rows = 0;
            for (Integer entrezGeneId : entrezGeneIds) {
                pstmt = con.prepareStatement("INSERT INTO geneset_gene "
                        + "(`GENESET_ID`, `ENTREZ_GENE_ID`)"
                        + "VALUES(?,?)");
                pstmt.setInt(1, geneSet.getId());
                pstmt.setInt(2, entrezGeneId);
                rows += pstmt.executeUpdate();
            }
            
            return rows;
        }
        catch (SQLException e) {
            throw new DaoException(e);
        } 
        finally {
            JdbcUtil.closeAll(DaoGene.class, con, pstmt, rs);
        }
    }
    
    /**
     * Given an external id, returns a GeneSet record.
     * @param externalId
     * @return GeneSet record
     * @throws DaoException 
     */
    public GeneSet getGeneSetByExternalId(String externalId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;        
        try {
            con = JdbcUtil.getDbConnection(DaoGeneSet.class);
            pstmt = con.prepareStatement("SELECT * FROM geneset WHERE `EXTERNAL_ID` = ?");
            pstmt.setString(1, externalId);
            rs = pstmt.executeQuery();
            
            // return null if result set is empty
            if (rs.next()) {
                return extractGeneSet(rs);
            }
            else {
                return null;
            }
        }
        catch (SQLException e) {
            throw new DaoException(e);
        } 
        finally {
            JdbcUtil.closeAll(DaoGene.class, con, pstmt, rs);
        }
    }

    /**
     * Given a GeneSet record, returns list of CanonicalGene records.
     * @param geneSet
     * @return list of geneset genes
     * @throws DaoException 
     */
    public List<CanonicalGene> getGeneSetGenes(GeneSet geneSet) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGeneSet.class);
            pstmt = con.prepareStatement("SELECT * FROM geneset_gene WHERE GENESET_ID = ?");
            pstmt.setInt(1, geneSet.getId());
            rs = pstmt.executeQuery();
            
            // get list of entrez gene ids for geneset record
            Set<Long> entrezGeneIds = new HashSet<>();
            while (rs.next()) {
                entrezGeneIds.add(rs.getLong("ENTREZ_GENE_ID"));
            }
            
            // get list of genes by entrez gene ids
            List<CanonicalGene> genes = new ArrayList();
            for (Long entrezGeneId : entrezGeneIds) {
                CanonicalGene gene = daoGene.getGene(entrezGeneId);
                genes.add(gene);
            }
            
            return genes;
        }
        catch (SQLException e) {
            throw new DaoException(e);
        } 
        finally {
            JdbcUtil.closeAll(DaoGene.class, con, pstmt, rs);
        }
    }
    
    /**
     * Extracts GeneSet record from ResultSet.
     * @param rs
     * @return GeneSet record
     * @throws SQLException
     * @throws DaoException 
     */
    private GeneSet extractGeneSet(ResultSet rs) throws SQLException, DaoException {
        Integer id = rs.getInt("ID");
        Integer geneticEntityId = rs.getInt("GENETIC_ENTITY_ID");
        String externalId = rs.getString("EXTERNAL_ID");
        String nameShort = rs.getString("NAME_SHORT");
        String name = rs.getString("NAME");
        String refLink = rs.getString("REF_LINK");
        String version = rs.getString("VERSION");
        
        GeneSet geneSet = new GeneSet();
        geneSet.setId(id);
        geneSet.setGeneticEntityId(geneticEntityId);
        geneSet.setExternalId(externalId);
        geneSet.setNameShort(nameShort);
        geneSet.setName(name);
        geneSet.setRefLink(refLink);
        geneSet.setVersion(version);
        
        return geneSet;
    }
    
    /**
     * Checks the usage of a geneset by genetic entity id.
     * @param geneticEntityId
     * @return boolean indicating whether geneset is in use by other studies
     * @throws DaoException 
     */
    public boolean checkUsage(Integer geneticEntityId) throws DaoException {
        String SQL = "SELECT COUNT(DISTINCT `CANCER_STUDY_ID`) FROM genetic_profile " +
                "WHERE `GENETIC_PROFILE_ID` IN (SELECT `GENETIC_PROFILE_ID` FROM genetic_alteration WHERE `GENETIC_ENTITY_ID` = ?)";
        
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGene.class);
            pstmt = con.prepareStatement(SQL);
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
            JdbcUtil.closeAll(DaoGene.class, con, pstmt, rs);
        }
    }
    
    public void updateGeneSet(GeneSet geneSet, boolean updateGeneSetGenes) throws DaoException {
        String SQL = "UPDATE geneset SET " + 
                "`NAME_SHORT` = ?, `NAME` = ?, `REF_LINK` = ?, `VERSION` = ? " +
                "WHERE `ID` = ?";
                
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGene.class);
            pstmt = con.prepareStatement(SQL);
            pstmt.setString(1, geneSet.getNameShort());
            pstmt.setString(2, geneSet.getName());
            pstmt.setString(3, geneSet.getRefLink());
            pstmt.setString(4, geneSet.getVersion());
            pstmt.setInt(5, geneSet.getId());
            pstmt.executeUpdate();

            // update geneset genes as well if indicated
            if (updateGeneSetGenes) {
                updateGeneSetGenes(geneSet);
            }            
        } 
        catch (SQLException e) {
            throw new DaoException(e);
        } 
        finally {
            JdbcUtil.closeAll(DaoGene.class, con, pstmt, rs);
        }
    }
    
    /**
     * Updates geneset genes for given geneset. 
     * @param geneSet
     * @throws DaoException 
     */
    public void updateGeneSetGenes(GeneSet geneSet) throws DaoException {
        // first delete existing genes for geneset
        deleteGeneSetGenes(geneSet.getId());
            
        // insert new geneset genes for given geneset
        addGeneSetGenes(geneSet);        
    }
    
    /**
     * Deletes a GeneSet record from geneset related tables:
     * geneset, geneset_gene, geneset_hierarchy_parent, geneset_hierarchy
     * @param id
     * @throws DaoException 
     */
    public void deleteGeneSetRecord(Integer id) throws DaoException {
        String[] SQLs = {"DELETE FROM geneset WHERE `ID` = ?", 
                                "DELETE FROM geneset_gene WHERE `GENESET_ID` = ?", 
                                "DELETE FROM geneset_hierarchy_parent WHERE `GENESET_ID` = ?"};        
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGene.class);
            for (String sql : SQLs) {
                pstmt = con.prepareStatement(sql);
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
            }
        } 
        catch (SQLException e) {
            throw new DaoException(e);
        } 
        finally {
            JdbcUtil.closeAll(DaoGene.class, con, pstmt, rs);
        }
    }
    
    /**
     * Deletes from geneset_genes all records associated with given geneset id.
     * @param id
     * @throws DaoException 
     */
    public void deleteGeneSetGenes(Integer id) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGene.class);
            pstmt = con.prepareStatement("DELETE FROM geneset_gene WHERE `GENESET_ID` = ?");
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } 
        catch (SQLException e) {
            throw new DaoException(e);
        } 
        finally {
            JdbcUtil.closeAll(DaoGene.class, con, pstmt, rs);
        }
    }
    
    /**
     * Deletes all records from 'geneset' table in database and records in related tables.
     * @throws DaoException 
     */
    public void deleteAllRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGeneSet.class);
            JdbcUtil.disableForeignKeyCheck(con);
            pstmt = con.prepareStatement("TRUNCATE TABLE geneset");
            pstmt.executeUpdate();
            JdbcUtil.enableForeignKeyCheck(con);
        }
        catch (SQLException e) {
            throw new DaoException(e);
        } 
        finally {
            JdbcUtil.closeAll(DaoGene.class, con, pstmt, rs);
        }
        deleteAllGeneSetGeneRecords();
        deleteAllGeneSetHierarchyRecords();
        deleteAllGeneSetHierarchyParentRecords();
    }

    /**
     * Deletes all records from 'geneset_gene' table in database.
     * @throws DaoException 
     */    
    public void deleteAllGeneSetGeneRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGeneSet.class);
            pstmt = con.prepareStatement("TRUNCATE TABLE geneset_gene");
            pstmt.executeUpdate();
        }
        catch (SQLException e) {
            throw new DaoException(e);
        } 
        finally {
            JdbcUtil.closeAll(DaoGene.class, con, pstmt, rs);
        }
    }
    
    /**
     * Deletes all records from 'geneset_hierarchy' table in database.
     * @throws DaoException 
     */
    public void deleteAllGeneSetHierarchyRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGeneSet.class);
            pstmt = con.prepareStatement("TRUNCATE TABLE geneset_hierarchy");
            pstmt.executeUpdate();
        }
        catch (SQLException e) {
            throw new DaoException(e);
        } 
        finally {
            JdbcUtil.closeAll(DaoGene.class, con, pstmt, rs);
        }
    }
    
    /**
     * Deletes all records from 'geneset_hierarchy_parent' table in database.
     * @throws DaoException 
     */
    public void deleteAllGeneSetHierarchyParentRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGeneSet.class);
            pstmt = con.prepareStatement("TRUNCATE TABLE geneset_hierarchy_parent");
            pstmt.executeUpdate();
        }
        catch (SQLException e) {
            throw new DaoException(e);
        } 
        finally {
            JdbcUtil.closeAll(DaoGene.class, con, pstmt, rs);
        }
    }
}

/**
 * Compares two genes by their HUGO Symbols--ignores case
 */
class GeneComparator implements Comparator {

    public int compare(Object o, Object o1) {
        CanonicalGene gene0 = (CanonicalGene) o;
        CanonicalGene gene1 = (CanonicalGene) o1;
        return (gene0.getHugoGeneSymbolAllCaps().compareTo(gene1.getHugoGeneSymbolAllCaps()));
    }
}
