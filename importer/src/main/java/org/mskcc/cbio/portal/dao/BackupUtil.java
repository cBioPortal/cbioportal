package org.mskcc.cbio.portal.dao;

import org.mskcc.cbio.portal.util.ProgressMonitor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class BackupUtil {

    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }

    public static void conditionalBackup(
        boolean isIncrementalUpdateMode,
        List<String> tableNames,
        ThrowingRunnable fn
    ) throws Exception {

        // We don't need to worry about backing up tables
        // if we're not running in incremental update mode--
        // the new data will be uploaded in a single SQL query,
        // and even if there is an interruption mid-import the data
        // should not be left in an inconsistent state.
        if (!isIncrementalUpdateMode) {
            fn.run();
            return;
        }

        backup(tableNames, fn);
    }

    public static void backup(List<String> tableNames, ThrowingRunnable fn) throws Exception {
        try {
            for (String table : tableNames) {
                backup(table);
            }
            fn.run();
        } catch (Throwable t) {
            ProgressMonitor.setCurrentMessage("Caught exception. Restoring from backup tables...");
            for (String table : tableNames) {
                try {
                    restore(table);
                } catch (Throwable restoreEx) {
                    t.addSuppressed(restoreEx);
                }
            }
            throw t;
        } finally {
            for (String table : tableNames) {
                deleteBackup(table);
            }
        }
    }

    public static void backup(String tableName) throws DaoException {
        String backupTable = tableName + "_backup";
        ProgressMonitor.setCurrentMessage("Backing up " + tableName + " to " + backupTable + "...");

        Connection con = null;
        try {
            con = JdbcUtil.getDbConnection(BackupUtil.class);
            con.prepareStatement("DROP TABLE IF EXISTS " + backupTable + ";").executeUpdate();
            con.prepareStatement("CREATE TABLE " + backupTable + " AS " + tableName + ";").executeUpdate();
            con.prepareStatement("INSERT INTO " + backupTable + " SELECT * FROM " + tableName + ";").executeUpdate();
            
            ProgressMonitor.setCurrentMessage(tableName + " successfully backed up.");
        } catch (SQLException e) {
            ProgressMonitor.logWarning("Failed to create a backup table for " + tableName);
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(BackupUtil.class, con, null, null);
        }
    }

    public static void restore(String tableName) throws DaoException {
        String backupTable = tableName + "_backup";
        ProgressMonitor.setCurrentMessage("Restoring " + tableName + " from " + backupTable + "...");

        Connection con = null;
        try {
            con = JdbcUtil.getDbConnection(BackupUtil.class);
            con.prepareStatement("EXCHANGE TABLES " + backupTable + " AND " + tableName + ";").executeUpdate();

            ProgressMonitor.setCurrentMessage(tableName + " successfully restored.");
        } catch (SQLException e) {
            ProgressMonitor.logWarning("Failed to restore " + tableName + " from backup.");
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(BackupUtil.class, con, null, null);
        }
    }

    private static void deleteBackup(String tableName) {
        String backupTable = tableName + "_backup";
        ProgressMonitor.setCurrentMessage("Deleting " + backupTable + "...");
        
        Connection con = null;
        try {
            con = JdbcUtil.getDbConnection(BackupUtil.class);
            con.prepareStatement("DROP TABLE IF EXISTS " + backupTable + ";").executeUpdate();
        } catch (Exception ignored) {
        } finally {
            JdbcUtil.closeAll(BackupUtil.class, con, null, null);
        }
    }
}
