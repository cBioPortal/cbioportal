package org.mskcc.cgds.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Data Access Objects for the Genetic Profile Cases Table.
 *
 * @author Ethan Cerami.
 */
public class DaoGeneticProfileCases {
    private static final String DELIM = ",";

    /**
     * Adds a new Ordered Case List for a Specified Genetic Profile ID.
     *
     * @param geneticProfileId  Genetic Profile ID.
     * @param orderedCaseList   Array List of Case IDs.
     * @return number of rows added.
     * @throws DaoException Data Access Exception.
     */
    public int addGeneticProfileCases(int geneticProfileId, ArrayList<String> orderedCaseList)
            throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        StringBuffer orderedCaseListBuf = new StringBuffer();
        //  Created Joined String, based on DELIM token
        for (String caseId:  orderedCaseList) {
            if (caseId.contains(DELIM)) {
                throw new IllegalArgumentException("Case ID cannot contain:  " + DELIM
                    + " --> " + caseId);
            }
            orderedCaseListBuf.append(caseId + DELIM);
        }
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("INSERT INTO genetic_profile_cases (`GENETIC_PROFILE_ID`, " +
                    "`ORDERED_CASE_LIST`) "+ "VALUES (?,?)");
            pstmt.setInt(1, geneticProfileId);
            pstmt.setString(2, orderedCaseListBuf.toString());
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    /**
     * Deletes all cases associated with the specified Genetic Profile ID.
     *
     * @param geneticProfileId Genetic Profile ID.
     * @throws DaoException Database Error.
     */
    public void deleteAllCasesInGeneticProfile(int geneticProfileId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement("DELETE from " +
                    "genetic_profile_cases WHERE GENETIC_PROFILE_ID=?");
            pstmt.setLong(1, geneticProfileId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    /**
     * Gets an Ordered Case List for the specified Genetic Profile ID.
     *
     * @param geneticProfileId Genetic Profile ID.
     * @return ArrayList of Case IDs.
     * @throws DaoException Database Error.
     */
    public ArrayList <String> getOrderedCaseList (int geneticProfileId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT * FROM genetic_profile_cases WHERE GENETIC_PROFILE_ID = ?");
            pstmt.setInt(1, geneticProfileId);
            rs = pstmt.executeQuery();
            if  (rs.next()) {
                String orderedCaseList = rs.getString("ORDERED_CASE_LIST");

                //  Split, based on DELIM token
                String parts[] = orderedCaseList.split(DELIM);
                ArrayList <String> caseList = new ArrayList <String>();
                for (String part:  parts) {
                    caseList.add(part);
                }
                return caseList;
            } else {
                return new ArrayList<String>();
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    /**
     * Deletes all Records in the table.
     * @throws DaoException Database Exception.
     */
    public void deleteAllRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement("TRUNCATE TABLE genetic_profile_cases");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }
}