package org.mskcc.cgds.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Data access object for Case table
 */
public class DaoCaseProfile {
   
   public static final int NO_SUCH_PROFILE_ID = -1;

   // TODO: I think these methods should be static, as this object has no state
    public int addCaseProfile(String caseId, int geneticProfileId) throws DaoException {
        if (caseId == null || caseId.trim().length() == 0) {
            throw new IllegalArgumentException ("Case ID is null or empty");
        }
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            if (!caseExistsInGeneticProfile(caseId, geneticProfileId)) {
                con = JdbcUtil.getDbConnection();
                pstmt = con.prepareStatement
                        ("INSERT INTO case_profile (`CASE_ID`, `GENETIC_PROFILE_ID`) "
                                + "VALUES (?,?)");
                pstmt.setString(1, caseId);
                pstmt.setInt(2, geneticProfileId);
                int rows = pstmt.executeUpdate();
                return rows;
            } else {
                return 0;
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    public boolean caseExistsInGeneticProfile(String caseId, int geneticProfileId)
            throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT * FROM case_profile WHERE CASE_ID = ? AND GENETIC_PROFILE_ID = ?");
            pstmt.setString(1, caseId);
            pstmt.setInt(2, geneticProfileId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
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

    public int getProfileIdForCase( String caseId ) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement("SELECT GENETIC_PROFILE_ID FROM case_profile WHERE CASE_ID = ?");
            pstmt.setString(1, caseId );
            rs = pstmt.executeQuery();
            if( rs.next() ) {
               return rs.getInt("GENETIC_PROFILE_ID");
            }else{
               return NO_SUCH_PROFILE_ID;
            }

        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    public ArrayList<String> getAllCaseIdsInProfile(int geneticProfileId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT * FROM case_profile WHERE GENETIC_PROFILE_ID = ?");
            pstmt.setInt(1, geneticProfileId);
            rs = pstmt.executeQuery();
            ArrayList<String> caseIds = new ArrayList<String>();
            while (rs.next()) {
                caseIds.add(rs.getString("CASE_ID"));
            }
            return caseIds;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    public ArrayList<String> getAllCases() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT * FROM case_profile");
            rs = pstmt.executeQuery();
            ArrayList<String> caseIds = new ArrayList<String>();
            while (rs.next()) {
                caseIds.add(rs.getString("CASE_ID"));
            }
            return caseIds;
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
            pstmt = con.prepareStatement("TRUNCATE TABLE case_profile");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }
}
