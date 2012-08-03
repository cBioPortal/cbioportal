
package org.mskcc.cbio.cgds.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import org.apache.commons.lang.StringUtils;
import org.mskcc.cbio.cgds.model.CopyNumberSegment;

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
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("INSERT INTO copy_number_seg (`CASE_ID`, `CHR`,"
                        + " `START`, `END`, `NUM_PROBES`, `SEGMENT_MEAN`)"
                        + " VALUES (?,?,?,?,?,?)");
            pstmt.setString(1, seg.getCaseId());
            pstmt.setString(2, seg.getChr());
            pstmt.setLong(3, seg.getStart());
            pstmt.setLong(4, seg.getEnd());
            pstmt.setInt(5, seg.getNumProbes());
            pstmt.setDouble(6, seg.getSegMean());
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }
    
    public static List<CopyNumberSegment> getSegmentForACase(
            String caseId) throws DaoException {
        return getSegmentForCases(Collections.singleton(caseId));
    }
    
    public static List<CopyNumberSegment> getSegmentForCases(
            Collection<String> caseIds) throws DaoException {
        if (caseIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        String concatCaseIds = "('"+StringUtils.join(caseIds, "','")+"')";
        
        List<CopyNumberSegment> segs = new ArrayList<CopyNumberSegment>();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT * FROM copy_number_seg WHERE `CASE_ID` IN "
                    + concatCaseIds);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                CopyNumberSegment seg = new CopyNumberSegment(
                        rs.getString("CASE_ID"),
                        rs.getString("CHR"),
                        rs.getLong("START"),
                        rs.getLong("END"),
                        rs.getInt("NUM_PROBES"),
                        rs.getDouble("SEGMENT_MEAN"));
                seg.setSegId(rs.getLong("SEG_ID"));
                segs.add(seg);
            }
            return segs;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }
    
    public static double getCopyNumberActeredFraction(String caseId,
            double cutoff) throws DaoException {
        Double d = getCopyNumberActeredFraction(Collections.singleton(caseId), cutoff)
                .get(caseId);
        return d==null ? Double.NaN : d.doubleValue();
    }
    
    public static Map<String,Double> getCopyNumberActeredFraction(Collection<String> caseIds,
            double cutoff) throws DaoException {
        Map<String,Long> alteredLength = getCopyNumberAlteredLength(caseIds, cutoff);
        Map<String,Long> measuredLength = getCopyNumberAlteredLength(caseIds, 0);
        Map<String,Double> fraction = new HashMap<String,Double>(alteredLength.size());
        for (String caseId : caseIds) {
            Long ml = measuredLength.get(caseId);
            if (ml==null || ml==0) {
                continue;
            }
            long al = alteredLength.get(caseId);
            fraction.put(caseId, 1.0*al/ml);
        }
        return fraction;
    }
    
    private static Map<String,Long> getCopyNumberAlteredLength(Collection<String> caseIds,
            double cutoff) throws DaoException {
        Map<String,Long> map = new HashMap<String,Long>();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = null;
        try {
            con = JdbcUtil.getDbConnection();
            if (cutoff>0) {
                sql = "SELECT  `CASE_ID`, SUM(`END`-`START`)"
                    + " FROM `copy_number_seg`"
                    + " WHERE ABS(`SEGMENT_MEAN`)>=" + cutoff
                    + " AND `CASE_ID` IN ('" + StringUtils.join(caseIds,"','") +"')"
                    + " GROUP BY `CASE_ID`";
            } else {
                sql = "SELECT  `CASE_ID`, SUM(`END`-`START`)"
                    + " FROM `copy_number_seg`"
                    + " WHERE `CASE_ID` IN ('" + StringUtils.join(caseIds,"','") +"')"
                    + " GROUP BY `CASE_ID`";
            }
            
            System.out.println(sql);
            
            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                map.put(rs.getString(1), rs.getLong(2));
            }
            
            return map;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }
}
