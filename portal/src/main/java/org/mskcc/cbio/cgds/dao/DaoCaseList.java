package org.mskcc.cbio.cgds.dao;

import org.mskcc.cbio.cgds.model.CaseList;
import org.mskcc.cbio.cgds.model.CaseListCategory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Data access object for Case_List table
 */
public class DaoCaseList {

	/**
	 * Adds record to case_list table.
	 */
    public int addCaseList(CaseList caseList) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();

            pstmt = con.prepareStatement("INSERT INTO case_list (`STABLE_ID`, `CANCER_STUDY_ID`, `NAME`, `CATEGORY`," +
                    "`DESCRIPTION`)" + " VALUES (?,?,?,?,?)");
            pstmt.setString(1, caseList.getStableId());
            pstmt.setInt(2, caseList.getCancerStudyId());
            pstmt.setString(3, caseList.getName());
            pstmt.setString(4, caseList.getCaseListCategory().getCategory());
            pstmt.setString(5, caseList.getDescription());
            int rows = pstmt.executeUpdate();
   			int listListRow = addCaseListList(caseList, con);
   			rows = (listListRow != -1) ? (rows + listListRow) : rows;
            return rows;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

	/**
	 * Given a case list by stable Id, returns a case list.
	 */
    public CaseList getCaseListByStableId(String stableId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT * FROM case_list WHERE STABLE_ID = ?");
            pstmt.setString(1, stableId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                CaseList caseList = extractCaseList(rs);
                caseList.setCaseList(getCaseListList(caseList, con));
                return caseList;
            }
			return null;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

	/**
	 * Given a case list ID, returns a case list.
	 */
    public CaseList getCaseListById(int id) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT * FROM case_list WHERE LIST_ID = ?");
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                CaseList caseList = extractCaseList(rs);
				caseList.setCaseList(getCaseListList(caseList, con));
                return caseList;
            }
			return null;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

	/**
	 * Given a cancerStudyId, returns all case list.
	 */
    public ArrayList<CaseList> getAllCaseLists( int cancerStudyId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();

            pstmt = con.prepareStatement
                    ("SELECT * FROM case_list WHERE CANCER_STUDY_ID = ? ORDER BY NAME");
            pstmt.setInt(1, cancerStudyId);
            rs = pstmt.executeQuery();
            ArrayList<CaseList> list = new ArrayList<CaseList>();
            while (rs.next()) {
                CaseList caseList = extractCaseList(rs);
                list.add(caseList);
            }
			// get case list-list
			for (CaseList caseList : list) {
				caseList.setCaseList(getCaseListList(caseList, con));
			}
            return list;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

	/**
	 * Returns a list of all case lists.
	 */
    public ArrayList<CaseList> getAllCaseLists() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT * FROM case_list");
            rs = pstmt.executeQuery();
            ArrayList<CaseList> list = new ArrayList<CaseList>();
            while (rs.next()) {
                CaseList caseList = extractCaseList(rs);
                list.add(caseList);
            }
			// get case list-list
			for (CaseList caseList : list) {
				caseList.setCaseList(getCaseListList(caseList, con));
			}
            return list;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    /**
	 * Given a case id, determines if it exists
	 */
    public boolean caseIDExists(String caseID) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT * FROM case_list_list WHERE CASE_ID = ?");
            pstmt.setString(1, caseID);
            rs = pstmt.executeQuery();
            return (rs.next());
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

	/**
	 * Clears all records from case list & case_list_list.
	 */
    public void deleteAllRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement("TRUNCATE TABLE case_list");
            pstmt.executeUpdate();
            pstmt = con.prepareStatement("TRUNCATE TABLE case_list_list");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

	/**
	 * Given a case list, gets list id from case_list table
	 */
	private int getCaseListId(CaseList caseList) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement("SELECT LIST_ID FROM case_list WHERE STABLE_ID=?");
            pstmt.setString(1, caseList.getStableId());
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("LIST_ID");
            }
            return -1;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
	}

	/**
	 * Adds record to case_list_list.
	 */
    private int addCaseListList(CaseList caseList, Connection con) throws DaoException {
		
		// get case list id
		int caseListId = getCaseListId(caseList);
		if (caseListId == -1) {
            return -1;
        }

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
			int rows = 0;
			for (String caseId : caseList.getCaseList()) {
                pstmt = con.prepareStatement("INSERT INTO case_list_list (`LIST_ID`, `CASE_ID`) VALUES (?,?)");
				pstmt.setInt(1, caseListId);
				pstmt.setString(2, caseId);
				rows += pstmt.executeUpdate();
			}
			return rows;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(pstmt, rs);
        }
    }

	/**
	 * Given a case list object (thus case list id) gets case list list.
	 */
	private ArrayList<String> getCaseListList(CaseList caseList, Connection con) throws DaoException {

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = con.prepareStatement
                    ("SELECT * FROM case_list_list WHERE LIST_ID = ?");
            pstmt.setInt(1, caseList.getCaseListId());
            rs = pstmt.executeQuery();
            ArrayList<String> toReturn = new ArrayList<String>();
            while (rs.next()) {
				toReturn.add(rs.getString("CASE_ID"));
			}
			return toReturn;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(pstmt, rs);
        }
	}

	/**
	 * Given a result set, creates a case list object.
	 */
    private CaseList extractCaseList(ResultSet rs) throws SQLException {
        CaseList caseList = new CaseList();
        caseList.setStableId(rs.getString("STABLE_ID"));
        caseList.setCancerStudyId(rs.getInt("CANCER_STUDY_ID"));
        caseList.setName(rs.getString("NAME"));
        caseList.setCaseListCategory(CaseListCategory.get(rs.getString("CATEGORY")));
        caseList.setDescription(rs.getString("DESCRIPTION"));
        caseList.setCaseListId(rs.getInt("LIST_ID"));
        return caseList;
    }
}