
package org.mskcc.cgds.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.mskcc.cgds.model.CopyNumberSegment;

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
                    ("INSERT INTO copy_number_seg (`CASE_ID`, `CHROMOSOME`,"
                        + " `START`, `END`, `NUM_PROBES`, `SEGMENT_MEAN`)"
                        + " VALUES (?,?,?,?,?,?)");
            pstmt.setString(1, seg.getCaseId());
            pstmt.setInt(2, seg.getChromosome());
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
    
    public static Map<String, CopyNumberSegment> getSegmentForACase(
            String caseId) throws DaoException {
        return getSegmentForCases(Collections.singleton(caseId));
    }
    
    public static Map<String, CopyNumberSegment> getSegmentForCases(
            Set<String> caseIds) throws DaoException {
        if (caseIds.isEmpty()) {
            return Collections.emptyMap();
        }
        
        String concatCaseIds = "('"+StringUtils.join(caseIds, "','")+"')";
        
        Map<String, CopyNumberSegment> map = new HashMap<String, CopyNumberSegment>();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT * FROM copy_number_seg WHERE `CASE_ID` IN "
                    + concatCaseIds);
            pstmt.executeQuery();
            return map;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }
}
