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
import java.util.ArrayList;import java.util.Arrays;

/**
 * Data Access Objects for the Genetic Profile Cases Table.
 *
 * @author Ethan Cerami.
 */
public final class DaoGeneticProfileCases {
    private DaoGeneticProfileCases() {}
    
    private static final String DELIM = ",";

    /**
     * Adds a new Ordered Case List for a Specified Genetic Profile ID.
     *
     * @param geneticProfileId  Genetic Profile ID.
     * @param orderedCaseList   Array List of Case IDs.
     * @return number of rows added.
     * @throws DaoException Data Access Exception.
     */
    public static int addGeneticProfileCases(int geneticProfileId, ArrayList<String> orderedCaseList)
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
            orderedCaseListBuf.append(caseId).append(DELIM);
        }
        try {
            con = JdbcUtil.getDbConnection(DaoGeneticProfileCases.class);
            pstmt = con.prepareStatement
                    ("INSERT INTO genetic_profile_cases (`GENETIC_PROFILE_ID`, " +
                    "`ORDERED_CASE_LIST`) "+ "VALUES (?,?)");
            pstmt.setInt(1, geneticProfileId);
            pstmt.setString(2, orderedCaseListBuf.toString());
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGeneticProfileCases.class, con, pstmt, rs);
        }
    }

    /**
     * Deletes all cases associated with the specified Genetic Profile ID.
     *
     * @param geneticProfileId Genetic Profile ID.
     * @throws DaoException Database Error.
     */
    public static void deleteAllCasesInGeneticProfile(int geneticProfileId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGeneticProfileCases.class);
            pstmt = con.prepareStatement("DELETE from " +
                    "genetic_profile_cases WHERE GENETIC_PROFILE_ID=?");
            pstmt.setLong(1, geneticProfileId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGeneticProfileCases.class, con, pstmt, rs);
        }
    }

    /**
     * Gets an Ordered Case List for the specified Genetic Profile ID.
     *
     * @param geneticProfileId Genetic Profile ID.
     * @return ArrayList of Case IDs.
     * @throws DaoException Database Error.
     */
    public static ArrayList <String> getOrderedCaseList (int geneticProfileId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGeneticProfileCases.class);
            pstmt = con.prepareStatement
                    ("SELECT * FROM genetic_profile_cases WHERE GENETIC_PROFILE_ID = ?");
            pstmt.setInt(1, geneticProfileId);
            rs = pstmt.executeQuery();
            if  (rs.next()) {
                String orderedCaseList = rs.getString("ORDERED_CASE_LIST");

                //  Split, based on DELIM token
                String parts[] = orderedCaseList.split(DELIM);
                ArrayList <String> caseList = new ArrayList <String>();
                caseList.addAll(Arrays.asList(parts));
                return caseList;
            } else {
                return new ArrayList<String>();
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGeneticProfileCases.class, con, pstmt, rs);
        }
    }

    /**
     * Deletes all Records in the table.
     * @throws DaoException Database Exception.
     */
    public static void deleteAllRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGeneticProfileCases.class);
            pstmt = con.prepareStatement("TRUNCATE TABLE genetic_profile_cases");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGeneticProfileCases.class, con, pstmt, rs);
        }
    }
}