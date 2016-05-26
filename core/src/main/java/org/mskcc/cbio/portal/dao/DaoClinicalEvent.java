/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
import org.apache.commons.lang.StringUtils;
import org.mskcc.cbio.portal.model.ClinicalEvent;

/**
 *
 * @author gaoj
 */
public final class DaoClinicalEvent {
    private DaoClinicalEvent() {}
    
    public static int addClinicalEvent(ClinicalEvent clinicalEvent) {
        if (!MySQLbulkLoader.isBulkLoad()) {
            throw new IllegalStateException("Only buld load mode is allowed for importing clinical events");
        }
        
        MySQLbulkLoader.getMySQLbulkLoader("clinical_event").insertRecord(
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
            MySQLbulkLoader.getMySQLbulkLoader("clinical_event_data").insertRecord(
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
                pstmt = con.prepareStatement("SELECT * FROM clinical_event WHERE PATIENT_ID=?");
            } else {
                pstmt = con.prepareStatement("SELECT * FROM clinical_event WHERE PATIENT_ID=? AND EVENT_TYPE=?");
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
                pstmt = con.prepareStatement("SELECT * FROM clinical_event_data WHERE CLINICAL_EVENT_ID IN ("
                        + StringUtils.join(clinicalEvents.keySet(), ",") + ")");

                rs = pstmt.executeQuery();
                while (rs.next()) {
                   long eventId = rs.getLong("CLINICAL_EVENT_ID");
                   clinicalEvents.get(eventId).addEventDatum(rs.getString("KEY"), rs.getString("VALUE"));
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
        clinicalEvent.setClinicalEventId(rs.getLong("CLINICAL_EVENT_ID"));
        clinicalEvent.setPatientId(rs.getInt("PATIENT_ID"));
        clinicalEvent.setStartDate(JdbcUtil.readLongFromResultSet(rs, "START_DATE"));
        clinicalEvent.setStopDate(JdbcUtil.readLongFromResultSet(rs, "STOP_DATE"));
        clinicalEvent.setEventType(rs.getString("EVENT_TYPE"));
        return clinicalEvent;
    }
    
    public static long getLargestClinicalEventId() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoClinicalEvent.class);
            pstmt = con.prepareStatement
                    ("SELECT MAX(`CLINICAL_EVENT_ID`) FROM `clinical_event`");
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
            pstmt = con.prepareStatement("SELECT EXISTS(SELECT 1 FROM `clinical_event` WHERE `PATIENT_ID`=?)");
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
            
            pstmt = con.prepareStatement("DELETE FROM clinical_event_data WHERE CLINICAL_EVENT_ID IN "
                    + "(SELECT CLINICAL_EVENT_ID FROM clinical_event WHERE PATIENT_ID in (SELECT INTERNAL_ID FROM patient where CANCER_STUDY_ID=?))");
            pstmt.setInt(1, cancerStudyId);
            pstmt.executeUpdate();
            
            pstmt = con.prepareStatement("DELETE FROM clinical_event WHERE PATIENT_ID in (SELECT INTERNAL_ID FROM patient where CANCER_STUDY_ID=?)");
            pstmt.setInt(1, cancerStudyId);
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
            JdbcUtil.disableForeignKeyCheck(con);
            pstmt = con.prepareStatement("TRUNCATE TABLE clinical_event_data");
            pstmt.executeUpdate();
            pstmt = con.prepareStatement("TRUNCATE TABLE clinical_event");
            pstmt.executeUpdate();
            JdbcUtil.enableForeignKeyCheck(con);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoClinicalData.class, con, pstmt, rs);
        }
    }
}
