/*
 * Copyright (c) 2026 Memorial Sloan Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.integrationTest.dao;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.dao.ClickHouseBulkDeleter;
import org.mskcc.cbio.portal.dao.ClickHouseBulkLoader;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoResourceData;
import org.mskcc.cbio.portal.dao.JdbcUtil;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Direct-load mode (--loadMode directLoad) writes rows one at a time instead of buffering them,
 * which changes what a delete issued after the writes can see.
 *
 * <p>In bulk mode ImportResourceData can read the file, queue the inserts, and only then ask
 * DaoResourceData which rows to delete: the new rows are still in memory, so the lookup returns
 * the old rows alone. In direct-load mode the same sequence has already committed the new rows, so
 * the lookup -- a plain SELECT on (cancer_study_id, resource_id) -- returns the new rows as well.
 *
 * <p>This pins that difference down: a change which flushes the deleter in direct-load mode
 * without moving the delete ahead of the inserts fails here rather than emptying the table on a
 * curator's re-import.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/applicationContext-dao.xml"})
public class TestDaoResourceDataDirectLoad extends AbstractDaoDeleteTest {

    private static final String RESOURCE_ID = "DIRECT_LOAD_PROBE";
    private static final String PATIENT_STABLE_ID = "TCGA-A1-A0SB";
    private static final String OTHER_PATIENT_STABLE_ID = "TCGA-A1-A0SD";

    private int studyId;
    private boolean bulkLoadWasOn;

    @Before
    public void setUp() throws DaoException {
        DaoCancerStudy.reCacheAll();
        CancerStudy study = DaoCancerStudy.getCancerStudyByStableId("study_tcga_pub");
        assertNotNull(study);
        studyId = study.getInternalId();
        bulkLoadWasOn = ClickHouseBulkLoader.isBulkLoad();
        ClickHouseBulkLoader.bulkLoadOff();
    }

    @After
    public void tearDown() throws DaoException {
        DaoResourceData.deleteResourceData(studyId, Set.of(RESOURCE_ID));
        ClickHouseBulkDeleter.flushAll();
        if (bulkLoadWasOn) {
            ClickHouseBulkLoader.bulkLoadOn();
        }
    }

    /**
     * The rows a direct-load import has just written are visible to the delete lookup, so deleting
     * after inserting takes the new rows with the old ones.
     */
    @Test
    public void deletingAfterTheInsertsAlsoRemovesTheRowsJustWritten() throws DaoException {
        addRow("http://already-there");
        assertEquals("the row written before the re-import", 1, countProbeRows());

        // What a re-import does in direct-load mode: the new row is committed as it is read.
        addRow("http://re-imported");
        assertEquals("both rows are in the table", 2, countProbeRows());

        DaoResourceData.deleteResourceData(studyId, Set.of(RESOURCE_ID));
        ClickHouseBulkDeleter.flushAll();

        assertEquals(
            "a delete issued after direct-load inserts removes the new row too, so flushing the"
                + " deleter here without moving the delete ahead of the inserts empties the"
                + " resource instead of replacing it",
            0, countProbeRows());
    }

    /** Deleting before the inserts leaves the re-imported row, which is the behaviour wanted. */
    @Test
    public void deletingBeforeTheInsertsKeepsTheRowsWrittenAfterwards() throws DaoException {
        addRow("http://already-there");
        assertEquals(1, countProbeRows());

        DaoResourceData.deleteResourceData(studyId, Set.of(RESOURCE_ID));
        ClickHouseBulkDeleter.flushAll();
        assertEquals("the stale row is gone", 0, countProbeRows());

        addRow("http://re-imported");
        assertEquals("the re-imported row survives", 1, countProbeRows());
    }

    /**
     * A re-import replaces the rows of the entities its file names and leaves every other
     * entity's rows alone. Deleting on (study, resource) alone would take the whole study's rows
     * for that resource.
     */
    @Test
    public void reimportingOnePatientLeavesTheOtherPatientsRowsAlone() throws DaoException {
        addRowFor(PATIENT_STABLE_ID, "http://first/original");
        addRowFor(OTHER_PATIENT_STABLE_ID, "http://second/original");
        assertEquals("both patients have a row", 2, countProbeRows());

        DaoResourceData.deleteResourceDataForReimport(
            studyId, "PATIENT", Set.of(RESOURCE_ID), Set.of(PATIENT_STABLE_ID));
        ClickHouseBulkDeleter.flushAll();

        assertEquals("only the named patient's row is removed", 1, countProbeRows());
        assertEquals("the row left behind belongs to the other patient",
            1, countProbeRowsFor(OTHER_PATIENT_STABLE_ID));
    }

    /**
     * A file whose every row was rejected loads nothing, so there is nothing for it to replace.
     * Deleting on the resource id alone would empty the resource and insert nothing in its place.
     */
    @Test
    public void aFileThatImportedNothingDeletesNothing() throws DaoException {
        addRowFor(PATIENT_STABLE_ID, "http://already-there");
        assertEquals(1, countProbeRows());

        DaoResourceData.deleteResourceDataForReimport(
            studyId, "PATIENT", Set.of(RESOURCE_ID), Set.of());
        ClickHouseBulkDeleter.flushAll();

        assertEquals("nothing was replaced, so nothing is deleted", 1, countProbeRows());
    }

    private void addRowFor(String patientStableId, String url) throws DaoException {
        DaoResourceData.addResourceDatum(
            studyId, RESOURCE_ID, "PATIENT", patientStableId, null, url, null, null);
    }

    private long countProbeRowsFor(String patientStableId) throws DaoException {
        Connection con = null;
        try {
            con = JdbcUtil.getDbConnection(ClickHouseBulkDeleter.class);
            PreparedStatement ps = con.prepareStatement(
                "SELECT count() FROM resource WHERE cancer_study_id = ? AND resource_id = ?"
                    + " AND patient_id = ?");
            ps.setInt(1, studyId);
            ps.setString(2, RESOURCE_ID);
            ps.setString(3, patientStableId);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getLong(1);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(ClickHouseBulkDeleter.class, con, null, null);
        }
    }

    private void addRow(String url) throws DaoException {
        DaoResourceData.addResourceDatum(
            studyId, RESOURCE_ID, "PATIENT", PATIENT_STABLE_ID, null, url, null, null);
    }

    private long countProbeRows() throws DaoException {
        Connection con = null;
        try {
            con = JdbcUtil.getDbConnection(ClickHouseBulkDeleter.class);
            PreparedStatement ps = con.prepareStatement(
                "SELECT count() FROM resource WHERE cancer_study_id = ? AND resource_id = ?");
            ps.setInt(1, studyId);
            ps.setString(2, RESOURCE_ID);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getLong(1);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(ClickHouseBulkDeleter.class, con, null, null);
        }
    }
}
