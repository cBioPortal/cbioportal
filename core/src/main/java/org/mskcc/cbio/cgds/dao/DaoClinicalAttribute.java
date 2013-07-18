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

import com.google.inject.internal.Join;
import org.mskcc.cbio.cgds.model.CancerStudy;
import org.mskcc.cbio.cgds.model.ClinicalAttribute;

import java.sql.*;
import java.util.*;

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
     * Gets all the clinical attributes for a particular set of samples
     * Looks in the clinical table for all records associated with any of the samples, extracts and uniques
     * the attribute ids, then finally uses the attribute ids to fetch the clinical attributes from the db.
     *
     * @param sampleIdSet
     * @return
     * @throws DaoException
     */
    public static List<ClinicalAttribute> getDataBySamples(Set<String> sampleIdSet) throws DaoException {

        Iterator<String> sampleIdIterator = sampleIdSet.iterator();
        List<String> sampleIds = new ArrayList<String>();

        // convert to List
        while (sampleIdIterator.hasNext()) {
            sampleIds.add("\'" + sampleIdIterator.next());
        }

        String sampleIdsSql = Join.join("\',", sampleIds);      // add a quote to end of each
        sampleIdsSql += "\'";                                   // add a quote to end of the very last one
        List<ClinicalAttribute> toReturn = new ArrayList<ClinicalAttribute>();

        Connection con = null;
        ResultSet rs = null;
		PreparedStatement pstmt = null;
        String sql = "SELECT DISTINCT ATTR_ID FROM clinical WHERE `CASE_ID` IN ("
                + sampleIdsSql + ")";

        try {
            con = JdbcUtil.getDbConnection(DaoClinicalAttribute.class);
            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();

             while(rs.next()) {
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

	public static Map<String, String> getAllMap() throws DaoException {

		HashMap<String, String> toReturn = new HashMap<String, String>();
		for (ClinicalAttribute clinicalAttribute : DaoClinicalAttribute.getAll()) {
			toReturn.put(clinicalAttribute.getAttrId(), clinicalAttribute.getDisplayName());
		}
		return toReturn;
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
