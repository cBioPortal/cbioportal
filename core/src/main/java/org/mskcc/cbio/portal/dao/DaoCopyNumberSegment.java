
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
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Sample sample = DaoSample.getSampleByStableId(seg.getCaseId());
        try {
            con = JdbcUtil.getDbConnection(DaoCopyNumberSegment.class);
            pstmt = con.prepareStatement
                    ("INSERT INTO copy_number_seg (`SAMPLE_ID`, `CHR`,"
                        + " `START`, `END`, `NUM_PROBES`, `SEGMENT_MEAN`, `CANCER_STUDY_ID`)"
                        + " VALUES (?,?,?,?,?,?,?)");
            pstmt.setInt(1, sample.getInternalId());
            pstmt.setString(2, seg.getChr());
            pstmt.setLong(3, seg.getStart());
            pstmt.setLong(4, seg.getEnd());
            pstmt.setInt(5, seg.getNumProbes());
            pstmt.setDouble(6, seg.getSegMean());
            pstmt.setInt(7, seg.getCancerStudyId());
            return pstmt.executeUpdate();
        } catch (NullPointerException e) {
            throw new DaoException(e);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCopyNumberSegment.class, con, pstmt, rs);
        }
    }
    
    public static List<CopyNumberSegment> getSegmentForACase(
            String caseId, int cancerStudyId) throws DaoException {
        return getSegmentForCases(Collections.singleton(caseId),cancerStudyId);
    }
    
    public static List<CopyNumberSegment> getSegmentForCases(
            Collection<String> caseIds, int cancerStudyId) throws DaoException {
        if (caseIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Integer> internalSampleIds = DaoSample.getInternalSampleIds(caseIds);        
        String concatCaseIds = "('"+StringUtils.join(internalSampleIds, "','")+"')";
        
        List<CopyNumberSegment> segs = new ArrayList<CopyNumberSegment>();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCopyNumberSegment.class);
            pstmt = con.prepareStatement
                    ("SELECT * FROM copy_number_seg"
                    + " WHERE `SAMPLE_ID` IN "+ concatCaseIds
                    + " AND `CANCER_STUDY_ID`="+cancerStudyId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Sample sample = DaoSample.getSampleByInternalId(rs.getInt("SAMPLE_ID"));
                CopyNumberSegment seg = new CopyNumberSegment(
                        rs.getInt("CANCER_STUDY_ID"),
                        sample.getStableId(),
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
    
    public static double getCopyNumberActeredFraction(String caseId,
            int cancerStudyId, double cutoff) throws DaoException {
        Double d = getCopyNumberActeredFraction(Collections.singleton(caseId), cancerStudyId, cutoff)
                .get(caseId);
        return d==null ? Double.NaN : d;
    }
    
    public static Map<String,Double> getCopyNumberActeredFraction(Collection<String> caseIds,
            int cancerStudyId, double cutoff) throws DaoException {
        Map<String,Long> alteredLength = getCopyNumberAlteredLength(caseIds, cancerStudyId, cutoff);
        Map<String,Long> measuredLength = getCopyNumberAlteredLength(caseIds, cancerStudyId, 0);
        Map<String,Double> fraction = new HashMap<String,Double>(alteredLength.size());
        for (String caseId : caseIds) {
            Long ml = measuredLength.get(caseId);
            if (ml==null || ml==0) {
                continue;
            }
            Long al = alteredLength.get(caseId);
            if (al==null) {
                al = (long) 0;
            }
            fraction.put(caseId, 1.0*al/ml);
        }
        return fraction;
    }
    
    private static Map<String,Long> getCopyNumberAlteredLength(Collection<String> caseIds,
            int cancerStudyId, double cutoff) throws DaoException {
        Map<String,Long> map = new HashMap<String,Long>();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql;
        List<Integer> internalSampleIds = DaoSample.getInternalSampleIds(caseIds);
        try {
            con = JdbcUtil.getDbConnection(DaoCopyNumberSegment.class);
            if (cutoff>0) {
                sql = "SELECT  `SAMPLE_ID`, SUM(`END`-`START`)"
                    + " FROM `copy_number_seg`"
                    + " WHERE `CANCER_STUDY_ID`="+cancerStudyId
                    + " AND ABS(`SEGMENT_MEAN`)>=" + cutoff
                    + " AND `SAMPLE_ID` IN ('" + StringUtils.join(internalSampleIds,"','") +"')"
                    + " GROUP BY `SAMPLE_ID`";
            } else {
                sql = "SELECT  `SAMPLE_ID`, SUM(`END`-`START`)"
                    + " FROM `copy_number_seg`"
                    + " WHERE `CANCER_STUDY_ID`="+cancerStudyId
                    + " AND `SAMPLE_ID` IN ('" + StringUtils.join(internalSampleIds,"','") +"')"
                    + " GROUP BY `SAMPLE_ID`";
            }
            
            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Sample sample = DaoSample.getSampleByInternalId(rs.getInt(1));
                map.put(sample.getStableId(), rs.getLong(2));
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
     * @param caseId
     * @return true if segment data exist for the case
     * @throws DaoException 
     */
    public static boolean segmentDataExistForCase(int cancerStudyId, String caseId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Sample sample = DaoSample.getSampleByStableId(caseId);
        try {
            con = JdbcUtil.getDbConnection(DaoCopyNumberSegment.class);
            pstmt = con.prepareStatement("SELECT EXISTS(SELECT 1 FROM `copy_number_seg`"
                + " WHERE `CANCER_STUDY_ID`=? AND `SAMPLE_ID`=?");
            pstmt.setInt(1, cancerStudyId);
            pstmt.setInt(2, sample.getInternalId());
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
