package org.mskcc.cbio.portal.dao;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.model.shared.GeneticAlterationType;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Verifies the behaviour of DaoMutation.createMutationCountClinicalData after the fix that
 * removed the pre-INSERT DELETE. The DELETE caused a race condition on the ReplacingMergeTree
 * clinical_sample table where the lightweight-delete mutation could wipe the freshly inserted
 * rows before OPTIMIZE TABLE FINAL deduplication ran.
 */
public class DaoMutationCountTest {

    private List<String> capturedSql;

    @Before
    public void setUp() throws Exception {
        capturedSql = new ArrayList<>();

        ResultSet emptyRs = EasyMock.createNiceMock(ResultSet.class);
        EasyMock.expect(emptyRs.next()).andReturn(false).anyTimes();
        EasyMock.replay(emptyRs);

        PreparedStatement genericStmt = EasyMock.createNiceMock(PreparedStatement.class);
        EasyMock.expect(genericStmt.executeUpdate()).andReturn(0).anyTimes();
        EasyMock.expect(genericStmt.executeQuery()).andReturn(emptyRs).anyTimes();
        EasyMock.replay(genericStmt);

        Connection mockCon = EasyMock.createNiceMock(Connection.class);
        EasyMock.expect(mockCon.prepareStatement(EasyMock.anyString()))
                .andAnswer(() -> {
                    capturedSql.add((String) EasyMock.getCurrentArguments()[0]);
                    return genericStmt;
                })
                .anyTimes();
        EasyMock.replay(mockCon);

        DataSource mockDs = EasyMock.createNiceMock(DataSource.class);
        EasyMock.expect(mockDs.getConnection()).andReturn(mockCon).anyTimes();
        EasyMock.replay(mockDs);

        JdbcUtil.setDataSource(mockDs);
        // Keep bulk-load off so flushAll() is skipped; addDatum uses plain JDBC which the mock handles.
        ClickHouseBulkLoader.bulkLoadOff();
    }

    private GeneticProfile makeProfile() {
        GeneticProfile profile = new GeneticProfile(
                "study_mutations", 1, GeneticAlterationType.MUTATION_EXTENDED,
                "MAF", "Mutations", "", false);
        profile.setGeneticProfileId(42);
        return profile;
    }

    @Test
    public void createMutationCountClinicalData_doesNotIssueDeleteStatement() throws DaoException {
        DaoMutation.createMutationCountClinicalData(makeProfile());

        boolean hasDelete = capturedSql.stream()
                .anyMatch(sql -> sql.toUpperCase().contains("DELETE"));
        assertFalse("createMutationCountClinicalData must not issue any DELETE statement", hasDelete);
    }

    @Test
    public void createMutationCountClinicalData_issuesOptimizeTable() throws DaoException {
        DaoMutation.createMutationCountClinicalData(makeProfile());

        boolean hasOptimize = capturedSql.stream()
                .anyMatch(sql -> sql.toUpperCase().contains("OPTIMIZE TABLE") && sql.contains("clinical_sample"));
        assertTrue("createMutationCountClinicalData must issue OPTIMIZE TABLE clinical_sample", hasOptimize);
    }

    @Test
    public void createMutationCountClinicalData_selectsCountsWithoutInsertSelect() throws DaoException {
        DaoMutation.createMutationCountClinicalData(makeProfile());

        // The old code used INSERT INTO clinical_sample SELECT ... in one statement.
        // The new code issues a plain SELECT and inserts rows individually via the bulk loader.
        boolean hasInsertSelect = capturedSql.stream()
                .anyMatch(sql -> sql.toUpperCase().startsWith("INSERT") && sql.toUpperCase().contains("SELECT"));
        assertFalse("Mutation counts must be fetched with a plain SELECT, not INSERT INTO ... SELECT", hasInsertSelect);

        boolean hasCountSelect = capturedSql.stream()
                .anyMatch(sql -> sql.contains("MUTATION_COUNT") && sql.toUpperCase().startsWith("SELECT"));
        assertTrue("A SELECT for MUTATION_COUNT must be issued", hasCountSelect);
    }
}
