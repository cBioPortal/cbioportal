package org.mskcc.cbio.portal.dao;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

/**
 * Verifies that ClickHouseBulkUploader never builds SQL strings whose length grows with
 * the size of the ID collection. The old IN (?, ?, ...) pattern would produce ~350 KB of
 * SQL for 50,000 IDs, exceeding ClickHouse's default max_query_size of 262,144 bytes.
 */
public class ClickHouseBulkUploaderTest {

    private static final int CLICKHOUSE_MAX_QUERY_SIZE = 262_144;
    private static final int LARGE_ID_COUNT = 50_000;

    private List<String> capturedSql;

    @Before
    public void setUp() throws Exception {
        capturedSql = new ArrayList<>();

        PreparedStatement mockStmt = EasyMock.createNiceMock(PreparedStatement.class);
        EasyMock.expect(mockStmt.executeUpdate()).andReturn(0).anyTimes();
        EasyMock.replay(mockStmt);

        Connection mockCon = EasyMock.createNiceMock(Connection.class);
        EasyMock.expect(mockCon.prepareStatement(EasyMock.anyString()))
                .andAnswer(() -> {
                    capturedSql.add((String) EasyMock.getCurrentArguments()[0]);
                    return mockStmt;
                })
                .anyTimes();
        EasyMock.replay(mockCon);

        DataSource mockDs = EasyMock.createNiceMock(DataSource.class);
        EasyMock.expect(mockDs.getConnection()).andReturn(mockCon).anyTimes();
        EasyMock.replay(mockDs);

        JdbcUtil.setDataSource(mockDs);
    }

    @Test
    public void upload_withLargeIdSet_allSqlUnderMaxQuerySize() throws DaoException {
        Set<Integer> ids = IntStream.rangeClosed(1, LARGE_ID_COUNT).boxed().collect(Collectors.toSet());

        ClickHouseBulkUploader.upload(ids, stagingTable -> null);

        assertFalse("Expected at least one SQL statement to be issued", capturedSql.isEmpty());
        for (String sql : capturedSql) {
            int byteLen = sql.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
            assertTrue("SQL exceeds max_query_size (" + byteLen + " bytes): " + sql.substring(0, Math.min(200, sql.length())),
                    byteLen < CLICKHOUSE_MAX_QUERY_SIZE);
        }
    }

    @Test
    public void upload_withLargeIdSet_noLiteralIdsInSql() throws DaoException {
        Set<Integer> ids = IntStream.rangeClosed(1, LARGE_ID_COUNT).boxed().collect(Collectors.toSet());

        ClickHouseBulkUploader.upload(ids, stagingTable -> null);

        boolean hasInWithLiterals = capturedSql.stream()
                .anyMatch(sql -> sql.matches("(?s).*IN\\s*\\(\\s*\\d.*"));
        assertFalse("SQL should not contain an IN clause with literal IDs", hasInWithLiterals);
    }

    @Test
    public void upload_withLargeIdSet_callbackReceivesStagingTableName() throws DaoException {
        Set<Integer> ids = IntStream.rangeClosed(1, LARGE_ID_COUNT).boxed().collect(Collectors.toSet());
        String[] receivedTable = new String[1];

        ClickHouseBulkUploader.upload(ids, stagingTable -> {
            receivedTable[0] = stagingTable;
            return null;
        });

        assertNotNull("Callback should receive a staging table name", receivedTable[0]);
        assertTrue("Staging table name should start with staging_upload_",
                receivedTable[0].startsWith("staging_upload_"));
    }

    @Test
    public void upload_withLargeIdSet_stagingTableDroppedInFinally() throws DaoException {
        Set<Integer> ids = IntStream.rangeClosed(1, LARGE_ID_COUNT).boxed().collect(Collectors.toSet());
        String[] receivedTable = new String[1];

        ClickHouseBulkUploader.upload(ids, stagingTable -> {
            receivedTable[0] = stagingTable;
            return null;
        });

        long dropCount = capturedSql.stream()
                .filter(sql -> sql.contains("DROP TABLE IF EXISTS " + receivedTable[0]))
                .count();
        // once before CREATE (safety net) and once in finally
        assertEquals("Staging table should be dropped twice", 2, dropCount);
    }

    @Test
    public void upload_withNullIds_callbackReceivesNullAndNoSqlIssued() throws DaoException {
        String[] receivedTable = {"sentinel"};

        ClickHouseBulkUploader.upload(null, stagingTable -> {
            receivedTable[0] = stagingTable;
            return null;
        });

        assertNull("Callback should receive null table name when ids is null", receivedTable[0]);
        assertTrue("No SQL should be issued when ids is null", capturedSql.isEmpty());
    }
}
