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

import org.mskcc.cbio.portal.model.Sample;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DAO to `sample`.
 * 
 * @author Benjamin Gross
 */
public class DaoSample {

    private static final Map<String, Sample> byStableId = new ConcurrentHashMap<String, Sample>();
    private static final Map<Integer, Sample> byInternalId = new ConcurrentHashMap<Integer, Sample>();
    private static final Map<Integer, HashSet<Sample>> byInternalPatientId = new ConcurrentHashMap<Integer, HashSet<Sample>>();
    private static final Map<String, HashSet<Sample>> byCancerTypeId = new ConcurrentHashMap<String, HashSet<Sample>>();

    static {
        cache();
    }

    private static void clearCache()
    {
        byStableId.clear();
        byInternalId.clear();
        byInternalPatientId.clear();
        byCancerTypeId.clear();
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
            ArrayList<Sample> list = new ArrayList<Sample>();
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
        byStableId.put(sample.getStableId(), sample);
        byInternalId.put(sample.getInternalId(), sample);

        if (!byInternalPatientId.containsKey(sample.getInternalPatientId())) {
            byInternalPatientId.put(sample.getInternalPatientId(), new HashSet<Sample>());
        }
        byInternalPatientId.get(sample.getInternalPatientId()).add(sample);

        if (!byCancerTypeId.containsKey(sample.getCancerTypeId())) {
            byCancerTypeId.put(sample.getCancerTypeId(), new HashSet<Sample>());
        }
        byCancerTypeId.get(sample.getCancerTypeId()).add(sample);
    }

    public static void addSample(Sample sample) throws DaoException
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
                cacheSample(new Sample(rs.getInt(1), sample.getStableId(), sample.getType().toString(),
                                       sample.getInternalPatientId(), sample.getCancerTypeId()));
            }
        }
        catch (SQLException e) {
            throw new DaoException(e);
        }
        finally {
            JdbcUtil.closeAll(DaoSample.class, con, pstmt, rs);
        }
    }

    public static Sample getSampleByInternalId(int internalId)
    {
        return byInternalId.get(internalId);
    }

    public static Sample getSampleByStableId(String stableId)
    {
        return byStableId.get(stableId);
    }

    public static List<Sample> getAllSamples()
    {
        return new ArrayList<Sample>(byStableId.values());
    }

    public static List<Sample> getSamplesByInternalPatientId(int internalPatientId)
    {
        return new ArrayList<Sample>(byInternalPatientId.get(internalPatientId));
    }

    public static List<Sample> getSamplesByCancerTypeId(String cancerTypeId)
    {
        return new ArrayList<Sample>(byCancerTypeId.get(cancerTypeId));
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
                          rs.getString("SAMPLE_TYPE"),
                          rs.getInt("PATIENT_ID"),
                          rs.getString("TYPE_OF_CANCER_ID"));
    }
}
