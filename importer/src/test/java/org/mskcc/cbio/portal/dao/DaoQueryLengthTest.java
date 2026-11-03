package org.mskcc.cbio.portal.dao;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

/**
 * Verifies that DAO methods do not build SQL whose length grows with the size of the
 * input collection. Each test feeds 50,000 IDs — enough to exceed ClickHouse's default
 * max_query_size of 262,144 bytes under the old IN (?,?,…) pattern — and asserts that
 * every SQL string captured at the PreparedStatement level stays well under the limit.
 */
public class DaoQueryLengthTest {

    private static final int CLICKHOUSE_MAX_QUERY_SIZE = 262_144;
    private static final int LARGE_ID_COUNT = 50_000;

    private List<String> capturedSql;

    @Before
    public void setUp() throws Exception {
        capturedSql = new ArrayList<>();

        // Empty result set used for most queries (DELETEs, and the main SELECT in DaoGeneticAlteration).
        ResultSet emptyRs = EasyMock.createNiceMock(ResultSet.class);
        EasyMock.expect(emptyRs.next()).andReturn(false).anyTimes();
        EasyMock.replay(emptyRs);

        // Result set that returns one row for DaoGeneticProfileSamples.getOrderedSampleList —
        // "ordered_sample_list" is a comma-separated string of internal sample IDs.
        ResultSet profileSamplesRs = EasyMock.createNiceMock(ResultSet.class);
        EasyMock.expect(profileSamplesRs.next()).andReturn(true).once().andReturn(false).anyTimes();
        EasyMock.expect(profileSamplesRs.getString("ordered_sample_list")).andReturn("1,2,3").anyTimes();
        EasyMock.replay(profileSamplesRs);

        // PreparedStatement that serves genetic_profile_samples lookups.
        PreparedStatement profileSamplesStmt = EasyMock.createNiceMock(PreparedStatement.class);
        EasyMock.expect(profileSamplesStmt.executeQuery()).andReturn(profileSamplesRs).anyTimes();
        EasyMock.replay(profileSamplesStmt);

        // Generic PreparedStatement for everything else (DELETEs and the main SELECT).
        PreparedStatement genericStmt = EasyMock.createNiceMock(PreparedStatement.class);
        EasyMock.expect(genericStmt.executeUpdate()).andReturn(0).anyTimes();
        EasyMock.expect(genericStmt.executeQuery()).andReturn(emptyRs).anyTimes();
        EasyMock.replay(genericStmt);

        Connection mockCon = EasyMock.createNiceMock(Connection.class);
        EasyMock.expect(mockCon.prepareStatement(EasyMock.anyString()))
                .andAnswer(() -> {
                    String sql = (String) EasyMock.getCurrentArguments()[0];
                    capturedSql.add(sql);
                    return sql.contains("genetic_profile_samples") ? profileSamplesStmt : genericStmt;
                })
                .anyTimes();
        EasyMock.replay(mockCon);

        DataSource mockDs = EasyMock.createNiceMock(DataSource.class);
        EasyMock.expect(mockDs.getConnection()).andReturn(mockCon).anyTimes();
        EasyMock.replay(mockDs);

        JdbcUtil.setDataSource(mockDs);
    }

    private void assertNoSqlExceedsLimit() {
        assertFalse("Expected at least one SQL statement to be captured", capturedSql.isEmpty());
        for (String sql : capturedSql) {
            int byteLen = sql.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
            assertTrue(
                "SQL exceeds ClickHouse max_query_size (" + byteLen + " bytes): "
                    + sql.substring(0, Math.min(300, sql.length())),
                byteLen < CLICKHOUSE_MAX_QUERY_SIZE);
        }
    }

    // ── DaoCopyNumberSegment ──────────────────────────────────────────────────────

    @Test
    public void daoCopyNumberSegment_deleteSegmentDataForSamples_sqlUnderLimit() throws DaoException {
        Set<Integer> sampleIds = IntStream.rangeClosed(1, LARGE_ID_COUNT).boxed().collect(Collectors.toSet());

        DaoCopyNumberSegment.deleteSegmentDataForSamples(1, sampleIds);

        assertNoSqlExceedsLimit();
    }

    // ── DaoStructuralVariant ─────────────────────────────────────────────────────

    @Test
    public void daoStructuralVariant_deleteStructuralVariants_sqlUnderLimit() throws DaoException {
        Set<Integer> sampleIds = IntStream.rangeClosed(1, LARGE_ID_COUNT).boxed().collect(Collectors.toSet());

        DaoStructuralVariant.deleteStructuralVariants(1, sampleIds);

        assertNoSqlExceedsLimit();
    }

    // ── DaoCnaEvent ──────────────────────────────────────────────────────────────

    @Test
    public void daoCnaEvent_removeSampleCnaEvents_sqlUnderLimit() throws DaoException {
        List<Integer> sampleIds = IntStream.rangeClosed(1, LARGE_ID_COUNT).boxed().collect(Collectors.toList());

        DaoCnaEvent.removeSampleCnaEvents(1, sampleIds);

        assertNoSqlExceedsLimit();
    }

    // ── DaoClinicalData ──────────────────────────────────────────────────────────

    @Test
    public void daoClinicalData_removeSampleAttributesData_sqlUnderLimit() throws DaoException {
        Set<Integer> sampleIds = IntStream.rangeClosed(1, LARGE_ID_COUNT).boxed().collect(Collectors.toSet());

        DaoClinicalData.removeSampleAttributesData(sampleIds, "FRACTION_GENOME_ALTERED");

        assertNoSqlExceedsLimit();
    }

    // ── DaoGeneticAlteration ─────────────────────────────────────────────────────

    @Test
    public void daoGeneticAlteration_getGeneticAlterationMapForEntityIds_sqlUnderLimit() throws DaoException {
        // The mock returns an ordered_sample_list of "1,2,3" for the profile-samples lookup,
        // then an empty result set for the main SELECT, so the method returns an empty map.
        Set<Integer> entityIds = IntStream.rangeClosed(1, LARGE_ID_COUNT).boxed().collect(Collectors.toSet());

        DaoGeneticAlteration.getInstance().getGeneticAlterationMapForEntityIds(1, entityIds);

        assertNoSqlExceedsLimit();
    }

    // ── DaoCopyNumberSegment (read paths) ────────────────────────────────────────

    @Test
    public void daoCopyNumberSegment_getSegmentForSamples_sqlUnderLimit() throws DaoException {
        Collection<Integer> sampleIds = IntStream.rangeClosed(1, LARGE_ID_COUNT).boxed().collect(Collectors.toSet());

        DaoCopyNumberSegment.getSegmentForSamples(sampleIds, 1);

        assertNoSqlExceedsLimit();
    }

    @Test
    public void daoCopyNumberSegment_getCopyNumberActeredFraction_sqlUnderLimit() throws DaoException {
        Collection<Integer> sampleIds = IntStream.rangeClosed(1, LARGE_ID_COUNT).boxed().collect(Collectors.toSet());

        DaoCopyNumberSegment.getCopyNumberActeredFraction(sampleIds, 1, 0.2);

        assertNoSqlExceedsLimit();
    }

    // ── DaoClinicalData (read paths) ─────────────────────────────────────────────

    @Test
    public void daoClinicalData_getCancerTypeInfoBySamples_sqlUnderLimit() throws DaoException {
        List<String> sampleIds = IntStream.rangeClosed(1, LARGE_ID_COUNT)
                .mapToObj(i -> "SAMPLE_" + i)
                .collect(Collectors.toList());

        DaoClinicalData.getCancerTypeInfoBySamples(sampleIds);

        assertNoSqlExceedsLimit();
    }

    // ── DaoMutation ──────────────────────────────────────────────────────────────

    @Test
    public void daoMutation_getMutationsWithSampleCollection_sqlUnderLimit() throws DaoException {
        Collection<Integer> sampleIds = IntStream.rangeClosed(1, LARGE_ID_COUNT).boxed().collect(Collectors.toList());

        DaoMutation.getMutations(1, sampleIds, 672L);

        assertNoSqlExceedsLimit();
    }

    @Test
    public void daoMutation_getMutationsWithSampleList_sqlUnderLimit() throws DaoException {
        List<Integer> sampleIds = IntStream.rangeClosed(1, LARGE_ID_COUNT).boxed().collect(Collectors.toList());

        DaoMutation.getMutations(1, sampleIds);

        assertNoSqlExceedsLimit();
    }

    @Test
    @SuppressWarnings("deprecation")
    public void daoMutation_getSMGs_sqlUnderLimit() throws DaoException {
        Collection<Long> geneIds = IntStream.rangeClosed(1, LARGE_ID_COUNT).mapToObj(Long::valueOf).collect(Collectors.toSet());
        Collection<Integer> caseIds = IntStream.rangeClosed(1, LARGE_ID_COUNT).boxed().collect(Collectors.toSet());

        DaoMutation.getSMGs(1, geneIds, 0, 0, caseIds);

        assertNoSqlExceedsLimit();
    }

    @Test
    @SuppressWarnings("deprecation")
    public void daoMutation_getSimilarSamplesWithMutatedGenes_sqlUnderLimit() throws DaoException {
        Collection<Long> geneIds = IntStream.rangeClosed(1, LARGE_ID_COUNT).mapToObj(Long::valueOf).collect(Collectors.toSet());

        DaoMutation.getSimilarSamplesWithMutatedGenes(geneIds);

        assertNoSqlExceedsLimit();
    }
}
