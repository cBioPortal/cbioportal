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
    
    public static int addPdbUniprotAlignment(PdbUniprotAlignment alignment) {
        if (!MySQLbulkLoader.isBulkLoad()) {
            throw new IllegalStateException("only bulk load is supported");
        }
        
        MySQLbulkLoader.getMySQLbulkLoader("pdb_uniprot_alignment").insertRecord(
                Integer.toString(alignment.getAlignmentId()),
                alignment.getPdbId(),
                alignment.getChain(),
                alignment.getUniprotId(),
                alignment.getPdbFrom(),
                alignment.getPdbTo(),
                Integer.toString(alignment.getUniprotFrom()),
                Integer.toString(alignment.getUniprotTo()),
                alignment.getEValue()==null?null:Double.toString(alignment.getEValue()),
                Double.toString(alignment.getIdentity()),
                Double.toString(alignment.getIdentityPerc()),
                alignment.getUniprotAlign(),
                alignment.getPdbAlign(),
                alignment.getMidlineAlign());
        return 1;
    }
    
    public static int addPdbUniprotResidueMapping(PdbUniprotResidueMapping mapping) throws DaoException {
        if (!MySQLbulkLoader.isBulkLoad()) {
            throw new IllegalStateException("only bulk load is supported");
        }
        //  write to the temp file maintained by the MySQLbulkLoader
        MySQLbulkLoader.getMySQLbulkLoader("pdb_uniprot_residue_mapping").insertRecord(
                Integer.toString(mapping.getAlignmentId()),
                Integer.toString(mapping.getPdbPos()),
                mapping.getPdbInsertionCode(),
                Integer.toString(mapping.getUniprotPos()),
                mapping.getMatch());

        // return 1 because normal insert will return 1 if no error occurs
        return 1;
    }
    
    
    
    public static int getLargestAlignmentId() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoPdbUniprotResidueMapping.class);
            pstmt = con.prepareStatement
                    ("SELECT MAX(`ALIGNMENT_ID`) FROM `pdb_uniprot_alignment`");
            rs = pstmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoPdbUniprotResidueMapping.class, con, pstmt, rs);
        }
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
			                             "WHERE UNIPROT_ID=? " +
			                             "ORDER BY UNIPROT_FROM ASC");
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
			con = JdbcUtil.getDbConnection(DaoPdbUniprotResidueMapping.class);
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
			JdbcUtil.closeAll(DaoPdbUniprotResidueMapping.class, con, pstmt, rs);
		}
	}

	/**
	 * Retrieves all residue mappings (position mappings)
	 * for the given alignment id.
	 *
	 * @param alignmentId   alignment id
	 * @return  a list of PdbUniprotResidueMapping instances
	 * @throws DaoException
	 */
	public static List<PdbUniprotResidueMapping> getResidueMappings(Integer alignmentId) throws DaoException
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

			List<PdbUniprotResidueMapping> list = new ArrayList<PdbUniprotResidueMapping>();

			while (rs.next())
			{
				list.add(extractResidueMapping(rs));
			}

			return list;
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

		// TODO do not sort by uniprot position
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

    public static void deleteAllRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoPdbUniprotResidueMapping.class);
            pstmt = con.prepareStatement("TRUNCATE TABLE pdb_uniprot_alignment");
            pstmt.executeUpdate();
            pstmt = con.prepareStatement("TRUNCATE TABLE pdb_uniprot_residue_mapping");
            pstmt.executeUpdate();
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
		Integer alignmentId = rs.getInt("ALIGNMENT_ID");
		Integer pdbPosition = rs.getInt("PDB_POSITION");
		String pdbInsertion = rs.getString("PDB_INSERTION_CODE");
		Integer uniprotPosition = rs.getInt("UNIPROT_POSITION");
		String match = rs.getString("MATCH");

		return new PdbUniprotResidueMapping(alignmentId,
				pdbPosition,
				pdbInsertion,
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

		Integer alignmentId = rs.getInt("ALIGNMENT_ID");
		String pdbId = rs.getString("PDB_ID");
		String chain = rs.getString("CHAIN");
		String uniprotId = rs.getString("UNIPROT_ID");
		String pdbFrom = rs.getString("PDB_FROM");
		String pdbTo = rs.getString("PDB_TO");
		Integer uniprotFrom = rs.getInt("UNIPROT_FROM");
		Integer uniprotTo = rs.getInt("UNIPROT_TO");
		Float eValue = rs.getFloat("EVALUE");
		Float identity = rs.getFloat("IDENTITY");
		Float identityProtein = rs.getFloat("IDENTP");
		String uniprotAlign = rs.getString("UNIPROT_ALIGN");
		String pdbAlign = rs.getString("PDB_ALIGN");
		String midlineAlign = rs.getString("MIDLINE_ALIGN");

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
		alignment.setIdentityPerc(identityProtein);
		alignment.setUniprotAlign(uniprotAlign);
		alignment.setPdbAlign(pdbAlign);
		alignment.setMidlineAlign(midlineAlign);

		return alignment;
	}
}
