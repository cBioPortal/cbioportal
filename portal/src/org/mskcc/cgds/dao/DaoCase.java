package org.mskcc.cgds.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.mskcc.cgds.model.Case;

/**
 * Data access object for Case table
 */
public final class DaoCase {
    public static int addCase(Case _case) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            if (!DaoCancerStudy.doesCancerStudyExistByInternalId(_case.getCancerStudyId())) {
                System.err.println("Cancer Study of "+_case.getCancerStudyId()+" does not exist.");
                return 0;
            }
            
            Case existingCase = getCase(_case.getCaseId());
            if (existingCase!=null) {
                if (!_case.equals(existingCase)) {
                    System.err.println("Inconsistent case information, e.g. different cancer study IDs "
                            + "for the same case "+_case.getCaseId());
                }
                return 0;
            }
            
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("INSERT INTO _case (`CASE_ID`, `CANCER_STUDY_ID`) "
                            + "VALUES (?,?)");
            pstmt.setString(1, _case.getCaseId());
            pstmt.setInt(2, _case.getCancerStudyId());
            int rows = pstmt.executeUpdate();
            return rows;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    public static Case getCase( String caseId ) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement("SELECT * FROM _case WHERE CASE_ID = ?");
            pstmt.setString(1, caseId );
            rs = pstmt.executeQuery();
            List<Case> cases = retriveCasesFromRS(rs);
            if( !cases.isEmpty() ) {
               return cases.get(0);
            }else{
               return null;
            }

        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    public static List<Case> getAllCaseIdsInCancer(int cancerStudyId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT * FROM _case WHERE CANCER_STUDY_ID = ?");
            pstmt.setInt(1, cancerStudyId);
            rs = pstmt.executeQuery();
            return retriveCasesFromRS(rs);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    public static List<Case> getAllCases() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT * FROM _case");
            rs = pstmt.executeQuery();
            return retriveCasesFromRS(rs);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }
    
    public static int countCases(int cancerStudyId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT count(*) FROM _case WHERE CANCER_STUDY_ID = ?");
            pstmt.setInt(1, cancerStudyId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                return 0;
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    public static void deleteAllRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement("TRUNCATE TABLE _case");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }
    
    private static List<Case> retriveCasesFromRS(ResultSet rs) throws SQLException {
        List<Case> cases = new ArrayList<Case>();
        while (rs.next()) {
            Case _case = new Case(rs.getString("CASE_ID"), rs.getInt("CANCER_STUDY_ID"));
            cases.add(_case);
        }
        return cases;
    }
}
