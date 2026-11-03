/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.dao;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;
import org.mskcc.cbio.portal.model.shared.GeneticAlterationType;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.model.Patient;
import org.mskcc.cbio.portal.model.Sample;
import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DAO to `sample`.
 * 
 * @author Benjamin Gross
 */
public class DaoSample {


    private static final Logger log = LoggerFactory.getLogger(DaoSample.class);

    private static final int MISSING_CANCER_STUDY_ID = -1;

    private static final Map<String, Sample> byStableId = new HashMap<String, Sample>();
    private static final Map<Integer, Sample> byInternalId = new HashMap<Integer, Sample>();
    private static final Map<Integer, Map<String, Sample>> byInternalPatientAndStableSampleId = new HashMap<Integer, Map<String, Sample>>();
    private static final Map<Integer, Map<String, Sample>> byCancerStudyIdAndStableSampleId = new HashMap<Integer, Map<String, Sample>>();

    private static void clearCache()
    {
        byStableId.clear();
        byInternalId.clear();
        byInternalPatientAndStableSampleId.clear();
        byCancerStudyIdAndStableSampleId.clear();
    }

    public static synchronized void reCache()
    {
        clearCache();

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoSample.class);
            pstmt = con.prepareStatement("SELECT * FROM sample");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                cacheSample(extractSample(rs));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            JdbcUtil.closeAll(DaoSample.class, con, pstmt, rs);
        }
    }

    private static void cacheSample(Sample sample)
    {
        cacheSample(sample, getCancerStudyId(sample));
    }

    private static int getCancerStudyId(Sample sample)
    {
        Patient patient = DaoPatient.getPatientById(sample.getInternalPatientId());
        return (patient == null) ? MISSING_CANCER_STUDY_ID : patient.getCancerStudy().getInternalId();
    }

    private static void cacheSample(Sample sample, int cancerStudyId)
    {
        if (!byStableId.containsKey(sample.getStableId())) {
            byStableId.put(sample.getStableId(), sample);
        }

        if (!byInternalId.containsKey(sample.getInternalId())) {
            byInternalId.put(sample.getInternalId(), sample);
        }

        Map<String, Sample> samples = byInternalPatientAndStableSampleId.get(sample.getInternalPatientId());
        if (samples==null) {
            samples = new HashMap<String, Sample>();
            byInternalPatientAndStableSampleId.put(sample.getInternalPatientId(), samples);
        }
        if (samples.containsKey(sample.getStableId())) {
            System.err.println("Something is wrong: there are two samples of "+sample.getStableId()+" in the same patient.");
        }
        samples.put(sample.getStableId(), sample);

        samples = byCancerStudyIdAndStableSampleId.get(cancerStudyId);
        if (samples==null) {
            samples = new HashMap<String, Sample>();
            byCancerStudyIdAndStableSampleId.put(cancerStudyId, samples);
        }
        if (samples.containsKey(sample.getStableId())) {
            System.err.println("Something is wrong: there are two samples of "+sample.getStableId()+" in the same study.");
        }
        samples.put(sample.getStableId(), sample);
    }

    private static final String SAMPLE_SEQUENCE = "seq_sample";

    public static int addSample(Sample sample) throws DaoException
    {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            long internalId = ClickHouseAutoIncrement.nextId(SAMPLE_SEQUENCE);
            if (ClickHouseBulkLoader.isBulkLoad()) {
                ClickHouseBulkLoader loader = ClickHouseBulkLoader.getClickHouseBulkLoader("sample");
                loader.setFieldNames(new String[]{"internal_id", "stable_id", "sample_type", "patient_id"});
                loader.insertRecord(
                    Long.toString(internalId),
                    sample.getStableId(),
                    sample.getType().toString(),
                    Integer.toString(sample.getInternalPatientId())
                );
            } else {
                con = JdbcUtil.getDbConnection(DaoSample.class);
                pstmt = con.prepareStatement("INSERT INTO sample " +
                                             "( `internal_id`, `stable_id`, `sample_type`, `patient_id` ) " +
                                             "VALUES (?,?,?,?)");
                pstmt.setLong(1, internalId);
                pstmt.setString(2, sample.getStableId());
                pstmt.setString(3, sample.getType().toString());
                pstmt.setInt(4, sample.getInternalPatientId());
                pstmt.executeUpdate();
            }
            cacheSample(new Sample((int) internalId, sample.getStableId(),
                    sample.getInternalPatientId()));
            return (int) internalId;
        }
        catch (SQLException e) {
            throw new DaoException(e);
        }
        finally {
            JdbcUtil.closeAll(DaoSample.class, con, pstmt, null);
        }
    }

    public static List<Sample> getAllSamples()
    {
        return (byStableId.isEmpty()) ? Collections.<Sample>emptyList() :
            new ArrayList<Sample>(byStableId.values());
    }

    public static Sample getSampleById(int internalId)
    {
        return byInternalId.get(internalId);
    }

    public static List<Sample> getSamplesByPatientId(int internalPatientId)
    {
        return (byInternalPatientAndStableSampleId.isEmpty() || !byInternalPatientAndStableSampleId.containsKey(internalPatientId)) ? Collections.<Sample>emptyList() :
            new ArrayList<Sample>(byInternalPatientAndStableSampleId.get(internalPatientId).values());
    }

    public static Sample getSampleByPatientAndSampleId(int internalPatientId, String stableSampleId)
    {
        Map<String, Sample> samples = byInternalPatientAndStableSampleId.get(internalPatientId);
        if (samples==null) {
            return null;
        }
        
        return samples.get(stableSampleId);
    }
    
    public static List<Sample> getSamplesByCancerStudy(int cancerStudyId)
    {
        Map<String, Sample> samples = byCancerStudyIdAndStableSampleId.get(cancerStudyId);
        if (samples==null) {
            return Collections.emptyList();
        }
        
        return new ArrayList<Sample>(samples.values());
    }
    
    /**
     * This method returns the list of sample (stable) ids for a study, e.g. TCGA-A1-A0SB-01, etc.
     * 
     * @param cancerStudyId : the cancer study internal id
     * @return
     */
    public static List<String> getSampleStableIdsByCancerStudy(int cancerStudyId) 
    {
    	List<Sample> samples = getSamplesByCancerStudy(cancerStudyId);
    	List<String> result = new ArrayList<String>();
    	for (Sample sample : samples) {
    		result.add(sample.getStableId());
    	}
    	return result;
    }

    public static Sample getSampleByCancerStudyAndSampleId(int cancerStudyId, String stableSampleId)
    {
    	return getSampleByCancerStudyAndSampleId(cancerStudyId, stableSampleId, true);
    	
    }
    
    /**
     * Same as getSampleByCancerStudyAndSampleId, but with extra option on whether or not
     * to log extra warning when sample is not found. 
     * 
     * @param cancerStudyId
     * @param stableSampleId
     * @param errorWhenNotFound : set to true to log warning in ProgressMonitor in case sample is not found. 
     * Some processes, like ImportClinicalData do not want an error here, but use the null result as a flag
     * to decide whether a new sample should be added to the DB. These processes should set this parameter
     * to false.
     * 
     * @return Sample object if sample was found in cache, null otherwise.
     */
    public static Sample getSampleByCancerStudyAndSampleId(int cancerStudyId, String stableSampleId, boolean errorWhenNotFound)
    {
        Map<String, Sample> samples = byCancerStudyIdAndStableSampleId.get(cancerStudyId);
        if (samples==null) {
        	if (errorWhenNotFound) {
        		ProgressMonitor.logWarning("Couldn't find sample "+stableSampleId+" in study "+cancerStudyId);
        	}
            return null;
        }
        
        return samples.get(stableSampleId);
    }

    public static void deleteAllRecords() throws DaoException
    {
        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            con = JdbcUtil.getDbConnection(DaoSample.class);
            pstmt = con.prepareStatement("TRUNCATE TABLE sample");
            pstmt.executeUpdate();
        }
        catch (SQLException e) {
            throw new DaoException(e);
        }
        finally {
            JdbcUtil.closeAll(DaoSample.class, con, pstmt, null);
        }

        clearCache();
    }

    /**
     * Remove set of samples from the study
     * @param internalStudyId - id of the study that contains the samples
     * @param sampleStableIds - sample stable ids of samples to remove
     * @throws DaoException
     */
    public static void deleteSamples(int internalStudyId, Set<String> sampleStableIds) throws DaoException
    {
        if (sampleStableIds == null || sampleStableIds.isEmpty()) {
            log.info("No samples specified to remove for study with internal id={}. Skipping.", internalStudyId);
            return;
        }

        log.info("Removing {} samples from study with internal id={} ...", sampleStableIds, internalStudyId);

        Set<Integer> internalSampleIds = findInternalSampleIdsInStudy(internalStudyId, sampleStableIds);
        removeSamplesInGeneticAlterationsForStudy(internalStudyId, internalSampleIds);

        ClickHouseBulkDeleter.getBulkDeleter("alteration_driver_annotation", "sample_id").addIds(internalSampleIds);
        ClickHouseBulkDeleter.getBulkDeleter("sample_cna_event", "sample_id").addIds(internalSampleIds);
        ClickHouseBulkDeleter.getBulkDeleter("mutation", "sample_id").addIds(internalSampleIds);
        ClickHouseBulkDeleter.getBulkDeleter("structural_variant", "sample_id").addIds(internalSampleIds);
        ClickHouseBulkDeleter.getBulkDeleter("sample_profile", "sample_id").addIds(internalSampleIds);
        ClickHouseBulkDeleter.getBulkDeleter("sample_list_list", "sample_id").addIds(internalSampleIds);
        ClickHouseBulkDeleter.getBulkDeleter("copy_number_seg", "sample_id").addIds(internalSampleIds);
        ClickHouseBulkDeleter.getBulkDeleter("allele_specific_copy_number", "sample_id").addIds(internalSampleIds);
        ClickHouseBulkDeleter.getBulkDeleter("clinical_sample", "internal_id").addIds(internalSampleIds);
        DaoResourceData.deleteResourceDataForSamples(internalStudyId, sampleStableIds);
        ClickHouseBulkDeleter.getBulkDeleter("sample", "internal_id").addIds(internalSampleIds);
        ClickHouseBulkDeleter.flushAll();
        clearCache();
        DaoCancerStudy.purgeUnreferencedRecordsAfterDeletionOfStudy();
        log.info("Removing {} samples from study with internal id={} done.", sampleStableIds, internalStudyId);
    }

    private static Sample extractSample(ResultSet rs) throws SQLException
    {
        return new Sample(rs.getInt("internal_id"),
                          rs.getString("stable_id"),
                          rs.getInt("patient_id"));
    }

    /**
     * Removes samples from genetic alteration data for a study.
     * <p>
     * The affected {@code genetic_alteration} and {@code genetic_profile_samples}
     * tables are backed up before removal starts and restored from backup if the
     * operation fails.
     *
     * @param internalStudyId internal ID of the study whose genetic alteration sample data should be updated
     * @param internalSampleIdsToRemove internal IDs of samples to remove
     * @throws DaoException if removing samples from genetic alteration data fails
     */
    private static void removeSamplesInGeneticAlterationsForStudy(int internalStudyId, Set<Integer> internalSampleIdsToRemove) throws DaoException {
        try {
            BackupUtil.backup(List.of("genetic_alteration", "genetic_profile_samples"), () -> {
                List<GeneticProfile> geneticProfiles = DaoGeneticProfile.getAllGeneticProfiles(internalStudyId);
                for (GeneticProfile geneticProfile : geneticProfiles) {
                    Set<Integer> removedInternalSampleIds = removeSamplesInGeneticAlterationsForGeneticProfile(geneticProfile, internalSampleIdsToRemove);
                    log.debug("Genetic alterations data for {} sample ids ouf of {} requested have been removed for genetic profile with stable id={}",
                            removedInternalSampleIds, internalSampleIdsToRemove, geneticProfile.getStableId());
                }
            });
        } catch (Exception e) {
            throw new DaoException(e);
        }
    }

    /**
     * Removes sample in genetic alterations' data for a genetic profile
     * @param geneticProfile - genetic profile to remove samples in genetic alteration data
     * @param internalSampleIdsToRemove - internal ids of samples to remove
     * @return set of sample internal ids that were actually removed
     * @throws DaoException
     */
    private static Set<Integer> removeSamplesInGeneticAlterationsForGeneticProfile(GeneticProfile geneticProfile, Set<Integer> internalSampleIdsToRemove) throws DaoException {
        int geneticProfileId = geneticProfile.getGeneticProfileId();
        List<Integer> orderedSampleList = DaoGeneticProfileSamples.getOrderedSampleList(geneticProfileId);
        Set<Integer> actualInternalSampleIdsToRemove = orderedSampleList.stream()
                .filter(internalSampleIdsToRemove::contains).collect(Collectors.toUnmodifiableSet());
        if (!actualInternalSampleIdsToRemove.isEmpty()) {
            if (GeneticAlterationType.GENESET_SCORE.equals(geneticProfile.getGeneticAlterationType())) {
                List<String> sampleStableIds = actualInternalSampleIdsToRemove.stream()
                        .map(internalSampleID ->
                                DaoSample.getSampleById(internalSampleID).getStableId())
                        .toList();
                throw new RuntimeException("Sample(s) with stable id "
                        + String.join(", ", sampleStableIds)
                        + " can't be removed as it contains GSVA data." +
                        " Consider dropping and re-uploading the whole study.");
            }
            orderedSampleList.removeAll(actualInternalSampleIdsToRemove);
            HashMap<Integer, HashMap<Integer, String>> geneticAlterationMapForEntityIds = DaoGeneticAlteration.getInstance().getGeneticAlterationMapForEntityIds(geneticProfileId, null);
            DaoGeneticAlteration.getInstance().deleteAllRecordsInGeneticProfile(geneticProfileId);
            if (!orderedSampleList.isEmpty()) {
                for (Map.Entry<Integer, HashMap<Integer, String>> entry : geneticAlterationMapForEntityIds.entrySet()) {
                    String[] values = orderedSampleList.stream().map(isid -> entry.getValue().get(isid)).toArray(String[]::new);
                    DaoGeneticAlteration.getInstance().addGeneticAlterationsForGeneticEntity(geneticProfileId, entry.getKey(), values);
                }
            }
            DaoGeneticProfileSamples.deleteAllSamplesInGeneticProfile(geneticProfileId);
            if (!orderedSampleList.isEmpty()) {
                DaoGeneticProfileSamples.addGeneticProfileSamples(geneticProfileId, orderedSampleList);
            }
        }
        return actualInternalSampleIdsToRemove;
    }

    public static Set<Integer> findInternalSampleIdsInStudy(Integer internalStudyId, Set<String> sampleStableIds) {
        HashSet<Integer> internalSampleIds = new HashSet<>();
        for (String sampleId : sampleStableIds) {
            Sample sampleByCancerStudyAndSampleId = DaoSample.getSampleByCancerStudyAndSampleId(internalStudyId, sampleId);
            if (sampleByCancerStudyAndSampleId == null) {
                throw new NoSuchElementException("Sample with stable id=" + sampleId + " not found in study with internal id=" + internalStudyId + ".");
            }
            internalSampleIds.add(sampleByCancerStudyAndSampleId.getInternalId());
        }
        return internalSampleIds;
    }

}
