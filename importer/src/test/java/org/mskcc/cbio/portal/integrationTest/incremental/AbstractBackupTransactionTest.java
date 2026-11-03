/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.integrationTest.incremental;

import org.junit.Test;
import org.mskcc.cbio.portal.dao.BackupUtil;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.JdbcUtil;
import org.mskcc.cbio.portal.integrationTest.IntegrationTestBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Abstract base for integration tests that verify BackupUtil.backupIfIncrementalUpdateMode behaviour
 * for a specific importer class. Subclasses declare which tables are backed up and
 * how to run / fail the import; this class supplies the shared @Test methods.
 *
 * Contract for runFailingImport(): implementations must call assertBackupTablesExist()
 * before throwing so that the mid-import invariant is verified within the failure test.
 */
public abstract class AbstractBackupTransactionTest extends IntegrationTestBase {

    /** Tables passed to BackupUtil.backupIfIncrementalUpdateMode by the importer under test. */
    protected abstract List<String> getBackedUpTables();

    /** Run the import to completion without errors, with incremental update mode enabled. */
    protected abstract void runSuccessfulImport() throws Exception;

    /** Run the import to completion without errors, with incremental update mode disabled. */
    protected abstract void runNonIncrementalImport() throws Exception;

    /**
     * Run the import in a way that causes it to fail.
     * Implementations MUST call {@link #assertBackupTablesExist()} at the point of
     * failure injection so the mid-import backup invariant is verified.
     */
    protected abstract void runFailingImport() throws Exception;

    /** Capture a snapshot of the relevant table data before import starts. */
    protected abstract Object captureDataState() throws Exception;

    /** Assert that table data matches the snapshot taken by captureDataState(). */
    protected abstract void assertDataStateUnchanged(Object stateBefore) throws Exception;

    // ── @Test methods ────────────────────────────────────────────────────────

    @Test
    public void testOnSuccess_noBackupTablesExist() throws Exception {
        runSuccessfulImport();
        assertNoBackupTablesExist();
    }

    @Test
    public void testNonIncrementalMode_noBackupTablesCreated() throws Exception {
        runNonIncrementalImport();
        assertNoBackupTablesExist();
    }

    @Test
    public void testOnFailure_noBackupTablesExist_andDataRestored() throws Exception {
        Object stateBefore = captureDataState();
        try {
            runFailingImport();
            fail("Import should have thrown an exception");
        } catch (Exception expected) {
            // expected
        }
        assertNoBackupTablesExist();
        assertDataStateUnchanged(stateBefore);
    }

    // ── Shared helpers ────────────────────────────────────────────────────────

    /** Assert all backup tables exist (call this mid-import from runFailingImport). */
    protected void assertBackupTablesExist() throws DaoException {
        for (String table : getBackedUpTables()) {
            assertTrue(table + "_backup should exist during import", backupTableExists(table));
        }
    }

    protected void assertNoBackupTablesExist() throws DaoException {
        for (String table : getBackedUpTables()) {
            assertFalse(table + "_backup should not exist after import", backupTableExists(table));
        }
    }

    protected boolean backupTableExists(String table) throws DaoException {
        Connection con = null;
        try {
            con = JdbcUtil.getDbConnection(BackupUtil.class);
            PreparedStatement ps = con.prepareStatement(
                "SELECT count() FROM system.tables WHERE database = currentDatabase() AND name = ?");
            ps.setString(1, table + "_backup");
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getLong(1) > 0;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(BackupUtil.class, con, null, null);
        }
    }
}
