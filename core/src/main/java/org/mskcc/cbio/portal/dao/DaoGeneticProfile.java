/*
 * Copyright (c) 2015 - 2016 Memorial Sloan-Kettering Cancer Center.
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

import java.sql.*;
import java.util.*;
import org.mskcc.cbio.portal.model.*;
import org.cbioportal.service.GeneticProfileService;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

/**
 * Analogous to and replaces the old DaoCancerType. A CancerStudy has a NAME and
 * DESCRIPTION. If PUBLIC is true a CancerStudy can be accessed by anyone,

/**
 * Data access object for Genetic Profile table
 */
public final class DaoGeneticProfile {
    private DaoGeneticProfile() {}
    
    private static final Map<String,GeneticProfile> byStableId = new HashMap<String,GeneticProfile>();
    private static final Map<Integer,GeneticProfile> byInternalId = new HashMap<Integer,GeneticProfile>();
    private static final Map<Integer,List<GeneticProfile>> byStudy = new HashMap<Integer,List<GeneticProfile>>();

    public static synchronized void reCache() {
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
            pstmt.setString(3, profile.getGeneticAlterationType().name());
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

    /**
     * This method returns the genetic profiles that are referred by to the given referringGeneticProfile.
     *  
     * @param referringGeneticProfile: the referred genetic profile
     * @return
     * @throws GeneticProfileNotFoundException 
     */
    public static List<GeneticProfile> getGeneticProfilesReferredBy(GeneticProfile referringGeneticProfile) throws GeneticProfileNotFoundException {
    	//this method is already implemented in the new API, so use it here:
    	List<org.cbioportal.model.GeneticProfile> list = GeneticProfileApi.geneticProfileService.getGeneticProfilesReferredBy(referringGeneticProfile.getStableId());
    	List<GeneticProfile> result = list.stream().map(p -> GeneticProfileApi.getOldFormat(p)).collect(Collectors.toList());
    	return result;
    }
    
    /**
     * This method returns the genetic profiles that are referring to the given referredGeneticProfile.
     *  
     * @param referredGeneticProfile: the referred genetic profile
     * @return
     * @throws GeneticProfileNotFoundException 
     */
    public static List<GeneticProfile> getGeneticProfilesReferringTo(GeneticProfile referredGeneticProfile) throws GeneticProfileNotFoundException {
    	//this method is already implemented in the new API, so use it here:
    	List<org.cbioportal.model.GeneticProfile> list = GeneticProfileApi.geneticProfileService.getGeneticProfilesReferringTo(referredGeneticProfile.getStableId());
    	List<GeneticProfile> result = list.stream().map(p -> GeneticProfileApi.getOldFormat(p)).collect(Collectors.toList());
    	return result;
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
        profileType.setGeneticAlterationType(GeneticAlterationType.valueOf(rs.getString("GENETIC_ALTERATION_TYPE")));
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
            JdbcUtil.disableForeignKeyCheck(con);
            pstmt = con.prepareStatement("TRUNCATE TABLE genetic_profile");
            pstmt.executeUpdate();
            JdbcUtil.enableForeignKeyCheck(con);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGeneticProfile.class, con, pstmt, rs);
        }
    }
    
    @Component
    static class GeneticProfileApi {
    	//this class provides a link to the new API service layer
    	
    	static GeneticProfileService geneticProfileService = null;
    	
    	@Autowired
    	GeneticProfileApi(GeneticProfileService geneticProfileService) {
    		GeneticProfileApi.geneticProfileService = geneticProfileService;
    	}
    	
    	//support for backwards compatibility: translate values from new to old format objects
    	static GeneticProfile getOldFormat(org.cbioportal.model.GeneticProfile profileIn) {
    		
    		GeneticProfile profileOut = new GeneticProfile();
            profileOut.setStableId(profileIn.getStableId());
            profileOut.setCancerStudyId(profileIn.getCancerStudyId());
            profileOut.setProfileName(profileIn.getName());
            profileOut.setProfileDescription(profileIn.getDescription());
            if (profileIn.getShowProfileInAnalysisTab() != null) {
                profileOut.setShowProfileInAnalysisTab(profileIn.getShowProfileInAnalysisTab());
            } else {
                profileOut.setShowProfileInAnalysisTab(true);
            }
            profileOut.setGeneticAlterationType(GeneticAlterationType.valueOf(profileIn.getGeneticAlterationType().name()));
            profileOut.setDatatype(profileIn.getDatatype());
            profileOut.setGeneticProfileId(profileIn.getGeneticProfileId());
            return profileOut;
    	}
    }
}
