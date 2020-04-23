package org.mskcc.cbio.portal.dao;

import org.mskcc.cbio.portal.model.*;
import org.apache.commons.lang.StringUtils;
import java.sql.*;
import java.util.*;

/**
 * Data Access Object for `resource` tables
 */
public final class DaoResourceData {

    public static final String RESOURCE_SAMPLE_TABLE = "resource_sample";
    public static final String RESOURCE_PATIENT_TABLE = "resource_patient";
    public static final String RESOURCE_STUDY_TABLE = "resource_study";

    private static final String SAMPLE_INSERT = "INSERT INTO " + RESOURCE_SAMPLE_TABLE
            + "(`INTERNAL_ID`,`RESOURCE_ID`,`URL` VALUES(?,?,?)";
    private static final String PATIENT_INSERT = "INSERT INTO " + RESOURCE_PATIENT_TABLE
            + "(`INTERNAL_ID`,`RESOURCE_ID`,`URL` VALUES(?,?,?)";
    private static final String STUDY_INSERT = "INSERT INTO " + RESOURCE_STUDY_TABLE
            + "(`INTERNAL_ID`,`RESOURCE_ID`,`URL` VALUES(?,?,?)";

    private DaoResourceData() {
    }

    public static int addSampleDatum(int internalSampleId, String resourceId, String url) throws DaoException {
        return addDatum(SAMPLE_INSERT, RESOURCE_SAMPLE_TABLE, internalSampleId, resourceId, url);
    }

    public static int addPatientDatum(int internalPatientId, String resourceId, String url) throws DaoException {
        return addDatum(PATIENT_INSERT, RESOURCE_PATIENT_TABLE, internalPatientId, resourceId, url);
    }

    public static int addStudyDatum(int internalStudyId, String resourceId, String url) throws DaoException {
        return addDatum(STUDY_INSERT, RESOURCE_STUDY_TABLE, internalStudyId, resourceId, url);
    }

    public static int addDatum(String query, String tableName, int internalId, String resourceId, String url)
            throws DaoException {
        if (MySQLbulkLoader.isBulkLoad()) {
            MySQLbulkLoader.getMySQLbulkLoader(tableName).insertRecord(Integer.toString(internalId), resourceId, url);
            return 1;
        }

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoResourceData.class);

            pstmt = con.prepareStatement(query);
            pstmt.setInt(1, internalId);
            pstmt.setString(2, resourceId);
            pstmt.setString(3, url);
            
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoResourceData.class, con, pstmt, rs);
        }
    }

    public static List<ResourceBaseData> getDataByPatientId(int cancerStudyId, String patientId) throws DaoException
    {
        List<Integer> internalIds = new ArrayList<Integer>();
        internalIds.add(DaoPatient.getPatientByCancerStudyAndPatientId(cancerStudyId, patientId).getInternalId());
        return getDataByInternalIds(cancerStudyId, RESOURCE_PATIENT_TABLE, internalIds);
    }

    private static List<ResourceBaseData> getDataByInternalIds(int internalCancerStudyId, String table, List<Integer> internalIds) throws DaoException
    {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        List<ResourceBaseData> resources = new ArrayList<ResourceBaseData>();
        String sql = ("SELECT * FROM " + table + " WHERE `INTERNAL_ID` IN " +
            "(" + generateIdsSql(internalIds) + ")");

        try {
            con = JdbcUtil.getDbConnection(DaoResourceData.class);
            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                resources.add(extract(table, internalCancerStudyId, rs));
            }
        }
        catch (SQLException e) {
            throw new DaoException(e);
        }
        finally {
            JdbcUtil.closeAll(DaoResourceData.class, con, pstmt, rs);
        }

        return resources;
    }

    private static String generateIdsSql(Collection<Integer> ids) {
        return "'" + StringUtils.join(ids, "','") + "'";
    }

    private static ResourceBaseData extract(String table, int internalCancerStudyId, ResultSet rs) throws SQLException {
        String stableId = getStableIdFromInternalId(table, rs.getInt("INTERNAL_ID"));
        return new ResourceBaseData(internalCancerStudyId, stableId, rs.getString("RESOURCE_ID"), rs.getString("URL"));
    }

    private static String getStableIdFromInternalId(String table, int internalId) {
        if (table.equals(RESOURCE_SAMPLE_TABLE)) {
            return DaoSample.getSampleById(internalId).getStableId();
        } else {
            return DaoPatient.getPatientById(internalId).getStableId();
        }
    }
}