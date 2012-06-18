package org.mskcc.cgds.dao;

import org.mskcc.cgds.model.ExtendedMutation;
import org.mskcc.cgds.model.CanonicalGene;

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
     * @throws org.mskcc.cgds.dao.DaoException Dao Initialization Error.
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
                    mutation.getAminoAcidChange(), 
                    mutation.getMutationType(), 
                    mutation.getFunctionalImpactScore(), 
                    mutation.getLinkXVar(), 
                    mutation.getLinkPdb(), 
                    mutation.getLinkMsa()
              
              );
              
              // return 1 because normal insert will return 1 if no error occurs
              return 1;
           } else {
           

            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("INSERT INTO mutation (`GENETIC_PROFILE_ID`, `CASE_ID`,"
                        + " `ENTREZ_GENE_ID`,"
                        + " `CENTER`, `SEQUENCER`, `MUTATION_STATUS`, `VALIDATION_STATUS`, `CHR`,"
                        + " `START_POSITION`, `END_POSITION`, `AMINO_ACID_CHANGE`, "
                        + " `MUTATION_TYPE`, `FUNCTIONAL_IMPACT_SCORE`, `LINK_XVAR`, `LINK_PDB`,"
                        + " `LINK_MSA`)"
                        + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
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
            pstmt.setString(11, mutation.getAminoAcidChange());
            pstmt.setString(12, mutation.getMutationType());
            pstmt.setString(13, mutation.getFunctionalImpactScore());
            pstmt.setString(14, mutation.getLinkXVar());
            pstmt.setString(15, mutation.getLinkPdb());
            pstmt.setString(16, mutation.getLinkMsa());
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

    public ArrayList<ExtendedMutation> getMutations (int geneticProfileId, 
            String CaseId) throws DaoException {
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
        mutation.setAminoAcidChange(rs.getString("AMINO_ACID_CHANGE"));
        mutation.setMutationType(rs.getString("MUTATION_TYPE"));
        mutation.setFunctionalImpactScore(rs.getString("FUNCTIONAL_IMPACT_SCORE"));
        mutation.setLinkXVar(rs.getString("LINK_XVAR"));
        mutation.setLinkPdb(rs.getString("LINK_PDB"));
        mutation.setLinkMsa(rs.getString("LINK_MSA"));
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
    
    public int countSamplesWithSpecificMutations(int geneticProfileId, long gene,
            String aminoAcidChange) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            if (aminoAcidChange==null) {
                pstmt = con.prepareStatement
                        ("SELECT COUNT(DISTINCT CASE_ID) FROM mutation WHERE GENETIC_PROFILE_ID=?"
                        + " AND ENTREZ_GENE_ID=?");
                pstmt.setInt(1, geneticProfileId);
                pstmt.setLong(2, gene);
            } else {
                pstmt = con.prepareStatement
                        ("SELECT COUNT(DISTINCT CASE_ID) FROM mutation WHERE GENETIC_PROFILE_ID=?"
                        + " AND ENTREZ_GENE_ID=? AND AMINO_ACID_CHANGE=?");
                pstmt.setInt(1, geneticProfileId);
                pstmt.setLong(2, gene);
                pstmt.setString(3, aminoAcidChange);
            } 
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