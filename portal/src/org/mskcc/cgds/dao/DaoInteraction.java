package org.mskcc.cgds.dao;

import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.model.Interaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;

/**
 * Data Access Object to Interaction Table.
 *
 * @author Ethan Cerami.
 */
public class DaoInteraction {
    // use a MySQLbulkLoader instead of SQL "INSERT" statements to load data into table
    private static MySQLbulkLoader myMySQLbulkLoader = null;
    private static DaoInteraction daoInteraction;
    private static final String NA = "NA";

    /**
     * Private Constructor to enforce Singleton Pattern.
     */
    private DaoInteraction() {
    }

    /**
     * Gets Global Singleton Instance.
     *
     * @return DaoInteraction Singleton.
     * @throws org.mskcc.cgds.dao.DaoException Database Error.
     */
    public static DaoInteraction getInstance() throws DaoException {
        if (daoInteraction == null) {
            daoInteraction = new DaoInteraction();
        }

        if (myMySQLbulkLoader == null) {
            myMySQLbulkLoader = new MySQLbulkLoader("interaction");
        }
        return daoInteraction;
    }

    /**
     * Adds a new Interaction Record to the Database.
     *
     * @param geneA             Gene A.
     * @param geneB             Gene B.
     * @param interactionType   Interaction Type.
     * @param dataSource        Data Source.
     * @param experimentTypes   Experiment Types.
     * @param pmids             PubMed IDs.
     * @return number of records added.
     * @throws DaoException Database Error.
     */
    public int addInteraction(CanonicalGene geneA, CanonicalGene geneB,
            String interactionType, String dataSource, String experimentTypes, String pmids)
            throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        //  Basic Parameter Checking.  Some are required.  Some are not.
        if (interactionType == null) {
            throw new IllegalArgumentException ("Interaction type cannot be null");
        }
        if (dataSource == null) {
            throw new IllegalArgumentException ("Data Source cannot be null");
        }
        if (experimentTypes == null) {
            experimentTypes = NA;
        }
        if (pmids == null) {
            pmids = NA;
        }

        try {
            if (MySQLbulkLoader.isBulkLoad()) {
                //  write to the temp file maintained by the MySQLbulkLoader
                myMySQLbulkLoader.insertRecord(Long.toString(geneA.getEntrezGeneId()),
                        Long.toString(geneB.getEntrezGeneId()), interactionType,
                        dataSource, experimentTypes, pmids);

                // return 1 because normal insert will return 1 if no error occurs
                return 1;
            } else {
                con = JdbcUtil.getDbConnection();
                pstmt = con.prepareStatement
                        ("INSERT INTO interaction (`GENE_A`,`GENE_B`, `INTERACTION_TYPE`," +
                                "`DATA_SOURCE`, `EXPERIMENT_TYPES`, `PMIDS`)"
                                + "VALUES (?,?,?,?,?,?)");
                pstmt.setLong(1, geneA.getEntrezGeneId());
                pstmt.setLong(2, geneB.getEntrezGeneId());
                pstmt.setString(3, interactionType);
                pstmt.setString(4, dataSource);
                pstmt.setString(5, experimentTypes);
                pstmt.setString(6, pmids);
                return pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    /**
     * Gets all Interactions involving the Specified Gene.
     * @param gene Gene
     * @return ArrayList of Interaction Objects.
     * @throws DaoException Database Error.
     */
    public ArrayList<Interaction> getInteractions (CanonicalGene gene)
        throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ArrayList <Interaction> interactionList = new ArrayList <Interaction>();
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                ("SELECT * FROM interaction where GENE_A=? or GENE_B=?");
            pstmt.setLong(1, gene.getEntrezGeneId());
            pstmt.setLong(2, gene.getEntrezGeneId());
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Interaction interaction = extractInteraction(rs);
                interactionList.add(interaction);
            }
            return interactionList;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    /**
     * Gets all Interactions involving the Specified Gene.
     * @param gene Gene
     * @return ArrayList of Interaction Objects.
     * @throws DaoException Database Error.
     */
    public ArrayList<Interaction> getInteractions (Collection<Long> entrezGeneIds)
        throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ArrayList <Interaction> interactionList = new ArrayList <Interaction>();
        try {
            con = JdbcUtil.getDbConnection();
            String idStr = "("+StringUtils.join(entrezGeneIds, ",")+")";
            pstmt = con.prepareStatement
                ("SELECT * FROM interaction where GENE_A IN "
                    + idStr + " OR GENE_B IN "+idStr);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Interaction interaction = extractInteraction(rs);
                interactionList.add(interaction);
            }
            return interactionList;
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
     * @throws org.mskcc.cgds.dao.DaoException Database Error.
     */
    public int flushToDatabase() throws DaoException {
        try {
            return myMySQLbulkLoader.loadDataFromTempFileIntoDBMS();
        } catch (IOException e) {
            System.err.println("Could not open temp file");
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Gets all Interactions in the Database.
     *
     * @return ArrayList of Interaction Objects.
     * @throws DaoException Database Error.
     */
    public ArrayList<Interaction> getAllInteractions() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ArrayList <Interaction> interactionList = new ArrayList <Interaction>();
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT * FROM interaction");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Interaction interaction = extractInteraction(rs);
                interactionList.add(interaction);
            }
            return interactionList;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    private Interaction extractInteraction(ResultSet rs) throws SQLException {
        Interaction interaction = new Interaction();
        interaction.setGeneA(rs.getLong("GENE_A"));
        interaction.setGeneB(rs.getLong("GENE_B"));
        interaction.setInteractionType(rs.getString("INTERACTION_TYPE"));
        interaction.setSource(rs.getString("DATA_SOURCE"));
        interaction.setExperimentTypes(rs.getString("EXPERIMENT_TYPES"));
        interaction.setPmids(rs.getString("PMIDS"));
        return interaction;
    }

    /**
     * Gets the Number of Interaction Records in the Database.
     *
     * @return number of gene records.
     * @throws org.mskcc.cgds.dao.DaoException Database Error.
     */
    public int getCount() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT COUNT(*) FROM interaction");
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
     * Deletes all Gene Records in the Database.
     *
     * @throws org.mskcc.cgds.dao.DaoException Database Error.
     */
    public void deleteAllRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement("TRUNCATE TABLE interaction");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }
}
