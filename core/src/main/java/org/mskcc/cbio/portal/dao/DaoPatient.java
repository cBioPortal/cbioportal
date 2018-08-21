/*
 * Copyright (c) 2015 - 2018 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.dao;

import java.sql.*;
import java.util.*;
import org.apache.commons.collections.map.MultiKeyMap;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.Patient;

/**
 * Helper class for reCache() method
 */
class PrimitivePatientRecord {
    private int cancerStudyId;
    private String stableId;
    private int internalId;
    public PrimitivePatientRecord(int cancerStudyId, String stableId, int internalId) {
        this.cancerStudyId = cancerStudyId;
        this.stableId = stableId;
        this.internalId = internalId;
    }
    public int getCancerStudyId() {
        return cancerStudyId;
    }
    public String getStableId() {
        return stableId;
    }
    public int getInternalId() {
        return internalId;
    }
}

/**
 * DAO to `patient`.
 * 
 * @author Benjamin Gross
 */
public class DaoPatient {

    private static final Map<Integer, Patient> byInternalId = new HashMap<Integer, Patient>();
    private static final Map<Integer, Set<Patient>> byInternalCancerStudyId = new HashMap<Integer, Set<Patient>>();
    private static final MultiKeyMap byCancerIdAndStablePatientId = new MultiKeyMap();

    private static void clearCache()
    {
        byInternalId.clear();
        byInternalCancerStudyId.clear();
        byCancerIdAndStablePatientId.clear();
    }

    public static synchronized void reCache()
    {
        clearCache();
        ArrayList<PrimitivePatientRecord> patientRecordsRetrieved = new ArrayList<>();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoPatient.class);
            pstmt = con.prepareStatement("SELECT * FROM patient");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                int cancerStudyId = rs.getInt("CANCER_STUDY_ID");
                String stableId = rs.getString("STABLE_ID");
                int internalId = rs.getInt("INTERNAL_ID");
                PrimitivePatientRecord primitivePatientRecord = new PrimitivePatientRecord(cancerStudyId, stableId, internalId);
                patientRecordsRetrieved.add(primitivePatientRecord);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            JdbcUtil.closeAll(DaoPatient.class, con, pstmt, rs);
        }
        for (PrimitivePatientRecord primitivePatientRecord : patientRecordsRetrieved) {
            int cancerStudyId = primitivePatientRecord.getCancerStudyId();
            CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByInternalId(cancerStudyId);
            if (cancerStudy != null) {
                Patient patient = new Patient(cancerStudy, primitivePatientRecord.getStableId(), primitivePatientRecord.getInternalId());
                cachePatient(patient);
            }
        }
    }

    public static void cachePatient(Patient patient)
    {
        int cancerStudyId = patient.getCancerStudy().getInternalId();
        if (!byInternalId.containsKey(patient.getInternalId())) {
            byInternalId.put(patient.getInternalId(), patient);
        } 
        if (byInternalCancerStudyId.containsKey(cancerStudyId)) {
            byInternalCancerStudyId.get(cancerStudyId).add(patient);
        }
        else {
            Set<Patient> patientList = new HashSet<Patient>();
            patientList.add(patient);
            byInternalCancerStudyId.put(cancerStudyId, patientList);
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
                cachePatient(new Patient(patient.getCancerStudy(), patient.getStableId(), rs.getInt(1)));
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
            JdbcUtil.disableForeignKeyCheck(con);
            pstmt = con.prepareStatement("TRUNCATE TABLE patient");
            pstmt.executeUpdate();
            JdbcUtil.enableForeignKeyCheck(con);
        }
        catch (SQLException e) {
            throw new DaoException(e);
        }
        finally {
            JdbcUtil.closeAll(DaoPatient.class, con, pstmt, rs);
        }

        clearCache();
    }

}
