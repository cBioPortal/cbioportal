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

import org.apache.commons.lang.StringUtils;
import org.mskcc.cbio.portal.model.CnaEvent;
import org.mskcc.cbio.portal.model.Sample;

import java.sql.*;
import java.util.*;

/**
 *
 * @author jgao
 */
public final class DaoCnaEvent {
    private DaoCnaEvent() {}
    
    public static void addCaseCnaEvent(CnaEvent cnaEvent, boolean newCnaEvent) throws DaoException {
        if (!MySQLbulkLoader.isBulkLoad()) {
            throw new DaoException("You have to turn on MySQLbulkLoader in order to insert sample_cna_event");
        }
        else {
        	long eventId = cnaEvent.getEventId();
        	if (newCnaEvent) {
                eventId = addCnaEventDirectly(cnaEvent);
                // update object based on new DB id (since this object is locally cached after this):
                cnaEvent.setEventId(eventId);
            }
            
            MySQLbulkLoader.getMySQLbulkLoader("sample_cna_event").insertRecord(
                    Long.toString(eventId),
                    Integer.toString(cnaEvent.getSampleId()),
                    Integer.toString(cnaEvent.getCnaProfileId())
                    );

            if ((cnaEvent.getDriverFilter() != null
                && !cnaEvent.getDriverFilter().isEmpty()
                && !cnaEvent.getDriverFilter().toLowerCase().equals("na"))
                || 
                (cnaEvent.getDriverTiersFilter() != null
                && !cnaEvent.getDriverTiersFilter().isEmpty()
                && !cnaEvent.getDriverTiersFilter().toLowerCase().equals("na"))) {
                MySQLbulkLoader.getMySQLbulkLoader("alteration_driver_annotation").insertRecord(
                    Long.toString(eventId),
                    Integer.toString(cnaEvent.getCnaProfileId()),
                    Integer.toString(cnaEvent.getSampleId()),
                    cnaEvent.getDriverFilter(),
                    cnaEvent.getDriverFilterAnnotation(),
                    cnaEvent.getDriverTiersFilter(),
                    cnaEvent.getDriverTiersFilterAnnotation()
                );
            }
        }
    }
    
    /**
     * Add new event directly and return the auto increment value.
     * 
     * @param cnaEvent
     * @return
     * @throws DaoException 
     */
    private static long addCnaEventDirectly(CnaEvent cnaEvent) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCnaEvent.class);
            pstmt = con.prepareStatement
                    ("INSERT INTO cna_event (" +
                            "`ENTREZ_GENE_ID`," +
                            "`ALTERATION` )" +
                            " VALUES(?,?)", Statement.RETURN_GENERATED_KEYS);
            pstmt.setLong(1, cnaEvent.getEntrezGeneId());
            pstmt.setShort(2, cnaEvent.getAlteration().getCode());
            pstmt.executeUpdate();
            rs = pstmt.getGeneratedKeys();
            rs.next();
            long newId = rs.getLong(1);
            return newId;
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
		("SELECT sample_cna_event.CNA_EVENT_ID,"
                    + " sample_cna_event.SAMPLE_ID,"
                    + " sample_cna_event.GENETIC_PROFILE_ID,"
                    + " ENTREZ_GENE_ID,"
                    + " ALTERATION,"
                    + " alteration_driver_annotation.DRIVER_FILTER,"
                    + " alteration_driver_annotation.DRIVER_FILTER_ANNOTATION,"
                    + " alteration_driver_annotation.DRIVER_TIERS_FILTER,"
                    + " alteration_driver_annotation.DRIVER_TIERS_FILTER_ANNOTATION"
                    + " FROM sample_cna_event"
                    + " LEFT JOIN alteration_driver_annotation ON"
                    + "  sample_cna_event.GENETIC_PROFILE_ID = alteration_driver_annotation.GENETIC_PROFILE_ID"
                    + "  and sample_cna_event.SAMPLE_ID = alteration_driver_annotation.SAMPLE_ID"
                    + "  and sample_cna_event.CNA_EVENT_ID = alteration_driver_annotation.ALTERATION_EVENT_ID,"
                    + " cna_event"
                    + " WHERE sample_cna_event.GENETIC_PROFILE_ID=?"
                    + " AND sample_cna_event.CNA_EVENT_ID=cna_event.CNA_EVENT_ID"
                    + (entrezGeneIds==null?"":" AND ENTREZ_GENE_ID IN(" + StringUtils.join(entrezGeneIds,",") + ")")
                    + " AND ALTERATION IN (" + StringUtils.join(cnaLevels,",") + ")"
                    + " AND sample_cna_event.SAMPLE_ID in ('"+StringUtils.join(sampleIds, "','")+"')");
            pstmt.setInt(1, profileId);
            rs = pstmt.executeQuery();
            List<CnaEvent> events = new ArrayList<CnaEvent>();
            while (rs.next()) {
                events.add(extractCnaEvent(rs));
            }
            return events;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCnaEvent.class, con, pstmt, rs);
        }
    }

    private static CnaEvent extractCnaEvent(ResultSet rs) throws SQLException {
        CnaEvent cnaEvent = new CnaEvent(rs.getInt("SAMPLE_ID"),
            rs.getInt("GENETIC_PROFILE_ID"),
            rs.getLong("ENTREZ_GENE_ID"),
            rs.getShort("ALTERATION"));
        cnaEvent.setEventId(rs.getLong("CNA_EVENT_ID"));
        cnaEvent.setDriverFilter(rs.getString("DRIVER_FILTER"));
        cnaEvent.setDriverFilterAnnotation(rs.getString("DRIVER_FILTER_ANNOTATION"));
        cnaEvent.setDriverTiersFilter(rs.getString("DRIVER_TIERS_FILTER"));
        cnaEvent.setDriverTiersFilterAnnotation(rs.getString("DRIVER_TIERS_FILTER_ANNOTATION"));
        return cnaEvent;
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
