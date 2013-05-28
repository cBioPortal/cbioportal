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
import org.mskcc.cbio.cgds.model.ClinicalAttribute;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Data Access Object for `clinical_attribute` table
 *
 * @author Gideon Dresdner
 */
public class DaoClinicalAttribute {

    public static int addDatum(ClinicalAttribute attr)  throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoClinicalAttribute.class);
            pstmt = con.prepareStatement
                    ("INSERT INTO clinical_attribute(" +
                            "`ATTR_ID`," +
                            "`DISPLAY_NAME`," +
                            "`DESCRIPTION`," +
                            "`DATATYPE`)" +
                            " VALUES(?,?,?,?)");
            pstmt.setString(1, attr.getAttrId());
            pstmt.setString(2, attr.getDisplayName());
            pstmt.setString(3, attr.getDescription());
            pstmt.setString(4, attr.getDatatype());

            int rows = pstmt.executeUpdate();
            return rows;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoClinicalAttribute.class, con, pstmt, rs);
        }
    }

    public static ClinicalAttribute unpack(ResultSet rs) throws SQLException {
        return new ClinicalAttribute(rs.getString("ATTR_ID"),
                rs.getString("DISPLAY_NAME"),
                rs.getString("DESCRIPTION"),
                rs.getString("DATATYPE"));
    }

    public static ClinicalAttribute getDatum(String attrId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoClinicalAttribute.class);

            pstmt = con.prepareStatement("SELECT * FROM clinical_attribute WHERE ATTR_ID=? ");
            pstmt.setString(1, attrId);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                return unpack(rs);
            } else {
                throw new DaoException(String.format("clinical attribute not found for (%s)", attrId));
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoClinicalAttribute.class, con, pstmt, rs);
        }
    }

    /**
     * Gets all the clinical attributes for a particular cancer study.
     * Looks in the clinical table for all records associated with the cancer study, extracts and uniques
     * the attribute ids.  Then those attribute ids are used to fetch the clinical attributes from the db.
     *
     * @param cancerStudy
     * @return
     * @throws DaoException
     */
    public static List<ClinicalAttribute> getDataByCancerStudy(CancerStudy cancerStudy) throws DaoException {
        int cancerStudyInternalId = cancerStudy.getInternalId();
        List<ClinicalAttribute> toReturn = new ArrayList<ClinicalAttribute>();

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoClinicalAttribute.class);

            pstmt = con.prepareStatement("SELECT DISTINCT ATTR_ID FROM clinical WHERE CANCER_STUDY_ID=?");
            pstmt.setInt(1,cancerStudyInternalId);

            rs = pstmt.executeQuery();  // list of attr_ids

            if (rs.next()) {
                String attrId = rs.getString("ATTR_ID");
                toReturn.add(getDatum(attrId));
            }

        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoClinicalAttribute.class, con, pstmt, rs);
        }

        return toReturn;
    }

    public static Collection<ClinicalAttribute> getAll() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Collection<ClinicalAttribute> all = new ArrayList<ClinicalAttribute>();

        try {
            con = JdbcUtil.getDbConnection(DaoClinicalAttribute.class);
            pstmt = con.prepareStatement("SELECT * FROM clinical_attribute");
            rs = pstmt.executeQuery();

            while (rs.next()) {
                all.add(unpack(rs));
            }

        } catch (SQLException e) {
            throw new DaoException(e);
        }
        return all;
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
            con = JdbcUtil.getDbConnection(DaoClinicalAttribute.class);
            pstmt = con.prepareStatement("TRUNCATE TABLE clinical_attribute");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoClinicalAttribute.class, con, pstmt, rs);
        }
    }
}
