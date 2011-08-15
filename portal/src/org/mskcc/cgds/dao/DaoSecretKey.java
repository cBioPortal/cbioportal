package org.mskcc.cgds.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.mskcc.cgds.model.SecretKey;

/**
 * Stores secret keys.
 * 
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 */
public class DaoSecretKey {

   public static int addSecretKey(SecretKey secretKey) throws DaoException {

      Connection con = null;
      PreparedStatement pstmt = null;
      ResultSet rs = null;
      try {
         con = JdbcUtil.getDbConnection();
         pstmt = con.prepareStatement("INSERT INTO encrypted_keys ( `ENCRYPTED_KEY` ) VALUES (?)");
         pstmt.setString(1, secretKey.getEncryptedKey());
         int rows = pstmt.executeUpdate();
         return rows;
      } catch (SQLException e) {
         throw new DaoException(e);
      } finally {
         JdbcUtil.closeAll(con, pstmt, rs);
      }
   }

   public static ArrayList<SecretKey> getAllSecretKeys() throws DaoException {
      Connection con = null;
      PreparedStatement pstmt = null;
      ResultSet rs = null;
      try {
         con = JdbcUtil.getDbConnection();
         pstmt = con.prepareStatement("SELECT * FROM encrypted_keys");
         rs = pstmt.executeQuery();
         ArrayList<SecretKey> list = new ArrayList<SecretKey>();
         while (rs.next()) {
            SecretKey secretKey = extractSecretKey(rs);
            list.add(secretKey);
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
         pstmt = con.prepareStatement("TRUNCATE TABLE encrypted_keys");
         pstmt.executeUpdate();
      } catch (SQLException e) {
         throw new DaoException(e);
      } finally {
         JdbcUtil.closeAll(con, pstmt, rs);
      }
   }

   public static void deleteSecretKey(SecretKey secretKey) throws DaoException {
      Connection con = null;
      PreparedStatement pstmt = null;
      ResultSet rs = null;
      try {
         con = JdbcUtil.getDbConnection();
         pstmt = con.prepareStatement("DELETE FROM encrypted_keys WHERE ENCRYPTED_KEY=?");
         pstmt.setString(1, secretKey.getEncryptedKey());
         pstmt.executeUpdate();
      } catch (SQLException e) {
         throw new DaoException(e);
      } finally {
         JdbcUtil.closeAll(con, pstmt, rs);
      }
   }

   private static SecretKey extractSecretKey(ResultSet rs) throws SQLException {

      SecretKey secretKey = new SecretKey( );
      secretKey.setEncryptedKey( rs.getString("ENCRYPTED_KEY") );
      return secretKey;
   }

}
