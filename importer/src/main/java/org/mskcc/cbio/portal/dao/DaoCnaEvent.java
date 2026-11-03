/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.dao;

import java.sql.*;
import java.util.*;
import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.portal.model.CnaEvent;
import org.mskcc.cbio.portal.model.Sample;

/**
 *
 * @author jgao
 */
public final class DaoCnaEvent {

    private static final String CNA_EVENT_SEQUENCE = "seq_cna_event";
    private DaoCnaEvent() {}
    
    public static void addCaseCnaEvent(CnaEvent cnaEvent, boolean newCnaEvent) throws DaoException {
        if (!ClickHouseBulkLoader.isBulkLoad()) {
            throw new DaoException("You have to turn on ClickHouseBulkLoader in order to insert sample_cna_event");
        }
        else {
        	long eventId = cnaEvent.getEventId();
        	if (newCnaEvent) {
                eventId = addCnaEventDirectly(cnaEvent);
                // update object based on new DB id (since this object is locally cached after this):
                cnaEvent.setEventId(eventId);
            }
            
            ClickHouseBulkLoader.getClickHouseBulkLoader("sample_cna_event").insertRecord(
                    Long.toString(eventId),
                    Integer.toString(cnaEvent.getSampleId()),
                    Integer.toString(cnaEvent.getCnaProfileId()),
                    cnaEvent.getAnnotationJson()
            );

            if ((cnaEvent.getDriverFilter() != null
                && !cnaEvent.getDriverFilter().isEmpty()
                && !cnaEvent.getDriverFilter().toLowerCase().equals("na"))
                || 
                (cnaEvent.getDriverTiersFilter() != null
                && !cnaEvent.getDriverTiersFilter().isEmpty()
                && !cnaEvent.getDriverTiersFilter().toLowerCase().equals("na"))
            ) {
                ClickHouseBulkLoader
                    .getClickHouseBulkLoader("alteration_driver_annotation")
                    .insertRecord(
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
        long newId = ClickHouseAutoIncrement.nextId(CNA_EVENT_SEQUENCE);
        ClickHouseBulkLoader.getClickHouseBulkLoader("cna_event").insertRecord(
            Long.toString(newId),
            Long.toString(cnaEvent.getEntrezGeneId()),
            Integer.toString(cnaEvent.getAlteration())
        );
        return newId;
    }

    public static void removeSampleCnaEvents(int cnaProfileId, List<Integer> sampleIds) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            if (sampleIds.isEmpty()) {
                return;
            }
            con = JdbcUtil.getDbConnection(DaoCnaEvent.class);
            final Connection queryCon = con;
            ClickHouseBulkUploader.upload(sampleIds, stagingTable -> {
                try (PreparedStatement stmt = queryCon.prepareStatement(
                        "DELETE FROM alteration_driver_annotation " +
                        "WHERE `genetic_profile_id` = ? AND `sample_id` IN (SELECT id FROM " + stagingTable + ") " +
                        "AND `alteration_event_id` IN (SELECT `cna_event_id` FROM sample_cna_event WHERE `genetic_profile_id` = ? AND `sample_id` IN (SELECT id FROM " + stagingTable + "))")) {
                    stmt.setInt(1, cnaProfileId);
                    stmt.setInt(2, cnaProfileId);
                    stmt.executeUpdate();
                }
                try (PreparedStatement stmt = queryCon.prepareStatement(
                        "DELETE FROM sample_cna_event WHERE `genetic_profile_id` = ? AND `sample_id` IN (SELECT id FROM " + stagingTable + ")")) {
                    stmt.setInt(1, cnaProfileId);
                    stmt.executeUpdate();
                }
                return null;
            });
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
                    + " WHERE `cna_event_id` IN ("
                    + concatEventIds + ")";
            pstmt = con.prepareStatement(sql);
            
            Map<Sample, Set<Long>>  map = new HashMap<Sample, Set<Long>> ();
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Sample sample = DaoSample.getSampleById(rs.getInt("sample_id"));
                long eventId = rs.getLong("cna_event_id");
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
    
    public static List<CnaEvent> getCnaEvents(List<Integer> sampleIds, Collection<Long> entrezGeneIds , int profileId, Collection<Integer> cnaLevels) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCnaEvent.class);
            pstmt = con.prepareStatement
		("SELECT sample_cna_event.cna_event_id AS cna_event_id,"
                    + " sample_cna_event.sample_id AS sample_id,"
                    + " sample_cna_event.genetic_profile_id AS genetic_profile_id,"
                    + " cna_event.entrez_gene_id AS entrez_gene_id,"
                    + " cna_event.alteration AS alteration,"
                    + " alteration_driver_annotation.driver_filter AS driver_filter,"
                    + " alteration_driver_annotation.driver_filter_annotation AS driver_filter_annotation,"
                    + " alteration_driver_annotation.driver_tiers_filter AS driver_tiers_filter,"
                    + " alteration_driver_annotation.driver_tiers_filter_annotation AS driver_tiers_filter_annotation"
                    + " FROM sample_cna_event"
                    + " LEFT JOIN alteration_driver_annotation ON"
                    + "  sample_cna_event.genetic_profile_id = alteration_driver_annotation.genetic_profile_id"
                    + "  and sample_cna_event.sample_id = alteration_driver_annotation.sample_id"
                    + "  and sample_cna_event.cna_event_id = alteration_driver_annotation.alteration_event_id"
                    + " INNER JOIN cna_event ON sample_cna_event.cna_event_id=cna_event.cna_event_id"
                    + " WHERE sample_cna_event.genetic_profile_id=?"
                    + (entrezGeneIds==null?"":" AND entrez_gene_id IN(" + StringUtils.join(entrezGeneIds,",") + ")")
                    + " AND alteration IN (" + StringUtils.join(cnaLevels,",") + ")"
                    + " AND sample_cna_event.sample_id in ('"+StringUtils.join(sampleIds, "','")+"')");
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
        CnaEvent cnaEvent = new CnaEvent(rs.getInt("sample_id"),
            rs.getInt("genetic_profile_id"),
            rs.getLong("entrez_gene_id"),
            rs.getShort("alteration"));
        cnaEvent.setEventId(rs.getLong("cna_event_id"));
        cnaEvent.setDriverFilter(rs.getString("driver_filter"));
        cnaEvent.setDriverFilterAnnotation(rs.getString("driver_filter_annotation"));
        cnaEvent.setDriverTiersFilter(rs.getString("driver_tiers_filter"));
        cnaEvent.setDriverTiersFilterAnnotation(rs.getString("driver_tiers_filter_annotation"));
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
                    event.setEventId(rs.getLong("cna_event_id"));
                    event.setEntrezGeneId(rs.getLong("entrez_gene_id"));
                    event.setAlteration(rs.getShort("alteration"));
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
            String sql = "SELECT `entrez_gene_id`, `alteration`, count(*)"
                    + " FROM sample_cna_event, cna_event"
                    + " WHERE `genetic_profile_id`=" + profileId
                    + " and sample_cna_event.`cna_event_id`=cna_event.`cna_event_id`"
                    + " and `entrez_gene_id` IN ("
                    + concatEntrezGeneIds
                    + ") GROUP BY `entrez_gene_id`, `alteration`";
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
            String sql = "SELECT `cna_event_id`, count(*) FROM sample_cna_event"
                    + " WHERE `genetic_profile_id`=" + profileId
                    + " and `cna_event_id` IN ("
                    + concatEventIds
                    + ") GROUP BY `cna_event_id`";
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
            String sql = "SELECT DISTINCT entrez_gene_id FROM cna_event "
                    + "WHERE cna_event_id in ("
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
