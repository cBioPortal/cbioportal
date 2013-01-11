/*
 * Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center
 * has been advised of the possibility of such damage.  See
 * the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */

package org.mskcc.cbio.cgds.dao;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.mskcc.cbio.cgds.model.ClinicalTrial;

/**
 * Data access object for ClinicalTrial table
 */
public class DaoClinicalTrial {
    private static MySQLbulkLoader drugMySQLBulkLoader = null;
    private static DaoClinicalTrial daoClinicalTrial;

    private DaoClinicalTrial() {
    }

    /**
     * Gets Global Singleton Instance.
     *
     * @return DaoGeneOptimized Singleton.
     * @throws DaoException Database Error.
     */
    public static DaoClinicalTrial getInstance() throws DaoException {
        if (daoClinicalTrial == null) {
            daoClinicalTrial = new DaoClinicalTrial();
        }

        if (drugMySQLBulkLoader == null) {
            drugMySQLBulkLoader = new MySQLbulkLoader("clinicaltrial");
        }
        return daoClinicalTrial;
    }

    public int addClinicalTrial(ClinicalTrial clinicalTrial, Collection<String> keywords) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int rows = 0;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("INSERT INTO clinical_trials (`PROTOCOLID`, `TITLE`, `PHASE`, `LOCATION`, `STATUS`) "
                            + "VALUES (?,?,?,?,?)");
            pstmt.setString(1, clinicalTrial.getId());
            pstmt.setString(2, clinicalTrial.getTitle());
            pstmt.setString(3, clinicalTrial.getPhase());
            pstmt.setString(4, clinicalTrial.getLocation());
            pstmt.setString(5, clinicalTrial.getStatus());

            rows = pstmt.executeUpdate();

            for (String keyword : keywords) {
                pstmt = con.prepareStatement(
                        "INSERT INTO clinical_trial_keywords (`PROTOCOLID`, `KEYWORD`) "
                            +  "VALUES (?, ?)"
                );
                pstmt.setString(1, clinicalTrial.getId());
                pstmt.setString(2, keyword);

                rows += pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }

        return rows;
    }

    public List<ClinicalTrial> searchClinicalTrials(String keyword) throws DaoException {
        ArrayList<ClinicalTrial> clinicalTrials = new ArrayList<ClinicalTrial>();

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement(
                "SELECT * FROM `clinical_trial_keywords` WHERE keyword = ?"
            );
            pstmt.setString(1, keyword);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                String id = rs.getString("PROTOCOLID");
                ClinicalTrial clinicalTrial = getClinicalTrialById(id);
                if(clinicalTrial != null) {
                    clinicalTrials.add(clinicalTrial);
                }
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }

        return clinicalTrials;
    }


    public ClinicalTrial getClinicalTrialById(String id) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT * FROM clinical_trials WHERE PROTOCOLID = ?");
            pstmt.setString(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractClinicalTrial(rs);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    private ClinicalTrial extractClinicalTrial(ResultSet rs) throws SQLException {
        ClinicalTrial clinicalTrial = new ClinicalTrial();
        clinicalTrial.setId(rs.getString("PROTOCOLID"));
        clinicalTrial.setTitle(rs.getString("TITLE"));
        clinicalTrial.setPhase(rs.getString("PHASE"));
        clinicalTrial.setLocation(rs.getString("LOCATION"));
        clinicalTrial.setStatus(rs.getString("STATUS"));
        return clinicalTrial;
    }

    public int countClinicalStudies() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("SELECT count(*) FROM clinical_trials");
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            } else {
                return 0;
            }
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
            pstmt = con.prepareStatement("TRUNCATE TABLE clinical_trials");
            pstmt.executeUpdate();

            pstmt = con.prepareStatement("TRUNCATE TABLE clinical_trial_keywords");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }
}

