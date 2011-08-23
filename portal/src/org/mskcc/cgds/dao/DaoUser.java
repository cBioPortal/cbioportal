package org.mskcc.cgds.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.mskcc.cgds.model.User;

/**
 * A User. They must have an EMAIL & ENABLED.
 * NAME is optional (i.e., could be "").
 * 
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 * @author Benjamin Gross
 */
public class DaoUser {

   public static int addUser(User user) throws DaoException {

      Connection con = null;
      PreparedStatement pstmt = null;
      ResultSet rs = null;
      try {
         con = JdbcUtil.getDbConnection();
         pstmt = con.prepareStatement("INSERT INTO users ( `EMAIL`, `NAME`, `ENABLED` ) VALUES (?,?,?)");
         pstmt.setString(1, user.getEmail());
         pstmt.setString(2, user.getName());
         pstmt.setBoolean(3, user.isEnabled());
         int rows = pstmt.executeUpdate();
         return rows;
      } catch (SQLException e) {
         throw new DaoException(e);
      } finally {
         JdbcUtil.closeAll(con, pstmt, rs);
      }
   }

   /**
    * If a user with the email exists in the dbms, return their User object.
    * Otherwise, return null.
    * 
    * @param email
    * @return
    * @throws DaoException
    */
   public static User getUserByEmail(String email) throws DaoException {
      Connection con = null;
      PreparedStatement pstmt = null;
      ResultSet rs = null;
      try {
         con = JdbcUtil.getDbConnection();
         pstmt = con.prepareStatement("SELECT * FROM users WHERE EMAIL=?");
         pstmt.setString(1, email);
         rs = pstmt.executeQuery();
         if (rs.next()) {
            User user = extractUser(rs);
            return user;
         }
         return null;
      } catch (SQLException e) {
         throw new DaoException(e);
      } finally {
         JdbcUtil.closeAll(con, pstmt, rs);
      }
   }

   public static ArrayList<User> getAllUsers() throws DaoException {
      Connection con = null;
      PreparedStatement pstmt = null;
      ResultSet rs = null;
      try {
         con = JdbcUtil.getDbConnection();
         pstmt = con.prepareStatement("SELECT * FROM users");
         rs = pstmt.executeQuery();
         ArrayList<User> list = new ArrayList<User>();
         while (rs.next()) {
            User user = extractUser(rs);
            list.add(user);
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
         pstmt = con.prepareStatement("TRUNCATE TABLE users");
         pstmt.executeUpdate();
      } catch (SQLException e) {
         throw new DaoException(e);
      } finally {
         JdbcUtil.closeAll(con, pstmt, rs);
      }
   }

   public static void deleteUser(String email) throws DaoException {
      Connection con = null;
      PreparedStatement pstmt = null;
      ResultSet rs = null;
      try {
         con = JdbcUtil.getDbConnection();
         pstmt = con.prepareStatement("DELETE FROM users WHERE EMAIL=?");
         pstmt.setString(1, email);
         pstmt.executeUpdate();
      } catch (SQLException e) {
         throw new DaoException(e);
      } finally {
         JdbcUtil.closeAll(con, pstmt, rs);
      }
   }

   private static User extractUser(ResultSet rs) throws SQLException {

       return new User(rs.getString("EMAIL"),
                       rs.getString("NAME"),
                       rs.getBoolean("ENABLED"));
   }
}
