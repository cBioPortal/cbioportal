package org.mskcc.cgds.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import org.apache.commons.lang.StringUtils;
import org.mskcc.cgds.model.ExtendedMutation;

/**
 * Data access object for mutation_event and case_mutation_event tables
 */
public final class DaoMutationEvent {

    /**
     * Private Constructor (Singleton pattern).
     */
    private DaoMutationEvent() {
    }

    public static int addMutation(ExtendedMutation mutation) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            long eventId = addMutationEvent(mutation, con);
            
            if (eventExists(eventId, mutation.getCaseId(), con)) {
                return 0;
            }
            
            pstmt = con.prepareStatement
		("INSERT INTO case_mutation_event (`MUTATION_EVENT_ID`, `CASE_ID`, `GENETIC_PROFILE_ID`, "
                    + "`VALIDATION_STATUS`) VALUES(?,?,?,?)");
            pstmt.setLong(1, eventId);
            pstmt.setString(2, mutation.getCaseId());
            pstmt.setInt(3, mutation.getGeneticProfileId());
            pstmt.setString(4, mutation.getValidationStatus());
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }
    
    private static long addMutationEvent(ExtendedMutation mutation, Connection con) throws DaoException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = con.prepareStatement
		("SELECT `MUTATION_EVENT_ID` FROM mutation_event WHERE `ENTREZ_GENE_ID`=? "
                    + "AND `AMINO_ACID_CHANGE`=? AND `MUTATION_STATUS`=?");
            pstmt.setLong(1, mutation.getEntrezGeneId());
            pstmt.setString(2, mutation.getAminoAcidChange());
            pstmt.setString(3, mutation.getMutationStatus());
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
            
            // no existing, create new
            pstmt = con.prepareStatement
		("INSERT INTO mutation_event (`ENTREZ_GENE_ID`, `AMINO_ACID_CHANGE`, "
                    + "`MUTATION_STATUS`, `MUTATION_TYPE`) VALUES(?,?,?,?)");
            pstmt.setLong(1, mutation.getEntrezGeneId());
            pstmt.setString(2, mutation.getAminoAcidChange());
            pstmt.setString(3, mutation.getMutationStatus());
            pstmt.setString(4, mutation.getMutationType());
            pstmt.executeUpdate();
            return addMutationEvent(mutation, con);
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }
    
    private static boolean eventExists(long eventId, String caseId, Connection con) throws DaoException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = con.prepareStatement
		("SELECT count(*) FROM case_mutation_event WHERE `MUTATION_EVENT_ID`=? AND `CASE_ID`=?");
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
    
    public static List<ExtendedMutation> getMutationEvents(String caseId, int profileId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
		("SELECT case_mutation_event.MUTATION_EVENT_ID, CASE_ID, GENETIC_PROFILE_ID,"
                    + " VALIDATION_STATUS, ENTREZ_GENE_ID, MUTATION_STATUS, AMINO_ACID_CHANGE, MUTATION_TYPE"
                    + " FROM case_mutation_event, mutation_event"
                    + " WHERE `CASE_ID`=? AND `GENETIC_PROFILE_ID`=? AND"
                    + " case_mutation_event.MUTATION_EVENT_ID=mutation_event.MUTATION_EVENT_ID");
            pstmt.setString(1, caseId);
            pstmt.setInt(2, profileId);
            rs = pstmt.executeQuery();
            List<ExtendedMutation> events = new ArrayList<ExtendedMutation>();
            while (rs.next()) {
                ExtendedMutation event = new ExtendedMutation(
                        DaoGeneOptimized.getInstance().getGene(rs.getLong("ENTREZ_GENE_ID")),
                        rs.getString("VALIDATION_STATUS"),
                        rs.getString("MUTATION_STATUS"),
                        rs.getString("MUTATION_TYPE"));
                event.setCaseId(rs.getString("CASE_ID"));
                event.setGeneticProfileId(rs.getInt("GENETIC_PROFILE_ID"));
                event.setAminoAcidChange(rs.getString("AMINO_ACID_CHANGE"));
                event.setMutationEventId(rs.getLong("MUTATION_EVENT_ID"));
                events.add(event);
            }
            return events;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }
    
    public static Map<String, Set<Long>> getCasesWithMutations(Collection<Long> eventIds) throws DaoException {
        return getCasesWithMutations(StringUtils.join(eventIds, ","));
    }
    
    public static Map<String, Set<Long>> getCasesWithMutations(String concatEventIds) throws DaoException {
        if (concatEventIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            String sql = "SELECT `CASE_ID`, `MUTATION_EVENT_ID` FROM case_mutation_event"
                    + " WHERE `MUTATION_EVENT_ID` IN ("
                    + concatEventIds + ")";
            pstmt = con.prepareStatement(sql);
            
            Map<String, Set<Long>>  map = new HashMap<String, Set<Long>> ();
            rs = pstmt.executeQuery();
            while (rs.next()) {
                String caseId = rs.getString("CASE_ID");
                long eventId = rs.getLong("MUTATION_EVENT_ID");
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
    
    public static Map<Long, Integer> countSamplesWithMutationEvents(Collection<Long> eventIds, int profileId) throws DaoException {
        return countSamplesWithMutationEvents(StringUtils.join(eventIds, ","), profileId);
    }
    
    public static Map<Long, Integer> countSamplesWithMutationEvents(String concatEventIds, int profileId) throws DaoException {
        if (concatEventIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            String sql = "SELECT `MUTATION_EVENT_ID`, count(*) FROM case_mutation_event"
                    + " WHERE `GENETIC_PROFILE_ID`=" + profileId
                    + " AND `MUTATION_EVENT_ID` IN ("
                    + concatEventIds
                    + ") GROUP BY `MUTATION_EVENT_ID`";
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
    
    public static Map<Long, Integer> countSamplesWithMutatedGenes(Collection<Long> entrezGeneIds, int profileId) throws DaoException {
        return countSamplesWithMutatedGenes(StringUtils.join(entrezGeneIds, ","), profileId);
    }
    
    public static Map<Long, Integer> countSamplesWithMutatedGenes(String concatEntrezGeneIds, int profileId) throws DaoException {
        if (concatEntrezGeneIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            String sql = "SELECT ENTREZ_GENE_ID, count(DISTINCT CASE_ID)"
                    + " FROM case_mutation_event, mutation_event"
                    + " WHERE GENETIC_PROFILE_ID=" + profileId
                    + " AND case_mutation_event.MUTATION_EVENT_ID=mutation_event.MUTATION_EVENT_ID"
                    + " AND ENTREZ_GENE_ID IN ("
                    + concatEntrezGeneIds
                    + ") GROUP BY `ENTREZ_GENE_ID`";
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
    
    public static Map<Long, Integer> countSamplesWithMutatedGenesByEventIds(
            Collection<Long> eventIds, int profileId) throws DaoException {
        return countSamplesWithMutatedGenesByEventIds(StringUtils.join(eventIds, ","), profileId);
    }
    
    public static Map<Long, Integer> countSamplesWithMutatedGenesByEventIds(
            String concatEventIds, int profileId) throws DaoException {
        if (concatEventIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            String sql = "SELECT ENTREZ_GENE_ID, count(DISTINCT CASE_ID)"
                    + " FROM case_mutation_event, mutation_event"
                    + " WHERE GENETIC_PROFILE_ID=" + profileId
                    + " AND case_mutation_event.MUTATION_EVENT_ID=mutation_event.MUTATION_EVENT_ID"
                    + " AND mutation_event.MUTATION_EVENT_ID IN ("
                    + concatEventIds
                    + ") GROUP BY `ENTREZ_GENE_ID`";
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

}