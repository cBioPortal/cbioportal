package org.mskcc.cbio.oncotator;

import org.mskcc.cbio.dbcache.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created with IntelliJ IDEA.
 * User: sos
 * Date: 10/26/12
 * Time: 3:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class DaoJsonCache implements OncotatorCacheService
{
	public int put(OncotatorRecord record) throws SQLException
	{
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

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
		try {
			con = DatabaseUtil.getDbConnection();
			pstmt = con.prepareStatement
					("SELECT * FROM onco_json_cache WHERE CACHE_KEY = ?");
			pstmt.setString(1, key);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				OncotatorRecord record = new OncotatorRecord(rs.getString("CACHE_KEY"));
				record.setRawJson(rs.getString("RAW_JSON"));
				return record;
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
