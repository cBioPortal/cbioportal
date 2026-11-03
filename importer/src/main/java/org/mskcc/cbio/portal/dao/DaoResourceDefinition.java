package org.mskcc.cbio.portal.dao;

import java.sql.*;
import java.util.*;
import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.portal.model.ResourceDefinition;
import org.mskcc.cbio.portal.model.shared.ResourceType;

/**
 * Data Access Object for `resource_definition` table
 */
public class DaoResourceDefinition {

    public static int addDatum(ResourceDefinition resource) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoResourceDefinition.class);
            pstmt = con.prepareStatement("INSERT INTO resource_definition(" + "`resource_id`," + "`display_name`,"
                    + "`description`," + "`resource_type`," + "`open_by_default`," + "`priority`," + "`cancer_study_id`," + "`custom_metadata`)"
                    + " VALUES(?,?,?,?,?,?,?,?)");
            pstmt.setString(1, resource.getResourceId());
            pstmt.setString(2, resource.getDisplayName());
            pstmt.setString(3, resource.getDescription());
            pstmt.setString(4, resource.getResourceType().name());
            pstmt.setBoolean(5, resource.isOpenByDefault());
            pstmt.setInt(6, resource.getPriority());
            pstmt.setInt(7, resource.getCancerStudyId());
            pstmt.setString(8, resource.getCustomMetadata());
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoResourceDefinition.class, con, pstmt, rs);
        }
    }

    private static ResourceDefinition unpack(ResultSet rs) throws SQLException {
        return new ResourceDefinition(rs.getString("resource_id"), rs.getString("display_name"), rs.getString("description"),
                ResourceType.valueOf(rs.getString("resource_type")), rs.getBoolean("open_by_default"), rs.getInt("priority"),
                rs.getInt("cancer_study_id"), rs.getString("custom_metadata"));
    }

    public static ResourceDefinition getDatum(String resourceId, Integer cancerStudyId) throws DaoException {
        List<ResourceDefinition> resources = getDatum(Arrays.asList(resourceId), cancerStudyId);
        if (resources.isEmpty()) {
            return null;
        }

        return resources.get(0);
    }

    public static List<ResourceDefinition> getDatum(Collection<String> resourceIds, Integer cancerStudyId)
            throws DaoException {
        if (resourceIds == null || resourceIds.isEmpty()) {
            return Collections.emptyList();
        }
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoResourceDefinition.class);

            pstmt = con.prepareStatement("SELECT * FROM resource_definition WHERE resource_id IN ('"
                    + StringUtils.join(resourceIds, "','") + "')  AND cancer_study_id=" + String.valueOf(cancerStudyId));

            rs = pstmt.executeQuery();

            List<ResourceDefinition> list = new ArrayList<ResourceDefinition>();
            while (rs.next()) {
                list.add(unpack(rs));
            }

            return list;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoResourceDefinition.class, con, pstmt, rs);
        }
    }

    public static List<ResourceDefinition> getDatum(Collection<String> resourceIds) throws DaoException {
        if (resourceIds == null || resourceIds.isEmpty()) {
            return Collections.emptyList();
        }
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoResourceDefinition.class);

            pstmt = con.prepareStatement("SELECT * FROM resource_definition WHERE resource_id IN ('"
                    + StringUtils.join(resourceIds, "','") + "')");

            rs = pstmt.executeQuery();

            List<ResourceDefinition> list = new ArrayList<ResourceDefinition>();
            while (rs.next()) {
                list.add(unpack(rs));
            }

            return list;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoResourceDefinition.class, con, pstmt, rs);
        }
    }

    public static List<ResourceDefinition> getDatumByStudy(int cancerStudyId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoResourceDefinition.class);

            pstmt = con.prepareStatement("SELECT * FROM resource_definition WHERE cancer_study_id=" + String.valueOf(cancerStudyId));

            rs = pstmt.executeQuery();

            List<ResourceDefinition> list = new ArrayList<ResourceDefinition>();
            while (rs.next()) {
                list.add(unpack(rs));
            }

            return list;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoResourceDefinition.class, con, pstmt, rs);
        }
    }
}
