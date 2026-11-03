/*
 * Copyright (c) 2015 - 2016 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
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
import org.mskcc.cbio.portal.model.shared.GeneticAlterationType;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.slf4j.Logger;

/**
 * Analogous to and replaces the old DaoCancerType. A CancerStudy has a NAME and
 * DESCRIPTION. If PUBLIC is true a CancerStudy can be accessed by anyone,

/**
 * Data access object for Genetic Profile table
 */
public final class DaoGeneticProfile {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(DaoGeneticProfile.class);
    private DaoGeneticProfile() {}
    private static final String GENETIC_PROFILE_SEQUENCE = "seq_genetic_profile";
    private static final Map<String,GeneticProfile> byStableId = new HashMap<String,GeneticProfile>();
    private static final Map<Integer,GeneticProfile> byInternalId = new HashMap<Integer,GeneticProfile>();
    private static final Map<Integer,List<GeneticProfile>> byStudy = new HashMap<Integer,List<GeneticProfile>>();

    static {
        reCache();
    }

    public static synchronized void reCache() {
        byStableId.clear();
        byInternalId.clear();
        byStudy.clear();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGeneticProfile.class);
            log.info("Connection obtained for recaching genetic_profile: " + con);

            pstmt = con.prepareStatement
                    ("SELECT * FROM genetic_profile");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                GeneticProfile profileType = extractGeneticProfile(rs);
                cacheGeneticProfile(profileType);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            JdbcUtil.closeAll(DaoGeneticProfile.class, con, pstmt, rs);
        }
    }
    
    private static void cacheGeneticProfile(GeneticProfile profile) {
        byStableId.put(profile.getStableId(), profile);
        byInternalId.put(profile.getGeneticProfileId(), profile);
        List<GeneticProfile> list = byStudy.get(profile.getCancerStudyId());
        if (list==null) {
            list = new ArrayList<GeneticProfile>();
            byStudy.put(profile.getCancerStudyId(), list);
        }
        list.add(profile);
    }
   
    public static int addGeneticProfile(GeneticProfile profile) throws DaoException {
        //FIXME: this cast may cause problems in the future
        profile.setGeneticProfileId((int) ClickHouseAutoIncrement.nextId(GENETIC_PROFILE_SEQUENCE));
        log.info("Assigned genetic profile ID " + profile.getGeneticProfileId() +
                " to stable ID " + profile.getStableId());
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int rows = 0;
        try {
            con = JdbcUtil.getDbConnection(DaoGeneticProfile.class);
            log.info("Connection obtained for inserting genetic_profile: " + con);

            pstmt = con.prepareStatement
                    ("INSERT INTO genetic_profile (`genetic_profile_id`, `stable_id`, `cancer_study_id`, "+
                            "`genetic_alteration_type`, `datatype`, `name`, `description`, "+
                            "`show_profile_in_analysis_tab`, `pivot_threshold`, `sort_order`, `generic_assay_type`, `patient_level`) " +
                            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)");

            int index = 1;
            pstmt.setInt(index++, profile.getGeneticProfileId());
            pstmt.setString(index++, profile.getStableId());
            pstmt.setInt(index++, profile.getCancerStudyId());
            pstmt.setString(index++, profile.getGeneticAlterationType().name());
            pstmt.setString(index++, profile.getDatatype());
            pstmt.setString(index++, profile.getProfileName());
            pstmt.setString(index++, profile.getProfileDescription());
            pstmt.setBoolean(index++, profile.showProfileInAnalysisTab());

            // `pivot_threshold_value` and `value_sort_order` `generic_assay_type` fields are geneirc assay data specific.
            // These fields are set to null when not present in profile object.
            if (profile.getPivotThreshold() == null) {
                pstmt.setNull(index++, java.sql.Types.FLOAT);
            } else {
                pstmt.setFloat(index++, profile.getPivotThreshold());
            }
            if (profile.getSortOrder() == null) {
                pstmt.setNull(index++, java.sql.Types.INTEGER);
            } else {
                pstmt.setString(index++, profile.getSortOrder());
            }
            if (profile.getGenericAssayType() == null) {
                pstmt.setNull(index++, java.sql.Types.VARCHAR);
            } else {
                pstmt.setString(index++, profile.getGenericAssayType());
            }

            // default value is false
            pstmt.setBoolean(index++, profile.getPatientLevel());
            
            rows = pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGeneticProfile.class, con, pstmt, rs);
        }
        
        // Add new profile to the local cache directly rather than running a
        // full reCache().  On ClickHouse Cloud (SharedMergeTree) a SELECT
        // that follows an INSERT on a different connection may return a stale
        // snapshot that does not yet contain the freshly-inserted row.
        cacheGeneticProfile(profile);
        return rows;
    }

    /**
     * Updates a Genetic Profile Name and Description.
     * @param geneticProfileId  Genetic Profile ID.
     * @param name              New Genetic Profile Name.
     * @param description       New Genetic Profile Description.
     * @return                  Returns True if Genetic Profile was Updated.
     * @throws DaoException     Data Access Error.
     */
    public static boolean updateNameAndDescription (int geneticProfileId, String name, String description)
        throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean ret = false;
        try {
            con = JdbcUtil.getDbConnection(DaoGeneticProfile.class);
            pstmt = con.prepareStatement("UPDATE genetic_profile SET name=?, description=? " +
                    "WHERE genetic_profile_id=?");
            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.setInt(3, geneticProfileId);
            ret = pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGeneticProfile.class, con, pstmt, rs);
        }
        
        // Update the in-memory cache directly instead of a full reCache()
        GeneticProfile cached = byInternalId.get(geneticProfileId);
        if (cached != null) {
            cached.setProfileName(name);
            cached.setProfileDescription(description);
        }
        return ret;
    }    
    
    /**
     * Updates a Genetic Profile datatype
     */
    public static boolean updateDatatype(
        int geneticProfileId,
        String datatype
    ) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean ret;
        try {
            con = JdbcUtil.getDbConnection(DaoGeneticProfile.class);
            pstmt = con.prepareStatement("UPDATE genetic_profile SET datatype=? " +
                "WHERE genetic_profile_id=?");
            pstmt.setString(1, datatype);
            pstmt.setInt(2, geneticProfileId);
            ret = pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGeneticProfile.class, con, pstmt, rs);
        }

        // Update the in-memory cache directly instead of a full reCache()
        GeneticProfile cached = byInternalId.get(geneticProfileId);
        if (cached != null) {
            cached.setDatatype(datatype);
        }
        return ret;
    }
    
    public static int deleteGeneticProfile(GeneticProfile profile) throws DaoException {
       int rows = 0;
       Connection con = null;
       PreparedStatement pstmt = null;
       ResultSet rs = null;
       try {
           con = JdbcUtil.getDbConnection(DaoGeneticProfile.class);
           pstmt = con.prepareStatement("DELETE FROM genetic_profile WHERE stable_id = ?");
           pstmt.setString(1, profile.getStableId());
           rows = pstmt.executeUpdate();
       } catch (SQLException e) {
           throw new DaoException(e);
       } finally {
           JdbcUtil.closeAll(DaoGeneticProfile.class, con, pstmt, rs);
       }
       
       // Remove from in-memory cache directly instead of a full reCache()
       byStableId.remove(profile.getStableId());
       byInternalId.remove(profile.getGeneticProfileId());
       List<GeneticProfile> list = byStudy.get(profile.getCancerStudyId());
       if (list != null) {
           list.remove(profile);
       }
       return rows;
   }
    
    public static GeneticProfile getGeneticProfileByStableId(String stableId) {
        return byStableId.get(stableId);
    }

    public static GeneticProfile getGeneticProfileById(int geneticProfileId) {
        return byInternalId.get(geneticProfileId);
    }
    
    // TODO: UNIT TEST
    public static ArrayList <GeneticProfile> getGeneticProfiles (int[] geneticProfileIds) throws
            DaoException {
        ArrayList <GeneticProfile> geneticProfileList = new ArrayList <GeneticProfile>();
        for (int geneticProfileId:  geneticProfileIds) {
            GeneticProfile geneticProfile =
                    DaoGeneticProfile.getGeneticProfileById(geneticProfileId);
            if (geneticProfile != null) {
                geneticProfileList.add(geneticProfile);
            } else {
                throw new IllegalArgumentException ("Could not find genetic profile for:  "
                        + geneticProfileId);
            }
        }
        return geneticProfileList;
    }

    public static int getCount() {
        return byStableId.size();
    }

    private static GeneticProfile extractGeneticProfile(ResultSet rs) throws SQLException {
        GeneticProfile profileType = new GeneticProfile();
        profileType.setStableId(rs.getString("stable_id"));

        profileType.setCancerStudyId(rs.getInt("cancer_study_id"));
        profileType.setProfileName(rs.getString("name"));
        profileType.setProfileDescription(rs.getString("description"));
        try {
            profileType.setShowProfileInAnalysisTab(rs.getBoolean("show_profile_in_analysis_tab"));
        } catch (SQLException e) {
            profileType.setShowProfileInAnalysisTab(true);
        }
        profileType.setGeneticAlterationType(GeneticAlterationType.valueOf(rs.getString("genetic_alteration_type")));
        profileType.setDatatype(rs.getString("datatype"));
        profileType.setGeneticProfileId(rs.getInt("genetic_profile_id"));
        if (rs.getFloat("pivot_threshold") != 0) {
            profileType.setPivotThreshold(rs.getFloat("pivot_threshold"));
        }
        if (rs.getString("sort_order") != null && ! rs.getString("sort_order").equals("") ) {
            profileType.setSortOrder(rs.getString("sort_order"));
        }
        if (rs.getString("generic_assay_type") != null && ! rs.getString("generic_assay_type").equals("") ) {
            profileType.setGenericAssayType(rs.getString("generic_assay_type"));
        }
        profileType.setPatientLevel(rs.getBoolean("patient_level"));
        return profileType;
    }

    public static ArrayList<GeneticProfile> getAllGeneticProfiles(int cancerStudyId) {
        List<GeneticProfile> list = byStudy.get(cancerStudyId);
        if (list==null) {
            return new ArrayList<GeneticProfile>();
        }
        
        // TODO: refactor the code to use List
        return new ArrayList<GeneticProfile>(list);
    }

    public static void deleteAllRecords() throws DaoException {
        byStableId.clear();
        byInternalId.clear();
        byStudy.clear();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGeneticProfile.class);
            pstmt = con.prepareStatement("TRUNCATE TABLE genetic_profile");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGeneticProfile.class, con, pstmt, rs);
        }
    }
}
