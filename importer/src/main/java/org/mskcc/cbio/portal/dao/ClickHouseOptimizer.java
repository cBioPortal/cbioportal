package org.mskcc.cbio.portal.dao;

import java.sql.Connection;
import java.sql.SQLException;

public class ClickHouseOptimizer {

    private ClickHouseOptimizer() {}

    /**
     * Flushes pending bulk-load data (if bulk load is on), then runs
     * OPTIMIZE TABLE ... FINAL on each of the given tables to force
     * ReplacingMergeTree deduplication before the data is read back.
     */
    public static void optimizeTables(String... tableNames) throws DaoException {
        // It's important to flush all pending inserts *prior* to OPTIMIZE TABLE
        // so that we can detect duplicates properly
        if (ClickHouseBulkLoader.isBulkLoad()) {
            ClickHouseBulkLoader.flushAll();
        }
        Connection con = null;
        try {
            con = JdbcUtil.getDbConnection(ClickHouseOptimizer.class);
            for (String table : tableNames) {
                con.prepareStatement("OPTIMIZE TABLE " + table + " FINAL").executeUpdate();
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(ClickHouseOptimizer.class, con, null, null);
        }
    }
}
