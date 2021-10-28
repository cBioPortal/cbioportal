package org.mskcc.cbio.portal.dao;

import java.sql.*;
import java.util.*;

import org.cbioportal.model.GenericEntityProperty;
import org.cbioportal.model.GeneticEntity;
import org.cbioportal.model.meta.GenericAssayMeta;

public class DaoGenericAssay {

    public static void setGenericEntityProperty(Integer entityId, String name, String value) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = JdbcUtil.getDbConnection(DaoGeneticEntity.class);
            pstmt = con.prepareStatement("INSERT INTO generic_entity_properties (`GENETIC_ENTITY_ID`, `NAME`, `VALUE`) "
            + "VALUES(?,?,?)");
            if (entityId == null) {
                return;
            }
            pstmt.setInt(1, entityId);
            pstmt.setString(2, name);
            pstmt.setString(3, value);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            JdbcUtil.closeAll(DaoGeneticEntity.class, con, pstmt, rs);
        }
    }

    public static void setGenericEntityPropertiesUsingBatch(List<GenericEntityProperty> properties) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        final int batchSize = 1000;

        try {
            con = JdbcUtil.getDbConnection(DaoGeneticEntity.class);
            pstmt = con.prepareStatement("INSERT INTO generic_entity_properties (`GENETIC_ENTITY_ID`, `NAME`, `VALUE`) "
                + "VALUES(?,?,?)");
            if (properties.size() == 0) {
                return;
            }

            // batch execution
            int count = 0;
            boolean preservedAutoCommitMode = con.getAutoCommit();
            con.setAutoCommit(false);
            for (GenericEntityProperty property : properties) {
                pstmt.setInt(1, property.getEntityId());
                pstmt.setString(2, property.getName());
                pstmt.setString(3, property.getValue());
                pstmt.addBatch();
                if (++count % batchSize == 0) {
                    pstmt.executeBatch();
                }
            }
            // insert remaining records
            pstmt.executeBatch();
            con.setAutoCommit(preservedAutoCommitMode);
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

    public static void deleteGenericEntityPropertiesByEntityId(Integer entityId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;     

        try {
            con = JdbcUtil.getDbConnection(DaoGeneticEntity.class);
            pstmt = con.prepareStatement("DELETE FROM generic_entity_properties WHERE GENETIC_ENTITY_ID=?");
            if (entityId == null) {
                return;
            }
            pstmt.setInt(1, entityId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            JdbcUtil.closeAll(DaoGeneticEntity.class, con, pstmt, rs);
        }
    }

    public static boolean geneticEntitiesOnlyExistInSingleStudy(int cancerStudyId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = JdbcUtil.getDbConnection(DaoGeneticEntity.class);
            pstmt = con.prepareStatement("SELECT DISTINCT CANCER_STUDY_ID FROM genetic_profile WHERE GENETIC_PROFILE_ID IN (SELECT GENETIC_PROFILE_ID FROM genetic_alteration WHERE GENETIC_ENTITY_ID IN (SELECT GENETIC_ENTITY_ID FROM genetic_alteration WHERE GENETIC_PROFILE_ID IN (SELECT GENETIC_PROFILE_ID FROM genetic_profile WHERE CANCER_STUDY_ID=? AND GENETIC_ALTERATION_TYPE='GENERIC_ASSAY')))");
            pstmt.setInt(1, cancerStudyId);
            rs = pstmt.executeQuery();

            List<Integer> studies = new ArrayList<Integer>();
            while(rs.next()) {
                studies.add(rs.getInt("CANCER_STUDY_ID"));
            }
            // check if entities only exist in single study
            return studies.size() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            JdbcUtil.closeAll(DaoGeneticEntity.class, con, pstmt, rs);
        }
        // do not update if there is an error
        return false;
    }
}
