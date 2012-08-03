// package
package org.mskcc.cbio.cgds.dao;

// imports
import org.mskcc.cbio.cgds.model.User;
import org.mskcc.cbio.cgds.model.UserAuthorities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * DAO into authorities table.
 *
 * @author Benjamin Gross
 */
public class DaoUserAuthorities {

	public static int addUserAuthorities(UserAuthorities userAuthorities) throws DaoException {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int toReturn = 0;
		try {
			con = JdbcUtil.getDbConnection();
			String email = userAuthorities.getEmail();
			for (String authority : userAuthorities.getAuthorities()) {
                pstmt = con.prepareStatement("INSERT INTO authorities (`EMAIL`, `AUTHORITY`) VALUES (?,?)");
                pstmt.setString(1, email);
				pstmt.setString(2, authority);
				toReturn += pstmt.executeUpdate();
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JdbcUtil.closeAll(con, pstmt, rs);
		}

		// outta here
		return toReturn;
	}

	public static UserAuthorities getUserAuthorities(User user) throws DaoException {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		UserAuthorities toReturn;
		try {
			con = JdbcUtil.getDbConnection();
			pstmt = con.prepareStatement("SELECT * FROM authorities where EMAIL=?");
			pstmt.setString(1, user.getEmail());
			rs = pstmt.executeQuery();
			ArrayList<String> authorities = new ArrayList<String>();
			while (rs.next()) {
				authorities.add(rs.getString("AUTHORITY"));
			}
			return new UserAuthorities(user.getEmail(), authorities);
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JdbcUtil.closeAll(con, pstmt, rs);
		}
	}

	public static void removeUserAuthorities(User user) throws DaoException {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		UserAuthorities toReturn;
		try {
			con = JdbcUtil.getDbConnection();
			pstmt = con.prepareStatement("DELETE FROM authorities where EMAIL=?");
			pstmt.setString(1, user.getEmail());
			pstmt.executeUpdate();
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
		  pstmt = con.prepareStatement("TRUNCATE TABLE authorities");
		  pstmt.executeUpdate();
      } catch (SQLException e) {
		  throw new DaoException(e);
      } finally {
		  JdbcUtil.closeAll(con, pstmt, rs);
      }
   }
}
