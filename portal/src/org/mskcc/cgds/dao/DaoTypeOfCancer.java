package org.mskcc.cgds.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.mskcc.cgds.model.TypeOfCancer;

/**
 * A TypeOfCancer is a clinical cancer type, such as Glioblastoma, Ovarian, etc.
 * Eventually, we'll have ontology problems with this, but initially the dbms
 * will be loaded from a file with a static table of types.
 * 
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 */
public class DaoTypeOfCancer {

   // these methods should be static, as this object has no state
   public static int addTypeOfCancer(TypeOfCancer TypeOfCancer) throws DaoException {
      Connection con = null;
      PreparedStatement pstmt = null;
      ResultSet rs = null;
      try {
         con = JdbcUtil.getDbConnection();
         pstmt = con.prepareStatement("INSERT INTO type_of_cancer ( `TYPE_OF_CANCER_ID`, " + "`NAME` ) VALUES (?,?)");
         pstmt.setString(1, TypeOfCancer.getTypeOfCancerId());
         pstmt.setString(2, TypeOfCancer.getName());
         int rows = pstmt.executeUpdate();
         return rows;
      } catch (SQLException e) {
         throw new DaoException(e);
      } finally {
         JdbcUtil.closeAll(con, pstmt, rs);
      }
   }

   public static TypeOfCancer getTypeOfCancerById(String TypeOfCancerId) throws DaoException {
      Connection con = null;
      PreparedStatement pstmt = null;
      ResultSet rs = null;
      try {
         con = JdbcUtil.getDbConnection();
         pstmt = con.prepareStatement("SELECT * FROM type_of_cancer WHERE TYPE_OF_CANCER_ID=?");
         pstmt.setString(1, TypeOfCancerId);
         rs = pstmt.executeQuery();
         if (rs.next()) {
            TypeOfCancer TypeOfCancer = extractTypeOfCancer(rs);
            return TypeOfCancer;
         }
         return null;
      } catch (SQLException e) {
         throw new DaoException(e);
      } finally {
         JdbcUtil.closeAll(con, pstmt, rs);
      }
   }

   public static ArrayList<TypeOfCancer> getAllTypesOfCancer() throws DaoException {
      Connection con = null;
      PreparedStatement pstmt = null;
      ResultSet rs = null;
      try {
         con = JdbcUtil.getDbConnection();
         pstmt = con.prepareStatement("SELECT * FROM type_of_cancer");
         rs = pstmt.executeQuery();
         ArrayList<TypeOfCancer> list = new ArrayList<TypeOfCancer>();
         while (rs.next()) {
            TypeOfCancer TypeOfCancer = extractTypeOfCancer(rs);
            list.add(TypeOfCancer);
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
         pstmt = con.prepareStatement("SELECT COUNT(*) FROM type_of_cancer");
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
         pstmt = con.prepareStatement("TRUNCATE TABLE type_of_cancer");
         pstmt.executeUpdate();
      } catch (SQLException e) {
         throw new DaoException(e);
      } finally {
         JdbcUtil.closeAll(con, pstmt, rs);
      }
   }

   public static void deleteTypeOfCancer(String TypeOfCancerId) throws DaoException {
      Connection con = null;
      PreparedStatement pstmt = null;
      ResultSet rs = null;
      try {
         con = JdbcUtil.getDbConnection();
         pstmt = con.prepareStatement("DELETE from " + "type_of_cancer WHERE TYPE_OF_CANCER_ID=?");
         pstmt.setString(1, TypeOfCancerId);
         pstmt.executeUpdate();
      } catch (SQLException e) {
         throw new DaoException(e);
      } finally {
         JdbcUtil.closeAll(con, pstmt, rs);
      }
   }

   private static TypeOfCancer extractTypeOfCancer(ResultSet rs) throws SQLException {
      TypeOfCancer TypeOfCancer = new TypeOfCancer();

      TypeOfCancer.setTypeOfCancerId(rs.getString("TYPE_OF_CANCER_ID"));
      TypeOfCancer.setName(rs.getString("NAME"));
      return TypeOfCancer;
   }
}
