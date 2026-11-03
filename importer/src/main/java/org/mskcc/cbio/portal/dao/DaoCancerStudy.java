/*
 * Copyright (c) 2015 - 2017 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.dao;

import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.CancerStudyTags;
import org.mskcc.cbio.portal.model.ReferenceGenome;
import org.mskcc.cbio.portal.model.TypeOfCancer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Analogous to and replaces the old DaoCancerType. A CancerStudy has a NAME and
 * DESCRIPTION. If PUBLIC is true a CancerStudy can be accessed by anyone,
 * otherwise can only be accessed through access control.
 *
 * @author Ethan Cerami
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 * @author Ersin Ciftci
 */
public final class DaoCancerStudy {

    public static enum Status {
        UNAVAILABLE,
        AVAILABLE
    }

    private static final Map<String, java.util.Date> cacheDateByStableId = new HashMap<String, java.util.Date>();
    private static final Map<Integer, java.util.Date> cacheDateByInternalId = new HashMap<Integer, java.util.Date>();
    private static final Map<String,CancerStudy> byStableId = new HashMap<String,CancerStudy>();
    private static final Map<Integer,CancerStudy> byInternalId = new HashMap<Integer,CancerStudy>();
    private static final String CANCER_STUDY_SEQUENCE = "seq_cancer_study";

    static {
        reCacheAll();
    }

    private DaoCancerStudy() {}

    public static synchronized void reCacheAll() {
        System.out.println("Recaching... ");
        DaoCancerStudy.reCache();
        DaoGeneticProfile.reCache();
        DaoPatient.reCache();
        DaoSample.reCache();
        DaoClinicalData.reCache();
        DaoInfo.setVersion();
        System.out.println("Finished recaching... ");
    }

    private static synchronized void reCache() {
        cacheDateByStableId.clear();
        cacheDateByInternalId.clear();
        byStableId.clear();
        byInternalId.clear();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCancerStudy.class);
            pstmt = con.prepareStatement("SELECT * FROM cancer_study");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                CancerStudy cancerStudy = extractCancerStudy(rs);
                cacheCancerStudy(cancerStudy, new java.util.Date());
            }
        } catch (SQLException | DaoException e) {
            e.printStackTrace();
        } finally {
            JdbcUtil.closeAll(DaoCancerStudy.class, con, pstmt, rs);
        }
    }

    private static void cacheCancerStudy(CancerStudy study, java.util.Date importDate) {
        cacheDateByStableId.put(study.getCancerStudyStableId(), importDate);
        cacheDateByInternalId.put(study.getInternalId(), importDate);
        byStableId.put(study.getCancerStudyStableId(), study);
        byInternalId.put(study.getInternalId(), study);
    }

    /**
     * Removes the cancer study from cache
     * @param internalCancerStudyId Internal cancer study ID
     */
    private static void removeCancerStudyFromCache(int internalCancerStudyId) {
        String stableId = byInternalId.get(internalCancerStudyId).getCancerStudyStableId();
        cacheDateByStableId.remove(stableId);
        cacheDateByInternalId.remove(internalCancerStudyId);
        byStableId.remove(stableId);
        byInternalId.remove(internalCancerStudyId);
    }

    public static void setStatus(Status status, String stableCancerStudyId, Integer ... internalId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCancerStudy.class);
            if (internalId.length > 0) {
                pstmt = con.prepareStatement("UPDATE cancer_study set status = ? where cancer_study_id = ?");
                pstmt.setInt(1, status.ordinal());
                pstmt.setInt(2, internalId[0]);
            } else {
                pstmt = con.prepareStatement("UPDATE cancer_study set status = ? where cancer_study_identifier = ?");
                pstmt.setInt(1, status.ordinal());
                pstmt.setString(2, stableCancerStudyId);
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            if (!e.getMessage().toLowerCase().contains("unknown column")) {
                throw new DaoException(e);
            }
        } finally {
            JdbcUtil.closeAll(DaoCancerStudy.class, con, pstmt, rs);
        }
    }

    public static Status getStatus(String stableCancerStudyId, Integer ... internalId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCancerStudy.class);
            if (internalId.length > 0) {
                pstmt = con.prepareStatement("SELECT status FROM cancer_study where cancer_study_id = ?");
                pstmt.setInt(1, internalId[0]);
            } else {
                pstmt = con.prepareStatement("SELECT status FROM cancer_study where cancer_study_identifier = ?");
                pstmt.setString(1, stableCancerStudyId);
            }
            rs = pstmt.executeQuery();
            if (rs.next()) {
                Integer status = rs.getInt(1);
                if (rs.wasNull()) {
                    return Status.AVAILABLE;
                }
                if (status>=Status.values().length) {
                    return Status.AVAILABLE;
                }
                return Status.values()[status];
            } else {
                return Status.AVAILABLE;
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCancerStudy.class, con, pstmt, rs);
        }
    }

    private static Integer getStudyCount() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCancerStudy.class);
            pstmt = con.prepareStatement("SELECT count(*) from cancer_study");
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                return 0;
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCancerStudy.class, con, pstmt, rs);
        }
    }

    public static void setImportDate(Integer internalId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCancerStudy.class);
            pstmt = con.prepareStatement("UPDATE cancer_study set import_date = now() where cancer_study_id = ?");
            pstmt.setInt(1, internalId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            if (e.getMessage().toLowerCase().contains("unknown column")) {
                return;
            }
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCancerStudy.class, con, pstmt, rs);
        }
    }

    public static java.util.Date getImportDate(String stableCancerStudyId, Integer ... internalId) throws DaoException, ParseException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCancerStudy.class);
            if (internalId.length > 0) {
                pstmt = con.prepareStatement("SELECT import_date FROM cancer_study where cancer_study_id = ?");
                pstmt.setInt(1, internalId[0]);
            } else {
                pstmt = con.prepareStatement("SELECT import_date FROM cancer_study where cancer_study_identifier = ?");
                pstmt.setString(1, stableCancerStudyId);
            }
            rs = pstmt.executeQuery();
            if (rs.next()) {
                java.sql.Timestamp importDate = rs.getTimestamp(1);
                if (rs.wasNull()) {
                    return new SimpleDateFormat("yyyyMMdd").parse("19180511");
                } else {
                    return importDate;
                }
            } else {
                return new SimpleDateFormat("yyyyMMdd").parse("19180511");
            }
        } catch (SQLException e) {
            if (e.getMessage().toLowerCase().contains("unknown column")) {
                return new SimpleDateFormat("yyyyMMdd").parse("19180511");
            }
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCancerStudy.class, con, pstmt, rs);
        }
    }

    /**
     * Adds a cancer study to the Database.
     * Updates cancerStudy with its auto incremented uid, in studyID.
     *
     * @param cancerStudy   Cancer Study Object.
     * @throws DaoException Database Error.
     */
    public static void addCancerStudy(CancerStudy cancerStudy) throws DaoException {
        addCancerStudy(cancerStudy, false);
    }

    /**
     * Adds a cancer study to the Database.
     * @param cancerStudy
     * @param overwrite if true, overwrite if exist.
     * @throws DaoException
     */
    public static void addCancerStudy(CancerStudy cancerStudy, boolean overwrite) throws DaoException {
        // make sure that cancerStudy refers to a valid TypeOfCancerId
        // TODO: have a foreign key constraint do this; why not?
        TypeOfCancer aTypeOfCancer = DaoTypeOfCancer.getTypeOfCancerById(cancerStudy.getTypeOfCancerId());
        if (null == aTypeOfCancer) {
            throw new DaoException("cancerStudy.getTypeOfCancerId() '" + cancerStudy.getTypeOfCancerId() + "' does not refer to a TypeOfCancer.");
        }
        // CANCER_STUDY_IDENTIFIER cannot be null
        String stableId = cancerStudy.getCancerStudyStableId();
        if (stableId == null) {
            throw new DaoException("Cancer study stable ID cannot be null.");
        }
        CancerStudy existing = getCancerStudyByStableId(stableId);
        if (existing!=null) {
            if (overwrite) {
                deleteCancerStudy(existing.getInternalId());
            } else {
                throw new DaoException("Cancer study " + stableId + "is already imported.");
            }
        }
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCancerStudy.class);
            long cancerStudyId = ClickHouseAutoIncrement.nextId(CANCER_STUDY_SEQUENCE);
            pstmt = con.prepareStatement("INSERT INTO cancer_study " +
                    "( `cancer_study_id`, `cancer_study_identifier`, `name`, "
                    + "`description`, `public`, `type_of_cancer_id`, "
                    + "`pmid`, `citation`, `groups`, `status`,`reference_genome_id`, `import_date` ) VALUES (?,?,?,?,?,?,?,?,?,?,?,NOW())");
            pstmt.setLong(1, cancerStudyId);
            pstmt.setString(2, stableId);
            pstmt.setString(3, cancerStudy.getName());
            pstmt.setString(4, cancerStudy.getDescription());
            pstmt.setBoolean(5, cancerStudy.isPublicStudy());
            pstmt.setString(6, cancerStudy.getTypeOfCancerId());
            pstmt.setString(7, cancerStudy.getPmid());
            pstmt.setString(8, cancerStudy.getCitation());
            Set<String> groups = cancerStudy.getGroups();
            if (groups==null) {
                pstmt.setString(9, null);
            } else {
                pstmt.setString(9, StringUtils.join(groups, ";"));
            }
            //status is UNAVAILABLE until other data is loaded for this study. Once all is loaded, the
            //data loading process can set this to AVAILABLE:
            //TODO - use this field in parts of the system that build up the list of studies to display in home page:
            pstmt.setInt(10, Status.UNAVAILABLE.ordinal());
            try {
                ReferenceGenome referenceGenome = DaoReferenceGenome.getReferenceGenomeByGenomeName(cancerStudy.getReferenceGenome());
                pstmt.setInt(11, referenceGenome.getReferenceGenomeId());
            }
            catch (NullPointerException e) {
                throw new DaoException("Unsupported reference genome");
            }
            pstmt.executeUpdate();
            cancerStudy.setInternalId((int)cancerStudyId);
            cacheCancerStudy(cancerStudy, new java.util.Date());
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCancerStudy.class, con, pstmt, rs);
        }
        reCacheAll();
    }

    public static void addCancerStudyTags(CancerStudyTags cancerStudyTags) throws DaoException {
    
            Connection con = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            try {
                con = JdbcUtil.getDbConnection(DaoCancerStudy.class);
                pstmt = con.prepareStatement("INSERT INTO cancer_study_tags " +
                        "( `cancer_study_id`,`tags` ) VALUES (?,?)");
                pstmt.setInt(1, cancerStudyTags.getCancerStudyId());
                pstmt.setString(2, cancerStudyTags.getTags());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                throw new DaoException(e);
            } finally {
                JdbcUtil.closeAll(DaoCancerStudy.class, con, pstmt, rs);
            }
        } 

    /**
     * Return the cancerStudy identified by the internal cancer study ID, if it exists.
     *
     * @param internalId     Internal (int) Cancer Study ID.
     * @return Cancer Study Object, or null if there's no such study.
     */
    public static CancerStudy getCancerStudyByInternalId(int internalId) throws DaoException {
        return byInternalId.get(internalId);
    }

    /**
     * Returns the cancerStudy identified by the stable identifier, if it exists.
     *
     * @param stableId Cancer Study Stable ID.
     * @return the CancerStudy, or null if there's no such study.
     */
    public static CancerStudy getCancerStudyByStableId(String stableId) throws DaoException {
        return byStableId.get(stableId);
    }

    /**
     * Indicates whether the cancerStudy identified by the stable ID exists.
     *
     * @param cancerStudyStableId Cancer Study Stable ID.
     * @return true if the CancerStudy exists, otherwise false
     */
    public static boolean doesCancerStudyExistByStableId(String cancerStudyStableId) {
        return byStableId.containsKey(cancerStudyStableId);
    }

    /**
     * Indicates whether the cancerStudy identified by internal study ID exist.
     * does no access control, so only returns a boolean.
     *
     * @param internalCancerStudyId Internal Cancer Study ID.
     * @return true if the CancerStudy exists, otherwise false
     */
    public static boolean doesCancerStudyExistByInternalId(int internalCancerStudyId) {
        return byInternalId.containsKey(internalCancerStudyId);
    }

    /**
     * Returns all the cancerStudies.
     *
     * @return ArrayList of all CancerStudy Objects.
     */
    public static ArrayList<CancerStudy> getAllCancerStudies() {
        return new ArrayList<CancerStudy>(byStableId.values());
    }

    /**
     * Gets Number of Cancer Studies.
     * @return number of cancer studies.
     */
    public static int getCount() {
        return byStableId.size();
    }

    /**
     * Deletes all Cancer Studies.
     * @throws DaoException Database Error.
     *
     * @deprecated this should not be used. Use deleteCancerStudy(cancerStudyStableId) instead
     */
    public static void deleteAllRecords() throws DaoException {
        cacheDateByStableId.clear();
        cacheDateByInternalId.clear();
        byStableId.clear();
        byInternalId.clear();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCancerStudy.class);
            pstmt = con.prepareStatement("TRUNCATE TABLE cancer_study");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCancerStudy.class, con, pstmt, rs);
        }
    }

    /**
     *
     * @param cancerStudyStableId
     * @throws DaoException
     * @deprecated
     */
    public static void deleteCancerStudy(String cancerStudyStableId) throws DaoException {
        CancerStudy study = getCancerStudyByStableId(cancerStudyStableId);
        if (study != null){
            deleteCancerStudy(study.getInternalId());
        }
    }

    public static Set<String> getFreshGroups(int internalCancerStudyId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCancerStudy.class);
            pstmt = con.prepareStatement("SELECT * FROM cancer_study where cancer_study_id = ?");
            pstmt.setInt(1, internalCancerStudyId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                CancerStudy cancerStudy = extractCancerStudy(rs);
                return cancerStudy.getGroups();
            } else {
                return Collections.emptySet();
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCancerStudy.class, con, pstmt, rs);
        }
    }

    /**
     * Deletes the Specified Cancer Study.
     * This method uses a large set of database operations to progressively delete all associated
     * data for a cancer study (including the study itself). A cleaner alternative based on
     * foreign key constraints and the cascade of delete operations is also available
     * (see deleteCancerStudyByCascade(int internalCancerStudyId)).
     *
     * Note to maintainers: Although deprecated, this method must be maintained for use in
     * environments where foreign key constraints are not in use, so remember to update the
     * implementation of this method if new tables are added or existing tables are changed.
     *
     * @param internalCancerStudyId Internal Cancer Study ID.
     * @throws DaoException Database Error.
     * @deprecated
     */
    public static void deleteCancerStudy(int internalCancerStudyId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            // The cancer_study row is deleted last; hide the study first so the portal does not
            // serve it while its data is partially deleted.
            setStatus(Status.UNAVAILABLE, null, internalCancerStudyId);

            // check whether should delete generic assay meta profile by profile
            DaoGenericAssay.checkAndDeleteGenericAssayMetaInStudy(internalCancerStudyId);
            
            String geneticProfileIdQuery = "SELECT genetic_profile_id FROM genetic_profile WHERE cancer_study_id=?";
            List<Long> geneticProfileIds = collectIds(geneticProfileIdQuery, internalCancerStudyId);
            String patientIdQuery = "SELECT internal_id FROM patient WHERE cancer_study_id=?";
            List<Long> patientIds = collectIds(patientIdQuery, internalCancerStudyId);
            String sampleIdQuery = "SELECT internal_id FROM sample WHERE patient_id IN (SELECT internal_id FROM patient WHERE cancer_study_id=?)";
            List<Long> sampleIds = collectIds(sampleIdQuery, internalCancerStudyId);
            String sampleListIdQuery = "SELECT list_id FROM sample_list WHERE cancer_study_id=?";
            List<Long> sampleListIds = collectIds(sampleListIdQuery, internalCancerStudyId);
            String clinicalEventIdQuery = "SELECT clinical_event_id FROM clinical_event WHERE patient_id IN (SELECT internal_id FROM patient WHERE cancer_study_id=?)";
            List<Long> clinicalEventIds = collectIds(clinicalEventIdQuery, internalCancerStudyId);
            String gisticIdQuery = "SELECT gistic_roi_id FROM gistic WHERE cancer_study_id=?";
            List<Long> gisticIds = collectIds(gisticIdQuery, internalCancerStudyId);

            ClickHouseBulkDeleter.getBulkDeleter("sample_cna_event", "genetic_profile_id").addIds(geneticProfileIds);
            ClickHouseBulkDeleter.getBulkDeleter("genetic_alteration", "genetic_profile_id").addIds(geneticProfileIds);
            ClickHouseBulkDeleter.getBulkDeleter("genetic_profile_samples", "genetic_profile_id").addIds(geneticProfileIds);
            ClickHouseBulkDeleter.getBulkDeleter("sample_profile", "genetic_profile_id").addIds(geneticProfileIds);
            ClickHouseBulkDeleter.getBulkDeleter("mutation", "genetic_profile_id").addIds(geneticProfileIds);
            ClickHouseBulkDeleter.getBulkDeleter("alteration_driver_annotation", "genetic_profile_id").addIds(geneticProfileIds);
            ClickHouseBulkDeleter.getBulkDeleter("mutation_count_by_keyword", "genetic_profile_id").addIds(geneticProfileIds);
            ClickHouseBulkDeleter.getBulkDeleter("structural_variant", "genetic_profile_id").addIds(geneticProfileIds);
            ClickHouseBulkDeleter.getBulkDeleter("genetic_profile_link", "referred_genetic_profile_id").addIds(geneticProfileIds);

            ClickHouseBulkDeleter.getBulkDeleter("gistic_to_gene", "gistic_roi_id").addIds(gisticIds);

            ClickHouseBulkDeleter.getBulkDeleter("clinical_event_data", "clinical_event_id").addIds(clinicalEventIds);
            ClickHouseBulkDeleter.getBulkDeleter("clinical_event", "clinical_event_id").addIds(clinicalEventIds);

            ClickHouseBulkDeleter.getBulkDeleter("sample_list_list", "list_id").addIds(sampleListIds);

            ClickHouseBulkDeleter.getBulkDeleter("clinical_sample", "internal_id").addIds(sampleIds);

            ClickHouseBulkDeleter.getBulkDeleter("sample", "internal_id").addIds(sampleIds);
            ClickHouseBulkDeleter.getBulkDeleter("clinical_patient", "internal_id").addIds(patientIds);

            ClickHouseBulkDeleter.flushAll();

            deleteByStudyId("DELETE FROM clinical_attribute_meta WHERE cancer_study_id=?", internalCancerStudyId);
            deleteByStudyId("DELETE FROM resource_definition WHERE cancer_study_id=?", internalCancerStudyId);
            // One statement for every resource row the study owns, whatever entity it hangs off:
            // resource carries cancer_study_id, where the three tables it replaced had to be
            // reached through the sample's and patient's internal ids and the study's own id.
            deleteByStudyId("DELETE FROM resource WHERE cancer_study_id=?", internalCancerStudyId);
            deleteByStudyId("DELETE FROM cancer_study_tags WHERE cancer_study_id=?", internalCancerStudyId);
            deleteByStudyId("DELETE FROM copy_number_seg WHERE cancer_study_id=?", internalCancerStudyId);
            deleteByStudyId("DELETE FROM copy_number_seg_file WHERE cancer_study_id=?", internalCancerStudyId);
            deleteByStudyId("DELETE FROM patient WHERE cancer_study_id=?", internalCancerStudyId);
            deleteByStudyId("DELETE FROM sample_list WHERE cancer_study_id=?", internalCancerStudyId);
            deleteByStudyId("DELETE FROM genetic_profile WHERE cancer_study_id=?", internalCancerStudyId);
            deleteByStudyId("DELETE FROM gistic WHERE cancer_study_id=?", internalCancerStudyId);
            deleteByStudyId("DELETE FROM mut_sig WHERE cancer_study_id=?", internalCancerStudyId);
            deleteByStudyId("DELETE FROM cancer_study WHERE cancer_study_id=?", internalCancerStudyId);

            removeCancerStudyFromCache(internalCancerStudyId);
        } catch (DaoException e) {
            throw e;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
        purgeUnreferencedRecordsAfterDeletionOfStudy();
        reCacheAll();
        System.out.println("deleted study:\nID: "+internalCancerStudyId);
    }

    /**
     * Cleans up unreferenced records after cancer study deletion
     * @throws DaoException
     */
    public static void purgeUnreferencedRecordsAfterDeletionOfStudy() throws DaoException {
        String[] deleteStudyStatements = {
            "DELETE FROM cna_event WHERE cna_event_id NOT IN (SELECT DISTINCT cna_event_id FROM sample_cna_event)",
            "DELETE FROM mutation_event WHERE mutation_event_id NOT IN (SELECT DISTINCT mutation_event_id FROM mutation)"
        };
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCancerStudy.class);
            for (String statementString : deleteStudyStatements) {
                pstmt = con.prepareStatement(statementString);
                pstmt.executeUpdate();
                pstmt.close();
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCancerStudy.class, con, pstmt, rs);
        }
    }

    private static List<Long> collectIds(String sql, int cancerStudyId) throws SQLException {
        List<Long> ids = new ArrayList<>();
        Connection con = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCancerStudy.class);
            try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                pstmt.setInt(1, cancerStudyId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        ids.add(rs.getLong(1));
                    }
                }
            }
        } finally {
            JdbcUtil.closeAll(DaoCancerStudy.class, con, null, null);
        }
        return ids;
    }

    private static void deleteByStudyId(String sql, int cancerStudyId) throws SQLException {
        Connection con = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCancerStudy.class);
            try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                pstmt.setInt(1, cancerStudyId);
                pstmt.executeUpdate();
            }
        } finally {
            JdbcUtil.closeAll(DaoCancerStudy.class, con, null, null);
        }
    }

    /**
     * Extracts Cancer Study JDBC Results.
     */
    private static CancerStudy extractCancerStudy(ResultSet rs) throws DaoException {
        try {
            CancerStudy cancerStudy = new CancerStudy(rs.getString("name"),
                    rs.getString("description"),
                    rs.getString("cancer_study_identifier"),
                    rs.getString("type_of_cancer_id"),
                    rs.getBoolean("public"));
            cancerStudy.setPmid(rs.getString("pmid"));
            cancerStudy.setCitation(rs.getString("citation"));
            cancerStudy.setGroupsInUpperCase(rs.getString("groups"));
            cancerStudy.setInternalId(rs.getInt("cancer_study_id"));
            cancerStudy.setImportDate(rs.getDate("import_date"));
            cancerStudy.setReferenceGenome(DaoReferenceGenome.getReferenceGenomeByInternalId(
                rs.getInt("reference_genome_id")).getGenomeName());
            return cancerStudy;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    private static boolean studyNeedsRecaching(String stableId, Integer ... internalId) {
        if (cacheOutOfSyncWithDb()) {
            return true;
        }
        try {
            java.util.Date importDate = null;
            java.util.Date cacheDate = null;
            if (internalId.length > 0) {
                importDate = getImportDate(null, internalId[0]);
                cacheDate = cacheDateByInternalId.get(internalId[0]);
            } else {
                if (stableId.equals(org.mskcc.cbio.portal.util.AccessControl.ALL_CANCER_STUDIES_ID)) {
                    return false;
                }
                importDate = getImportDate(stableId);
                cacheDate = cacheDateByStableId.get(stableId);
            }
            return (importDate == null || cacheDate == null) ? false : cacheDate.before(importDate);
        } catch (ParseException e) {
            return false;
        } catch (DaoException e) {
            return false;
        }
    }

    private static boolean cacheOutOfSyncWithDb() {
        try {
            return getStudyCount() != byStableId.size();
        } catch (DaoException e) {
        }
        return false;
    }
}
