
package org.mskcc.cgds.dao;

import org.mskcc.cgds.model.ProteinArrayInfo;

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
public class DaoProteinArrayInfo {
    // use a MySQLbulkLoader instead of SQL "INSERT" statements to load data into table
    private static MySQLbulkLoader myMySQLbulkLoader = null;
    private static DaoProteinArrayInfo daoProteinArrayInfo;

    /**
     * Private Constructor to enforce Singleton Pattern.
     */
    private DaoProteinArrayInfo() {
    }

    /**
     * Gets Global Singleton Instance.
     *
     * @return DaoProteinArrayInfo Singleton.
     * @throws DaoException Database Error.
     */
    public static DaoProteinArrayInfo getInstance() throws DaoException {
        if (daoProteinArrayInfo == null) {
            daoProteinArrayInfo = new DaoProteinArrayInfo();
        }

        if (myMySQLbulkLoader == null) {
            myMySQLbulkLoader = new MySQLbulkLoader("protein_array_info");
        }
        return daoProteinArrayInfo;
    }

    /**
     * Adds a new ProteinArrayInfo Record to the Database.
     *
     * @param pai ProteinArrayInfo Object.
     * @return number of records successfully added.
     * @throws DaoException Database Error.
     */
    public int addProteinArrayInfo(ProteinArrayInfo pai) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            if (MySQLbulkLoader.isBulkLoad()) {
                //  write to the temp file maintained by the MySQLbulkLoader
                myMySQLbulkLoader.insertRecord(pai.getId(),
                        pai.getType(), pai.getSource(), Boolean.toString(pai.isValidated()));
                // return 1 because normal insert will return 1 if no error occurs
                return 1;
            } else {
                con = JdbcUtil.getDbConnection();
                pstmt = con.prepareStatement
                        ("INSERT INTO protein_array_info (`PROTEIN_ARRAY_ID`,`TYPE`,`SOURCE_ORGANISM`,`VALIDATED`) "
                                + "VALUES (?,?,?,?)");
                pstmt.setString(1, pai.getId());
                pstmt.setString(2, pai.getType());
                pstmt.setString(3, pai.getSource());
                pstmt.setBoolean(4, pai.isValidated());
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
    public int flushProteinArrayInfoesToDatabase() throws DaoException {
        try {
            return myMySQLbulkLoader.loadDataFromTempFileIntoDBMS();
        } catch (IOException e) {
            System.err.println("Could not open temp file");
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Gets the ProteinArrayInfo with the Specified array ID.
     *
     * @param arrayId protein array ID.
     * @return ProteinArrayInfo Object.
     * @throws DaoException Database Error.
     */
    public ProteinArrayInfo getProteinArrayInfo(String arrayId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT * FROM protein_array_info WHERE PROTEIN_ARRAY_ID = ?");
            pstmt.setString(1, arrayId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return new ProteinArrayInfo(arrayId,
                        rs.getString("TYPE"),
                        rs.getString("SOURCE_ORGANISM"),
                        rs.getBoolean("VALIDATED"));
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    /**
     * Gets all Protein array information in the Database.
     *
     * @return ArrayList of ProteinArrayInfoes.
     * @throws DaoException Database Error.
     */
    public ArrayList<ProteinArrayInfo> getAllProteinArrayInfoes() throws DaoException {
        ArrayList<ProteinArrayInfo> list = new ArrayList<ProteinArrayInfo>();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT * FROM protein_array_info");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                ProteinArrayInfo pai = new ProteinArrayInfo(rs.getString("PROTEIN_ARRAY_ID"),
                        rs.getString("TYPE"),
                        rs.getString("SOURCE_ORGANISM"),
                        rs.getBoolean("VALIDATED"));
                list.add(pai);
            }
            return list;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    /**
     * Deletes all protein array info Records in the Database.
     *
     * @throws DaoException Database Error.
     */
    public void deleteAllRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement("TRUNCATE TABLE protein_array_info");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }
}
