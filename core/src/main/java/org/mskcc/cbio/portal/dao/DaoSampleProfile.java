package org.mskcc.cbio.portal.dao;

import org.mskcc.cbio.portal.model.*;

import org.apache.commons.lang.StringUtils;

import java.sql.*;
import java.util.*;

/**
 * Data access object for sample_profile table
 */
public final class DaoSampleProfile {
    private DaoSampleProfile() {}
   
    private static final int NO_SUCH_PROFILE_ID = -1;

    public static int addSampleProfile(int sampleId, int geneticProfileId) throws DaoException {

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            if (!sampleExistsInGeneticProfile(sampleId, geneticProfileId)) {
                con = JdbcUtil.getDbConnection(DaoSampleProfile.class);
                pstmt = con.prepareStatement
                        ("INSERT INTO sample_profile (`SAMPLE_ID`, `GENETIC_PROFILE_ID`) "
                                + "VALUES (?,?)");
                pstmt.setInt(1, sampleId);
                pstmt.setInt(2, geneticProfileId);
                return pstmt.executeUpdate();
            } else {
                return 0;
            }
        } catch (NullPointerException e) {
            throw new DaoException(e);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoSampleProfile.class, con, pstmt, rs);
        }
    }

    public static boolean sampleExistsInGeneticProfile(int sampleId, int geneticProfileId)
            throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = JdbcUtil.getDbConnection(DaoSampleProfile.class);
            pstmt = con.prepareStatement
                    ("SELECT * FROM sample_profile WHERE SAMPLE_ID = ? AND GENETIC_PROFILE_ID = ?");
            pstmt.setInt(1, sampleId);
            pstmt.setInt(2, geneticProfileId);
            rs = pstmt.executeQuery();
            return (rs.next());
        } catch (NullPointerException e) {
            throw new DaoException(e);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoSampleProfile.class, con, pstmt, rs);
        }
    }
    
    public static int countSamplesInProfile(int geneticProfileId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoSampleProfile.class);
            pstmt = con.prepareStatement
                    ("SELECT count(*) FROM sample_profile WHERE GENETIC_PROFILE_ID = ?");
            pstmt.setInt(1, geneticProfileId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoSampleProfile.class, con, pstmt, rs);
        }
    }

    public static int getProfileIdForSample(int sampleId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = JdbcUtil.getDbConnection(DaoSampleProfile.class);
            pstmt = con.prepareStatement("SELECT GENETIC_PROFILE_ID FROM sample_profile WHERE SAMPLE_ID = ?");
            pstmt.setInt(1, sampleId);
            rs = pstmt.executeQuery();
            if( rs.next() ) {
               return rs.getInt("GENETIC_PROFILE_ID");
            }else{
               return NO_SUCH_PROFILE_ID;
            }
        } catch (NullPointerException e) {
            throw new DaoException(e);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoSampleProfile.class, con, pstmt, rs);
        }
    }

    public static ArrayList<Integer> getAllSampleIdsInProfile(int geneticProfileId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoSampleProfile.class);
            pstmt = con.prepareStatement
                    ("SELECT * FROM sample_profile WHERE GENETIC_PROFILE_ID = ?");
            pstmt.setInt(1, geneticProfileId);
            rs = pstmt.executeQuery();
            ArrayList<Integer> sampleIds = new ArrayList<Integer>();
            while (rs.next()) {
                Sample sample = DaoSample.getSampleById(rs.getInt("SAMPLE_ID"));
                sampleIds.add(rs.getInt("SAMPLE_ID"));
            }
            return sampleIds;
        } catch (NullPointerException e) {
            throw new DaoException(e);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoSampleProfile.class, con, pstmt, rs);
        }
    }

    public static ArrayList<Integer> getAllSamples() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoSampleProfile.class);
            pstmt = con.prepareStatement
                    ("SELECT * FROM sample_profile");
            rs = pstmt.executeQuery();
            ArrayList<Integer> sampleIds = new ArrayList<Integer>();
            while (rs.next()) {
                sampleIds.add(rs.getInt("SAMPLE_ID"));
            }
            return sampleIds;
        } catch (NullPointerException e) {
            throw new DaoException(e);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoSampleProfile.class, con, pstmt, rs);
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
    public static List<Map<String, Object>> metaData(List<CancerStudy> cancerStudies) throws DaoException {
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

            String sql = "select `GENETIC_PROFILE_ID`, count(`SAMPLE_ID`) from sample_profile " +
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
            con = JdbcUtil.getDbConnection(DaoSampleProfile.class);
            pstmt = con.prepareStatement("TRUNCATE TABLE sample_profile");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoSampleProfile.class, con, pstmt, rs);
        }
    }
}
