/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.portal.model.ClinicalEvent;

/**
 *
 * @author gaoj
 */
public final class DaoClinicalEvent {
    private DaoClinicalEvent() {}
    
    public static int addClinicalEvent(ClinicalEvent clinicalEvent) {
        if (!ClickHouseBulkLoader.isBulkLoad()) {
            throw new IllegalStateException("Only bulk load mode is allowed for importing clinical events");
        }
        
        ClickHouseBulkLoader.getClickHouseBulkLoader("clinical_event").insertRecord(
                Long.toString(clinicalEvent.getClinicalEventId()),
                Integer.toString(clinicalEvent.getPatientId()),
                clinicalEvent.getStartDate().toString(),
                clinicalEvent.getStopDate()==null?null:clinicalEvent.getStopDate().toString(),
                clinicalEvent.getEventType()
                );
        return 1+addClinicalEventData(clinicalEvent);
    }
    
    private static int addClinicalEventData(ClinicalEvent clinicalEvent) {
        long eventId = clinicalEvent.getClinicalEventId();
        for (Map.Entry<String,String> entry : clinicalEvent.getEventData().entrySet()) {
            ClickHouseBulkLoader.getClickHouseBulkLoader("clinical_event_data").insertRecord(
                    Long.toString(eventId),
                    entry.getKey(),
                    entry.getValue()
                    );
        }
        return 1;
        
    }
    
    public static List<ClinicalEvent> getClinicalEvent(int patientId) throws DaoException {
        return getClinicalEvent(patientId, null);
    }
    
    public static List<ClinicalEvent> getClinicalEvent(int patientId, String eventType) throws DaoException {

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoClinicalEvent.class);

            // get events first
            if (eventType==null) {
                pstmt = con.prepareStatement("SELECT * FROM clinical_event WHERE patient_id=?");
            } else {
                pstmt = con.prepareStatement("SELECT * FROM clinical_event WHERE patient_id=? AND event_type=?");
            }
            pstmt.setInt(1, patientId);
            if (eventType!=null) {
                pstmt.setString(2, eventType);
            }

            rs = pstmt.executeQuery();
            Map<Long, ClinicalEvent> clinicalEvents = new HashMap<Long, ClinicalEvent>();
            while (rs.next()) {
               ClinicalEvent clinicalEvent = extractClinicalEvent(rs);
               clinicalEvents.put(clinicalEvent.getClinicalEventId(), clinicalEvent);
            }

            rs.close();
           
           // get data then
           if (!clinicalEvents.isEmpty()) {
                pstmt = con.prepareStatement("SELECT * FROM clinical_event_data WHERE clinical_event_id IN ("
                        + StringUtils.join(clinicalEvents.keySet(), ",") + ")");

                rs = pstmt.executeQuery();
                while (rs.next()) {
                   long eventId = rs.getLong("clinical_event_id");
                   clinicalEvents.get(eventId).addEventDatum(rs.getString("key"), rs.getString("value"));
                }
            }

            return new ArrayList<ClinicalEvent>(clinicalEvents.values());
        } catch (SQLException e) {
           throw new DaoException(e);
        } finally {
           JdbcUtil.closeAll(DaoClinicalEvent.class, con, pstmt, rs);
        }
    }
    
    private static ClinicalEvent extractClinicalEvent(ResultSet rs) throws SQLException {
        ClinicalEvent clinicalEvent = new ClinicalEvent();
        clinicalEvent.setClinicalEventId(rs.getLong("clinical_event_id"));
        clinicalEvent.setPatientId(rs.getInt("patient_id"));
        clinicalEvent.setStartDate(JdbcUtil.readLongFromResultSet(rs, "start_date"));
        clinicalEvent.setStopDate(JdbcUtil.readLongFromResultSet(rs, "stop_date"));
        clinicalEvent.setEventType(rs.getString("event_type"));
        return clinicalEvent;
    }
    
    public static long getLargestClinicalEventId() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoClinicalEvent.class);
            pstmt = con.prepareStatement
                    ("SELECT max(`clinical_event_id`) FROM `clinical_event`");
            rs = pstmt.executeQuery();
            return rs.next() ? rs.getLong(1) : 0;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoClinicalEvent.class, con, pstmt, rs);
        }
    }
    
    /**
     * 
     * @param cancerStudyId
     * @param caseId
     * @return true if timeline data exist for the case
     * @throws DaoException 
     */
    public static boolean timeEventsExistForPatient(int patientId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCopyNumberSegment.class);
            pstmt = con.prepareStatement("SELECT EXISTS(SELECT 1 FROM `clinical_event` WHERE `patient_id`=?)");
            pstmt.setInt(1, patientId);
            rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1)==1;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCopyNumberSegment.class, con, pstmt, rs);
        }
    }
    
    public static void deleteByCancerStudyId(int cancerStudyId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoClinicalEvent.class);
            
            pstmt = con.prepareStatement("DELETE FROM clinical_event_data WHERE clinical_event_id IN "
                    + "(SELECT clinical_event_id FROM clinical_event WHERE patient_id in (SELECT internal_id FROM patient where cancer_study_id=?))");
            pstmt.setInt(1, cancerStudyId);
            pstmt.executeUpdate();
            
            pstmt = con.prepareStatement("DELETE FROM clinical_event WHERE patient_id in (SELECT internal_id FROM patient where cancer_study_id=?)");
            pstmt.setInt(1, cancerStudyId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoClinicalEvent.class, con, pstmt, rs);
        }
    }

    public static void deleteByPatientId(int patientId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoClinicalEvent.class);

            pstmt = con.prepareStatement("DELETE FROM clinical_event_data WHERE clinical_event_id IN (" +
                    "SELECT clinical_event_id FROM clinical_event WHERE patient_id = ?)");
            pstmt.setInt(1, patientId);
            pstmt.executeUpdate();
            pstmt.close();
            pstmt = con.prepareStatement("DELETE FROM clinical_event WHERE patient_id = ?");
            pstmt.setInt(1, patientId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoClinicalEvent.class, con, pstmt, rs);
        }
    }
    
    public static void deleteAllRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoClinicalData.class);
            pstmt = con.prepareStatement("TRUNCATE TABLE clinical_event_data");
            pstmt.executeUpdate();
            pstmt = con.prepareStatement("TRUNCATE TABLE clinical_event");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoClinicalData.class, con, pstmt, rs);
        }
    }
}
