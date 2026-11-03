/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.dao;

import java.sql.*;
import java.util.*;
import org.mskcc.cbio.portal.model.Sample;
import org.mskcc.cbio.portal.model.SampleList;
import org.mskcc.cbio.portal.model.SampleListCategory;

/**
 * Data access object for patient_List table
 */
public class DaoSampleList {

    private static final String DELETE_SAMPLE_LIST_LIST = "DELETE FROM sample_list_list WHERE `list_id` = ?";

    private static final String SAMPLE_LIST_SEQUENCE = "seq_sample_list";

    /**
	 * Adds record to sample_list table.
	 */
    public int addSampleList(SampleList sampleList) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        int rows;
        try {
            con = JdbcUtil.getDbConnection(DaoSampleList.class);

            long listId = ClickHouseAutoIncrement.nextId(SAMPLE_LIST_SEQUENCE);
            sampleList.setSampleListId((int) listId);
            pstmt = con.prepareStatement("INSERT INTO sample_list (`list_id`, `stable_id`, `cancer_study_id`, `name`, `category`," +
                    "`description`)" + " VALUES (?,?,?,?,?,?)");
            pstmt.setLong(1, listId);
            pstmt.setString(2, sampleList.getStableId());
            pstmt.setInt(3, sampleList.getCancerStudyId());
            pstmt.setString(4, sampleList.getName());
            pstmt.setString(5, sampleList.getSampleListCategory().getCategory());
            pstmt.setString(6, sampleList.getDescription());
            rows = pstmt.executeUpdate();
            addSampleListList(sampleList.getCancerStudyId(), (int) listId, sampleList.getSampleList(), con);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoSampleList.class, con, pstmt, null);
        }
        
        return rows;
    }

	/**
	 * Given a patient list by stable Id, returns a patient list.
	 */
    public SampleList getSampleListByStableId(String stableId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoSampleList.class);
            pstmt = con.prepareStatement
                    ("SELECT * FROM sample_list WHERE stable_id = ?");
            pstmt.setString(1, stableId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                SampleList sampleList = extractSampleList(rs);
                sampleList.setSampleList(getSampleListList(sampleList, con));
                return sampleList;
            }
			return null;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoSampleList.class, con, pstmt, rs);
        }
    }

	/**
	 * Given a patient list ID, returns a patient list.
	 */
    public SampleList getSampleListById(int id) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoSampleList.class);
            pstmt = con.prepareStatement
                    ("SELECT * FROM sample_list WHERE list_id = ?");
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                SampleList sampleList = extractSampleList(rs);
				sampleList.setSampleList(getSampleListList(sampleList, con));
                return sampleList;
            }
			return null;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoSampleList.class, con, pstmt, rs);
        }
    }

	/**
	 * Given a cancerStudyId, returns all patient list.
	 */
    public ArrayList<SampleList> getAllSampleLists( int cancerStudyId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoSampleList.class);

            pstmt = con.prepareStatement
                    ("SELECT * FROM sample_list WHERE cancer_study_id = ? ORDER BY name");
            pstmt.setInt(1, cancerStudyId);
            rs = pstmt.executeQuery();
            ArrayList<SampleList> list = new ArrayList<SampleList>();
            while (rs.next()) {
                SampleList sampleList = extractSampleList(rs);
                list.add(sampleList);
            }
			// get patient list-list
			for (SampleList sampleList : list) {
				sampleList.setSampleList(getSampleListList(sampleList, con));
			}
            return list;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoSampleList.class, con, pstmt, rs);
        }
    }

	/**
	 * Returns a list of all patient lists.
	 */
    public ArrayList<SampleList> getAllSampleLists() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoSampleList.class);
            pstmt = con.prepareStatement
                    ("SELECT * FROM sample_list");
            rs = pstmt.executeQuery();
            ArrayList<SampleList> list = new ArrayList<SampleList>();
            while (rs.next()) {
                SampleList sampleList = extractSampleList(rs);
                list.add(sampleList);
            }
			// get patient list-list
			for (SampleList sampleList : list) {
				sampleList.setSampleList(getSampleListList(sampleList, con));
			}
            return list;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoSampleList.class, con, pstmt, rs);
        }
    }

	/**
	 * Clears all records from patient list & sample_list_list.
	 */
    public void deleteAllRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoSampleList.class);
            pstmt = con.prepareStatement("TRUNCATE TABLE sample_list");
            pstmt.executeUpdate();
            pstmt = con.prepareStatement("TRUNCATE TABLE sample_list_list");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoSampleList.class, con, pstmt, rs);
        }
    }

	/**
	 * Adds record to sample_list_list.
	 */
    private int addSampleListList(int cancerStudyId, int sampleListId, List<String> sampleList, Connection con) throws DaoException {
		
        if (sampleList.isEmpty()) {
            return 0;
        }

        PreparedStatement pstmt  ;
        ResultSet rs = null;
        int skippedPatients = 0;
        try {
            StringBuilder sql = new StringBuilder("INSERT INTO sample_list_list (`list_id`, `sample_id`) VALUES ");
            // NOTE - as of 12/12/14, patient lists contain sample ids
            for (String sampleId : sampleList) {
                Sample sample = DaoSample.getSampleByCancerStudyAndSampleId(cancerStudyId, sampleId);
                if (sample == null) {
                    System.out.println("null sample: " + sampleId);
                    ++skippedPatients;
                    continue;
                }
                sql.append("('").append(sampleListId).append("','").append(sample.getInternalId()).append("'),");
            }
            if (skippedPatients == sampleList.size()) {
                return 0;
            }
            sql.deleteCharAt(sql.length()-1);
            pstmt = con.prepareStatement(sql.toString());
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(rs);
        }
    }

    public void updateSampleListList(SampleList sampleList) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            con = JdbcUtil.getDbConnection(DaoSampleList.class);
            pstmt = con.prepareStatement(DELETE_SAMPLE_LIST_LIST);
            pstmt.setInt(1, sampleList.getSampleListId());
            pstmt.executeUpdate();

            addSampleListList(sampleList.getCancerStudyId(), sampleList.getSampleListId(), sampleList.getSampleList(), con);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoSampleList.class, con, pstmt, null);
        }
    }


	/**
	 * Given a patient list object (thus patient list id) gets patient list list.
	 */
	private ArrayList<String> getSampleListList(SampleList sampleList, Connection con) throws DaoException {

        PreparedStatement pstmt  ;
        ResultSet rs = null;
        try {
            pstmt = con.prepareStatement
                    ("SELECT * FROM sample_list_list WHERE list_id = ?");
            pstmt.setInt(1, sampleList.getSampleListId());
            rs = pstmt.executeQuery();
            ArrayList<String> patientIds = new ArrayList<String>();
            while (rs.next()) {
                // NOTE - as of 12/12/14, patient lists contain sample ids
                int sample_id = rs.getInt("sample_id");
                Sample sample = DaoSample.getSampleById(sample_id);
				patientIds.add(sample.getStableId());
			}
            return patientIds;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(rs);
        }
	}

	/**
	 * Given a result set, creates a patient list object.
	 */
    private SampleList extractSampleList(ResultSet rs) throws SQLException {
        SampleList sampleList = new SampleList();
        sampleList.setStableId(rs.getString("stable_id"));
        sampleList.setCancerStudyId(rs.getInt("cancer_study_id"));
        sampleList.setName(rs.getString("name"));
        sampleList.setSampleListCategory(SampleListCategory.get(rs.getString("category")));
        sampleList.setDescription(rs.getString("description"));
        sampleList.setSampleListId(rs.getInt("list_id"));
        return sampleList;
    }
}
