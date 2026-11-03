/*
 * Copyright (c) 2015 - 2022 Memorial Sloan Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.util;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneticProfile;
import org.mskcc.cbio.portal.dao.DaoGeneticProfileLink;
import org.mskcc.cbio.portal.dao.DaoInfo;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.shared.GeneticAlterationType;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.model.GeneticProfileLink;
import org.mskcc.cbio.portal.model.shared.MolecularProfileDataType;
import org.mskcc.cbio.portal.scripts.TrimmedProperties;

/**
 * Prepare a GeneticProfile for having its data loaded.
 *
 * @author Ethan Cerami
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 */
public class GeneticProfileReader {

    /**
     * Load a GeneticProfile. Get a stableID from a description file. If the same
     * GeneticProfile already exists in the dbms use it, otherwise create a new
     * GeneticProfile dbms record, defining all parameters from the file.
     *
     * @author Ethan Cerami
     * @author Arthur Goldberg goldberg@cbio.mskcc.org
     *
     * @param file
     *           A handle to a description of the genetic profile, i.e., a
     *           'description' or 'meta' file.
     * @return an instantiated GeneticProfile record
     * @throws IOException
     *            if the description file cannot be read
     * @throws DaoException
     */
    public static GeneticProfile loadGeneticProfile(File file) throws IOException, DaoException {
        GeneticProfile geneticProfile = loadGeneticProfileFromMeta(file);
        GeneticProfile existingGeneticProfile = DaoGeneticProfile.getGeneticProfileByStableId(geneticProfile.getStableId());
        if (existingGeneticProfile != null) {
            ProgressMonitor.setCurrentMessage("genetic_profile record found with same Stable ID (" + geneticProfile.getStableId()
                    + "). Using it instead.");
            if (geneticProfile.getGeneticAlterationType() != existingGeneticProfile.getGeneticAlterationType()) {
                throw new IllegalStateException("genetic_profile record found with same Stable ID ("
                                          + existingGeneticProfile.getStableId() + ") but different genetic alteration type: "
                                          + existingGeneticProfile.getGeneticProfileId());
            }
            if (MolecularProfileDataType.DISCRETE_LONG.equals(geneticProfile.getDatatype())) {
                if (!Set.of(MolecularProfileDataType.DISCRETE_LONG, MolecularProfileDataType.DISCRETE)
                        .contains(existingGeneticProfile.getDatatype())) {
                    throw new IllegalStateException("genetic_profile record found with same Stable ID ("
                            + existingGeneticProfile.getStableId() + ") but unsupported data type: "
                            + existingGeneticProfile.getDatatype());
                }
            } else {
                if (!existingGeneticProfile.getDatatype().equals(geneticProfile.getDatatype())) {
                    throw new IllegalStateException("genetic_profile record found with same Stable ID ("
                            + existingGeneticProfile.getStableId() + ") but different data type: "
                            + existingGeneticProfile.getDatatype());
                }
            }
            if (geneticProfile.getCancerStudyId() != existingGeneticProfile.getCancerStudyId()) {
                throw new IllegalStateException("genetic_profile record found with same Stable ID ("
                        + existingGeneticProfile.getStableId() + ") but different cancer study (id="
                        + existingGeneticProfile.getCancerStudyId() + ")");
            }
            existingGeneticProfile.setOtherMetadataFields(geneticProfile.getAllOtherMetadataFields());
            return existingGeneticProfile;
        }

        // For GSVA profiles, we want to create a geneticProfileLink from source_stable_id for:
        // - expression zscores -> expression
        // - gsva scores -> expression
        // - gsva pvalues -> gsva scores
        // Currently we only create geneticProfileLink for expression zscores when it's available.
        // This geneticProfileLink is required for the oncoprint in a GSVA study. In the future it might
        // be useful to make this a requirement for every expression zscore file.
        GeneticProfileLink geneticProfileLink = null;
        if (geneticProfile.getGeneticAlterationType() == GeneticAlterationType.GENESET_SCORE ||
                (geneticProfile.getGeneticAlterationType() == GeneticAlterationType.MRNA_EXPRESSION &&
                geneticProfile.getDatatype().equals("Z-SCORE") &&
                geneticProfile.getAllOtherMetadataFields().getProperty("source_stable_id") != null)) {
            geneticProfileLink = createGeneticProfileLink(geneticProfile);
        }

        // For GSVA profiles, we want to check that the version in the meta file is
        // the same as the version of the gene sets in the database (genesets_info table).
        if (geneticProfile.getGeneticAlterationType() == GeneticAlterationType.GENESET_SCORE) {
            validateGenesetProfile(geneticProfile, file);
        }

        if (geneticProfile.getGeneticAlterationType() == GeneticAlterationType.GENERIC_ASSAY) {
            validateGenericAssay(geneticProfile, file);
            geneticProfile.setGenericAssayType(geneticProfile.getOtherMetaDataField("generic_assay_type"));
            // if genericAssayType is TREATMENT_RESPONSE, validate if pivotThreshold and sortOrder exist
            if (geneticProfile.getGenericAssayType().equals("TREATMENT_RESPONSE")) {
                validateTreatmentResponse(geneticProfile, file);
            }
            // add pivotThreshold and sortOrder if exist
            if (geneticProfile.getOtherMetaDataField("pivot_threshold_value") != null) {
                geneticProfile.setPivotThreshold(Float.parseFloat(geneticProfile.getOtherMetaDataField("pivot_threshold_value")));
            }
            if (geneticProfile.getOtherMetaDataField("value_sort_order") != null) {
                geneticProfile.setSortOrder(geneticProfile.getOtherMetaDataField("value_sort_order"));
            }
        }

        // add new genetic profile
        // addGeneticProfile() assigns the internal id on this instance before the INSERT and
        // caches this same instance, so geneticProfile.getGeneticProfileId() is authoritative
        // from here on. No read-back is needed, which also avoids the ClickHouse Cloud
        // (SharedMergeTree) window where a SELECT following an INSERT on another connection
        // may still return a stale snapshot.
        DaoGeneticProfile.addGeneticProfile(geneticProfile);

        // add genetic profile link if set
        if (geneticProfileLink != null) {
            // Set `REFERRING_GENETIC_PROFILE_ID`
            geneticProfileLink.setReferringGeneticProfileId(geneticProfile.getGeneticProfileId());
            DaoGeneticProfileLink.addGeneticProfileLink(geneticProfileLink);
        }

        return geneticProfile;
    }

    private static GeneticProfileLink createGeneticProfileLink(GeneticProfile geneticProfile) {
        GeneticProfileLink geneticProfileLink = new GeneticProfileLink();

        // Set `REFERRED_GENETIC_PROFILE_ID`
        String referredGeneticProfileStableId = parseStableId(geneticProfile.getAllOtherMetadataFields(), "source_stable_id");
        if (referredGeneticProfileStableId == null) {
            throw new RuntimeException("'source_stable_id' is required in meta file for " + geneticProfile.getStableId());
        }
        GeneticProfile referredGeneticProfile = DaoGeneticProfile.getGeneticProfileByStableId(referredGeneticProfileStableId);
        if (referredGeneticProfile == null) {
            // Fallback: the referred profile may not be in the cache yet
            DaoGeneticProfile.reCache();
            referredGeneticProfile = DaoGeneticProfile.getGeneticProfileByStableId(referredGeneticProfileStableId);
        }
        if (referredGeneticProfile == null) {
            throw new RuntimeException("Referred genetic profile not found: "
                    + referredGeneticProfileStableId
                    + " (required by source_stable_id in meta file for "
                    + geneticProfile.getStableId() + ")");
        }
        geneticProfileLink.setReferredGeneticProfileId(referredGeneticProfile.getGeneticProfileId());

        // Decide reference type
        // In the future with other types of genetic profile links, this should be configurable in the meta file.
        String referenceType;
        if (Arrays.asList("P-VALUE", "Z-SCORE").contains(geneticProfile.getDatatype())) {
            referenceType = "STATISTIC";
        } else if (geneticProfile.getDatatype().equals("GSVA-SCORE")) {
            referenceType = "AGGREGATION";
        } else {
            // not expected but might be useful for future genetic profile links
            throw new RuntimeException("Unknown datatype '" + geneticProfile.getDatatype() + "' in meta file for " + geneticProfile.getStableId());
        }
        // Set `REFERENCE_TYPE`
        geneticProfileLink.setReferenceType(referenceType);

        return geneticProfileLink;
    }

    private static void validateGenesetProfile(GeneticProfile geneticProfile, File file) throws DaoException {
        String genesetVersion = DaoInfo.getGenesetVersion();

        // TODO Auto-generated method stub

        // Check if version is present in database
        if (genesetVersion == null) {
            throw new RuntimeException("Attempted to import GENESET_SCORE data, but all gene set tables are empty.\n"
            + "Please load gene sets with ImportGenesetData.pl first. See:\n"
            + "https://github.com/cBioPortal/cbioportal/blob/master/docs/Import-Gene-Sets.md\n");

            // Check if version is present in meta file
        } else if (geneticProfile.getOtherMetaDataField("geneset_def_version") == null) {
            throw new RuntimeException("Missing geneset_def_version property in '" + file.getPath() + "'. This version must be "
            + "the same as the gene set version loaded with ImportGenesetData.pl .");

            // Check if version is same as database version
        } else if (!geneticProfile.getOtherMetaDataField("geneset_def_version").equals(genesetVersion)) {
            throw new RuntimeException("'geneset_def_version' property (" + geneticProfile.getOtherMetaDataField("geneset_def_version") +
            ") in '" + file.getPath() + "' differs from database version (" + genesetVersion + ").");
        }

        // Prevent p-value profile to show up as selectable genomic profile
        if (geneticProfile.getDatatype().equals("P-VALUE")) {
            geneticProfile.setShowProfileInAnalysisTab(false);
        }
    }

    /**
     * Check whether required columns for assay response data were defined in the meta file
     */
    private static void validateTreatmentResponse(GeneticProfile geneticProfile, File file) {
        if (geneticProfile.getOtherMetaDataField("pivot_threshold_value") == null)
            throw new RuntimeException("Missing `pivot_threshold_value` property in '" + file.getPath() + "' meta data file.");

        if (geneticProfile.getOtherMetaDataField("value_sort_order") == null)
            throw new RuntimeException("Missing `value_sort_order` property in '" + file.getPath() + "' meta data file.");
    }

    /**
     * Check whether required columns for assay response data were defined in the meta file
     */
    private static void validateGenericAssay(GeneticProfile geneticProfile, File file) {
        if (geneticProfile.getOtherMetaDataField("generic_assay_type") == null)
            throw new RuntimeException("Missing `generic_assay_type` property in '" + file.getPath() + "' meta data file.");
    }
    
    /**
     * Load a GeneticProfile from a description file.
     *
     * @author Ethan Cerami
     * @author Arthur Goldberg goldberg@cbio.mskcc.org
     *
     * @param file
     *           A handle to a description of the genetic profile, i.e., a
     *           'description' or 'meta' file.
     * @return an instantiated GeneticProfile
     * @throws IOException
     *            if the description file cannot be read
     * @throws DaoException
     */
    public static GeneticProfile loadGeneticProfileFromMeta(File file) throws IOException, DaoException {
        Properties properties = new TrimmedProperties();
        properties.load(new FileInputStream(file));
        // when loading cancer studies and their profiles from separate files,
        // use the cancer_study_identifier as a unique id for each study.
        // this was called the "cancer_type_id" previously.
        // eventually, it won't be needed when studies are loaded by a connected client that
        // knows its study_id in its state
        String cancerStudyIdentifier = properties.getProperty("cancer_study_identifier");
        if (cancerStudyIdentifier == null) {
            throw new IllegalArgumentException("cancer_study_identifier is not specified.");
        }
        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyIdentifier);
        if (cancerStudy == null) {
            throw new IllegalArgumentException("cancer study identified by cancer_study_identifier " + cancerStudyIdentifier + " not found in dbms.");
        }
        String stableId = parseStableId(properties, "stable_id");
        String profileName = properties.getProperty("profile_name");
        String profileDescription = properties.getProperty("profile_description");
        String geneticAlterationTypeString = properties.getProperty("genetic_alteration_type");
        String datatype = properties.getProperty("datatype");
        if (profileName == null) {
            profileName = geneticAlterationTypeString;
        }
        if (profileDescription == null) {
            profileDescription = geneticAlterationTypeString;
        }
        if (geneticAlterationTypeString == null) {
            throw new IllegalArgumentException("genetic_alteration_type is not specified.");
        } else if (datatype == null) {
            datatype = "";
        }
        boolean showProfileInAnalysisTab = true;
        String showProfileInAnalysisTabStr = properties.getProperty("show_profile_in_analysis_tab");
        if (showProfileInAnalysisTabStr != null && showProfileInAnalysisTabStr.equalsIgnoreCase("FALSE")) {
            showProfileInAnalysisTab = false;
        }
        String patientLevelStr = properties.getProperty("patient_level");
        boolean patientLevel = patientLevelStr != null && patientLevelStr.equalsIgnoreCase("TRUE");

        profileDescription = profileDescription.replaceAll("\t", " ");
        GeneticAlterationType alterationType = GeneticAlterationType.valueOf(geneticAlterationTypeString);
        GeneticProfile geneticProfile = new GeneticProfile();
        geneticProfile.setCancerStudyId(cancerStudy.getInternalId());
        geneticProfile.setStableId(stableId);
        geneticProfile.setProfileName(profileName);
        geneticProfile.setProfileDescription(profileDescription);
        geneticProfile.setGeneticAlterationType(alterationType);
        geneticProfile.setDatatype(datatype);
        geneticProfile.setShowProfileInAnalysisTab(showProfileInAnalysisTab);
        geneticProfile.setTargetLine(properties.getProperty("target_line"));
        geneticProfile.setOtherMetadataFields(properties);
        geneticProfile.setPatientLevel(patientLevel);
        return geneticProfile;
    }

    private static String parseStableId(Properties properties, String stableIdPropName) {
        String stableId = properties.getProperty(stableIdPropName);
        if (stableId == null) {
            throw new IllegalArgumentException("stable_id is not specified.");
        }
        String cancerStudyIdentifier = properties.getProperty("cancer_study_identifier");
        //automatically add the cancerStudyIdentifier in front of stableId (since the rest of the
        //code still relies on this - TODO: this can be removed once the rest of the backend and frontend code
        //stop assuming cancerStudyIdentifier to be part of stableId):
        if (!stableId.startsWith(cancerStudyIdentifier + "_")) {
            stableId = cancerStudyIdentifier + "_" + stableId;
        }
        return stableId;
    }

    public static String loadGenePanelInformation(File file) throws Exception {
        Properties properties = new TrimmedProperties();
        properties.load(new FileInputStream(file));
        return properties.getProperty("gene_panel");
    }

    /**
     * Gets the information of "variant_classification_filter" in the file, if it exists. Otherwise, it
     * returns null. "variant_classification_filter" can be used in the mutation meta file to specify
     * which types of mutations want to be filtered.
     *
     * @param file
     * @return a string with the types of mutations that should be filtered, comma-separated.
     * @throws Exception
     */
    public static Set<String> getVariantClassificationFilter(File file) throws Exception {
        Properties properties = new TrimmedProperties();
        properties.load(new FileInputStream(file));
        String variantClassificationFilter = properties.getProperty("variant_classification_filter");
        if (variantClassificationFilter != null) {
                Set<String> filteredMutations = new HashSet<String>();
                for (String mutation : (Arrays.asList(variantClassificationFilter.split(",")))) {
                        mutation = mutation.trim();
                        filteredMutations.add(mutation);
                    }
                return filteredMutations;
        } else {
            return null;
        }
    }

    /**
     * Returns a set of namespaces if present in the maf metafile.
     * The defined namespaces will be used to construct a JSON doc
     * containing namespace => {  key,value pairs  } to import into
     * the `mutation` database as a sample-level variant annotation.
     * @param file
     * @return
     * @throws Exception
     */
    public static Set<String> getNamespaces(File file) throws Exception {
        Properties properties = new TrimmedProperties();
        properties.load(new FileInputStream(file));
        String value = properties.getProperty("namespaces");
        return value != null ?
            Arrays.stream(value.split(","))
                .map(String::trim)
                .collect(Collectors.toSet())
        :
            null;
    }
}
