/*
 * Copyright (c) 2026 Memorial Sloan Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.dao;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.mskcc.cbio.portal.util.GlobalProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bulk deleter that buffers IDs in memory, streams them into a ClickHouse staging
 * table via TSVWithNames, and issues a single DELETE ... WHERE id IN (SELECT id FROM
 * staging_table) statement. The staging table is a real MergeTree table (not temporary)
 * so that it persists across HTTP requests on ClickHouse Cloud.
 *
 * Mirrors the structure of ClickHouseBulkLoader.
 */
public class ClickHouseBulkDeleter {

    private boolean flushed; // once flushed, any particular deleter instance cannot not be flushed again
    private final Set<Long> pendingIds = new HashSet<>();
    private final String idColumn;
    private final String stagingTable;
    private final String targetTable;

    private static final Logger log = LoggerFactory.getLogger(ClickHouseBulkDeleter.class);
    private static final Map<String, ClickHouseBulkDeleter> BULK_DELETERS = new LinkedHashMap<>();

    private static final Integer DEFAULT_CREATE_STAGING_TABLE_MAX_RETRY_SECONDS = 2 * 60;
    private static final Integer DEFAULT_POPULATE_STAGING_TABLE_MAX_RETRY_SECONDS = 3 * 60;
    private static final Integer DEFAULT_CONFIRM_DELETE_DATA_MAX_RETRY_SECONDS = 15 * 60;
    private static final Integer DEFAULT_CONFIRM_DELETE_METADATA_MAX_RETRY_SECONDS = 2 * 60;
    private static final Integer DEFAULT_RETRY_CYCLE_PERIOD_SECONDS = 10;
    private static final Integer DEFAULT_RETRY_CYCLE_MAX_EXCEPTION_COUNT = 6;
    private static final Integer CREATE_STAGING_TABLE_MAX_RETRY_SECONDS;
    private static final Integer POPULATE_STAGING_TABLE_MAX_RETRY_SECONDS;
    private static final Integer CONFIRM_DELETE_DATA_MAX_RETRY_SECONDS;
    private static final Integer CONFIRM_DELETE_METADATA_MAX_RETRY_SECONDS;
    private static final Integer RETRY_CYCLE_PERIOD_SECONDS;
    private static final Integer RETRY_CYCLE_MAX_EXCEPTION_COUNT;

    static {
        CREATE_STAGING_TABLE_MAX_RETRY_SECONDS = GlobalProperties.parseIntegerProperty(
                "bulkdeleter.create_staging_table.max_retry_seconds",
                DEFAULT_CREATE_STAGING_TABLE_MAX_RETRY_SECONDS);
        POPULATE_STAGING_TABLE_MAX_RETRY_SECONDS = GlobalProperties.parseIntegerProperty(
                "bulkdeleter.populate_staging_table.max_retry_seconds",
                DEFAULT_POPULATE_STAGING_TABLE_MAX_RETRY_SECONDS);
        CONFIRM_DELETE_DATA_MAX_RETRY_SECONDS = GlobalProperties.parseIntegerProperty(
                "bulkdeleter.confirm_delete_data.max_retry_seconds",
                DEFAULT_CONFIRM_DELETE_DATA_MAX_RETRY_SECONDS);
        CONFIRM_DELETE_METADATA_MAX_RETRY_SECONDS = GlobalProperties.parseIntegerProperty(
                "bulkdeleter.confirm_delete_metadata.max_retry_seconds",
                DEFAULT_CONFIRM_DELETE_METADATA_MAX_RETRY_SECONDS);
        RETRY_CYCLE_PERIOD_SECONDS = GlobalProperties.parseIntegerProperty(
                "bulkdeleter.retry_cycle_period_seconds",
                DEFAULT_RETRY_CYCLE_PERIOD_SECONDS);
        RETRY_CYCLE_MAX_EXCEPTION_COUNT = GlobalProperties.parseIntegerProperty(
                "bulkdeleter.retry_cycle_max_exception_count",
                DEFAULT_RETRY_CYCLE_MAX_EXCEPTION_COUNT);
    }

    public enum AllReplicaTestType {
        CONSISTENT,
        EXPECTED_VALUE
    }

    public enum AllReplicaCriterionType {
        LONG,
        STRING
    }

// --- Interface ---

    public static ClickHouseBulkDeleter getBulkDeleter(String targetTable, String idColumn) {
        if (ClickHouseBulkDeleter.stringContainsIllegalCharacter(targetTable)) {
            throw new RuntimeException(String.format(
                    "An illegal character was used in a Clickhouse table name argument : %s",
                    targetTable));
        }
        if (ClickHouseBulkDeleter.stringContainsIllegalCharacter(idColumn)) {
            throw new RuntimeException(String.format(
                    "An illegal character was used in a Clickhouse field name argument : %s",
                    idColumn));
        }
        String key = ClickHouseBulkDeleter.deleterKeyString(targetTable, idColumn);
        return BULK_DELETERS.computeIfAbsent(key, k -> new ClickHouseBulkDeleter(targetTable, idColumn));
    }

    public static long flushAll() throws DaoException {
        List<ClickHouseBulkDeleter> allDeleters = BULK_DELETERS.values().stream().collect(Collectors.toList());
        long countOfDeletedRecords = ClickHouseBulkDeleter.flush(allDeleters); // deleters which have any potential deletes are removed from BULK_DELETERS here
        // delete unused deleters : after flushAll is called, any retained references to ClickHouseBulkDeleter objects will become invalid
        List<ClickHouseBulkDeleter> unusedDeleters = BULK_DELETERS.values().stream().collect(Collectors.toList());
        teardownDeleters(unusedDeleters);
        return countOfDeletedRecords;
    }

// --- Static helper functions

    // might be made public in the future for partial flushes (would require testing)
    private static long flush(List<ClickHouseBulkDeleter> deleters) throws DaoException {
        long totalDeleted = 0;
        if (anyDeleterWasAlreadyFlushed(deleters)) {
            throw new RuntimeException("a ClickHouseBulkDeleter object had been flushed previously, and was attempted to be flushed a second time");
        }
        List<ClickHouseBulkDeleter> effectiveDeleters = deletersWithPendingDeletes(deleters);
        try {
            dropExistingStagingTables(effectiveDeleters, false); // drop any leftover tables from previous crash/failure
            createStagingTables(effectiveDeleters);
            populateStagingTables(effectiveDeleters);
            totalDeleted = deleteRecordsReferencedInStagingTables(effectiveDeleters);
        } finally {
            dropExistingStagingTables(effectiveDeleters, true);
            teardownDeleters(effectiveDeleters);
        }
        return totalDeleted;
    }

    private static boolean stringContainsIllegalCharacter(String s) {
        // clickhouse field and table names can contain only letters, digits, or underscore (unless universally backquoted)
        return s.matches(".*[^a-zA-Z0-9_].*");
    }

    private static String deleterKeyString(String targetTable, String idColumn) {
        return String.format("%s:%s", targetTable, idColumn);
    }

    private static boolean anyDeleterWasAlreadyFlushed(List<ClickHouseBulkDeleter> deleters) {
        return deleters.stream().anyMatch(d -> d.wasFlushed());
    }

    // returns only the deleters which might delete anything (duplicates discarded)
    private static List<ClickHouseBulkDeleter> deletersWithPendingDeletes(List<ClickHouseBulkDeleter> deleters) {
        List<ClickHouseBulkDeleter> effectiveDeleters = new ArrayList<>();
        Set<String> keysSeen = new HashSet<>();
        for (ClickHouseBulkDeleter d : deleters) {
            if (d.hasPendingDeletes()) {
                if (keysSeen.contains(d.getKey())) {
                    continue; // skip duplicated keys
                }
                keysSeen.add(d.getKey());
                effectiveDeleters.add(d);
            }
        }
        return effectiveDeleters;
    }

    private static void teardownDeleters(List<ClickHouseBulkDeleter> deleters) {
        for (ClickHouseBulkDeleter d : deleters) {
            if (BULK_DELETERS.containsKey(d.getKey())) {
                BULK_DELETERS.remove(d.getKey()); // object will not be re-used
            } else {
                String msg = String.format(
                        "%s%s%s%s%s",
                        "unable to deregister deleter which has just been flushed for targetTable ",
                        d.targetTable,
                        " and idColumn ",
                        d.idColumn,
                        ". Apparently an instance which was not in map BULK_DELETERS was used for record deletion");
                log.warn(msg);
            }
            d.teardown();
        }
    }

    private static void createStagingTables(List<ClickHouseBulkDeleter> deleters) throws DaoException {
        for (ClickHouseBulkDeleter d : deleters) {
            d.createStagingTable();
        }
        for (ClickHouseBulkDeleter d : deleters) {
            d.confirmCreationOfStagingTable();
        }
    }

    private static void populateStagingTables(List<ClickHouseBulkDeleter> deleters) throws DaoException {
        for (ClickHouseBulkDeleter d : deleters) {
            d.populateStagingTable();
        }
        // wait for inserts to be recognized by all replicas
        // note: this is intentionally done in a second loop in the hope that while values are being inserted
        //     into the second, third, ... tables, the time for processing those operations allows for the values
        //     inserted into the first table to be recognized / become visible in the other replica nodes
        //     running in a Clickhouse cluster (such as with clickhouse.cloud). This may avoid retry cycles.
        for (ClickHouseBulkDeleter d : deleters) {
            d.confirmPopulationOfStagingTable();
        }
    }

    private static long deleteRecordsReferencedInStagingTables(List<ClickHouseBulkDeleter> deleters) throws DaoException {
        long totalDeleted = 0;
        for (ClickHouseBulkDeleter d : deleters) {
            totalDeleted += d.deleteRecordsReferencedInStagingTable();
        }
        return totalDeleted;
    }

    private static void dropExistingStagingTables(List<ClickHouseBulkDeleter> deleters, boolean deletionHasBeenExecuted) throws DaoException {
        for (ClickHouseBulkDeleter d : deleters) {
            // wait until all ids in the stagingTable table have been fully deleted from the target table
            if (deletionHasBeenExecuted) {
                d.confirmDeletionIsComplete();
            }
            d.dropStagingTable(deletionHasBeenExecuted);
        }
    }

// --- instance methods ---

    public void addId(long id) {
        if (this.flushed) {
            throw new RuntimeException("attempting to add identifiers to a ClickHouseBulkDeleter object which has already been flushed");
        }
        pendingIds.add(id);
    }

    public void addIds(Collection<? extends Number> ids) {
        if (this.flushed) {
            throw new RuntimeException("attempting to add identifiers to a ClickHouseBulkDeleter object which has already been flushed");
        }
        for (Number id : ids) {
            pendingIds.add(id.longValue());
        }
    }

    private ClickHouseBulkDeleter(String targetTable, String idColumn) {
        this.targetTable = targetTable;
        this.idColumn = idColumn;
        this.stagingTable = String.format("staging_delete_for_table_%s_column_%s", targetTable, idColumn);
        this.flushed = false;
    }

    private boolean hasPendingDeletes() {
        return !this.pendingIds.isEmpty();
    }

    private boolean wasFlushed() {
        return flushed;
    }

    private void teardown() {
        this.pendingIds.clear();
        this.flushed = true; // prevent re-flushing
    }

    private String getKey() {
        return ClickHouseBulkDeleter.deleterKeyString(this.targetTable, this.idColumn);
    }

// methods which are database alterations (on failure, these throw DaoException)

    private void createStagingTable() throws DaoException {
        try {
            Connection con = JdbcUtil.getDbConnection(ClickHouseBulkDeleter.class);
            String createTableStatementString = String.format(
                    "CREATE TABLE %s (id Int64) ENGINE = MergeTree() ORDER BY id",
                    this.stagingTable);
            try (PreparedStatement stmt = con.prepareStatement(createTableStatementString)) {
                stmt.executeUpdate();
            } finally {
                JdbcUtil.closeAll(ClickHouseBulkDeleter.class, con, null, null);
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    private void populateStagingTable() throws DaoException {
        // Insert IDs into staging table via TSV stream
        byte[] payload = buildTsvPayload();
        String insertStatementString = String.format(
                "INSERT INTO %s (id) FORMAT TSVWithNames",
                stagingTable);
        try {
            Connection con = JdbcUtil.getDbConnection(ClickHouseBulkDeleter.class);
            try (PreparedStatement stmt = con.prepareStatement(insertStatementString)) {
                stmt.setBinaryStream(1, new ByteArrayInputStream(payload));
                stmt.executeUpdate();
            } finally {
                JdbcUtil.closeAll(ClickHouseBulkDeleter.class, con, null, null);
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    private long deleteRecordsReferencedInStagingTable() throws DaoException {
        long records_deleted;
        String statementString = String.format(
                "DELETE FROM %s WHERE %s IN (SELECT id FROM %s)",
                targetTable, idColumn, stagingTable);
        try {
            Connection con = JdbcUtil.getDbConnection(ClickHouseBulkDeleter.class);
            try (PreparedStatement stmt = con.prepareStatement(statementString)) {
                records_deleted = stmt.executeUpdate();
            } finally {
                JdbcUtil.closeAll(ClickHouseBulkDeleter.class, con, null, null);
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        }
        return records_deleted;
    }

    private void dropStagingTable(boolean tolerateFailure) throws DaoException {
        try {
            String dropStatementString = String.format(
                    "DROP TABLE IF EXISTS %s",
                    this.stagingTable);
            Connection con = JdbcUtil.getDbConnection(ClickHouseBulkDeleter.class);
            try (PreparedStatement stmt = con.prepareStatement(dropStatementString)) {
                stmt.executeUpdate();
            } catch (SQLException e) {
                if (! tolerateFailure) {
                    // it is fatal to fail to DROP any old staging tables during the first call
                    // (during setup for deletion), because then the CREATE for staging will fail.
                    // on the second call, so long as the deletion completed, it is allowabe to leave behind
                    // residual staging tables -- they will (hopefully) DROP during the next update run
                    throw new DaoException(e);
                }
            } finally {
                JdbcUtil.closeAll(ClickHouseBulkDeleter.class, con, null, null);
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

// methods which are complete confirmations of database alterations (on failure these throw DaoExcpetion)

    private void confirmCreationOfStagingTable() throws DaoException {
        if (! this.conditionIsTrueAfterQueryWithRetry(this::allReplicasReportStagingTableExists, CREATE_STAGING_TABLE_MAX_RETRY_SECONDS)) {
            String exceptionMsgString = String.format(
                    "Failed to see all replicas report existence of staging table %s after creation",
                    this.stagingTable);
            throw new DaoException(exceptionMsgString);
        }
    }

    private void confirmPopulationOfStagingTable() throws DaoException {
        if (! this.conditionIsTrueAfterQueryWithRetry(this::allReplicasReportExpectedStagingTableRecordCount, POPULATE_STAGING_TABLE_MAX_RETRY_SECONDS)) {
            String exceptionMsgString = String.format(
                    "Failed to see all replicas reflect delete list inserted into table %s",
                    this.stagingTable);
            throw new DaoException(exceptionMsgString);
        }
    }

    private void confirmDeletionIsComplete() throws DaoException {
        this.confirmDeletionIsCompleteInData();
        this.confirmDeletionIsCompleteInMetadata();
    }

    private void confirmDeletionIsCompleteInData() throws DaoException {
        if (!this.conditionIsTrueAfterQueryWithRetry(this::deletionIsComplete, CONFIRM_DELETE_DATA_MAX_RETRY_SECONDS)) {
            String exceptionMessageString = String.format(
                    "Failed to complete the delete operation on all replicas for table %s after retrying for %d seconds",
                    this.targetTable,
                    CONFIRM_DELETE_DATA_MAX_RETRY_SECONDS);
            throw new DaoException(exceptionMessageString);
        }
        // TODO: if condition fails a few times, we might start checking whether any
        // mutations are still in progress -- if not, stop waiting and fail early
    }

    private void confirmDeletionIsCompleteInMetadata() throws DaoException {
        if (!this.conditionIsTrueAfterQueryWithRetry(this::allReplicasReportSameMetadataForTargetTable, CONFIRM_DELETE_METADATA_MAX_RETRY_SECONDS)) {
            String exceptionMessageString = String.format(
                    "Metadata for table %s could not be confirmed to have propagated across all cluster replica nodes after retrying for %d seconds",
                    this.targetTable,
                    CONFIRM_DELETE_METADATA_MAX_RETRY_SECONDS);
            throw new DaoException(exceptionMessageString);
        }
    }

// functional predicates for performing tests (these may return SQLException which allows retry, or DaoException for fatal failures)

    private boolean allReplicasReportStagingTableExists() throws SQLException, DaoException {
        String getTableExistsString = String.format(
                "SELECT hostname() AS host, count() AS table_exists FROM clusterAllReplicas('default', 'system', 'tables') WHERE database = current_database() AND name = '%s' GROUP BY host",
                stagingTable);
        return allReplicasReportExpectedResult(getTableExistsString, "table_exists", AllReplicaTestType.EXPECTED_VALUE, AllReplicaCriterionType.LONG, 1L);
    }

    private boolean allReplicasReportExpectedStagingTableRecordCount() throws SQLException, DaoException {
        String queryPart1 = "SELECT host, sum(record_count) AS total_rows FROM ((";
        String queryPart2 = "SELECT hostname() AS host, rows AS record_count FROM clusterAllReplicas('default', 'system', 'parts')";
        String queryPart3 = "WHERE database = current_database() AND table = ";
        String queryPart4 = ") union all (";
        // This union insures that at least one part with one record is included in the query result before aggregation. In this way, every host will be present in the results
        String queryPart5 = "SELECT hostname() AS host, 0 AS record_count FROM clusterAllReplicas('default', 'system', 'one')";
        String queryPart6 = ")) GROUP BY host";
        String getRecordCountsString = String.format(
                "%s%s %s '%s'%s%s%s",
                queryPart1, queryPart2, queryPart3, stagingTable, queryPart4, queryPart5, queryPart6);
        return allReplicasReportExpectedResult(getRecordCountsString, "total_rows", AllReplicaTestType.EXPECTED_VALUE, AllReplicaCriterionType.LONG, new Long(pendingIds.size()));
    }

    private boolean deletionIsComplete() throws SQLException, DaoException {
        String getUndeletedRecordCountsString = String.format(
                "SELECT count() AS record_count FROM %s WHERE %s IN (SELECT id FROM %s)",
                targetTable, idColumn, stagingTable);
        return queryProducedExpectedResult(getUndeletedRecordCountsString, "record_count", 0L);
    }

    private boolean allReplicasReportSameMetadataForTargetTable() throws SQLException, DaoException {
        String queryPart1 = "SELECT host, sum(record_count) AS total_rows FROM ((";
        String queryPart2 = "SELECT hostname() AS host, rows AS record_count FROM clusterAllReplicas('default', 'system', 'parts')";
        String queryPart3 = "WHERE database = current_database() AND table = ";
        String queryPart4 = ") union all (";
        // This union insures that at least one part with one record is included in the query result before aggregation. In this way, every host will be present in the results
        String queryPart5 = "SELECT hostname() AS host, 0 AS record_count FROM clusterAllReplicas('default', 'system', 'one')";
        String queryPart6 = ")) GROUP BY host";
        String getMetadataString = String.format(
                "%s%s %s '%s'%s%s%s",
                queryPart1, queryPart2, queryPart3, targetTable, queryPart4, queryPart5, queryPart6);
        return allReplicasReportExpectedResult(getMetadataString, "total_rows", AllReplicaTestType.CONSISTENT, AllReplicaCriterionType.LONG, null);
    }

// reusable logic to perform test/retry loops for database queries with criteria

    @FunctionalInterface
    private interface ConditionPredicate {
        boolean conditionIsSatisfied() throws SQLException, DaoException;
    }

    private boolean conditionIsTrueAfterQueryWithRetry(ConditionPredicate conPred, int maxTestingSeconds) throws DaoException {
        int totalCycleCount = maxTestingSeconds / RETRY_CYCLE_PERIOD_SECONDS;
        int exceptionsEncountered = 0;
        boolean returnValue = false;
        for (int cycle = 0 ; cycle < totalCycleCount ; cycle = cycle + 1) {
            try {
                if (conPred.conditionIsSatisfied()) {
                    returnValue = true;
                    break;
                }
            } catch (SQLException e) {
                exceptionsEncountered = exceptionsEncountered + 1;
                String msg = String.format(
                        "SQL Exception encountered during try/retry loop on cycle number %d : %s",
                        cycle, e.getMessage());
                log.warn(msg);
                if (exceptionsEncountered > RETRY_CYCLE_MAX_EXCEPTION_COUNT) {
                    throw new DaoException(e);
                }
            } finally {
                if (returnValue == false && cycle < totalCycleCount - 1 && exceptionsEncountered <= RETRY_CYCLE_MAX_EXCEPTION_COUNT) {
                    // sleep if we will be trying another iteration
                    try {
                        Thread.sleep(1000 * RETRY_CYCLE_PERIOD_SECONDS);
                    } catch (InterruptedException ie) {
                        // allow sleep interruption, and go on to next cycle immediately
                        Thread.currentThread().interrupt(); // set interrupted status, in case we want to adjust for interrupted sleep
                    }
                } else {
                }
            }
        }
        return returnValue;
    }

    // checks specified outputField in the results of running queryString and looks for expectedResult
    // only the first result (in the result set) is examined
    private boolean queryProducedExpectedResult(String queryString, String outputField, long expectedResult) throws SQLException {
        Connection con = JdbcUtil.getDbConnection(ClickHouseBulkDeleter.class);
        try (PreparedStatement pstmt = con.prepareStatement(queryString);
                ResultSet rs = pstmt.executeQuery()) {
            if (!rs.next()) {
                log.warn(String.format(
                        "evaluation of query produced empty result set : %s",
                        queryString));
                return false;
            }
            long result = rs.getLong(outputField);
            if (result != expectedResult) {
                log.warn(String.format(
                        "waiting to reach expected result for query '%s' : expected %d saw %d",
                        queryString,
                        expectedResult,
                        result
                        ));
                return false;
            }
        } finally {
            JdbcUtil.closeAll(ClickHouseBulkDeleter.class, con, null, null);
        }
        return true;
    }

    // queryString must be a SQL query which returns results which includes a field "host" with the hostname() of the replica responding. "SELECT hostname() AS host..."
    // with testType AllReplicaTestType.CONSISTENT, the expectedValue argument is ignored (pass null)
    private boolean allReplicasReportExpectedResult(String queryString, String outputField, AllReplicaTestType testType, AllReplicaCriterionType criterionType, Object expectedValue) throws SQLException, DaoException {
        Set<String> allReplicaHostNames = getReplicaHostnames();
        Map<String, Object> replicaValueMap = getReportedResultFromAllReplicas(queryString, criterionType, outputField);
        if (replicaValueMap.isEmpty()) {
            return false; // for this purpose, at least one replica must report a match
        }
        Collection<Object> values = replicaValueMap.values();
        switch (testType) {
            case AllReplicaTestType.CONSISTENT:
                Object firstValue = values.iterator().next();
                if (!values.stream().allMatch(value -> firstValue.equals(value))) {
                    log.warn(String.format(
                            "waiting to reach expected result for query '%s' : expected consistent values but saw: %s",
                            queryString,
                            replicaValueMap.toString()));
                    return false;
                }
                break;
            case AllReplicaTestType.EXPECTED_VALUE:
                if (!values.stream().allMatch(value -> expectedValue.equals(value))) {
                    log.warn(String.format(
                            "waiting to reach expected result for query '%s' : expected %d as value but saw: %s",
                            queryString,
                            expectedValue,
                            replicaValueMap.toString()));
                    return false;
                }
                break;
            default:
                throw new DaoException("Unknown testType argument passed to allReplicasReportExpectedResult()");
        }
        if (!replicaValueMap.keySet().containsAll(allReplicaHostNames)) {
            // .containsAll() was used instead of .equals() because if additional replicas became active while the query was running,
            // that is still acceptable so long as the new replicas all report the expected result. we might worry about additional non-responding replicas...
            log.warn(String.format(
                    "while querying all replicas using query '%s' : either the replica set was reduced during the query execution or one or more replicas failed to be evaluated. waiting will continue. All replicas : %s ; Reported replicas : %s",
                    queryString,
                    String.join(", ", allReplicaHostNames),
                    String.join(", ", replicaValueMap.keySet())));
            return false;
        }
        return true;
    }

    private Map<String, Object> getReportedResultFromAllReplicas(String queryString, AllReplicaCriterionType outputType, String outputField) throws SQLException, DaoException {
        Map<String, Object> replicaValueMap = new LinkedHashMap<>();
        Connection con = JdbcUtil.getDbConnection(ClickHouseBulkDeleter.class);
        try (PreparedStatement pstmt = con.prepareStatement(queryString);
                ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                String replicaHostname = rs.getString("host");
                switch (outputType) {
                    case LONG:
                        replicaValueMap.put(replicaHostname, rs.getLong(outputField));
                        break;
                    case STRING:
                        replicaValueMap.put(replicaHostname, rs.getString(outputField));
                        break;
                    default:
                        throw new DaoException("Unknown outputType argument passed to getReportedResultFromAllReplicas()");
                }
            }
        } finally {
            JdbcUtil.closeAll(ClickHouseBulkDeleter.class, con, null, null);
        }
        return replicaValueMap;
    }

// helper functions which query the database directly

    private Set<String> getReplicaHostnames() throws SQLException {
        Set<String> hostnameSet = new HashSet<>();
        Connection con = JdbcUtil.getDbConnection(ClickHouseBulkDeleter.class);
        String getHostNamesQueryString = "SELECT hostname() AS host FROM clusterAllReplicas('default', 'system', 'one') GROUP BY host";
        try (PreparedStatement pstmt = con.prepareStatement(getHostNamesQueryString);
                ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                hostnameSet.add(rs.getString("host"));
            }
        } finally {
            JdbcUtil.closeAll(ClickHouseBulkDeleter.class, con, null, null);
        }
        if (hostnameSet.isEmpty()) {
            throw new SQLException(String.format(
                    "unable to find set of active replica hosts using query : %s",
                    getHostNamesQueryString));
        }
        return hostnameSet;
    }

    private byte[] buildTsvPayload() throws DaoException {
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            // header row
            buffer.write("id\n".getBytes(StandardCharsets.UTF_8));
            // data rows
            for (Long id : pendingIds) {
                buffer.write((id.toString() + "\n").getBytes(StandardCharsets.UTF_8));
            }
            return buffer.toByteArray();
        } catch (IOException e) {
            throw new DaoException(e);
        }
    }

}
