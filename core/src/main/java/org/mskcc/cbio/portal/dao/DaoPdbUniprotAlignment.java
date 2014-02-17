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

package org.mskcc.cbio.portal.dao;

import org.mskcc.cbio.portal.model.PdbUniprotAlignment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 *
 * @author jgao
 */
public final class DaoPdbUniprotAlignment {
    private DaoPdbUniprotAlignment() {}
    
    public static int addPdbUniprotAlignment(PdbUniprotAlignment alignment) {
        if (!MySQLbulkLoader.isBulkLoad()) {
            throw new IllegalStateException("only bulk load is supported");
        }
        
        MySQLbulkLoader.getMySQLbulkLoader("pdb_uniprot_alignment").insertRecord(
                Integer.toString(alignment.getAlignmentId()),
                alignment.getPdbId(),
                alignment.getChain(),
                alignment.getUniprotAcc(),
                Integer.toString(alignment.getPdbFrom()),
                Integer.toString(alignment.getPdbTo()),
                Integer.toString(alignment.getUniprotFrom()),
                Integer.toString(alignment.getUniprotTo()));
        return 1;
    }

	/**
	 * Retrieves all alignments for the given Uniprot id.
	 *
	 * @param uniprotId     uniprot id
	 * @return  a list of PdbUniprotAlignment instances
	 * @throws DaoException
	 */
	public static List<PdbUniprotAlignment> getAlignments(String uniprotAcc) throws DaoException
	{
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			con = JdbcUtil.getDbConnection(DaoPdbUniprotAlignment.class);
			pstmt = con.prepareStatement("SELECT * FROM pdb_uniprot_alignment " +
			                             "WHERE UNIPROT_ACC=?");
			pstmt.setString(1, uniprotAcc);
			rs = pstmt.executeQuery();

			List<PdbUniprotAlignment> alignments = new ArrayList<PdbUniprotAlignment>();

			while (rs.next())
			{
				alignments.add(extractAlignment(rs));
			}

			return alignments;
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JdbcUtil.closeAll(DaoPdbUniprotAlignment.class, con, pstmt, rs);
		}
	}

	/**
	 * Retrieves the total number alignments for the given Uniprot id.
	 *
	 * @param uniprotId     uniprot id
	 * @return  total number of alignments for the given Uniprot id.
	 * @throws DaoException
	 */
	public static Integer getAlignmentCount(String uniprotId) throws DaoException
	{
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			con = JdbcUtil.getDbConnection(DaoPdbUniprotAlignment.class);
			pstmt = con.prepareStatement("SELECT COUNT(*) FROM pdb_uniprot_alignment " +
			                             "WHERE UNIPROT_ID=?");
			pstmt.setString(1, uniprotId);
			rs = pstmt.executeQuery();

			Integer count = -1;

			if (rs.next())
			{
				count = rs.getInt(1);
			}

			return count;
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JdbcUtil.closeAll(DaoPdbUniprotAlignment.class, con, pstmt, rs);
		}
	}

    public static void deleteAllRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoPdbUniprotAlignment.class);
            pstmt = con.prepareStatement("TRUNCATE TABLE pdb_uniprot_alignment");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoPdbUniprotAlignment.class, con, pstmt, rs);
        }
    }

	/**
	 * Extracts a single result row into a PdbUniprotAlignment instance.
	 *
	 * @param rs    Result Set
	 * @return      PdbUniprotAlignment instance
	 * @throws SQLException
	 */
	private static PdbUniprotAlignment extractAlignment(ResultSet rs) throws SQLException
	{
		PdbUniprotAlignment alignment = new PdbUniprotAlignment();

		Integer alignmentId = rs.getInt(1);
		String pdbId = rs.getString(2);
		String chain = rs.getString(3);
		String uniprotId = rs.getString(4);
		Integer pdbFrom = rs.getInt(5);
		Integer pdbTo = rs.getInt(6);
		Integer uniprotFrom = rs.getInt(7);
		Integer uniprotTo = rs.getInt(8);

		alignment.setAlignmentId(alignmentId);
		alignment.setPdbId(pdbId);
		alignment.setChain(chain);
		alignment.setUniprotAcc(uniprotId);
		alignment.setUniprotFrom(uniprotFrom);
		alignment.setUniprotTo(uniprotTo);
		alignment.setPdbFrom(pdbFrom);
		alignment.setPdbTo(pdbTo);

		return alignment;
	}
}
