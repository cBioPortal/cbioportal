package org.mskcc.cbio.portal.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import org.cbioportal.model.GeneticEntity;
import org.cbioportal.model.meta.GenericAssayMeta;
import org.mskcc.cbio.portal.util.ProgressMonitor;

public class DaoGenericAssay {
    private static Map<String, GenericAssayMeta> genericAssayMetaMap = initMap();

    // public static GenericAssayMeta getGenericAssayMetaByStableId(String stableId) {
    //     if (genericAssayMetaMap != null && genericAssayMetaMap.containsKey(stableId)) {
    //         return genericAssayMetaMap.get(stableId);
    //     }
    //     else {
    //         return null;
    //     }
    // }

    public static void setGenericEntityProperty(String stableId, String name, String value) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = JdbcUtil.getDbConnection(DaoGeneticEntity.class);
            pstmt = con.prepareStatement("INSERT INTO generic_entity_properties (`GENETIC_ENTITY_ID`, `NAME`, `VALUE`) "
            + "VALUES(?,?,?)");
            GeneticEntity entity = DaoGeneticEntity.getGeneticEntityByStableId(stableId);
            if (entity == null) {
                return;
            }
            pstmt.setInt(1, entity.getId());
            pstmt.setString(2, name);
            pstmt.setString(3, value);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            JdbcUtil.closeAll(DaoGeneticEntity.class, con, pstmt, rs);
        }
    }

    public static GenericAssayMeta getGenericAssayMetaByStableId(String stableId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = JdbcUtil.getDbConnection(DaoGeneticEntity.class);
            pstmt = con.prepareStatement("SELECT * FROM generic_entity_properties WHERE GENETIC_ENTITY_ID=?");
            GeneticEntity entity = DaoGeneticEntity.getGeneticEntityByStableId(stableId);
            if (entity == null) {
                return null;
            }
            pstmt.setInt(1, entity.getId());
            rs = pstmt.executeQuery();

            HashMap<String, String> map = new HashMap<>();
            while(rs.next()) {
                map.put(rs.getString("NAME"), rs.getString("VALUE"));
            }
            GenericAssayMeta genericAssayMeta = new GenericAssayMeta(entity.getEntityType(), entity.getStableId(), map);
            return genericAssayMeta;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            JdbcUtil.closeAll(DaoGeneticEntity.class, con, pstmt, rs);
        }
        return null;
    }

    public static void deleteGenericEntityPropertiesByStableId(String stableId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;     

        try {
            con = JdbcUtil.getDbConnection(DaoGeneticEntity.class);
            pstmt = con.prepareStatement("DELETE FROM generic_entity_properties WHERE GENETIC_ENTITY_ID=?");
            GeneticEntity entity = DaoGeneticEntity.getGeneticEntityByStableId(stableId);
            if (entity == null) {
                return;
            }
            pstmt.setInt(1, entity.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            JdbcUtil.closeAll(DaoGeneticEntity.class, con, pstmt, rs);
        }
    }

    private static Map<String, GenericAssayMeta> initMap() {
        Map<String, GenericAssayMeta> genericAssayMetaMap = null;
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoGeneticEntity.class);
            pstmt = con.prepareStatement("SELECT * FROM generic_entity_properties");
            rs = pstmt.executeQuery();
            genericAssayMetaMap = extractGenericAssayMetaMap(rs);
        } catch (SQLException | DaoException e) {
            e.printStackTrace();
        } finally {
            JdbcUtil.closeAll(DaoGeneticEntity.class, con, pstmt, rs);
        }
        return genericAssayMetaMap;
    }

    private static Map<String, GenericAssayMeta> extractGenericAssayMetaMap(ResultSet rs) throws SQLException, DaoException {
        Map<String, GenericAssayMeta> genericAssayMetaMap = new HashMap<>();
        try {
            while(rs.next()) {
                int entityId = rs.getInt("GENETIC_ENTITY_ID");
                GeneticEntity geneticEntity = DaoGeneticEntity.getGeneticEntityById(entityId);
    
                String entityType = geneticEntity.getEntityType();
                String stableId = geneticEntity.getStableId();
                String name = rs.getString("NAME");
                String value = rs.getString("VALUE");
                
                if (genericAssayMetaMap.containsKey(stableId)) {
                    genericAssayMetaMap.get(stableId).getGenericEntityMetaProperties().put(name, value);
                }
                else {
                    HashMap<String, String> map = new HashMap<>();
                    map.put(name, value);
                    GenericAssayMeta genericAssayMeta = new GenericAssayMeta(entityType, stableId, map);
                    genericAssayMetaMap.put(stableId, genericAssayMeta);
                }
            }
            return genericAssayMetaMap;
        }
        catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoGene.class, null, null, rs);
        }
    }

}