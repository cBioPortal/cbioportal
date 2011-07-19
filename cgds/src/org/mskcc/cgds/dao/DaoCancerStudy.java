package org.mskcc.cgds.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.mskcc.cgds.model.CancerStudy;
import org.mskcc.cgds.model.TypeOfCancer;

/**
 * Analogous to and replaces the old DaoCancerType. A CancerStudy has a NAME and
 * DESCRIPTION. If PUBLIC is true a CancerStudy can be accessed by anyone,
 * otherwise can only be accessed through access control.
 * 
 * @author Ethan Cerami
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 */
public class DaoCancerStudy {

   /**
    * add a cancer study to the dbms. updates cancerStudy with its auto incremented uid, in studyID. 
    * 
    * @param cancerStudy
    * @throws DaoException
    */
   public static void addCancerStudy(CancerStudy cancerStudy) throws DaoException {

      // make sure that cancerStudy refers to a valid TypeOfCancerId
      // TODO: have a foreign key constraint do this; why not?
      TypeOfCancer aTypeOfCancer = DaoTypeOfCancer.getTypeOfCancerById(cancerStudy.getTypeOfCancerId());
      if (null == aTypeOfCancer) {
         throw new DaoException("cancerStudy.getTypeOfCancerId() '" + cancerStudy.getTypeOfCancerId()
                  + "' does not refer to a TypeOfCancer.");
      }

      Connection con = null;
      PreparedStatement pstmt = null;
      ResultSet rs = null;
      try {
         con = JdbcUtil.getDbConnection();
         // CANCER_STUDY_IDENTIFIER may be null
         if (cancerStudy.getCancerStudyIdentifier() != null) {
            pstmt = con.prepareStatement("INSERT INTO cancer_study ( `CANCER_STUDY_IDENTIFIER`, `NAME`, "
                     + "`DESCRIPTION`, `PUBLIC`, `TYPE_OF_CANCER_ID` ) VALUES (?,?,?,?,?)",
                     Statement.RETURN_GENERATED_KEYS );
            pstmt.setString(1, cancerStudy.getCancerStudyIdentifier());
            pstmt.setString(2, cancerStudy.getName());
            pstmt.setString(3, cancerStudy.getDescription());
            pstmt.setBoolean(4, cancerStudy.isPublicStudy());
            pstmt.setString(5, cancerStudy.getTypeOfCancerId());
         } else {
            pstmt = con.prepareStatement("INSERT INTO cancer_study ( `NAME`, "
                     + "`DESCRIPTION`, `PUBLIC`, `TYPE_OF_CANCER_ID` ) VALUES (?,?,?,?)",
                     Statement.RETURN_GENERATED_KEYS );
            pstmt.setString(1, cancerStudy.getName());
            pstmt.setString(2, cancerStudy.getDescription());
            pstmt.setBoolean(3, cancerStudy.isPublicStudy());
            pstmt.setString(4, cancerStudy.getTypeOfCancerId());
         }
         pstmt.executeUpdate();
         rs = pstmt.getGeneratedKeys();
         if ( rs.next() ) {
            int auto_id = rs.getInt( 1 );
            cancerStudy.setStudyId(auto_id);
         }

      } catch (SQLException e) {
         throw new DaoException(e);
      } finally {
         JdbcUtil.closeAll(con, pstmt, rs);
      }
   }

   /**
    * return the cancerStudy identified by cancerStudyID, if it exists.
    * 
    * @param cancerStudyID
    * @return
    * @throws DaoException
    */
   public static CancerStudy getCancerStudyById(int cancerStudyID) throws DaoException {
      Connection con = null;
      PreparedStatement pstmt = null;
      ResultSet rs = null;
      try {
         con = JdbcUtil.getDbConnection();
         pstmt = con.prepareStatement("SELECT * FROM cancer_study WHERE CANCER_STUDY_ID=?");
         pstmt.setInt(1, cancerStudyID);
         rs = pstmt.executeQuery();
         if (rs.next()) {
            CancerStudy cancerStudy = extractCancerStudy(rs);
            return cancerStudy;
         }
         return null;
      } catch (SQLException e) {
         throw new DaoException(e);
      } finally {
         JdbcUtil.closeAll(con, pstmt, rs);
      }
   }

   /**
    * return the cancerStudy identified by cancerStudyIdentifier, if it exists.
    * 
    * @param cancerStudyIdentifier
    * @return the CancerStudy, or null if there's no such study
    * @throws DaoException
    */
   public static CancerStudy getCancerStudyByIdentifier(String cancerStudyIdentifier) throws DaoException {
      Connection con = null;
      PreparedStatement pstmt = null;
      ResultSet rs = null;
      try {
         con = JdbcUtil.getDbConnection();
         pstmt = con.prepareStatement("SELECT * FROM cancer_study WHERE CANCER_STUDY_IDENTIFIER=?");
         pstmt.setString(1, cancerStudyIdentifier);
         rs = pstmt.executeQuery();
         if (rs.next()) {
            CancerStudy cancerStudy = extractCancerStudy(rs);
            return cancerStudy;
         }
         return null;
      } catch (SQLException e) {
         throw new DaoException(e);
      } finally {
         JdbcUtil.closeAll(con, pstmt, rs);
      }
   }

   /**
    * indicate whether the cancerStudy identified by cancerStudyIdentifier exists.
    * 
    * @param cancerStudyIdentifier
    * @return true if the CancerStudy exists, otherwise false 
    * @throws DaoException
    */
   public static boolean doesCancerStudyExistByIdentifier(String cancerStudyIdentifier) throws DaoException {
      CancerStudy cancerStudy = getCancerStudyByIdentifier( cancerStudyIdentifier );
      return ( null != cancerStudy );
   }

   /**
    * indicate whether the cancerStudy identified by studyID exists.
    * does no access control, so only returns a boolean.
    * 
    * @param studyID
    * @return true if the CancerStudy exists, otherwise false 
    * @throws DaoException
    */
   public static boolean doesCancerStudyExistByID( int studyID ) throws DaoException {
      CancerStudy cancerStudy = getCancerStudyById( studyID );
      return ( null != cancerStudy );
   }

   /**
    * return all the cancerStudies.
    * 
    * @param cancerStudyIdentifier
    * @return all the cancerStudies
    * @throws DaoException
    */
   public static ArrayList<CancerStudy> getAllCancerStudies() throws DaoException {
      Connection con = null;
      PreparedStatement pstmt = null;
      ResultSet rs = null;
      try {
         con = JdbcUtil.getDbConnection();
         pstmt = con.prepareStatement("SELECT * FROM cancer_study");
         rs = pstmt.executeQuery();
         ArrayList<CancerStudy> list = new ArrayList<CancerStudy>();
         while (rs.next()) {
            CancerStudy cancerStudy = extractCancerStudy(rs);
            list.add(cancerStudy);
         }
         return list;
      } catch (SQLException e) {
         throw new DaoException(e);
      } finally {
         JdbcUtil.closeAll(con, pstmt, rs);
      }
   }

   public static int getCount() throws DaoException {
      Connection con = null;
      PreparedStatement pstmt = null;
      ResultSet rs = null;
      try {
         con = JdbcUtil.getDbConnection();
         pstmt = con.prepareStatement("SELECT COUNT(*) FROM cancer_study");
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

   public static void deleteAllRecords() throws DaoException {
      Connection con = null;
      PreparedStatement pstmt = null;
      ResultSet rs = null;
      try {
         con = JdbcUtil.getDbConnection();
         pstmt = con.prepareStatement("TRUNCATE TABLE cancer_study");
         pstmt.executeUpdate();
      } catch (SQLException e) {
         throw new DaoException(e);
      } finally {
         JdbcUtil.closeAll(con, pstmt, rs);
      }
   }

   public static void deleteCancerStudy(int cancerStudyId) throws DaoException {
      Connection con = null;
      PreparedStatement pstmt = null;
      ResultSet rs = null;
      try {
         con = JdbcUtil.getDbConnection();
         pstmt = con.prepareStatement("DELETE from " + "cancer_study WHERE CANCER_STUDY_ID=?");
         pstmt.setInt(1, cancerStudyId);
         pstmt.executeUpdate();
      } catch (SQLException e) {
         throw new DaoException(e);
      } finally {
         JdbcUtil.closeAll(con, pstmt, rs);
      }
   }

   private static CancerStudy extractCancerStudy(ResultSet rs) throws SQLException {
      CancerStudy cancerStudy = new CancerStudy(rs.getString("NAME"), 
                  rs.getString("DESCRIPTION"), 
                  rs.getString("CANCER_STUDY_IDENTIFIER"), 
                  rs.getString("TYPE_OF_CANCER_ID"),
                  rs.getBoolean("PUBLIC") );

      cancerStudy.setStudyId(rs.getInt("CANCER_STUDY_ID"));
      return cancerStudy;
   }
}
