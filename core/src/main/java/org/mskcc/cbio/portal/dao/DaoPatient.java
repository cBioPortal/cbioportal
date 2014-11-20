/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center 
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center 
 * has been advised of the possibility of such damage.
*/

package org.mskcc.cbio.portal.dao;

import org.mskcc.cbio.portal.model.*;

import org.apache.commons.collections.map.MultiKeyMap;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DAO to `patient`.
 * 
 * @author Benjamin Gross
 */
public class DaoPatient {

    private static final Map<Integer, Patient> byInternalId = new ConcurrentHashMap<Integer, Patient>();
    private static final Map<Integer, Set<Patient>> byInternalCancerStudyId = new ConcurrentHashMap<Integer, Set<Patient>>();
    private static final MultiKeyMap byCancerIdAndStablePatientId = new MultiKeyMap();

    static {
        cache();
    }

    private static void clearCache()
    {
        byInternalId.clear();
        byInternalCancerStudyId.clear();
        byCancerIdAndStablePatientId.clear();
    }

    private static void cache()
    {
        clearCache();

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoPatient.class);
            pstmt = con.prepareStatement("SELECT * FROM patient");
            rs = pstmt.executeQuery();
            ArrayList<Patient> list = new ArrayList<Patient>();
            while (rs.next()) {
                Patient p = extractPatient(rs);
                if (p != null) {
                    cachePatient(p, p.getCancerStudy().getInternalId());
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            JdbcUtil.closeAll(DaoPatient.class, con, pstmt, rs);
        }
    }

    public static void cachePatient(Patient patient, int cancerStudyId)
    {
        if (!byInternalId.containsKey(patient.getInternalId())) {
            byInternalId.put(patient.getInternalId(), patient);
        } 
        if (byInternalCancerStudyId.containsKey(patient.getCancerStudy().getInternalId())) {
            byInternalCancerStudyId.get(patient.getCancerStudy().getInternalId()).add(patient);
        }
        else {
            Set<Patient> patientList = new HashSet<Patient>();
            patientList.add(patient);
            byInternalCancerStudyId.put(patient.getCancerStudy().getInternalId(), patientList);
        }

        if (!byCancerIdAndStablePatientId.containsKey(cancerStudyId, patient.getStableId())) {
            byCancerIdAndStablePatientId.put(cancerStudyId, patient.getStableId(), patient);
        }
    }

    public static int addPatient(Patient patient) throws DaoException
    {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoPatient.class);
            pstmt = con.prepareStatement("INSERT INTO patient (`STABLE_ID`, `CANCER_STUDY_ID`) VALUES (?,?)",
                                         Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, patient.getStableId());
            pstmt.setInt(2, patient.getCancerStudy().getInternalId());
            pstmt.executeUpdate();
            rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                cachePatient(new Patient(patient.getCancerStudy(), patient.getStableId(), rs.getInt(1)), patient.getCancerStudy().getInternalId());
                return rs.getInt(1);
            }
            return -1;
        }
        catch (SQLException e) {
            throw new DaoException(e);
        }
        finally {
            JdbcUtil.closeAll(DaoPatient.class, con, pstmt, rs);
        }
    }

    public static Patient getPatientById(int internalId)
    {
        return byInternalId.get(internalId);
    }

    public static Patient getPatientByCancerStudyAndPatientId(int cancerStudyId, String stablePatientId) 
    {
        return (Patient)byCancerIdAndStablePatientId.get(cancerStudyId, stablePatientId);
    }

    public static Set<Patient> getPatientsByCancerStudyId(int cancerStudyId)
    {
        return byInternalCancerStudyId.get(cancerStudyId);
    }

    public static List<Patient> getAllPatients()
    {
        return (byInternalId.isEmpty()) ? Collections.<Patient>emptyList() :
            new ArrayList<Patient>(byInternalId.values());
    }

    public static void deleteAllRecords() throws DaoException
    {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoPatient.class);
            pstmt = con.prepareStatement("TRUNCATE TABLE patient");
            pstmt.executeUpdate();
        }
        catch (SQLException e) {
            throw new DaoException(e);
        }
        finally {
            JdbcUtil.closeAll(DaoPatient.class, con, pstmt, rs);
        }

        clearCache();
    }

    private static Patient extractPatient(ResultSet rs) throws SQLException
    {
        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByInternalId(rs.getInt("CANCER_STUDY_ID"));
        if (cancerStudy == null) return null;
        return new Patient(cancerStudy,
                           rs.getString("STABLE_ID"),
                           rs.getInt("INTERNAL_ID"));
    }
}
