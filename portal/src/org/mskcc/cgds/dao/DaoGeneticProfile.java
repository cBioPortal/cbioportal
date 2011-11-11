package org.mskcc.cgds.dao;

import org.mskcc.cgds.model.GeneticAlterationType;
import org.mskcc.cgds.model.GeneticProfile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Data access object for Genetic Profile table
 */
public class DaoGeneticProfile {
   
   // TODO: these methods should be static, as this object has no state
    public int addGeneticProfile(GeneticProfile profile) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();

            pstmt = con.prepareStatement
                    ("INSERT INTO genetic_profile (`STABLE_ID`, `CANCER_STUDY_ID`, `GENETIC_ALTERATION_TYPE`," +
                            "`NAME`, `DESCRIPTION`, `SHOW_PROFILE_IN_ANALYSIS_TAB`) " +
                            "VALUES (?,?,?,?,?,?)");
            pstmt.setString(1, profile.getStableId());
            pstmt.setInt(2, profile.getCancerStudyId());
            pstmt.setString(3, profile.getGeneticAlterationType().toString());
            pstmt.setString(4, profile.getProfileName());
            pstmt.setString(5, profile.getProfileDescription());
            pstmt.setBoolean(6, profile.showProfileInAnalysisTab());
            int rows = pstmt.executeUpdate();
            return rows;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    /**
     * Updates a Genetic Profile Name and Description.
     * @param geneticProfileId  Genetic Profile ID.
     * @param name              New Genetic Profile Name.
     * @param description       New Genetic Profile Description.
     * @return                  Returns True if Genetic Profile was Updated.
     * @throws DaoException     Data Access Error.
     */
    public boolean updateNameAndDescription (int geneticProfileId, String name, String description)
        throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement("UPDATE genetic_profile SET NAME=?, DESCRIPTION=? " +
                    "WHERE GENETIC_PROFILE_ID=?");
            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.setInt(3, geneticProfileId);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }
    
    public int deleteGeneticProfile(GeneticProfile profile) throws DaoException {
       Connection con = null;
       PreparedStatement pstmt = null;
       ResultSet rs = null;
       try {
           con = JdbcUtil.getDbConnection();
           pstmt = con.prepareStatement("DELETE FROM genetic_profile WHERE STABLE_ID = ?");
           pstmt.setString(1, profile.getStableId());
           int rows = pstmt.executeUpdate();
           return rows;
       } catch (SQLException e) {
           throw new DaoException(e);
       } finally {
           JdbcUtil.closeAll(con, pstmt, rs);
       }
   }
    
    public GeneticProfile getGeneticProfileByStableId(String stableId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT * FROM genetic_profile WHERE STABLE_ID = ?");
            pstmt.setString(1, stableId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                GeneticProfile geneticProfile = extractGeneticProfile(rs);
                return geneticProfile;
            }
            return null;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    public GeneticProfile getGeneticProfileById(int geneticProfileId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT * FROM genetic_profile WHERE GENETIC_PROFILE_ID = ?");
            pstmt.setInt(1, geneticProfileId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                GeneticProfile geneticProfile = extractGeneticProfile(rs);
                return geneticProfile;
            }
            return null;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }
    
    // TODO: UNIT TEST
    public ArrayList <GeneticProfile> getGeneticProfiles (int[] geneticProfileIds) throws
            DaoException {
        DaoGeneticProfile daoGeneticProfile = new DaoGeneticProfile();
        ArrayList <GeneticProfile> geneticProfileList = new ArrayList <GeneticProfile>();
        for (int geneticProfileId:  geneticProfileIds) {
            GeneticProfile geneticProfile =
                    daoGeneticProfile.getGeneticProfileById(geneticProfileId);
            if (geneticProfile != null) {
                geneticProfileList.add(geneticProfile);
            } else {
                throw new IllegalArgumentException ("Could not find genetic profile for:  "
                        + geneticProfileId);
            }
        }
        return geneticProfileList;
    }

    public int getCount() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT COUNT(*) FROM genetic_profile");
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    private GeneticProfile extractGeneticProfile(ResultSet rs) throws SQLException {
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
        profileType.setGeneticProfileId(rs.getInt("GENETIC_PROFILE_ID"));
        return profileType;
    }

    public ArrayList<GeneticProfile> getAllGeneticProfiles(int cancerStudyId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();

            pstmt = con.prepareStatement
                    ("SELECT * FROM genetic_profile WHERE CANCER_STUDY_ID = ?");
            pstmt.setInt(1, cancerStudyId);
            rs = pstmt.executeQuery();
            ArrayList<GeneticProfile> list = new ArrayList<GeneticProfile>();
            while (rs.next()) {
                GeneticProfile profileType = extractGeneticProfile(rs);
                list.add(profileType);
            }
            return list;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    public void deleteAllRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement("TRUNCATE TABLE genetic_profile");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }
}