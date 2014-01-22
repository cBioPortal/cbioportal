/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
