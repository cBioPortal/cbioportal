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

import java.sql.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.SpringUtil;

import java.util.*;


/**
 * Adding or updating Reference Genomes used by molecular profiling
 * @author Kelsey Zhu
 */
public final class DaoReferenceGenome {

    private static final Map<String,ReferenceGenome> byGenomeBuild = new HashMap<String,ReferenceGenome>();
    private static final Map<String,ReferenceGenome> byGenomeName = new HashMap<String,ReferenceGenome>();
    private static final Map<Integer,ReferenceGenome> byGenomeInternalId = new HashMap<Integer,ReferenceGenome>();
    private static final Map<String, Integer> genomeInternalIds = new HashMap<String,Integer>();

    static {
        SpringUtil.initDataSource();
        reCache();
    }

    private static synchronized void clearCache() {
        byGenomeBuild.clear();
        byGenomeInternalId.clear();
        byGenomeName.clear();
        genomeInternalIds.clear();
    }

    private static synchronized void addCache(ReferenceGenome referenceGenome) {
        byGenomeBuild.put(referenceGenome.getBuildName(), referenceGenome);
        byGenomeName.put(referenceGenome.getGenomeName(), referenceGenome);
        byGenomeInternalId.put(referenceGenome.getReferenceGenomeId(), referenceGenome);
        genomeInternalIds.put(referenceGenome.getBuildName(), referenceGenome.getReferenceGenomeId());
        genomeInternalIds.put(referenceGenome.getGenomeName(), referenceGenome.getReferenceGenomeId());
    }

    private static synchronized void reCache() {
        clearCache();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoReferenceGenome.class);
            pstmt = con.prepareStatement("SELECT * FROM reference_genome");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                ReferenceGenome referenceGenome = extractReferenceGenome(rs);
                addCache(referenceGenome);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            JdbcUtil.closeAll(DaoReferenceGenome.class, con, pstmt, rs);
        }
    }


    /**
     * Add a new reference genome to the Database.
     *
     * @param referenceGenome   Reference Genome.
     * @throws DaoException Database Error.
     */
    public static void addReferenceGenome(ReferenceGenome referenceGenome) throws DaoException {
        reCache();
        addReferenceGenome(referenceGenome, false);
    }

    /**
     * Add a new reference genome to the Database.
     * @param referenceGenome
     * @param overwrite if true, overwrite if exist.
     * @throws DaoException
     */
    public static void addReferenceGenome(ReferenceGenome referenceGenome, boolean overwrite) throws DaoException {

        ReferenceGenome existing = getReferenceGenomeByInternalId(referenceGenome.getReferenceGenomeId());
        if (existing!=null) {
            if (!overwrite) {
                throw new DaoException("Reference Genome " + referenceGenome.getBuildName() + "is already imported.");
            } else {
                updateReferenceGenome(referenceGenome);
            }
        }
        else {
            Connection con = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            try {
                con = JdbcUtil.getDbConnection(DaoReferenceGenome.class);
                pstmt = con.prepareStatement("INSERT INTO reference_genome " +
                        "( `species`, `name`, "
                        + "`build_name`, `genome_size`, `URL`, "
                        + " `release_date` ) VALUES (?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
                pstmt.setString(1, referenceGenome.getSpecies());
                pstmt.setString(2, referenceGenome.getGenomeName());
                pstmt.setString(3, referenceGenome.getBuildName());
                pstmt.setLong(4, referenceGenome.getGenomeSize());
                pstmt.setString(5, referenceGenome.getUrl());
                pstmt.setDate(6, new java.sql.Date(referenceGenome.getReleaseDate().getTime()));

                pstmt.executeUpdate();
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int autoId = rs.getInt(1);
                    referenceGenome.setReferenceGenomeId(autoId);
                }
                // update reference cache
                addCache(referenceGenome);
            } catch (SQLException e) {
                throw new DaoException(e);
            } finally {
                JdbcUtil.closeAll(DaoReferenceGenome.class, con, pstmt, rs);
            }
        }
    }

    /**
     * Deletes all Reference Genomes.
     * @throws DaoException Database Error.
     */
    public static void deleteAllRecords() throws DaoException {

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoReferenceGenome.class);
            JdbcUtil.disableForeignKeyCheck(con);
            pstmt = con.prepareStatement("TRUNCATE TABLE reference_genome");
            pstmt.executeUpdate();
            JdbcUtil.enableForeignKeyCheck(con);
            // clear cache
            clearCache();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoReferenceGenome.class, con, pstmt, rs);
        }
    }

    /**
     * Update existing reference genome.
     * @param referenceGenome Reference Genome Object
     * @throws DaoException
     */
    public static int updateReferenceGenome(ReferenceGenome referenceGenome) throws DaoException {

        ReferenceGenome existing = getReferenceGenomeByInternalId(referenceGenome.getReferenceGenomeId());
        if (existing==null) {
            throw new DaoException("Reference Genome " + referenceGenome.getBuildName() + "does not exist.");
        }
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int rows = 0;
        try {
            con = JdbcUtil.getDbConnection(DaoReferenceGenome.class);
            pstmt = con.prepareStatement("UPDATE reference_genome " +
                "SET `species`=?, `name`=?, "
                + "`build_name`=?, `genome_size`=?, `URL`=?, "
                + " `release_date`=? WHERE `reference_genome_id`=?");
            pstmt.setString(1, referenceGenome.getSpecies());
            pstmt.setString(2, referenceGenome.getGenomeName());
            pstmt.setString(3, referenceGenome.getBuildName());
            pstmt.setLong(4, referenceGenome.getGenomeSize());
            pstmt.setString(5, referenceGenome.getUrl());
            pstmt.setDate(6, new java.sql.Date(referenceGenome.getReleaseDate().getTime()));
            pstmt.setInt(7, referenceGenome.getReferenceGenomeId());
            rows += pstmt.executeUpdate();
            // update reference cache
            reCache();
            return rows;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoReferenceGenome.class, con, pstmt, rs);
        }
    }

    /**
     * Retrieve reference genome by internal DB ID
     * @param internalId   Reference Genome internal DB ID
     * @throws DaoException Database Error.
     */

    public static ReferenceGenome getReferenceGenomeByInternalId(int internalId) throws DaoException {
        ReferenceGenome genome = byGenomeInternalId.get(internalId);
        return genome;
    }

    /**
     * Retrieve reference genome by genome build name
     * @param buildName   Reference Genome build name
     * @throws DaoException Database Error.
     */

    public static ReferenceGenome getReferenceGenomeByBuildName(String buildName) throws DaoException {
        return byGenomeBuild.get(buildName);
    }


    /**
     * Retrieve reference genome by genome build name
     * @param genomeName   Reference Genome build name
     * @throws DaoException Database Error.
     */

    public static ReferenceGenome getReferenceGenomeByGenomeName(String genomeName) throws DaoException {
        //return byGenomeName.get(genomeName);
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ReferenceGenome referenceGenome = null;
        try {
            con = JdbcUtil.getDbConnection(DaoReferenceGenome.class);
            pstmt = con.prepareStatement("SELECT * FROM reference_genome WHERE `name` = ?");
            pstmt.setString(1,genomeName);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                referenceGenome = extractReferenceGenome(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            JdbcUtil.closeAll(DaoReferenceGenome.class, con, pstmt, rs);
            return referenceGenome;
        }
    }

    /**
     * Retrieve reference genome of interest by genome name or genome assembly name
     * @param name   Name of Reference Genome or Genome Assembly
     * @throws DaoException Database Error.
     */
    @Deprecated
    public static int getReferenceGenomeIdByName(String name) throws DaoException {
        return getReferenceGenomeIdByName(name, ReferenceGenome.HOMO_SAPIENS);
    }

    /**
     * Retrieve reference genome of interest by genome name or genome assembly name
     * @param name   Name of Reference Genome or Genome Assembly
     * @param species genetic species               
     * @throws DaoException Database Error.
     */
    public static int getReferenceGenomeIdByName(String name, String species) throws DaoException {
        try {
            return genomeInternalIds.get(name);
        } catch (java.lang.NullPointerException exp) {
            if (species.equals(ReferenceGenome.HOMO_SAPIENS)) {
                return genomeInternalIds.get(ReferenceGenome.HOMO_SAPIENS_DEFAULT_GENOME_BUILD);
            } else if (species.equals(ReferenceGenome.MUS_MUSCULUS))  {
                return genomeInternalIds.get(ReferenceGenome.MUS_MUSCULUS_DEFAULT_GENOME_BUILD); // NCBI_BUILD field was an optional in the past
            } else {
                throw new DaoException("Species not supproted yet");
            }
        }
    }

    /**
     * Extracts Reference Genome JDBC Results.
     * @param rs JDBC Result Set
     */
    private static ReferenceGenome extractReferenceGenome(ResultSet rs) throws SQLException {
        ReferenceGenome referenceGenome = new ReferenceGenome(
            rs.getString("SPECIES"),
            rs.getString("NAME"),
            rs.getString("BUILD_NAME"));
        referenceGenome.setReferenceGenomeId(rs.getInt("REFERENCE_GENOME_ID"));
        referenceGenome.setGenomeSize(rs.getLong("GENOME_SIZE"));
        referenceGenome.setReleaseDate(rs.getDate("RELEASE_DATE"));
        referenceGenome.setUrl(rs.getString("URL"));
        return referenceGenome;
    }

}