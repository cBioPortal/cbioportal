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

import org.mskcc.cbio.portal.model.*;

import org.apache.commons.logging.*;
import org.apache.commons.lang.StringUtils;

import java.sql.*;
import java.util.*;

/**
 * Data Access Object for `clinical` table
 *
 * @author Gideon Dresdner dresdnerg@cbio.mskcc.org
 */
public final class DaoClinicalData {

    private static final String SAMPLE_TABLE = "clinical_sample";
    private static final String PATIENT_TABLE = "clinical_patient";

    private static final String SAMPLE_INSERT = "INSERT INTO " + SAMPLE_TABLE + "(`INTERAL_ID`,`ATTR_ID`,`ATTR_VALUE` VALUES(?,?,?)";
    private static final String PATIENT_INSERT = "INSERT INTO " + PATIENT_TABLE + "(`INTERNAL_ID`,`ATTR_ID`,`ATTR_VALUE` VALUES(?,?,?)";

    private static final Map<String, String> sampleAttributes = new HashMap<String, String>();
    private static final Map<String, String> patientAttributes = new HashMap<String, String>();

    static {
        cache();
    }

    private static void cache()
    {
        clearCache();
        cacheAttributes(SAMPLE_TABLE, sampleAttributes);
        cacheAttributes(PATIENT_TABLE, patientAttributes);
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
                cache.put(rs.getString("ATTR_ID"), rs.getString("ATTR_ID"));
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
        return addDatum(SAMPLE_INSERT, SAMPLE_TABLE, internalSampleId, attrId, attrVal);
    }

    public static int addPatientDatum(int internalPatientId, String attrId, String attrVal) throws DaoException
    {
        patientAttributes.put(attrId, attrId);
        return addDatum(PATIENT_INSERT, PATIENT_TABLE, internalPatientId, attrId, attrVal);
    }

    public static int addDatum(String query, String tableName,
                               int internalId, String attrId, String attrVal) throws DaoException
    {
        if (MySQLbulkLoader.isBulkLoad()) {
            MySQLbulkLoader.getMySQLbulkLoader(tableName).insertRecord(Integer.toString(internalId),
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

            return pstmt.executeUpdate();
        }
        catch (SQLException e) {
            throw new DaoException(e);
        }
        finally {
            JdbcUtil.closeAll(DaoClinicalData.class, con, pstmt, rs);
        }
    }

    public static ClinicalData getDatum(String cancerStudyId, String caseId, String attrId) throws DaoException
    {
        int internalCancerStudyId = getInternalCancerStudyId(cancerStudyId);
        return getDatum(internalCancerStudyId,
                        getAttributeTable(attrId),
                        DaoPatient.getPatient(internalCancerStudyId, caseId).getInternalId(),
                        attrId);
    }

    private static int getInternalCancerStudyId(String cancerStudyId)
    {
        return DaoCancerStudy.getCancerStudyByStableId(cancerStudyId).getInternalId();
    }

    private static int getInternalId(String attrId, String stableId) throws DaoException
    {
        if (sampleAttributes.containsKey(attrId)) {
            return DaoSample.getSampleByStableId(stableId).getInternalId();
        }
        else if (patientAttributes.containsKey(stableId)) {
            return DaoPatient.getPatientByStableId(stableId).getInternalId();
        }
        else {
            throw new DaoException("Unknown clinical attribute: " + attrId);
        }
    }
    
    private static String getAttributeTable(String attrId) throws DaoException
    {
        if (sampleAttributes.containsKey(attrId)) {
            return SAMPLE_TABLE;
        }
        else if (patientAttributes.containsKey(attrId)) {
            return (PATIENT_TABLE);
        }
        else {
            throw new DaoException("Unknown clinical attribute: " + attrId);
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
                                         " WHERE INTERNAL_ID=? AND ATTR_ID=?");
            pstmt.setInt(1, internalId);
            pstmt.setString(2, attrId);

            rs = pstmt.executeQuery();
            if (rs.next()) {
                return extract(internalCancerStudyId, rs);
            }
            else {
                throw new DaoException(String.format("clinical data not found for (%d, %s)",
                                                     internalId, attrId));
            }
        }
        catch (SQLException e) {
            throw new DaoException(e);
        }
        finally {
            JdbcUtil.closeAll(DaoClinicalData.class, con, pstmt, rs);
        }
    }

	public static List<ClinicalData> getCasesById(int cancerStudyId, String caseId) throws DaoException
    {
        List<Integer> internalIds = new ArrayList<Integer>();
        internalIds.add(DaoPatient.getPatient(cancerStudyId, caseId).getInternalId());
		return getDataByInternalIds(cancerStudyId, PATIENT_TABLE, internalIds);
	}

    private static List<ClinicalData> getDataByInternalIds(int internalCancerStudyId, String table, List<Integer> internalIds) throws DaoException
    {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        List<ClinicalData> clinicals = new ArrayList<ClinicalData>();
        String sql = ("SELECT * FROM " + table + " WHERE `INTERNAL_ID` IN " +
                      "(" + generateCaseIdsSql(internalIds) + ")");
                      
        try {
            con = JdbcUtil.getDbConnection(DaoClinicalData.class);
            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                clinicals.add(extract(internalCancerStudyId, rs));
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
        
        return getDataByInternalIds(cancerStudyId, PATIENT_TABLE, getPatientIdsByCancerStudy(cancerStudyId));
    }

    private static List<Integer> getPatientIdsByCancerStudy(int cancerStudyId)
    {
        List<Integer> patientIds = new ArrayList<Integer>();
        for (Patient patient : DaoPatient.getPatientsByInternalCancerStudyId(cancerStudyId)) {
            patientIds.add(patient.getInternalId());
        }
        return patientIds;
    }

    public static List<ClinicalData> getData(String cancerStudyId, Collection<String> caseIds) throws DaoException
    {
        return getData(getInternalCancerStudyId(cancerStudyId), caseIds);
    }
    public static List<ClinicalData> getData(int cancerStudyId, Collection<String> caseIds) throws DaoException
    {
        List<Integer> patientIds = new ArrayList<Integer>();
        for (String patientId : caseIds) {
            patientIds.add(DaoPatient.getPatient(cancerStudyId, patientId).getInternalId());
        }

		return getDataByInternalIds(cancerStudyId, PATIENT_TABLE, patientIds);
	}

    public static List<ClinicalData> getData(String cancerStudyId, Collection<String> caseIds, ClinicalAttribute attr) throws DaoException
    {
        int internalCancerStudyId = getInternalCancerStudyId(cancerStudyId);
        List<Integer> patientIds = new ArrayList<Integer>();
        for (String patientId : caseIds) {
            patientIds.add(DaoPatient.getPatient(internalCancerStudyId, patientId).getInternalId());
        }

		return getDataByInternalIds(internalCancerStudyId, PATIENT_TABLE, patientIds, Collections.singletonList(attr.getAttrId()));
    }



    private static List<ClinicalData> getDataByInternalIds(int internalCancerStudyId, String table, List<Integer> internalIds, Collection<String> attributeIds) throws DaoException
    {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        List<ClinicalData> clinicals = new ArrayList<ClinicalData>();

        String sql = ("SELECT * FROM " + table + " WHERE `INTERNAL_ID` IN " +
                      "(" + generateCaseIdsSql(internalIds) + ") " +
                      " AND ATTR_ID IN ('"+ StringUtils.join(attributeIds, "','")+"') ");

        try {
            con = JdbcUtil.getDbConnection(DaoClinicalData.class);
            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                clinicals.add(extract(internalCancerStudyId, rs));
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        }
        finally {
                JdbcUtil.closeAll(DaoClinicalData.class, con, pstmt, rs);
        }
        return clinicals;
    }

    private static List<ClinicalData> getDataByAttributeIds(int internalCancerStudyId, Collection<String> attributeIds) throws DaoException {

		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		List<ClinicalData> clinicals = new ArrayList<ClinicalData>();

		try {
            con = JdbcUtil.getDbConnection(DaoClinicalData.class);

            pstmt = con.prepareStatement("SELECT * FROM clinical_patient WHERE" +
                                         " ATTR_ID IN ('" + StringUtils.join(attributeIds, "','") +"') ");

            List<Integer> patients = getPatientIdsByCancerStudy(internalCancerStudyId);

            rs = pstmt.executeQuery();
            while(rs.next()) {
                Integer patientId = rs.getInt("INTERNAL_ID");
                if (patients.contains(patientId)) {
                    clinicals.add(extract(internalCancerStudyId, rs));
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

    private static ClinicalData extract(int internalCancerStudyId, ResultSet rs) throws SQLException {
		return new ClinicalData(internalCancerStudyId,
								rs.getString("INTERNAL_ID"),
								rs.getString("ATTR_ID"),
								rs.getString("ATTR_VALUE"));
    }

    private static String generateCaseIdsSql(Collection<Integer> caseIds) {
        return "'" + StringUtils.join(caseIds, "','") + "'";
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
    }

	/*********************************************************
	 * Previous DaoClinicalData class methods (accessors only)
	 *********************************************************/
	
	public static List<Patient> getSurvivalData(int cancerStudyId, Collection<String> caseSet) throws DaoException {
            CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByInternalId(cancerStudyId);
            List<ClinicalData> data = getData(cancerStudyId, caseSet);
            Map<String,Map<String,ClinicalData>> clinicalData = new LinkedHashMap<String,Map<String,ClinicalData>>();
            for (ClinicalData cd : data) {
                String caseId = cd.getCaseId();
                Map<String,ClinicalData> msc = clinicalData.get(cd.getCaseId());
                if (msc==null) {
                    msc = new HashMap<String,ClinicalData>();
                    clinicalData.put(caseId, msc);
                }
                msc.put(cd.getAttrId(), cd);
            }

            ArrayList<Patient> toReturn = new ArrayList<Patient>();
            for (Map.Entry<String,Map<String,ClinicalData>> entry : clinicalData.entrySet()) {
                Patient patient = DaoPatient.getPatientByInternalId(Integer.parseInt(entry.getKey()));
                toReturn.add(new Patient(cancerStudy, patient.getStableId(), patient.getStableId(), entry.getValue()));
            }
            return toReturn;
	}

	/**************************************************************
	 * Previous DaoClinicalFreeForm class methods (accessors only)
	 *************************************************************/

	public static List<ClinicalParameterMap> getDataSlice(int cancerStudyId, Collection<String> attributeIds) throws DaoException {
		
        Map<String,Map<String, String>> mapAttrCaseValue = new HashMap<String,Map<String, String>>();
        for (ClinicalData cd : getDataByAttributeIds(cancerStudyId, attributeIds)) {

            String attrId = cd.getAttrId();
            String value = cd.getAttrVal();
            String caseId = cd.getCaseId();
                    
            if (value.isEmpty() || value.equals(ClinicalAttribute.NA)) {
                continue;
            }
                    
            Map<String, String> mapCaseValue = mapAttrCaseValue.get(attrId);
            if (mapCaseValue == null) {
                mapCaseValue = new HashMap<String, String>();
                mapAttrCaseValue.put(attrId, mapCaseValue);
            }
                    
            mapCaseValue.put(caseId, value);
        }
                
        List<ClinicalParameterMap> maps = new ArrayList<ClinicalParameterMap>();
        for (Map.Entry<String,Map<String, String>> entry : mapAttrCaseValue.entrySet()) {
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
	public static HashSet<String> getAllCases(int cancerStudyId) throws DaoException {

		HashSet<String> toReturn = new HashSet<String>();
		for (ClinicalData clinicalData : getData(cancerStudyId)) {
			toReturn.add(clinicalData.getCaseId());
		}

		return toReturn;
	}
	public static List<ClinicalData> getCasesByCancerStudy(int cancerStudyId) throws DaoException {

		return DaoClinicalData.getData(cancerStudyId);
	}

	public static List<ClinicalData> getCasesByCases(int cancerStudyId, List<String> caseIds) throws DaoException {

		return DaoClinicalData.getData(cancerStudyId, caseIds);
	}
        
    public static List<String> getCaseIdsByAttribute(int cancerStudyId, String paramName, String paramValue) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
            con = JdbcUtil.getDbConnection(DaoClinicalData.class);
            pstmt = con.prepareStatement ("SELECT INTERNAL_ID FROM `clinical_patient`"
                                          + " WHERE ATTR_ID=? AND ATTR_VALUE=?");
            pstmt.setString(1, paramName);
            pstmt.setString(2, paramValue);
            rs = pstmt.executeQuery();

            List<String> cases = new ArrayList<String>();
            List<Integer> patients = getPatientIdsByCancerStudy(cancerStudyId);

            while (rs.next())
            {
                Integer patientId = rs.getInt("INTERNAL_ID");
                if (patients.contains(patientId)) {
                    cases.add(patientId.toString());
                }
            }

            return cases;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoClinicalData.class, con, pstmt, rs);
        }
    }
}
