package org.mskcc.cbio.cgds.dao;


import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.mskcc.cgds.model.Drug;

/**
 * Data access object for Drug table
 */
public class DaoDrug {
    private static MySQLbulkLoader drugMySQLBulkLoader = null;
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

        if (drugMySQLBulkLoader == null) {
            drugMySQLBulkLoader = new MySQLbulkLoader("drug");
        }
        return daoDrug;
    }

    public int addDrug(Drug drug) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            if (MySQLbulkLoader.isBulkLoad()) {
                drugMySQLBulkLoader.insertRecord(
                        drug.getId(),
                        drug.getResource(),
                        drug.getName(),
                        drug.getSynonyms(),
                        drug.getDescription(),
                        drug.getExternalReference(),
                        drug.isApprovedFDA() ? "1" : "0",
                        drug.getATCCode()
                );
                return 1;
            } else {
                Drug existingDrug = getDrug(drug.getId());
                if (existingDrug == null) {
                    con = JdbcUtil.getDbConnection();
                    pstmt = con.prepareStatement(
                            "INSERT INTO drug "
                                    + "(`DRUG_ID`, `DRUG_RESOURCE`, `DRUG_NAME`, "
                                        + "`DRUG_SYNONYMS`, `DRUG_DESCRIPTION`, `DRUG_XREF`, "
                                        + "`DRUG_APPROVED`, `DRUG_ATC_CODE`) "
                                    + "VALUES (?,?,?,?,?,?,?,?)"
                            );
                    pstmt.setString(1, drug.getId());
                    pstmt.setString(2, drug.getResource());
                    pstmt.setString(3, drug.getName());
                    pstmt.setString(4, drug.getSynonyms());
                    pstmt.setString(5, drug.getDescription());
                    pstmt.setString(6, drug.getExternalReference());
                    pstmt.setInt(7, drug.isApprovedFDA() ? 1 : 0);
                    pstmt.setString(8, drug.getATCCode());
                    return pstmt.executeUpdate();
                } else {
                    return 0;
                }
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    public Drug getDrug(String drugID) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT * FROM drug WHERE DRUG_ID = ?");
            pstmt.setString(1, drugID);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                Drug drug = new Drug();
                drug.setId(drugID);
                drug.setResource(rs.getString("DRUG_RESOURCE"));
                drug.setName(rs.getString("DRUG_NAME"));
                drug.setSynonyms(rs.getString("DRUG_SYNONYMS"));
                drug.setDescription(rs.getString("DRUG_DESCRIPTION"));
                drug.setExternalReference(rs.getString("DRUG_XREF"));
                drug.setApprovedFDA(rs.getInt("DRUG_APPROVED") == 1);
                drug.setATCCode(rs.getString("DRUG_ATC_CODE"));
                return drug;
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    public ArrayList<Drug> getAllDrugs() throws DaoException {
        ArrayList<Drug> drugList = new ArrayList<Drug>();

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT * FROM drug");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Drug drug = new Drug();
                drug.setId(rs.getString("DRUG_ID"));
                drug.setResource(rs.getString("DRUG_RESOURCE"));
                drug.setName(rs.getString("DRUG_NAME"));
                drug.setSynonyms(rs.getString("DRUG_SYNONYMS"));
                drug.setDescription(rs.getString("DRUG_DESCRIPTION"));
                drug.setExternalReference(rs.getString("DRUG_XREF"));
                drug.setApprovedFDA(rs.getInt("DRUG_APPROVED") == 1);
                drug.setATCCode(rs.getString("DRUG_ATC_CODE"));
                drugList.add(drug);
            }
            return drugList;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    public int getCount() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = JdbcUtil.getDbConnection();
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
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    public void deleteAllRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement("TRUNCATE TABLE drug");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    public int flushToDatabase() throws DaoException {
        try {
            return drugMySQLBulkLoader.loadDataFromTempFileIntoDBMS();
        } catch (IOException e) {
            System.err.println("Could not open temp file");
            e.printStackTrace();
            return -1;
        }
    }
}

