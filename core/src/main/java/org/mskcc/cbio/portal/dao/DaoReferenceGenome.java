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


/**
 * Adding or updating Reference Genomes used by molecular profiling
 * @author Kelsey Zhu
 */
public class DaoReferenceGenome {
    private static DaoReferenceGenome instance = null;

    protected DaoReferenceGenome() {}

    public static DaoReferenceGenome getInstance() {
        if (instance == null) {
            instance = new DaoReferenceGenome();
        }
        return instance;
    }
    /**
     * Add a new reference genome to the Database.
     *
     * @param referenceGenome   Reference Genome.
     * @throws DaoException Database Error.
     */
    public void addReferenceGenome(ReferenceGenome referenceGenome) throws DaoException {
        addReferenceGenome(referenceGenome, false);
    }

    /**
     * Add a new reference genome to the Database.
     * @param referenceGenome
     * @param overwrite if true, overwrite if exist.
     * @throws DaoException
     */
    public void addReferenceGenome(ReferenceGenome referenceGenome, boolean overwrite) throws DaoException {

        ReferenceGenome existing = getReferenceGenomeByInternalId(referenceGenome.getReferenceGenomeId());
        if (existing!=null) {
            if (!overwrite) {
                throw new DaoException("Reference Genome " + referenceGenome.getBuildName() + "is already imported.");
            } else {
                updateReferenceGenome(referenceGenome);
            }
        }
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCancerStudy.class);
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
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoReferenceGenome.class, con, pstmt, rs);
        }
    }

    /**
     * Deletes all Reference Genomes.
     * @throws DaoException Database Error.
     */
    public void deleteAllRecords() throws DaoException {

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoReferenceGenome.class);
            JdbcUtil.disableForeignKeyCheck(con);
            pstmt = con.prepareStatement("TRUNCATE TABLE reference_genome");
            pstmt.executeUpdate();
            JdbcUtil.enableForeignKeyCheck(con);
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
    public int updateReferenceGenome(ReferenceGenome referenceGenome) throws DaoException {

        ReferenceGenome existing = getReferenceGenomeByInternalId(referenceGenome.getReferenceGenomeId());
        if (existing==null) {
            throw new DaoException("Reference Genome " + referenceGenome.getBuildName() + "does not exist.");
        }
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int rows = 0;
        try {
            con = JdbcUtil.getDbConnection(DaoCancerStudy.class);
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

    public ReferenceGenome getReferenceGenomeByInternalId(int internalId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoReferenceGenome.class);
            pstmt = con.prepareStatement("SELECT * FROM reference_genome where reference_genome_id = ?");
            pstmt.setInt(1, internalId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                ReferenceGenome referenceGenome = extractReferenceGenome(rs);
                return referenceGenome;
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoReferenceGenome.class, con, pstmt, rs);
        }
    }

    /**
     * Retrieve reference genome of interest by genome name or genome assembly name
     * @param name   Name of Reference Genome or Genome Assembly
     * @throws DaoException Database Error.
     */

    public int getReferenceGenomeByName(String name) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoReferenceGenome.class);
            pstmt = con.prepareStatement("SELECT * FROM reference_genome WHERE name = ? OR build_name = ?");
            pstmt.setString(1, name);
            pstmt.setString(2,name);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                ReferenceGenome referenceGenome = extractReferenceGenome(rs);
                return referenceGenome.getReferenceGenomeId();
            } else {
                return -1;
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoReferenceGenome.class, con, pstmt, rs);
        }
    }

    /**
     * Extracts Reference Genome JDBC Results.
     * @param rs JDBC Result Set
     */
    private ReferenceGenome extractReferenceGenome(ResultSet rs) throws SQLException {
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
