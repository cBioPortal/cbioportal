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
            JdbcUtil.closeAll(DaoPatient.class, con, pstmt, rs);
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
        getDatum(caseId, attrId);
    }
    public static ClinicalData getDatum(String stableId, String attrId) throws DaoException
    {
        return getDatum(getAttributeTable(attrId),
                        getInternalId(attrId, stableId),
                        attrId);
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

    private static ClinicalData getDatum(String table, int internalId, String attrId) throws DaoException
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
                return extract(rs);
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

	public static List<ClinicalData> getCasesById(int cancerStudyId, String stableId) throws DaoException
    {
		return getDataByStableId(stableId);
	}

    public static List<ClinicalData> getDataByStableId(String stableId) throws DaoException
    {
        List<Integer> internalIds = new ArrayList<Integer>();
        if (DaoPatient.getPatientByStableId(stableId) != null) {
            internalIds.add(DaoPatient.getPatientByStableId(stableId).getInternalId());
            return getDataByInternalIds(PATIENT_TABLE, internalIds);
        }
        else if (DaoSample.getSampleByStableId(stableId) != null) {
            internalIds.add(DaoSample.getSampleByStableId(stableId).getInternalId());
            return getDataByInternalIds(SAMPLE_TABLE, internalIds);
        }
        else {
            throw new DaoException("Unknown stableId: " + stableId);
        }
    }

    private static List<ClinicalData> getDataByInternalIds(String table, List<Integer> internalIds) throws DaoException
    {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        List<ClinicalData> clinicals = new ArrayList<ClinicalData>();
        String sql = ("SELECT * FROM " + table + "WHERE `INTERNAL_ID` IN " +
                      "(" + generateCaseIdsSqlFromInt(internalIds) + ")");
                      
        try {
            con = JdbcUtil.getDbConnection(DaoClinicalData.class);
            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                clinicals.add(extract(rs));
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
        List<Integer> patientIds = new ArrayList<Integer>();
        for (Patient patient : DaoPatient.getPatientsByStableCancerStudyId(cancerStudyId)) {
            patientIds.add(patient.getInternalId());
        }

        return getData(PATIENT_TABLE, patientIds);
	}

    public static List<ClinicalData> getData(String cancerStudyId, Collection<String> stableIds) throws DaoException
    {
        List<Integer> patientIds = new ArrayList<Integer>();
        for (String patientId : stableIds) {
            patientIds.add(DaoPatient.getPatientByStableId(patientId).getInternalId());
        }

		return getDataByInternalIds(PATIENT_TABLE, patientIds);
	}




    public static List<ClinicalData> getData(String cancerStudyId, Collection<String> caseIds, ClinicalAttribute attr) throws DaoException {
        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyId);

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        List<ClinicalData> clinicals = new ArrayList<ClinicalData>();

        String caseIdsSql = generateCaseIdsSql(caseIds);

        String sql = "SELECT * FROM clinical WHERE"
                + " `CANCER_STUDY_ID`=" + "'" + cancerStudy.getInternalId() + "'"
                + " AND `ATTR_ID`=" + "'" + attr.getAttrId() + "'"
                + " AND `PATIENT_OR_SAMPLE_ID` IN (" + caseIdsSql + ")";

        try {
            con = JdbcUtil.getDbConnection(DaoClinicalData.class);
            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while(rs.next()) {
                clinicals.add(extract(rs));
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        }
        finally {
                JdbcUtil.closeAll(DaoClinicalData.class, con, pstmt, rs);
        }
        return clinicals;
    }

    public static List<ClinicalData> getDataByAttributeIds(int cancerStudyId, Collection<String> attributeIds) throws DaoException {

		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		List<ClinicalData> clinicals = new ArrayList<ClinicalData>();

		try {
            con = JdbcUtil.getDbConnection(DaoClinicalData.class);

            pstmt = con.prepareStatement("SELECT * FROM clinical WHERE" +
                    " CANCER_STUDY_ID= " + cancerStudyId +
                    " AND ATTR_ID IN ('"+ StringUtils.join(attributeIds, "','")+"') ");

            rs = pstmt.executeQuery();
            while(rs.next()) {
                clinicals.add(extract(rs));
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

    /**
     * Generates a comma separated string of caseIds
     *
     * @param caseIds
     * @return
     */
    private static String generateCaseIdsSql(Collection<String> caseIds) {
        return "'" + StringUtils.join(caseIds, "','") + "'";
    }

    private static String generateCaseIdsSqlFromInt(Collection<Integer> caseIds) {
        return "'" + StringUtils.join(caseIds, "','") + "'";
    }

    /**
     * Turns a result set into a <code>ClinicalData</code> object
     *
     * returns null on failure to extract
     *
     * @param rs
     * @return
     * @throws SQLException
     */
    private static ClinicalData extract(ResultSet rs) throws SQLException {
		return new ClinicalData(rs.getInt("CANCER_STUDY_ID"),
								rs.getString("PATIENT_OR_SAMPLE_ID"),
								rs.getString("ATTR_ID"),
								rs.getString("ATTR_VALUE"));
    }

    /**
     * Deletes all Records.
     * @throws DaoException DAO Error.
     */
    public static void deleteAllRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoClinicalData.class);
            pstmt = con.prepareStatement("TRUNCATE TABLE clinical");
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
	
	public static Patient getSurvivalData(int cancerStudyId, String _case)  throws DaoException {
            List<Patient> patients = getSurvivalData(cancerStudyId, Collections.singleton(_case));
            return patients.isEmpty() ? null : patients.get(0);
	}

	public static List<Patient> getSurvivalData(int cancerStudyId, Collection<String> caseSet) throws DaoException {
            List<ClinicalData> data = getData(cancerStudyId, caseSet);
            CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByInternalId(cancerStudyId);
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
                toReturn.add(new Patient(cancerStudy, entry.getKey(), entry.getKey(), entry.getValue()));
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

		if (cancerStudyId < 0) {
			throw new IllegalArgumentException("Invalid cancer study id: [" + cancerStudyId + "]");
		}

		HashSet<String> toReturn = new HashSet<String>();
		for (ClinicalData clinicalData : DaoClinicalData.getData(cancerStudyId)) {
			toReturn.add(clinicalData.getAttrId());
		}

		return toReturn;
	}
	public static HashSet<String> getAllCases (int cancerStudyId) throws DaoException {

		if (cancerStudyId < 0) {
			throw new IllegalArgumentException("Invalid cancer study id: [" + cancerStudyId + "]");
		}

		HashSet<String> toReturn = new HashSet<String>();
		for (ClinicalData clinicalData : DaoClinicalData.getData(cancerStudyId)) {
			toReturn.add(clinicalData.getCaseId());
		}

		return toReturn;
	}
	public static List<ClinicalData> getCasesByCancerStudy(int cancerStudyId) throws DaoException {

		if (cancerStudyId < 0) {
			throw new IllegalArgumentException("Invalid cancer study id: [" + cancerStudyId + "]");
		}

		return DaoClinicalData.getData(cancerStudyId);
	}

	public static List<ClinicalData> getCasesByCases(int cancerStudyId, List<String> caseIds) throws DaoException {

		if (cancerStudyId < 0 || caseIds.isEmpty()) {
			throw new IllegalArgumentException("Invalid cancer study or case id set size: [" +
											   cancerStudyId + ", " + caseIds.size() + "]");
		}

		return DaoClinicalData.getData(cancerStudyId, caseIds);
	}
        
    public static List<String> getCaseIdsByAttribute(int cancerStudyId, String paramName, String paramValue) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
            con = JdbcUtil.getDbConnection(DaoClinicalData.class);
            pstmt = con.prepareStatement ("SELECT PATIENT_OR_SAMPLE_ID FROM `clinical`"
                                          + "WHERE CANCER_STUDY_ID=? AND ATTR_ID=? AND ATTR_VALUE=?");
            pstmt.setInt(1, cancerStudyId);
            pstmt.setString(2, paramName);
            pstmt.setString(3, paramValue);
            rs = pstmt.executeQuery();

            List<String> cases = new ArrayList<String>();

            while (rs.next())
            {
                cases.add(rs.getString("PATIENT_OR_SAMPLE_ID"));
            }

            return cases;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoClinicalData.class, con, pstmt, rs);
        }

    }
}
