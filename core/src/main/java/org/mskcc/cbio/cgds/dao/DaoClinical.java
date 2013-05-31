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

import com.google.common.base.Joiner;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mskcc.cbio.cgds.model.CancerStudy;
import org.mskcc.cbio.cgds.model.Clinical;
import org.mskcc.cbio.cgds.model.ClinicalAttribute;

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

    private static Log log = LogFactory.getLog(DaoClinical.class);

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
                return extract(rs);
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

    public static Clinical getDatum(String cancerStudyId, String caseId, String attrId) throws DaoException {
        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyId);

        return DaoClinical.getDatum(cancerStudy.getInternalId(), caseId, attrId);
    }

    public static Clinical getDatum(int cancerStudyId, String caseId)
            throws DaoException {

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = JdbcUtil.getDbConnection(DaoClinical.class);

            pstmt = con.prepareStatement("SELECT * FROM clinical WHERE " +
                    "CANCER_STUDY_ID=? " +
                    "AND CASE_ID=? ");

            pstmt.setInt(1, cancerStudyId);
            pstmt.setString(2, caseId);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                return extract(rs);
            } else {
                throw new DaoException(
                        String.format("clincial not found for (%d, %s, %s)", cancerStudyId, caseId));
            }

        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoClinical.class, con, pstmt, rs);
        }
    }

    /**
     * Query by cancer_study_id
     *
     * Looks up the corresponding <code>CancerStudy</code> object to get the database id
     *
     * @param cancerStudyId     String
     * @return
     * @throws DaoException
     */
    public static List<Clinical> getData(String cancerStudyId) throws DaoException {
        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyId);

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        List<Clinical> clinicals = new ArrayList<Clinical>();

        try {
            con = JdbcUtil.getDbConnection(DaoClinical.class);
            pstmt = con.prepareStatement("SELECT * FROM clinical WHERE CANCER_STUDY_ID=?");
            pstmt.setInt(1, cancerStudy.getInternalId());

           rs = pstmt.executeQuery();

            while(rs.next()) {
                clinicals.add(extract(rs));
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        }
        return clinicals;
    }

    /**
     * Generates a comma separated string of caseIds
     *
     * @param caseIds
     * @return
     */
    public static String generateCaseIdsSql(List<String> caseIds) {
        String caseIdsSql = "'" + StringUtils.join(caseIds, "','") + "'";

        return caseIdsSql;
    }

    /**
     * Get data for a list of case ids, for a particular cancer study
     * @param cancerStudyId
     * @param caseIds
     * @return
     */
    public static List<Clinical> getData(String cancerStudyId, List<String> caseIds) throws DaoException {
        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyId);

        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;

        List<Clinical> clinicals = new ArrayList<Clinical>();

        String caseIdsSql = generateCaseIdsSql(caseIds);

        String sql = "SELECT * FROM clinical WHERE `CANCER_STUDY_ID`=" + cancerStudy.getInternalId()
                + " " + "AND `CASE_ID` IN (" + caseIdsSql + ")";

        try {
            con = JdbcUtil.getDbConnection(DaoClinical.class);
            stmt = con.createStatement();
            rs = stmt.executeQuery(sql);
            while(rs.next()) {
                clinicals.add(extract(rs));
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        }
        return clinicals;
    }


    public static List<Clinical> getData(String cancerStudyId, List<String> caseIds, ClinicalAttribute attr) throws DaoException {
        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyId);

        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;

        List<Clinical> clinicals = new ArrayList<Clinical>();

        String caseIdsSql = generateCaseIdsSql(caseIds);

        String sql = "SELECT * FROM clinical WHERE"
                + "`CANCER_STUDY_ID`=" + "'" + cancerStudy.getInternalId() + "'"
                + " " + "AND `ATTR_ID`=" + "'" + attr.getAttrId() + "'"
                + " " + "AND `CASE_ID` IN (" + caseIdsSql + ")";

        try {
            con = JdbcUtil.getDbConnection(DaoClinical.class);
            stmt = con.createStatement();
            rs = stmt.executeQuery(sql);
            while(rs.next()) {
                clinicals.add(extract(rs));
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        }
        return clinicals;
    }

    /**
     * Turns a result set into a <code>Clinical</code> object
     *
     * returns null on failure to extract
     *
     * @param rs
     * @return
     * @throws SQLException
     */
    public static Clinical extract(ResultSet rs) throws SQLException {
            return new Clinical(rs.getInt("CANCER_STUDY_ID"),
                    rs.getString("CASE_ID"),
                    rs.getString("ATTR_ID"),
                    rs.getString("ATTR_VALUE"));
    }

    /**
     * Adds a list of <code>Clinical</code> objects into the db
     *
     * @param clinicals
     * @return int rows added
     * @throws DaoException
     */
    public static int addAllData(Collection<Clinical> clinicals) throws DaoException {
        Connection con = null;
        ResultSet rs = null;
        Statement stmt;

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
