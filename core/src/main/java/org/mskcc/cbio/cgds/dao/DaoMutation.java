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

import org.mskcc.cbio.cgds.model.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.mskcc.cbio.cgds.model.ExtendedMutation.MutationEvent;

/**
 * Data access object for Mutation table
 */
public final class DaoMutation {
    public static final String NAN = "NaN";

    public static int addMutation(ExtendedMutation mutation, boolean newMutationEvent) throws DaoException {
            if (!MySQLbulkLoader.isBulkLoad()) {
                throw new DaoException("You have to turn on MySQLbulkLoader in order to insert mutations");
            } else {

                    // use this code if bulk loading
                    // write to the temp file maintained by the MySQLbulkLoader
                    MySQLbulkLoader.getMySQLbulkLoader("mutation").insertRecord(
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
                        return addMutationEvent(mutation.getEvent())+1;
                    } else {
                        return 1;
                    }
            }
    }
        
        public static int addMutationEvent(MutationEvent event) throws DaoException {
            // use this code if bulk loading
            // write to the temp file maintained by the MySQLbulkLoader
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
                    filterCosmic(event),
                    event.getOncotatorRefseqMrnaId(),
                    event.getOncotatorCodonChange(),
                    event.getOncotatorUniprotName(),
                    event.getOncotatorUniprotAccession(),
                    Integer.toString(event.getOncotatorProteinPosStart()),
                    Integer.toString(event.getOncotatorProteinPosEnd()),
                    boolToStr(event.isCanonicalTranscript()),
                    extractMutationKeyword(event));
            // add cosmic
            for (CosmicMutationFrequency cosmic :
                    parseCosmic(event)) {
                importCosmic(event.getMutationEventId(), cosmic);
            }
            // return 1 because normal insert will return 1 if no error occurs
            return 1;
    }
        
    private static String extractMutationKeyword(MutationEvent event) {
        String hugo = event.getGene().getHugoGeneSymbolAllCaps();
        String type = event.getMutationType();
        if (type.equals("Nonsense_Mutation") ||
            type.equals("Splice_Site") || 
            type.startsWith("Frame_Shift_") || 
            type.equals("Nonstop_Mutation")) {
            return hugo + " truncating";
        }
        
        if (type.equals("Missense_Mutation")) {
            String aa = event.getProteinChange();
            if (aa.startsWith("M1")&&!aa.equals("M1M")) { // how about indels on the first position?
                // non-start
                return hugo + " truncating";
            }
            
            Pattern p = Pattern.compile("([A-Z][0-9]+)");
            Matcher m = p.matcher(aa);
            if (m.find()) {
                return hugo + " " + m.group(1) + " missense";
            }
        }
        
        if (type.equals("In_Frame_Ins")) {
            String aa = event.getProteinChange();
            if (aa.contains("*")) { // insert *
                return hugo + " truncating";
            }
            
            Pattern p = Pattern.compile("([0-9]+)");
            Matcher m = p.matcher(aa);
            if (m.find()) {
               return hugo + " " + m.group(1) + " ins";
            }
        }
        
        if (type.equals("In_Frame_Del")) {
            String aa = event.getProteinChange();
            // only the first deleted residue was considered
            Pattern p = Pattern.compile("([0-9]+)");
            Matcher m = p.matcher(aa);
            if (m.find()) {
               return hugo + " " + m.group(1) + " del";
            }
        }
        
        if (type.equals("Silent")) {
            String aa = event.getProteinChange();
            Pattern p = Pattern.compile("([0-9]+)");
            Matcher m = p.matcher(aa);
            if (m.find()) {
               return hugo + " " + m.group(1) + "silent";
            }
        }
            
        // RNA or Translation_Start_Site
        return "Chm"+event.getChr()+","+event.getStartPosition();
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
                    + "WHERE CASE_ID = ? AND GENETIC_PROFILE_ID = ? AND mutation.ENTREZ_GENE_ID = ?");
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
        
        public static ArrayList<ExtendedMutation> getMutations (int geneticProfileId, String caseId) throws DaoException {
            return getMutations(geneticProfileId, new String[]{caseId});
        }
    
        public static ArrayList<ExtendedMutation> getMutations (int geneticProfileId, String[] caseIds) throws DaoException {
            Connection con = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            ArrayList <ExtendedMutation> mutationList = new ArrayList <ExtendedMutation>();
            try {
                con = JdbcUtil.getDbConnection(DaoMutation.class);
                pstmt = con.prepareStatement
                        ("SELECT * FROM mutation "
                        + "INNER JOIN mutation_event ON mutation.MUTATION_EVENT_ID=mutation_event.MUTATION_EVENT_ID "
                        + "WHERE GENETIC_PROFILE_ID = ? AND CASE_ID in ('"+StringUtils.join(caseIds, "','")+"')");
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
                        + "AND mutation.ENTREZ_GENE_ID = ? AND PROTEIN_CHANGE = ? AND CASE_ID <> ?");
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
        event.setOncotatorCosmicOverlapping(rs.getString("ONCOTATOR_COSMIC_OVERLAPPING"));
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
                    ("SELECT COUNT(DISTINCT `CASE_ID`, `MUTATION_EVENT_ID`) FROM mutation");
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
            String sql = "SELECT count(DISTINCT `CASE_ID`, `MUTATION_EVENT_ID`) FROM mutation"
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
     * return the number of mutations for each case
     * @param caseIds if null, return all case available
     * @param profileId
     * @return Map &lt; case id, mutation count &gt;
     * @throws DaoException 
     */
    public static Map<String, Integer> countMutationEvents(
            int profileId, Collection<String> caseIds) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            String sql;
            if (caseIds==null) {
                sql = "SELECT `CASE_ID`, count(DISTINCT `MUTATION_EVENT_ID`) FROM mutation"
                        + " WHERE `GENETIC_PROFILE_ID`=" + profileId
                        + " GROUP BY `CASE_ID`";
                
            } else {
                sql = "SELECT `CASE_ID`, count(DISTINCT `MUTATION_EVENT_ID`) FROM mutation"
                        + " WHERE `GENETIC_PROFILE_ID`=" + profileId
                        + " AND `CASE_ID` IN ('"
                        + StringUtils.join(caseIds,"','")
                        + "') GROUP BY `CASE_ID`";
            }
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
     * get events for each case
     * @return Map &lt; case id, list of event ids &gt;
     * @throws DaoException 
     */
    public static Map<String, Set<Long>> getCasesWithMutations(Collection<Long> eventIds) throws DaoException {
        return getCasesWithMutations(StringUtils.join(eventIds, ","));
    }
    
    /**
     * get events for each case
     * @param concatEventIds event ids concatenated by comma (,)
     * @return Map &lt; case id, list of event ids &gt;
     * @throws DaoException 
     */
    public static Map<String, Set<Long>> getCasesWithMutations(String concatEventIds) throws DaoException {
        if (concatEventIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            String sql = "SELECT `CASE_ID`, `MUTATION_EVENT_ID` FROM mutation"
                    + " WHERE `MUTATION_EVENT_ID` IN ("
                    + concatEventIds + ")";
            pstmt = con.prepareStatement(sql);
            
            Map<String, Set<Long>>  map = new HashMap<String, Set<Long>> ();
            rs = pstmt.executeQuery();
            while (rs.next()) {
                String caseId = rs.getString("CASE_ID");
                long eventId = rs.getLong("MUTATION_EVENT_ID");
                Set<Long> events = map.get(caseId);
                if (events == null) {
                    events = new HashSet<Long>();
                    map.put(caseId, events);
                }
                events.add(eventId);
            }
            return map;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
    }
    
    /**
     * @return Map &lt; case id, list of event ids &gt;
     * @throws DaoException 
     */
    public static Map<Case, Set<Long>> getSimilarCasesWithMutationsByKeywords(
            Collection<Long> eventIds) throws DaoException {
        return getSimilarCasesWithMutationsByKeywords(StringUtils.join(eventIds, ","));
    }
    
    
    /**
     * @param concatEventIds event ids concatenated by comma (,)
     * @return Map &lt; case id, list of event ids &gt;
     * @throws DaoException 
     */
    public static Map<Case, Set<Long>> getSimilarCasesWithMutationsByKeywords(
            String concatEventIds) throws DaoException {
        if (concatEventIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            String sql = "SELECT `CASE_ID`, `GENETIC_PROFILE_ID`, me1.`MUTATION_EVENT_ID`"
                    + " FROM mutation cme, mutation_event me1, mutation_event me2"
                    + " WHERE me1.`MUTATION_EVENT_ID` IN ("+ concatEventIds + ")"
                    + " AND me1.`KEYWORD`=me2.`KEYWORD`"
                    + " AND cme.`MUTATION_EVENT_ID`=me2.`MUTATION_EVENT_ID`";
            pstmt = con.prepareStatement(sql);
            
            Map<Case, Set<Long>>  map = new HashMap<Case, Set<Long>> ();
            rs = pstmt.executeQuery();
            while (rs.next()) {
                String caseId = rs.getString("CASE_ID");
                int cancerStudyId = DaoGeneticProfile.getGeneticProfileById(
                        rs.getInt("GENETIC_PROFILE_ID")).getCancerStudyId();
                Case _case = new Case(caseId, cancerStudyId);
                long eventId = rs.getLong("MUTATION_EVENT_ID");
                Set<Long> events = map.get(_case);
                if (events == null) {
                    events = new HashSet<Long>();
                    map.put(_case, events);
                }
                events.add(eventId);
            }
            return map;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
    }
    
    
    /**
     * @param entrezGeneIds event ids concatenated by comma (,)
     * @return Map &lt; case id, list of event ids &gt;
     * @throws DaoException 
     */
    public static Map<Case, Set<Long>> getSimilarCasesWithMutatedGenes(
            Collection<Long> entrezGeneIds) throws DaoException {
        if (entrezGeneIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            String sql = "SELECT `CASE_ID`, `GENETIC_PROFILE_ID`, `ENTREZ_GENE_ID`"
                    + " FROM mutation"
                    + " WHERE `ENTREZ_GENE_ID` IN ("+ StringUtils.join(entrezGeneIds,",") + ")";
            pstmt = con.prepareStatement(sql);
            
            Map<Case, Set<Long>>  map = new HashMap<Case, Set<Long>> ();
            rs = pstmt.executeQuery();
            while (rs.next()) {
                String caseId = rs.getString("CASE_ID");
                int cancerStudyId = DaoGeneticProfile.getGeneticProfileById(
                        rs.getInt("GENETIC_PROFILE_ID")).getCancerStudyId();
                Case _case = new Case(caseId, cancerStudyId);
                long entrez = rs.getLong("ENTREZ_GENE_ID");
                Set<Long> genes = map.get(_case);
                if (genes == null) {
                    genes = new HashSet<Long>();
                    map.put(_case, genes);
                }
                genes.add(entrez);
            }
            return map;
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
            String sql = "SELECT `MUTATION_EVENT_ID`, count(DISTINCT `CASE_ID`) FROM mutation"
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
            String sql = "SELECT ENTREZ_GENE_ID, count(DISTINCT CASE_ID)"
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
            String sql = "SELECT KEYWORD, count(DISTINCT CASE_ID)"
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
     * @return Collection of Maps {"keyword" , "geneticProfileIds" , "count"}
     * @throws DaoException
     * @author Gideon Dresdner <dresdnerg@cbio.mskcc.org>
     */
    public static Collection<Map<String, Object>> countSamplesWithKeywords(Collection<String> keywords, Collection<Integer> internalProfileIds) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);

            String sql = "SELECT KEYWORD, GENETIC_PROFILE_ID, count(DISTINCT CASE_ID) FROM mutation, mutation_event " +
                    "WHERE GENETIC_PROFILE_ID IN (" + StringUtils.join(internalProfileIds, ",") + ") " +
                    "AND mutation.MUTATION_EVENT_ID=mutation_event.MUTATION_EVENT_ID " +
                    "AND KEYWORD IN ('" + StringUtils.join(keywords, "','") + "') " +
                    "GROUP BY KEYWORD, GENETIC_PROFILE_ID";

            System.out.println(sql);

            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();

            Collection<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
            while (rs.next()) {

                Map<String, Object> d = new HashMap<String, Object>();

                String keyword = rs.getString(1);
                Integer geneticProfileId = rs.getInt(2);
                Integer count = rs.getInt(3);

                d.put("keyword", keyword);
                d.put("geneticProfileId", geneticProfileId);
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

    public static Set<Long> getMutatedGenesForACase(String caseId, int profileId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            String sql = "SELECT DISTINCT ENTREZ_GENE_ID"
                    + " FROM mutation"
                    + " AND CASE_ID='" + caseId + "'";
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
            pstmt = con.prepareStatement("TRUNCATE TABLE mutation_event_cosmic_mapping");
            pstmt.executeUpdate();
            pstmt = con.prepareStatement("TRUNCATE TABLE cosmic_mutation");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
    }
    
    
    // the follwing methods deal with cosmic data in oncocator report
    // TODO: need to be refactored using latest cosmic data.
    static String filterCosmic(MutationEvent event) {
        List<CosmicMutationFrequency> cmfs = parseCosmic(event);
        StringBuilder sb = new StringBuilder();
        for (CosmicMutationFrequency cmf : cmfs) {
            sb.append(cmf.getAminoAcidChange()).append("(")
                    .append(cmf.getFrequency()).append(")|");
        }
        if (sb.length()>0) {
            sb.deleteCharAt(sb.length()-1);
        }
        return sb.toString();
    }
    
    private static List<CosmicMutationFrequency> parseCosmic(MutationEvent event) {
        String strCosmic = event.getOncotatorCosmicOverlapping();
        if (strCosmic==null || strCosmic.isEmpty()) {
            return Collections.emptyList();
        }
        
        String[] parts = strCosmic.split("\\|");
        List<CosmicMutationFrequency> list = new ArrayList<CosmicMutationFrequency>(parts.length);
        Pattern p = Pattern.compile("(p\\..+)\\(([0-9]+)\\)");
        for (String part : parts) {
            Matcher m = p.matcher(part);
            if (m.matches()) {
                String aa = m.group(1);
                if (matchCosmic(event, aa)) {
                    int count = Integer.parseInt(m.group(2));
                    list.add(new CosmicMutationFrequency(event.getGene().getEntrezGeneId(), aa, count));
                }
            } 
//            else if (!part.equals("NA")) {
//                System.err.println("wrong cosmic string: "+part);
//            }
        }
        
        return list;
    }
    
    private static boolean matchCosmic(MutationEvent event, String cosmicAAChange) {
        if (cosmicAAChange.endsWith("p.?")||cosmicAAChange.endsWith("p.0?")) {
            return false;
        }
        
        String type = event.getMutationType();
        if (cosmicAAChange.matches(
                "(p\\.[A-Z]?[0-9]+_[A-Z]?[0-9]+((>)|(ins))[A-Z]+)|(p\\.[A-Z][0-9]+>[A-Z][A-Z]+)|(p\\.[A-Z]?[0-9]+.+del[A-Z]*)")) {
            // in frame del or ins
            return type.toLowerCase().startsWith("in_frame_");
        }
        
        if (cosmicAAChange.matches("p\\.[A-Z][0-9]+>?[A-Z]")) {
            return type.toLowerCase().startsWith("missense");
        }
        
        return type.toLowerCase().startsWith("nonsense") ||
            type.toLowerCase().startsWith("splice_site") ||
            type.toLowerCase().startsWith("frame_shift_") ||
            type.toLowerCase().startsWith("nonstop");
        
        // TODO: how about Translation_Start_Site
    }
    
    private static int importCosmic(long eventId, CosmicMutationFrequency cosmic) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            int cosmicId = importCosmic(cosmic, con);
            if (cosmicId==0) {
                CosmicMutationFrequency cmf = getCosmicMutationFrequency(cosmic.getEntrezGeneId(),
                        cosmic.getAminoAcidChange(), con);
                cosmicId = cmf.getId();
            }
            
            pstmt = con.prepareStatement("INSERT INTO mutation_event_cosmic_mapping"
                    + " (`MUTATION_EVENT_ID`,`COSMIC_MUTATION_ID`) VALUES(?,?)");
            pstmt.setLong(1, eventId);
            pstmt.setInt(2, cosmicId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
        
    }
    
    private static int importCosmic(CosmicMutationFrequency cosmic, Connection con) throws DaoException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            if (null != getCosmicMutationFrequency(
                    cosmic.getEntrezGeneId(), cosmic.getAminoAcidChange(),con)) {
                return 0;
            }
            
            pstmt = con.prepareStatement("INSERT INTO cosmic_mutation (`ENTREZ_GENE_ID`,"
                    + "`AMINO_ACID_CHANGE`,`COUNT`) VALUES(?,?,?)",
                        Statement.RETURN_GENERATED_KEYS);
            pstmt.setLong(1, cosmic.getEntrezGeneId());
            pstmt.setString(2, cosmic.getAminoAcidChange());
            pstmt.setInt(3, cosmic.getFrequency());
            pstmt.executeUpdate();
            rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new DaoException("auto key for cosmic not generated.");
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(pstmt, rs);
        }
    }
    
    public static CosmicMutationFrequency getCosmicMutationFrequency(long entrez,
            String aaChange) throws DaoException {
        Connection con = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            return getCosmicMutationFrequency(entrez, aaChange, con);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeConnection(DaoMutation.class, con);
        }
    }
    
    private static CosmicMutationFrequency getCosmicMutationFrequency(long entrez,
            String aaChange, Connection con) throws DaoException {
        if (con == null) {
            throw new NullPointerException("Null SQL connection");
        }
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT * FROM cosmic_mutation "
                    + "WHERE `ENTREZ_GENE_ID`=? AND `AMINO_ACID_CHANGE`=?";
            pstmt = con.prepareStatement(sql);
            pstmt.setLong(1, entrez);
            pstmt.setString(2, aaChange);
            
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return new CosmicMutationFrequency(rs.getInt("COSMIC_MUTATION_ID"),
                        entrez, aaChange, rs.getInt("COUNT"));
            }
            return null;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(pstmt, rs);
        }
    }
    
    public static Map<Long, List<CosmicMutationFrequency>> getCosmicMutationFrequency(
            Collection<Long> mutationEventIds)  throws DaoException {
        return getCosmicMutationFrequency(StringUtils.join(mutationEventIds,","));
    }

    /**
     * get cosmic data for a mutation event
     * @param strMutationEventIds
     * @return Map &lt; mutation event id &gt; , list of cosmic &gt; &gt;
     * @throws DaoException 
     */
    public static Map<Long, List<CosmicMutationFrequency>> getCosmicMutationFrequency(
            String strMutationEventIds) throws DaoException {
        if (strMutationEventIds==null || strMutationEventIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            String sql = "SELECT MUTATION_EVENT_ID, cosmic_mutation.COSMIC_MUTATION_ID,"
                    + " `ENTREZ_GENE_ID`, `AMINO_ACID_CHANGE`, `COUNT`"
                    + " FROM cosmic_mutation, mutation_event_cosmic_mapping"
                    + " WHERE `MUTATION_EVENT_ID` IN ("+ strMutationEventIds +")"
                    + " AND cosmic_mutation.COSMIC_MUTATION_ID=mutation_event_cosmic_mapping.COSMIC_MUTATION_ID";
            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            Map<Long,List<CosmicMutationFrequency>> map = new HashMap<Long,List<CosmicMutationFrequency>>();
            while (rs.next()) {
                long eventId = rs.getLong(1);
                List<CosmicMutationFrequency> list = map.get(eventId);
                if (list==null) {
                    list = new ArrayList<CosmicMutationFrequency>();
                    map.put(eventId, list);
                }
                list.add(new CosmicMutationFrequency(rs.getInt(2),rs.getLong(3),rs.getString(4),rs.getInt(5)));
            }
            
            return map;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
    }
}
