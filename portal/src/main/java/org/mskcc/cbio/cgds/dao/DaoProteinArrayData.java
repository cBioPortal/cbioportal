
package org.mskcc.cbio.cgds.dao;

import org.mskcc.cbio.cgds.model.ProteinArrayData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.io.IOException;

import java.util.Collections;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author jj
 */
public class DaoProteinArrayData {
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

        return daoProteinArrayData;
    }

    /**
     * Adds a new ProteinArrayData Record to the Database.
     *
     * @param pad ProteinArrayData Object.
     * @return number of records successfully added.
     * @throws DaoException Database Error.
     */
    public int addProteinArrayData(ProteinArrayData pad) throws DaoException {
        if (getProteinArrayData(pad.getArrayId(),pad.getCaseId())!=null) {
            System.err.println("RPPA data of "+pad.getArrayId()+" for case "
                    +pad.getCaseId()+" has already been added.");
            return 0;
        }
        
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("INSERT INTO protein_array_data (`PROTEIN_ARRAY_ID`,`CASE_ID`,`ABUNDANCE`) "
                            + "VALUES (?,?,?)");
            pstmt.setString(1, pad.getArrayId());
            pstmt.setString(2, pad.getCaseId());
            pstmt.setDouble(3, pad.getAbundance());
            int rows = pstmt.executeUpdate();
            return rows;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }
    
    public ProteinArrayData getProteinArrayData(String arrayId, String caseId) throws DaoException {
        ArrayList<ProteinArrayData> list = getProteinArrayData(arrayId, Collections.singleton(caseId));
        if (list.isEmpty()) {
            return null;
        }
        
        return list.get(0);
    }
    
    public ArrayList<ProteinArrayData> getProteinArrayData(String arrayId, Collection<String> caseIds)
            throws DaoException {
        return getProteinArrayData(Collections.singleton(arrayId), caseIds);
    }

    /**
     * Gets the ProteinArrayData with the Specified array ID.
     *
     * @param arrayIds protein array ID.
     * @return map of array id to a list of protein array data.
     * @throws DaoException Database Error.
     */
    public ArrayList<ProteinArrayData> getProteinArrayData(Collection<String> arrayIds)
            throws DaoException {
        return getProteinArrayData(arrayIds, null);
    }

    /**
     * Gets the ProteinArrayData with the Specified array ID for specific cases.
     *
     * @param arrayIds protein array ID.
     * @return map of array id to a list of protein array data.
     * @throws DaoException Database Error.
     */
    public ArrayList<ProteinArrayData> getProteinArrayData(Collection<String> arrayIds, Collection<String> caseIds)
            throws DaoException {
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
}
