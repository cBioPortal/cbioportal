package org.mskcc.cbio.portal.dao;

import org.mskcc.cbio.portal.model.*;

import org.apache.commons.lang.StringUtils;
import org.cbioportal.model.ResourceType;

import java.sql.*;
import java.util.*;

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
            pstmt = con.prepareStatement("INSERT INTO resource_definition(" + "`RESOURCE_ID`," + "`DISPLAY_NAME`,"
                    + "`DESCRIPTION`," + "`RESOURCE_TYPE`," + "`OPEN_BY_DEFAULT`," + "`PRIORITY`," + "`CANCER_STUDY_ID`)"
                    + " VALUES(?,?,?,?,?,?,?)");
            pstmt.setString(1, resource.getResourceId());
            pstmt.setString(2, resource.getDisplayName());
            pstmt.setString(3, resource.getDescription());
            pstmt.setString(4, resource.getResourceType().name());
            pstmt.setBoolean(5, resource.isOpenByDefault());
            pstmt.setInt(6, resource.getPriority());
            pstmt.setInt(7, resource.getCancerStudyId());
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoResourceDefinition.class, con, pstmt, rs);
        }
    }

    private static ResourceDefinition unpack(ResultSet rs) throws SQLException {
        return new ResourceDefinition(rs.getString("RESOURCE_ID"), rs.getString("DISPLAY_NAME"), rs.getString("DESCRIPTION"),
                ResourceType.valueOf(rs.getString("RESOURCE_TYPE")), rs.getBoolean("OPEN_BY_DEFAULT"), rs.getInt("PRIORITY"),
                rs.getInt("CANCER_STUDY_ID"));
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

            pstmt = con.prepareStatement("SELECT * FROM resource_definition WHERE RESOURCE_ID IN ('"
                    + StringUtils.join(resourceIds, "','") + "')  AND CANCER_STUDY_ID=" + String.valueOf(cancerStudyId));

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

            pstmt = con.prepareStatement("SELECT * FROM resource_definition WHERE RESOURCE_ID IN ('"
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

            pstmt = con.prepareStatement("SELECT * FROM resource_definition WHERE CANCER_STUDY_ID=" + String.valueOf(cancerStudyId));

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