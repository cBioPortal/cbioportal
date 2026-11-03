package org.mskcc.cbio.portal.dao;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.mskcc.cbio.portal.model.shared.GenericAssayMeta;
import org.mskcc.cbio.portal.model.shared.GenericEntityProperty;
import org.mskcc.cbio.portal.model.shared.GeneticAlterationType;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.model.shared.GeneticEntity;

public class DaoGenericAssay {

    public static void setGenericEntityProperty(Integer entityId, String name, String value) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = JdbcUtil.getDbConnection(DaoGeneticEntity.class);
            long propertyId = ClickHouseAutoIncrement.nextId("seq_generic_entity_properties");
            pstmt = con.prepareStatement("INSERT INTO generic_entity_properties (`id`, `genetic_entity_id`, `name`, `value`) "
            + "VALUES(?,?,?,?)");
            if (entityId == null) {
                return;
            }
            pstmt.setLong(1, propertyId);
            pstmt.setInt(2, entityId);
            pstmt.setString(3, name);
            pstmt.setString(4, value);
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
            pstmt = con.prepareStatement("INSERT INTO generic_entity_properties (`id`, `genetic_entity_id`, `name`, `value`) "
                + "VALUES(?,?,?,?)");
            if (properties.size() == 0) {
                return;
            }

            // batch execution
            int count = 0;
            boolean preservedAutoCommitMode = con.getAutoCommit();
            con.setAutoCommit(false);
            for (GenericEntityProperty property : properties) {
                long propertyId = ClickHouseAutoIncrement.nextId("seq_generic_entity_properties");
                pstmt.setLong(1, propertyId);
                pstmt.setInt(2, property.getEntityId());
                pstmt.setString(3, property.getName());
                pstmt.setString(4, property.getValue());
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
            pstmt = con.prepareStatement("SELECT * FROM generic_entity_properties WHERE genetic_entity_id=?");
            GeneticEntity entity = DaoGeneticEntity.getGeneticEntityByStableId(stableId);
            if (entity == null) {
                return null;
            }
            pstmt.setInt(1, entity.getId());
            rs = pstmt.executeQuery();

            HashMap<String, String> map = new HashMap<>();
            while(rs.next()) {
                map.put(rs.getString("name"), rs.getString("value"));
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
            pstmt = con.prepareStatement("DELETE FROM generic_entity_properties WHERE genetic_entity_id=?");
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

    public static boolean geneticEntitiesOnlyExistInSingleStudy(int geneticProfileId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = JdbcUtil.getDbConnection(DaoGeneticEntity.class);
            pstmt = con.prepareStatement("SELECT DISTINCT cancer_study_id FROM genetic_profile WHERE genetic_profile_id IN (SELECT genetic_profile_id FROM genetic_alteration WHERE genetic_entity_id IN (SELECT genetic_entity_id FROM genetic_alteration WHERE genetic_profile_id=?))");
            pstmt.setInt(1, geneticProfileId);
            rs = pstmt.executeQuery();

            List<Integer> studies = new ArrayList<Integer>();
            while(rs.next()) {
                studies.add(rs.getInt("cancer_study_id"));
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

    /**
     * delete generic assay meta records if meta is not shared with other studies
     * check and delete generic assay meta records profile by profile
     * @throws DaoException
     */
    public static void checkAndDeleteGenericAssayMetaInStudy(int internalCancerStudyId) throws DaoException {
        List<GeneticProfile> geneticProfiles = DaoGeneticProfile.getAllGeneticProfiles(internalCancerStudyId);
        List<GeneticProfile> genericAssayProfiles = geneticProfiles.stream().filter(profile -> profile.getGeneticAlterationType() == GeneticAlterationType.GENERIC_ASSAY).collect(Collectors.toList());
        List<Pair<Integer, String>> deleteGenericAssayStatements = new ArrayList<>();

        for (GeneticProfile profile : genericAssayProfiles) {
            if (DaoGenericAssay.geneticEntitiesOnlyExistInSingleStudy(profile.getGeneticProfileId())) {
                deleteGenericAssayStatements.add(Pair.of(profile.getGeneticProfileId(), "DELETE FROM generic_entity_properties WHERE genetic_entity_id IN (SELECT genetic_entity_id FROM genetic_alteration WHERE genetic_profile_id=?)"));
                deleteGenericAssayStatements.add(Pair.of(profile.getGeneticProfileId(), "DELETE FROM genetic_entity WHERE id IN (SELECT genetic_entity_id FROM genetic_alteration WHERE genetic_profile_id=?)"));
            }
        }

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = JdbcUtil.getDbConnection(DaoCancerStudy.class);
            for (Pair<Integer, String> profileIdAndStatementPair : deleteGenericAssayStatements) {
                pstmt = con.prepareStatement(profileIdAndStatementPair.getValue());
                if (profileIdAndStatementPair.getValue().contains("?")) {
                    pstmt.setInt(1, profileIdAndStatementPair.getKey());
                }
                pstmt.executeUpdate();
                pstmt.close();
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCancerStudy.class, con, pstmt, rs);
        }
    }
}
