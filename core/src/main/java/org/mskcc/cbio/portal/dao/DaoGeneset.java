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

/*
 * @author ochoaa
 * @author Sander Tan
 */

package org.mskcc.cbio.portal.dao;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;
import org.cbioportal.model.EntityType;
import org.cbioportal.model.GeneticEntity;
import org.mskcc.cbio.portal.dao.DaoGenesetHierarchyNode;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.Geneset;
import org.mskcc.cbio.portal.scripts.ImportGenesetData;
import org.mskcc.cbio.portal.util.ProgressMonitor;

public class DaoGeneset {

    private DaoGeneset() {}

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
            GeneticEntity geneticEntity = DaoGeneticEntity.addNewGeneticEntity(
                new GeneticEntity(EntityType.GENESET.name())
            );
            int geneticEntityId = geneticEntity.getId();
            geneset.setGeneticEntityId(geneticEntityId);

            con = JdbcUtil.getDbConnection(DaoGeneset.class);
            pstmt =
                con.prepareStatement(
                    "INSERT INTO geneset " +
                    "(`GENETIC_ENTITY_ID`, `EXTERNAL_ID`, `NAME`, `DESCRIPTION`, `REF_LINK`) " +
                    "VALUES(?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS
                );
            pstmt.setInt(1, geneset.getGeneticEntityId());
            pstmt.setString(2, geneset.getExternalId());
            pstmt.setString(3, geneset.getName());
            pstmt.setString(4, geneset.getDescription());
            pstmt.setString(5, geneset.getRefLink());
            pstmt.executeUpdate();
            //get the auto generated key:
            rs = pstmt.getGeneratedKeys();
            rs.next();
            int newId = rs.getInt(1);
            geneset.setId(newId);

            return geneset;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGeneset.class, con, pstmt, rs);
        }
    }

    /**
     * Prepares a list of Gene records from Geneset object to be added to database via MySQLbulkLoader.
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
                ProgressMonitor.logWarning(
                    geneset.getExternalId() +
                    " contains Entrez gene ID not found in local gene table: " +
                    entrezGeneId
                );
                ImportGenesetData.skippedGenes++;
                continue;
            }
            // use this code if bulk loading
            // write to the temp file maintained by the MySQLbulkLoader
            MySQLbulkLoader
                .getMySQLbulkLoader("geneset_gene")
                .insertRecord(
                    Integer.toString(geneset.getId()),
                    Long.toString(entrezGeneId)
                );
            rows++;
        }
        return rows;
    }

    /**
     * Given a Geneset record, returns list of CanonicalGene records.
     * @param geneset
     * @return list of geneset genes
     * @throws DaoException
     */
    public static List<CanonicalGene> getGenesetGenes(Geneset geneset)
        throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();

        try {
            con = JdbcUtil.getDbConnection(DaoGeneset.class);
            pstmt =
                con.prepareStatement(
                    "SELECT * FROM geneset_gene WHERE GENESET_ID = ?"
                );
            pstmt.setInt(1, geneset.getId());
            rs = pstmt.executeQuery();

            // get list of entrez gene ids for geneset record
            Set<Long> entrezGeneIds = new HashSet<Long>();
            while (rs.next()) {
                entrezGeneIds.add(rs.getLong("ENTREZ_GENE_ID"));
            }

            // get list of genes by entrez gene ids
            List<CanonicalGene> genes = new ArrayList<CanonicalGene>();
            for (Long entrezGeneId : entrezGeneIds) {
                CanonicalGene gene = daoGeneOptimized.getGene(entrezGeneId);
                genes.add(gene);
            }

            return genes;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGeneset.class, con, pstmt, rs);
        }
    }

    /**
     * Given an external id, returns a Geneset record.
     * @param externalId
     * @return Geneset record
     * @throws DaoException
     */
    public static Geneset getGenesetByExternalId(String externalId)
        throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGeneset.class);
            pstmt =
                con.prepareStatement(
                    "SELECT * FROM geneset WHERE `EXTERNAL_ID` = ?"
                );
            pstmt.setString(1, externalId);
            rs = pstmt.executeQuery();

            // return null if result set is empty
            if (rs.next()) {
                return extractGeneset(rs);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGeneset.class, con, pstmt, rs);
        }
    }

    /**
     * Get Geneset record.
     */
    public Geneset getGenesetById(int genesetId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGeneset.class);
            pstmt = con.prepareStatement("SELECT * FROM geneset WHERE ID = ?");
            pstmt.setInt(1, genesetId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractGeneset(rs);
            } else {
                return null;
            }
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
    private static Geneset extractGeneset(ResultSet rs)
        throws SQLException, DaoException {
        Integer id = rs.getInt("ID");
        Integer geneticEntityId = rs.getInt("GENETIC_ENTITY_ID");
        String externalId = rs.getString("EXTERNAL_ID");
        String name = rs.getString("NAME");
        String description = rs.getString("DESCRIPTION");
        String refLink = rs.getString("REF_LINK");

        Geneset geneset = new Geneset();
        geneset.setId(id);
        geneset.setGeneticEntityId(geneticEntityId);
        geneset.setExternalId(externalId);
        geneset.setName(name);
        geneset.setDescription(description);
        geneset.setRefLink(refLink);
        List<CanonicalGene> genesetGenes = getGenesetGenes(geneset);
        if (genesetGenes != null && genesetGenes.size() > 0) {
            geneset.setGenesetGenes(
                genesetGenes
                    .stream()
                    .map(g -> g.getEntrezGeneId())
                    .collect(Collectors.toSet())
            );
        }
        return geneset;
    }

    public static Set<Long> getGenesetGeneticEntityIds() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGeneset.class);
            pstmt =
                con.prepareStatement(
                    "SELECT ID FROM genetic_entity WHERE ENTITY_TYPE = 'GENESET'"
                );
            rs = pstmt.executeQuery();

            Set<Long> geneticEntities = new HashSet<Long>();
            while (rs.next()) {
                geneticEntities.add(rs.getLong("ID"));
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
    public static boolean checkUsage(Integer geneticEntityId)
        throws DaoException {
        String SQL =
            "SELECT COUNT(DISTINCT `CANCER_STUDY_ID`) FROM genetic_profile " +
            "WHERE `GENETIC_PROFILE_ID` IN (SELECT `GENETIC_PROFILE_ID` FROM genetic_alteration WHERE `GENETIC_ENTITY_ID` = ?)";
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGeneset.class);
            pstmt = con.prepareStatement(SQL);
            pstmt.setInt(1, geneticEntityId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGeneset.class, con, pstmt, rs);
        }
    }

    public static void updateGeneset(
        Geneset geneset,
        boolean updateGenesetGenes
    )
        throws DaoException {
        String SQL =
            "UPDATE geneset SET " +
            "`NAME` = ?, `DESCRIPTION` = ?, `REF_LINK` = ?" +
            "WHERE `ID` = ?";

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
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGeneset.class, con, pstmt, rs);
        }
    }

    /**
     * Delete Geneset genetic entity records.
     */
    private static void deleteGenesetGeneticEntityRecords()
        throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGeneset.class);
            pstmt =
                con.prepareStatement(
                    "DELETE FROM genetic_entity WHERE ENTITY_TYPE = 'GENESET'"
                );
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
            preparedStatement =
                connection.prepareStatement(
                    "DELETE FROM genetic_profile WHERE GENETIC_ALTERATION_TYPE = 'GENESET_SCORE'"
                );

            // Execute statement
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(
                DaoGeneset.class,
                connection,
                preparedStatement,
                resultSet
            );
        }
    }

    /**
     * Deletes genetic_profile_link records which are pointing to a profile of type to GENESET_SCORE
     * @throws DaoException
     */
    private static void deleteGenesetGeneticProfileLinks() throws DaoException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = JdbcUtil.getDbConnection(DaoGeneset.class);

            // Prepare statement: DELETE genetic_profile_link which are pointing to a profile of type to GENESET_SCORE
            preparedStatement =
                connection.prepareStatement(
                    "DELETE FROM genetic_profile_link WHERE REFERRED_GENETIC_PROFILE_ID IN " +
                    "(SELECT GENETIC_PROFILE_ID FROM genetic_profile WHERE GENETIC_ALTERATION_TYPE = 'GENESET_SCORE')"
                );

            // Execute statement
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(
                DaoGeneset.class,
                connection,
                preparedStatement,
                resultSet
            );
        }
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

            pstmt = con.prepareStatement("DELETE FROM geneset");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
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
