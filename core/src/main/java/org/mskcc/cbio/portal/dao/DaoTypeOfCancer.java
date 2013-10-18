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

package org.mskcc.cbio.portal.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.mskcc.cbio.portal.model.TypeOfCancer;

/**
 * A TypeOfCancer is a clinical cancer type, such as Glioblastoma, Ovarian, etc.
 * Eventually, we'll have ontology problems with this, but initially the dbms
 * will be loaded from a file with a static table of types.
 * 
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 */
public class DaoTypeOfCancer {

   // these methods should be static, as this object has no state
   public static int addTypeOfCancer(TypeOfCancer typeOfCancer) throws DaoException {
      Connection con = null;
      PreparedStatement pstmt = null;
      ResultSet rs = null;
      try {
         con = JdbcUtil.getDbConnection(DaoTypeOfCancer.class);
         pstmt = con.prepareStatement("INSERT INTO type_of_cancer ( `TYPE_OF_CANCER_ID`, `NAME`, `CLINICAL_TRIAL_KEYWORDS`, `DEDICATED_COLOR`, `SHORT_NAME` ) VALUES (?,?,?,?,?)");
         pstmt.setString(1, typeOfCancer.getTypeOfCancerId());
         pstmt.setString(2, typeOfCancer.getName());
         pstmt.setString(3, typeOfCancer.getClinicalTrialKeywords());
         pstmt.setString(4, typeOfCancer.getDedicatedColor());
         pstmt.setString(5, typeOfCancer.getShortName());
         int rows = pstmt.executeUpdate();
         return rows;
      } catch (SQLException e) {
         throw new DaoException(e);
      } finally {
         JdbcUtil.closeAll(DaoTypeOfCancer.class, con, pstmt, rs);
      }
   }

   public static TypeOfCancer getTypeOfCancerById(String typeOfCancerId) throws DaoException {
      Connection con = null;
      PreparedStatement pstmt = null;
      ResultSet rs = null;
      try {
         con = JdbcUtil.getDbConnection(DaoTypeOfCancer.class);
         pstmt = con.prepareStatement("SELECT * FROM type_of_cancer WHERE TYPE_OF_CANCER_ID=?");
         pstmt.setString(1, typeOfCancerId);
         rs = pstmt.executeQuery();
         if (rs.next()) {
            TypeOfCancer typeOfCancer = extractTypeOfCancer(rs);
            return typeOfCancer;
         }
         return null;
      } catch (SQLException e) {
         throw new DaoException(e);
      } finally {
         JdbcUtil.closeAll(DaoTypeOfCancer.class, con, pstmt, rs);
      }
   }

   public static ArrayList<TypeOfCancer> getAllTypesOfCancer() throws DaoException {
      Connection con = null;
      PreparedStatement pstmt = null;
      ResultSet rs = null;
      try {
         con = JdbcUtil.getDbConnection(DaoTypeOfCancer.class);
         pstmt = con.prepareStatement("SELECT * FROM type_of_cancer");
         rs = pstmt.executeQuery();
         ArrayList<TypeOfCancer> list = new ArrayList<TypeOfCancer>();
         while (rs.next()) {
            TypeOfCancer typeOfCancer = extractTypeOfCancer(rs);
            list.add(typeOfCancer);
         }
         return list;
      } catch (SQLException e) {
         throw new DaoException(e);
      } finally {
         JdbcUtil.closeAll(DaoTypeOfCancer.class, con, pstmt, rs);
      }
   }

   public static int getCount() throws DaoException {
      Connection con = null;
      PreparedStatement pstmt = null;
      ResultSet rs = null;
      try {
         con = JdbcUtil.getDbConnection(DaoTypeOfCancer.class);
         pstmt = con.prepareStatement("SELECT COUNT(*) FROM type_of_cancer");
         rs = pstmt.executeQuery();
         if (rs.next()) {
            return rs.getInt(1);
         }
         return 0;
      } catch (SQLException e) {
         throw new DaoException(e);
      } finally {
         JdbcUtil.closeAll(DaoTypeOfCancer.class, con, pstmt, rs);
      }
   }

   public static void deleteAllRecords() throws DaoException {
      Connection con = null;
      PreparedStatement pstmt = null;
      ResultSet rs = null;
      try {
         con = JdbcUtil.getDbConnection(DaoTypeOfCancer.class);
         pstmt = con.prepareStatement("TRUNCATE TABLE type_of_cancer");
         pstmt.executeUpdate();
      } catch (SQLException e) {
         throw new DaoException(e);
      } finally {
         JdbcUtil.closeAll(DaoTypeOfCancer.class, con, pstmt, rs);
      }
   }

   public static void deleteTypeOfCancer(String typeOfCancerId) throws DaoException {
      Connection con = null;
      PreparedStatement pstmt = null;
      ResultSet rs = null;
      try {
         con = JdbcUtil.getDbConnection(DaoTypeOfCancer.class);
         pstmt = con.prepareStatement("DELETE from " + "type_of_cancer WHERE TYPE_OF_CANCER_ID=?");
         pstmt.setString(1, typeOfCancerId);
         pstmt.executeUpdate();
      } catch (SQLException e) {
         throw new DaoException(e);
      } finally {
         JdbcUtil.closeAll(DaoTypeOfCancer.class, con, pstmt, rs);
      }
   }

   private static TypeOfCancer extractTypeOfCancer(ResultSet rs) throws SQLException {
      TypeOfCancer typeOfCancer = new TypeOfCancer();

      typeOfCancer.setTypeOfCancerId(rs.getString("TYPE_OF_CANCER_ID"));
      typeOfCancer.setName(rs.getString("NAME"));
      typeOfCancer.setClinicalTrialKeywords(rs.getString("CLINICAL_TRIAL_KEYWORDS"));
      typeOfCancer.setDedicatedColor(rs.getString("DEDICATED_COLOR"));
      typeOfCancer.setShortName(rs.getString("SHORT_NAME"));

      return typeOfCancer;
   }
}
