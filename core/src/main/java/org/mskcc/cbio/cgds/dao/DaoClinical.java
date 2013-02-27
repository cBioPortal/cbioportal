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

import org.mskcc.cbio.cgds.model.Clinical;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Data Access Object for `clinical` table
 *
 * @author Gideon Dresdner dresdnerg@cbio.mskcc.org
 */

public final class DaoClinical {

    /**
     * add a new clinical datum
     *
     * @param cancerStudyId
     * @param caseId
     * @param attrId
     * @param attrVal
     * @return number of rows added to the database
     */
    public static int addDatum(int cancerStudyId,
                        String caseId,
                        String attrId,
                        String attrVal) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoClinical.class);
            pstmt = con.prepareStatement
                    ("INSERT INTO clinical(" +
                            "`CANCER_STUDY_ID`," +
                            "`CASE_ID`," +
                            "`ATTR_ID`," +
                            "`ATTR_VALUE`)" +
                            " VALUES(?,?,?,?)");
            pstmt.setInt(1, cancerStudyId);
            pstmt.setString(2, caseId);
            pstmt.setString(3, attrId);
            pstmt.setString(4, attrVal);

            int rows = pstmt.executeUpdate();
            return rows;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoClinical.class, con, pstmt, rs);
        }
    }

    public static Clinical getDatum(int cancerStudyId, String caseId, String attrId)
            throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoClinical.class);

            pstmt = con.prepareStatement("SELECT * FROM clinical WHERE " +
                    "CANCER_STUDY_ID=? " +
                    "AND CASE_ID=? " +
                    "AND ATTR_ID=?");

            pstmt.setInt(1, cancerStudyId);
            pstmt.setString(2, caseId);
            pstmt.setString(3, attrId);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                Clinical clinical = new Clinical(rs.getInt("CANCER_STUDY_ID"),
                        rs.getString("CASE_ID"),
                        rs.getString("ATTR_ID"),
                        rs.getString("ATTR_VALUE"));

                return clinical;
            } else {
                throw new DaoException(String.format("clincial not found for (%d, %s, %s)",
                        cancerStudyId, caseId, attrId));
            }

        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoClinical.class, con, pstmt, rs);
        }
    }

    /**
     * Get all clinical data associated with a sample id.
     *
     * Returns a <code>ResultSet</code>, a lazy sequence which then can be parsed using the extract method
     * @param sampleId
     * @return
     * @throws DaoException
     */
    public static ResultSet getBySampleId(String sampleId) throws DaoException {

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = JdbcUtil.getDbConnection(DaoClinical.class);

            pstmt = con.prepareStatement("SELECT * FROM clinical WHERE "
                    + "SAMPLE_ID=?");
            pstmt.setString(1, sampleId);

            rs = pstmt.executeQuery();

            return rs;

        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public static Clinical extract(ResultSet rs) throws SQLException {
        return new Clinical(rs.getInt("CANCER_STUDY_ID"),
                rs.getString("CASE_ID"),
                rs.getString("ATTR_ID"),
                rs.getString("ATTR_VALUE"));
    }

    public static int addAllData(Collection<Clinical> clinicals) throws DaoException {
        Connection con = null;
        ResultSet rs = null;
        Statement stmt = null;

        String sql = "INSERT INTO clinical (`CANCER_STUDY_ID`, `CASE_ID`, `ATTR_ID`, `ATTR_VALUE`)";
        sql += "VALUES";

        for (Clinical clinical : clinicals) {
            sql = sql + "(" +
                    "'" + clinical.getCancerStudyId() + "'," +
                    "'" + clinical.getCaseId() + "'," +
                    "'" + clinical.getAttrId() + "'," +
                    "'" + clinical.getAttrVal() + "'),";
        }
        sql = sql.substring(0, sql.length()-1); // get rid of that last comma
        try {
            con = JdbcUtil.getDbConnection(DaoClinical.class);
            stmt = con.createStatement();
            int rows = stmt.executeUpdate(sql);

            return rows;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoClinical.class, con, null, rs);
        }
    }

    /**
     * Deletes all Records.
     * @throws DaoException DAO Error.
     */
    public static void deleteAllRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoClinical.class);
            pstmt = con.prepareStatement("TRUNCATE TABLE clinical");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoClinical.class, con, pstmt, rs);
        }
    }
}
