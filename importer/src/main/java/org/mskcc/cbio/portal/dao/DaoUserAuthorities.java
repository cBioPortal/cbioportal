/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.dao;

import java.sql.*;
import java.util.*;
import org.mskcc.cbio.portal.model.User;
import org.mskcc.cbio.portal.model.UserAuthorities;

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
			con = JdbcUtil.getDbConnection(DaoUserAuthorities.class);
			String email = userAuthorities.getEmail();
			for (String authority : userAuthorities.getAuthorities()) {
                pstmt = con.prepareStatement("INSERT INTO authorities (`email`, `authority`) VALUES (?,?)");
                pstmt.setString(1, email);
				pstmt.setString(2, authority);
				toReturn += pstmt.executeUpdate();
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JdbcUtil.closeAll(DaoUserAuthorities.class, con, pstmt, rs);
		}

		// outta here
		return toReturn;
	}

	public static UserAuthorities getUserAuthorities(User user) throws DaoException {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			con = JdbcUtil.getDbConnection(DaoUserAuthorities.class);
			pstmt = con.prepareStatement("SELECT * FROM authorities where email=?");
			pstmt.setString(1, user.getEmail());
			rs = pstmt.executeQuery();
			ArrayList<String> authorities = new ArrayList<String>();
			while (rs.next()) {
				authorities.add(rs.getString("authority"));
			}
			return new UserAuthorities(user.getEmail(), authorities);
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JdbcUtil.closeAll(DaoUserAuthorities.class, con, pstmt, rs);
		}
	}

	public static void removeUserAuthorities(User user) throws DaoException {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			con = JdbcUtil.getDbConnection(DaoUserAuthorities.class);
			pstmt = con.prepareStatement("DELETE FROM authorities where email=?");
			pstmt.setString(1, user.getEmail());
			pstmt.executeUpdate();
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JdbcUtil.closeAll(DaoUserAuthorities.class, con, pstmt, rs);
		}
	}

	public static void deleteAllRecords() throws DaoException {
	   Connection con = null;
	   PreparedStatement pstmt = null;
	   ResultSet rs = null;
      try {
		  con = JdbcUtil.getDbConnection(DaoUserAuthorities.class);
		  pstmt = con.prepareStatement("TRUNCATE TABLE authorities");
		  pstmt.executeUpdate();
      } catch (SQLException e) {
		  throw new DaoException(e);
      } finally {
		  JdbcUtil.closeAll(DaoUserAuthorities.class, con, pstmt, rs);
      }
   }
}
