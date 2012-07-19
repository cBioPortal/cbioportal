package org.mskcc.cbio.cgds.dao;

import org.mskcc.cgds.model.ClinicalData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Set;

/**
 * Data access object for Clinical Data table
 */
public class DaoClinicalData {

    /**
     * Add a New Case.
     *
     * @param caseId                    Case ID.
     * @param overallSurvivalMonths     Overall Survival Months.
     * @param overallSurvivalStatus     Overall Survival Status.
     * @param diseaseFreeSurvivalMonths Disease Free Survival Months.
     * @param diseaseFreeSurvivalStatus Disease Free Survival Status.
     * @return number of cases added.
     * @throws DaoException Error Adding new Record.
     */
    public int addCase(String caseId, Double overallSurvivalMonths, String overallSurvivalStatus,
            Double diseaseFreeSurvivalMonths, String diseaseFreeSurvivalStatus,
            Double ageAtDiagnosis)
            throws DaoException {
        if (caseId == null || caseId.trim().length() == 0) {
            throw new IllegalArgumentException ("Case ID is null or empty");
        }
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
                con = JdbcUtil.getDbConnection();
                pstmt = con.prepareStatement
                        ("INSERT INTO clinical (`CASE_ID`, `OVERALL_SURVIVAL_MONTHS`, " +
                                "`OVERALL_SURVIVAL_STATUS`, " +
                                "`DISEASE_FREE_SURVIVAL_MONTHS`, `DISEASE_FREE_SURVIVAL_STATUS`," +
                                "`AGE_AT_DIAGNOSIS`) "
                                + "VALUES (?,?,?,?,?,?)");
                pstmt.setString(1, caseId);

                //  Make sure to set to Null if we are missing data.
                if (overallSurvivalMonths == null) {
                    pstmt.setNull(2, java.sql.Types.DOUBLE);
                } else {
                    pstmt.setDouble(2, overallSurvivalMonths);
                }

                if (overallSurvivalStatus == null) {
                    pstmt.setNull(3, java.sql.Types.VARCHAR);
                } else {
                    pstmt.setString(3, overallSurvivalStatus);
                }

                if (diseaseFreeSurvivalMonths == null) {
                    pstmt.setNull(4, java.sql.Types.DOUBLE);
                } else {
                    pstmt.setDouble(4, diseaseFreeSurvivalMonths);
                }

                if (diseaseFreeSurvivalStatus == null) {
                    pstmt.setNull(5, java.sql.Types.VARCHAR);
                } else {
                    pstmt.setString(5, diseaseFreeSurvivalStatus);
                }

                if (ageAtDiagnosis == null) {
                    pstmt.setNull(6, java.sql.Types.DOUBLE);
                } else {
                    pstmt.setDouble(6, ageAtDiagnosis);
                }

                int rows = pstmt.executeUpdate();
                return rows;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    /**
     * Gets All Cases in the Specified Case Set.
     *
     * @param caseSet       Target Case Set.
     * @return  ArrayList of CaseSurvival Objects.
     * @throws DaoException Error Accessing Database.
     */
    public ArrayList<ClinicalData> getCases(Set<String> caseSet) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement ("SELECT * FROM clinical");
            rs = pstmt.executeQuery();
            ArrayList<ClinicalData> caseList = new ArrayList<ClinicalData>();
            while (rs.next()) {
                String caseId = rs.getString("CASE_ID");
                if (caseSet.contains(caseId)) {

                    //  Must check for NULL Data via rs.wasNull
                    Double overallSurvivalMonths = rs.getDouble("OVERALL_SURVIVAL_MONTHS");
                    if (rs.wasNull()) {
                        overallSurvivalMonths = null;
                    }

                    String overallSurvivalStatus = rs.getString("OVERALL_SURVIVAL_STATUS");
                    if (rs.wasNull()) {
                        overallSurvivalStatus = null;
                    }

                    Double diseaseFreeSurvivalMonths = rs.getDouble("DISEASE_FREE_SURVIVAL_MONTHS");
                    if (rs.wasNull()) {
                        diseaseFreeSurvivalMonths = null;
                    }

                    String diseaseFreeSurvivalStatus = rs.getString("DISEASE_FREE_SURVIVAL_STATUS");
                    if (rs.wasNull()) {
                        diseaseFreeSurvivalStatus = null;
                    }

                    Double ageAtDiagnosis = rs.getDouble("AGE_AT_DIAGNOSIS");
                    if (rs.wasNull()) {
                        ageAtDiagnosis = null;
                    }

                    ClinicalData caseSurvival = new ClinicalData(caseId, overallSurvivalMonths,
                            overallSurvivalStatus, diseaseFreeSurvivalMonths,
                            diseaseFreeSurvivalStatus, ageAtDiagnosis);
                    caseList.add(caseSurvival);
                }
            }
            return caseList;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    /**
     * Deletes all Records.
     * @throws DaoException DAO Error.
     */
    public void deleteAllRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement("TRUNCATE TABLE clinical");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }
}