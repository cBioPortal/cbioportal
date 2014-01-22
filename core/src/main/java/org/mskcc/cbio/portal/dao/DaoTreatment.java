/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.mskcc.cbio.portal.model.Treatment;

/**
 *
 * @author jgao
 */
public final class DaoTreatment {
    private DaoTreatment() {
        throw new AssertionError("DaoTreatment should not be instanciated");
    }
    
    public static int addDatum(Treatment treatment) {
        if (!MySQLbulkLoader.isBulkLoad()) {
            throw new IllegalStateException("Only buld load mode is allowed for importing treatment data");
        }
        
        MySQLbulkLoader.getMySQLbulkLoader("treatment").insertRecord(
                Long.toString(treatment.getTreatmentId()),
                Integer.toString(treatment.getCancerStudyId()),
                treatment.getPatientId(),
                Integer.toString(treatment.getStartDate()),
                Integer.toString(treatment.getStopDate()),
                treatment.getType(),
                treatment.getAgent(),
                Double.toString(treatment.getDose()),
                treatment.getUnit(),
                treatment.getSchedule()
                );
        return 1;
    }
    
    
    public static void deleteByCancerStudyId(int cancerStudyId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoTreatment.class);
            pstmt = con.prepareStatement("DELETE FROM treatment WHERE CANCER_STUDY_ID=?");
            pstmt.setInt(1, cancerStudyId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoTreatment.class, con, pstmt, rs);
        }
    }
}
