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

package org.mskcc.cbio.portal.dao;

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
    public void deleteAllCasesInGeneticProfile(int geneticProfileId) throws DaoException {
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
    public ArrayList <String> getOrderedCaseList (int geneticProfileId) throws DaoException {
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
            JdbcUtil.closeAll(DaoGeneticProfileCases.class, con, pstmt, rs);
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