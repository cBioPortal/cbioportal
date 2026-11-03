/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.dao;

import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.ClinicalAttribute;
import org.mskcc.cbio.portal.model.ClinicalData;
import org.mskcc.cbio.portal.model.ClinicalParameterMap;
import org.mskcc.cbio.portal.model.Patient;
import org.mskcc.cbio.portal.model.Sample;
import org.mskcc.cbio.portal.util.InternalIdUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Data Access Object for `clinical` table
 *
 * @author Gideon Dresdner dresdnerg@cbio.mskcc.org
 */
public final class DaoClinicalData {

    public static final String SAMPLE_ATTRIBUTES_TABLE = "clinical_sample";
    public static final String PATIENT_ATTRIBUTES_TABLE = "clinical_patient";

    private static final String SAMPLE_ATTRIBUTES_INSERT = "INSERT INTO " + SAMPLE_ATTRIBUTES_TABLE + "(`internal_id`,`attr_id`,`attr_value`) VALUES(?,?,?)";
    private static final String PATIENT_ATTRIBUTES_INSERT = "INSERT INTO " + PATIENT_ATTRIBUTES_TABLE + "(`internal_id`,`attr_id`,`attr_value`) VALUES(?,?,?)";

    //MUTATION_COUNT and FRACTION_GENOME_ALTERED attributes are calculated from mutation and CN segment data and should not be deleted when clinical data is updated for a sample, so we exclude them from the delete statement
    private static final String SAMPLE_ATTRIBUTES_DELETE = "DELETE FROM " + SAMPLE_ATTRIBUTES_TABLE + " WHERE `internal_id` = ? AND `attr_id` NOT IN ('MUTATION_COUNT', 'FRACTION_GENOME_ALTERED')";
    private static final String PATIENT_ATTRIBUTES_DELETE = "DELETE FROM " + PATIENT_ATTRIBUTES_TABLE + " WHERE `internal_id` = ?";
    private static final Map<String, String> sampleAttributes = new HashMap<String, String>();
    private static final Map<String, String> patientAttributes = new HashMap<String, String>();

    private DaoClinicalData() {}

    public static synchronized void reCache()
    {
        clearCache();
        cacheAttributes(SAMPLE_ATTRIBUTES_TABLE, sampleAttributes);
        cacheAttributes(PATIENT_ATTRIBUTES_TABLE, patientAttributes);
    }

    private static void clearCache()
    {
        sampleAttributes.clear();
        patientAttributes.clear();
    }

    private static void cacheAttributes(String table, Map<String,String> cache)
    {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoClinicalData.class);
            pstmt = con.prepareStatement("SELECT * FROM " + table);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                cache.put(rs.getString("attr_id"), rs.getString("attr_id"));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            JdbcUtil.closeAll(DaoClinicalData.class, con, pstmt, rs);
        }
    }

    public static int addSampleDatum(int internalSampleId, String attrId, String attrVal) throws DaoException
    {
        sampleAttributes.put(attrId, attrId);
        return addDatum(SAMPLE_ATTRIBUTES_INSERT, SAMPLE_ATTRIBUTES_TABLE, internalSampleId, attrId, attrVal);
    }

    public static int addPatientDatum(int internalPatientId, String attrId, String attrVal) throws DaoException
    {
        patientAttributes.put(attrId, attrId);
        return addDatum(PATIENT_ATTRIBUTES_INSERT, PATIENT_ATTRIBUTES_TABLE, internalPatientId, attrId, attrVal);
    }

    public static int addDatum(String query, String tableName,
                               int internalId, String attrId, String attrVal) throws DaoException
    {
        if (ClickHouseBulkLoader.isBulkLoad()) {
            ClickHouseBulkLoader.getClickHouseBulkLoader(tableName).insertRecord(Integer.toString(internalId),
                attrId,
                attrVal);
            return 1;
        }

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoClinicalData.class);

            pstmt = con.prepareStatement(query);
            pstmt.setInt(1, internalId);
            pstmt.setString(2, attrId);
            pstmt.setString(3, attrVal);
            int toReturn = pstmt.executeUpdate();

            if (tableName.equals(PATIENT_ATTRIBUTES_TABLE)) {
                patientAttributes.put(attrId, attrId);
            }
            else {
                sampleAttributes.put(attrId, attrId);
            }

            return toReturn;
        }
        catch (SQLException e) {
            throw new DaoException(e);
        }
        finally {
            JdbcUtil.closeAll(DaoClinicalData.class, con, pstmt, rs);
        }
    }

    public static ClinicalData getDatum(String cancerStudyId, String patientId, String attrId) throws DaoException
    {
        int internalCancerStudyId = getInternalCancerStudyId(cancerStudyId);
        String table = getAttributeTable(attrId);
        if (table==null) {
            return null;
        }
        return getDatum(internalCancerStudyId,
            table,
            DaoPatient.getPatientByCancerStudyAndPatientId(internalCancerStudyId, patientId).getInternalId(),
            attrId);
    }

    private static int getInternalCancerStudyId(String cancerStudyId) throws DaoException
    {
        return DaoCancerStudy.getCancerStudyByStableId(cancerStudyId).getInternalId();
    }


    private static String getAttributeTable(String attrId) throws DaoException
    {
        if (sampleAttributes.containsKey(attrId)) {
            return SAMPLE_ATTRIBUTES_TABLE;
        }
        else if (patientAttributes.containsKey(attrId)) {
            return (PATIENT_ATTRIBUTES_TABLE);
        }
        else {
            return null;
        }
    }

    private static ClinicalData getDatum(int internalCancerStudyId, String table, int internalId, String attrId) throws DaoException
    {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = JdbcUtil.getDbConnection(DaoClinicalData.class);

            pstmt = con.prepareStatement("SELECT * FROM " + table +
                " WHERE internal_id=? AND attr_id=?");
            pstmt.setInt(1, internalId);
            pstmt.setString(2, attrId);

            rs = pstmt.executeQuery();
            if (rs.next()) {
                return extract(table, internalCancerStudyId, rs);
            }
            else {
                return null;
            }
        }
        catch (SQLException e) {
            throw new DaoException(e);
        }
        finally {
            JdbcUtil.closeAll(DaoClinicalData.class, con, pstmt, rs);
        }
    }

    public static List<ClinicalData> getDataByPatientId(int cancerStudyId, String patientId) throws DaoException
    {
        List<Integer> internalIds = new ArrayList<Integer>();
        internalIds.add(DaoPatient.getPatientByCancerStudyAndPatientId(cancerStudyId, patientId).getInternalId());
        return getDataByInternalIds(cancerStudyId, PATIENT_ATTRIBUTES_TABLE, internalIds);
    }

    private static List<ClinicalData> getDataByInternalIds(int internalCancerStudyId, String table, List<Integer> internalIds) throws DaoException
    {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        List<ClinicalData> clinicals = new ArrayList<ClinicalData>();
        String sql = ("SELECT * FROM " + table + " WHERE `internal_id` IN " +
            "(" + generateIdsSql(internalIds) + ")");

        try {
            con = JdbcUtil.getDbConnection(DaoClinicalData.class);
            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                clinicals.add(extract(table, internalCancerStudyId, rs));
            }
        }
        catch (SQLException e) {
            throw new DaoException(e);
        }
        finally {
            JdbcUtil.closeAll(DaoClinicalData.class, con, pstmt, rs);
        }

        return clinicals;
    }

    public static List<ClinicalData> getData(String cancerStudyId) throws DaoException
    {
        return getData(getInternalCancerStudyId(cancerStudyId));
    }
    public static List<ClinicalData> getData(int cancerStudyId) throws DaoException
    {

        return getDataByInternalIds(cancerStudyId, PATIENT_ATTRIBUTES_TABLE, getPatientIdsByCancerStudy(cancerStudyId));
    }

    private static List<Integer> getPatientIdsByCancerStudy(int cancerStudyId)
    {
        List<Integer> patientIds = new ArrayList<Integer>();
        for (Patient patient : DaoPatient.getPatientsByCancerStudyId(cancerStudyId)) {
            patientIds.add(patient.getInternalId());
        }
        return patientIds;
    }

    private static List<Integer> getSampleIdsByCancerStudy(int cancerStudyId)
    {
        List<Integer> sampleIds = new ArrayList<Integer>();
        for (Patient patient : DaoPatient.getPatientsByCancerStudyId(cancerStudyId)) {
            for (Sample s : DaoSample.getSamplesByPatientId(patient.getInternalId())) {
                sampleIds.add(s.getInternalId());
            }
        }
        return sampleIds;
    }

    public static List<ClinicalData> getData(String cancerStudyId, Collection<String> patientIds) throws DaoException
    {
        return getData(getInternalCancerStudyId(cancerStudyId), patientIds);
    }
    public static List<ClinicalData> getData(int cancerStudyId, Collection<String> patientIds) throws DaoException
    {
        List<Integer> patientIdsInt = new ArrayList<Integer>();
        for (String patientId : patientIds) {
            patientIdsInt.add(DaoPatient.getPatientByCancerStudyAndPatientId(cancerStudyId, patientId).getInternalId());
        }

        return getDataByInternalIds(cancerStudyId, PATIENT_ATTRIBUTES_TABLE, patientIdsInt);
    }

    public static List<ClinicalData> getSampleAndPatientData(int cancerStudyId, Collection<String> sampleIds) throws DaoException
    {
        List<Integer> sampleIdsInt = new ArrayList<Integer>();
        List<Integer> patientIdsInt = new ArrayList<Integer>();
        Map<String,Set<String>> mapPatientIdSampleIds = new HashMap<String,Set<String>>();
        for (String sampleId : sampleIds) {
            Sample sample = DaoSample.getSampleByCancerStudyAndSampleId(cancerStudyId, sampleId);
            sampleIdsInt.add(sample.getInternalId());
            int patientIdInt = sample.getInternalPatientId();
            String patientIdStable = DaoPatient.getPatientById(patientIdInt).getStableId();
            patientIdsInt.add(patientIdInt);
            Set<String> sampleIdsForPatient = mapPatientIdSampleIds.get(patientIdStable);
            if (sampleIdsForPatient==null) {
                sampleIdsForPatient = new HashSet<String>();
                mapPatientIdSampleIds.put(patientIdStable, sampleIdsForPatient);
            }
            sampleIdsForPatient.add(sampleId);
        }
        List<ClinicalData> sampleClinicalData =  getDataByInternalIds(cancerStudyId, SAMPLE_ATTRIBUTES_TABLE, sampleIdsInt);

        List<ClinicalData> patientClinicalData = getDataByInternalIds(cancerStudyId, PATIENT_ATTRIBUTES_TABLE, patientIdsInt);
        for (ClinicalData cd : patientClinicalData) {
            String stablePatientId = cd.getStableId();
            Set<String> sampleIdsForPatient = mapPatientIdSampleIds.get(stablePatientId);
            for (String sampleId : sampleIdsForPatient) {
                ClinicalData cdSample = new ClinicalData(cd);
                cdSample.setStableId(sampleId);
                sampleClinicalData.add(cdSample);
            }
        }

        return sampleClinicalData;
    }

    public static List<ClinicalData> getSampleAndPatientData(int cancerStudyId, Collection<String> sampleIds, ClinicalAttribute attr) throws DaoException
    {
        List<Integer> sampleIdsInt = new ArrayList<Integer>();
        List<Integer> patientIdsInt = new ArrayList<Integer>();
        Map<String,Set<String>> mapPatientIdSampleIds = new HashMap<String,Set<String>>();
        for (String sampleId : sampleIds) {
            Sample sample = DaoSample.getSampleByCancerStudyAndSampleId(cancerStudyId, sampleId);
            sampleIdsInt.add(sample.getInternalId());
            int patientIdInt = sample.getInternalPatientId();
            String patientIdStable = DaoPatient.getPatientById(patientIdInt).getStableId();
            patientIdsInt.add(patientIdInt);
            Set<String> sampleIdsForPatient = mapPatientIdSampleIds.get(patientIdStable);
            if (sampleIdsForPatient==null) {
                sampleIdsForPatient = new HashSet<String>();
                mapPatientIdSampleIds.put(patientIdStable, sampleIdsForPatient);
            }
            sampleIdsForPatient.add(sampleId);
        }
        List<ClinicalData> sampleClinicalData =  getDataByInternalIds(cancerStudyId, SAMPLE_ATTRIBUTES_TABLE, sampleIdsInt, Collections.singletonList(attr.getAttrId()));

        List<ClinicalData> patientClinicalData = getDataByInternalIds(cancerStudyId, PATIENT_ATTRIBUTES_TABLE, patientIdsInt, Collections.singletonList(attr.getAttrId()));
        for (ClinicalData cd : patientClinicalData) {
            String stablePatientId = cd.getStableId();
            Set<String> sampleIdsForPatient = mapPatientIdSampleIds.get(stablePatientId);
            for (String sampleId : sampleIdsForPatient) {
                ClinicalData cdSample = new ClinicalData(cd);
                cdSample.setStableId(sampleId);
                sampleClinicalData.add(cdSample);
            }
        }

        return sampleClinicalData;
    }

    public static List<ClinicalData> getSampleData(int cancerStudyId, Collection<String> sampleIds, ClinicalAttribute attr) throws DaoException
    {
        List<Integer> sampleIdsInt = new ArrayList<Integer>();
        for (String sampleId : sampleIds) {
            Sample _sample = DaoSample.getSampleByCancerStudyAndSampleId(cancerStudyId, sampleId);
            if (_sample != null) {
                sampleIdsInt.add(_sample.getInternalId());
            }
        }
        return getDataByInternalIds(cancerStudyId, SAMPLE_ATTRIBUTES_TABLE, sampleIdsInt, Collections.singletonList(attr.getAttrId()));
    }

    /**
     * Removes sample attributes associated with the sample with internal sample id.
     * No error raised if you try to delete record for sample which does not exist.
     * @param sampleInternalId - internal id of sample for which to remove attributes
     * @throws DaoException
     */
    public static void removeSampleAttributesData(int sampleInternalId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            con = JdbcUtil.getDbConnection(DaoClinicalData.class);
            pstmt = con.prepareStatement(SAMPLE_ATTRIBUTES_DELETE);
            pstmt.setInt(1, sampleInternalId);
            pstmt.executeUpdate();
        }
        catch (SQLException e) {
            throw new DaoException(e);
        }
        finally {
            JdbcUtil.closeAll(DaoClinicalData.class, con, pstmt, null);
        }
    }

    /**
     * Deletes any matching sample attributes records if they exist.
     * No error raised if you try to delete records which do not exist.
     * @param sampleInternalIds - internal sample ids for which remove attributes
     * @param attrId - attribute to delete
     * @throws DaoException
     */
    public static void removeSampleAttributesData(Set<Integer> sampleInternalIds, String attrId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            if (sampleInternalIds.isEmpty()) {
                return;
            }
            con = JdbcUtil.getDbConnection(DaoClinicalData.class);
            final Connection queryCon = con;
            ClickHouseBulkUploader.upload(sampleInternalIds, stagingTable -> {
                try (PreparedStatement stmt = queryCon.prepareStatement(
                        "DELETE FROM " + SAMPLE_ATTRIBUTES_TABLE +
                        " WHERE `attr_id` = ? AND `internal_id` IN (SELECT id FROM " + stagingTable + ")")) {
                    stmt.setString(1, attrId);
                    stmt.executeUpdate();
                }
                return null;
            });
        }
        catch (SQLException e) {
            throw new DaoException(e);
        }
        finally {
            JdbcUtil.closeAll(DaoClinicalData.class, con, pstmt, null);
        }
    }

    public static List<ClinicalData> getSampleData(int cancerStudyId, Collection<String> sampleIds) throws DaoException
    {
        List<Integer> sampleIdsInt = new ArrayList<Integer>();
        for (String sampleId : sampleIds) {
            sampleIdsInt.add(DaoSample.getSampleByCancerStudyAndSampleId(cancerStudyId, sampleId).getInternalId());
        }
        return getDataByInternalIds(cancerStudyId, SAMPLE_ATTRIBUTES_TABLE, sampleIdsInt);
    }

    public static List<ClinicalData> getData(String cancerStudyId, Collection<String> patientIds, ClinicalAttribute attr) throws DaoException
    {
        int internalCancerStudyId = getInternalCancerStudyId(cancerStudyId);
        List<Integer> patientIdsInt = new ArrayList<Integer>();
        for (String patientId : patientIds) {
            patientIdsInt.add(DaoPatient.getPatientByCancerStudyAndPatientId(internalCancerStudyId, patientId).getInternalId());
        }

        return getDataByInternalIds(internalCancerStudyId, PATIENT_ATTRIBUTES_TABLE, patientIdsInt, Collections.singletonList(attr.getAttrId()));
    }

    private static List<ClinicalData> getDataByInternalIds(int internalCancerStudyId, String table, List<Integer> internalIds, Collection<String> attributeIds) throws DaoException
    {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        List<ClinicalData> clinicals = new ArrayList<ClinicalData>();

        String sql = ("SELECT * FROM " + table + " WHERE `internal_id` IN " +
            "(" + generateIdsSql(internalIds) + ") " +
            " AND attr_id IN ('"+ StringUtils.join(attributeIds, "','")+"') ");

        try {
            con = JdbcUtil.getDbConnection(DaoClinicalData.class);
            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                clinicals.add(extract(table, internalCancerStudyId, rs));
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        }
        finally {
            JdbcUtil.closeAll(DaoClinicalData.class, con, pstmt, rs);
        }
        return clinicals;
    }

    public static List<ClinicalData> getDataByAttributeIds(int internalCancerStudyId, Collection<String> attributeIds) throws DaoException {

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        List<ClinicalData> clinicals = new ArrayList<ClinicalData>();

        try {
            con = JdbcUtil.getDbConnection(DaoClinicalData.class);

            pstmt = con.prepareStatement("SELECT * FROM clinical_patient WHERE" +
                " attr_id IN ('" + StringUtils.join(attributeIds, "','") +"') ");

            List<Integer> patients = getPatientIdsByCancerStudy(internalCancerStudyId);

            rs = pstmt.executeQuery();
            while(rs.next()) {
                Integer patientId = rs.getInt("internal_id");
                if (patients.contains(patientId)) {
                    clinicals.add(extract(PATIENT_ATTRIBUTES_TABLE, internalCancerStudyId, rs));
                }
            }
        }
        catch (SQLException e) {
            throw new DaoException(e);
        }
        finally {
            JdbcUtil.closeAll(DaoClinicalData.class, con, pstmt, rs);
        }

        return clinicals;
    }

    private static ClinicalData extract(String table, int internalCancerStudyId, ResultSet rs) throws SQLException {
        // get 
        String stableId = getStableIdFromInternalId(table, rs.getInt("internal_id"));
        return new ClinicalData(internalCancerStudyId,
            stableId,
            rs.getString("attr_id"),
            rs.getString("attr_value"));
    }

    private static String getStableIdFromInternalId(String table, int internalId)
    {
        if (table.equals(SAMPLE_ATTRIBUTES_TABLE)) {
            return DaoSample.getSampleById(internalId).getStableId();
        }
        else {
            return DaoPatient.getPatientById(internalId).getStableId();
        }
    }

    private static String generateIdsSql(Collection<Integer> ids) {
        return "'" + StringUtils.join(ids, "','") + "'";
    }

    public static void deleteAllRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoClinicalData.class);
            pstmt = con.prepareStatement("TRUNCATE TABLE clinical_patient");
            pstmt.executeUpdate();
            pstmt = con.prepareStatement("TRUNCATE TABLE clinical_sample");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoClinicalData.class, con, pstmt, rs);
        }
        reCache();
    }

    /*********************************************************
     * Previous DaoClinicalData class methods (accessors only)
     *********************************************************/

    /**
     * Returns the survival data for the given patientSet, NOT necessarily in the 
     * same order as the patientSet.
     *
     * @param cancerStudyId: the cancerstudy internal id
     * @param patientSet: set of patient ids
     * @return
     * @throws DaoException
     */
    public static List<Patient> getSurvivalData(int cancerStudyId, Collection<String> patientSet) throws DaoException {
        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByInternalId(cancerStudyId);
        List<ClinicalData> data = getData(cancerStudyId, patientSet);
        Map<String,Map<String,ClinicalData>> clinicalData = new LinkedHashMap<String,Map<String,ClinicalData>>();
        for (ClinicalData cd : data) {
            String patientId = cd.getStableId();
            Map<String,ClinicalData> msc = clinicalData.get(cd.getStableId());
            if (msc==null) {
                msc = new HashMap<String,ClinicalData>();
                clinicalData.put(patientId, msc);
            }
            msc.put(cd.getAttrId(), cd);
        }

        ArrayList<Patient> toReturn = new ArrayList<Patient>();
        for (Map.Entry<String,Map<String,ClinicalData>> entry : clinicalData.entrySet()) {
            Patient patient = DaoPatient.getPatientByCancerStudyAndPatientId(cancerStudyId, entry.getKey());
            toReturn.add(new Patient(cancerStudy, patient.getStableId(), patient.getInternalId(), entry.getValue()));
        }
        return toReturn;
    }

    /**************************************************************
     * Previous DaoClinicalFreeForm class methods (accessors only)
     *************************************************************/

    public static List<ClinicalParameterMap> getDataSlice(int cancerStudyId, Collection<String> attributeIds) throws DaoException {

        Map<String,Map<String, String>> mapAttrStableIdValue = new HashMap<String,Map<String, String>>();
        for (ClinicalData cd : getDataByAttributeIds(cancerStudyId, attributeIds)) {

            String attrId = cd.getAttrId();
            String value = cd.getAttrVal();
            String stableId = cd.getStableId();

            if (value.isEmpty() || value.equals(ClinicalAttribute.NA)) {
                continue;
            }

            Map<String, String> mapStableIdValue = mapAttrStableIdValue.get(attrId);
            if (mapStableIdValue == null) {
                mapStableIdValue = new HashMap<String, String>();
                mapAttrStableIdValue.put(attrId, mapStableIdValue);
            }
            mapStableIdValue.put(stableId, value);
        }

        List<ClinicalParameterMap> maps = new ArrayList<ClinicalParameterMap>();
        for (Map.Entry<String,Map<String, String>> entry : mapAttrStableIdValue.entrySet()) {
            maps.add(new ClinicalParameterMap(entry.getKey(), entry.getValue()));
        }

        return maps;
    }

    public static HashSet<String> getDistinctParameters(int cancerStudyId) throws DaoException {

        HashSet<String> toReturn = new HashSet<String>();
        for (ClinicalData clinicalData : DaoClinicalData.getData(cancerStudyId)) {
            toReturn.add(clinicalData.getAttrId());
        }

        return toReturn;
    }
    public static HashSet<String> getAllPatients(int cancerStudyId) throws DaoException {

        HashSet<String> toReturn = new HashSet<String>();
        for (ClinicalData clinicalData : getData(cancerStudyId)) {
            toReturn.add(clinicalData.getStableId());
        }

        return toReturn;
    }
    public static List<ClinicalData> getDataByCancerStudy(int cancerStudyId) throws DaoException {

        return DaoClinicalData.getData(cancerStudyId);
    }

    public static List<ClinicalData> getDataByPatientIds(int cancerStudyId, List<String> patientIds) throws DaoException {

        return DaoClinicalData.getData(cancerStudyId, patientIds);
    }

    public static List<Patient> getPatientsByAttribute(int cancerStudy, String paramName, String paramValue) throws DaoException
    {
        List<Integer> ids = getIdsByAttribute(cancerStudy, paramName, paramValue, PATIENT_ATTRIBUTES_TABLE);
        return InternalIdUtil.getPatientsById(ids);
    }

    public static List<Sample> getSamplesByAttribute(int cancerStudy, String paramName, String paramValue) throws DaoException
    {
        List<Integer> ids = getIdsByAttribute(cancerStudy, paramName, paramValue, SAMPLE_ATTRIBUTES_TABLE);
        return InternalIdUtil.getSamplesById(ids);
    }


    private static List<Integer> getIdsByAttribute(int cancerStudyId, String paramName, String paramValue, String tableName) throws DaoException
    {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
            con = JdbcUtil.getDbConnection(DaoClinicalData.class);
            pstmt = con.prepareStatement ("SELECT internal_id FROM `" + tableName + "`"
                + " WHERE attr_id=? AND attr_value=?");
            pstmt.setString(1, paramName);
            pstmt.setString(2, paramValue);
            rs = pstmt.executeQuery();

            List<Integer> ids = new ArrayList<Integer>();

            while (rs.next())
            {
                ids.add(rs.getInt("internal_id"));
            }

            return ids;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoClinicalData.class, con, pstmt, rs);
        }
    }

    // get cancerType from the clinical_sample table to determine whether we have multiple cancer types
    // for given samples
    public static Map<String, Set<String>> getCancerTypeInfoBySamples(List<String> samplesList) throws DaoException {
        return ClickHouseBulkUploader.uploadStrings(samplesList, stagingTable -> {
            String inClause = stagingTable == null
                    ? "select internal_id from sample"
                    : "select internal_id from sample where stable_id in (select id from " + stagingTable + ")";
            Connection con = null;
            try {
                con = JdbcUtil.getDbConnection(DaoClinicalData.class);
                try (PreparedStatement pstmt = con.prepareStatement(
                        "select distinct attr_value as attributeValue, attr_id as attributeID " +
                        "from clinical_sample " +
                        "where attr_id in (?, ?) and internal_id in (" + inClause + ")")) {
                    pstmt.setString(1, ClinicalAttribute.CANCER_TYPE);
                    pstmt.setString(2, ClinicalAttribute.CANCER_TYPE_DETAILED);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        Map<String, Set<String>> result = new LinkedHashMap<>();
                        result.put(ClinicalAttribute.CANCER_TYPE, new HashSet<>());
                        result.put(ClinicalAttribute.CANCER_TYPE_DETAILED, new HashSet<>());
                        while (rs.next()) {
                            result.get(rs.getString("attributeID")).add(rs.getString("attributeValue"));
                        }
                        return result;
                    }
                }
            } finally {
                JdbcUtil.closeAll(DaoClinicalData.class, con, null, null);
            }
        });
    }

    public static void removePatientAttributesData(int internalPatientId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoClinicalData.class);
            pstmt = con.prepareStatement(PATIENT_ATTRIBUTES_DELETE);
            pstmt.setInt(1, internalPatientId);
            pstmt.executeUpdate();
        }
        catch (SQLException e) {
            throw new DaoException(e);
        }
        finally {
            JdbcUtil.closeAll(DaoClinicalData.class, con, pstmt, rs);
        }
    }
}
