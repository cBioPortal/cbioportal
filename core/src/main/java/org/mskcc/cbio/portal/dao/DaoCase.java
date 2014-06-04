/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center 
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center 
 * has been advised of the possibility of such damage.
*/

package org.mskcc.cbio.portal.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.mskcc.cbio.portal.model.Case;

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
            
            Case existingCase = getCase(_case.getCaseId(), _case.getCancerStudyId());
            if (existingCase!=null) {
                return 0;
            }
            
            con = JdbcUtil.getDbConnection(DaoCase.class);
            pstmt = con.prepareStatement
                    ("INSERT INTO _case (`CASE_ID`, `CANCER_STUDY_ID`) "
                            + "VALUES (?,?)");
            pstmt.setString(1, _case.getCaseId());
            pstmt.setInt(2, _case.getCancerStudyId());
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCase.class, con, pstmt, rs);
        }
    }

    public static Case getCase( String caseId, int cancerStudyId ) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCase.class);
            pstmt = con.prepareStatement("SELECT * FROM _case WHERE CASE_ID = ? AND CANCER_STUDY_ID=?");
            pstmt.setString(1, caseId );
            pstmt.setInt(2, cancerStudyId);
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
            JdbcUtil.closeAll(DaoCase.class, con, pstmt, rs);
        }
    }

    public static List<Case> getCase( String caseId ) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCase.class);
            pstmt = con.prepareStatement("SELECT * FROM _case WHERE CASE_ID = ?");
            pstmt.setString(1, caseId );
            rs = pstmt.executeQuery();
            return retriveCasesFromRS(rs);

        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCase.class, con, pstmt, rs);
        }
    }

    public static List<Case> getAllCaseIdsInCancer(int cancerStudyId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCase.class);
            pstmt = con.prepareStatement
                    ("SELECT * FROM _case WHERE CANCER_STUDY_ID = ?");
            pstmt.setInt(1, cancerStudyId);
            rs = pstmt.executeQuery();
            return retriveCasesFromRS(rs);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCase.class, con, pstmt, rs);
        }
    }

    public static List<Case> getAllCases() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCase.class);
            pstmt = con.prepareStatement
                    ("SELECT * FROM _case");
            rs = pstmt.executeQuery();
            return retriveCasesFromRS(rs);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCase.class, con, pstmt, rs);
        }
    }
    
    public static int countCases(int cancerStudyId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCase.class);
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
            JdbcUtil.closeAll(DaoCase.class, con, pstmt, rs);
        }
    }

    public static void deleteAllRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCase.class);
            pstmt = con.prepareStatement("TRUNCATE TABLE _case");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCase.class, con, pstmt, rs);
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
