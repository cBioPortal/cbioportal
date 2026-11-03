/*
 * Copyright (c) 2015 - 2022 Memorial Sloan Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.dao;

import org.mskcc.cbio.portal.model.Sample;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Data access object for sample_profile table
 */
public final class DaoSampleProfile {
    /**
     * Adds a record to the sample_profile table. This is part of adding records from genetic profiles.
     * Can use the bulk loader.
     */
    private DaoSampleProfile() {}

    private static final int NO_SUCH_PROFILE_ID = -1;
    private static final int UPSERT_BATCH_SIZE = 1_000;

    public static void upsertSampleToProfileMapping(Collection<Integer> sampleIds, Integer geneticProfileId, Integer panelId) throws DaoException {
        upsertSampleToProfileMapping(
                sampleIds.stream()
                        .map(sampleId -> new SampleProfileTuple(geneticProfileId, sampleId, panelId)).toList());
    }

    public record SampleProfileTuple(int geneticProfileId, int sampleId, Integer panelId) {}

    public static void upsertSampleToProfileMapping(Collection<SampleProfileTuple> idTuples) throws DaoException {
        if (idTuples.isEmpty()) {
            return;
        }
        if (ClickHouseBulkLoader.isBulkLoad()) {
            upsertWithBulkLoader(idTuples);
            ClickHouseBulkLoader.flushAll();
            optimizeSampleProfileTable();
            return;
        }
        upsertWithJdbcBatch(idTuples);
        optimizeSampleProfileTable();
    }

    private static void optimizeSampleProfileTable() throws DaoException {
        ClickHouseOptimizer.optimizeTables("sample_profile");
    }

    private static void upsertWithJdbcBatch(Collection<SampleProfileTuple> idTuples) throws DaoException {
        Connection con = null;
        PreparedStatement insertStmt = null;
        try {
            con = JdbcUtil.getDbConnection(DaoSampleProfile.class);
            insertStmt = con.prepareStatement(
                "INSERT INTO sample_profile (`sample_id`, `genetic_profile_id`, `panel_id`) VALUES (?,?,?)");

            int batchCount = 0;
            for (SampleProfileTuple idTuple : idTuples) {
                bindInsert(insertStmt, idTuple);
                if (++batchCount % UPSERT_BATCH_SIZE == 0) {
                    insertStmt.executeBatch();
                }
            }

            insertStmt.executeBatch();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoSampleProfile.class, con, insertStmt, null);
        }
    }

    private static void upsertWithBulkLoader(Collection<SampleProfileTuple> idTuples) throws DaoException {
        ClickHouseBulkLoader loader = ClickHouseBulkLoader.getClickHouseBulkLoader("sample_profile");
        loader.setFieldNames(new String[]{"sample_id", "genetic_profile_id", "panel_id"});
        for (SampleProfileTuple idTuple : idTuples) {
            loader.insertRecord(
                Integer.toString(idTuple.sampleId()),
                Integer.toString(idTuple.geneticProfileId()),
                idTuple.panelId() != null ? Integer.toString(idTuple.panelId()) : null
            );
        }
    }

    private static void bindInsert(PreparedStatement insertStmt, SampleProfileTuple idTuple) throws SQLException {
        insertStmt.setInt(1, idTuple.sampleId());
        insertStmt.setInt(2, idTuple.geneticProfileId());
        if (idTuple.panelId() != null) {
            insertStmt.setInt(3, idTuple.panelId());
        } else {
            insertStmt.setNull(3, java.sql.Types.INTEGER);
        }
        insertStmt.addBatch();
    }

    public static boolean sampleExistsInGeneticProfile(int sampleId, int geneticProfileId)
            throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = JdbcUtil.getDbConnection(DaoSampleProfile.class);
            pstmt = con.prepareStatement
                    ("SELECT * FROM sample_profile WHERE sample_id = ? AND genetic_profile_id = ?");
            pstmt.setInt(1, sampleId);
            pstmt.setInt(2, geneticProfileId);
            rs = pstmt.executeQuery();
            return (rs.next());
        } catch (NullPointerException e) {
            throw new DaoException(e);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoSampleProfile.class, con, pstmt, rs);
        }
    }

    public static Integer getPanelId(int sampleId, int geneticProfileId)
            throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = JdbcUtil.getDbConnection(DaoSampleProfile.class);
            pstmt = con.prepareStatement
                    ("SELECT panel_id FROM sample_profile WHERE sample_id = ? AND genetic_profile_id = ?");
            pstmt.setInt(1, sampleId);
            pstmt.setInt(2, geneticProfileId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                int panelId = rs.getInt(1);
                if (rs.wasNull()) {
                    return null;
                }
                return panelId;
            } else {
                throw new NoSuchElementException("No sample_profile with SAMPLE_ID=" + sampleId + " and genetic_profile_id=" + geneticProfileId);
            }
        } catch (NoSuchElementException | SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoSampleProfile.class, con, pstmt, rs);
        }
    }

    public static int countSamplesInProfile(int geneticProfileId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoSampleProfile.class);
            pstmt = con.prepareStatement
                    ("SELECT count(*) FROM sample_profile WHERE genetic_profile_id = ?");
            pstmt.setInt(1, geneticProfileId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoSampleProfile.class, con, pstmt, rs);
        }
    }

    public static int getProfileIdForSample(int sampleId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = JdbcUtil.getDbConnection(DaoSampleProfile.class);
            pstmt = con.prepareStatement("SELECT genetic_profile_id FROM sample_profile WHERE sample_id = ?");
            pstmt.setInt(1, sampleId);
            rs = pstmt.executeQuery();
            if( rs.next() ) {
               return rs.getInt("genetic_profile_id");
            }else{
               return NO_SUCH_PROFILE_ID;
            }
        } catch (NullPointerException e) {
            throw new DaoException(e);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoSampleProfile.class, con, pstmt, rs);
        }
    }

    public static ArrayList<Integer> getAllSampleIdsInProfile(int geneticProfileId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoSampleProfile.class);
            pstmt = con.prepareStatement
                    ("SELECT * FROM sample_profile WHERE genetic_profile_id = ?");
            pstmt.setInt(1, geneticProfileId);
            rs = pstmt.executeQuery();
            ArrayList<Integer> sampleIds = new ArrayList<Integer>();
            while (rs.next()) {
                Sample sample = DaoSample.getSampleById(rs.getInt("sample_id"));
                sampleIds.add(rs.getInt("sample_id"));
            }
            return sampleIds;
        } catch (NullPointerException e) {
            throw new DaoException(e);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoSampleProfile.class, con, pstmt, rs);
        }
    }

    public static ArrayList<Integer> getAllSamples() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoSampleProfile.class);
            pstmt = con.prepareStatement
                    ("SELECT * FROM sample_profile");
            rs = pstmt.executeQuery();
            ArrayList<Integer> sampleIds = new ArrayList<Integer>();
            while (rs.next()) {
                sampleIds.add(rs.getInt("sample_id"));
            }
            return sampleIds;
        } catch (NullPointerException e) {
            throw new DaoException(e);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoSampleProfile.class, con, pstmt, rs);
        }
    }

    public static void deleteAllRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoSampleProfile.class);
            pstmt = con.prepareStatement("TRUNCATE TABLE sample_profile");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoSampleProfile.class, con, pstmt, rs);
        }
    }

    public static void deleteRecords(List<Integer> sampleIds, List<Integer> profileIds) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoSampleProfile.class);
            for (int i = 0; i < sampleIds.size(); i++) {
                pstmt = con.prepareCall("DELETE FROM sample_profile WHERE sample_id = ? and genetic_profile_id = ?");
                pstmt.setInt(1, sampleIds.get(i));
                pstmt.setInt(2, profileIds.get(i));
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoSampleProfile.class, con, pstmt, rs);
        }
    }

    public static boolean sampleProfileMappingExistsByPanel(Integer panelId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoSampleProfile.class);
            pstmt = con.prepareStatement("select count(*) from sample_profile where panel_id = ?");
            pstmt.setInt(1, panelId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return (rs.getInt(1) > 0);
            }
            else {
                return false;
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoSampleProfile.class, con, pstmt, rs);
        }
    }

}
