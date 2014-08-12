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
	public int put(OncotatorRecord record) throws OncotatorCacheException
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
			//e.printStackTrace();
			throw new OncotatorCacheException(e.getMessage());
		} finally {
			DatabaseUtil.closeAll(con, pstmt, rs);
		}
	}


	public OncotatorRecord get(String key) throws OncotatorCacheException
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
			//e.printStackTrace();
			throw new OncotatorCacheException(e.getMessage());
		} finally {
			DatabaseUtil.closeAll(con, pstmt, rs);
		}
	}
}
