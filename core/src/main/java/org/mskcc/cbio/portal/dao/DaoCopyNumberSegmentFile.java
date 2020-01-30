/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
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
import org.apache.commons.lang.StringUtils;
import org.mskcc.cbio.portal.model.CopyNumberSegmentFile;

public final class DaoCopyNumberSegmentFile {

    private DaoCopyNumberSegmentFile() {}

    public static int addCopyNumberSegmentFile(
        CopyNumberSegmentFile copySegFile
    )
        throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCopyNumberSegmentFile.class);
            pstmt =
                con.prepareStatement(
                    "INSERT INTO copy_number_seg_file (`CANCER_STUDY_ID`, `REFERENCE_GENOME_ID`, `DESCRIPTION`,`FILENAME`)" +
                    " VALUES (?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS
                );
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

    public static CopyNumberSegmentFile getCopyNumberSegmentFile(
        int cancerStudyId
    )
        throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCopyNumberSegmentFile.class);
            pstmt =
                con.prepareStatement(
                    "SELECT * from copy_number_seg_file WHERE `CANCER_STUDY_ID` = ?"
                );
            pstmt.setInt(1, cancerStudyId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                CopyNumberSegmentFile cnsf = new CopyNumberSegmentFile();
                cnsf.segFileId = rs.getInt("SEG_FILE_ID");
                cnsf.cancerStudyId = cancerStudyId;
                cnsf.referenceGenomeId =
                    CopyNumberSegmentFile.ReferenceGenomeId.valueOf(
                        rs.getString("REFERENCE_GENOME_ID")
                    );
                cnsf.description = rs.getString("DESCRIPTION");
                cnsf.filename = rs.getString("FILENAME");
                return cnsf;
            }
            return null;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCopyNumberSegmentFile.class, con, pstmt, rs);
        }
    }

    public static void deleteAllRecords() throws DaoException {
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
