
package org.mskcc.cbio.portal.dao;

import org.mskcc.cbio.portal.model.*;

import org.apache.commons.lang.StringUtils;

import java.sql.*;
import java.util.*;

/**
 *
 * @author jgao
 */
public final class DaoCopyNumberSegment {
    private DaoCopyNumberSegment() {}
    
    public static int addCopyNumberSegment(CopyNumberSegment seg) throws DaoException {
        if (!MySQLbulkLoader.isBulkLoad()) {
            throw new DaoException("You have to turn on MySQLbulkLoader in order to insert mutations");
        } else {
            MySQLbulkLoader.getMySQLbulkLoader("copy_number_seg").insertRecord(
                    Long.toString(seg.getSegId()),
                    Integer.toString(seg.getCancerStudyId()),
                    Integer.toString(seg.getSampleId()),
                    seg.getChr(),
                    Long.toString(seg.getStart()),
                    Long.toString(seg.getEnd()),
                    Integer.toString(seg.getNumProbes()),
                    Double.toString(seg.getSegMean())
            );
            return 1;
        }
    }
    
    public static long getLargestId() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);
            pstmt = con.prepareStatement
                    ("SELECT MAX(`SEG_ID`) FROM `copy_number_seg`");
            rs = pstmt.executeQuery();
            return rs.next() ? rs.getLong(1) : 0;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
    }
    
    public static List<CopyNumberSegment> getSegmentForASample(
            int sampleId, int cancerStudyId) throws DaoException {
        return getSegmentForSamples(Collections.singleton(sampleId),cancerStudyId);
    }
    
    public static List<CopyNumberSegment> getSegmentForSamples(
            Collection<Integer> sampleIds, int cancerStudyId) throws DaoException {
        if (sampleIds.isEmpty()) {
            return Collections.emptyList();
        }
        String concatSampleIds = "('"+StringUtils.join(sampleIds, "','")+"')";
        
        List<CopyNumberSegment> segs = new ArrayList<CopyNumberSegment>();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCopyNumberSegment.class);
            pstmt = con.prepareStatement
                    ("SELECT * FROM copy_number_seg"
                    + " WHERE `SAMPLE_ID` IN "+ concatSampleIds
                    + " AND `CANCER_STUDY_ID`="+cancerStudyId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                CopyNumberSegment seg = new CopyNumberSegment(
                        rs.getInt("CANCER_STUDY_ID"),
                        rs.getInt("SAMPLE_ID"),
                        rs.getString("CHR"),
                        rs.getLong("START"),
                        rs.getLong("END"),
                        rs.getInt("NUM_PROBES"),
                        rs.getDouble("SEGMENT_MEAN"));
                seg.setSegId(rs.getLong("SEG_ID"));
                segs.add(seg);
            }
            return segs;
        } catch (NullPointerException e) {
            throw new DaoException(e);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCopyNumberSegment.class, con, pstmt, rs);
        }
    }
    
    public static double getCopyNumberActeredFraction(int sampleId,
            int cancerStudyId, double cutoff) throws DaoException {
        Double d = getCopyNumberActeredFraction(Collections.singleton(sampleId), cancerStudyId, cutoff)
                .get(sampleId);
        return d==null ? Double.NaN : d;
    }
    
    public static Map<Integer,Double> getCopyNumberActeredFraction(Collection<Integer> sampleIds,
            int cancerStudyId, double cutoff) throws DaoException {
        Map<Integer,Long> alteredLength = getCopyNumberAlteredLength(sampleIds, cancerStudyId, cutoff);
        Map<Integer,Long> measuredLength = getCopyNumberAlteredLength(sampleIds, cancerStudyId, 0);
        Map<Integer,Double> fraction = new HashMap<Integer,Double>(alteredLength.size());
        for (Integer sampleId : sampleIds) {
            Long ml = measuredLength.get(sampleId);
            if (ml==null || ml==0) {
                continue;
            }
            Long al = alteredLength.get(sampleId);
            if (al==null) {
                al = (long) 0;
            }
            fraction.put(sampleId, 1.0*al/ml);
        }
        return fraction;
    }
    
    private static Map<Integer,Long> getCopyNumberAlteredLength(Collection<Integer> sampleIds,
            int cancerStudyId, double cutoff) throws DaoException {
        Map<Integer,Long> map = new HashMap<Integer,Long>();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql;
        try {
            con = JdbcUtil.getDbConnection(DaoCopyNumberSegment.class);
            if (cutoff>0) {
                sql = "SELECT  `SAMPLE_ID`, SUM(`END`-`START`)"
                    + " FROM `copy_number_seg`"
                    + " WHERE `CANCER_STUDY_ID`="+cancerStudyId
                    + " AND ABS(`SEGMENT_MEAN`)>=" + cutoff
                    + " AND `SAMPLE_ID` IN ('" + StringUtils.join(sampleIds,"','") +"')"
                    + " GROUP BY `SAMPLE_ID`";
            } else {
                sql = "SELECT  `SAMPLE_ID`, SUM(`END`-`START`)"
                    + " FROM `copy_number_seg`"
                    + " WHERE `CANCER_STUDY_ID`="+cancerStudyId
                    + " AND `SAMPLE_ID` IN ('" + StringUtils.join(sampleIds,"','") +"')"
                    + " GROUP BY `SAMPLE_ID`";
            }
            
            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                map.put(rs.getInt(1), rs.getLong(2));
            }
            
            return map;
        } catch (NullPointerException e) {
            throw new DaoException(e);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCopyNumberSegment.class, con, pstmt, rs);
        }
    }
    
    /**
     * 
     * @param cancerStudyId
     * @return true if segment data exist for the cancer study
     * @throws DaoException 
     */
    public static boolean segmentDataExistForCancerStudy(int cancerStudyId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCopyNumberSegment.class);
            pstmt = con.prepareStatement("SELECT EXISTS (SELECT 1 FROM `copy_number_seg` WHERE `CANCER_STUDY_ID`=?)");
            pstmt.setInt(1, cancerStudyId);
            rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1)==1;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCopyNumberSegment.class, con, pstmt, rs);
        }
    }
    
    /**
     * 
     * @param cancerStudyId
     * @param sampleId
     * @return true if segment data exist for the case
     * @throws DaoException 
     */
    public static boolean segmentDataExistForSample(int cancerStudyId, int sampleId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCopyNumberSegment.class);
            pstmt = con.prepareStatement("SELECT EXISTS(SELECT 1 FROM `copy_number_seg`"
                + " WHERE `CANCER_STUDY_ID`=? AND `SAMPLE_ID`=?");
            pstmt.setInt(1, cancerStudyId);
            pstmt.setInt(2, sampleId);
            rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1)==1;
        } catch (NullPointerException e) {
            throw new DaoException(e);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCopyNumberSegment.class, con, pstmt, rs);
        }
    }
}
