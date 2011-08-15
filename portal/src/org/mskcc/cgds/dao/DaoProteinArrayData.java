
package org.mskcc.cgds.dao;

import org.mskcc.cgds.model.ProteinArrayData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.io.IOException;

import java.util.Collections;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author jj
 */
public class DaoProteinArrayData {
    // use a MySQLbulkLoader instead of SQL "INSERT" statements to load data into table
    private static MySQLbulkLoader myMySQLbulkLoader = null;
    private static DaoProteinArrayData daoProteinArrayData;

    /**
     * Private Constructor to enforce Singleton Pattern.
     */
    private DaoProteinArrayData() {
    }

    /**
     * Gets Global Singleton Instance.
     *
     * @return DaoProteinArrayData Singleton.
     * @throws DaoException Database Error.
     */
    public static DaoProteinArrayData getInstance() throws DaoException {
        if (daoProteinArrayData == null) {
            daoProteinArrayData = new DaoProteinArrayData();
        }

        if (myMySQLbulkLoader == null) {
            myMySQLbulkLoader = new MySQLbulkLoader("protein_array_data");
        }
        return daoProteinArrayData;
    }

    /**
     * Adds a new ProteinArrayData Record to the Database.
     *
     * @param pai ProteinArrayData Object.
     * @return number of records successfully added.
     * @throws DaoException Database Error.
     */
    public int addProteinArrayData(ProteinArrayData pad) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            if (MySQLbulkLoader.isBulkLoad()) {
                //  write to the temp file maintained by the MySQLbulkLoader
                myMySQLbulkLoader.insertRecord(pad.getArrayId(),
                        pad.getCaseId(), Integer.toString(pad.getCancerStudyId()),
                        Double.toString(pad.getAbundance()));
                // return 1 because normal insert will return 1 if no error occurs
                return 1;
            } else {
                con = JdbcUtil.getDbConnection();
                pstmt = con.prepareStatement
                        ("INSERT INTO protein_array_data (`PROTEIN_ARRAY_ID`,`CASE_ID`,`CANCER_STUDY_ID`,`ABUNDANCE`) "
                                + "VALUES (?,?,?,?)");
                pstmt.setString(1, pad.getArrayId());
                pstmt.setString(2, pad.getCaseId());
                pstmt.setInt(3, pad.getCancerStudyId());
                pstmt.setDouble(4, pad.getAbundance());
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
    public int flushProteinArrayDataesToDatabase() throws DaoException {
        try {
            return myMySQLbulkLoader.loadDataFromTempFileIntoDBMS();
        } catch (IOException e) {
            System.err.println("Could not open temp file");
            e.printStackTrace();
            return -1;
        }
    }
    
    public ArrayList<ProteinArrayData> getProteinArrayData(String arrayId, ArrayList<String> caseIds) throws DaoException {
        return getProteinArrayData(Collections.singleton(arrayId), caseIds);
    }

    /**
     * Gets the ProteinArrayData with the Specified array ID.
     *
     * @param arrayId protein array ID.
     * @return map of array id to a list of protein array data.
     * @throws DaoException Database Error.
     */
    public ArrayList<ProteinArrayData> getProteinArrayData(Collection<String> arrayIds, ArrayList<String> caseIds) throws DaoException {
        ArrayList<ProteinArrayData> list = new ArrayList<ProteinArrayData>();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            if (caseIds==null) {
                pstmt = con.prepareStatement
                        ("SELECT * FROM protein_array_data WHERE PROTEIN_ARRAY_ID IN ('"
                        + StringUtils.join(arrayIds, "','") + "')");
            } else {
                pstmt = con.prepareStatement
                        ("SELECT * FROM protein_array_data WHERE PROTEIN_ARRAY_ID IN ('"
                        + StringUtils.join(arrayIds, "','") + "')"
                        + " AND CASE_ID IN ('"+StringUtils.join(caseIds,"','") +"')");
            }
            rs = pstmt.executeQuery();
            while (rs.next()) {
                ProteinArrayData pad = new ProteinArrayData(
                        rs.getString("PROTEIN_ARRAY_ID"),
                        rs.getString("CASE_ID"),
                        rs.getInt("CANCER_STUDY_ID"),
                        rs.getDouble("ABUNDANCE"));
                list.add(pad);
            }
            
            return list;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    /**
     * Gets all Protein array data in the Database.
     *
     * @return ArrayList of ProteinArrayDataes.
     * @throws DaoException Database Error.
     */
    public ArrayList<ProteinArrayData> getAllProteinArrayData() throws DaoException {
        ArrayList<ProteinArrayData> list = new ArrayList<ProteinArrayData>();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT * FROM protein_array_data");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                ProteinArrayData pai = new ProteinArrayData(rs.getString("PROTEIN_ARRAY_ID"),
                        rs.getString("CASE_ID"),
                        rs.getInt("CANCER_STUDY_ID"),
                        rs.getDouble("ABUNDANCE"));
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
     * Deletes all protein array data Records in the Database.
     *
     * @throws DaoException Database Error.
     */
    public void deleteAllRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement("TRUNCATE TABLE protein_array_data");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }
    
    public void deleteRecordsOfOneStudy(int cancerStudyId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement("DELETE FROM protein_array_data WHERE CANCER_STUDY_ID=?");
            pstmt.setInt(1, cancerStudyId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }        
    }
}
