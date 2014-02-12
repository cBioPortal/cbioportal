
package org.mskcc.cbio.portal.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import org.apache.commons.lang.StringUtils;
import org.mskcc.cbio.portal.model.*;

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
        Sample sample = DaoSample.getSampleByStableId(cnaEvent.getCaseId());
        try {
            con = JdbcUtil.getDbConnection(DaoCnaEvent.class);
            long eventId = addCnaEvent(cnaEvent, con);
            
            if (eventExists(eventId, sample.getInternalId(), cnaEvent.getCnaProfileId(), con)) {
                return 0;
            }
            
            pstmt = con.prepareStatement
		("INSERT INTO sample_cna_event (`CNA_EVENT_ID`, `SAMPLE_ID`,"
                    + " `GENETIC_PROFILE_ID`) VALUES(?,?,?)");
            pstmt.setLong(1, eventId);
            pstmt.setInt(2, sample.getInternalId());
            pstmt.setInt(3, cnaEvent.getCnaProfileId());
            
            return pstmt.executeUpdate();
        } catch (NullPointerException e) {
            throw new DaoException(e);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCnaEvent.class, con, pstmt, rs);
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
        PreparedStatement pstmt  ;
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
        } finally {
            JdbcUtil.closeAll(rs);
        }
    }
    
    private static boolean eventExists(long eventId, int internalSampleId, int cnaProfileId, Connection con)
            throws DaoException {
        PreparedStatement pstmt  ;
        ResultSet rs = null;

        try {
            pstmt = con.prepareStatement
		("SELECT count(*) FROM sample_cna_event WHERE `CNA_EVENT_ID`=? AND `SAMPLE_ID`=? AND `GENETIC_PROFILE_ID`=?");
            pstmt.setLong(1, eventId);
            pstmt.setInt(2, internalSampleId);
            pstmt.setInt(3, cnaProfileId);
            rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
            } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(rs);
        }
    }
    
    public static Map<Patient, Set<Long>> getCasesWithAlterations(
            Collection<Long> eventIds) throws DaoException {
        return getCasesWithAlterations(StringUtils.join(eventIds, ","));
    }
    
    public static Map<Patient, Set<Long>> getCasesWithAlterations(String concatEventIds)
            throws DaoException {
        if (concatEventIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCnaEvent.class);
            String sql = "SELECT * FROM sample_cna_event"
                    + " WHERE `CNA_EVENT_ID` IN ("
                    + concatEventIds + ")";
            pstmt = con.prepareStatement(sql);
            
            Map<Patient, Set<Long>>  map = new HashMap<Patient, Set<Long>> ();
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Sample sample = DaoSample.getSampleByInternalId(rs.getInt("SAMPLE_ID"));
                Patient patient = DaoPatient.getPatientByInternalId(sample.getInternalPatientId());
                long eventId = rs.getLong("CNA_EVENT_ID");
                Set<Long> events = map.get(patient);
                if (events == null) {
                    events = new HashSet<Long>();
                    map.put(patient, events);
                }
                events.add(eventId);
            }
            return map;
        } catch (NullPointerException e) {
            throw new DaoException(e);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCnaEvent.class, con, pstmt, rs);
        }
    }
    
    public static List<CnaEvent> getCnaEvents(String[] caseIds, int profileId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Integer> internalSampleIds = DaoSample.getInternalSampleIds(Arrays.asList(caseIds));
        try {
            con = JdbcUtil.getDbConnection(DaoCnaEvent.class);
            pstmt = con.prepareStatement
		("SELECT sample_cna_event.CNA_EVENT_ID, SAMPLE_ID, GENETIC_PROFILE_ID,"
                    + " ENTREZ_GENE_ID, ALTERATION FROM sample_cna_event, cna_event"
                    + " WHERE `GENETIC_PROFILE_ID`=?"
                    + " AND sample_cna_event.CNA_EVENT_ID=cna_event.CNA_EVENT_ID"
                    + " AND SAMPLE_ID in ('"+StringUtils.join(internalSampleIds, "','")+"')");
            pstmt.setInt(1, profileId);
            rs = pstmt.executeQuery();
            List<CnaEvent> events = new ArrayList<CnaEvent>();
            while (rs.next()) {
                try {
                    Sample sample = DaoSample.getSampleByInternalId(rs.getInt("SAMPLE_ID"));
                    CnaEvent event = new CnaEvent(sample.getStableId(),
                            rs.getInt("GENETIC_PROFILE_ID"),
                            rs.getLong("ENTREZ_GENE_ID"), rs.getShort("ALTERATION"));
                    event.setEventId(rs.getLong("CNA_EVENT_ID"));
                    events.add(event);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
            return events;
        } catch (NullPointerException e) {
            throw new DaoException(e);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCnaEvent.class, con, pstmt, rs);
        }
    }
    
    public static Map<Long, Map<Integer, Integer>> countSamplesWithCNAGenes(
            Collection<Long> entrezGeneIds, int profileId) throws DaoException {
        return countSamplesWithCNAGenes(StringUtils.join(entrezGeneIds, ","), profileId);
    }
    
    public static Map<Long, Map<Integer, Integer>> countSamplesWithCNAGenes(
            String concatEntrezGeneIds, int profileId) throws DaoException {
        if (concatEntrezGeneIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCnaEvent.class);
            String sql = "SELECT `ENTREZ_GENE_ID`, `ALTERATION`, count(*)"
                    + " FROM sample_cna_event, cna_event"
                    + " WHERE `GENETIC_PROFILE_ID`=" + profileId
                    + " and sample_cna_event.`CNA_EVENT_ID`=cna_event.`CNA_EVENT_ID`"
                    + " and `ENTREZ_GENE_ID` IN ("
                    + concatEntrezGeneIds
                    + ") GROUP BY `ENTREZ_GENE_ID`, `ALTERATION`";
            pstmt = con.prepareStatement(sql);
            
            Map<Long, Map<Integer, Integer>> map = new HashMap<Long, Map<Integer, Integer>>();
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Long entrez = rs.getLong(1);
                Integer alt = rs.getInt(2);
                Integer count = rs.getInt(3);
                Map<Integer, Integer> mapII = map.get(entrez);
                if (mapII==null) {
                    mapII = new HashMap<Integer, Integer>();
                    map.put(entrez, mapII);
                }
                mapII.put(alt, count);
            }
            return map;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCnaEvent.class, con, pstmt, rs);
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
            con = JdbcUtil.getDbConnection(DaoCnaEvent.class);
            String sql = "SELECT `CNA_EVENT_ID`, count(*) FROM sample_cna_event"
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
        } finally {
            JdbcUtil.closeAll(DaoCnaEvent.class, con, pstmt, rs);
        }
    }
    
    public static Set<Long> getAlteredGenes(String concatEventIds)
            throws DaoException {
        if (concatEventIds.isEmpty()) {
            return Collections.emptySet();
        }
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCnaEvent.class);
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
        } finally {
            JdbcUtil.closeAll(DaoCnaEvent.class, con, pstmt, rs);
        }
    }
}
