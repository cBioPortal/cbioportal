/*
 * Copyright (c) 2015 - 2017 Memorial Sloan-Kettering Cancer Center.
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
import java.text.*;
import java.time.LocalDate;
import java.util.*;
import org.apache.commons.lang.StringUtils;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.*;

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

    static {
        SpringUtil.initDataSource();
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
            pstmt = con.prepareStatement("UPDATE cancer_study set IMPORT_DATE = NOW() where cancer_study_id = ?");
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
                pstmt = con.prepareStatement("SELECT IMPORT_DATE FROM cancer_study where cancer_study_id = ?");
                pstmt.setInt(1, internalId[0]);
            } else {
                pstmt = con.prepareStatement("SELECT IMPORT_DATE FROM cancer_study where cancer_study_identifier = ?");
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
                //setStatus(Status.UNAVAILABLE, stableId);
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
            pstmt = con.prepareStatement("INSERT INTO cancer_study " +
                    "( `CANCER_STUDY_IDENTIFIER`, `NAME`, "
                    + "`DESCRIPTION`, `PUBLIC`, `TYPE_OF_CANCER_ID`, "
                    + "`PMID`, `CITATION`, `GROUPS`, `SHORT_NAME`, `STATUS`, `IMPORT_DATE`,`REFERENCE_GENOME_ID` ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, stableId);
            pstmt.setString(2, cancerStudy.getName());
            pstmt.setString(3, cancerStudy.getDescription());
            pstmt.setBoolean(4, cancerStudy.isPublicStudy());
            pstmt.setString(5, cancerStudy.getTypeOfCancerId());
            pstmt.setString(6, cancerStudy.getPmid());
            pstmt.setString(7, cancerStudy.getCitation());
            Set<String> groups = cancerStudy.getGroups();
            if (groups==null) {
                pstmt.setString(8, null);
            } else {
                pstmt.setString(8, StringUtils.join(groups, ";"));
            }
            pstmt.setString(9, cancerStudy.getShortName());
            //status is UNAVAILABLE until other data is loaded for this study. Once all is loaded, the
            //data loading process can set this to AVAILABLE:
            //TODO - use this field in parts of the system that build up the list of studies to display in home page:
            pstmt.setInt(10, Status.UNAVAILABLE.ordinal());
            pstmt.setDate(11, java.sql.Date.valueOf(LocalDate.now()));
            try {
                ReferenceGenome referenceGenome = DaoReferenceGenome.getReferenceGenomeByGenomeName(cancerStudy.getReferenceGenome());
                pstmt.setInt(12, referenceGenome.getReferenceGenomeId());
            }
            catch (NullPointerException e) {
                throw new DaoException("Unsupported reference genome");
            }
            pstmt.executeUpdate();
            rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                int autoId = rs.getInt(1);
                cancerStudy.setInternalId(autoId);
            }
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
                        "( `CANCER_STUDY_ID`,`TAGS` ) VALUES (?,?)");
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
            JdbcUtil.disableForeignKeyCheck(con);
            pstmt = con.prepareStatement("TRUNCATE TABLE cancer_study");
            pstmt.executeUpdate();
            JdbcUtil.enableForeignKeyCheck(con);
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
            //setStatus(Status.UNAVAILABLE, cancerStudyStableId);
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
     * Calls deleteCancerStudyByCascade() if cancer study exists by stable id.
     * @param cancerStudyStableId
     * @throws DaoException
     */
    public static void deleteCancerStudyByCascade(String cancerStudyStableId) throws DaoException {
        CancerStudy study = getCancerStudyByStableId(cancerStudyStableId);
        if (study != null) {
            deleteCancerStudyByCascade(study.getInternalId());
        }
    }

    /**
     * Deletes a cancer study by internal id from db with foreign key constraints enforced.
     * @param internalCancerStudyId
     * @throws DaoException
     */
    public static void deleteCancerStudyByCascade(int internalCancerStudyId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCancerStudy.class);
            pstmt = con.prepareStatement("DELETE FROM cancer_study WHERE cancer_study_id = ?");
            pstmt.setInt(1, internalCancerStudyId);
            int rows = pstmt.executeUpdate();
            // throw dao exception if no rows affected by update
            if (rows == 0) {
                throw new DaoException("deleteCancerStudyByCascade() failed: No rows affected");
            }
            removeCancerStudyFromCache(internalCancerStudyId);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCancerStudy.class, con, pstmt, rs);
        }
        purgeUnreferencedRecordsAfterDeletionOfStudy();
        reCacheAll();
        System.out.println("deleted study:\nID: "+internalCancerStudyId);
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
        String[] deleteStudyStatements = {
                "DELETE FROM sample_cna_event WHERE GENETIC_PROFILE_ID IN (SELECT GENETIC_PROFILE_ID FROM genetic_profile WHERE CANCER_STUDY_ID=?)",
                "DELETE FROM generic_entity_properties WHERE GENETIC_ENTITY_ID IN (SELECT GENETIC_ENTITY_ID FROM genetic_alteration WHERE GENETIC_PROFILE_ID IN (SELECT GENETIC_PROFILE_ID FROM genetic_profile WHERE CANCER_STUDY_ID=?))",
                "DELETE FROM genetic_entity WHERE ID IN (SELECT GENETIC_ENTITY_ID FROM genetic_alteration WHERE GENETIC_PROFILE_ID IN (SELECT GENETIC_PROFILE_ID FROM genetic_profile WHERE CANCER_STUDY_ID=? AND GENETIC_ALTERATION_TYPE='GENERIC_ASSAY'))",
                "DELETE FROM genetic_alteration WHERE GENETIC_PROFILE_ID IN (SELECT GENETIC_PROFILE_ID FROM genetic_profile WHERE CANCER_STUDY_ID=?)",
                "DELETE FROM genetic_profile_samples WHERE GENETIC_PROFILE_ID IN (SELECT GENETIC_PROFILE_ID FROM genetic_profile WHERE CANCER_STUDY_ID=?)",
                "DELETE FROM sample_profile WHERE GENETIC_PROFILE_ID IN (SELECT GENETIC_PROFILE_ID FROM genetic_profile WHERE CANCER_STUDY_ID=?)",
                "DELETE FROM mutation WHERE GENETIC_PROFILE_ID IN (SELECT GENETIC_PROFILE_ID FROM genetic_profile WHERE CANCER_STUDY_ID=?)",
                "DELETE FROM mutation_count_by_keyword WHERE GENETIC_PROFILE_ID IN (SELECT GENETIC_PROFILE_ID FROM genetic_profile WHERE CANCER_STUDY_ID=?)",
                "DELETE FROM clinical_attribute_meta WHERE CANCER_STUDY_ID=?",
                "DELETE FROM resource_definition WHERE CANCER_STUDY_ID=?",
                "DELETE FROM resource_study WHERE INTERNAL_ID=?",
                "DELETE FROM clinical_event_data WHERE CLINICAL_EVENT_ID IN (SELECT CLINICAL_EVENT_ID FROM clinical_event WHERE PATIENT_ID IN (SELECT INTERNAL_ID FROM patient WHERE CANCER_STUDY_ID=?))",
                "DELETE FROM clinical_event WHERE PATIENT_ID IN (SELECT INTERNAL_ID FROM patient WHERE CANCER_STUDY_ID=?)",
                "DELETE FROM sample_list_list WHERE LIST_ID IN (SELECT LIST_ID FROM sample_list WHERE CANCER_STUDY_ID=?)",
                "DELETE FROM clinical_sample WHERE INTERNAL_ID IN (SELECT INTERNAL_ID FROM sample WHERE PATIENT_ID IN (SELECT INTERNAL_ID FROM patient WHERE CANCER_STUDY_ID=?))",
                "DELETE FROM resource_sample WHERE INTERNAL_ID IN (SELECT INTERNAL_ID FROM sample WHERE PATIENT_ID IN (SELECT INTERNAL_ID FROM patient WHERE CANCER_STUDY_ID=?))",
                "DELETE FROM copy_number_seg WHERE CANCER_STUDY_ID=?",
                "DELETE FROM copy_number_seg_file WHERE CANCER_STUDY_ID=?",
                "DELETE FROM protein_array_data WHERE CANCER_STUDY_ID=?",
                "DELETE FROM protein_array_cancer_study WHERE CANCER_STUDY_ID=?",
                "DELETE FROM sample WHERE PATIENT_ID IN (SELECT INTERNAL_ID FROM patient WHERE CANCER_STUDY_ID=?)",
                "DELETE FROM clinical_patient WHERE INTERNAL_ID IN (SELECT INTERNAL_ID FROM patient WHERE CANCER_STUDY_ID=?)",
                "DELETE FROM resource_patient WHERE INTERNAL_ID IN (SELECT INTERNAL_ID FROM patient WHERE CANCER_STUDY_ID=?)",
                "DELETE FROM patient WHERE CANCER_STUDY_ID=?",
                "DELETE FROM sample_list WHERE CANCER_STUDY_ID=?",
                "DELETE FROM structural_variant WHERE GENETIC_PROFILE_ID IN (SELECT GENETIC_PROFILE_ID FROM genetic_profile WHERE CANCER_STUDY_ID=?)",
                "DELETE FROM genetic_profile_link WHERE REFERRED_GENETIC_PROFILE_ID IN (select GENETIC_PROFILE_ID FROM genetic_profile where CANCER_STUDY_ID=?)",
                "DELETE FROM genetic_profile WHERE CANCER_STUDY_ID=?",
                "DELETE FROM gistic_to_gene WHERE GISTIC_ROI_ID IN (SELECT GISTIC_ROI_ID FROM gistic WHERE CANCER_STUDY_ID=?)",
                "DELETE FROM gistic WHERE CANCER_STUDY_ID=?",
                "DELETE FROM mut_sig WHERE CANCER_STUDY_ID=?",
                "DELETE FROM cancer_study WHERE CANCER_STUDY_ID=?;"
                };
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCancerStudy.class);
            for (String statementString : deleteStudyStatements) {
                pstmt = con.prepareStatement(statementString);
                if (statementString.contains("?")) {
                    pstmt.setInt(1, internalCancerStudyId);
                }
                pstmt.executeUpdate();
                pstmt.close();
            }
            removeCancerStudyFromCache(internalCancerStudyId);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCancerStudy.class, con, pstmt, rs);
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
                "DELETE FROM cna_event WHERE NOT EXISTS (SELECT * FROM sample_cna_event WHERE sample_cna_event.CNA_EVENT_ID = cna_event.CNA_EVENT_ID)",
                "DELETE FROM mutation_event WHERE NOT EXISTS (SELECT * FROM mutation WHERE mutation.MUTATION_EVENT_ID = mutation_event.MUTATION_EVENT_ID)"
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

    /**
     * Extracts Cancer Study JDBC Results.
     */
    private static CancerStudy extractCancerStudy(ResultSet rs) throws DaoException {
        try {
            CancerStudy cancerStudy = new CancerStudy(rs.getString("NAME"),
                    rs.getString("DESCRIPTION"),
                    rs.getString("CANCER_STUDY_IDENTIFIER"),
                    rs.getString("TYPE_OF_CANCER_ID"),
                    rs.getBoolean("PUBLIC"));
            cancerStudy.setPmid(rs.getString("PMID"));
            cancerStudy.setCitation(rs.getString("CITATION"));
            cancerStudy.setGroupsInUpperCase(rs.getString("GROUPS"));
            cancerStudy.setShortName(rs.getString("SHORT_NAME"));
            cancerStudy.setInternalId(rs.getInt("CANCER_STUDY_ID"));
            cancerStudy.setImportDate(rs.getDate("IMPORT_DATE"));
            cancerStudy.setReferenceGenome(DaoReferenceGenome.getReferenceGenomeByInternalId(
                rs.getInt("REFERENCE_GENOME_ID")).getGenomeName());
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
