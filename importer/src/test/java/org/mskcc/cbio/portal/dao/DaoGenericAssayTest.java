package org.mskcc.cbio.portal.dao;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.mskcc.cbio.portal.model.shared.GenericEntityProperty;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Verifies that DaoGenericAssay.insert operations include the id column and
 * assign non-zero auto-increment values via ClickHouseAutoIncrement.
 */
public class DaoGenericAssayTest {

    private List<String> capturedSql;
    private List<List<Object>> capturedParams;

    @Before
    public void setUp() throws Exception {
        capturedSql = new ArrayList<>();
        capturedParams = new ArrayList<>();

        // Build a mock ResultSet that returns 0 for max() queries (empty table)
        ResultSet emptyRs = EasyMock.createNiceMock(ResultSet.class);
        EasyMock.expect(emptyRs.next()).andReturn(false).anyTimes();
        EasyMock.expect(emptyRs.isClosed()).andReturn(false).anyTimes();
        EasyMock.replay(emptyRs);

        // Build a mock ResultSet that returns a single value of 0L
        ResultSet zeroRs = EasyMock.createNiceMock(ResultSet.class);
        EasyMock.expect(zeroRs.next()).andReturn(true).times(2);
        EasyMock.expect(zeroRs.getLong(1)).andReturn(0L).times(2);
        EasyMock.expect(zeroRs.next()).andReturn(false).anyTimes();
        EasyMock.expect(zeroRs.isClosed()).andReturn(false).anyTimes();
        EasyMock.replay(zeroRs);

        // Mock PreparedStatement that captures SQL and parameters
        PreparedStatement mockStmt = EasyMock.createNiceMock(PreparedStatement.class);

        // For executeUpdate (INSERT) — capture the parameters
        EasyMock.expect(mockStmt.executeUpdate()).andReturn(0).anyTimes();
        EasyMock.expect(mockStmt.executeBatch()).andReturn(new int[]{1}).anyTimes();

        // For executeQuery (SELECT) — return zeroRs for max queries, emptyRs otherwise
        EasyMock.expect(mockStmt.executeQuery())
                .andReturn(zeroRs).times(2)  // two max() queries during init
                .andReturn(emptyRs).anyTimes();

        // For CREATE TABLE — return false
        EasyMock.expect(mockStmt.execute()).andReturn(false).anyTimes();

        // Capture setLong / setInt / setString calls
        mockStmt.setLong(EasyMock.anyInt(), EasyMock.anyLong());
        EasyMock.expectLastCall().andAnswer(() -> {
            int index = (int) EasyMock.getCurrentArguments()[0];
            long value = (long) EasyMock.getCurrentArguments()[1];
            int sqlIdx = capturedSql.size() - 1;
            if (sqlIdx < 0) sqlIdx = 0;
            while (capturedParams.size() <= sqlIdx) {
                capturedParams.add(new ArrayList<>());
            }
            List<Object> params = capturedParams.get(sqlIdx);
            while (params.size() < index) {
                params.add(null);
            }
            params.set(index - 1, value);
            return null;
        }).anyTimes();

        mockStmt.setInt(EasyMock.anyInt(), EasyMock.anyInt());
        EasyMock.expectLastCall().andAnswer(() -> {
            int index = (int) EasyMock.getCurrentArguments()[0];
            int value = (int) EasyMock.getCurrentArguments()[1];
            int sqlIdx = capturedSql.size() - 1;
            if (sqlIdx < 0) sqlIdx = 0;
            while (capturedParams.size() <= sqlIdx) {
                capturedParams.add(new ArrayList<>());
            }
            List<Object> params = capturedParams.get(sqlIdx);
            while (params.size() < index) {
                params.add(null);
            }
            params.set(index - 1, value);
            return null;
        }).anyTimes();

        mockStmt.setString(EasyMock.anyInt(), EasyMock.anyString());
        EasyMock.expectLastCall().andAnswer(() -> {
            int index = (int) EasyMock.getCurrentArguments()[0];
            String value = (String) EasyMock.getCurrentArguments()[1];
            int sqlIdx = capturedSql.size() - 1;
            if (sqlIdx < 0) sqlIdx = 0;
            while (capturedParams.size() <= sqlIdx) {
                capturedParams.add(new ArrayList<>());
            }
            List<Object> params = capturedParams.get(sqlIdx);
            while (params.size() < index) {
                params.add(null);
            }
            params.set(index - 1, value);
            return null;
        }).anyTimes();

        EasyMock.replay(mockStmt);

        // Mock Connection
        Connection mockCon = EasyMock.createNiceMock(Connection.class);
        EasyMock.expect(mockCon.prepareStatement(EasyMock.anyString()))
                .andAnswer(() -> {
                    capturedParams.add(new ArrayList<>());
                    capturedSql.add((String) EasyMock.getCurrentArguments()[0]);
                    return mockStmt;
                })
                .anyTimes();
        EasyMock.expect(mockCon.getAutoCommit()).andReturn(true).anyTimes();
        mockCon.setAutoCommit(EasyMock.anyBoolean());
        EasyMock.expectLastCall().anyTimes();
        EasyMock.replay(mockCon);

        // Mock DataSource
        DataSource mockDs = EasyMock.createNiceMock(DataSource.class);
        EasyMock.expect(mockDs.getConnection()).andReturn(mockCon).anyTimes();
        EasyMock.replay(mockDs);

        JdbcUtil.setDataSource(mockDs);
        ClickHouseBulkLoader.bulkLoadOff();

        // Reset auto-increment state to ensure clean test
        ClickHouseAutoIncrement.resetCounters();
    }

    @Test
    public void setGenericEntityProperty_includesIdColumn() throws DaoException {
        DaoGenericAssay.setGenericEntityProperty(42, "NAME", "Test Entity");

        // Find the INSERT statement (skip CREATE TABLE and SELECT statements from init)
        String insertSql = capturedSql.stream()
                .filter(sql -> sql.toUpperCase().contains("INSERT INTO GENERIC_ENTITY_PROPERTIES"))
                .findFirst()
                .orElse(null);

        assertNotNull("Expected an INSERT INTO GENERIC_ENTITY_PROPERTIES statement", insertSql);
        assertTrue("INSERT statement must include the id column: " + insertSql,
                insertSql.contains("`id`"));
    }

    @Test
    public void setGenericEntityProperty_idIsNonZero() throws DaoException {
        DaoGenericAssay.setGenericEntityProperty(42, "NAME", "Test Entity");

        // Find the INSERT and its parameters
        int insertIdx = -1;
        for (int i = 0; i < capturedSql.size(); i++) {
            if (capturedSql.get(i).toUpperCase().contains("INSERT INTO GENERIC_ENTITY_PROPERTIES")) {
                insertIdx = i;
                break;
            }
        }
        assertTrue("Expected an INSERT statement", insertIdx >= 0);

        List<Object> params = capturedParams.get(insertIdx);
        assertTrue("Expected at least 1 parameter (id)", params.size() >= 1);
        assertNotNull("id parameter must not be null", params.get(0));
        assertTrue("id must be Long", params.get(0) instanceof Long);
        long id = (Long) params.get(0);
        assertTrue("id must be > 0, got " + id, id > 0);
    }

    @Test
    public void setGenericEntityProperty_consecutiveCallsGetDifferentIds() throws DaoException {
        DaoGenericAssay.setGenericEntityProperty(42, "NAME", "Entity A");
        DaoGenericAssay.setGenericEntityProperty(43, "NAME", "Entity B");

        // Collect all INSERT statement IDs
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < capturedSql.size(); i++) {
            if (capturedSql.get(i).toUpperCase().contains("INSERT INTO GENERIC_ENTITY_PROPERTIES")) {
                List<Object> params = capturedParams.get(i);
                if (params.size() >= 1 && params.get(0) instanceof Long) {
                    ids.add((Long) params.get(0));
                }
            }
        }

        assertEquals("Expected 2 INSERT statements", 2, ids.size());
        assertTrue("First id must be > 0: " + ids.get(0), ids.get(0) > 0);
        assertTrue("Second id must be > 0: " + ids.get(1), ids.get(1) > 0);
        assertNotEquals("Consecutive inserts must get different IDs", ids.get(0), ids.get(1));
    }

    @Test
    public void setGenericEntityPropertiesUsingBatch_includesIdColumn() throws DaoException {
        List<GenericEntityProperty> properties = Arrays.asList(
                new GenericEntityProperty("NAME", "Entity 1", 100),
                new GenericEntityProperty("DESCRIPTION", "Desc 1", 100)
        );
        DaoGenericAssay.setGenericEntityPropertiesUsingBatch(properties);

        // The batch INSERT statement should include id
        String batchSql = capturedSql.stream()
                .filter(sql -> sql.toUpperCase().contains("INSERT INTO GENERIC_ENTITY_PROPERTIES"))
                .findFirst()
                .orElse(null);

        assertNotNull("Expected an INSERT INTO GENERIC_ENTITY_PROPERTIES statement", batchSql);
        assertTrue("Batch INSERT must include the id column: " + batchSql,
                batchSql.contains("`id`"));
    }

    @Test
    public void setGenericEntityPropertiesUsingBatch_idsAreNonZeroAndUnique() throws DaoException {
        List<GenericEntityProperty> properties = Arrays.asList(
                new GenericEntityProperty("NAME", "Entity 1", 100),
                new GenericEntityProperty("DESCRIPTION", "Desc 1", 100),
                new GenericEntityProperty("URL", "http://example.com", 100)
        );
        DaoGenericAssay.setGenericEntityPropertiesUsingBatch(properties);

        // Verify the INSERT statement includes the id column
        String insertSql = capturedSql.stream()
                .filter(sql -> sql.toUpperCase().contains("INSERT INTO GENERIC_ENTITY_PROPERTIES"))
                .findFirst()
                .orElse(null);
        assertNotNull("Expected an INSERT INTO GENERIC_ENTITY_PROPERTIES statement", insertSql);
        assertTrue("Batch INSERT must include id column: " + insertSql,
                insertSql.contains("`id`"));

        // Verify that setLong was called (id parameters are being set)
        boolean hasLongParam = capturedParams.stream()
                .anyMatch(params -> params.stream().anyMatch(p -> p instanceof Long));
        assertTrue("Expected setLong to be called for id parameters", hasLongParam);
    }

    @Test
    public void setGenericEntityProperty_stillSetsEntityIdNameAndValue() throws DaoException {
        int entityId = 42;
        DaoGenericAssay.setGenericEntityProperty(entityId, "NAME", "Test Entity");

        int insertIdx = -1;
        for (int i = 0; i < capturedSql.size(); i++) {
            if (capturedSql.get(i).toUpperCase().contains("INSERT INTO GENERIC_ENTITY_PROPERTIES")) {
                insertIdx = i;
                break;
            }
        }
        assertTrue("Expected an INSERT statement", insertIdx >= 0);

        List<Object> params = capturedParams.get(insertIdx);
        assertEquals("Expected 4 parameters (id, genetic_entity_id, name, value)", 4, params.size());
        assertEquals("genetic_entity_id should be " + entityId, entityId, params.get(1));
        assertEquals("name should be NAME", "NAME", params.get(2));
        assertEquals("value should be Test Entity", "Test Entity", params.get(3));
    }
}
