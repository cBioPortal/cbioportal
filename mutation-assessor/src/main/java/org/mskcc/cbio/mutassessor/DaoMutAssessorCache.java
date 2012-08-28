package org.mskcc.cbio.mutassessor;

import org.mskcc.cbio.dbcache.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO for mutation assessor cache table.
 */
public class DaoMutAssessorCache
{
	private static DaoMutAssessorCache daoMutAssessorCache;

	private DaoMutAssessorCache() {
	}

	/**
	 * Returns the singleton instance.
	 * @return DaoOncotator singleton instance.
	 */
	public static DaoMutAssessorCache getInstance()
	{
		if (daoMutAssessorCache == null) {
			daoMutAssessorCache = new DaoMutAssessorCache();
		}

		return daoMutAssessorCache;
	}

	/**
	 * Adds a new cache record to the database.
	 *
	 * @param record
	 * @return number of records successfully added.
	 * @throws SQLException Database Error.
	 */
	public int put(MutationAssessorRecord record) throws SQLException
	{
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try
		{
			con = DatabaseUtil.getDbConnection();
			pstmt = con.prepareStatement
					("INSERT INTO mutation_assessor_cache (`CACHE_KEY`, `PREDICTED_IMPACT`, `PROTEIN_CHANGE`," +
					 " `STRUCTURE_LINK`, `ALIGNMENT_LINK`) + VALUES (?,?,?,?,?)");

			pstmt.setString(1, record.getKey());
			pstmt.setString(2, record.getImpact());
			pstmt.setString(3, record.getProteinChange());
			pstmt.setString(4, record.getStructureLink());
			pstmt.setString(5, record.getAlignmentLink());

			int rows = pstmt.executeUpdate();
			return rows;
		} catch (SQLException e) {
			throw e;
		} finally {
			DatabaseUtil.closeAll(con, pstmt, rs);
		}
	}

	public MutationAssessorRecord get(String key) throws SQLException
	{
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			con = DatabaseUtil.getDbConnection();
			pstmt = con.prepareStatement
					("SELECT * FROM mutation_assessor_cache WHERE CACHE_KEY = ?");
			pstmt.setString(1, key);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				MutationAssessorRecord record = new MutationAssessorRecord(rs.getString("CACHE_KEY"));
				record.setImpact(rs.getString("PREDICTED_IMPACT"));
				record.setProteinChange(rs.getString("PROTEIN_CHANGE"));
				record.setAlignmentLink(rs.getString("ALIGNMENT_LINK"));
				record.setStructureLink(rs.getString("STRUCTURE_LINK"));
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

	public void deleteAllRecords() throws SQLException {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			con = DatabaseUtil.getDbConnection();
			pstmt = con.prepareStatement("TRUNCATE TABLE mutation_assessor_cache");
			pstmt.executeUpdate();
		} catch (SQLException e) {
			throw e;
		} finally {
			DatabaseUtil.closeAll(con, pstmt, rs);
		}
	}
}
