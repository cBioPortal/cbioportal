/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** Memorial Sloan-Kettering Cancer Center
 ** has no obligations to provide maintenance, support,
 ** updates, enhancements or modifications.  In no event shall
 ** Memorial Sloan-Kettering Cancer Center
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** Memorial Sloan-Kettering Cancer Center
 ** has been advised of the possibility of such damage.  See
 ** the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **/

package org.mskcc.cbio.oncotator;

import org.mskcc.cbio.dbcache.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO for oncotator JSON cache.
 *
 * @author Selcuk Onur Sumer
 */
public class DaoJsonCache implements OncotatorCacheService
{
	public int put(OncotatorRecord record) throws SQLException
	{
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		// do not allow null values to go into db
		if (record.getRawJson() == null)
		{
			return -1;
		}

		try {
			con = DatabaseUtil.getDbConnection();
			pstmt = con.prepareStatement
					("INSERT INTO onco_json_cache (`CACHE_KEY`, `RAW_JSON`)" +
					 " VALUES (?,?)");
			pstmt.setString(1, record.getKey());
			pstmt.setString(2, record.getRawJson());
			int rows = pstmt.executeUpdate();
			return rows;
		} catch (SQLException e) {
			throw e;
		} finally {
			DatabaseUtil.closeAll(con, pstmt, rs);
		}
	}


	public OncotatorRecord get(String key) throws SQLException
	{
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try
		{
			con = DatabaseUtil.getDbConnection();
			pstmt = con.prepareStatement
					("SELECT * FROM onco_json_cache WHERE CACHE_KEY = ?");
			pstmt.setString(1, key);
			rs = pstmt.executeQuery();

			if (rs.next()) {
				return OncotatorParser.parseJSON(rs.getString("CACHE_KEY"),
					rs.getString("RAW_JSON"));
			} else {
				return null;
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			DatabaseUtil.closeAll(con, pstmt, rs);
		}
	}
}
