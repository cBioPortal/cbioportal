package org.mskcc.cbio.cgds.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.*;
import org.apache.commons.lang.StringUtils;
import org.mskcc.cbio.cgds.model.CosmicMutationFrequency;
import org.mskcc.cbio.cgds.model.ExtendedMutation;

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
            long eventId = getMutationEventId(mutation, con);
            if (eventId==-1) {
                // no existing, create new
                pstmt = con.prepareStatement
                    ("INSERT INTO mutation_event (`ENTREZ_GENE_ID`, `AMINO_ACID_CHANGE`, "
                        + "`MUTATION_STATUS`, `MUTATION_TYPE`,`CHR`,`START_POSITION`,"
                        + "`END_POSITION`) VALUES(?,?,?,?,?,?,?)");
                pstmt.setLong(1, mutation.getEntrezGeneId());
                pstmt.setString(2, mutation.getProteinChange());
                pstmt.setString(3, mutation.getMutationStatus());
                pstmt.setString(4, mutation.getMutationType());
                pstmt.setString(5, mutation.getChr());
                pstmt.setLong(6, mutation.getStartPosition());
                pstmt.setLong(7, mutation.getEndPosition());
                pstmt.executeUpdate();
                eventId = getMutationEventId(mutation, con);
                
                // add cosmic
                for (CosmicMutationFrequency cosmic :
                        parseCosmic(mutation.getEntrezGeneId(), mutation.getOncotatorCosmicOverlapping())) {
                    importCosmic(eventId, cosmic, con);
                }
                
                addMutationKeywords(mutation, eventId, con);
            }
            
            return eventId;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }
    
    private static long getMutationEventId(ExtendedMutation mutation, Connection con) throws DaoException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = con.prepareStatement
		("SELECT `MUTATION_EVENT_ID` FROM mutation_event WHERE `ENTREZ_GENE_ID`=? "
                    + "AND `AMINO_ACID_CHANGE`=? AND `MUTATION_STATUS`=?");
            pstmt.setLong(1, mutation.getEntrezGeneId());
            pstmt.setString(2, mutation.getProteinChange());
            pstmt.setString(3, mutation.getMutationStatus());
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            } else {
                return -1;
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }
    
    private static int addMutationKeywords(ExtendedMutation mutation,
            long mutationEventId, Connection con) throws DaoException {
        Set<String> keywords = extractMutationKeywords(mutation);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            int ret = 0;
            for (String keyword : keywords) {
                pstmt = con.prepareStatement("INSERT INTO mutation_event_keyword"
                        + " (`MUTATION_EVENT_ID`,`KEYWORD`) VALUES(?,?)");
                pstmt.setLong(1, mutationEventId);
                pstmt.setString(2, keyword);
                ret += pstmt.executeUpdate();
            }
            return ret;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }
    
    private static Set<String> extractMutationKeywords(ExtendedMutation mutation) {
        Set<String> keywords = new HashSet<String>();
        
        String type = mutation.getMutationType().toLowerCase();
        if (type.equals("Nonsense_Mutation") ||
            type.equals("Splice_Site") || 
            type.startsWith("Frame_Shift_") || 
            type.equals("Nonstop_Mutation")) {
            keywords.add("Truncating");
        } else if (type.equals("missense_mutation")) {
            String aa = mutation.getProteinChange();
            if (aa.equals("M1*")) {
                // non-start
                keywords.add("Truncating");
            } else {
                Pattern p = Pattern.compile("([A-Z][0-9]+)");
                Matcher m = p.matcher(aa);
                if (m.find()) {
                    keywords.add(m.group(1)+" Missense");
                }
            }
        } else if (type.equals("in_frame_ins")) {
            String aa = mutation.getProteinChange();
            Pattern p = Pattern.compile("(0-9)+_(0-9)+");
            Matcher m = p.matcher(aa);
            if (m.find()) {
                int s = Integer.parseInt(m.group(1));
                int t = Integer.parseInt(m.group(2));
                for (int i=s; i<=t; i++) {
                    keywords.add(Integer.toBinaryString(i) + " In_Frame_Ins");
                }
            }
        } else if (type.equals("in_frame_del")) {
            String aa = mutation.getProteinChange();
            Pattern p = Pattern.compile("(0-9)+_(0-9)+");
            Matcher m = p.matcher(aa);
            if (m.find()) {
                int s = Integer.parseInt(m.group(1));
                int t = Integer.parseInt(m.group(2));
                for (int i=s; i<=t; i++) {
                    keywords.add(Integer.toBinaryString(i) + " In_Frame_Del");
                }
            } else {
                p = Pattern.compile("([0-9]+)");
                m = p.matcher(aa);
                if (m.find()) {
                    keywords.add(m.group(1) + " In_Frame_Del");
                }
            }
        }
            
        // TODO: how about Translation_Start_Site
        
        return keywords;
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
                    + " VALIDATION_STATUS, ENTREZ_GENE_ID, MUTATION_STATUS, AMINO_ACID_CHANGE, MUTATION_TYPE,"
                    + " CHR, START_POSITION, END_POSITION"
                    + " FROM case_mutation_event, mutation_event"
                    + " WHERE `CASE_ID`=? AND `GENETIC_PROFILE_ID`=? AND"
                    + " case_mutation_event.MUTATION_EVENT_ID=mutation_event.MUTATION_EVENT_ID");
            pstmt.setString(1, caseId);
            pstmt.setInt(2, profileId);
            rs = pstmt.executeQuery();
            
            return extractMutations(rs);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }
    
    private static List<ExtendedMutation> extractMutations(ResultSet rs) throws SQLException, DaoException {
        List<ExtendedMutation> events = new ArrayList<ExtendedMutation>();
        while (rs.next()) {
            ExtendedMutation event = new ExtendedMutation(
                    DaoGeneOptimized.getInstance().getGene(rs.getLong("ENTREZ_GENE_ID")),
                    rs.getString("VALIDATION_STATUS"),
                    rs.getString("MUTATION_STATUS"),
                    rs.getString("MUTATION_TYPE"));
            event.setCaseId(rs.getString("CASE_ID"));
            event.setGeneticProfileId(rs.getInt("GENETIC_PROFILE_ID"));
            event.setProteinChange(rs.getString("AMINO_ACID_CHANGE"));
            event.setChr(rs.getString("CHR"));
            event.setStartPosition(rs.getLong("START_POSITION"));
            event.setEndPosition(rs.getLong("END_POSITION"));
            event.setMutationEventId(rs.getLong("MUTATION_EVENT_ID"));
            events.add(event);
        }
        return events;
    }
    
    /**
     * return the number of mutations for each case
     * @param caseIds if null, return all case available
     * @param profileId
     * @return Map &lt; case id, mutation count &gt;
     * @throws DaoException 
     */
    public static Map<String, Integer> countMutationEvents(
            int profileId, Collection<String> caseIds) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            String sql;
            if (caseIds==null) {
                sql = "SELECT `CASE_ID`, count(*) FROM case_mutation_event"
                        + " WHERE `GENETIC_PROFILE_ID`=" + profileId
                        + " GROUP BY `CASE_ID`";
                
            } else {
                sql = "SELECT `CASE_ID`, count(*) FROM case_mutation_event"
                        + " WHERE `GENETIC_PROFILE_ID`=" + profileId
                        + " AND `CASE_ID` IN ('"
                        + StringUtils.join(caseIds,"','")
                        + "') GROUP BY `CASE_ID`";
            }
            pstmt = con.prepareStatement(sql);
            
            Map<String, Integer> map = new HashMap<String, Integer>();
            rs = pstmt.executeQuery();
            while (rs.next()) {
                map.put(rs.getString(1), rs.getInt(2));
            }
            return map;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }
    
    /**
     * get events for each case
     * @param concatEventIds
     * @return Map &lt; case id, list of event ids &gt;
     * @throws DaoException 
     */
    public static Map<String, Set<Long>> getCasesWithMutations(Collection<Long> eventIds) throws DaoException {
        return getCasesWithMutations(StringUtils.join(eventIds, ","));
    }
    
    /**
     * get events for each case
     * @param concatEventIds event ids concatenated by comma (,)
     * @return Map &lt; case id, list of event ids &gt;
     * @throws DaoException 
     */
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
    
    /**
     * return the number of samples for each mutation event
     * @param concatEventIds
     * @param profileId
     * @return Map &lt; event id, sampleCount &gt;
     * @throws DaoException 
     */
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
    
    /**
     * return the number of samples for each mutated genes
     * @param concatEntrezGeneIds
     * @param profileId
     * @return Map &lt; entrez, sampleCount &gt;
     * @throws DaoException 
     */
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
    
    public static Set<Long> getGenesOfMutations(
            Collection<Long> eventIds, int profileId) throws DaoException {
        return getGenesOfMutations(StringUtils.join(eventIds, ","), profileId);
    }
    
    /**
     * return entrez gene ids of the mutations specified by their mutaiton event ids.
     * @param concatEventIds
     * @param profileId
     * @return
     * @throws DaoException 
     */
    public static Set<Long> getGenesOfMutations(String concatEventIds, int profileId)
            throws DaoException {
        if (concatEventIds.isEmpty()) {
            return Collections.emptySet();
        }
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            String sql = "SELECT DISTINCT ENTREZ_GENE_ID FROM mutation_event "
                    + "WHERE MUTATION_EVENT_ID in ("
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
    
    private static List<CosmicMutationFrequency> parseCosmic(long entrez, String strCosmic) {
        if (strCosmic==null || strCosmic.isEmpty()) {
            return Collections.emptyList();
        }
        
        String[] parts = strCosmic.split("\\|");
        List<CosmicMutationFrequency> list = new ArrayList<CosmicMutationFrequency>(parts.length);
        Pattern p = Pattern.compile("(p\\..+)\\(([0-9]+)\\)");
        for (String part : parts) {
            Matcher m = p.matcher(part);
            if (m.matches()) {
                String aa = m.group(1);
                int count = Integer.parseInt(m.group(2));
                list.add(new CosmicMutationFrequency(entrez, aa, count));
            } else {
                System.err.println("wrong cosmic string: "+part);
            }
        }
        
        return list;
    }
    
    private static int importCosmic(long eventId, CosmicMutationFrequency cosmic,
            Connection con) throws DaoException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            int n = importCosmic(cosmic, con);
            CosmicMutationFrequency cmf = getCosmicMutationFrequency(cosmic.getEntrezGeneId(),
                    cosmic.getAminoAcidChange(), con);
            
            pstmt = con.prepareStatement("INSERT INTO mutation_event_cosmic_mapping"
                    + " (`MUTATION_EVENT_ID`,`COSMIC_MUTATION_ID`) VALUES(?,?)");
            pstmt.setLong(1, eventId);
            pstmt.setInt(2, cmf.getId());
            n += pstmt.executeUpdate();
            return n;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
        
    }
    
    private static int importCosmic(CosmicMutationFrequency cosmic, Connection con) throws DaoException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            if (null != getCosmicMutationFrequency(
                    cosmic.getEntrezGeneId(), cosmic.getAminoAcidChange(),con)) {
                return 0;
            }
            
            pstmt = con.prepareStatement("INSERT INTO cosmic_mutation (`ENTREZ_GENE_ID`,"
                    + "`AMINO_ACID_CHANGE`,`COUNT`) VALUES(?,?,?)");
            pstmt.setLong(1, cosmic.getEntrezGeneId());
            pstmt.setString(2, cosmic.getAminoAcidChange());
            pstmt.setInt(3, cosmic.getFrequency());
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }
    
    public static CosmicMutationFrequency getCosmicMutationFrequency(long entrez,
            String aaChange) throws DaoException {
        return getCosmicMutationFrequency(entrez, aaChange, null);
    }
    
    private static CosmicMutationFrequency getCosmicMutationFrequency(long entrez,
            String aaChange, Connection con) throws DaoException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            if (con==null) {
                con = JdbcUtil.getDbConnection();
            }
            
            String sql = "SELECT * FROM cosmic_mutation "
                    + "WHERE `ENTREZ_GENE_ID`=? AND `AMINO_ACID_CHANGE`=?";
            pstmt = con.prepareStatement(sql);
            pstmt.setLong(1, entrez);
            pstmt.setString(2, aaChange);
            
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return new CosmicMutationFrequency(rs.getInt("COSMIC_MUTATION_ID"),
                        entrez, aaChange, rs.getInt("COUNT"));
            }
            return null;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
        
    }
    
    public static Map<Long, List<CosmicMutationFrequency>> getCosmicMutationFrequency(
            Collection<Long> mutationEventIds)  throws DaoException {
        return getCosmicMutationFrequency(StringUtils.join(mutationEventIds,","));
    }

    /**
     * get cosmic data for a mutation event
     * @param strMutationEventIds
     * @return Map &lt; mutation event id &gt; , list of cosmic &gt; &gt;
     * @throws DaoException 
     */
    public static Map<Long, List<CosmicMutationFrequency>> getCosmicMutationFrequency(
            String strMutationEventIds) throws DaoException {
        if (strMutationEventIds==null || strMutationEventIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            String sql = "SELECT MUTATION_EVENT_ID, cosmic_mutation.COSMIC_MUTATION_ID,"
                    + " `ENTREZ_GENE_ID`, `AMINO_ACID_CHANGE`, `COUNT`"
                    + " FROM cosmic_mutation, mutation_event_cosmic_mapping"
                    + " WHERE `MUTATION_EVENT_ID` IN ("+ strMutationEventIds +")"
                    + " AND cosmic_mutation.COSMIC_MUTATION_ID=mutation_event_cosmic_mapping.COSMIC_MUTATION_ID";
            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            Map<Long,List<CosmicMutationFrequency>> map = new HashMap<Long,List<CosmicMutationFrequency>>();
            while (rs.next()) {
                long eventId = rs.getLong(1);
                List<CosmicMutationFrequency> list = map.get(eventId);
                if (list==null) {
                    list = new ArrayList<CosmicMutationFrequency>();
                    map.put(eventId, list);
                }
                list.add(new CosmicMutationFrequency(rs.getInt(2),rs.getLong(3),rs.getString(4),rs.getInt(5)));
            }
            
            return map;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }
}