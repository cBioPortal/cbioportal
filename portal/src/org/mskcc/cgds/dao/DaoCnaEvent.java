/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cgds.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import org.apache.commons.lang.StringUtils;
import org.mskcc.cgds.model.CnaEvent;

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
            
            if (eventExists(eventId, cnaEvent.getCaseId(), con)) {
                return 0;
            }
            
            pstmt = con.prepareStatement
		("INSERT INTO case_cna_event (`CNA_EVENT_ID`, `CASE_ID`,"
                    + " `GENETIC_PROFILE_ID`) VALUES(?,?,?)");
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
		("SELECT `CNA_EVENT_ID` FROM cna_event"
                    + " WHERE `ENTREZ_GENE_ID`=? AND `ALTERATION`=?");
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
    
    private static boolean eventExists(long eventId, String caseId, Connection con)
            throws DaoException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = con.prepareStatement
		("SELECT count(*) FROM case_cna_event WHERE `CNA_EVENT_ID`=? AND `CASE_ID`=?");
            pstmt.setLong(1, eventId);
            pstmt.setString(2, caseId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1)>0;
            }
            return false;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }
    
    public static Map<String, Set<Long>> getCasesWithAlterations(
            Collection<Long> eventIds) throws DaoException {
        return getCasesWithAlterations(StringUtils.join(eventIds, ","));
    }
    
    public static Map<String, Set<Long>> getCasesWithAlterations(String concatEventIds)
            throws DaoException {
        if (concatEventIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            String sql = "SELECT `CASE_ID`, `CNA_EVENT_ID` FROM case_cna_event"
                    + " WHERE `CNA_EVENT_ID` IN ("
                    + concatEventIds + ")";
            pstmt = con.prepareStatement(sql);
            
            Map<String, Set<Long>>  map = new HashMap<String, Set<Long>> ();
            rs = pstmt.executeQuery();
            while (rs.next()) {
                String caseId = rs.getString("CASE_ID");
                long eventId = rs.getLong("CNA_EVENT_ID");
                Set<Long> events = map.get(caseId);
                if (events == null) {
                    events = new HashSet<Long>();
                    map.put(caseId, events);
                }
                events.add(eventId);
            }
            return map;
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
		("SELECT case_cna_event.CNA_EVENT_ID, CASE_ID, GENETIC_PROFILE_ID,"
                    + " ENTREZ_GENE_ID, ALTERATION FROM case_cna_event, cna_event"
                    + " WHERE `CASE_ID`=? AND `GENETIC_PROFILE_ID`=?"
                    + " AND case_cna_event.CNA_EVENT_ID=cna_event.CNA_EVENT_ID");
            pstmt.setString(1, caseId);
            pstmt.setInt(2, profileId);
            rs = pstmt.executeQuery();
            List<CnaEvent> events = new ArrayList<CnaEvent>();
            while (rs.next()) {
                CnaEvent event = new CnaEvent(rs.getString("CASE_ID"),
                        rs.getInt("GENETIC_PROFILE_ID"),
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
    
    public static Map<Long, Integer> countSamplesWithCnaEvents(Collection<Long> eventIds,
            int profileId) throws DaoException {
        return countSamplesWithCnaEvents(StringUtils.join(eventIds, ","), profileId);
    }
    
    public static Map<Long, Integer> countSamplesWithCnaEvents(String concatEventIds,
            int profileId) throws DaoException {
        if (concatEventIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            String sql = "SELECT `CNA_EVENT_ID`, count(*) FROM case_cna_event"
                    + " WHERE `GENETIC_PROFILE_ID`=" + profileId
                    + " and `CNA_EVENT_ID` IN ("
                    + concatEventIds
                    + ") GROUP BY `CNA_EVENT_ID`";
            pstmt = con.prepareStatement(sql);
            
            Map<Long, Integer> map = new HashMap<Long, Integer>();
            rs = pstmt.executeQuery();
            while (rs.next()) {
                map.put(rs.getLong(1), rs.getInt(2));
            }
            return map;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }
    
    public static Set<Long> getAlteredGenes(String concatEventIds, int profileId)
            throws DaoException {
        if (concatEventIds.isEmpty()) {
            return Collections.emptySet();
        }
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            String sql = "SELECT DISTINCT ENTREZ_GENE_ID FROM cna_event "
                    + "WHERE CNA_EVENT_ID in ("
                    +       concatEventIds
                    + ")";
            pstmt = con.prepareStatement(sql);
            
            Set<Long> set = new HashSet<Long>();
            rs = pstmt.executeQuery();
            while (rs.next()) {
                set.add(rs.getLong(1));
            }
            return set;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }
}
