/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.dao;

import java.sql.*;
import java.util.*;
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
         pstmt = con.prepareStatement("INSERT INTO type_of_cancer ( `type_of_cancer_id`, `name`, `dedicated_color`, `short_name`, `parent` ) VALUES (?,?,?,?,?)");
         pstmt.setString(1, typeOfCancer.getTypeOfCancerId());
         pstmt.setString(2, typeOfCancer.getName());
         pstmt.setString(3, typeOfCancer.getDedicatedColor());
         pstmt.setString(4, typeOfCancer.getShortName());
         pstmt.setString(5, typeOfCancer.getParentTypeOfCancerId());
         return pstmt.executeUpdate();
      } catch (SQLException e) {
         throw new DaoException(e);
      } finally {
         JdbcUtil.closeAll(DaoTypeOfCancer.class, con, pstmt, rs);
      }
   }

   public static TypeOfCancer getTypeOfCancerById(String typeOfCancerId) throws DaoException {
      if (typeOfCancerId == null) {
         return null;
      }
      Connection con = null;
      PreparedStatement pstmt = null;
      ResultSet rs = null;
      try {
         con = JdbcUtil.getDbConnection(DaoTypeOfCancer.class);
         pstmt = con.prepareStatement("SELECT * FROM type_of_cancer WHERE LOWER(type_of_cancer_id)=?");
         pstmt.setString(1, typeOfCancerId.toLowerCase());
         rs = pstmt.executeQuery();
         if (rs.next()) {
            return extractTypeOfCancer(rs);
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
         pstmt = con.prepareStatement("SELECT count(*) FROM type_of_cancer");
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

    /**
     * Deletes all records from DB using TRUNCATE statement!
     * 
     * @throws DaoException
     * 
     * @deprecated do not use this! Use deleteAllTypesOfCancer instead
     */
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
    
    /**
     * Deletes all records from DB using DELETE statement.
     * 
     * @param typeOfCancerId
     * @throws DaoException
     */
    public static void deleteAllTypesOfCancer() throws DaoException {
        for (TypeOfCancer typeOfCancer : getAllTypesOfCancer()) {
            deleteTypeOfCancer(typeOfCancer.getTypeOfCancerId());
        }
    }

   public static void deleteTypeOfCancer(String typeOfCancerId) throws DaoException {
      Connection con = null;
      PreparedStatement pstmt = null;
      ResultSet rs = null;
      try {
         con = JdbcUtil.getDbConnection(DaoTypeOfCancer.class);
         pstmt = con.prepareStatement("DELETE from " + "type_of_cancer WHERE type_of_cancer_id=?");
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

      typeOfCancer.setTypeOfCancerId(rs.getString("type_of_cancer_id"));
      typeOfCancer.setName(rs.getString("name"));
      typeOfCancer.setDedicatedColor(rs.getString("dedicated_color"));
      typeOfCancer.setShortName(rs.getString("short_name"));
      typeOfCancer.setParentTypeOfCancerId(rs.getString("parent"));
      
      return typeOfCancer;
   }
}
