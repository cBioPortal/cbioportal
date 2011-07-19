package org.mskcc.cgds.dao;

import org.mskcc.cgds.model.UserAccessRight;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * User rights to access a cancer study. Record (EMAIL, CANCER_STUDY_ID)
 * means that the user identified by EMAIL has the right to access the cancer
 * study identified by CANCER_STUDY_ID. 
 * 
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 */
public class DaoUserAccessRight {

   public static int addUserAccessRight(UserAccessRight userAccessRight) throws DaoException {
      Connection con = null;
      PreparedStatement pstmt = null;
      ResultSet rs = null;
      try {
         con = JdbcUtil.getDbConnection();
         pstmt = con.prepareStatement("INSERT INTO access_rights (`EMAIL`, `CANCER_STUDY_ID`) VALUES (?,?)");
         pstmt.setString(1, userAccessRight.getEmail());
         pstmt.setInt(2, userAccessRight.getCancerStudyId());
         int rows = pstmt.executeUpdate();
         return rows;
      } catch (SQLException e) {
         throw new DaoException(e);
      } finally {
         JdbcUtil.closeAll(con, pstmt, rs);
      }
   }
   
   /**
    * return true if access_rights contains an email, cancerStudyID row
    * @param email
    * @param cancerStudyID
    * @return
    * @throws DaoException
    */
   public static boolean containsUserAccessRightsByEmailAndStudy(String email, int cancerStudyID) throws DaoException {
      Connection con = null;
      PreparedStatement pstmt = null;
      ResultSet rs = null;
      try {
         con = JdbcUtil.getDbConnection();
         pstmt = con.prepareStatement("SELECT COUNT(*) FROM access_rights WHERE EMAIL=? AND CANCER_STUDY_ID=?");
         pstmt.setString(1, email );
         pstmt.setInt(2, cancerStudyID );
         rs = pstmt.executeQuery();
         if( rs.next() ){
            return( rs.getInt(1) == 1 );
         }else{
            throw new DaoException( "No rows in ResultSet." );
         }

      } catch (SQLException e) {
         throw new DaoException(e);
      } finally {
         JdbcUtil.closeAll(con, pstmt, rs);
      }
   }

   public static ArrayList<UserAccessRight> getAllUserAccessRights() throws DaoException {
      Connection con = null;
      PreparedStatement pstmt = null;
      ResultSet rs = null;
      try {
          con = JdbcUtil.getDbConnection();
          pstmt = con.prepareStatement("SELECT * FROM access_rights");
          rs = pstmt.executeQuery();
          ArrayList<UserAccessRight> list = new ArrayList<UserAccessRight>();
          while (rs.next()) {
              UserAccessRight userAccessRight = extractUserAccessRight(rs);
              list.add(userAccessRight);
          }
          return list;
      } catch (SQLException e) {
          throw new DaoException(e);
      } finally {
          JdbcUtil.closeAll(con, pstmt, rs);
      }
  }

   public static void deleteAllRecords() throws DaoException {
      Connection con = null;
      PreparedStatement pstmt = null;
      ResultSet rs = null;
      try {
         con = JdbcUtil.getDbConnection();
         pstmt = con.prepareStatement("TRUNCATE TABLE access_rights");
         pstmt.executeUpdate();
      } catch (SQLException e) {
         throw new DaoException(e);
      } finally {
         JdbcUtil.closeAll(con, pstmt, rs);
      }
   }

   public static void deleteUserAccessRight(String email, int cancerStudyId) throws DaoException {
      Connection con = null;
      PreparedStatement pstmt = null;
      ResultSet rs = null;
      try {
         con = JdbcUtil.getDbConnection();
         pstmt = con.prepareStatement("DELETE from " + "access_rights WHERE EMAIL=? AND CANCER_STUDY_ID=?");
         pstmt.setString(1, email);
         pstmt.setInt(2, cancerStudyId);
         pstmt.executeUpdate();
      } catch (SQLException e) {
         throw new DaoException(e);
      } finally {
         JdbcUtil.closeAll(con, pstmt, rs);
      }
   }

   /**
    * Store a dbms result into an UserAccessRight.
    * 
    * @throws SQLException
    */
   private static  UserAccessRight extractUserAccessRight(ResultSet rs) throws SQLException {
      UserAccessRight userAccessRight = new UserAccessRight(
               rs.getString("EMAIL"), 
               rs.getInt("CANCER_STUDY_ID") );
      return userAccessRight;
   }
}
