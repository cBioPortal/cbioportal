/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

package org.mskcc.cbio.cgds.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.mskcc.cbio.cgds.model.Case;

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
