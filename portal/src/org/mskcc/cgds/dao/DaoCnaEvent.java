/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cgds.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.mskcc.cgds.model.CnaEvent;
import java.util.Map;
import java.util.EnumMap;

/**
 *
 * @author jgao
 */
public final class DaoCnaEvent {
    private DaoCnaEvent() {}
    
    public static int addCaseCnaEvent(CnaEvent cnaEvent) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            long eventId = addCnaEvent(cnaEvent, con);
            
            if (eventExists(eventId, cnaEvent.getCaseId(), cnaEvent.getCnaProfileId(), con)) {
                return 0;
            }
            
            pstmt = con.prepareStatement
		("INSERT INTO case_cna_event (`CNA_EVENT_ID`, `CASE_ID`, `GENETIC_PROFILE_ID`) VALUES(?,?,?)");
            pstmt.setLong(1, eventId);
            pstmt.setString(2, cnaEvent.getCaseId());
            pstmt.setInt(3, cnaEvent.getCnaProfileId());
            
            int rows = pstmt.executeUpdate();
            return rows;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }
    
    /**
     * add event and return the event id
     * @param cnaEvent
     * @param con
     * @return
     * @throws DaoException 
     */
    private static long addCnaEvent(CnaEvent cnaEvent, Connection con) throws DaoException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = con.prepareStatement
		("SELECT `CNA_EVENT_ID` FROM cna_event WHERE `ENTREZ_GENE_ID`=? AND `ALTERATION`=?");
            pstmt.setLong(1, cnaEvent.getEntrezGeneId());
            pstmt.setShort(2, cnaEvent.getAlteration().getCode());
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
            
            // no existing, create new
            pstmt = con.prepareStatement
		("INSERT INTO cna_event (`ENTREZ_GENE_ID`, `ALTERATION`) VALUES(?,?)");
            pstmt.setLong(1, cnaEvent.getEntrezGeneId());
            pstmt.setInt(2, cnaEvent.getAlteration().getCode());
            pstmt.executeUpdate();
            return addCnaEvent(cnaEvent, con);
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }
    
    private static boolean eventExists(long eventId, String caseId, int profileId, Connection con) throws DaoException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = con.prepareStatement
		("SELECT count(*) FROM case_cna_event WHERE `CNA_EVENT_ID`=? AND `CASE_ID`=? AND `GENETIC_PROFILE_ID`=?");
            pstmt.setLong(1, eventId);
            pstmt.setString(2, caseId);
            pstmt.setInt(3, profileId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1)>0;
            }
            return false;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }
    
    public static List<CnaEvent> getCnaEvents(String caseId, int profileId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
		("SELECT case_cna_event.CNA_EVENT_ID, CASE_ID, GENETIC_PROFILE_ID, ENTREZ_GENE_ID,"
                    + " ALTERATION FROM case_cna_event, cna_event "
                    + "WHERE `CASE_ID`=? AND `GENETIC_PROFILE_ID`=? AND case_cna_event.CNA_EVENT_ID=cna_event.CNA_EVENT_ID");
            pstmt.setString(1, caseId);
            pstmt.setInt(2, profileId);
            rs = pstmt.executeQuery();
            List<CnaEvent> events = new ArrayList<CnaEvent>();
            while (rs.next()) {
                CnaEvent event = new CnaEvent(rs.getString("CASE_ID"), rs.getInt("GENETIC_PROFILE_ID"),
                        rs.getLong("ENTREZ_GENE_ID"), rs.getShort("ALTERATION"));
                event.setEventId(rs.getLong("CNA_EVENT_ID"));
                events.add(event);
            }
            return events;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }
    
    public static int countSamplesWithCnaEvents(long eventId, int profileId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
		("SELECT count(*) FROM case_cna_event WHERE `CNA_EVENT_ID`=? AND `GENETIC_PROFILE_ID`=?");
            pstmt.setLong(1, eventId);
            pstmt.setInt(2, profileId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }
}
