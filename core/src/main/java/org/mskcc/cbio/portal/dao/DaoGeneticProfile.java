/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

package org.mskcc.cbio.portal.dao;

import org.mskcc.cbio.portal.model.GeneticAlterationType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.mskcc.cbio.portal.model.GeneticProfile;

/**
 * Data access object for Genetic Profile table
 */
public final class DaoGeneticProfile {
    private DaoGeneticProfile() {}
    
    private static final Map<String,GeneticProfile> byStableId = new HashMap<String,GeneticProfile>();
    private static final Map<Integer,GeneticProfile> byInternalId = new HashMap<Integer,GeneticProfile>();
    private static final Map<Integer,List<GeneticProfile>> byStudy = new HashMap<Integer,List<GeneticProfile>>();
    
    static {
        reCache();
    }
    
    private static synchronized void reCache() {
        byStableId.clear();
        byInternalId.clear();
        byStudy.clear();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGeneticProfile.class);

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
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int rows = 0;
        try {
            con = JdbcUtil.getDbConnection(DaoGeneticProfile.class);

            pstmt = con.prepareStatement
                    ("INSERT INTO genetic_profile (`STABLE_ID`, `CANCER_STUDY_ID`, `GENETIC_ALTERATION_TYPE`," +
                            "`DATATYPE`, `NAME`, `DESCRIPTION`, `SHOW_PROFILE_IN_ANALYSIS_TAB`) " +
                            "VALUES (?,?,?,?,?,?,?)");
            pstmt.setString(1, profile.getStableId());
            pstmt.setInt(2, profile.getCancerStudyId());
            pstmt.setString(3, profile.getGeneticAlterationType().toString());
            pstmt.setString(4, profile.getDatatype());
            pstmt.setString(5, profile.getProfileName());
            pstmt.setString(6, profile.getProfileDescription());
            pstmt.setBoolean(7, profile.showProfileInAnalysisTab());
            rows = pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGeneticProfile.class, con, pstmt, rs);
        }
        
        reCache();
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
            pstmt = con.prepareStatement("UPDATE genetic_profile SET NAME=?, DESCRIPTION=? " +
                    "WHERE GENETIC_PROFILE_ID=?");
            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.setInt(3, geneticProfileId);
            ret = pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGeneticProfile.class, con, pstmt, rs);
        }
        
        reCache();
        return ret;
    }
    
    public static int deleteGeneticProfile(GeneticProfile profile) throws DaoException {
       int rows = 0;
       Connection con = null;
       PreparedStatement pstmt = null;
       ResultSet rs = null;
       try {
           con = JdbcUtil.getDbConnection(DaoGeneticProfile.class);
           pstmt = con.prepareStatement("DELETE FROM genetic_profile WHERE STABLE_ID = ?");
           pstmt.setString(1, profile.getStableId());
           rows = pstmt.executeUpdate();
       } catch (SQLException e) {
           throw new DaoException(e);
       } finally {
           JdbcUtil.closeAll(DaoGeneticProfile.class, con, pstmt, rs);
       }
       
       reCache();
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
        profileType.setStableId(rs.getString("STABLE_ID"));

        profileType.setCancerStudyId(rs.getInt("CANCER_STUDY_ID"));
        profileType.setProfileName(rs.getString("NAME"));
        profileType.setProfileDescription(rs.getString("DESCRIPTION"));
        try {
            profileType.setShowProfileInAnalysisTab(rs.getBoolean("SHOW_PROFILE_IN_ANALYSIS_TAB"));
        } catch (SQLException e) {
            profileType.setShowProfileInAnalysisTab(true);
        }
        profileType.setGeneticAlterationType
                (GeneticAlterationType.getType(rs.getString("GENETIC_ALTERATION_TYPE")));
		profileType.setDatatype(rs.getString("DATATYPE"));
        profileType.setGeneticProfileId(rs.getInt("GENETIC_PROFILE_ID"));
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