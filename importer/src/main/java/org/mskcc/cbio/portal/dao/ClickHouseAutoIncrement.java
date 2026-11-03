/*
 * Copyright (c) 2026 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sequence manager that keeps counters in memory and persists the high-water mark
 * in ClickHouse on shutdown so ids are never reused even if rows are deleted.
 */
public final class ClickHouseAutoIncrement {

    private static final Logger LOG = LoggerFactory.getLogger(ClickHouseAutoIncrement.class);

    private ClickHouseAutoIncrement() {
    }

    private static final class SequenceConfig {
        private final String tableName;
        private final String columnName;

        private SequenceConfig(String tableName, String columnName) {
            this.tableName = tableName;
            this.columnName = columnName;
        }
    }

    private static final Map<String, SequenceConfig> CONFIG = new ConcurrentHashMap<>();
    private static final Map<String, AtomicLong> COUNTERS = new ConcurrentHashMap<>();
    private static final String SEQUENCE_TABLE = "cbioportal_sequence_state";
    private static final AtomicBoolean SEQUENCE_TABLE_READY = new AtomicBoolean(false);
    private static final AtomicBoolean SHUTDOWN_HOOK_REGISTERED = new AtomicBoolean(false);

    static {
        register("seq_reference_genome", "reference_genome", "reference_genome_id");
        register("seq_cancer_study", "cancer_study", "cancer_study_id");
        register("seq_patient", "patient", "internal_id");
        register("seq_sample", "sample", "internal_id");
        register("seq_sample_list", "sample_list", "list_id");
        register("seq_genetic_entity", "genetic_entity", "id");
        register("seq_geneset", "geneset", "id");
        register("seq_geneset_hierarchy_node", "geneset_hierarchy_node", "node_id");
        register("seq_generic_entity_properties", "generic_entity_properties", "id");
        register("seq_genetic_profile", "genetic_profile", "genetic_profile_id");
        register("seq_gene_panel", "gene_panel", "internal_id");
        register("seq_structural_variant", "structural_variant", "internal_id");
        register("seq_mutation_event", "mutation_event", "mutation_event_id");
        register("seq_gistic", "gistic", "gistic_roi_id");
        register("seq_cna_event", "cna_event", "cna_event_id");
        register("seq_copy_number_seg", "copy_number_seg", "seg_id");
        register("seq_copy_number_seg_file", "copy_number_seg_file", "seg_file_id");
        register("seq_clinical_event", "clinical_event", "clinical_event_id");
        register("seq_resource", "resource", "internal_id");
        registerShutdownHook();
    }

    private static void register(String sequenceName, String tableName, String columnName) {
        CONFIG.put(sequenceName, new SequenceConfig(tableName, columnName.toLowerCase(Locale.ROOT)));
    }

    private static void registerShutdownHook() {
        if (!SHUTDOWN_HOOK_REGISTERED.compareAndSet(false, true)) {
            return;
        }
        Runtime.getRuntime().addShutdownHook(
            new Thread(ClickHouseAutoIncrement::persistAllSequences, "clickhouse-sequence-persist"));
    }

    public static long nextId(String sequenceName) throws DaoException {
        SequenceConfig config = CONFIG.get(sequenceName);
        if (config == null) {
            throw new DaoException("Unknown sequence: " + sequenceName);
        }
        AtomicLong counter = COUNTERS.get(sequenceName);
        if (counter == null) {
            synchronized (ClickHouseAutoIncrement.class) {
                counter = COUNTERS.get(sequenceName);
                if (counter == null) {
                    long current = initializeCounter(sequenceName, config);
                    counter = new AtomicLong(current);
                    COUNTERS.put(sequenceName, counter);
                }
            }
        }
        return counter.incrementAndGet();
    }

    private static long initializeCounter(String sequenceName, SequenceConfig config) throws DaoException {
        ensureSequenceTableExists();
        long persisted = fetchPersistedMax(sequenceName);
        long tableMax = fetchCurrentMax(config);
        long current = Math.max(persisted, tableMax);
        if (current != persisted) {
            persistLastValue(sequenceName, current);
        }
        return current;
    }

    private static void ensureSequenceTableExists() throws DaoException {
        if (SEQUENCE_TABLE_READY.get()) {
            return;
        }
        synchronized (ClickHouseAutoIncrement.class) {
            if (SEQUENCE_TABLE_READY.get()) {
                return;
            }
            Connection con = null;
            PreparedStatement stmt = null;
            try {
                con = JdbcUtil.getDbConnection(ClickHouseAutoIncrement.class);
                stmt = con.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS " + SEQUENCE_TABLE + " ("
                        + "sequence_name String, "
                        + "last_value UInt64"
                        + ") ENGINE = ReplacingMergeTree "
                        + "ORDER BY sequence_name");
                stmt.execute();
                SEQUENCE_TABLE_READY.set(true);
            } catch (SQLException ex) {
                throw new DaoException(ex);
            } finally {
                JdbcUtil.closeAll(ClickHouseAutoIncrement.class, con, stmt, null);
            }
        }
    }

    private static long fetchPersistedMax(String sequenceName) throws DaoException {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(ClickHouseAutoIncrement.class);
            stmt = con.prepareStatement(
                "SELECT max(last_value) FROM " + SEQUENCE_TABLE + " WHERE sequence_name = ?");
            stmt.setString(1, sequenceName);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0L;
        } catch (SQLException ex) {
            throw new DaoException(ex);
        } finally {
            JdbcUtil.closeAll(ClickHouseAutoIncrement.class, con, stmt, rs);
        }
    }

    private static long fetchCurrentMax(SequenceConfig config) throws DaoException {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(ClickHouseAutoIncrement.class);
            stmt = con.prepareStatement("SELECT max(" + config.columnName + ") FROM " + config.tableName);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0L;
        } catch (SQLException ex) {
            throw new DaoException(ex);
        } finally {
            JdbcUtil.closeAll(ClickHouseAutoIncrement.class, con, stmt, rs);
        }
    }

    private static void persistLastValue(String sequenceName, long value) throws DaoException {
        Connection con = null;
        PreparedStatement stmt = null;
        try {
            con = JdbcUtil.getDbConnection(ClickHouseAutoIncrement.class);
            stmt = con.prepareStatement(
                "INSERT INTO " + SEQUENCE_TABLE + " (sequence_name, last_value) VALUES (?, ?)");
            stmt.setString(1, sequenceName);
            stmt.setLong(2, value);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new DaoException(ex);
        } finally {
            JdbcUtil.closeAll(ClickHouseAutoIncrement.class, con, stmt, null);
        }
    }

    private static void persistAllSequences() {
        if (COUNTERS.isEmpty()) {
            return;
        }
        try {
            ensureSequenceTableExists();
        } catch (DaoException ex) {
            LOG.warn("Failed to ensure sequence table during shutdown persistence.", ex);
            return;
        }
        for (Map.Entry<String, AtomicLong> entry : COUNTERS.entrySet()) {
            long value = entry.getValue().get();
            try {
                persistLastValue(entry.getKey(), value);
            } catch (DaoException ex) {
                LOG.warn(
                    "Failed to persist sequence {} with value {} during shutdown.",
                    entry.getKey(),
                    value,
                    ex);
            }
        }
    }

    public static void resetCounters() {
        COUNTERS.clear();
        SEQUENCE_TABLE_READY.set(false);
    }

}
