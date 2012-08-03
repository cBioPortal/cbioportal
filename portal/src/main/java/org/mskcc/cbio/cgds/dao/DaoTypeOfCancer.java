package org.mskcc.cbio.cgds.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.mskcc.cbio.cgds.model.TypeOfCancer;

/**
 * A TypeOfCancer is a clinical cancer type, such as Glioblastoma, Ovarian, etc.
 * Eventually, we'll have ontology problems with this, but initially the dbms
 * will be loaded from a file with a static table of types.
 * 
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 */
public class DaoTypeOfCancer {

   // these methods should be static, as this object has no state
   public static int addTypeOfCancer(TypeOfCancer typeOfCancer) throws DaoException {
      Connection con = null;
      PreparedStatement pstmt = null;
      ResultSet rs = null;
      try {
         con = JdbcUtil.getDbConnection();
         pstmt = con.prepareStatement("INSERT INTO type_of_cancer ( `TYPE_OF_CANCER_ID`, " + "`NAME` ) VALUES (?,?)");
         pstmt.setString(1, typeOfCancer.getTypeOfCancerId());
         pstmt.setString(2, typeOfCancer.getName());
         int rows = pstmt.executeUpdate();
         return rows;
      } catch (SQLException e) {
         throw new DaoException(e);
      } finally {
         JdbcUtil.closeAll(con, pstmt, rs);
      }
   }

   public static TypeOfCancer getTypeOfCancerById(String typeOfCancerId) throws DaoException {
      Connection con = null;
      PreparedStatement pstmt = null;
      ResultSet rs = null;
      try {
         con = JdbcUtil.getDbConnection();
         pstmt = con.prepareStatement("SELECT * FROM type_of_cancer WHERE TYPE_OF_CANCER_ID=?");
         pstmt.setString(1, typeOfCancerId);
         rs = pstmt.executeQuery();
         if (rs.next()) {
            TypeOfCancer typeOfCancer = extractTypeOfCancer(rs);
            return typeOfCancer;
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
            TypeOfCancer typeOfCancer = extractTypeOfCancer(rs);
            list.add(typeOfCancer);
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

   public static void deleteTypeOfCancer(String typeOfCancerId) throws DaoException {
      Connection con = null;
      PreparedStatement pstmt = null;
      ResultSet rs = null;
      try {
         con = JdbcUtil.getDbConnection();
         pstmt = con.prepareStatement("DELETE from " + "type_of_cancer WHERE TYPE_OF_CANCER_ID=?");
         pstmt.setString(1, typeOfCancerId);
         pstmt.executeUpdate();
      } catch (SQLException e) {
         throw new DaoException(e);
      } finally {
         JdbcUtil.closeAll(con, pstmt, rs);
      }
   }

   private static TypeOfCancer extractTypeOfCancer(ResultSet rs) throws SQLException {
      TypeOfCancer typeOfCancer = new TypeOfCancer();

      typeOfCancer.setTypeOfCancerId(rs.getString("TYPE_OF_CANCER_ID"));
      typeOfCancer.setName(rs.getString("NAME"));
      return typeOfCancer;
   }
}
