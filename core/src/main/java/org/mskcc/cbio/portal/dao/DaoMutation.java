/*
 * Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
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

import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.model.ExtendedMutation.*;
import org.mskcc.cbio.portal.util.MutationKeywordUtils;

import org.apache.commons.lang.StringUtils;

import java.sql.*;
import java.util.*;

/**
 * Data access object for Mutation table
 */
public final class DaoMutation {
    public static final String NAN = "NaN";

    public static int addMutation(ExtendedMutation mutation, boolean newMutationEvent) throws DaoException {
        if (!MySQLbulkLoader.isBulkLoad()) {
            throw new DaoException("You have to turn on MySQLbulkLoader in order to insert mutations");
        }
        else {
            MySQLbulkLoader.getMySQLbulkLoader("mutation").insertRecord(
                Long.toString(mutation.getMutationEventId()),
                Integer.toString(mutation.getGeneticProfileId()),
                Integer.toString(mutation.getSampleId()),
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
                return addMutationEvent(mutation.getEvent())+1;
            }
            else {
                return 1;
            }
        }
    }
        
        public static int addMutationEvent(ExtendedMutation.MutationEvent event) throws DaoException {
            // use this code if bulk loading
            // write to the temp file maintained by the MySQLbulkLoader
            String keyword = MutationKeywordUtils.guessOncotatorMutationKeyword(event.getProteinChange(), event.getMutationType());
            MySQLbulkLoader.getMySQLbulkLoader("mutation_event").insertRecord(
                    Long.toString(event.getMutationEventId()),
                    Long.toString(event.getGene().getEntrezGeneId()),
                    event.getChr(),
                    Long.toString(event.getStartPosition()),
                    Long.toString(event.getEndPosition()),
                    event.getReferenceAllele(),
                    event.getTumorSeqAllele(),
                    event.getProteinChange(),
                    event.getMutationType(),
                    event.getFunctionalImpactScore(),
                    Float.toString(event.getFisValue()),
                    event.getLinkXVar(),
                    event.getLinkPdb(),
                    event.getLinkMsa(),
                    event.getNcbiBuild(),
                    event.getStrand(),
                    event.getVariantType(),
                    event.getDbSnpRs(),
                    event.getDbSnpValStatus(),
                    event.getOncotatorDbSnpRs(),
                    event.getOncotatorRefseqMrnaId(),
                    event.getOncotatorCodonChange(),
                    event.getOncotatorUniprotName(),
                    event.getOncotatorUniprotAccession(),
                    Integer.toString(event.getOncotatorProteinPosStart()),
                    Integer.toString(event.getOncotatorProteinPosEnd()),
                    boolToStr(event.isCanonicalTranscript()),
                    keyword==null ? "\\N":(event.getGene().getHugoGeneSymbolAllCaps()+" "+keyword));
            return 1;
    }
        
    public static int calculateMutationCount (int profileId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            pstmt = con.prepareStatement
                    ("INSERT INTO mutation_count " +
                    "SELECT genetic_profile.`GENETIC_PROFILE_ID` , `SAMPLE_ID` , COUNT( * )  AS MUTATION_COUNT " +
                    "FROM `mutation` , `genetic_profile` " +
                    "WHERE mutation.`GENETIC_PROFILE_ID` = genetic_profile.`GENETIC_PROFILE_ID` " +
                    "AND genetic_profile.`GENETIC_PROFILE_ID`=? " +
                    "GROUP BY genetic_profile.`GENETIC_PROFILE_ID` , `SAMPLE_ID`;");
            pstmt.setInt(1, profileId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
    }

    public static ArrayList<ExtendedMutation> getMutations (int geneticProfileId, Collection<Integer> targetSampleList,
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
                    + "WHERE SAMPLE_ID IN ('"
                    + org.apache.commons.lang.StringUtils.join(targetSampleList, "','")+
                     "') AND GENETIC_PROFILE_ID = ? AND mutation.ENTREZ_GENE_ID = ?");
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

    public static ArrayList<ExtendedMutation> getMutations (int geneticProfileId, int sampleId,
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
                    + "WHERE SAMPLE_ID = ? AND GENETIC_PROFILE_ID = ? AND mutation.ENTREZ_GENE_ID = ?");
            pstmt.setInt(1, sampleId);
            pstmt.setInt(2, geneticProfileId);
            pstmt.setLong(3, entrezGeneId);
            rs = pstmt.executeQuery();
            while  (rs.next()) {
                ExtendedMutation mutation = extractMutation(rs);
                mutationList.add(mutation);
            }
        } catch (NullPointerException e) {
            throw new DaoException(e);
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
                        + "WHERE mutation.ENTREZ_GENE_ID = ?");
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
                        + " INNER JOIN mutation ON mutation.MUTATION_EVENT_ID=mutation_event.MUTATION_EVENT_ID "
                        + " WHERE mutation.ENTREZ_GENE_ID = ? AND PROTEIN_CHANGE = ?");
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
        
        public static ArrayList<ExtendedMutation> getMutations (int geneticProfileId, int sampleId) throws DaoException {
            return getMutations(geneticProfileId, Arrays.asList(new Integer(sampleId)));
        }
    
        public static ArrayList<ExtendedMutation> getMutations (int geneticProfileId, List<Integer> sampleIds) throws DaoException {
            Connection con = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            ArrayList <ExtendedMutation> mutationList = new ArrayList <ExtendedMutation>();
            try {
                con = JdbcUtil.getDbConnection(DaoMutation.class);
                pstmt = con.prepareStatement
                        ("SELECT * FROM mutation "
                        + "INNER JOIN mutation_event ON mutation.MUTATION_EVENT_ID=mutation_event.MUTATION_EVENT_ID "
                        + "WHERE GENETIC_PROFILE_ID = ? AND SAMPLE_ID in ('"+ StringUtils.join(sampleIds, "','")+"')");
                pstmt.setInt(1, geneticProfileId);
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
    
        public static boolean hasAlleleFrequencyData (int geneticProfileId, int sampleId) throws DaoException {
            Connection con = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            try {
                con = JdbcUtil.getDbConnection(DaoMutation.class);
                pstmt = con.prepareStatement
                        ("SELECT EXISTS (SELECT 1 FROM mutation "
                        + "WHERE GENETIC_PROFILE_ID = ? AND SAMPLE_ID = ? AND TUMOR_ALT_COUNT>=0 AND TUMOR_REF_COUNT>=0)");
                pstmt.setInt(1, geneticProfileId);
                pstmt.setInt(2, sampleId);
                rs = pstmt.executeQuery();
                return rs.next() && rs.getInt(1)==1;
            } catch (NullPointerException e) {
                throw new DaoException(e);
            } catch (SQLException e) {
                throw new DaoException(e);
            } finally {
                JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
            }
        }

        public static ArrayList<ExtendedMutation> getSimilarMutations (long entrezGeneId, String aminoAcidChange, int excludeSampleId) throws DaoException {
            Connection con = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            ArrayList <ExtendedMutation> mutationList = new ArrayList <ExtendedMutation>();
            try {
                con = JdbcUtil.getDbConnection(DaoMutation.class);
                pstmt = con.prepareStatement
                        ("SELECT * FROM mutation, mutation_event "
                        + "WHERE mutation.MUTATION_EVENT_ID=mutation_event.MUTATION_EVENT_ID "
                        + "AND mutation.ENTREZ_GENE_ID = ? AND PROTEIN_CHANGE = ? AND SAMPLE_ID <> ?");
                pstmt.setLong(1, entrezGeneId);
                pstmt.setString(2, aminoAcidChange);
                pstmt.setInt(3, excludeSampleId);
                rs = pstmt.executeQuery();
                while  (rs.next()) {
                    ExtendedMutation mutation = extractMutation(rs);
                    mutationList.add(mutation);
                }
            } catch (NullPointerException e) {
                throw new DaoException(e);
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
                        + "WHERE GENETIC_PROFILE_ID = ? AND mutation.ENTREZ_GENE_ID = ?");
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
                    ("SELECT * FROM mutation "
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
    
    public static Set<ExtendedMutation.MutationEvent> getAllMutationEvents() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Set<ExtendedMutation.MutationEvent> events = new HashSet<ExtendedMutation.MutationEvent>();
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            pstmt = con.prepareStatement
                    ("SELECT * FROM mutation_event");
            rs = pstmt.executeQuery();
            while  (rs.next()) {
                ExtendedMutation.MutationEvent event = extractMutationEvent(rs);
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
        try {
            ExtendedMutation mutation = new ExtendedMutation(extractMutationEvent(rs));
            mutation.setGeneticProfileId(rs.getInt("GENETIC_PROFILE_ID"));
            mutation.setSampleId(rs.getInt("SAMPLE_ID"));
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
        catch(NullPointerException e) {
            throw new DaoException(e);
        }
    }
    
    private static ExtendedMutation.MutationEvent extractMutationEvent(ResultSet rs) throws SQLException, DaoException {
        ExtendedMutation.MutationEvent event = new ExtendedMutation.MutationEvent();
        event.setMutationEventId(rs.getLong("MUTATION_EVENT_ID"));
        long entrezId = rs.getLong("mutation_event.ENTREZ_GENE_ID");
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
        event.setOncotatorRefseqMrnaId(rs.getString("ONCOTATOR_REFSEQ_MRNA_ID"));
        event.setOncotatorCodonChange(rs.getString("ONCOTATOR_CODON_CHANGE"));
        event.setOncotatorUniprotName(rs.getString("ONCOTATOR_UNIPROT_ENTRY_NAME"));
        event.setOncotatorUniprotAccession(rs.getString("ONCOTATOR_UNIPROT_ACCESSION"));
        event.setOncotatorProteinPosStart(rs.getInt("ONCOTATOR_PROTEIN_POS_START"));
        event.setOncotatorProteinPosEnd(rs.getInt("ONCOTATOR_PROTEIN_POS_END"));
        event.setCanonicalTranscript(rs.getBoolean("CANONICAL_TRANSCRIPT"));
        event.setTumorSeqAllele(rs.getString("TUMOR_SEQ_ALLELE"));
        event.setKeyword(rs.getString("KEYWORD"));
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

    /**
     * Get significantly mutated genes
     * @param entrezGeneIds
     * @return
     * @throws DaoException 
     */
    public static Map<Long, Integer> getSMGs(int profileId, Collection<Long> entrezGeneIds,
            int thresholdRecurrence, int thresholdNumGenes,
            Collection<String> selectedCaseIds) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            String sql = "SELECT mutation.ENTREZ_GENE_ID, COUNT(*), COUNT(*)/`LENGTH` AS count_per_nt"
                    + " FROM mutation, gene"
                    + " WHERE mutation.ENTREZ_GENE_ID=gene.ENTREZ_GENE_ID"
                    + " AND GENETIC_PROFILE_ID=" + profileId
                    + (entrezGeneIds==null?"":(" AND mutation.ENTREZ_GENE_ID IN("+StringUtils.join(entrezGeneIds,",")+")"))
                    + (selectedCaseIds==null?"":(" AND mutation.SAMPLE_ID IN("+StringUtils.join(selectedCaseIds,",")+")"))
                    + " GROUP BY mutation.ENTREZ_GENE_ID"
                    + (thresholdRecurrence>0?(" HAVING COUNT(*)>="+thresholdRecurrence):"")
                    + " ORDER BY count_per_nt DESC"
                    + (thresholdNumGenes>0?(" LIMIT 0,"+thresholdNumGenes):"");
            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();
            Map<Long, Integer> map = new HashMap<Long, Integer>();
            while (rs.next()) {
                map.put(rs.getLong(1), rs.getInt(2));
            }
            return map;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
    }
    
    /**
     * return the number of all mutations for a profile
     * @param profileId
     * @return Map &lt; case id, mutation count &gt;
     * @throws DaoException 
     */
    public static int countMutationEvents(int profileId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            String sql = "SELECT count(DISTINCT `SAMPLE_ID`, `MUTATION_EVENT_ID`) FROM mutation"
                        + " WHERE `GENETIC_PROFILE_ID`=" + profileId;
            pstmt = con.prepareStatement(sql);
            
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
    
    
    /**
     * return the number of mutations for each sample
     * @param sampleIds if null, return all case available
     * @param profileId
     * @return Map &lt; sample id, mutation count &gt;
     * @throws DaoException 
     */
    public static Map<Integer, Integer> countMutationEvents(int profileId, Collection<Integer> sampleIds) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            String sql;
            if (sampleIds==null) {
                sql = "SELECT `SAMPLE_ID`, `MUTATION_COUNT` FROM mutation_count"
                        + " WHERE `GENETIC_PROFILE_ID`=" + profileId;
                
            } else {
                sql = "SELECT `SAMPLE_ID`, `MUTATION_COUNT` FROM mutation_count"
                        + " WHERE `GENETIC_PROFILE_ID`=" + profileId
                        + " AND `SAMPLE_ID` IN ('"
                        + StringUtils.join(sampleIds,"','")
                        + "')";
            }
            pstmt = con.prepareStatement(sql);
            
            Map<Integer, Integer> map = new HashMap<Integer, Integer>();
            rs = pstmt.executeQuery();
            while (rs.next()) {
                map.put(rs.getInt(1), rs.getInt(2));
            }
            return map;
        } catch (NullPointerException e) {
            throw new DaoException(e);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
    }
    
    /**
     * get events for each sample
     * @return Map &lt; sample id, list of event ids &gt;
     * @throws DaoException 
     */
    public static Map<Integer, Set<Long>> getSamplesWithMutations(Collection<Long> eventIds) throws DaoException {
        return getSamplesWithMutations(StringUtils.join(eventIds, ","));
    }
    
    /**
     * get events for each sample
     * @param concatEventIds event ids concatenated by comma (,)
     * @return Map &lt; sample id, list of event ids &gt;
     * @throws DaoException 
     */
    public static Map<Integer, Set<Long>> getSamplesWithMutations(String concatEventIds) throws DaoException {
        if (concatEventIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            String sql = "SELECT `SAMPLE_ID`, `MUTATION_EVENT_ID` FROM mutation"
                    + " WHERE `MUTATION_EVENT_ID` IN ("
                    + concatEventIds + ")";
            pstmt = con.prepareStatement(sql);
            
            Map<Integer, Set<Long>>  map = new HashMap<Integer, Set<Long>> ();
            rs = pstmt.executeQuery();
            while (rs.next()) {
                int sampleId = rs.getInt("SAMPLE_ID");
                long eventId = rs.getLong("MUTATION_EVENT_ID");
                Set<Long> events = map.get(sampleId);
                if (events == null) {
                    events = new HashSet<Long>();
                    map.put(sampleId, events);
                }
                events.add(eventId);
            }
            return map;
        } catch (NullPointerException e) {
            throw new DaoException(e);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
    }
    
    /**
     * @return Map &lt; sample, list of event ids &gt;
     * @throws DaoException 
     */
    public static Map<Sample, Set<Long>> getSimilarSamplesWithMutationsByKeywords(
            Collection<Long> eventIds) throws DaoException {
        return getSimilarSamplesWithMutationsByKeywords(StringUtils.join(eventIds, ","));
    }
    
    
    /**
     * @param concatEventIds event ids concatenated by comma (,)
     * @return Map &lt; sample, list of event ids &gt;
     * @throws DaoException 
     */
    public static Map<Sample, Set<Long>> getSimilarSamplesWithMutationsByKeywords(
            String concatEventIds) throws DaoException {
        if (concatEventIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            String sql = "SELECT `SAMPLE_ID`, `GENETIC_PROFILE_ID`, me1.`MUTATION_EVENT_ID`"
                    + " FROM mutation cme, mutation_event me1, mutation_event me2"
                    + " WHERE me1.`MUTATION_EVENT_ID` IN ("+ concatEventIds + ")"
                    + " AND me1.`KEYWORD`=me2.`KEYWORD`"
                    + " AND cme.`MUTATION_EVENT_ID`=me2.`MUTATION_EVENT_ID`";
            pstmt = con.prepareStatement(sql);
            
            Map<Sample, Set<Long>>  map = new HashMap<Sample, Set<Long>> ();
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Sample sample = DaoSample.getSampleById(rs.getInt("SAMPLE_ID"));
                long eventId = rs.getLong("MUTATION_EVENT_ID");
                Set<Long> events = map.get(sample);
                if (events == null) {
                    events = new HashSet<Long>();
                    map.put(sample, events);
                }
                events.add(eventId);
            }
            return map;
        } catch (NullPointerException e) {
            throw new DaoException(e);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
    }
    
    /**
     * @param entrezGeneIds event ids concatenated by comma (,)
     * @return Map &lt; sample, list of event ids &gt;
     * @throws DaoException 
     */
    public static Map<Sample, Set<Long>> getSimilarSamplesWithMutatedGenes(
            Collection<Long> entrezGeneIds) throws DaoException {
        if (entrezGeneIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            String sql = "SELECT `SAMPLE_ID`, `GENETIC_PROFILE_ID`, `ENTREZ_GENE_ID`"
                    + " FROM mutation"
                    + " WHERE `ENTREZ_GENE_ID` IN ("+ StringUtils.join(entrezGeneIds,",") + ")";
            pstmt = con.prepareStatement(sql);
           
            Map<Sample, Set<Long>>  map = new HashMap<Sample, Set<Long>> ();
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Sample sample = DaoSample.getSampleById(rs.getInt("SAMPLE_ID"));
                long entrez = rs.getLong("ENTREZ_GENE_ID");
                Set<Long> genes = map.get(sample);
                if (genes == null) {
                    genes = new HashSet<Long>();
                    map.put(sample, genes);
                }
                genes.add(entrez);
            }
            return map;
        } catch (NullPointerException e) {
            throw new DaoException(e);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
    }
    
    public static Map<Long, Integer> countSamplesWithMutationEvents(Collection<Long> eventIds, int profileId) throws DaoException {
        return countSamplesWithMutationEvents(StringUtils.join(eventIds, ","), profileId);
    }
    
    /**
     * return the number of samples for each mutation event
     * @param concatEventIds
     * @param profileId
     * @return Map &lt; event id, sampleCount &gt;
     * @throws DaoException 
     */
    public static Map<Long, Integer> countSamplesWithMutationEvents(String concatEventIds, int profileId) throws DaoException {
        if (concatEventIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            String sql = "SELECT `MUTATION_EVENT_ID`, count(DISTINCT `SAMPLE_ID`) FROM mutation"
                    + " WHERE `GENETIC_PROFILE_ID`=" + profileId
                    + " AND `MUTATION_EVENT_ID` IN ("
                    + concatEventIds
                    + ") GROUP BY `MUTATION_EVENT_ID`";
            pstmt = con.prepareStatement(sql);
            
            Map<Long, Integer> map = new HashMap<Long, Integer>();
            rs = pstmt.executeQuery();
            while (rs.next()) {
                map.put(rs.getLong(1), rs.getInt(2));
            }
            return map;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
    }
    
    public static Map<Long, Integer> countSamplesWithMutatedGenes(Collection<Long> entrezGeneIds, int profileId) throws DaoException {
        return countSamplesWithMutatedGenes(StringUtils.join(entrezGeneIds, ","), profileId);
    }
    
    /**
     * return the number of samples for each mutated genes
     * @param concatEntrezGeneIds
     * @param profileId
     * @return Map &lt; entrez, sampleCount &gt;
     * @throws DaoException 
     */
        public static Map<Long, Integer> countSamplesWithMutatedGenes(String concatEntrezGeneIds, int profileId) throws DaoException {
        if (concatEntrezGeneIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            String sql = "SELECT ENTREZ_GENE_ID, count(DISTINCT SAMPLE_ID)"
                    + " FROM mutation"
                    + " WHERE GENETIC_PROFILE_ID=" + profileId
                    + " AND ENTREZ_GENE_ID IN ("
                    + concatEntrezGeneIds
                    + ") GROUP BY `ENTREZ_GENE_ID`";
            pstmt = con.prepareStatement(sql);
            
            Map<Long, Integer> map = new HashMap<Long, Integer>();
            rs = pstmt.executeQuery();
            while (rs.next()) {
                map.put(rs.getLong(1), rs.getInt(2));
            }
            return map;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
    }

    public static Map<String, Integer> countSamplesWithKeywords(Collection<String> keywords, int profileId) throws DaoException {
        if (keywords.isEmpty()) {
            return Collections.emptyMap();
        }
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            String sql = "SELECT KEYWORD, count(DISTINCT SAMPLE_ID)"
                    + " FROM mutation, mutation_event"
                    + " WHERE GENETIC_PROFILE_ID=" + profileId
                    + " AND mutation.MUTATION_EVENT_ID=mutation_event.MUTATION_EVENT_ID"
                    + " AND KEYWORD IN ('"
                    + StringUtils.join(keywords,"','")
                    + "') GROUP BY `KEYWORD`";
            pstmt = con.prepareStatement(sql);

            Map<String, Integer> map = new HashMap<String, Integer>();
            rs = pstmt.executeQuery();
            while (rs.next()) {
                map.put(rs.getString(1), rs.getInt(2));
            }
            return map;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
    }

    /**
     *
     * Counts all the samples in each cancer study in the collection of geneticProfileIds by mutation keyword
     *
     * @param keywords
     * @param internalProfileIds
     * @return Collection of Maps {"keyword" , "hugo" , "cancer_study" , "count"} where cancer_study == cancerStudy.getName();
     * @throws DaoException
     * @author Gideon Dresdner <dresdnerg@cbio.mskcc.org>
     */
    public static Collection<Map<String, Object>> countSamplesWithKeywords(Collection<String> keywords, Collection<Integer> internalProfileIds) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);

            String sql = "SELECT KEYWORD, GENETIC_PROFILE_ID, mutation.ENTREZ_GENE_ID, count(DISTINCT SAMPLE_ID) FROM mutation, mutation_event " +
                    "WHERE GENETIC_PROFILE_ID IN (" + StringUtils.join(internalProfileIds, ",") + ") " +
                    "AND mutation.MUTATION_EVENT_ID=mutation_event.MUTATION_EVENT_ID " +
                    "AND KEYWORD IN ('" + StringUtils.join(keywords, "','") + "') " +
                    "GROUP BY KEYWORD, GENETIC_PROFILE_ID";

            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();

            Collection<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
            while (rs.next()) {

                Map<String, Object> d = new HashMap<String, Object>();

                String keyword = rs.getString(1);
                Integer geneticProfileId = rs.getInt(2);
                Long entrez = rs.getLong(3);
                Integer count = rs.getInt(4);

                // this is computing a join and in not optimal
                GeneticProfile geneticProfile = DaoGeneticProfile.getGeneticProfileById(geneticProfileId);
                Integer cancerStudyId = geneticProfile.getCancerStudyId();
                CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByInternalId(cancerStudyId);
                String name = cancerStudy.getName();
                String cancerType = cancerStudy.getTypeOfCancerId();

                CanonicalGene gene = DaoGeneOptimized.getInstance().getGene(entrez);
                String hugo = gene.getHugoGeneSymbolAllCaps();

                d.put("keyword", keyword);
                d.put("hugo", hugo);
                d.put("cancer_study", name);
                d.put("cancer_type", cancerType);
                d.put("count", count);

                data.add(d);
            }

            return data;

        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
    }

    /**
     *
     * Counts up all the samples that have any mutation in a gene by genomic profile (ids)
     *
     * @param hugos
     * @param internalProfileIds
     * @return Collection of Maps {"hugo" , "cancer_study" , "count"} where cancer_study == cancerStudy.getName();
     * and gene is the hugo gene symbol.
     *
     * @throws DaoException
     * @author Gideon Dresdner <dresdnerg@cbio.mskcc.org>
     */
    public static Collection<Map<String, Object>> countSamplesWithGenes(Collection<String> hugos, Collection<Integer> internalProfileIds) throws DaoException {

        // convert hugos to entrezs
        // and simultaneously construct a map to turn them back into hugo gene symbols later
        List<Long> entrezs = new ArrayList<Long>();
        Map<Long, CanonicalGene> entrez2CanonicalGene = new HashMap<Long, CanonicalGene>();
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
        for (String hugo : hugos) {
            CanonicalGene canonicalGene = daoGeneOptimized.getGene(hugo);
            Long entrez = canonicalGene.getEntrezGeneId();

            entrezs.add(entrez);
            entrez2CanonicalGene.put(entrez, canonicalGene);
        }

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);

            String sql = "select mutation.ENTREZ_GENE_ID, mutation.GENETIC_PROFILE_ID, count(distinct SAMPLE_ID) from mutation, mutation_event\n" +
                    "where GENETIC_PROFILE_ID in (" + StringUtils.join(internalProfileIds, ",") + ")\n" +
                    "and mutation.ENTREZ_GENE_ID in (" + StringUtils.join(entrezs, ",")  + ")\n" +
                    "and mutation.MUTATION_EVENT_ID=mutation_event.MUTATION_EVENT_ID\n" +
                    "group by ENTREZ_GENE_ID, GENETIC_PROFILE_ID";

            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();

            Collection<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
            while (rs.next()) {

                Map<String, Object> d = new HashMap<String, Object>();

                Long entrez = rs.getLong(1);
                Integer geneticProfileId = rs.getInt(2);
                Integer count = rs.getInt(3);

                // can you do the boogie woogie to get a cancerStudy's name?
                // this is computing a join and in not optimal
                GeneticProfile geneticProfile = DaoGeneticProfile.getGeneticProfileById(geneticProfileId);
                Integer cancerStudyId = geneticProfile.getCancerStudyId();
                CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByInternalId(cancerStudyId);
                String name = cancerStudy.getName();
                String cancerType = cancerStudy.getTypeOfCancerId();

                CanonicalGene canonicalGene = entrez2CanonicalGene.get(entrez);
                String hugo = canonicalGene.getHugoGeneSymbolAllCaps();

                d.put("hugo", hugo);
                d.put("cancer_study", name);
                d.put("cancer_type", cancerType);
                d.put("count", count);

                data.add(d);
            }

            return data;

        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
    }
    
    public static Set<Long> getGenesOfMutations(
            Collection<Long> eventIds, int profileId) throws DaoException {
        return getGenesOfMutations(StringUtils.join(eventIds, ","), profileId);
    }
    
    /**
     * return entrez gene ids of the mutations specified by their mutaiton event ids.
     * @param concatEventIds
     * @param profileId
     * @return
     * @throws DaoException 
     */
    public static Set<Long> getGenesOfMutations(String concatEventIds, int profileId)
            throws DaoException {
        if (concatEventIds.isEmpty()) {
            return Collections.emptySet();
        }
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            String sql = "SELECT DISTINCT ENTREZ_GENE_ID FROM mutation_event "
                    + "WHERE MUTATION_EVENT_ID in ("
                    +       concatEventIds
                    + ")";
            pstmt = con.prepareStatement(sql);
            
            Set<Long> set = new HashSet<Long>();
            rs = pstmt.executeQuery();
            while (rs.next()) {
                set.add(rs.getLong(1));
            }
            return set;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
    }
    
    /**
     * return keywords of the mutations specified by their mutaiton event ids.
     * @param concatEventIds
     * @param profileId
     * @return
     * @throws DaoException 
     */
    public static Set<String> getKeywordsOfMutations(String concatEventIds, int profileId)
            throws DaoException {
        if (concatEventIds.isEmpty()) {
            return Collections.emptySet();
        }
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            String sql = "SELECT DISTINCT KEYWORD FROM mutation_event "
                    + "WHERE MUTATION_EVENT_ID in ("
                    +       concatEventIds
                    + ")";
            pstmt = con.prepareStatement(sql);
            
            Set<String> set = new HashSet<String>();
            rs = pstmt.executeQuery();
            while (rs.next()) {
                set.add(rs.getString(1));
            }
            return set;
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
            pstmt = con.prepareStatement("TRUNCATE TABLE mutation");
            pstmt.executeUpdate();
            pstmt = con.prepareStatement("TRUNCATE TABLE mutation_event");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
    }
}
