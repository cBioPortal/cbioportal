/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.dao;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.ClinicalAttribute;
import org.mskcc.cbio.portal.model.Patient;
import org.mskcc.cbio.portal.model.Sample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DAO to `patient`.
 * 
 * @author Benjamin Gross
 */
public class DaoPatient {

    private static final Logger log = LoggerFactory.getLogger(DaoPatient.class);

    private static final String SAMPLE_COUNT_ATTR_ID = "SAMPLE_COUNT";

    private static final Map<Integer, Patient> byInternalId = new HashMap<Integer, Patient>();
    private static final Map<Integer, Set<Patient>> byInternalCancerStudyId = new HashMap<Integer, Set<Patient>>();
    private static final MultiKeyMap byCancerIdAndStablePatientId = new MultiKeyMap();

    private static void clearCache()
    {
        byInternalId.clear();
        byInternalCancerStudyId.clear();
        byCancerIdAndStablePatientId.clear();
    }

    public static synchronized void reCache()
    {
        clearCache();

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoPatient.class);
            pstmt = con.prepareStatement("SELECT * FROM patient");
            rs = pstmt.executeQuery();
            ArrayList<Patient> list = new ArrayList<Patient>();
            while (rs.next()) {
                Patient p = extractPatient(rs);
                if (p != null) {
                    cachePatient(p, p.getCancerStudy().getInternalId());
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            JdbcUtil.closeAll(DaoPatient.class, con, pstmt, rs);
        }
    }

    public static void cachePatient(Patient patient, int cancerStudyId)
    {
        if (!byInternalId.containsKey(patient.getInternalId())) {
            byInternalId.put(patient.getInternalId(), patient);
        } 
        if (byInternalCancerStudyId.containsKey(patient.getCancerStudy().getInternalId())) {
            byInternalCancerStudyId.get(patient.getCancerStudy().getInternalId()).add(patient);
        }
        else {
            Set<Patient> patientList = new HashSet<Patient>();
            patientList.add(patient);
            byInternalCancerStudyId.put(patient.getCancerStudy().getInternalId(), patientList);
        }

        if (!byCancerIdAndStablePatientId.containsKey(cancerStudyId, patient.getStableId())) {
            byCancerIdAndStablePatientId.put(cancerStudyId, patient.getStableId(), patient);
        }
    }

    private static final String PATIENT_SEQUENCE = "seq_patient";

    public static int addPatient(Patient patient) throws DaoException
    {
        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            long internalId = ClickHouseAutoIncrement.nextId(PATIENT_SEQUENCE);
            if (ClickHouseBulkLoader.isBulkLoad()) {
                ClickHouseBulkLoader loader = ClickHouseBulkLoader.getClickHouseBulkLoader("patient");
                loader.setFieldNames(new String[]{"internal_id", "stable_id", "cancer_study_id"});
                loader.insertRecord(
                    Long.toString(internalId),
                    patient.getStableId(),
                    Integer.toString(patient.getCancerStudy().getInternalId())
                );
            } else {
                con = JdbcUtil.getDbConnection(DaoPatient.class);
                pstmt = con.prepareStatement("INSERT INTO patient (`internal_id`, `stable_id`, `cancer_study_id`) VALUES (?,?,?)");
                pstmt.setLong(1, internalId);
                pstmt.setString(2, patient.getStableId());
                pstmt.setInt(3, patient.getCancerStudy().getInternalId());
                pstmt.executeUpdate();
            }
            cachePatient(new Patient(patient.getCancerStudy(), patient.getStableId(), (int) internalId),
                    patient.getCancerStudy().getInternalId());
            return (int) internalId;
        }
        catch (SQLException e) {
            throw new DaoException(e);
        }
        finally {
            JdbcUtil.closeAll(DaoPatient.class, con, pstmt, null);
        }
    }

    public static Patient getPatientById(int internalId)
    {
        return byInternalId.get(internalId);
    }

    public static Patient getPatientByCancerStudyAndPatientId(int cancerStudyId, String stablePatientId) 
    {
        return (Patient)byCancerIdAndStablePatientId.get(cancerStudyId, stablePatientId);
    }

    public static Set<Patient> getPatientsByCancerStudyId(int cancerStudyId)
    {
        return byInternalCancerStudyId.get(cancerStudyId);
    }

    public static List<Patient> getAllPatients()
    {
        return (byInternalId.isEmpty()) ? Collections.<Patient>emptyList() :
            new ArrayList<Patient>(byInternalId.values());
    }

    public static void deleteAllRecords() throws DaoException
    {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoPatient.class);
            pstmt = con.prepareStatement("TRUNCATE TABLE patient");
            pstmt.executeUpdate();
        }
        catch (SQLException e) {
            throw new DaoException(e);
        }
        finally {
            JdbcUtil.closeAll(DaoPatient.class, con, pstmt, rs);
        }

        clearCache();
    }

    public static void createSampleCountClinicalData(int cancerStudyId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCopyNumberSegment.class);
            pstmt = con.prepareStatement(
                    "INSERT INTO clinical_patient(`internal_id`, `attr_id`, `attr_value`) " +
                            "SELECT patient.`internal_id` AS internal_id, ? as attr_id, CAST(COUNT(*) AS CHAR) AS sample_count FROM sample " +
                            "INNER JOIN patient ON sample.`patient_id` = patient.`internal_id` " +
                            "WHERE patient.`cancer_study_id`=? " +
                            "GROUP BY patient.`internal_id`");
            pstmt.setString(1, SAMPLE_COUNT_ATTR_ID);
            pstmt.setInt(2, cancerStudyId);
            pstmt.executeUpdate();
            ClinicalAttribute clinicalAttribute = DaoClinicalAttributeMeta.getDatum(SAMPLE_COUNT_ATTR_ID, cancerStudyId);
            if (clinicalAttribute == null) {
                ClinicalAttribute attr = new ClinicalAttribute(SAMPLE_COUNT_ATTR_ID, "Number of Samples Per Patient", 
                    "Number of Samples Per Patient", "STRING", true, "1", cancerStudyId);
                DaoClinicalAttributeMeta.addDatum(attr);
            }
            ClickHouseOptimizer.optimizeTables("clinical_patient");
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCopyNumberSegment.class, con, pstmt, null);
        }
    }

    private static Patient extractPatient(ResultSet rs) throws SQLException
    {
		try {
			CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByInternalId(rs.getInt("cancer_study_id"));
			if (cancerStudy == null) return null;
			return new Patient(cancerStudy,
							   rs.getString("stable_id"),
							   rs.getInt("internal_id"));
		}
		catch (DaoException e) {
			throw new SQLException(e);
		}
    }

    /**
     * Removes patients information from the study
     * @param internalStudyId - id of the study that contains the patients
     * @param patientStableIds - patient stable ids to remove
     * @throws DaoException
     */
    public static void deletePatients(int internalStudyId, Set<String> patientStableIds) throws DaoException
    {
        if (patientStableIds == null || patientStableIds.isEmpty()) {
            log.info("No patients specified to remove for study with internal id={}. Skipping.", internalStudyId);
            return;
        }
        log.info("Removing {} patients from study with internal id={} ...", patientStableIds, internalStudyId);

        Set<Integer> internalPatientIds = findInternalPatientIdsInStudy(internalStudyId, patientStableIds);
        Set<String> patientsSampleStableIds = internalPatientIds.stream().flatMap(internalPatientId ->
                DaoSample.getSamplesByPatientId(internalPatientId).stream().map(Sample::getStableId))
                .collect(Collectors.toSet());
        DaoSample.deleteSamples(internalStudyId, patientsSampleStableIds);

        Connection con = null;
        try {
            con = JdbcUtil.getDbConnection(DaoPatient.class);
            List<Long> clinicalEventIds = collectIds(con,
                    "SELECT clinical_event_id FROM clinical_event WHERE patient_id IN ", internalPatientIds);
            ClickHouseBulkDeleter.getBulkDeleter("clinical_event_data", "clinical_event_id").addIds(clinicalEventIds);
            ClickHouseBulkDeleter.getBulkDeleter("clinical_event", "clinical_event_id").addIds(clinicalEventIds);
            ClickHouseBulkDeleter.getBulkDeleter("clinical_patient", "internal_id").addIds(internalPatientIds);
            DaoResourceData.deleteResourceDataForPatients(internalStudyId, patientStableIds);
            ClickHouseBulkDeleter.getBulkDeleter("patient", "internal_id").addIds(internalPatientIds);
            ClickHouseBulkDeleter.flushAll();
        }
        catch (DaoException e) {
            throw e;
        }
        catch (SQLException e) {
            throw new DaoException(e);
        }
        finally {
            JdbcUtil.closeAll(DaoPatient.class, con, null, null);
        }
        clearCache();
        log.info("Removing {} patients from study with internal id={} done.", patientStableIds, internalStudyId);
    }

    public static Set<Integer> findInternalPatientIdsInStudy(Integer internalStudyId, Set<String> patientStableIds) {
        HashSet<Integer> internalPatientIds = new HashSet<>();
        for (String patientId : patientStableIds) {
            Patient patientByCancerStudyAndPatientId = DaoPatient.getPatientByCancerStudyAndPatientId(internalStudyId, patientId);
            if (patientByCancerStudyAndPatientId == null) {
                throw new NoSuchElementException("Patient with stable id=" + patientId + " not found in study with internal id=" + internalStudyId + ".");
            }
            internalPatientIds.add(patientByCancerStudyAndPatientId.getInternalId());
        }
        return internalPatientIds;
    }

    private static List<Long> collectIds(Connection con, String sqlPrefix, Collection<Integer> ids) throws DaoException, SQLException {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return ClickHouseBulkUploader.upload(ids, stagingTable -> {
            List<Long> collected = new ArrayList<>();
            try (PreparedStatement pstmt = con.prepareStatement(sqlPrefix + "(SELECT id FROM " + stagingTable + ")");
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    collected.add(rs.getLong(1));
                }
            }
            return collected;
        });
    }

}
