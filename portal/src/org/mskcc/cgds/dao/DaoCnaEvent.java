/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cgds.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.mskcc.cgds.model.CnaEvent;

/**
 *
 * @author jgao
 */
public class DaoCnaEvent {
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
            pstmt.setString(2, cnaEvent.getAlteration());
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
            
            // no existing, create new
            pstmt = con.prepareStatement
		("INSERT INTO cna_event (`ENTREZ_GENE_ID`, `ALTERATION`) VALUES(?,?)");
            pstmt.setLong(1, cnaEvent.getEntrezGeneId());
            pstmt.setString(2, cnaEvent.getAlteration());
            pstmt.executeUpdate();
            return addCnaEvent(cnaEvent, con);
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }
    
    private static boolean eventExists(long eventId, String caseId, Connection con) throws DaoException {
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
}
