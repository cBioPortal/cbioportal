
package org.mskcc.cgds.dao;

import org.mskcc.cgds.model.ProteinArrayTarget;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.io.IOException;

/**
 *
 * @author jj
 */
public class DaoProteinArrayTarget {
    // use a MySQLbulkLoader instead of SQL "INSERT" statements to load data into table
    private static MySQLbulkLoader myMySQLbulkLoader = null;
    private static DaoProteinArrayTarget daoProteinArrayTarget;

    /**
     * Private Constructor to enforce Singleton Pattern.
     */
    private DaoProteinArrayTarget() {
    }

    /**
     * Gets Global Singleton Instance.
     *
     * @return DaoProteinArrayTarget Singleton.
     * @throws DaoException Database Error.
     */
    public static DaoProteinArrayTarget getInstance() throws DaoException {
        if (daoProteinArrayTarget == null) {
            daoProteinArrayTarget = new DaoProteinArrayTarget();
        }

        if (myMySQLbulkLoader == null) {
            myMySQLbulkLoader = new MySQLbulkLoader("protein_array_target");
        }
        return daoProteinArrayTarget;
    }

    /**
     * Adds a new ProteinArrayTarget Record to the Database.
     *
     * @param pai ProteinArrayTarget Object.
     * @return number of records successfully added.
     * @throws DaoException Database Error.
     */
    public int addProteinArrayTarget(ProteinArrayTarget pat) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            if (MySQLbulkLoader.isBulkLoad()) {
                //  write to the temp file maintained by the MySQLbulkLoader
                myMySQLbulkLoader.insertRecord(pat.getArrayId(),
                        Long.toString(pat.getEntrezGeneId()), pat.getResidue());
                // return 1 because normal insert will return 1 if no error occurs
                return 1;
            } else {
                con = JdbcUtil.getDbConnection();
                pstmt = con.prepareStatement
                        ("INSERT INTO protein_array_target (`PROTEIN_ARRAY_ID`,`ENTREZ_GENE_ID`,`TARGET_RESIDUE`) "
                                + "VALUES (?,?,?)");
                pstmt.setString(1, pat.getArrayId());
                pstmt.setLong(2, pat.getEntrezGeneId());
                pstmt.setString(3, pat.getResidue());
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
    public int flushProteinArrayTargetsToDatabase() throws DaoException {
        try {
            return myMySQLbulkLoader.loadDataFromTempFileIntoDBMS();
        } catch (IOException e) {
            System.err.println("Could not open temp file");
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Gets the list of protein array target with the Specified entrez gene ID.
     *
     * @param gene entrez gene ID.
     * @return ProteinArrayInfo Object.
     * @throws DaoException Database Error.
     */
    public ArrayList<ProteinArrayTarget> getProteinArrayTarget(long gene) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT * FROM protein_array_target WHERE ENTREZ_GENE_ID = ?");
            pstmt.setLong(1, gene);
            
            ArrayList<ProteinArrayTarget> list = new ArrayList<ProteinArrayTarget>();
            rs = pstmt.executeQuery();
            while (rs.next()) {
                ProteinArrayTarget pat = new ProteinArrayTarget(rs.getString("PROTEIN_ARRAY_ID"),
                        gene, rs.getString("TARGET_RESIDUE"));
                list.add(pat);
            }
            
            return list;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    /**
     * Gets all Protein array targets in the Database.
     *
     * @return ArrayList of ProteinArrayTargets.
     * @throws DaoException Database Error.
     */
    public ArrayList<ProteinArrayTarget> getAllProteinArrayTargetes() throws DaoException {
        ArrayList<ProteinArrayTarget> list = new ArrayList<ProteinArrayTarget>();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT * FROM protein_array_target");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                ProteinArrayTarget pat = new ProteinArrayTarget(rs.getString("PROTEIN_ARRAY_ID"),
                        rs.getLong("ENTREZ_GENE_ID"),
                        rs.getString("TARGET_RESIDUE"));
                list.add(pat);
            }
            return list;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    /**
     * Deletes all protein array target Records in the Database.
     *
     * @throws DaoException Database Error.
     */
    public void deleteAllRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement("TRUNCATE TABLE protein_array_target");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }
}
