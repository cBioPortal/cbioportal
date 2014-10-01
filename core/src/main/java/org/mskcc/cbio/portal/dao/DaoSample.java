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
 * DAO to `sample`.
 * 
 * @author Benjamin Gross
 */
public class DaoSample {

    private static final int MISSING_CANCER_STUDY_ID = -1;

    private static final Map<String, Sample> byStableId = new ConcurrentHashMap<String, Sample>();
    private static final Map<Integer, Sample> byInternalId = new ConcurrentHashMap<Integer, Sample>();
    private static final Map<Integer, HashSet<Sample>> byInternalPatientId = new ConcurrentHashMap<Integer, HashSet<Sample>>();
    private static final MultiKeyMap byInternalPatientAndStableSampleId = new MultiKeyMap();
    private static final Map<String, HashSet<Sample>> byCancerTypeId = new ConcurrentHashMap<String, HashSet<Sample>>();
    private static final Map<String, HashSet<Sample>> normalsByCancerTypeId = new ConcurrentHashMap<String, HashSet<Sample>>();
    private static final MultiKeyMap byCancerIdAndStableSampleId = new MultiKeyMap();

    static {
        cache();
    }

    private static void clearCache()
    {
        byStableId.clear();
        byInternalId.clear();
        byInternalPatientId.clear();
        byInternalPatientAndStableSampleId.clear();
        byCancerTypeId.clear();
        byCancerIdAndStableSampleId.clear();
    }

    private static void cache()
    {
        clearCache();

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoSample.class);
            pstmt = con.prepareStatement("SELECT * FROM sample");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                cacheSample(extractSample(rs));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            JdbcUtil.closeAll(DaoSample.class, con, pstmt, rs);
        }
    }

    private static void cacheSample(Sample sample)
    {
        if (sample.getType().isNormal()) {
            if (!normalsByCancerTypeId.containsKey(sample.getCancerTypeId())) {
                normalsByCancerTypeId.put(sample.getCancerTypeId(), new HashSet<Sample>());
            }
            normalsByCancerTypeId.get(sample.getCancerTypeId()).add(sample);
        }
        cacheSample(sample, getCancerStudyId(sample));
    }

    private static int getCancerStudyId(Sample sample)
    {
        Patient patient = DaoPatient.getPatientById(sample.getInternalPatientId());
        return (patient == null) ? MISSING_CANCER_STUDY_ID : patient.getCancerStudy().getInternalId();
    }

    private static void cacheSample(Sample sample, int cancerStudyId)
    {
        if (!byStableId.containsKey(sample.getStableId())) {
            byStableId.put(sample.getStableId(), sample);
        }

        if (!byInternalId.containsKey(sample.getInternalId())) {
            byInternalId.put(sample.getInternalId(), sample);
        }

        if (!byInternalPatientId.containsKey(sample.getInternalPatientId())) {
            byInternalPatientId.put(sample.getInternalPatientId(), new HashSet<Sample>());
        }
        byInternalPatientId.get(sample.getInternalPatientId()).add(sample);

        if (!byInternalPatientAndStableSampleId.containsKey(sample.getInternalPatientId(),
                                                            sample.getStableId())) {
            byInternalPatientAndStableSampleId.put(sample.getInternalPatientId(),
                                                    sample.getStableId(), sample);
        }

        if (!byCancerTypeId.containsKey(sample.getCancerTypeId())) {
            byCancerTypeId.put(sample.getCancerTypeId(), new HashSet<Sample>());
        }
        byCancerTypeId.get(sample.getCancerTypeId()).add(sample);

        if (!byCancerIdAndStableSampleId.containsKey(cancerStudyId, sample.getStableId())) {
            byCancerIdAndStableSampleId.put(cancerStudyId, sample.getStableId(), sample);
        }
    }

    public static int addSample(Sample sample) throws DaoException
    {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoSample.class);
            pstmt = con.prepareStatement("INSERT INTO sample " +
                                         "( `STABLE_ID`, `SAMPLE_TYPE`, `PATIENT_ID`, `TYPE_OF_CANCER_ID` ) " +
                                         "VALUES (?,?,?,?)",
                                         Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, sample.getStableId());
            pstmt.setString(2, sample.getType().toString());
            pstmt.setInt(3, sample.getInternalPatientId());
            pstmt.setString(4, sample.getCancerTypeId());
            pstmt.executeUpdate();
            rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                Patient patient = DaoPatient.getPatientById(sample.getInternalPatientId());
                cacheSample(new Sample(rs.getInt(1), sample.getStableId(),
                                       sample.getInternalPatientId(), sample.getCancerTypeId()));
                return rs.getInt(1);
            }
            return -1;
        }
        catch (SQLException e) {
            throw new DaoException(e);
        }
        finally {
            JdbcUtil.closeAll(DaoSample.class, con, pstmt, rs);
        }
    }

    public static List<Sample> getAllSamples()
    {
        return (byStableId.isEmpty()) ? Collections.<Sample>emptyList() :
            new ArrayList<Sample>(byStableId.values());
    }

    public static Sample getSampleById(int internalId)
    {
        return byInternalId.get(internalId);
    }

    public static List<Sample> getSamplesByPatientId(int internalPatientId)
    {
        return (byInternalPatientId.isEmpty() || !byInternalPatientId.containsKey(internalPatientId)) ? Collections.<Sample>emptyList() :
            new ArrayList<Sample>(byInternalPatientId.get(internalPatientId));
    }

    public static Sample getSampleByPatientAndSampleId(int internalPatientId, String stableSampleId)
    {
        return (Sample)byInternalPatientAndStableSampleId.get(internalPatientId, stableSampleId);
    }

    public static List<Sample> getSamplesByCancerTypeId(String cancerTypeId)
    {
        return (byCancerTypeId.isEmpty() || !byCancerTypeId.containsKey(cancerTypeId)) ? Collections.<Sample>emptyList() :
            new ArrayList<Sample>(byCancerTypeId.get(cancerTypeId));
    }

    public static Sample getSampleByCancerStudyAndSampleId(int cancerStudyId, String stableSampleId)
    {
        return (Sample)byCancerIdAndStableSampleId.get(cancerStudyId, stableSampleId);
    }

    public static void deleteAllRecords() throws DaoException
    {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoSample.class);
            pstmt = con.prepareStatement("TRUNCATE TABLE sample");
            pstmt.executeUpdate();
        }
        catch (SQLException e) {
            throw new DaoException(e);
        }
        finally {
            JdbcUtil.closeAll(DaoSample.class, con, pstmt, rs);
        }

        clearCache();
    }

    private static Sample extractSample(ResultSet rs) throws SQLException
    {
        return new Sample(rs.getInt("INTERNAL_ID"),
                          rs.getString("STABLE_ID"),
                          rs.getInt("PATIENT_ID"),
                          rs.getString("TYPE_OF_CANCER_ID"));
    }
}
