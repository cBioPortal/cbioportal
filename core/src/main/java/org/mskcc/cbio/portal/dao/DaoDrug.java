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


import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import org.apache.commons.lang.StringUtils;
import org.mskcc.cbio.portal.model.Drug;

/**
 * Data access object for Drug table
 */
public class DaoDrug {
    private static DaoDrug daoDrug;

    private DaoDrug() {
    }

    /**
     * Gets Global Singleton Instance.
     *
     * @return DaoGeneOptimized Singleton.
     * @throws DaoException Database Error.
     */
    public static DaoDrug getInstance() throws DaoException {
        if (daoDrug == null) {
            daoDrug = new DaoDrug();
        }

        return daoDrug;
    }

    public int addDrug(Drug drug) throws DaoException {
        if (MySQLbulkLoader.isBulkLoad()) {
            MySQLbulkLoader.getMySQLbulkLoader("drug").insertRecord(
                    drug.getId(),
                    drug.getResource(),
                    drug.getName(),
                    drug.getSynonyms(),
                    drug.getDescription(),
                    drug.getExternalReference(),
                    drug.getATCCode(),
                    drug.isApprovedFDA() ? "1" : "0",
                    drug.isCancerDrug() ? "1" : "0",
                    drug.isNutraceuitical() ? "1" : "0",
                    drug.getNumberOfClinicalTrials().toString()
                    );
            return 1;
        }
            
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            Drug existingDrug = getDrug(drug.getId());
            if (existingDrug == null) {
                con = JdbcUtil.getDbConnection(DaoDrug.class);
                pstmt = con.prepareStatement(
                        "INSERT INTO drug "
                                + "(`DRUG_ID`, `DRUG_RESOURCE`, `DRUG_NAME`, "
                                    + "`DRUG_SYNONYMS`, `DRUG_DESCRIPTION`, `DRUG_XREF`, "
                                    + "`DRUG_ATC_CODE`, `DRUG_APPROVED`, `DRUG_CANCERDRUG`, "
                                    + "`DRUG_NUTRACEUTICAL`, `DRUG_NUMOFTRIALS`) "
                                + "VALUES (?,?,?,?,?,?,?,?,?,?,?)"
                        );
                pstmt.setString(1, drug.getId());
                pstmt.setString(2, drug.getResource());
                pstmt.setString(3, drug.getName());
                pstmt.setString(4, drug.getSynonyms());
                pstmt.setString(5, drug.getDescription());
                pstmt.setString(6, drug.getExternalReference());
                pstmt.setString(7, drug.getATCCode());
                pstmt.setInt(8, drug.isApprovedFDA() ? 1 : 0);
                pstmt.setInt(9, drug.isCancerDrug() ? 1 : 0);
                pstmt.setInt(10, drug.isNutraceuitical() ? 1 : 0);
                pstmt.setInt(11, drug.getNumberOfClinicalTrials());

                return pstmt.executeUpdate();
            } else {
                return 0;
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoDrug.class, con, pstmt, rs);
        }
    }

    public Drug getDrug(String drugID) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoDrug.class);
            pstmt = con.prepareStatement
                    ("SELECT * FROM drug WHERE DRUG_ID = ?");
            pstmt.setString(1, drugID);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractDrug(rs);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoDrug.class, con, pstmt, rs);
        }
    }

    public ArrayList<Drug> getDrugs(Collection<String> drugIds) throws DaoException {
        ArrayList<Drug> drugList = new ArrayList<Drug>();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoDrug.class);
            pstmt = con.prepareStatement
                    ("SELECT * FROM drug WHERE DRUG_ID in ('"
                    + StringUtils.join(drugIds, "','")+"')");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                drugList.add(extractDrug(rs));
            }
            return drugList;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoDrug.class, con, pstmt, rs);
        }
    }

    public ArrayList<Drug> getAllDrugs() throws DaoException {
        ArrayList<Drug> drugList = new ArrayList<Drug>();

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoDrug.class);
            pstmt = con.prepareStatement
                    ("SELECT * FROM drug");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                drugList.add(extractDrug(rs));
            }
            return drugList;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoDrug.class, con, pstmt, rs);
        }
    }
    
    private Drug extractDrug(ResultSet rs) throws SQLException {
        Drug drug = new Drug();
        drug.setId(rs.getString("DRUG_ID"));
        drug.setResource(rs.getString("DRUG_RESOURCE"));
        drug.setName(rs.getString("DRUG_NAME"));
        drug.setSynonyms(rs.getString("DRUG_SYNONYMS"));
        drug.setDescription(rs.getString("DRUG_DESCRIPTION"));
        drug.setExternalReference(rs.getString("DRUG_XREF"));
        drug.setATCCode(rs.getString("DRUG_ATC_CODE"));
        drug.setApprovedFDA(rs.getInt("DRUG_APPROVED") == 1);
        drug.setCancerDrug(rs.getInt("DRUG_CANCERDRUG") == 1);
        drug.setNutraceuitical(rs.getInt("DRUG_NUTRACEUTICAL") == 1);
        drug.setNumberOfClinicalTrials(rs.getInt("DRUG_NUMOFTRIALS"));
        return drug;
    }

    public int getCount() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = JdbcUtil.getDbConnection(DaoDrug.class);
            pstmt = con.prepareStatement
                    ("SELECT COUNT(*) FROM drug");
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoDrug.class, con, pstmt, rs);
        }
    }

    public void deleteAllRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoDrug.class);
            JdbcUtil.disableForeignKeyCheck(con);
            pstmt = con.prepareStatement("TRUNCATE TABLE drug");
            pstmt.executeUpdate();
            JdbcUtil.enableForeignKeyCheck(con);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoDrug.class, con, pstmt, rs);
        }
    }
}

