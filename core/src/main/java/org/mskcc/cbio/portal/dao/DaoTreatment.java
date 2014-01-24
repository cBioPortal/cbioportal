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
                treatment.getStartDate().toString(),
                treatment.getStopDate()==null?null:treatment.getStopDate().toString(),
                treatment.getType(),
                treatment.getSubtype(),
                treatment.getIndication(),
                treatment.getIntent(),
                treatment.getTarget(),
                treatment.getAgent(),
                treatment.getIsotope(),
                treatment.getDose()==null?null:treatment.getDose().toString(),
                treatment.getTotalDose()==null?null:treatment.getTotalDose().toString(),
                treatment.getUnit(),
                treatment.getSchedule(),
                treatment.getRoute()
                );
        return 1;
    }
    
    public static long getLargestTreatmentId() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoTreatment.class);
            pstmt = con.prepareStatement
                    ("SELECT MAX(`TREATMENT_ID`) FROM `treatment`");
            rs = pstmt.executeQuery();
            return rs.next() ? rs.getLong(1) : 0;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoTreatment.class, con, pstmt, rs);
        }
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
