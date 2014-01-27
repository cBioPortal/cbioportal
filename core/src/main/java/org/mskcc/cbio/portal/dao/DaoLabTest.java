/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.mskcc.cbio.portal.model.LabTest;
import org.mskcc.cbio.portal.model.LabTest;

/**
 *
 * @author jgao
 */
public final class DaoLabTest {
    private DaoLabTest() {
        throw new AssertionError("DaoLabTest should not be instanciated");
    }
    
    public static int addDatum(LabTest labTest) {
        if (!MySQLbulkLoader.isBulkLoad()) {
            throw new IllegalStateException("Only buld load mode is allowed for importing lab test data");
        }
        
        MySQLbulkLoader.getMySQLbulkLoader("lab_test").insertRecord(
                Long.toString(labTest.getLabTestId()),
                Integer.toString(labTest.getCancerStudyId()),
                labTest.getPatientId(),
                labTest.getDate().toString(),
                labTest.getTest(),
                labTest.getResult(),
                labTest.getUnit(),
                labTest.getNormalRange(),
                labTest.getNotes()
                );
        return 1;
    }
    
    public static List<LabTest> getLabTest(int cancerStudyId, String patientId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
           con = JdbcUtil.getDbConnection(DaoLabTest.class);
           pstmt = con.prepareStatement("SELECT * FROM lab_test WHERE CANCER_STUDY_ID=? AND PATIENT_ID=?");
           pstmt.setInt(1, cancerStudyId);
           pstmt.setString(2, patientId);
           
           rs = pstmt.executeQuery();
           List<LabTest> list = new ArrayList<LabTest>();
           while (rs.next()) {
              LabTest labTest = extractLabTest(rs);
              list.add(labTest);
           }
           return list;
        } catch (SQLException e) {
           throw new DaoException(e);
        } finally {
           JdbcUtil.closeAll(DaoLabTest.class, con, pstmt, rs);
        }
    }
    
    private static LabTest extractLabTest(ResultSet rs) throws SQLException {
        LabTest labTest = new LabTest();
        labTest.setLabTestId(rs.getLong("LAB_TEST_ID"));
        labTest.setCancerStudyId(rs.getInt("CANCER_STUDY_ID"));
        labTest.setPatientId(rs.getString("PATIENT_ID"));
        labTest.setDate(JdbcUtil.readIntegerFromResultSet(rs, "DATE"));
        labTest.setTest(rs.getString("TEST"));
        labTest.setResult(rs.getString("RESULT"));
        labTest.setUnit(rs.getString("UNIT"));
        labTest.setNormalRange(rs.getString("NORMAL_RANGE"));
        labTest.setNotes(rs.getString("NOTES"));
        return labTest;
    }
    
    public static long getLargestLabTestId() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoLabTest.class);
            pstmt = con.prepareStatement
                    ("SELECT MAX(`LAB_TEST_ID`) FROM `lab_test`");
            rs = pstmt.executeQuery();
            return rs.next() ? rs.getLong(1) : 0;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoLabTest.class, con, pstmt, rs);
        }
    }
    
    public static void deleteByCancerStudyId(int cancerStudyId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoLabTest.class);
            pstmt = con.prepareStatement("DELETE FROM lab_test WHERE CANCER_STUDY_ID=?");
            pstmt.setInt(1, cancerStudyId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoLabTest.class, con, pstmt, rs);
        }
    }
}
