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

import org.mskcc.cbio.cgds.model.CancerStudy;
import org.mskcc.cbio.cgds.model.TypeOfCancer;

import java.sql.*;
import java.util.ArrayList;

/**
 * Analogous to and replaces the old DaoCancerType. A CancerStudy has a NAME and
 * DESCRIPTION. If PUBLIC is true a CancerStudy can be accessed by anyone,
 * otherwise can only be accessed through access control.
 *
 * @author Ethan Cerami
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 */
public class DaoCancerStudy {

    /**
     * Adds a cancer study to the Database.
     * Updates cancerStudy with its auto incremented uid, in studyID.
     *
     * @param cancerStudy   Cancer Study Object.
     * @throws DaoException Database Error.
     */
    public static void addCancerStudy(CancerStudy cancerStudy) throws DaoException {

        // make sure that cancerStudy refers to a valid TypeOfCancerId
        // TODO: have a foreign key constraint do this; why not?
        TypeOfCancer aTypeOfCancer = DaoTypeOfCancer.getTypeOfCancerById
                (cancerStudy.getTypeOfCancerId());
        if (null == aTypeOfCancer) {
            throw new DaoException("cancerStudy.getTypeOfCancerId() '"
                    + cancerStudy.getTypeOfCancerId()
                    + "' does not refer to a TypeOfCancer.");
        }

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            // CANCER_STUDY_IDENTIFIER may be null
            if (cancerStudy.getCancerStudyStableId() != null) {
                pstmt = con.prepareStatement("INSERT INTO cancer_study " +
                        "( `CANCER_STUDY_IDENTIFIER`, `NAME`, "
                        + "`DESCRIPTION`, `PUBLIC`, `TYPE_OF_CANCER_ID`, "
                        + "`PMID`, `CITATION` ) VALUES (?,?,?,?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS);
                pstmt.setString(1, cancerStudy.getCancerStudyStableId());
                pstmt.setString(2, cancerStudy.getName());
                pstmt.setString(3, cancerStudy.getDescription());
                pstmt.setBoolean(4, cancerStudy.isPublicStudy());
                pstmt.setString(5, cancerStudy.getTypeOfCancerId());
                pstmt.setString(6, cancerStudy.getPmid());
                pstmt.setString(7, cancerStudy.getCitation());
            } else {
                pstmt = con.prepareStatement("INSERT INTO cancer_study ( `NAME`, "
                        + "`DESCRIPTION`, `PUBLIC`, `TYPE_OF_CANCER_ID`, "
                        + "`PMID`, `CITATION` ) VALUES (?,?,?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS);
                pstmt.setString(1, cancerStudy.getName());
                pstmt.setString(2, cancerStudy.getDescription());
                pstmt.setBoolean(3, cancerStudy.isPublicStudy());
                pstmt.setString(4, cancerStudy.getTypeOfCancerId());
                pstmt.setString(5, cancerStudy.getPmid());
                pstmt.setString(6, cancerStudy.getCitation());
            }
            pstmt.executeUpdate();
            rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                int autoId = rs.getInt(1);
                cancerStudy.setInternalId(autoId);
            }

        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    /**
     * Return the cancerStudy identified by the internal cancer study ID, if it exists.
     *
     * @param cancerStudyID     Internal (int) Cancer Study ID.
     * @return Cancer Study Object, or null if there's no such study.
     * @throws DaoException Database Error.
     */
    public static CancerStudy getCancerStudyByInternalId(int cancerStudyID) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement("SELECT * FROM cancer_study WHERE CANCER_STUDY_ID = ?");
            pstmt.setInt(1, cancerStudyID);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                CancerStudy cancerStudy = extractCancerStudy(rs);
                return cancerStudy;
            }
            return null;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    /**
     * Returns the cancerStudy identified by the stable identifier, if it exists.
     *
     * @param cancerStudyStableId Cancer Study Stable ID.
     * @return the CancerStudy, or null if there's no such study.
     * @throws DaoException Database Error.
     */
    public static CancerStudy getCancerStudyByStableId(String cancerStudyStableId)
            throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement("SELECT * FROM cancer_study " +
                    "WHERE CANCER_STUDY_IDENTIFIER=?");
            pstmt.setString(1, cancerStudyStableId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                CancerStudy cancerStudy = extractCancerStudy(rs);
                return cancerStudy;
            }
            return null;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    /**
     * Indicates whether the cancerStudy identified by the stable ID exists.
     *
     * @param cancerStudyStableId Cancer Study Stable ID.
     * @return true if the CancerStudy exists, otherwise false
     * @throws DaoException Database Error.
     */
    public static boolean doesCancerStudyExistByStableId(String cancerStudyStableId)
            throws DaoException {
        CancerStudy cancerStudy = getCancerStudyByStableId(cancerStudyStableId);
        return (null != cancerStudy);
    }

    /**
     * Indicates whether the cancerStudy identified by internal study ID exist.
     * does no access control, so only returns a boolean.
     *
     * @param internalCancerStudyId Internal Cancer Study ID.
     * @return true if the CancerStudy exists, otherwise false
     * @throws DaoException Database Error.
     */
    public static boolean doesCancerStudyExistByInternalId(int internalCancerStudyId) throws DaoException {
        CancerStudy cancerStudy = getCancerStudyByInternalId(internalCancerStudyId);
        return (null != cancerStudy);
    }

    /**
     * Returns all the cancerStudies.
     *
     * @return ArrayList of all CancerStudy Objects.
     * @throws DaoException Database Error.
     */
    public static ArrayList<CancerStudy> getAllCancerStudies() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement("SELECT * FROM cancer_study");
            rs = pstmt.executeQuery();
            ArrayList<CancerStudy> list = new ArrayList<CancerStudy>();
            while (rs.next()) {
                CancerStudy cancerStudy = extractCancerStudy(rs);
                list.add(cancerStudy);
            }
            return list;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    /**
     * Gets Number of Cancer Studies.
     * @return number of cancer studies.
     * @throws DaoException Database Error.
     */
    public static int getCount() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement("SELECT COUNT(*) FROM cancer_study");
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    /**
     * Deletes all Cancer Studies.
     * @throws DaoException Database Error.
     */
    public static void deleteAllRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement("TRUNCATE TABLE cancer_study");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    /**
     * Deletes the Specified Cancer Study.
     *
     * @param internalCancerStudyId Internal Cancer Study ID.
     * @throws DaoException Database Error.
     */
    public static void deleteCancerStudy(int internalCancerStudyId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement("DELETE from " + "cancer_study WHERE CANCER_STUDY_ID=?");
            pstmt.setInt(1, internalCancerStudyId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    /**
     * Extracts Cancer Study JDBC Results.
     */
    private static CancerStudy extractCancerStudy(ResultSet rs) throws SQLException {
        CancerStudy cancerStudy = new CancerStudy(rs.getString("NAME"),
                rs.getString("DESCRIPTION"),
                rs.getString("CANCER_STUDY_IDENTIFIER"),
                rs.getString("TYPE_OF_CANCER_ID"),
                rs.getBoolean("PUBLIC"));
        cancerStudy.setPmid(rs.getString("PMID"));
        cancerStudy.setCitation(rs.getString("CITATION"));

        cancerStudy.setInternalId(rs.getInt("CANCER_STUDY_ID"));
        return cancerStudy;
    }
}
