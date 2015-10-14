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

import org.mskcc.cbio.portal.util.*;
import org.mskcc.cbio.portal.model.*;

import org.apache.commons.lang.StringUtils;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Analogous to and replaces the old DaoCancerType. A CancerStudy has a NAME and
 * DESCRIPTION. If PUBLIC is true a CancerStudy can be accessed by anyone,
 * otherwise can only be accessed through access control.
 *
 * @author Ethan Cerami
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 */
public final class DaoCancerStudy {

	public static enum Status
	{
		UNAVAILABLE, RECACHE, AVAILABLE
	}

    private DaoCancerStudy() {}
    
    private static final Map<String,CancerStudy> byStableId = new ConcurrentHashMap<String,CancerStudy>();
    private static final Map<Integer,CancerStudy> byInternalId = new ConcurrentHashMap<Integer,CancerStudy>();
    
    static {
        SpringUtil.initDataSource();
        reCache();
    }

	public static void reCacheAll()
	{
		reCache();
		DaoGeneticProfile.reCache();
		DaoPatient.reCache();
		DaoSample.reCache();
		DaoClinicalData.reCache();
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
				setStatus(Status.AVAILABLE, cancerStudy.getCancerStudyStableId());
            }
		} catch (DaoException e) {
			e.printStackTrace();
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
    
	public static void setStatus(Status status, String stableCancerStudyId, Integer ... internalId) throws DaoException
    {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCancerStudy.class);
			if (internalId.length > 0) {
            	pstmt = con.prepareStatement("UPDATE cancer_study set status = ? where cancer_study_id = ?");
            	pstmt.setInt(1, status.ordinal());
            	pstmt.setInt(2, internalId[0]);
			}
			else {
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

	public static Status getStatus(String stableCancerStudyId, Integer ... internalId) throws DaoException
    {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCancerStudy.class);
			if (internalId.length > 0) {
				pstmt = con.prepareStatement("SELECT status FROM cancer_study where cancer_study_id = ?");
				pstmt.setInt(1, internalId[0]);
			}
			else {
				pstmt = con.prepareStatement("SELECT status FROM cancer_study where cancer_study_identifier = ?");
				pstmt.setString(1, stableCancerStudyId);
			}
            rs = pstmt.executeQuery();
            if (rs.next()) {
				Integer status = rs.getInt(1);
				if (rs.wasNull()) {
					return Status.AVAILABLE;
				}
				else {
					return Status.values()[status];
				}
            }
            else {
                return Status.AVAILABLE;
            }
        } catch (SQLException e) {
			if (e.getMessage().toLowerCase().contains("unknown column")) {
				return Status.AVAILABLE;
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
				setStatus(Status.UNAVAILABLE, stableId);
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
    public static CancerStudy getCancerStudyByInternalId(int internalId) throws DaoException
	{
		if (getStatus(null, internalId) == Status.RECACHE) {
			reCacheAll();
		}
        return byInternalId.get(internalId);
    }

    /**
     * Returns the cancerStudy identified by the stable identifier, if it exists.
     *
     * @param cancerStudyStableId Cancer Study Stable ID.
     * @return the CancerStudy, or null if there's no such study.
     */
    public static CancerStudy getCancerStudyByStableId(String stableId) throws DaoException
	{
		if (getStatus(stableId) == Status.RECACHE) {
			reCacheAll();
		}
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
			setStatus(Status.UNAVAILABLE, cancerStudyStableId);
            deleteCancerStudy(study.getInternalId());
        }
    }

    public static Set<String> getFreshGroups(int internalCancerStudyId) throws DaoException
    {
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
            }
            else {
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
                "delete from clinical_event_data where CLINICAL_EVENT_ID IN (select CLINICAL_EVENT_ID from clinical_event where PATIENT_ID in (SELECT INTERNAL_ID FROM patient where CANCER_STUDY_ID=?))",
                "delete from clinical_event where PATIENT_ID in (SELECT INTERNAL_ID FROM patient where CANCER_STUDY_ID=?)",
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
        reCacheAll();
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
