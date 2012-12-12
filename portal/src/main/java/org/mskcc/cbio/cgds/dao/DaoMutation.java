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

package org.mskcc.cbio.cgds.dao;

import org.mskcc.cbio.cgds.model.ExtendedMutation;
import org.mskcc.cbio.cgds.model.CanonicalGene;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;

/**
 * Data access object for Mutation table
 */
public class DaoMutation {
	public static final String NAN = "NaN";
	private static DaoMutation daoMutation = null;
	// use a MySQLbulkLoader instead of SQL "INSERT" statements to load data into table
	private static MySQLbulkLoader myMySQLbulkLoader = null;
	private static HashMap <String, PreparedStatement> preparedStatementMap =
			new HashMap <String, PreparedStatement> ();
	private static final String LOOK_UP_1 = "LOOK_UP_1";

	/**
	 * Private Constructor (Singleton pattern).
	 */
	private DaoMutation() {
	}

	/**
	 * Gets Instance of Dao Object. (Singleton pattern).
	 *
	 * @return DaoGeneticAlteration Object.
	 * @throws org.mskcc.cbio.cgds.dao.DaoException Dao Initialization Error.
	 */
	public static DaoMutation getInstance() throws DaoException {
		if (daoMutation == null) {
			daoMutation = new DaoMutation();

		}
		// create the MySQLbulkLoader if it doesn't exist
		if( myMySQLbulkLoader == null ){
			myMySQLbulkLoader = new MySQLbulkLoader( "mutation" );
		}

		return daoMutation;
	}

	public int addMutation(ExtendedMutation mutation) throws DaoException {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			if (MySQLbulkLoader.isBulkLoad()) {

				// use this code if bulk loading
				// write to the temp file maintained by the MySQLbulkLoader
				myMySQLbulkLoader.insertRecord(

						Integer.toString( mutation.getGeneticProfileId() ),
						mutation.getCaseId(),
						Long.toString( mutation.getGene().getEntrezGeneId()),
						mutation.getSequencingCenter(),
						mutation.getSequencer(),
						mutation.getMutationStatus(),
						mutation.getValidationStatus(),
						mutation.getChr(),
						Long.toString( mutation.getStartPosition() ),
						Long.toString( mutation.getEndPosition() ),
						mutation.getProteinChange(),
						mutation.getMutationType(),
						mutation.getFunctionalImpactScore(),
						mutation.getLinkXVar(),
						mutation.getLinkPdb(),
						mutation.getLinkMsa(),
						mutation.getNcbiBuild(),
						mutation.getStrand(),
						mutation.getVariantType(),
						mutation.getReferenceAllele(),
						mutation.getTumorSeqAllele1(),
						mutation.getTumorSeqAllele2(),
						mutation.getDbSnpRs(),
						mutation.getDbSnpValStatus(),
						mutation.getMatchedNormSampleBarcode(),
						mutation.getMatchNormSeqAllele1(),
						mutation.getMatchNormSeqAllele2(),
						mutation.getTumorValidationAllele1(),
						mutation.getTumorValidationAllele2(),
						mutation.getMatchNormValidationAllele1(),
						mutation.getMatchNormValidationAllele2(),
						mutation.getVerificationStatus(),
						mutation.getSequencingPhase(),
						mutation.getSequenceSource(),
						mutation.getValidationMethod(),
						mutation.getScore(),
						mutation.getBamFile(),
						Integer.toString(mutation.getTumorAltCount()),
						Integer.toString(mutation.getTumorRefCount()),
						Integer.toString(mutation.getNormalAltCount()),
						Integer.toString(mutation.getNormalRefCount()),
						mutation.getOncotatorDbSnpRs(),
						DaoMutationEvent.filterCosmic(mutation));

				// return 1 because normal insert will return 1 if no error occurs
				return 1;
			} else {


				con = JdbcUtil.getDbConnection();
				pstmt = con.prepareStatement
						("INSERT INTO mutation (`GENETIC_PROFILE_ID`, `CASE_ID`,"
						 + " `ENTREZ_GENE_ID`,"
						 + " `CENTER`, `SEQUENCER`, `MUTATION_STATUS`, `VALIDATION_STATUS`, `CHR`,"
						 + " `START_POSITION`, `END_POSITION`, `PROTEIN_CHANGE`, "
						 + " `MUTATION_TYPE`, `FUNCTIONAL_IMPACT_SCORE`, `LINK_XVAR`, `LINK_PDB`,"
						 + " `LINK_MSA`, `NCBI_BUILD`, `STRAND`, `VARIANT_TYPE`, `REFERENCE_ALLELE`,"
						 + " `TUMOR_SEQ_ALLELE1`, `TUMOR_SEQ_ALLELE2`, `DB_SNP_RS`, `DB_SNP_VAL_STATUS`,"
						 + " `MATCHED_NORM_SAMPLE_BARCODE`, `MATCH_NORM_SEQ_ALLELE1`, `MATCH_NORM_SEQ_ALLELE2`,"
						 + " `TUMOR_VALIDATION_ALLELE1`, `TUMOR_VALIDATION_ALLELE2`,"
						 + " `MATCH_NORM_VALIDATION_ALLELE1`, `MATCH_NORM_VALIDATION_ALLELE2`,"
						 + " `VERIFICATION_STATUS`, `SEQUENCING_PHASE`, `SEQUENCE_SOURCE`, `VALIDATION_METHOD`,"
						 + " `SCORE`, `BAM_FILE`, `TUMOR_ALT_COUNT`, `TUMOR_REF_COUNT`, `NORMAL_ALT_COUNT`,"
						 + " `NORMAL_REF_COUNT`, `ONCOTATOR_DBSNP_RS`, `ONCOTATOR_COSMIC_OVERLAPPING`)"
						 + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

				pstmt.setInt(1, mutation.getGeneticProfileId());
				pstmt.setString(2, mutation.getCaseId());
				pstmt.setLong(3, mutation.getGene().getEntrezGeneId());
				pstmt.setString(4, mutation.getSequencingCenter());
				pstmt.setString(5, mutation.getSequencer());
				pstmt.setString(6, mutation.getMutationStatus());
				pstmt.setString(7, mutation.getValidationStatus());
				pstmt.setString(8, mutation.getChr());
				pstmt.setLong(9, mutation.getStartPosition());
				pstmt.setLong(10, mutation.getEndPosition());
				pstmt.setString(11, mutation.getProteinChange());
				pstmt.setString(12, mutation.getMutationType());
				pstmt.setString(13, mutation.getFunctionalImpactScore());
				pstmt.setString(14, mutation.getLinkXVar());
				pstmt.setString(15, mutation.getLinkPdb());
				pstmt.setString(16, mutation.getLinkMsa());
				pstmt.setString(17, mutation.getNcbiBuild());
				pstmt.setString(18, mutation.getStrand());
				pstmt.setString(19, mutation.getVariantType());
				pstmt.setString(20, mutation.getReferenceAllele());
				pstmt.setString(21, mutation.getTumorSeqAllele1());
				pstmt.setString(22, mutation.getTumorSeqAllele2());
				pstmt.setString(23, mutation.getDbSnpRs());
				pstmt.setString(24, mutation.getDbSnpValStatus());
				pstmt.setString(25, mutation.getMatchedNormSampleBarcode());
				pstmt.setString(26, mutation.getMatchNormSeqAllele1());
				pstmt.setString(27, mutation.getMatchNormSeqAllele2());
				pstmt.setString(28, mutation.getTumorValidationAllele1());
				pstmt.setString(29, mutation.getTumorValidationAllele2());
				pstmt.setString(30, mutation.getMatchNormValidationAllele1());
				pstmt.setString(31, mutation.getMatchNormValidationAllele2());
				pstmt.setString(32, mutation.getVerificationStatus());
				pstmt.setString(33, mutation.getSequencingPhase());
				pstmt.setString(34, mutation.getSequenceSource());
				pstmt.setString(35, mutation.getValidationMethod());
				pstmt.setString(36, mutation.getScore());
				pstmt.setString(37, mutation.getBamFile());
				pstmt.setInt(38, mutation.getTumorAltCount());
				pstmt.setInt(39, mutation.getTumorRefCount());
				pstmt.setInt(40, mutation.getNormalAltCount());
				pstmt.setInt(41, mutation.getNormalRefCount());
				pstmt.setString(42, mutation.getOncotatorDbSnpRs());
				pstmt.setString(43, DaoMutationEvent.filterCosmic(mutation));

				return pstmt.executeUpdate();
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JdbcUtil.closeAll(con, pstmt, rs);
		}
	}

	/**
	 * load the temp file maintained by the MySQLbulkLoader into the DMBS.
	 *
	 * @return number of records inserted
	 * @throws DaoException
	 */
	public int flushMutations() throws DaoException {
		try {
			return myMySQLbulkLoader.loadDataFromTempFileIntoDBMS();
		} catch (IOException e) {
			System.err.println("Could not open temp file");
			e.printStackTrace();
			return -1;
		}
	}

	public ArrayList<ExtendedMutation> getMutations (int geneticProfileId, Collection<String> targetCaseList,
			long entrezGeneId) throws DaoException {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ArrayList <ExtendedMutation> mutationList = new ArrayList <ExtendedMutation>();
		try {
			con = JdbcUtil.getDbConnection();
			pstmt = con.prepareStatement
					("SELECT * FROM mutation WHERE CASE_ID IN ('"
					 +org.apache.commons.lang.StringUtils.join(targetCaseList, "','")+
					 "') AND GENETIC_PROFILE_ID = ? AND ENTREZ_GENE_ID = ?");
			pstmt.setInt(1, geneticProfileId);
			pstmt.setLong(2, entrezGeneId);
			rs = pstmt.executeQuery();
			while  (rs.next()) {
				ExtendedMutation mutation = extractMutation(rs);
				mutationList.add(mutation);
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JdbcUtil.closeAll(con, pstmt, rs);
		}
		return mutationList;
	}

	public ArrayList<ExtendedMutation> getMutations (int geneticProfileId, String caseId,
			long entrezGeneId) throws DaoException {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ArrayList <ExtendedMutation> mutationList = new ArrayList <ExtendedMutation>();
		try {
			con = JdbcUtil.getDbConnection();
			pstmt = con.prepareStatement
					("SELECT * FROM mutation WHERE CASE_ID = ? AND" +
					 " GENETIC_PROFILE_ID = ? AND ENTREZ_GENE_ID = ?");
			pstmt.setString(1, caseId);
			pstmt.setInt(2, geneticProfileId);
			pstmt.setLong(3, entrezGeneId);
			rs = pstmt.executeQuery();
			while  (rs.next()) {
				ExtendedMutation mutation = extractMutation(rs);
				mutationList.add(mutation);
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JdbcUtil.closeAll(con, pstmt, rs);
		}
		return mutationList;
	}

	/**
	 * Gets all Genes in a Specific Genetic Profile.
	 *
	 * @param geneticProfileId  Genetic Profile ID.
	 * @return Set of Canonical Genes.
	 * @throws DaoException Database Error.
	 */
	public Set <CanonicalGene> getGenesInProfile(int geneticProfileId) throws DaoException {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Set<CanonicalGene> geneSet = new HashSet<CanonicalGene>();
		DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
		try {
			con = JdbcUtil.getDbConnection();
			pstmt = con.prepareStatement
					("SELECT * FROM mutation WHERE GENETIC_PROFILE_ID = ?");
			pstmt.setInt(1, geneticProfileId);
			rs = pstmt.executeQuery();
			while  (rs.next()) {
				ExtendedMutation mutation = extractMutation(rs);
				geneSet.add(daoGene.getGene(mutation.getGene().getEntrezGeneId()));
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JdbcUtil.closeAll(con, pstmt, rs);
		}
		return geneSet;
	}
        
        public ArrayList<ExtendedMutation> getMutations (long entrezGeneId) throws DaoException {
            Connection con = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            ArrayList <ExtendedMutation> mutationList = new ArrayList <ExtendedMutation>();
            try {
                con = JdbcUtil.getDbConnection();
                pstmt = con.prepareStatement
                        ("SELECT * FROM mutation WHERE" +
                                " ENTREZ_GENE_ID = ?");
                pstmt.setLong(1, entrezGeneId);
                rs = pstmt.executeQuery();
                while  (rs.next()) {
                    ExtendedMutation mutation = extractMutation(rs);
                    mutationList.add(mutation);
                }
            } catch (SQLException e) {
                throw new DaoException(e);
            } finally {
                JdbcUtil.closeAll(con, pstmt, rs);
            }
            return mutationList;
        }

        public ArrayList<ExtendedMutation> getMutations (long entrezGeneId, String aminoAcidChange) throws DaoException {
            Connection con = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            ArrayList <ExtendedMutation> mutationList = new ArrayList <ExtendedMutation>();
            try {
                con = JdbcUtil.getDbConnection();
                pstmt = con.prepareStatement
                        ("SELECT * FROM mutation WHERE" +
                                " ENTREZ_GENE_ID = ? AND AMINO_ACID_CHANGE = ?");
                pstmt.setLong(1, entrezGeneId);
                pstmt.setString(2, aminoAcidChange);
                rs = pstmt.executeQuery();
                while  (rs.next()) {
                    ExtendedMutation mutation = extractMutation(rs);
                    mutationList.add(mutation);
                }
            } catch (SQLException e) {
                throw new DaoException(e);
            } finally {
                JdbcUtil.closeAll(con, pstmt, rs);
            }
            return mutationList;
        }
	
        public ArrayList<ExtendedMutation> getMutations (int geneticProfileId, String CaseId) throws DaoException {
            Connection con = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            ArrayList <ExtendedMutation> mutationList = new ArrayList <ExtendedMutation>();
            try {
                con = JdbcUtil.getDbConnection();
                pstmt = con.prepareStatement
                        ("SELECT * FROM mutation WHERE GENETIC_PROFILE_ID = ? AND CASE_ID = ?");
                pstmt.setInt(1, geneticProfileId);
                pstmt.setString(2, CaseId);
                rs = pstmt.executeQuery();
                while  (rs.next()) {
                    ExtendedMutation mutation = extractMutation(rs);
                    mutationList.add(mutation);
                }
            } catch (SQLException e) {
                throw new DaoException(e);
            } finally {
                JdbcUtil.closeAll(con, pstmt, rs);
            }
            return mutationList;
        }

        public ArrayList<ExtendedMutation> getSimilarMutations (long entrezGeneId, String aminoAcidChange, String excludeCaseId) throws DaoException {
            Connection con = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            ArrayList <ExtendedMutation> mutationList = new ArrayList <ExtendedMutation>();
            try {
                con = JdbcUtil.getDbConnection();
                pstmt = con.prepareStatement
                        ("SELECT * FROM mutation WHERE" +
                                " ENTREZ_GENE_ID = ? AND AMINO_ACID_CHANGE = ? AND CASE_ID <> ?");
                pstmt.setLong(1, entrezGeneId);
                pstmt.setString(2, aminoAcidChange);
                pstmt.setString(3, excludeCaseId);
                rs = pstmt.executeQuery();
                while  (rs.next()) {
                    ExtendedMutation mutation = extractMutation(rs);
                    mutationList.add(mutation);
                }
            } catch (SQLException e) {
                throw new DaoException(e);
            } finally {
                JdbcUtil.closeAll(con, pstmt, rs);
            }
            return mutationList;
        }

	public ArrayList<ExtendedMutation> getMutations (int geneticProfileId,
			long entrezGeneId) throws DaoException {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ArrayList <ExtendedMutation> mutationList = new ArrayList <ExtendedMutation>();
		try {
			con = JdbcUtil.getDbConnection();
			pstmt = con.prepareStatement
					("SELECT * FROM mutation WHERE" +
					 " GENETIC_PROFILE_ID = ? AND ENTREZ_GENE_ID = ?");
			pstmt.setInt(1, geneticProfileId);
			pstmt.setLong(2, entrezGeneId);
			rs = pstmt.executeQuery();
			while  (rs.next()) {
				ExtendedMutation mutation = extractMutation(rs);
				mutationList.add(mutation);
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JdbcUtil.closeAll(con, pstmt, rs);
		}
		return mutationList;
	}

	public ArrayList<ExtendedMutation> getAllMutations () throws DaoException {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ArrayList <ExtendedMutation> mutationList = new ArrayList <ExtendedMutation>();
		try {
			con = JdbcUtil.getDbConnection();
			pstmt = con.prepareStatement
					("SELECT * FROM mutation");
			rs = pstmt.executeQuery();
			while  (rs.next()) {
				ExtendedMutation mutation = extractMutation(rs);
				mutationList.add(mutation);
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JdbcUtil.closeAll(con, pstmt, rs);
		}
		return mutationList;
	}

	private ExtendedMutation extractMutation(ResultSet rs) throws SQLException, DaoException {
		ExtendedMutation mutation = new ExtendedMutation();

		mutation.setGeneticProfileId(rs.getInt("GENETIC_PROFILE_ID"));
		mutation.setCaseId(rs.getString("CASE_ID"));
		long entrezId = rs.getLong("ENTREZ_GENE_ID");
		DaoGeneOptimized aDaoGene = DaoGeneOptimized.getInstance();
		CanonicalGene gene = aDaoGene.getGene(entrezId);
		mutation.setGene(gene);
		mutation.setSequencingCenter(rs.getString("CENTER"));
		mutation.setSequencer(rs.getString("SEQUENCER"));
		mutation.setMutationStatus(rs.getString("MUTATION_STATUS"));
		mutation.setValidationStatus(rs.getString("VALIDATION_STATUS"));
		mutation.setChr(rs.getString("CHR"));
		mutation.setStartPosition(rs.getLong("START_POSITION"));
		mutation.setEndPosition(rs.getLong("END_POSITION"));
		mutation.setProteinChange(rs.getString("PROTEIN_CHANGE"));
		mutation.setMutationType(rs.getString("MUTATION_TYPE"));
		mutation.setFunctionalImpactScore(rs.getString("FUNCTIONAL_IMPACT_SCORE"));
		mutation.setLinkXVar(rs.getString("LINK_XVAR"));
		mutation.setLinkPdb(rs.getString("LINK_PDB"));
		mutation.setLinkMsa(rs.getString("LINK_MSA"));
		mutation.setNcbiBuild(rs.getString("NCBI_BUILD"));
		mutation.setStrand(rs.getString("STRAND"));
		mutation.setVariantType(rs.getString("VARIANT_TYPE"));
		mutation.setReferenceAllele(rs.getString("REFERENCE_ALLELE"));
		mutation.setTumorSeqAllele1(rs.getString("TUMOR_SEQ_ALLELE1"));
		mutation.setTumorSeqAllele2(rs.getString("TUMOR_SEQ_ALLELE2"));
		mutation.setDbSnpRs(rs.getString("DB_SNP_RS"));
		mutation.setDbSnpValStatus(rs.getString("DB_SNP_VAL_STATUS"));
		mutation.setMatchedNormSampleBarcode(rs.getString("MATCHED_NORM_SAMPLE_BARCODE"));
		mutation.setMatchNormSeqAllele1(rs.getString("MATCH_NORM_SEQ_ALLELE1"));
		mutation.setMatchNormSeqAllele2(rs.getString("MATCH_NORM_SEQ_ALLELE2"));
		mutation.setTumorValidationAllele1(rs.getString("TUMOR_VALIDATION_ALLELE1"));
		mutation.setTumorValidationAllele2(rs.getString("TUMOR_VALIDATION_ALLELE2"));
		mutation.setMatchNormValidationAllele1(rs.getString("MATCH_NORM_VALIDATION_ALLELE1"));
		mutation.setMatchNormValidationAllele2(rs.getString("MATCH_NORM_VALIDATION_ALLELE2"));
		mutation.setVerificationStatus(rs.getString("VERIFICATION_STATUS"));
		mutation.setSequencingPhase(rs.getString("SEQUENCING_PHASE"));
		mutation.setSequenceSource(rs.getString("SEQUENCE_SOURCE"));
		mutation.setValidationMethod(rs.getString("VALIDATION_METHOD"));
		mutation.setScore(rs.getString("SCORE"));
		mutation.setBamFile(rs.getString("BAM_FILE"));
		mutation.setTumorAltCount(rs.getInt("TUMOR_ALT_COUNT"));
		mutation.setTumorRefCount(rs.getInt("TUMOR_REF_COUNT"));
		mutation.setNormalAltCount(rs.getInt("NORMAL_ALT_COUNT"));
		mutation.setNormalRefCount(rs.getInt("NORMAL_REF_COUNT"));
		mutation.setOncotatorDbSnpRs(rs.getString("ONCOTATOR_DBSNP_RS"));
		mutation.setOncotatorCosmicOverlapping(rs.getString("ONCOTATOR_COSMIC_OVERLAPPING"));

		return mutation;
	}

	public int getCount() throws DaoException {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			con = JdbcUtil.getDbConnection();
			pstmt = con.prepareStatement
					("SELECT COUNT(*) FROM mutation");
			rs = pstmt.executeQuery();
			if (rs.next()) {
				return rs.getInt(1);
			}
			return 0;
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JdbcUtil.closeAll(con, pstmt, rs);
		}
	}

	public void deleteAllRecordsInGeneticProfile(long geneticProfileId) throws DaoException {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			con = JdbcUtil.getDbConnection();
			pstmt = con.prepareStatement("DELETE from mutation WHERE GENETIC_PROFILE_ID=?");
			pstmt.setLong(1, geneticProfileId);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JdbcUtil.closeAll(con, pstmt, rs);
		}
	}

	public void deleteAllRecords() throws DaoException {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			con = JdbcUtil.getDbConnection();
			pstmt = con.prepareStatement("TRUNCATE TABLE mutation");
			pstmt.executeUpdate();
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JdbcUtil.closeAll(con, pstmt, rs);
		}
	}
}
