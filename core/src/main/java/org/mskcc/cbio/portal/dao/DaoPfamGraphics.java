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

/**
 * Dao for the pfam graphics cache.
 *
 * @author Selcuk Onur Sumer
 */
public class DaoPfamGraphics
{
	/**
	 * Inserts the given key and text pair to the database.
	 *
	 * @param uniprotAcc	uniprot accession
	 * @param jsonData	pfam graphics data as a JSON object
	 * @throws DaoException	if an entity already exists with the same key
	 */
	public int addPfamGraphics(String uniprotAcc, String jsonData) throws DaoException
	{
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try
		{
			con = JdbcUtil.getDbConnection(DaoTextCache.class);
			pstmt = con.prepareStatement(
				"INSERT INTO pfam_graphics (`UNIPROT_ACC`, `JSON_DATA`) VALUES (?,?)");
			pstmt.setString(1, uniprotAcc);
			pstmt.setString(2, jsonData);

			return pstmt.executeUpdate();
		}
		catch (SQLException e)
		{
			throw new DaoException(e);
		}
		finally
		{
			JdbcUtil.closeAll(DaoTextCache.class, con, pstmt, rs);
		}
	}

	/**
	 * Retrieves the text corresponding to the given key form the DB.
	 *
	 * @param uniprotAcc	a uniprot acession
	 * @return  pfam data as a JSON string
	 * @throws DaoException
	 */
	public String getPfamGraphics(String uniprotAcc) throws DaoException
	{
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try
		{
			con = JdbcUtil.getDbConnection(DaoTextCache.class);
			pstmt = con.prepareStatement(
					"SELECT * FROM pfam_graphics WHERE UNIPROT_ACC=?");
			pstmt.setString(1, uniprotAcc);
			rs = pstmt.executeQuery();

			if (rs.next())
			{
				return rs.getString("JSON_DATA");
			}

			return null;
		}
		catch (SQLException e)
		{
			throw new DaoException(e);
		}
		finally
		{
			JdbcUtil.closeAll(DaoTextCache.class, con, pstmt, rs);
		}
	}
}
