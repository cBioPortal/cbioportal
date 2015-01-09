
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
    
    public static int addCaseCnaEvent(CnaEvent cnaEvent, boolean newCnaEvent) throws DaoException {
        if (!MySQLbulkLoader.isBulkLoad()) {
            throw new DaoException("You have to turn on MySQLbulkLoader in order to insert sample_cna_event");
        }
        else {
            MySQLbulkLoader.getMySQLbulkLoader("sample_cna_event").insertRecord(
                    Long.toString(cnaEvent.getEventId()),
                    Integer.toString(cnaEvent.getSampleId()),
                    Integer.toString(cnaEvent.getCnaProfileId())
                    );
            
            if (newCnaEvent) {
                return addCnaEvent(cnaEvent) + 1;
            }
            else {
                return 1;
            }
        }
    }
    
    /**
     * add event and return the event id
     * @param cnaEvent
     * @param con
     * @return
     * @throws DaoException 
     */
    private static int addCnaEvent(CnaEvent cnaEvent) throws DaoException {
        if (!MySQLbulkLoader.isBulkLoad()) {
            throw new DaoException("You have to turn on MySQLbulkLoader in order to insert cna_event");
        } else {
            MySQLbulkLoader.getMySQLbulkLoader("cna_event").insertRecord(
                    Long.toString(cnaEvent.getEventId()),
                    Long.toString(cnaEvent.getEntrezGeneId()),
                    Short.toString(cnaEvent.getAlteration().getCode())
                    );
            return 1;
        }
    }
    
    public static long getLargestCnaEventId() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCnaEvent.class);
            pstmt = con.prepareStatement
                    ("SELECT MAX(`CNA_EVENT_ID`) FROM `cna_event`");
            rs = pstmt.executeQuery();
            return rs.next() ? rs.getLong(1) : 0;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCnaEvent.class, con, pstmt, rs);
        }
    }
    
    public static Map<Sample, Set<Long>> getSamplesWithAlterations(
            Collection<Long> eventIds) throws DaoException {
        return getSamplesWithAlterations(StringUtils.join(eventIds, ","));
    }
    
    public static Map<Sample, Set<Long>> getSamplesWithAlterations(String concatEventIds)
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
            
            Map<Sample, Set<Long>>  map = new HashMap<Sample, Set<Long>> ();
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Sample sample = DaoSample.getSampleById(rs.getInt("SAMPLE_ID"));
                long eventId = rs.getLong("CNA_EVENT_ID");
                Set<Long> events = map.get(sample);
                if (events == null) {
                    events = new HashSet<Long>();
                    map.put(sample, events);
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
    
    public static List<CnaEvent> getCnaEvents(List<Integer> sampleIds, Collection<Long> entrezGeneIds , int profileId, Collection<Short> cnaLevels) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCnaEvent.class);
            pstmt = con.prepareStatement
		("SELECT sample_cna_event.CNA_EVENT_ID, SAMPLE_ID, GENETIC_PROFILE_ID,"
                    + " ENTREZ_GENE_ID, ALTERATION FROM sample_cna_event, cna_event"
                    + " WHERE `GENETIC_PROFILE_ID`=?"
                    + " AND sample_cna_event.CNA_EVENT_ID=cna_event.CNA_EVENT_ID"
                    + (entrezGeneIds==null?"":" AND ENTREZ_GENE_ID IN(" + StringUtils.join(entrezGeneIds,",") + ")")
                    + " AND ALTERATION IN (" + StringUtils.join(cnaLevels,",") + ")"
                    + " AND SAMPLE_ID in ('"+StringUtils.join(sampleIds, "','")+"')");
            pstmt.setInt(1, profileId);
            rs = pstmt.executeQuery();
            List<CnaEvent> events = new ArrayList<CnaEvent>();
            while (rs.next()) {
                try {
                    Sample sample = DaoSample.getSampleById(rs.getInt("SAMPLE_ID"));
                    CnaEvent event = new CnaEvent(sample.getInternalId(),
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
    
    public static List<CnaEvent.Event> getAllCnaEvents() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCnaEvent.class);
            pstmt = con.prepareStatement
		("SELECT * FROM cna_event");
            rs = pstmt.executeQuery();
            List<CnaEvent.Event> events = new ArrayList<CnaEvent.Event>();
            while (rs.next()) {
                try {
                    CnaEvent.Event event = new CnaEvent.Event();
                    event.setEventId(rs.getLong("CNA_EVENT_ID"));
                    event.setEntrezGeneId(rs.getLong("ENTREZ_GENE_ID"));
                    event.setAlteration(rs.getShort("ALTERATION"));
                    events.add(event);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
            return events;
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
