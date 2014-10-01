/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center 
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center 
 * has been advised of the possibility of such damage.
*/

package org.mskcc.cbio.portal.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
         pstmt = con.prepareStatement("INSERT INTO users ( `EMAIL`, `NAME`, `ENABLED` ) VALUES (?,?,?)");
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
         pstmt = con.prepareStatement("SELECT * FROM users WHERE EMAIL=?");
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
         pstmt = con.prepareStatement("DELETE FROM users WHERE EMAIL=?");
         pstmt.setString(1, email);
         pstmt.executeUpdate();
      } catch (SQLException e) {
         throw new DaoException(e);
      } finally {
         JdbcUtil.closeAll(DaoUser.class, con, pstmt, rs);
      }
   }

   private static User extractUser(ResultSet rs) throws SQLException {

       return new User(rs.getString("EMAIL"),
                       rs.getString("NAME"),
                       rs.getBoolean("ENABLED"));
   }
}
