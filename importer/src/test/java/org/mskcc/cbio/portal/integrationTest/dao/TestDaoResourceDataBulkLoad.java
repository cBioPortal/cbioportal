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
import static org.junit.Assert.assertNull;

/**
 * Bulk-load mode (the importer's default) writes through ClickHouseBulkLoader, which builds its
 * own INSERT statement rather than using the column list DaoResourceData spells out for the
 * direct-load path.
 *
 * <p>The loader names the columns only when the caller declares them; otherwise it falls back to
 * {@code DESCRIBE TABLE} and pairs the values positionally with whatever order that returns.
 * {@code resource} is created by the portal's schema, in another repository, so an order this
 * code does not control would decide which value lands in which column -- and only the value
 * <em>count</em> is checked, so a mismatch is silent.
 *
 * <p>Every column is given a value identifiable on sight here, so a misbinding shows up as a value
 * in the wrong column rather than as a row count that still looks right.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/applicationContext-dao.xml"})
public class TestDaoResourceDataBulkLoad extends AbstractDaoDeleteTest {

    private static final String RESOURCE_ID = "BULK_LOAD_PROBE";
    private static final String PATIENT_STABLE_ID = "TCGA-A1-A0SB";
    private static final String SAMPLE_STABLE_ID = "TCGA-A1-A0SB-01";
    private static final String URL = "http://bulk/probe.svs";
    private static final String DISPLAY_NAME = "Bulk Probe Slide";
    private static final String METADATA = "{\"stain\":\"H&E\"}";

    private int studyId;
    private boolean bulkLoadWasOn;

    @Before
    public void setUp() throws DaoException {
        DaoCancerStudy.reCacheAll();
        CancerStudy study = DaoCancerStudy.getCancerStudyByStableId("study_tcga_pub");
        assertNotNull(study);
        studyId = study.getInternalId();
        bulkLoadWasOn = ClickHouseBulkLoader.isBulkLoad();
        ClickHouseBulkLoader.bulkLoadOn();
    }

    @After
    public void tearDown() throws DaoException {
        if (!bulkLoadWasOn) {
            ClickHouseBulkLoader.bulkLoadOff();
        }
        DaoResourceData.deleteResourceData(studyId, Set.of(RESOURCE_ID));
        ClickHouseBulkDeleter.flushAll();
    }

    /**
     * Each value written through the bulk path must come back from the column it was given to,
     * whatever order the table declares its columns in.
     */
    @Test
    public void everyValueLandsInTheColumnItWasGivenTo() throws DaoException {
        DaoResourceData.addResourceDatum(
            studyId, RESOURCE_ID, "SAMPLE", PATIENT_STABLE_ID, SAMPLE_STABLE_ID,
            URL, DISPLAY_NAME, METADATA);
        ClickHouseBulkLoader.flushAll();

        Row row = readProbeRow();
        assertEquals("resource_scope", "SAMPLE", row.resourceScope);
        assertEquals("patient_id", PATIENT_STABLE_ID, row.patientId);
        assertEquals("sample_id", SAMPLE_STABLE_ID, row.sampleId);
        assertEquals("url", URL, row.url);
        assertEquals("display_name", DISPLAY_NAME, row.displayName);
        assertEquals("metadata", METADATA, row.metadata);
        assertEquals("cancer_study_id", studyId, row.cancerStudyId);
    }

    /**
     * A patient-level row carries no sample. The absent value has to reach the table as a real
     * NULL rather than an empty string: cohort queries select these rows with
     * {@code sample_id IS NULL}, which an empty string does not satisfy.
     */
    @Test
    public void anAbsentSampleIdIsWrittenAsNullNotEmptyString() throws DaoException {
        DaoResourceData.addResourceDatum(
            studyId, RESOURCE_ID, "PATIENT", PATIENT_STABLE_ID, null, URL, null, null);
        ClickHouseBulkLoader.flushAll();

        Row row = readProbeRow();
        assertEquals("resource_scope", "PATIENT", row.resourceScope);
        assertEquals("patient_id", PATIENT_STABLE_ID, row.patientId);
        assertNull("sample_id", row.sampleId);
        assertNull("display_name", row.displayName);
        assertNull("metadata", row.metadata);
    }

    private static final class Row {
        int cancerStudyId;
        String resourceScope;
        String patientId;
        String sampleId;
        String url;
        String displayName;
        String metadata;
    }

    private Row readProbeRow() throws DaoException {
        Connection con = null;
        try {
            con = JdbcUtil.getDbConnection(ClickHouseBulkLoader.class);
            PreparedStatement ps = con.prepareStatement(
                "SELECT cancer_study_id, resource_scope, patient_id, sample_id, url,"
                    + " display_name, metadata FROM resource"
                    + " WHERE cancer_study_id = ? AND resource_id = ?");
            ps.setInt(1, studyId);
            ps.setString(2, RESOURCE_ID);
            ResultSet rs = ps.executeQuery();
            assertEquals("exactly one probe row is expected", true, rs.next());
            Row row = new Row();
            row.cancerStudyId = rs.getInt("cancer_study_id");
            row.resourceScope = rs.getString("resource_scope");
            row.patientId = rs.getString("patient_id");
            row.sampleId = rs.getString("sample_id");
            row.url = rs.getString("url");
            row.displayName = rs.getString("display_name");
            row.metadata = rs.getString("metadata");
            return row;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(ClickHouseBulkLoader.class, con, null, null);
        }
    }
}
