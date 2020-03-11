package org.mskcc.cbio.portal.dao;

import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.InternalIdUtil;

import org.apache.commons.lang.StringUtils;

import java.sql.*;
import java.util.*;

/**
 * Data Access Object for `resource` tables
 */
public final class DaoResourceData {

    public static final String SAMPLE_TABLE = "resource_sample";
    public static final String PATIENT_TABLE = "resource_patient";
    public static final String STUDY_TABLE = "resource_study";

    private static final String SAMPLE_INSERT = "INSERT INTO " + SAMPLE_TABLE
            + "(`INTERNAL_ID`,`RESOURCE_ID`,`URL` VALUES(?,?,?)";
    private static final String PATIENT_INSERT = "INSERT INTO " + PATIENT_TABLE
            + "(`INTERNAL_ID`,`RESOURCE_ID`,`URL` VALUES(?,?,?)";
    private static final String STUDY_INSERT = "INSERT INTO " + STUDY_TABLE
            + "(`INTERNAL_ID`,`RESOURCE_ID`,`URL` VALUES(?,?,?)";

    private static final Map<String, String> sampleResources = new HashMap<String, String>();
    private static final Map<String, String> patientResources = new HashMap<String, String>();
    private static final Map<String, String> studyResources = new HashMap<String, String>();

    private DaoResourceData() {
    }

    public static synchronized void reCache() {
        clearCache();
        cacheResources(SAMPLE_TABLE, sampleResources);
        cacheResources(PATIENT_TABLE, patientResources);
        cacheResources(STUDY_TABLE, studyResources);
    }

    private static void clearCache() {
        sampleResources.clear();
        patientResources.clear();
        studyResources.clear();
    }

    private static void cacheResources(String table, Map<String, String> cache) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoResourceData.class);
            pstmt = con.prepareStatement("SELECT * FROM " + table);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                cache.put(rs.getString("RESOURCE_ID"), rs.getString("RESOURCE_ID"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            JdbcUtil.closeAll(DaoResourceData.class, con, pstmt, rs);
        }
    }

    public static int addSampleDatum(int internalSampleId, String resourceId, String url) throws DaoException {
        sampleResources.put(resourceId, resourceId);
        return addDatum(SAMPLE_INSERT, SAMPLE_TABLE, internalSampleId, resourceId, url);
    }

    public static int addPatientDatum(int internalPatientId, String resourceId, String url) throws DaoException {
        patientResources.put(resourceId, resourceId);
        return addDatum(PATIENT_INSERT, PATIENT_TABLE, internalPatientId, resourceId, url);
    }

    public static int addStudyDatum(int internalStudyId, String resourceId, String url) throws DaoException {
        patientResources.put(resourceId, resourceId);
        return addDatum(STUDY_INSERT, STUDY_TABLE, internalStudyId, resourceId, url);
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
            int toReturn = pstmt.executeUpdate();

            if (tableName.equals(PATIENT_TABLE)) {
                patientResources.put(resourceId, resourceId);
            } else if (tableName.equals(SAMPLE_TABLE)){
                sampleResources.put(resourceId, resourceId);
            } else {
                studyResources.put(resourceId, resourceId);
            }

            return toReturn;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoResourceData.class, con, pstmt, rs);
        }
    }

    public static ResourceBaseData getDatum(String cancerStudyId, String patientId, String resourceId) throws DaoException {
        int internalCancerStudyId = getInternalCancerStudyId(cancerStudyId);
        String table = getResourceTable(resourceId);
        if (table == null) {
            return null;
        }
        return getDatum(internalCancerStudyId, table,
                DaoPatient.getPatientByCancerStudyAndPatientId(internalCancerStudyId, patientId).getInternalId(),
                resourceId);
    }

    private static int getInternalCancerStudyId(String cancerStudyId) throws DaoException {
        return DaoCancerStudy.getCancerStudyByStableId(cancerStudyId).getInternalId();
    }

    private static String getResourceTable(String resourceId) throws DaoException {
        if (sampleResources.containsKey(resourceId)) {
            return SAMPLE_TABLE;
        } else if (patientResources.containsKey(resourceId)) {
            return PATIENT_TABLE;
        } else if (studyResources.containsKey(resourceId)) {
            return STUDY_TABLE;
        } else {
            return null;
        }
    }

    private static ResourceBaseData getDatum(int internalCancerStudyId, String table, int internalId, String resourceId)
            throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = JdbcUtil.getDbConnection(DaoResourceData.class);

            pstmt = con.prepareStatement("SELECT * FROM " + table + " WHERE INTERNAL_ID=? AND RESOURCE_ID=?");
            pstmt.setInt(1, internalId);
            pstmt.setString(2, resourceId);

            rs = pstmt.executeQuery();
            if (rs.next()) {
                return extract(table, internalCancerStudyId, rs);
            } else {
                return null;
            }
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
        return getDataByInternalIds(cancerStudyId, PATIENT_TABLE, internalIds);
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
        if (table.equals(SAMPLE_TABLE)) {
            return DaoSample.getSampleById(internalId).getStableId();
        } else {
            return DaoPatient.getPatientById(internalId).getStableId();
        }
    }
}