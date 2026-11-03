/*
 * Copyright (c) 2026 Memorial Sloan-Kettering Cancer Center.
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
import java.sql.SQLException;
import java.util.Collection;
import java.util.UUID;

/**
 * Uploads a collection of IDs into a short-lived ClickHouse staging table, invokes a
 * callback with the table name, then drops the table. This avoids building arbitrarily
 * large IN (?, ?, ...) query strings that exceed ClickHouse's max_query_size limit.
 *
 * The staging table is a real MergeTree table (not temporary) so it persists across
 * HTTP requests on ClickHouse Cloud. It is always dropped in a finally block.
 *
 * Mirrors the structure of ClickHouseBulkDeleter.
 */
public class ClickHouseBulkUploader {

    @FunctionalInterface
    public interface UploadCallback<T> {
        T execute(String tableName) throws DaoException, SQLException;
    }

    /**
     * Uploads ids into a staging table and invokes action with the table name.
     * If ids is null, action is invoked with null as the table name (no staging table created).
     * The callback should treat a null table name as "no filter".
     */
    public static <T> T upload(Collection<? extends Number> ids, UploadCallback<T> action) throws DaoException {
        if (ids == null) {
            try {
                return action.execute(null);
            } catch (SQLException e) {
                throw new DaoException(e);
            }
        }

        String stagingTable = "staging_upload_" + UUID.randomUUID().toString().replace("-", "");
        Connection con = null;
        try {
            con = JdbcUtil.getDbConnection(ClickHouseBulkUploader.class);

            try (PreparedStatement stmt = con.prepareStatement(
                    "DROP TABLE IF EXISTS " + stagingTable)) {
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = con.prepareStatement(
                    "CREATE TABLE " + stagingTable + " (id Int64) ENGINE = MergeTree() ORDER BY id")) {
                stmt.executeUpdate();
            }

            byte[] payload = buildTsvPayload(ids);
            try (PreparedStatement stmt = con.prepareStatement(
                    "INSERT INTO " + stagingTable + " (id) FORMAT TSVWithNames")) {
                stmt.setBinaryStream(1, new ByteArrayInputStream(payload));
                stmt.executeUpdate();
            }

            return action.execute(stagingTable);
        } catch (SQLException | IOException e) {
            throw new DaoException(e);
        } finally {
            try {
                if (con != null) {
                    try (PreparedStatement drop = con.prepareStatement(
                            "DROP TABLE IF EXISTS " + stagingTable)) {
                        drop.executeUpdate();
                    }
                }
            } catch (SQLException ignored) {
            }
            JdbcUtil.closeAll(ClickHouseBulkUploader.class, con, null, null);
        }
    }

    /**
     * Uploads string IDs into a staging table (column type String) and invokes action with the table name.
     * If ids is null, action is invoked with null as the table name (no staging table created).
     */
    public static <T> T uploadStrings(Collection<String> ids, UploadCallback<T> action) throws DaoException {
        if (ids == null) {
            try {
                return action.execute(null);
            } catch (SQLException e) {
                throw new DaoException(e);
            }
        }

        String stagingTable = "staging_upload_" + UUID.randomUUID().toString().replace("-", "");
        Connection con = null;
        try {
            con = JdbcUtil.getDbConnection(ClickHouseBulkUploader.class);

            try (PreparedStatement stmt = con.prepareStatement(
                    "DROP TABLE IF EXISTS " + stagingTable)) {
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = con.prepareStatement(
                    "CREATE TABLE " + stagingTable + " (id String) ENGINE = MergeTree() ORDER BY id")) {
                stmt.executeUpdate();
            }

            byte[] payload = buildStringTsvPayload(ids);
            try (PreparedStatement stmt = con.prepareStatement(
                    "INSERT INTO " + stagingTable + " (id) FORMAT TSVWithNames")) {
                stmt.setBinaryStream(1, new ByteArrayInputStream(payload));
                stmt.executeUpdate();
            }

            return action.execute(stagingTable);
        } catch (SQLException | IOException e) {
            throw new DaoException(e);
        } finally {
            try {
                if (con != null) {
                    try (PreparedStatement drop = con.prepareStatement(
                            "DROP TABLE IF EXISTS " + stagingTable)) {
                        drop.executeUpdate();
                    }
                }
            } catch (SQLException ignored) {
            }
            JdbcUtil.closeAll(ClickHouseBulkUploader.class, con, null, null);
        }
    }

    private static byte[] buildTsvPayload(Collection<? extends Number> ids) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        buffer.write("id\n".getBytes(StandardCharsets.UTF_8));
        for (Number id : ids) {
            buffer.write((id.toString() + "\n").getBytes(StandardCharsets.UTF_8));
        }
        return buffer.toByteArray();
    }

    private static byte[] buildStringTsvPayload(Collection<String> ids) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        buffer.write("id\n".getBytes(StandardCharsets.UTF_8));
        for (String id : ids) {
            buffer.write((id + "\n").getBytes(StandardCharsets.UTF_8));
        }
        return buffer.toByteArray();
    }
}
