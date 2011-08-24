package org.mskcc.cgds.dao;

import org.mskcc.cgds.model.CanonicalGene;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

/**
 * Data Access Object for microRNA Alteration Table.
 *
 * @author Ethan Cerami.
 */
public class DaoMicroRnaAlteration {
    private static final String DELIM = ",";

    // use a MySQLbulkLoader instead of SQL "INSERT" statements to load data into table
    private static MySQLbulkLoader myMySQLbulkLoader = null;

    public static final String NAN = "NaN";
    private static DaoMicroRnaAlteration daoMicroRnaAlteration = null;

    /**
     * Private Constructor (Singleton pattern).
     */
    private DaoMicroRnaAlteration() {
    }

    /**
     * Gets Instance of Dao Object. (Singleton pattern).
     *
     * @return DaoGeneticAlteration Object.
     * @throws org.mskcc.cgds.dao.DaoException Dao Initialization Error.
     */
    public static DaoMicroRnaAlteration getInstance() throws DaoException {
        if (daoMicroRnaAlteration == null) {
            daoMicroRnaAlteration = new DaoMicroRnaAlteration();
        }
        // create the MySQLbulkLoader if it doesn't exist
        if( myMySQLbulkLoader == null ){
            myMySQLbulkLoader = new MySQLbulkLoader( "micro_rna_alteration" );
        }
        return daoMicroRnaAlteration;
    }

    /**
     * Adds a Row of microRNA Alterations associated with a Genetic Profile ID and Entrez Gene ID.
     * @param geneticProfileId Genetic Profile ID.
     * @param microRnaId microRNA ID.
     * @param values multiple values.
     * @return number of rows successfully added.
     * @throws DaoException Database Error.
     */
    public int addMicroRnaAlterations(int geneticProfileId, String microRnaId, String[] values)
            throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        StringBuffer valueBuffer = new StringBuffer();
        for (String value:  values) {
            if (value.contains(DELIM)) {
                throw new IllegalArgumentException ("Value cannot contain delim:  " + DELIM
                    + " --> " + value);
            }
            valueBuffer.append (value + DELIM);
        }
        try {
            if (MySQLbulkLoader.isBulkLoad()) {

                // use this code if bulk loading
                // write to the temp file maintained by the MySQLbulkLoader 
                myMySQLbulkLoader.insertRecord( 
                        Integer.toString(geneticProfileId ), microRnaId,
                        valueBuffer.toString());
                
                // return 1 because normal insert will return 1 if no error occurs
                return 1;
             } else {

                con = JdbcUtil.getDbConnection();
                pstmt = con.prepareStatement
                        ("INSERT INTO micro_rna_alteration (`GENETIC_PROFILE_ID`, " +
                                " `MICRO_RNA_ID`,`VALUES`) VALUES (?,?,?)");
                pstmt.setInt(1, geneticProfileId);
                pstmt.setString(2, microRnaId);
                pstmt.setString(3, valueBuffer.toString());
                int rows = pstmt.executeUpdate();
                return rows;
             }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    /**
     * Loads the temp file maintained by the MySQLbulkLoader into the DMBS.
     * 
     * @return number of records inserted
     * @throws DaoException Database Error.
     */
    public int flushMicroRnaAlteration() throws DaoException {
        try {
            return myMySQLbulkLoader.loadDataFromTempFileIntoDBMS();
        } catch (IOException e) {
            System.err.println("Could not open temp file");
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Gets the microRNA Alteration for the Specified Parameters.
     *
     * @param geneticProfileId  Genetic Profile ID.
     * @param caseId            Case ID.
     * @param microRnaId        microRNA ID.
     * @return microRNA Value.
     * @throws DaoException Database Error.
     */
    public String getMicroRnaAlteration(int geneticProfileId, String caseId,
            String microRnaId) throws DaoException {
        return getMicroRnaAlterationMap(geneticProfileId, microRnaId).get(caseId);
    }

    /**
     * Gets the microRNA Values, keyed by Case ID.
     * @param geneticProfileId  Genetic Profile ID.
     * @param microRnaId        microRNA ID.
     * @return HashMap of microRNA values, keyed by Case ID.
     * @throws DaoException Database Error.
     */
    public HashMap<String, String> getMicroRnaAlterationMap(int geneticProfileId,
            String microRnaId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        HashMap<String, String> map = new HashMap<String, String>();

        DaoGeneticProfileCases daoGeneticProfileCases = new DaoGeneticProfileCases();
        ArrayList<String> orderedCaseList = daoGeneticProfileCases.getOrderedCaseList
                (geneticProfileId);
        if (orderedCaseList == null || orderedCaseList.size() ==0) {
            throw new IllegalArgumentException ("Could not find any cases for genetic" +
                    " profile ID:  " + geneticProfileId);
        }

        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT * FROM micro_rna_alteration WHERE" +
                            " MICRO_RNA_ID = ? AND GENETIC_PROFILE_ID = ?");
            pstmt.setString(1, microRnaId);
            pstmt.setInt(2, geneticProfileId);
            rs = pstmt.executeQuery();
            if  (rs.next()) {
                String values = rs.getString("VALUES");
                String valueParts[] = values.split(DELIM);
                for (int i=0; i<valueParts.length; i++) {
                    String value = valueParts[i];
                    String caseId = orderedCaseList.get(i);
                    map.put(caseId, value);
                }
            }
            return map;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    /**
     * Gets all microRNAs in a Specific Genetic Profile.
     * @param geneticProfileId  Genetic Profile ID.
     * @return Set of Canonical Genes.
     * @throws DaoException Database Error.
     */
    public Set<String> getGenesInProfile(int geneticProfileId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Set <String> microRNASet = new HashSet<String>();
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();

        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT * FROM micro_rna_alteration WHERE GENETIC_PROFILE_ID = ?");
            pstmt.setInt(1, geneticProfileId);

            rs = pstmt.executeQuery();
            while  (rs.next()) {
                String microRNAId = rs.getString("MICRO_RNA_ID");
                microRNASet.add(microRNAId);
            }
            return microRNASet;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    /**
     * Gets the Number of Records in the Database.
     * @return number of records.
     * @throws DaoException Database Error.
     */
    public int getCount() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT COUNT(*) FROM micro_rna_alteration");
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

    /**
     * Deletes all Records in the Specified Genetic Profile.
     *
     * @param geneticProfileId Genetic Profile ID.
     * @throws DaoException Database Error.
     */
    public void deleteAllRecordsInGeneticProfile(long geneticProfileId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement("DELETE from " +
                    "micro_rna_alteration WHERE GENETIC_PROFILE_ID=?");
            pstmt.setLong(1, geneticProfileId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    /**
     * Deletes all Records in the Table.
     * @throws DaoException Database Error.
     */
    public void deleteAllRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement("TRUNCATE TABLE micro_rna_alteration");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }
}