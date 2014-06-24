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

package org.mskcc.cbio.mutassessor;

import org.mskcc.cbio.dbcache.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO for mutation assessor cache table.
 *
 * @author Selcuk Onur Sumer
 */
public class DaoMutAssessorCache
{
	private static DaoMutAssessorCache daoMutAssessorCache;

	private DaoMutAssessorCache() {
	}

	/**
	 * Returns the singleton instance.
	 * @return DaoMutAssessorCache singleton instance.
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
	 * @param record    mutation assessor record
	 * @return          number of records successfully added.
	 * @throws          SQLException Database Error.
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
					("INSERT INTO mutation_assessor_cache (`CACHE_KEY`, `PREDICTED_IMPACT`," +
					 " `FUNC_IMPACT_SCORE`, `PROTEIN_CHANGE`," +
					 " `STRUCTURE_LINK`, `ALIGNMENT_LINK`) VALUES (?,?,?,?,?,?)");

			pstmt.setString(1, record.getKey());
			pstmt.setString(2, record.getImpact());
			pstmt.setFloat(3, record.getImpactScore());
			pstmt.setString(4, record.getProteinChange());
			pstmt.setString(5, record.getStructureLink());
			pstmt.setString(6, record.getAlignmentLink());

			int rows = pstmt.executeUpdate();
			return rows;
		} catch (SQLException e) {
			throw e;
		} finally {
			DatabaseUtil.closeAll(con, pstmt, rs);
		}
	}

	/**
	 * Retrieves the MutationAssessorRecord corresponding to the given key.
	 * Returns null if no matching record found.
	 *
	 * @param key   key to search
	 * @return      MutationAssessorRecord for the given key
	 * @throws SQLException
	 */
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
				record.setImpactScore(rs.getFloat("FUNC_IMPACT_SCORE"));
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

	/**
	 * Generates and insert SQL query string for single element insertion.
	 *
	 * @param record    mutation assessor record
	 * @return          SQL query string
	 */
	public String getInsertSql(MutationAssessorRecord record)
	{
		return this.getInsertHead() + "(" +
		       this.getInsertValues(record) + ");";
	}

	/**
	 * Beginning of the insert SQL string for mutation assessor cache.
	 *
	 * @return  beginning of the insert SQL
	 */
	public String getInsertHead()
	{
		return "INSERT INTO mutation_assessor_cache (`CACHE_KEY`, " +
			"`PREDICTED_IMPACT`, `FUNC_IMPACT_SCORE`, `PROTEIN_CHANGE`, " +
			"`STRUCTURE_LINK`, `ALIGNMENT_LINK`) VALUES ";
	}

	/**
	 * Generates the "values" as a part of the SQL query for the
	 * given mutation assessor record.
	 *
	 * @param record    mutation assessor record
	 * @return          part of the SQL query for values
	 */
	public String getInsertValues(MutationAssessorRecord record)
	{
		String impactScore;

		if(record.getImpactScore() == null)
		{
			impactScore = "NULL";
		}
		else
		{
			impactScore = "'" + record.getImpactScore().toString() + "'";
		}

		return "'" + record.getKey() + "', " +
			"'" + record.getImpact() + "', " +
			impactScore + ", " +
			"'" + record.getProteinChange() + "', " +
			"'" + record.getStructureLink() + "', " +
			"'" + record.getAlignmentLink() + "'";
	}
}
