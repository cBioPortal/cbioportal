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

import org.mskcc.cbio.portal.model.*;

import java.sql.*;
import java.util.*;

/**
 * Data access object for patient_List table
 */
public class DaoPatientList {

	/**
	 * Adds record to patient_list table.
	 */
    public int addPatientList(PatientList patientList) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int rows;
        try {
            con = JdbcUtil.getDbConnection(DaoPatientList.class);

            pstmt = con.prepareStatement("INSERT INTO patient_list (`STABLE_ID`, `CANCER_STUDY_ID`, `NAME`, `CATEGORY`," +
                    "`DESCRIPTION`)" + " VALUES (?,?,?,?,?)");
            pstmt.setString(1, patientList.getStableId());
            pstmt.setInt(2, patientList.getCancerStudyId());
            pstmt.setString(3, patientList.getName());
            pstmt.setString(4, patientList.getPatientListCategory().getCategory());
            pstmt.setString(5, patientList.getDescription());
            rows = pstmt.executeUpdate();
   			int listListRow = addPatientListList(patientList, con);
   			rows = (listListRow != -1) ? (rows + listListRow) : rows;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoPatientList.class, con, pstmt, rs);
        }
        
        return rows;
    }

	/**
	 * Given a patient list by stable Id, returns a patient list.
	 */
    public PatientList getPatientListByStableId(String stableId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoPatientList.class);
            pstmt = con.prepareStatement
                    ("SELECT * FROM patient_list WHERE STABLE_ID = ?");
            pstmt.setString(1, stableId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                PatientList patientList = extractPatientList(rs);
                patientList.setPatientList(getPatientListList(patientList, con));
                return patientList;
            }
			return null;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoPatientList.class, con, pstmt, rs);
        }
    }

	/**
	 * Given a patient list ID, returns a patient list.
	 */
    public PatientList getPatientListById(int id) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoPatientList.class);
            pstmt = con.prepareStatement
                    ("SELECT * FROM patient_list WHERE LIST_ID = ?");
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                PatientList patientList = extractPatientList(rs);
				patientList.setPatientList(getPatientListList(patientList, con));
                return patientList;
            }
			return null;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoPatientList.class, con, pstmt, rs);
        }
    }

	/**
	 * Given a cancerStudyId, returns all patient list.
	 */
    public ArrayList<PatientList> getAllPatientLists( int cancerStudyId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoPatientList.class);

            pstmt = con.prepareStatement
                    ("SELECT * FROM patient_list WHERE CANCER_STUDY_ID = ? ORDER BY NAME");
            pstmt.setInt(1, cancerStudyId);
            rs = pstmt.executeQuery();
            ArrayList<PatientList> list = new ArrayList<PatientList>();
            while (rs.next()) {
                PatientList patientList = extractPatientList(rs);
                list.add(patientList);
            }
			// get patient list-list
			for (PatientList patientList : list) {
				patientList.setPatientList(getPatientListList(patientList, con));
			}
            return list;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoPatientList.class, con, pstmt, rs);
        }
    }

	/**
	 * Returns a list of all patient lists.
	 */
    public ArrayList<PatientList> getAllPatientLists() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoPatientList.class);
            pstmt = con.prepareStatement
                    ("SELECT * FROM patient_list");
            rs = pstmt.executeQuery();
            ArrayList<PatientList> list = new ArrayList<PatientList>();
            while (rs.next()) {
                PatientList patientList = extractPatientList(rs);
                list.add(patientList);
            }
			// get patient list-list
			for (PatientList patientList : list) {
				patientList.setPatientList(getPatientListList(patientList, con));
			}
            return list;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoPatientList.class, con, pstmt, rs);
        }
    }

	/**
	 * Clears all records from patient list & patient_list_list.
	 */
    public void deleteAllRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoPatientList.class);
            pstmt = con.prepareStatement("TRUNCATE TABLE patient_list");
            pstmt.executeUpdate();
            pstmt = con.prepareStatement("TRUNCATE TABLE patient_list_list");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoPatientList.class, con, pstmt, rs);
        }
    }

	/**
	 * Given a patient list, gets list id from patient_list table
	 */
	private int getPatientListId(PatientList patientList) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoPatientList.class);
            pstmt = con.prepareStatement("SELECT LIST_ID FROM patient_list WHERE STABLE_ID=?");
            pstmt.setString(1, patientList.getStableId());
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("LIST_ID");
            }
            return -1;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoPatientList.class, con, pstmt, rs);
        }
	}

	/**
	 * Adds record to patient_list_list.
	 */
    private int addPatientListList(PatientList patientList, Connection con) throws DaoException {
		
	// get patient list id
	int patientListId = getPatientListId(patientList);
	if (patientListId == -1) {
            return -1;
        }
        
        if (patientList.getPatientList().isEmpty()) {
            return 0;
        }

        PreparedStatement pstmt  ;
        ResultSet rs = null;
        try {
            StringBuilder sql = new StringBuilder("INSERT INTO patient_list_list (`LIST_ID`, `PATIENT_ID`) VALUES ");
            for (String patientId : patientList.getPatientList()) {
                Patient patient = DaoPatient.getPatientByCancerStudyAndPatientId(patientList.getCancerStudyId(), patientId);
                sql.append("('").append(patientListId).append("','").append(patient.getInternalId()).append("'),");
            }
            sql.deleteCharAt(sql.length()-1);
            pstmt = con.prepareStatement(sql.toString());
            return pstmt.executeUpdate();
        } catch (NullPointerException e) {
            throw new DaoException(e);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(rs);
        }
    }

	/**
	 * Given a patient list object (thus patient list id) gets patient list list.
	 */
	private ArrayList<String> getPatientListList(PatientList patientList, Connection con) throws DaoException {

        PreparedStatement pstmt  ;
        ResultSet rs = null;
        try {
            pstmt = con.prepareStatement
                    ("SELECT * FROM patient_list_list WHERE LIST_ID = ?");
            pstmt.setInt(1, patientList.getPatientListId());
            rs = pstmt.executeQuery();
            ArrayList<String> patientIds = new ArrayList<String>();
            while (rs.next()) {
                Patient patient = DaoPatient.getPatientById(rs.getInt("PATIENT_ID"));
				patientIds.add(patient.getStableId());
			}
            return patientIds;
        } catch (NullPointerException e) {
            throw new DaoException(e);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(rs);
        }
	}

	/**
	 * Given a result set, creates a patient list object.
	 */
    private PatientList extractPatientList(ResultSet rs) throws SQLException {
        PatientList patientList = new PatientList();
        patientList.setStableId(rs.getString("STABLE_ID"));
        patientList.setCancerStudyId(rs.getInt("CANCER_STUDY_ID"));
        patientList.setName(rs.getString("NAME"));
        patientList.setPatientListCategory(PatientListCategory.get(rs.getString("CATEGORY")));
        patientList.setDescription(rs.getString("DESCRIPTION"));
        patientList.setPatientListId(rs.getInt("LIST_ID"));
        return patientList;
    }
}