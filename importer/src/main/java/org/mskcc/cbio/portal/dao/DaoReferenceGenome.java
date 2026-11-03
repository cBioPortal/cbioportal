/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.dao;

import java.sql.*;
import java.util.*;
import org.mskcc.cbio.portal.model.ReferenceGenome;

/**
 * Adding or updating Reference Genomes used by molecular profiling
 * @author Kelsey Zhu
 */
public final class DaoReferenceGenome {

    private static final String REFERENCE_GENOME_SEQUENCE = "seq_reference_genome";

    private static final Map<String,ReferenceGenome> byGenomeBuild = new HashMap<String,ReferenceGenome>();
    private static final Map<String,ReferenceGenome> byGenomeName = new HashMap<String,ReferenceGenome>();
    private static final Map<Integer,ReferenceGenome> byGenomeInternalId = new HashMap<Integer,ReferenceGenome>();
    private static final Map<String, Integer> genomeInternalIds = new HashMap<String,Integer>();

    static {
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
            try {
                con = JdbcUtil.getDbConnection(DaoReferenceGenome.class);
                long refId = ClickHouseAutoIncrement.nextId(REFERENCE_GENOME_SEQUENCE);
                pstmt = con.prepareStatement("INSERT INTO reference_genome " +
                        "( `reference_genome_id`, `species`, `name`, "
                        + "`build_name`, `genome_size`, `url`, "
                        + " `release_date` ) VALUES (?,?,?,?,?,?,?)");
                pstmt.setLong(1, refId);
                pstmt.setString(2, referenceGenome.getSpecies());
                pstmt.setString(3, referenceGenome.getGenomeName());
                pstmt.setString(4, referenceGenome.getBuildName());
                pstmt.setLong(5, referenceGenome.getGenomeSize());
                pstmt.setString(6, referenceGenome.getUrl());
                pstmt.setDate(7, new java.sql.Date(referenceGenome.getReleaseDate().getTime()));
                if (pstmt.executeUpdate() != 0 ) {
                    referenceGenome.setReferenceGenomeId((int) refId);
                    // update reference cache
                    addCache(referenceGenome);
                } else {
                    throw new DaoException("attempt to add new referenceGenome record failed");
                }
            } catch (SQLException e) {
                throw new DaoException(e);
            } finally {
                JdbcUtil.closeAll(DaoReferenceGenome.class, con, pstmt, null);
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
            pstmt = con.prepareStatement("TRUNCATE TABLE reference_genome");
            pstmt.executeUpdate();
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
    public static void updateReferenceGenome(ReferenceGenome referenceGenome) throws DaoException {

        ReferenceGenome existing = getReferenceGenomeByInternalId(referenceGenome.getReferenceGenomeId());
        if (existing==null) {
            throw new DaoException("Reference Genome " + referenceGenome.getBuildName() + "does not exist.");
        }
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoReferenceGenome.class);
            pstmt = con.prepareStatement("UPDATE reference_genome " +
                "SET `species`=?, `name`=?, "
                + "`build_name`=?, `genome_size`=?, `url`=?, "
                + " `release_date`=? WHERE `reference_genome_id`=?");
            pstmt.setString(1, referenceGenome.getSpecies());
            pstmt.setString(2, referenceGenome.getGenomeName());
            pstmt.setString(3, referenceGenome.getBuildName());
            pstmt.setLong(4, referenceGenome.getGenomeSize());
            pstmt.setString(5, referenceGenome.getUrl());
            pstmt.setDate(6, new java.sql.Date(referenceGenome.getReleaseDate().getTime()));
            pstmt.setInt(7, referenceGenome.getReferenceGenomeId());
            if (pstmt.executeUpdate() != 0) {
                // update reference cache
                reCache();
            }
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
        return byGenomeName.get(genomeName);
    }

    /**
     * Retrieve reference genome of interest by genome name or genome assembly name
     * @param name   Name of Reference Genome or Genome Assembly
     * @throws DaoException Database Error.
     */
    public static int getReferenceGenomeIdByName(String name) throws DaoException {
        Integer referenceGenomeId = genomeInternalIds.get(name);
        if (referenceGenomeId == null) {
            return -1;
        } else {
            return referenceGenomeId;
        }
    }

    /**
     * Extracts Reference Genome JDBC Results.
     * @param rs JDBC Result Set
     */
    private static ReferenceGenome extractReferenceGenome(ResultSet rs) throws SQLException {
        ReferenceGenome referenceGenome = new ReferenceGenome(
            rs.getString("name"),
            rs.getString("species"),
            rs.getString("build_name"));
        referenceGenome.setReferenceGenomeId(rs.getInt("reference_genome_id"));
        referenceGenome.setGenomeSize(rs.getLong("genome_size"));
        referenceGenome.setReleaseDate(rs.getDate("release_date"));
        referenceGenome.setUrl(rs.getString("url"));
        return referenceGenome;
    }
}
