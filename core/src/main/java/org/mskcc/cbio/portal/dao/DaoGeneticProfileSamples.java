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

import org.mskcc.cbio.portal.model.Sample;

import java.sql.*;
import java.util.*;

/**
 * Data Access Objects for the Genetic Profile Samples Table.
 *
 * @author Ethan Cerami.
 */
public final class DaoGeneticProfileSamples
{
    private static final String DELIM = ",";

    private DaoGeneticProfileSamples() {}

    /**
     * Adds a new Ordered Sample List for a Specified Genetic Profile ID.
     *
     * @param geneticProfileId  Genetic Profile ID.
     * @param orderedSampleList   Array List of Sample IDs.
     * @return number of rows added.
     * @throws DaoException Data Access Exception.
     */
    public static int addGeneticProfileSamples(int geneticProfileId, ArrayList<Integer> orderedSampleList)
            throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        StringBuffer orderedSampleListBuf = new StringBuffer();
        //  Created Joined String, based on DELIM token
        for (Integer sampleId :  orderedSampleList) {
            orderedSampleListBuf.append(Integer.toString(sampleId)).append(DELIM);
        }
        try {
            con = JdbcUtil.getDbConnection(DaoGeneticProfileSamples.class);
            pstmt = con.prepareStatement
                    ("INSERT INTO genetic_profile_samples (`GENETIC_PROFILE_ID`, " +
                    "`ORDERED_SAMPLE_LIST`) "+ "VALUES (?,?)");
            pstmt.setInt(1, geneticProfileId);
            pstmt.setString(2, orderedSampleListBuf.toString());
            return pstmt.executeUpdate();
        } catch (NullPointerException e) {
            throw new DaoException(e);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGeneticProfileSamples.class, con, pstmt, rs);
        }
    }

    /**
     * Deletes all samples associated with the specified Genetic Profile ID.
     *
     * @param geneticProfileId Genetic Profile ID.
     * @throws DaoException Database Error.
     */
    public static void deleteAllSamplesInGeneticProfile(int geneticProfileId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGeneticProfileSamples.class);
            pstmt = con.prepareStatement("DELETE from " +
                    "genetic_profile_samples WHERE GENETIC_PROFILE_ID=?");
            pstmt.setLong(1, geneticProfileId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGeneticProfileSamples.class, con, pstmt, rs);
        }
    }

    /**
     * Gets an Ordered Sample List for the specified Genetic Profile ID.
     *
     * @param geneticProfileId Genetic Profile ID.
     * @throws DaoException Database Error.
     */
    public static ArrayList <Integer> getOrderedSampleList(int geneticProfileId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGeneticProfileSamples.class);
            pstmt = con.prepareStatement
                    ("SELECT * FROM genetic_profile_samples WHERE GENETIC_PROFILE_ID = ?");
            pstmt.setInt(1, geneticProfileId);
            rs = pstmt.executeQuery();
            if  (rs.next()) {
                String orderedSampleList = rs.getString("ORDERED_SAMPLE_LIST");

                //  Split, based on DELIM token
                String parts[] = orderedSampleList.split(DELIM);
                ArrayList <Integer> sampleList = new ArrayList <Integer>();
                for (String internalSampleId : parts) {
                    sampleList.add(Integer.parseInt(internalSampleId));
                }
                return sampleList;
            } else {
                return new ArrayList<Integer>();
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGeneticProfileSamples.class, con, pstmt, rs);
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
            con = JdbcUtil.getDbConnection(DaoGeneticProfileSamples.class);
            pstmt = con.prepareStatement("TRUNCATE TABLE genetic_profile_samples");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGeneticProfileSamples.class, con, pstmt, rs);
        }
    }
}
