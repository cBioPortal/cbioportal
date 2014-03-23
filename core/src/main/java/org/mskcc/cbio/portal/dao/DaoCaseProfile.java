package org.mskcc.cbio.portal.dao;

import org.apache.commons.lang.StringUtils;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.util.AccessControl;
import org.mskcc.cbio.portal.web_api.ProtocolException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data access object for case_profile table
 */
public final class DaoCaseProfile {
    private DaoCaseProfile() {}
   
    private static final int NO_SUCH_PROFILE_ID = -1;

    public static int addCaseProfile(String caseId, int geneticProfileId) throws DaoException {
        if (caseId == null || caseId.trim().length() == 0) {
            throw new IllegalArgumentException ("Case ID is null or empty");
        }
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            if (!caseExistsInGeneticProfile(caseId, geneticProfileId)) {
                con = JdbcUtil.getDbConnection(DaoCaseProfile.class);
                pstmt = con.prepareStatement
                        ("INSERT INTO case_profile (`CASE_ID`, `GENETIC_PROFILE_ID`) "
                                + "VALUES (?,?)");
                pstmt.setString(1, caseId);
                pstmt.setInt(2, geneticProfileId);
                return pstmt.executeUpdate();
            } else {
                return 0;
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCaseProfile.class, con, pstmt, rs);
        }
    }

    public static boolean caseExistsInGeneticProfile(String caseId, int geneticProfileId)
            throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCaseProfile.class);
            pstmt = con.prepareStatement
                    ("SELECT * FROM case_profile WHERE CASE_ID = ? AND GENETIC_PROFILE_ID = ?");
            pstmt.setString(1, caseId);
            pstmt.setInt(2, geneticProfileId);
            rs = pstmt.executeQuery();
            return (rs.next());
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCaseProfile.class, con, pstmt, rs);
        }
    }
    
    public static int countCasesInProfile(int geneticProfileId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCaseProfile.class);
            pstmt = con.prepareStatement
                    ("SELECT count(*) FROM case_profile WHERE GENETIC_PROFILE_ID = ?");
            pstmt.setInt(1, geneticProfileId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCaseProfile.class, con, pstmt, rs);
        }
    }

    public static int getProfileIdForCase( String caseId ) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCaseProfile.class);
            pstmt = con.prepareStatement("SELECT GENETIC_PROFILE_ID FROM case_profile WHERE CASE_ID = ?");
            pstmt.setString(1, caseId);
            rs = pstmt.executeQuery();
            if( rs.next() ) {
               return rs.getInt("GENETIC_PROFILE_ID");
            }else{
               return NO_SUCH_PROFILE_ID;
            }

        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCaseProfile.class, con, pstmt, rs);
        }
    }

    public static ArrayList<String> getAllCaseIdsInProfile(int geneticProfileId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCaseProfile.class);
            pstmt = con.prepareStatement
                    ("SELECT * FROM case_profile WHERE GENETIC_PROFILE_ID = ?");
            pstmt.setInt(1, geneticProfileId);
            rs = pstmt.executeQuery();
            ArrayList<String> caseIds = new ArrayList<String>();
            while (rs.next()) {
                caseIds.add(rs.getString("CASE_ID"));
            }
            return caseIds;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCaseProfile.class, con, pstmt, rs);
        }
    }

    public static ArrayList<String> getAllCases() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCaseProfile.class);
            pstmt = con.prepareStatement
                    ("SELECT * FROM case_profile");
            rs = pstmt.executeQuery();
            ArrayList<String> caseIds = new ArrayList<String>();
            while (rs.next()) {
                caseIds.add(rs.getString("CASE_ID"));
            }
            return caseIds;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCaseProfile.class, con, pstmt, rs);
        }
    }

    /**
     * Counts the number of sequenced cases in each cancer study, returning a list of maps
     * containing the cancer study (full string name), the cancer type (e.g. `brca_tcga`), and the count
     *
     * @return  [ list of maps { cancer_study, cancer_type, num_sequenced_samples } ]
     * @throws DaoException
     * @author Gideon Dresdner <dresdnerg@cbio.mskcc.org>
     *
     */
    public static List<Map<String, Object>> metaData(List<CancerStudy> cancerStudies) throws DaoException, ProtocolException {
        // collect all mutationProfileIds
        Map<Integer, GeneticProfile> id2MutationProfile = new HashMap<Integer, GeneticProfile>();
        for (CancerStudy cancerStudy : cancerStudies) {
            GeneticProfile mutationProfile = cancerStudy.getMutationProfile();

            if (mutationProfile != null) {
                // e.g. if cancerStudy == All Cancer Studies
                Integer mutationProfileId = mutationProfile.getGeneticProfileId();
                id2MutationProfile.put(mutationProfileId, mutationProfile);
            }
        }

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
        try {
            con = JdbcUtil.getDbConnection(DaoMutation.class);

            String sql = "select `GENETIC_PROFILE_ID`, count(`CASE_ID`) from case_profile " +
                    " where `GENETIC_PROFILE_ID` in ("+ StringUtils.join(id2MutationProfile.keySet(), ",") + ")" +
                    " group by `GENETIC_PROFILE_ID`";

            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> datum = new HashMap<String, Object>();

                Integer mutationProfileId = rs.getInt(1);
                Integer numSequencedSamples = rs.getInt(2);

                GeneticProfile mutationProfile = id2MutationProfile.get(mutationProfileId);
                Integer cancerStudyId = mutationProfile.getCancerStudyId();
                CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByInternalId(cancerStudyId);
                String cancerStudyName = cancerStudy.getName();
                String cancerType = cancerStudy.getTypeOfCancerId();

                datum.put("cancer_study", cancerStudyName);
                datum.put("cancer_type", cancerType);
                datum.put("color", DaoTypeOfCancer.getTypeOfCancerById(cancerType).getDedicatedColor());
                datum.put("num_sequenced_samples", numSequencedSamples);

                data.add(datum);
            }

        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoMutation.class, con, pstmt, rs);
        }
        
	return data;
    }

    public static void deleteAllRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCaseProfile.class);
            pstmt = con.prepareStatement("TRUNCATE TABLE case_profile");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCaseProfile.class, con, pstmt, rs);
        }
    }
}
