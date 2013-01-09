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

import org.mskcc.cbio.cgds.model.ClinicalAttribute;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Data Access Object for `clinical_attribute` table
 *
 * @author Gideon Dresdner
 */
public class DaoClinicalAttribute {

    public int addDatum(String attrId,
                        String displayName,
                        String description,
                        String datatype)  throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("INSERT INTO clinical_attribute(" +
                            "`ATTR_ID`," +
                            "`DISPLAY_NAME`," +
                            "`DESCRIPTION`," +
                            "`DATATYPE`)" +
                            " VALUES(?,?,?,?)");
            pstmt.setString(1, attrId);
            pstmt.setString(2, displayName);
            pstmt.setString(3, description);
            pstmt.setString(4, datatype);

            int rows = pstmt.executeUpdate();
            return rows;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    public ClinicalAttribute getDatum(String attrId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();

            pstmt = con.prepareStatement("SELECT * FROM clinical_attribute WHERE ATTR_ID=? ");
            pstmt.setString(1, attrId);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                ClinicalAttribute clinicalAttribute = new ClinicalAttribute(rs.getString("ATTR_ID"),
                        rs.getString("DISPLAY_NAME"),
                        rs.getString("DESCRIPTION"),
                        rs.getString("DATATYPE"));
                return clinicalAttribute;
            } else {
                throw new DaoException(String.format("clincial attribute not found for (%s)", attrId));
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    /**
     * Deletes all Records.
     * @throws DaoException DAO Error.
     */
    public void deleteAllRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement("TRUNCATE TABLE clinical_attribute");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }
}
