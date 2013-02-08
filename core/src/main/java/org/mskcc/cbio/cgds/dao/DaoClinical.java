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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
