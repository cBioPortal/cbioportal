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

import org.mskcc.cbio.portal.model.*;

import org.apache.commons.lang3.StringUtils;

import java.sql.*;
import java.util.*;

/**
 *
 * @author jj
 */
public class DaoProteinArrayData {
    private static DaoProteinArrayData daoProteinArrayData;

    /**
     * Private Constructor to enforce Singleton Pattern.
     */
    private DaoProteinArrayData() {
    }

    /**
     * Gets Global Singleton Instance.
     *
     * @return DaoProteinArrayData Singleton.
     * @throws DaoException Database Error.
     */
    public static DaoProteinArrayData getInstance() throws DaoException {
        if (daoProteinArrayData == null) {
            daoProteinArrayData = new DaoProteinArrayData();
        }

        return daoProteinArrayData;
    }

    /**
     * Adds a new ProteinArrayData Record to the Database.
     *
     * @param pad ProteinArrayData Object.
     * @return number of records successfully added.
     * @throws DaoException Database Error.
     */
    public int addProteinArrayData(ProteinArrayData pad) throws DaoException {
        if (getProteinArrayData(pad.getCancerStudyId(), pad.getArrayId(),pad.getSampleId())!=null) {
            System.err.println("RPPA data of "+pad.getArrayId()+" for case "
                    +pad.getSampleId()+" in cancer study "
                    +pad.getCancerStudyId()+ " has already been added.");
            return 0;
        }
        
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoProteinArrayData.class);
            pstmt = con.prepareStatement
                    ("INSERT INTO protein_array_data (`PROTEIN_ARRAY_ID`,`CANCER_STUDY_ID`,`SAMPLE_ID`,`ABUNDANCE`) "
                            + "VALUES (?,?,?,?)");
            pstmt.setString(1, pad.getArrayId());
            pstmt.setInt(2, pad.getCancerStudyId());
            pstmt.setInt(3, pad.getSampleId());
            pstmt.setDouble(4, pad.getAbundance());
            return pstmt.executeUpdate();
        } catch (NullPointerException e) {
            throw new DaoException(e);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoProteinArrayData.class, con, pstmt, rs);
        }
    }
    
    public ProteinArrayData getProteinArrayData(int cancerStudyId, String arrayId, int sampleId) throws DaoException {
        ArrayList<ProteinArrayData> list = getProteinArrayData(cancerStudyId, arrayId, Collections.singleton(sampleId));
        if (list.isEmpty()) {
            return null;
        }
        
        return list.get(0);
    }
    
    public ArrayList<ProteinArrayData> getProteinArrayData(int cancerStudyId, String arrayId, Collection<Integer> sampleIds)
            throws DaoException {
        return getProteinArrayData(cancerStudyId, Collections.singleton(arrayId), sampleIds);
    }

    /**
     * Gets the ProteinArrayData with the Specified array ID.
     *
     * @param arrayIds protein array ID.
     * @return map of array id to a list of protein array data.
     * @throws DaoException Database Error.
     */
    public ArrayList<ProteinArrayData> getProteinArrayData(int cancerStudyId, Collection<String> arrayIds)
            throws DaoException {
        return getProteinArrayData(cancerStudyId, arrayIds, null);
    }

    /**
     * Gets the ProteinArrayData with the Specified array ID for specific cases.
     *
     * @param arrayIds protein array ID.
     * @return map of array id to a list of protein array data.
     * @throws DaoException Database Error.
     */
    public ArrayList<ProteinArrayData> getProteinArrayData(int cancerStudyId, Collection<String> arrayIds, Collection<Integer> sampleIds)
            throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoProteinArrayData.class);
            if (sampleIds == null) {
                pstmt = con.prepareStatement
                        ("SELECT * FROM protein_array_data WHERE CANCER_STUDY_ID='"
                        + cancerStudyId + "' AND PROTEIN_ARRAY_ID IN ('"
                        + StringUtils.join(arrayIds, "','") + "')");
            } else {
                pstmt = con.prepareStatement
                        ("SELECT * FROM protein_array_data WHERE CANCER_STUDY_ID='"
                        + cancerStudyId + "' AND PROTEIN_ARRAY_ID IN ('"
                        + StringUtils.join(arrayIds, "','") + "')"
                        + " AND SAMPLE_ID IN ('"+StringUtils.join(sampleIds,"','") +"')");
            }
            rs = pstmt.executeQuery();
            return extractData(rs);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoProteinArrayData.class, con, pstmt, rs);
        }
    }

    /**
     * Gets all Protein array data in the Database.
     *
     * @return ArrayList of ProteinArrayDataes.
     * @throws DaoException Database Error.
     */
    public ArrayList<ProteinArrayData> getAllProteinArrayData() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoProteinArrayData.class);
            pstmt = con.prepareStatement
                    ("SELECT * FROM protein_array_data");
            rs = pstmt.executeQuery();
            return extractData(rs);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoProteinArrayData.class, con, pstmt, rs);
        }
    }
    
    private ArrayList<ProteinArrayData> extractData(ResultSet rs) throws SQLException {
        try {
            ArrayList<ProteinArrayData> list = new ArrayList<ProteinArrayData>();
            while (rs.next()) {
                ProteinArrayData pai = new ProteinArrayData(
                    rs.getInt("CANCER_STUDY_ID"),
                    rs.getString("PROTEIN_ARRAY_ID"),
                    rs.getInt("SAMPLE_ID"),
                    rs.getDouble("ABUNDANCE"));
                list.add(pai);
            }
            return list;
        }
        catch(NullPointerException e) {
            throw new SQLException(e);
        }
    }

    /**
     * Deletes all protein array data Records in the Database.
     *
     * @throws DaoException Database Error.
     */
    public void deleteAllRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoProteinArrayData.class);
            pstmt = con.prepareStatement("TRUNCATE TABLE protein_array_data");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoProteinArrayData.class, con, pstmt, rs);
        }
    }
}
