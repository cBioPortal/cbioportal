/*
 * Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
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
 * has been advised of the possibility of such damage.
*/

package org.mskcc.cbio.portal.dao;

import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.ImportDataUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.mskcc.cbio.portal.util.ImportDataUtil;

/**
 * Analogous to and replaces the old DaoCancerType. A CancerStudy has a NAME and
 * DESCRIPTION. If PUBLIC is true a CancerStudy can be accessed by anyone,
 * otherwise can only be accessed through access control.
 *
 * @author Ethan Cerami
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 */
public final class DaoCancerStudy {
    private DaoCancerStudy() {}
    
    private static final Map<String,CancerStudy> byStableId = new HashMap<String,CancerStudy>();
    private static final Map<Integer,CancerStudy> byInternalId = new HashMap<Integer,CancerStudy>();
    
    static {
       reCache();
    }
    
    private static synchronized void reCache() {
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
                cacheCancerStudy(cancerStudy);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            JdbcUtil.closeAll(DaoCancerStudy.class, con, pstmt, rs);
        }
    }
    
    private static void cacheCancerStudy(CancerStudy study) {
        byStableId.put(study.getCancerStudyStableId(), study);
        byInternalId.put(study.getInternalId(), study);
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
        TypeOfCancer aTypeOfCancer = DaoTypeOfCancer.getTypeOfCancerById
                (cancerStudy.getTypeOfCancerId());
        if (null == aTypeOfCancer) {
            throw new DaoException("cancerStudy.getTypeOfCancerId() '"
                    + cancerStudy.getTypeOfCancerId()
                    + "' does not refer to a TypeOfCancer.");
        }
        
        // CANCER_STUDY_IDENTIFIER cannot be null
        String stableId = cancerStudy.getCancerStudyStableId();
        if (stableId == null) {
            throw new DaoException("Cancer study stable ID cannot be null.");
        }
        
        CancerStudy existing = getCancerStudyByStableId(stableId);
        if (existing!=null) {
            if (overwrite) {
                System.out.println("Overwrite cancer study " + stableId);
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
                    + "`PMID`, `CITATION`, `GROUPS`, `SHORT_NAME` ) VALUES (?,?,?,?,?,?,?,?,?)",
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

            pstmt.executeUpdate();
            rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                int autoId = rs.getInt(1);
                cancerStudy.setInternalId(autoId);
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCancerStudy.class, con, pstmt, rs);
        }
        
        reCache();
    }

    /**
     * Return the cancerStudy identified by the internal cancer study ID, if it exists.
     *
     * @param cancerStudyID     Internal (int) Cancer Study ID.
     * @return Cancer Study Object, or null if there's no such study.
     */
    public static CancerStudy getCancerStudyByInternalId(int cancerStudyID) {
        return byInternalId.get(cancerStudyID);
    }

    /**
     * Returns the cancerStudy identified by the stable identifier, if it exists.
     *
     * @param cancerStudyStableId Cancer Study Stable ID.
     * @return the CancerStudy, or null if there's no such study.
     */
    public static CancerStudy getCancerStudyByStableId(String cancerStudyStableId) {
        return byStableId.get(cancerStudyStableId);
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
     */
    public static void deleteAllRecords() throws DaoException {
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

    public static void deleteCancerStudy(String cancerStudyStableId) throws DaoException
    {
        CancerStudy study = getCancerStudyByStableId(cancerStudyStableId);
        if (study != null){
            deleteCancerStudy(study.getInternalId());
        }
    }

    /**
     * Deletes the Specified Cancer Study.
     *
     * @param internalCancerStudyId Internal Cancer Study ID.
     * @throws DaoException Database Error.
     */
    static void deleteCancerStudy(int internalCancerStudyId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCancerStudy.class);
            
            // this is a hacky way to delete all associated data with on cancer study.
            // ideally database dependency should be modeled with option of delete on cascade.
            // remember to update this code if new tables are added or existing tables are changed.
            String[] sqls = {
                "delete from sample_cna_event where GENETIC_PROFILE_ID IN (select GENETIC_PROFILE_ID from genetic_profile where CANCER_STUDY_ID=?);",
                "delete from genetic_alteration where GENETIC_PROFILE_ID IN (select GENETIC_PROFILE_ID from genetic_profile where CANCER_STUDY_ID=?);",
                "delete from genetic_profile_samples where GENETIC_PROFILE_ID IN (select GENETIC_PROFILE_ID from genetic_profile where CANCER_STUDY_ID=?);",
                "delete from sample_profile where GENETIC_PROFILE_ID IN (select GENETIC_PROFILE_ID from genetic_profile where CANCER_STUDY_ID=?);",
                "delete from mutation where GENETIC_PROFILE_ID IN (select GENETIC_PROFILE_ID from genetic_profile where CANCER_STUDY_ID=?);",
                "delete from mutation_event where MUTATION_EVENT_ID NOT IN (select MUTATION_EVENT_ID from mutation);",
                "delete from mutation_count where GENETIC_PROFILE_ID IN (select GENETIC_PROFILE_ID from genetic_profile where CANCER_STUDY_ID=?);",
                "delete from patient_list_list where LIST_ID IN (select LIST_ID from patient_list where CANCER_STUDY_ID=?);",
                "delete from clinical_sample where INTERNAL_ID IN (select INTERNAL_ID from sample where PATIENT_ID in (select INTERNAL_ID from patient where CANCER_STUDY_ID=?));",
                "delete from sample where PATIENT_ID IN (select INTERNAL_ID from patient where CANCER_STUDY_ID=?);",
                "delete from clinical_patient where INTERNAL_ID IN (select INTERNAL_ID from patient where CANCER_STUDY_ID=?);",
                "delete from patient where CANCER_STUDY_ID=?;",
                "delete from copy_number_seg where CANCER_STUDY_ID=?;",
                "delete from patient_list where CANCER_STUDY_ID=?;",
                "delete from genetic_profile where CANCER_STUDY_ID=?;",
                "delete from gistic_to_gene where GISTIC_ROI_ID IN (select GISTIC_ROI_ID from gistic where CANCER_STUDY_ID=?);",
                "delete from gistic where CANCER_STUDY_ID=?;",
                "delete from mut_sig where CANCER_STUDY_ID=?;",
                "delete from protein_array_data where CANCER_STUDY_ID=?;",
                "delete from protein_array_cancer_study where CANCER_STUDY_ID=?;",
                "delete from clinical_event_data where CLINICAL_EVENT_ID IN (select CLINICAL_EVENT_ID from clinical_event where CANCER_STUDY_ID=?)",
                "delete from clinical_event where CANCER_STUDY_ID=?;",
                "delete from cancer_study where CANCER_STUDY_ID=?;"
                };
            for (String sql : sqls) {    
                pstmt = con.prepareStatement(sql);
                if (sql.contains("?")) {
                    pstmt.setInt(1, internalCancerStudyId);
                }
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCancerStudy.class, con, pstmt, rs);
        }
        deleteCancerStudyEntities(internalCancerStudyId);
        reCache();
    }

    private static void deleteCancerStudyEntities(int internalCancerStudyId) throws DaoException
    {
        CancerStudy study = getCancerStudyByInternalId(internalCancerStudyId);
        Entity studyEntity = ImportDataUtil.entityService.getCancerStudy(study.getCancerStudyStableId());

        for (Entity patientEntity : ImportDataUtil.entityMapper.getChildren(studyEntity.internalId, EntityType.PATIENT)) {
            for (Entity sampleEntity : ImportDataUtil.entityMapper.getChildren(patientEntity.internalId, EntityType.SAMPLE)) {
                deleteEntity(sampleEntity.internalId);
            }
            deleteEntity(patientEntity.internalId);
        }
        deleteEntity(studyEntity.internalId);
    }

    private static void deleteEntity(int entityId)
    {
        ImportDataUtil.entityMapper.deleteEntity(entityId);
        ImportDataUtil.entityMapper.deleteEntityLinks(entityId);
        ImportDataUtil.entityAttributeMapper.deleteEntityAttributes(entityId);
    }

    /**
     * Extracts Cancer Study JDBC Results.
     */
    private static CancerStudy extractCancerStudy(ResultSet rs) throws SQLException {
        CancerStudy cancerStudy = new CancerStudy(rs.getString("NAME"),
                rs.getString("DESCRIPTION"),
                rs.getString("CANCER_STUDY_IDENTIFIER"),
                rs.getString("TYPE_OF_CANCER_ID"),
                rs.getBoolean("PUBLIC"));
        cancerStudy.setPmid(rs.getString("PMID"));
        cancerStudy.setCitation(rs.getString("CITATION"));
        cancerStudy.setGroups(rs.getString("GROUPS"));
        cancerStudy.setShortName(rs.getString("SHORT_NAME"));

        cancerStudy.setInternalId(rs.getInt("CANCER_STUDY_ID"));
        return cancerStudy;
    }
    
}
