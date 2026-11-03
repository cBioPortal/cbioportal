/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.dao;

import java.sql.*;
import java.util.ArrayList;
import org.mskcc.cbio.portal.model.User;

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
         con = JdbcUtil.getDbConnection(DaoUser.class);
         pstmt = con.prepareStatement("INSERT INTO users ( `email`, `name`, `enabled` ) VALUES (?,?,?)");
         pstmt.setString(1, user.getEmail());
         pstmt.setString(2, user.getName());
         pstmt.setBoolean(3, user.isEnabled());
         return pstmt.executeUpdate();
      } catch (SQLException e) {
         throw new DaoException(e);
      } finally {
         JdbcUtil.closeAll(DaoUser.class, con, pstmt, rs);
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
         con = JdbcUtil.getDbConnection(DaoUser.class);
         pstmt = con.prepareStatement("SELECT * FROM users WHERE email=?");
         pstmt.setString(1, email);
         rs = pstmt.executeQuery();
         if (rs.next()) {
            return extractUser(rs);
         }
         return null;
      } catch (SQLException e) {
         throw new DaoException(e);
      } finally {
         JdbcUtil.closeAll(DaoUser.class, con, pstmt, rs);
      }
   }

   public static ArrayList<User> getAllUsers() throws DaoException {
      Connection con = null;
      PreparedStatement pstmt = null;
      ResultSet rs = null;
      try {
         con = JdbcUtil.getDbConnection(DaoUser.class);
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
         JdbcUtil.closeAll(DaoUser.class, con, pstmt, rs);
      }
   }

   public static void deleteAllRecords() throws DaoException {
      Connection con = null;
      PreparedStatement pstmt = null;
      ResultSet rs = null;
      try {
         con = JdbcUtil.getDbConnection(DaoUser.class);
         pstmt = con.prepareStatement("TRUNCATE TABLE users");
         pstmt.executeUpdate();
      } catch (SQLException e) {
         throw new DaoException(e);
      } finally {
         JdbcUtil.closeAll(DaoUser.class, con, pstmt, rs);
      }
   }

   public static void deleteUser(String email) throws DaoException {
      Connection con = null;
      PreparedStatement pstmt = null;
      ResultSet rs = null;
      try {
         con = JdbcUtil.getDbConnection(DaoUser.class);
         pstmt = con.prepareStatement("DELETE FROM users WHERE email=?");
         pstmt.setString(1, email);
         pstmt.executeUpdate();
      } catch (SQLException e) {
         throw new DaoException(e);
      } finally {
         JdbcUtil.closeAll(DaoUser.class, con, pstmt, rs);
      }
   }

   private static User extractUser(ResultSet rs) throws SQLException {

       return new User(rs.getString("email"),
                       rs.getString("name"),
                       rs.getBoolean("enabled"));
   }
}
