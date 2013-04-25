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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import org.mskcc.cbio.cgds.model.ExtendedMutation.MutationEvent;

/**
 * Data access object for Mutation table
 */
public final class DaoMutation {
    public static final String NAN = "NaN";
    // use a MySQLbulkLoader instead of SQL "INSERT" statements to load data into table
    private static MySQLbulkLoader mutationMySQLbulkLoader = null,
                mutationEventMySQLbulkLoader = null;

    private DaoMutation() {}
        
        private static MySQLbulkLoader getMutationMySQLbulkLoader() {
            if (mutationMySQLbulkLoader==null) {
                mutationMySQLbulkLoader =  new MySQLbulkLoader( "mutation" );
            }
            return mutationMySQLbulkLoader;
        }
        
        private static MySQLbulkLoader getMutationEventMySQLbulkLoader() {
            if (mutationEventMySQLbulkLoader==null) {
                mutationEventMySQLbulkLoader =  new MySQLbulkLoader( "mutation_event" );
            }
            return mutationEventMySQLbulkLoader;
        }

    public static int addMutation(ExtendedMutation mutation, boolean newMutationEvent) throws DaoException {
            if (!MySQLbulkLoader.isBulkLoad()) {
                throw new DaoException("You have to turn on MySQLbulkLoader in order to insert mutations");
            } else {

                    // use this code if bulk loading
                    // write to the temp file maintained by the MySQLbulkLoader
                    getMutationMySQLbulkLoader().insertRecord(
                            Long.toString(mutation.getMutationEventId()),
                            Integer.toString(mutation.getGeneticProfileId()),
                            mutation.getCaseId(),
                            Long.toString(mutation.getGene().getEntrezGeneId()),
                            mutation.getSequencingCenter(),
                            mutation.getSequencer(),
                            mutation.getMutationStatus(),
                            mutation.getValidationStatus(),
                            mutation.getTumorSeqAllele1(),
                            mutation.getTumorSeqAllele2(),
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
                            Integer.toString(mutation.getNormalRefCount()));

                    if (newMutationEvent) {
                        return addMutationEvent(mutation)+1;
                    } else {
                        return 1;
                    }
            }
    }
        
        private static int addMutationEvent(ExtendedMutation mutation) throws DaoException {
            // use this code if bulk loading
            // write to the temp file maintained by the MySQLbulkLoader
            getMutationEventMySQLbulkLoader().insertRecord(
                    Long.toString(mutation.getMutationEventId()),
                    Long.toString(mutation.getGene().getEntrezGeneId()),
                    mutation.getChr(),
                    Long.toString(mutation.getStartPosition()),
                    Long.toString(mutation.getEndPosition()),
                    mutation.getReferenceAllele(),
                    mutation.getTumorSeqAllele(),
                    mutation.getProteinChange(),
                    mutation.getMutationType(),
                    mutation.getFunctionalImpactScore(),
                    Float.toString(mutation.getFisValue()),
                    mutation.getLinkXVar(),
                    mutation.getLinkPdb(),
                    mutation.getLinkMsa(),
                    mutation.getNcbiBuild(),
                    mutation.getStrand(),
                    mutation.getVariantType(),
                    mutation.getDbSnpRs(),
                    mutation.getDbSnpValStatus(),
                    mutation.getOncotatorDbSnpRs(),
                    DaoMutationEvent.filterCosmic(mutation),
                    mutation.getOncotatorRefseqMrnaId(),
                    mutation.getOncotatorCodonChange(),
                    mutation.getOncotatorUniprotName(),
                    mutation.getOncotatorUniprotAccession(),
                    Integer.toString(mutation.getOncotatorProteinPosStart()),
                    Integer.toString(mutation.getOncotatorProteinPosEnd()),
                    boolToStr(mutation.isCanonicalTranscript()),
                    DaoMutationEvent.extractMutationKeyword(mutation));

            // return 1 because normal insert will return 1 if no error occurs
            return 1;
    }

    /**
     * load the temp file maintained by the MySQLbulkLoader into the DMBS.
     *
     * @return number of records inserted
     * @throws DaoException
     */
    public static int flushMutations() throws DaoException {
        try {
            return getMutationMySQLbulkLoader().loadDataFromTempFileIntoDBMS()
                                + getMutationEventMySQLbulkLoader().loadDataFromTempFileIntoDBMS();
        } catch (IOException e) {
            System.err.println("Could not open temp file");
            e.printStackTrace();
            return -1;
        }
    }

    public static ArrayList<ExtendedMutation> getMutations (int geneticProfileId, Collection<String> targetCaseList,
            long entrezGeneId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ArrayList <ExtendedMutation> mutationList = new ArrayList <ExtendedMutation>();
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            pstmt = con.prepareStatement
                    ("SELECT * FROM mutation "
                    + "INNER JOIN mutation_event ON mutation.MUTATION_EVENT_ID=mutation_event.MUTATION_EVENT_ID "
                    + "WHERE CASE_ID IN ('"
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
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
        return mutationList;
    }

    public static ArrayList<ExtendedMutation> getMutations (int geneticProfileId, String caseId,
            long entrezGeneId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ArrayList <ExtendedMutation> mutationList = new ArrayList <ExtendedMutation>();
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            pstmt = con.prepareStatement
                    ("SELECT * FROM mutation "
                    + "INNER JOIN mutation_event ON mutation.MUTATION_EVENT_ID=mutation_event.MUTATION_EVENT_ID "
                    + "WHERE CASE_ID = ? AND GENETIC_PROFILE_ID = ? AND ENTREZ_GENE_ID = ?");
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
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
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
    public static Set<CanonicalGene> getGenesInProfile(int geneticProfileId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Set<CanonicalGene> geneSet = new HashSet<CanonicalGene>();
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            pstmt = con.prepareStatement
                    ("SELECT DISTINCT ENTREZ_GENE_ID FROM mutation WHERE GENETIC_PROFILE_ID = ?");
            pstmt.setInt(1, geneticProfileId);
            rs = pstmt.executeQuery();
            while  (rs.next()) {
                geneSet.add(daoGene.getGene(rs.getLong("ENTREZ_GENE_ID")));
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
        return geneSet;
    }
        
        public static ArrayList<ExtendedMutation> getMutations (long entrezGeneId) throws DaoException {
            Connection con = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            ArrayList <ExtendedMutation> mutationList = new ArrayList <ExtendedMutation>();
            try {
                con = JdbcUtil.getDbConnection(DaoMutation.class);
                pstmt = con.prepareStatement
                        ("SELECT * FROM mutation "
                        + "INNER JOIN mutation_event ON mutation.MUTATION_EVENT_ID=mutation_event.MUTATION_EVENT_ID "
                        + "WHERE ENTREZ_GENE_ID = ?");
                pstmt.setLong(1, entrezGeneId);
                rs = pstmt.executeQuery();
                while  (rs.next()) {
                    ExtendedMutation mutation = extractMutation(rs);
                    mutationList.add(mutation);
                }
            } catch (SQLException e) {
                throw new DaoException(e);
            } finally {
                JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
            }
            return mutationList;
        }

        public static ArrayList<ExtendedMutation> getMutations (long entrezGeneId, String aminoAcidChange) throws DaoException {
            Connection con = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            ArrayList <ExtendedMutation> mutationList = new ArrayList <ExtendedMutation>();
            try {
                con = JdbcUtil.getDbConnection(DaoMutation.class);
                pstmt = con.prepareStatement
                        ("SELECT * FROM mutation_event"
                        + "INNER JOIN mutation ON mutation.MUTATION_EVENT_ID=mutation_event.MUTATION_EVENT_ID "
                        + " WHERE ENTREZ_GENE_ID = ? AND AMINO_ACID_CHANGE = ?");
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
                JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
            }
            return mutationList;
        }
    
        public static ArrayList<ExtendedMutation> getMutations (int geneticProfileId, String CaseId) throws DaoException {
            Connection con = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            ArrayList <ExtendedMutation> mutationList = new ArrayList <ExtendedMutation>();
            try {
                con = JdbcUtil.getDbConnection(DaoMutation.class);
                pstmt = con.prepareStatement
                        ("SELECT * FROM mutation "
                        + "INNER JOIN mutation_event ON mutation.MUTATION_EVENT_ID=mutation_event.MUTATION_EVENT_ID "
                        + "WHERE GENETIC_PROFILE_ID = ? AND CASE_ID = ?");
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
                JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
            }
            return mutationList;
        }

        public static ArrayList<ExtendedMutation> getSimilarMutations (long entrezGeneId, String aminoAcidChange, String excludeCaseId) throws DaoException {
            Connection con = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            ArrayList <ExtendedMutation> mutationList = new ArrayList <ExtendedMutation>();
            try {
                con = JdbcUtil.getDbConnection(DaoMutation.class);
                pstmt = con.prepareStatement
                        ("SELECT * FROM mutation, mutation_event "
                        + "WHERE mutation.MUTATION_EVENT_ID=mutation_event.MUTATION_EVENT_ID "
                        + "AND ENTREZ_GENE_ID = ? AND AMINO_ACID_CHANGE = ? AND CASE_ID <> ?");
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
                JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
            }
            return mutationList;
        }

    public static ArrayList<ExtendedMutation> getMutations (int geneticProfileId,
            long entrezGeneId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ArrayList <ExtendedMutation> mutationList = new ArrayList <ExtendedMutation>();
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            pstmt = con.prepareStatement
                    ("SELECT * FROM mutation "
                        + "INNER JOIN mutation_event ON mutation.MUTATION_EVENT_ID=mutation_event.MUTATION_EVENT_ID "
                        + "WHERE GENETIC_PROFILE_ID = ? AND ENTREZ_GENE_ID = ?");
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
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
        return mutationList;
    }

    public static ArrayList<ExtendedMutation> getAllMutations () throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ArrayList <ExtendedMutation> mutationList = new ArrayList <ExtendedMutation>();
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            pstmt = con.prepareStatement
                    ("SELECT * FROM mutation"
                        + "INNER JOIN mutation_event ON mutation.MUTATION_EVENT_ID=mutation_event.MUTATION_EVENT_ID");
            rs = pstmt.executeQuery();
            while  (rs.next()) {
                ExtendedMutation mutation = extractMutation(rs);
                mutationList.add(mutation);
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
        return mutationList;
    }
    
    public static Set<MutationEvent> getAllMutationEvents() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Set<MutationEvent> events = new HashSet<MutationEvent>();
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            pstmt = con.prepareStatement
                    ("SELECT * FROM mutation_event");
            rs = pstmt.executeQuery();
            while  (rs.next()) {
                MutationEvent event = extractMutationEvent(rs);
                events.add(event);
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
        return events;
    }
    
    public static long getLargestMutationEventId() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            pstmt = con.prepareStatement
                    ("SELECT MAX(`MUTATION_EVENT_ID`) FROM `mutation_event`");
            rs = pstmt.executeQuery();
            return rs.next() ? rs.getLong(1) : 0;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
    }

    private static ExtendedMutation extractMutation(ResultSet rs) throws SQLException, DaoException {
        ExtendedMutation mutation = new ExtendedMutation(extractMutationEvent(rs));
        mutation.setGeneticProfileId(rs.getInt("GENETIC_PROFILE_ID"));
        mutation.setCaseId(rs.getString("CASE_ID"));
        mutation.setSequencingCenter(rs.getString("CENTER"));
        mutation.setSequencer(rs.getString("SEQUENCER"));
        mutation.setMutationStatus(rs.getString("MUTATION_STATUS"));
        mutation.setValidationStatus(rs.getString("VALIDATION_STATUS"));
        mutation.setTumorSeqAllele1(rs.getString("TUMOR_SEQ_ALLELE1"));
        mutation.setTumorSeqAllele2(rs.getString("TUMOR_SEQ_ALLELE2"));
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
        return mutation;
    }
    
    private static MutationEvent extractMutationEvent(ResultSet rs) throws SQLException, DaoException {
        MutationEvent event = new MutationEvent();
        event.setMutationEventId(rs.getLong("MUTATION_EVENT_ID"));
        long entrezId = rs.getLong("ENTREZ_GENE_ID");
        DaoGeneOptimized aDaoGene = DaoGeneOptimized.getInstance();
        CanonicalGene gene = aDaoGene.getGene(entrezId);
        event.setGene(gene);
        event.setChr(rs.getString("CHR"));
        event.setStartPosition(rs.getLong("START_POSITION"));
        event.setEndPosition(rs.getLong("END_POSITION"));
        event.setProteinChange(rs.getString("PROTEIN_CHANGE"));
        event.setMutationType(rs.getString("MUTATION_TYPE"));
        event.setFunctionalImpactScore(rs.getString("FUNCTIONAL_IMPACT_SCORE"));
        event.setFisValue(rs.getFloat("FIS_VALUE"));
        event.setLinkXVar(rs.getString("LINK_XVAR"));
        event.setLinkPdb(rs.getString("LINK_PDB"));
        event.setLinkMsa(rs.getString("LINK_MSA"));
        event.setNcbiBuild(rs.getString("NCBI_BUILD"));
        event.setStrand(rs.getString("STRAND"));
        event.setVariantType(rs.getString("VARIANT_TYPE"));
        event.setDbSnpRs(rs.getString("DB_SNP_RS"));
        event.setDbSnpValStatus(rs.getString("DB_SNP_VAL_STATUS"));
        event.setReferenceAllele(rs.getString("REFERENCE_ALLELE"));
        event.setOncotatorDbSnpRs(rs.getString("ONCOTATOR_DBSNP_RS"));
        event.setOncotatorCosmicOverlapping(rs.getString("ONCOTATOR_COSMIC_OVERLAPPING"));
        event.setOncotatorRefseqMrnaId(rs.getString("ONCOTATOR_REFSEQ_MRNA_ID"));
        event.setOncotatorCodonChange(rs.getString("ONCOTATOR_CODON_CHANGE"));
        event.setOncotatorUniprotName(rs.getString("ONCOTATOR_UNIPROT_ENTRY_NAME"));
        event.setOncotatorUniprotAccession(rs.getString("ONCOTATOR_UNIPROT_ACCESSION"));
        event.setOncotatorProteinPosStart(rs.getInt("ONCOTATOR_PROTEIN_POS_START"));
        event.setOncotatorProteinPosEnd(rs.getInt("ONCOTATOR_PROTEIN_POS_END"));
        event.setCanonicalTranscript(rs.getBoolean("CANONICAL_TRANSCRIPT"));
        return event;
    }

    public static int getCount() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
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
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
    }

    protected static String boolToStr(boolean value)
    {
        return value ? "1" : "0";
    }

    public static void deleteAllRecordsInGeneticProfile(long geneticProfileId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            pstmt = con.prepareStatement("DELETE from mutation WHERE GENETIC_PROFILE_ID=?");
            pstmt.setLong(1, geneticProfileId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
    }

    public static void deleteAllRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            pstmt = con.prepareStatement("TRUNCATE TABLE mutation; TRUNCATE TABLE mutation_event;");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
    }
}
