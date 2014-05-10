
package org.mskcc.cbio.portal.dao;

import org.mskcc.cbio.portal.model.CopyNumberSegmentFile;

import org.apache.commons.lang.StringUtils;

import java.sql.*;
import java.util.*;

public final class DaoCopyNumberSegmentFile {
    private DaoCopyNumberSegmentFile() {}
    
    public static int addCopyNumberSegmentFile(CopyNumberSegmentFile copySegFile) throws DaoException
    {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCopyNumberSegmentFile.class);
            pstmt = con.prepareStatement
                    ("INSERT INTO copy_number_seg_file (`CANCER_STUDY_ID`, `REFERENCE_GENOME_ID`, `DESCRIPTION`,`FILENAME`)"
                     + " VALUES (?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, copySegFile.cancerStudyId);
            pstmt.setString(2, copySegFile.referenceGenomeId.toString());
            pstmt.setString(3, copySegFile.description);
            pstmt.setString(4, copySegFile.filename);
            pstmt.executeUpdate();
            rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return -1;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCopyNumberSegment.class, con, pstmt, rs);
        }
    }

    public static CopyNumberSegmentFile getCopyNumberSegmentFile(int cancerStudyId) throws DaoException
    {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCopyNumberSegmentFile.class);
            pstmt = con.prepareStatement("SELECT * from copy_number_seg_file WHERE `CANCER_STUDY_ID` = ?");
            pstmt.setInt(1, cancerStudyId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                CopyNumberSegmentFile cnsf = new CopyNumberSegmentFile();
                cnsf.segFileId = rs.getInt("SEG_FILE_ID");
                cnsf.cancerStudyId = cancerStudyId;
                cnsf.referenceGenomeId = CopyNumberSegmentFile.ReferenceGenomeId.valueOf(rs.getString("REFERENCE_GENOME_ID"));
                cnsf.description = rs.getString("DESCRIPTION");
                cnsf.filename = rs.getString("FILENAME");
                return cnsf;
            }
            return null;
        }
        catch(SQLException e) {
            throw new DaoException(e);
        }
        finally {
            JdbcUtil.closeAll(DaoCopyNumberSegmentFile.class, con, pstmt, rs);
        }
    }

    public static void deleteAllRecords() throws DaoException
    {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCopyNumberSegmentFile.class);
            pstmt = con.prepareStatement("TRUNCATE TABLE copy_number_seg_file");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCopyNumberSegmentFile.class, con, pstmt, rs);
        }
    }
}
