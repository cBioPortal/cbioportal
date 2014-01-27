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
import org.mskcc.cbio.portal.model.Diagnostic;

/**
 *
 * @author jgao
 */
public final class DaoDiagnostic {
    private DaoDiagnostic() {
        throw new AssertionError("DaoDiagnostic should not be instanciated");
    }
    
    public static int addDatum(Diagnostic diagnostic) {
        if (!MySQLbulkLoader.isBulkLoad()) {
            throw new IllegalStateException("Only buld load mode is allowed for importing diagnostic data");
        }
        
        MySQLbulkLoader.getMySQLbulkLoader("diagnostic").insertRecord(
                Long.toString(diagnostic.getDiagnosticId()),
                Integer.toString(diagnostic.getCancerStudyId()),
                diagnostic.getPatientId(),
                diagnostic.getDate().toString(),
                diagnostic.getType(),
                diagnostic.getSide(),
                diagnostic.getTarget(),
                diagnostic.getResult(),
                diagnostic.getStatus(),
                diagnostic.getImageBaseline(),
                diagnostic.getNumNewTumors()==null?null:diagnostic.getNumNewTumors().toString(),
                diagnostic.getNotes()
                );
        return 1;
    }
    
    public static List<Diagnostic> getDiagnostic(int cancerStudyId, String patientId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
           con = JdbcUtil.getDbConnection(DaoDiagnostic.class);
           pstmt = con.prepareStatement("SELECT * FROM diagnostic WHERE CANCER_STUDY_ID=? AND PATIENT_ID=?");
           pstmt.setInt(1, cancerStudyId);
           pstmt.setString(2, patientId);
           
           rs = pstmt.executeQuery();
           List<Diagnostic> list = new ArrayList<Diagnostic>();
           while (rs.next()) {
              Diagnostic diagnostic = extractDiagnostic(rs);
              list.add(diagnostic);
           }
           return list;
        } catch (SQLException e) {
           throw new DaoException(e);
        } finally {
           JdbcUtil.closeAll(DaoDiagnostic.class, con, pstmt, rs);
        }
    }
    
    private static Diagnostic extractDiagnostic(ResultSet rs) throws SQLException {
        Diagnostic diagnostic = new Diagnostic();
        diagnostic.setDiagnosticId(rs.getLong("DIAGNOSTIC_ID"));
        diagnostic.setCancerStudyId(rs.getInt("CANCER_STUDY_ID"));
        diagnostic.setPatientId(rs.getString("PATIENT_ID"));
        diagnostic.setDate(JdbcUtil.readIntegerFromResultSet(rs, "DATE"));
        diagnostic.setType(rs.getString("TYPE"));
        diagnostic.setSide(rs.getString("SIDE"));
        diagnostic.setTarget(rs.getString("TARGET"));
        diagnostic.setResult(rs.getString("RESULT"));
        diagnostic.setStatus(rs.getString("STATUS"));
        diagnostic.setImageBaseline(rs.getString("IMAGE_BASELINE"));
        diagnostic.setNumNewTumors(JdbcUtil.readIntegerFromResultSet(rs, "NUM_NEW_TUMORS"));
        diagnostic.setNotes(rs.getString("NOTES"));
        return diagnostic;
    }
    
    public static long getLargestDiagnosticId() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoDiagnostic.class);
            pstmt = con.prepareStatement
                    ("SELECT MAX(`DIAGNOSTIC_ID`) FROM `diagnostic`");
            rs = pstmt.executeQuery();
            return rs.next() ? rs.getLong(1) : 0;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoDiagnostic.class, con, pstmt, rs);
        }
    }
    
    public static void deleteByCancerStudyId(int cancerStudyId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoDiagnostic.class);
            pstmt = con.prepareStatement("DELETE FROM diagnostic WHERE CANCER_STUDY_ID=?");
            pstmt.setInt(1, cancerStudyId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoDiagnostic.class, con, pstmt, rs);
        }
    }
}