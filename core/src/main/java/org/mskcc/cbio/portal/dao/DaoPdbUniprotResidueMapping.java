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
import org.mskcc.cbio.portal.model.PdbUniprotResidueMapping;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 *
 * @author jgao
 */
public final class DaoPdbUniprotResidueMapping {
    private DaoPdbUniprotResidueMapping() {}
    
    public static int addPdbUniprotAlignment(int alignId, String pdbId, String chain,
            String uniprotId, int pdbFrom, int pdbTo, int uniprotFrom, int uniprotTo,
            double evalue, double identity, double identp) {
        if (!MySQLbulkLoader.isBulkLoad()) {
            throw new IllegalStateException("only bulk load is supported");
        }
        
        MySQLbulkLoader.getMySQLbulkLoader("pdb_uniprot_alignment").insertRecord(
                Integer.toString(alignId),
                pdbId,
                chain,
                uniprotId,
                Integer.toString(pdbFrom),
                Integer.toString(pdbTo),
                Integer.toString(uniprotFrom),
                Integer.toString(uniprotTo),
                Double.toString(evalue),
                Double.toString(identity),
                Double.toString(identp));
        return 1;
    }
    
    public static int addPdbUniprotResidueMapping(int alignId, int pdbPos, int uniprotPos,
            char match) throws DaoException {
        if (!MySQLbulkLoader.isBulkLoad()) {
            throw new IllegalStateException("only bulk load is supported");
        }
        //  write to the temp file maintained by the MySQLbulkLoader
        MySQLbulkLoader.getMySQLbulkLoader("pdb_uniprot_residue_mapping").insertRecord(
                Integer.toString(alignId),
                Integer.toString(pdbPos),
                Integer.toString(uniprotPos),
                Character.toString(match));

        // return 1 because normal insert will return 1 if no error occurs
        return 1;
    }

	/**
	 * Retrieves all alignments for the given Uniprot id.
	 *
	 * @param uniprotId     uniprot id
	 * @return  a list of PdbUniprotAlignment instances
	 * @throws DaoException
	 */
	public static List<PdbUniprotAlignment> getAlignments(String uniprotId) throws DaoException
	{
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			con = JdbcUtil.getDbConnection(DaoPdbUniprotResidueMapping.class);
			pstmt = con.prepareStatement("SELECT * FROM pdb_uniprot_alignment " +
			                             "WHERE UNIPROT_ID=?");
			pstmt.setString(1, uniprotId);
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
			JdbcUtil.closeAll(DaoPdbUniprotResidueMapping.class, con, pstmt, rs);
		}

	}

	/**
	 * Maps the given Uniprot positions for the provided alignment id
	 * to PDB positions (PdbUniprotResidueMapping instances).
	 *
	 * @param alignmentId       alignment id to match
	 * @param uniprotPositions  set of uniprot positions
	 * @return      a map of uniprot positions to pdb positions
	 * @throws DaoException
	 */
	public static Map<Integer, PdbUniprotResidueMapping> mapToPdbResidues(int alignmentId,
			Set<Integer> uniprotPositions) throws DaoException
	{
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			con = JdbcUtil.getDbConnection(DaoPdbUniprotResidueMapping.class);
			pstmt = con.prepareStatement("SELECT * FROM pdb_uniprot_residue_mapping " +
			                             "WHERE ALIGNMENT_ID=? " +
			                             "ORDER BY UNIPROT_POSITION ASC");
			pstmt.setInt(1, alignmentId);
			rs = pstmt.executeQuery();

			Map<Integer, PdbUniprotResidueMapping> map = new HashMap<Integer, PdbUniprotResidueMapping>();

			while (rs.next())
			{
				PdbUniprotResidueMapping mapping = extractResidueMapping(rs);

				// only add positions matching the ones in the provided set
				if (uniprotPositions.contains(mapping.getUniprotPos()))
				{
					map.put(mapping.getUniprotPos(), mapping);
				}
			}

			return map;
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JdbcUtil.closeAll(DaoPdbUniprotResidueMapping.class, con, pstmt, rs);
		}
	}

	/**
	 * Extracts a single result row into a PdbUniprotResidueMapping instance.
	 *
	 * @param rs    Result Set
	 * @return      PdbUniprotResidueMapping instance
	 * @throws SQLException
	 */
	private static PdbUniprotResidueMapping extractResidueMapping(ResultSet rs) throws SQLException
	{
		Integer alignmentId = rs.getInt(1);
		Integer pdbPosition = rs.getInt(2);
		Integer uniprotPosition = rs.getInt(3);
		String match = rs.getString(4);

		return new PdbUniprotResidueMapping(alignmentId,
				pdbPosition,
				uniprotPosition,
				match);
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
		Float eValue = rs.getFloat(9);
		Float identity = rs.getFloat(10);
		Float identityProtein = rs.getFloat(11);

		alignment.setAlignmentId(alignmentId);
		alignment.setPdbId(pdbId);
		alignment.setChain(chain);
		alignment.setUniprotId(uniprotId);
		alignment.setUniprotFrom(uniprotFrom);
		alignment.setUniprotTo(uniprotTo);
		alignment.setPdbFrom(pdbFrom);
		alignment.setPdbTo(pdbTo);
		alignment.setEValue(eValue);
		alignment.setIdentity(identity);
		alignment.setIdentityProtein(identityProtein);

		return alignment;
	}
}
