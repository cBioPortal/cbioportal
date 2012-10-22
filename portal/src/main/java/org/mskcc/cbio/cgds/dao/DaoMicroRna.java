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

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Data access object for MicroRna table
 */
public class DaoMicroRna {

   // use a MySQLbulkLoader instead of SQL "INSERT" statements to load data into table
    private MySQLbulkLoader myMySQLbulkLoader = null;

   public int addMicroRna(String id, String variantId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
           if (MySQLbulkLoader.isBulkLoad()) {

              // use this code if bulk loading
              if( null == myMySQLbulkLoader ){
                 // create the MySQLbulkLoader if it doesn't exist
                 myMySQLbulkLoader = new MySQLbulkLoader( "micro_rna" );
              }

              // write to the temp file maintained by the MySQLbulkLoader 
              myMySQLbulkLoader.insertRecord( id, variantId);
              
              // return 1 because normal insert will return 1 if no error occurs
              return 1;
           } else {

              con = JdbcUtil.getDbConnection();
              pstmt = con.prepareStatement
                      ("INSERT INTO micro_rna (`ID`,`VARIANT_ID`) "
                              + "VALUES (?,?)");
              pstmt.setString(1, id);
              pstmt.setString(2, variantId);
              int rows = pstmt.executeUpdate();
              return rows;
              
           }
           
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

   /**
    * load the temp file maintained by the MySQLbulkLoader into the DMBS
    * @return
    * @throws DaoException
    */
    public int flushMicroRna() throws DaoException {
        try {
            return myMySQLbulkLoader.loadDataFromTempFileIntoDBMS();
        } catch (IOException e) {
            System.err.println("Could not open temp file");
            e.printStackTrace();
            return -1;
        }
    }
   
    public ArrayList<String> getVariantIds (String id) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT * FROM micro_rna WHERE ID = ?");
            pstmt.setString (1, id);
            rs = pstmt.executeQuery();
            ArrayList <String> variantIdList = new ArrayList<String>();
            while (rs.next()) {
                variantIdList.add(rs.getString("VARIANT_ID"));
            }
            return variantIdList;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    public HashSet<String> getEntireSet () throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        HashSet<String> microRnaIdSet = new HashSet<String>();
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT * FROM micro_rna");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                microRnaIdSet.add(rs.getString("ID"));
                microRnaIdSet.add(rs.getString("VARIANT_ID"));
            }
            return microRnaIdSet;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    public HashSet<String> getEntireVariantSet () throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        HashSet<String> variantMicroRnaSet = new HashSet<String>();
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT * FROM micro_rna");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                variantMicroRnaSet.add(rs.getString("VARIANT_ID"));
            }
            return variantMicroRnaSet;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    public void deleteAllRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement("TRUNCATE TABLE micro_rna");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }
}
