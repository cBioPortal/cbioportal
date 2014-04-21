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
import java.util.HashSet;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author jj
 */
public class DaoProteinArrayTarget {
    // use a MySQLbulkLoader instead of SQL "INSERT" statements to load data into table
    private static DaoProteinArrayTarget daoProteinArrayTarget;

    /**
     * Private Constructor to enforce Singleton Pattern.
     */
    private DaoProteinArrayTarget() {
    }

    /**
     * Gets Global Singleton Instance.
     *
     * @return DaoProteinArrayTarget Singleton.
     * @throws DaoException Database Error.
     */
    public static DaoProteinArrayTarget getInstance() throws DaoException {
        if (daoProteinArrayTarget == null) {
            daoProteinArrayTarget = new DaoProteinArrayTarget();
        }
        return daoProteinArrayTarget;
    }

    /**
     * Adds a new ProteinArrayTarget Record to the Database.
     *
     * @return number of records successfully added.
     * @throws DaoException Database Error.
     */
    public int addProteinArrayTarget(String proteinArrayId, long entrezGeneId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoProteinArrayTarget.class);
            pstmt = con.prepareStatement
                    ("INSERT INTO protein_array_target (`PROTEIN_ARRAY_ID`,`ENTREZ_GENE_ID`) "
                            + "VALUES (?,?)");
            pstmt.setString(1, proteinArrayId);
            pstmt.setLong(2, entrezGeneId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoProteinArrayTarget.class, con, pstmt, rs);
        }
    }
    
    public int deleteProteinArrayTarget(String proteinArrayId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoProteinArrayTarget.class);
            pstmt = con.prepareStatement
                    ("DELETE FROM protein_array_target WHERE `PROTEIN_ARRAY_ID`=?");
            pstmt.setString(1, proteinArrayId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoProteinArrayTarget.class, con, pstmt, rs);
        }
    }
    
    public Collection<Long> getEntrezGeneIdOfArray(String arrayId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoProteinArrayTarget.class);
            pstmt = con.prepareStatement
                    ("SELECT ENTREZ_GENE_ID FROM protein_array_target "
                    + "WHERE PROTEIN_ARRAY_ID = ?");
            pstmt.setString(1, arrayId);
            
            Collection<Long> set = new HashSet<Long>();
            rs = pstmt.executeQuery();
            while (rs.next()) {
                set.add(rs.getLong(1));
            }
            
            return set;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoProteinArrayTarget.class, con, pstmt, rs);
        }
    }

    /**
     * Gets the list of protein array target with the Specified entrez gene ID.
     *
     * @param entrezId entrez gene ID.
     * @return ProteinArrayInfo Object.
     * @throws DaoException Database Error.
     */
    public Collection<String> getProteinArrayIds(long entrezId) throws DaoException {
        return getProteinArrayIds(Collections.singleton(entrezId));
    }

    /**
     * Gets the list of protein array target with the Specified entrez gene ID.
     *
     * @param entrezIds entrez gene ID.
     * @return ProteinArrayInfo Object.
     * @throws DaoException Database Error.
     */
    public Collection<String> getProteinArrayIds(Collection<Long> entrezIds) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoProteinArrayTarget.class);
            pstmt = con.prepareStatement
                    ("SELECT PROTEIN_ARRAY_ID FROM protein_array_target WHERE ENTREZ_GENE_ID in ("
                    +StringUtils.join(entrezIds,",")+")");
            
            Collection<String> set = new HashSet<String>();
            rs = pstmt.executeQuery();
            while (rs.next()) {
                set.add(rs.getString(1));
            }
            
            return set;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoProteinArrayTarget.class, con, pstmt, rs);
        }
    }

    /**
     * Deletes all protein array target Records in the Database.
     *
     * @throws DaoException Database Error.
     */
    public void deleteAllRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoProteinArrayTarget.class);
            pstmt = con.prepareStatement("TRUNCATE TABLE protein_array_target");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoProteinArrayTarget.class, con, pstmt, rs);
        }
    }
}
