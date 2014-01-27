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
import org.mskcc.cbio.portal.model.Treatment;
import org.mskcc.cbio.portal.model.User;

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
    
    public static List<Treatment> getTreatment(int cancerStudyId, String patientId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
           con = JdbcUtil.getDbConnection(DaoTreatment.class);
           pstmt = con.prepareStatement("SELECT * FROM treatment WHERE CANCER_STUDY_ID=? AND PATIENT_ID=?");
           pstmt.setInt(1, cancerStudyId);
           pstmt.setString(2, patientId);
           
           rs = pstmt.executeQuery();
           List<Treatment> list = new ArrayList<Treatment>();
           while (rs.next()) {
              Treatment treatment = extractTreatment(rs);
              list.add(treatment);
           }
           return list;
        } catch (SQLException e) {
           throw new DaoException(e);
        } finally {
           JdbcUtil.closeAll(DaoTreatment.class, con, pstmt, rs);
        }
    }
    
    private static Treatment extractTreatment(ResultSet rs) throws SQLException {
        Treatment treatment = new Treatment();
        treatment.setTreatmentId(rs.getLong("TREATMENT_ID"));
        treatment.setCancerStudyId(rs.getInt("CANCER_STUDY_ID"));
        treatment.setPatientId(rs.getString("PATIENT_ID"));
        treatment.setStartDate(JdbcUtil.readIntegerFromResultSet(rs, "START_DATE"));
        treatment.setStopDate(JdbcUtil.readIntegerFromResultSet(rs, "STOP_DATE"));
        treatment.setType(rs.getString("TYPE"));
        treatment.setSubtype(rs.getString("SUBTYPE"));
        treatment.setIndication(rs.getString("INDICATION"));
        treatment.setIntent(rs.getString("INTENT"));
        treatment.setTarget(rs.getString("TARGET"));
        treatment.setAgent(rs.getString("AGENT"));
        treatment.setIsotope(rs.getString("ISOTOPE"));
        treatment.setDose(JdbcUtil.readDoubleFromResultSet(rs, "DOSE"));
        treatment.setTotalDose(JdbcUtil.readDoubleFromResultSet(rs, "TOTAL_DOSE"));
        treatment.setUnit(rs.getString("UNIT"));
        treatment.setSchedule(rs.getString("SCHEDULE"));
        treatment.setRoute(rs.getString("ROUTE"));
        return treatment;
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
