/*
 * Copyright (C) 2026  Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.integrationTest.dao;

import org.mskcc.cbio.portal.dao.ClickHouseBulkDeleter;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.JdbcUtil;
import org.mskcc.cbio.portal.integrationTest.IntegrationTestBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

/**
 * Shared helpers for integration tests that verify the ClickHouseBulkDeleter-based
 * delete paths in DaoCancerStudy, DaoPatient, and DaoSample.
 */
abstract class AbstractDaoDeleteTest extends IntegrationTestBase {

    /**
     * Count rows in {@code table} where {@code column = value}.
     */
    protected long countRowsWhereEq(String table, String column, long value) throws DaoException {
        Connection con = null;
        try {
            con = JdbcUtil.getDbConnection(ClickHouseBulkDeleter.class);
            PreparedStatement ps = con.prepareStatement(
                "SELECT count() FROM " + table + " WHERE " + column + " = ?");
            ps.setLong(1, value);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getLong(1);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(ClickHouseBulkDeleter.class, con, null, null);
        }
    }

    /**
     * Assert no staging tables created by {@link ClickHouseBulkDeleter} were left behind.
     * These are named {@code staging_delete_<targetTable>}.
     */
    protected void assertNoStagingTablesRemain() throws DaoException {
        Connection con = null;
        try {
            con = JdbcUtil.getDbConnection(ClickHouseBulkDeleter.class);
            PreparedStatement ps = con.prepareStatement(
                "SELECT count() FROM system.tables WHERE database = currentDatabase() AND name LIKE 'staging_delete_%'");
            ResultSet rs = ps.executeQuery();
            rs.next();
            assertEquals("No staging_delete_* tables should remain after flush", 0, rs.getLong(1));
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(ClickHouseBulkDeleter.class, con, null, null);
        }
    }
}
