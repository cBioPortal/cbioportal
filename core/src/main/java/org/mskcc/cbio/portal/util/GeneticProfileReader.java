/*
 * Copyright (c) 2015 - 2016 Memorial Sloan-Kettering Cancer Center.
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

package org.mskcc.cbio.portal.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.GeneticAlterationType;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.model.GeneticProfileLink;
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
            if (!existingGeneticProfile.getDatatype().equals("MAF")) {
               // the dbms already contains a GeneticProfile with the file's stable_id. This scenario is not supported
               // anymore, so throw error telling user to remove existing profile first:
               throw new RuntimeException("Error: genetic_profile record found with same Stable ID as the one used in your data:  "
                       + existingGeneticProfile.getStableId() + ". Remove the existing genetic_profile record first.");
            } else if (geneticProfile.getDatatype().equals("FUSION")) {
                geneticProfile.setGeneticProfileId(existingGeneticProfile.getGeneticProfileId());
                return geneticProfile;
            } else {
                // For mutation data only we can have multiple files with the same genetic_profile.
                // There is a constraint in the mutation database table to prevent duplicated data
                // If this constraint is hit (mistakenly importing the same maf twice) MySqlBulkLoader will throw an exception
                //
                // make an object combining the pre-existing profile with the file-specific properties of the current file
                GeneticProfile gp = new GeneticProfile(existingGeneticProfile);
                gp.setTargetLine(gp.getTargetLine());
                gp.setOtherMetadataFields(gp.getAllOtherMetadataFields());
                return gp;
            }
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

        // add new genetic profile
        DaoGeneticProfile.addGeneticProfile(geneticProfile);
        	
        // add genetic profile link if set
        if (geneticProfileLink != null) {
            // Set `REFERRING_GENETIC_PROFILE_ID`
        	int geneticProfileId = DaoGeneticProfile.getGeneticProfileByStableId(geneticProfile.getStableId()).getGeneticProfileId();
            geneticProfileLink.setReferringGeneticProfileId(geneticProfileId);
            DaoGeneticProfileLink.addGeneticProfileLink(geneticProfileLink);
        }
        
        // Get ID
        GeneticProfile gp = DaoGeneticProfile.getGeneticProfileByStableId(geneticProfile.getStableId());
        geneticProfile.setGeneticProfileId(gp.getGeneticProfileId());
        geneticProfile.setReferenceGenomeId(gp.getReferenceGenomeId());
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
        String genomeBuild = properties.getProperty("reference_genome_id");
        int referenceGenomeId = 1;
        if (genomeBuild != null && !genomeBuild.isEmpty()) {
            referenceGenomeId = DaoReferenceGenome.getReferenceGenomeByName(genomeBuild);
        }

        
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
        geneticProfile.setReferenceGenomeId(referenceGenomeId);
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
        // Workaround to import fusion data as mutation genetic profile. This way fusion meta file can contain 'stable_id: fusion'.
        // The validator will check for 'stable_id: fusion', and this section in the importer
        // will convert it to 'stable_id: mutations'. See https://github.com/cBioPortal/cbioportal/pull/2506
        // TODO: This should be removed when other parts of cBioPortal have implemented support for a separate fusion profile".
        if (stableId.equals(cancerStudyIdentifier + "_fusion")) {
            String newStableId = cancerStudyIdentifier + "_mutations";
            GeneticProfile existingGeneticProfile = DaoGeneticProfile.getGeneticProfileByStableId(newStableId);
            if (existingGeneticProfile == null) {
                throw new IllegalArgumentException("Wrong order: FUSION data should be loaded after MUTATION data");
            }
            stableId = newStableId;
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
}
