/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.dao;

// imports
import org.mskcc.cbio.portal.model.User;
import org.mskcc.cbio.portal.model.UserAuthorities;

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
			con = JdbcUtil.getDbConnection(DaoUserAuthorities.class);
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
			JdbcUtil.closeAll(DaoUserAuthorities.class, con, pstmt, rs);
		}
	}

	public static void removeUserAuthorities(User user) throws DaoException {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			con = JdbcUtil.getDbConnection(DaoUserAuthorities.class);
			pstmt = con.prepareStatement("DELETE FROM authorities where EMAIL=?");
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
